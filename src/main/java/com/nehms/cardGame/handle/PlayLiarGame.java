package com.nehms.cardGame.handle;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.nehms.cardGame.controllers.Play;
import com.nehms.cardGame.controllers.liardgame.LiarGamePlay;
import com.nehms.cardGame.entities.Card;
import com.nehms.cardGame.entities.GameState;
import com.nehms.cardGame.entities.GameStateObject;
import com.nehms.cardGame.entities.Pattern;
import com.nehms.cardGame.entities.Player;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PlayLiarGame implements Play {

	private boolean gameIsStillPlaying = false;
	private List<Card> cardsPlayed = new ArrayList<>();
	private Player currentPlayer = new Player(null);
	private int i = 0;
	private int currentIndex = 0;
	private Card currentCard = new Card();
	private Pattern currentPattern = null;
	private List<WebSocketSession> sessions = new ArrayList<>();
	private List<Player> players = new ArrayList<>();
	private static int num = 1;
	private static final int MAX_PLAYERS = 5;
	private static final String PLAYER_NAME_KEY = "personName";
	private static final String ADMIN_NAME = "admin";
	private Player admin;
	private static int nberOfOK = 0;
	private List<Card> cards;
	private LiarGamePlay gamePlay = new LiarGamePlay(cards, players);
	private boolean gameCanStart = false;
	private List<GameStateObject> infos = new ArrayList<>();
	private GameState checkGamestate = GameState.PLAY_CARD;
	private boolean contradictok = false;
	private Player currentContradictPlayer = new Player(null);
	int currentContestIndex = 0;
	private Communication communication;

	@Override
	public void letPlay(String player, TextMessage message) throws Exception {
		if (infos.isEmpty()) {
			log.error("La liste des infos est vide. Impossible de jouer.");
			return;
		}

		GameStateObject checkObject = infos.get(infos.size() - 1);
		String j = checkObject.getCurrentPlayer().getNamePlayer();
		String okPlayer = null;

		if (j != null && j.length() > 0) {
			okPlayer = j.substring(0, j.length() - 1) + (i + 1);
			System.out.println(okPlayer);
		}

		currentPlayer.setNamePlayer(okPlayer);

		if (checkGamestate.equals(GameState.PLAY_CARD) && player.equals(okPlayer)) {
			boolean playerFound = false;
			for (int k = 0; k < players.size(); k++) {
				if ((okPlayer).equals(players.get(k).getNamePlayer())) {
					currentIndex = k;
					playerFound = true;
					break;
				}
			}

			if (!playerFound) {
				log.error("Le joueur {} n'a pas été trouvé dans la liste des joueurs.", okPlayer);
				return;
			}

			String[] value = communication.getReponse(message.getPayload());
			if (value.length != 2) {
				log.error("La réponse du joueur n'est pas valide: {}", message.getPayload());
			communication.adminSendMessage("Réponse invalide. Veuillez fournir une réponse valide.");
				return;
			}

			try {
				int valueNumber = Integer.parseInt(value[0].trim());
				Pattern valuePattern = Pattern.valueOf(value[1].trim().toUpperCase());

				currentPattern = valuePattern;
				gamePlay.playOneCard(valueNumber, players.get(currentIndex), cardsPlayed, currentCard, valuePattern);

				currentPattern = valuePattern;

				for (Card card : players.get(currentIndex).getHand()) {
					System.out.println(card + "\n");
				}
			} catch (NumberFormatException e) {
				log.error("Le nombre de la carte n'est pas valide: {}", value[0].trim(), e);
				communication.adminSendMessage("Nombre de carte invalide. Veuillez fournir une carte valide.");
				return;
			} catch (IllegalArgumentException e) {
				log.error("Le motif de la carte n'est pas valide: {}", value[1].trim(), e);
				communication.adminSendMessage("Motif de carte invalide. Veuillez fournir une carte valide.");
				return;
			}

			if (currentIndex == MAX_PLAYERS) {
				i = 0;
			} else {
				++i;
			}

			checkGamestate = GameState.CONTESTAITON;
			communication.adminSendMessage("C'est actuellement le tour du joueur_" + (int) (i + 1));

			// Annonce de la phase de contestation
			communication.adminSendMessage("Est-ce que quelqu'un veut contester ? ");
			communication.adminSendMessage("Vous avez 20 secondes pour contester.");
			Thread.sleep(20000); // Attendre 20 secondes pour la contestation

			// Si personne n'a contesté, passer au tour suivant
			if (!contradictok) {
				checkGamestate = GameState.PLAY_CARD;
				communication.adminSendMessage("Personne n'a contesté. Passons au tour suivant.");
			}
		} else if (checkGamestate.equals(GameState.PLAY_CARD) && !player.equals(okPlayer)) {
			if (currentIndex == MAX_PLAYERS) {
				communication.adminSendMessage("Ce n'est pas ton tour, c'est celui du joueur_0");
			} else {
				communication.adminSendMessage("Ce n'est pas votre tour, c'est celui du joueur_" + (int) (currentIndex + 1));
			}
			log.warn("Ce n'est pas le tour du joueur: {}", player);
		}
	}

	@Override
	public void Contradiction(String player, TextMessage message) throws Exception {
		if (checkGamestate == GameState.CONTESTAITON) {
			if (message.getPayload().equalsIgnoreCase("moi")) {
				currentContradictPlayer = new Player(player);
				log.info("Le joueur {} conteste.", player);
				communication.adminSendMessage(player + " conteste.");

				for (int k = 0; k < players.size(); k++) {
					if ((player).equals(players.get(k).getNamePlayer())) {
						currentContestIndex = k;
					}

					gamePlay.contradict(players.get(currentContestIndex), players.get(currentIndex), currentCard,
							currentPattern, cardsPlayed);

					contradictok = true;
				}
				checkGamestate = GameState.PLAY_CARD; // Mettre à jour l'état du jeu

				for (Player f : players) {
					System.out.println(f.getNamePlayer() + " j'ai donc " + f.getHand().size());
				}
			}
		}
	}

}
