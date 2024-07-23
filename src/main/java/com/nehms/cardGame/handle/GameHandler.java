package com.nehms.cardGame.handle;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.nehms.cardGame.controllers.CommunicationSocket;
import com.nehms.cardGame.controllers.Play;
import com.nehms.cardGame.controllers.liardgame.LiarGamePlay;
import com.nehms.cardGame.entities.Card;
import com.nehms.cardGame.entities.GameState;
import com.nehms.cardGame.entities.GameStateObject;
import com.nehms.cardGame.entities.Pattern;
import com.nehms.cardGame.entities.Player;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class GameHandler extends TextWebSocketHandler implements CommunicationSocket, Play {
	
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
	private LiarGamePlay gamePlay = new LiarGamePlay(cards, players, this);
	private boolean gameCanStart = false;
	private List<GameStateObject> infos = new ArrayList<>();
	private GameState checkGamestate = GameState.PLAY_CARD;

	@jakarta.annotation.PostConstruct
	public void init() {
		admin = new Player(ADMIN_NAME);
		players.add(admin);
		log.info("Admin ajouté automatiquement à la liste des joueurs.");
		infos.add(new GameStateObject(new Player("joueur_0"), null, null, null));
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		if (players.size() - 1 >= MAX_PLAYERS) {
			session.sendMessage(new TextMessage("Le maximum de joueurs est atteint."));
			session.close(CloseStatus.POLICY_VIOLATION);
			log.info("Connexion refusée pour la session : {} car le maximum de joueurs est atteint.", session.getId());
			return;
		}

		Player player = new Player("joueur_" + num);
		sessions.add(session);
		players.add(player);

		String personName = player.getNamePlayer();
		session.getAttributes().put(PLAYER_NAME_KEY, personName);

		log.info("Connection établie sur la session : {} pour le joueur : {}", session.getId(), personName);

		++num;
		adminSendMessage("Bienvenue " + personName);

		if (players.size() - 1 == MAX_PLAYERS) {
			adminSendMessage("Est-ce que tout le monde est prêt ?");
		}

		for (Player p : players) {
			System.out.println(p);
		}
	}

	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		String personName = (String) session.getAttributes().get(PLAYER_NAME_KEY);
		String currentMessage = message.getPayload();

		log.info("{} dit : {}", personName, currentMessage);
		broadcastMessage(personName + ": " + currentMessage);

		if (currentMessage.equals("oui") && !gameCanStart) {
			++nberOfOK;
			if (nberOfOK == MAX_PLAYERS && !gameIsStillPlaying) {
				adminSendMessage("Le jeu peut désormais commencer. Bonne chance !!");
				gamePlay.ConfigurePlay();
				for (Player f : players) {
					System.out.println(f.getNamePlayer() + " j'ai " + f.getHand().size());
				}
				gameCanStart = true;
			}
		} else if (gameCanStart) {
			// Appeler letPlay une fois que tous les joueurs sont prêts
			gameIsStillPlaying = true;
			if (checkGamestate.equals(GameState.PLAY_CARD)) {
				letPlay(personName, message);
			} else if (checkGamestate.equals(GameState.CONTESTAITON)) {
				Contradiction(personName, message);
			}
		}
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		log.info("Exception survenue : {}", exception.getMessage(), session.getId());
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
		sessions.remove(session);
		String personName = (String) session.getAttributes().get(PLAYER_NAME_KEY);
		Player playerToRemove = null;

		for (Player player : players) {
			if (personName != null && personName.equals(player.getNamePlayer())) {
				playerToRemove = player;
				break;
			}
		}

		if (playerToRemove != null) {
			players.remove(playerToRemove);
			log.info("Connexion fermée pour la session : {} avec le nom : {} et le statut : {}", session.getId(),
					personName, closeStatus.getCode());
			broadcastMessage("Déconnexion : " + personName);
		}

		for (Player p : players) {
			System.out.println(p);
		}
	}

	@Override
	public boolean supportsPartialMessages() {
		return false;
	}

	@Override
	public void broadcastMessage(String message) throws Exception {
		for (WebSocketSession session : sessions) {
			if (session.isOpen()) {
				session.sendMessage(new TextMessage(message));
			}
		}
	}

	@Override
	public void adminSendMessage(String message) throws Exception {
		log.info("Admin dit : {}", message);
		broadcastMessage(ADMIN_NAME + ": " + message);
	}
	
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

			String[] value = getReponse(message.getPayload());
			if (value.length != 2) {
				log.error("La réponse du joueur n'est pas valide: {}", message.getPayload());
				adminSendMessage("Réponse invalide. Veuillez fournir une réponse valide.");
				return;
			}

			try {
				int valueNumber = Integer.parseInt(value[0].trim());
				Pattern valuePattern = Pattern.valueOf(value[1].trim().toUpperCase());

				currentPattern = valuePattern;
				gamePlay.playOneCard(valueNumber, players.get(currentIndex), cardsPlayed, currentCard, valuePattern);
				
				currentPattern = valuePattern;
				
				adminSendMessageToPlayer(currentContradictPlayer);
				
				for (Card card : players.get(currentIndex).getHand()) {
					System.out.println(card + "\n");
				}
			} catch (NumberFormatException e) {
				log.error("Le nombre de la carte n'est pas valide: {}", value[0].trim(), e);
				adminSendMessage("Nombre de carte invalide. Veuillez fournir une carte valide.");
				return;
			} catch (IllegalArgumentException e) {
				log.error("Le motif de la carte n'est pas valide: {}", value[1].trim(), e);
				adminSendMessage("Motif de carte invalide. Veuillez fournir une carte valide.");
				return;
			}

			if (currentIndex == MAX_PLAYERS) {
				i = 0;
			} else {
				++i;
			}

			checkGamestate = GameState.CONTESTAITON;
			this.adminSendMessage("C'est actuellement le tour du joueur_" + (int) (i + 1));

			// Annonce de la phase de contestation
			adminSendMessage("Est-ce que quelqu'un veut contester ? ");
			adminSendMessage("Vous avez 20 secondes pour contester.");
			Thread.sleep(20000); // Attendre 20 secondes pour la contestation

			// Si personne n'a contesté, passer au tour suivant
			if (!contradictok) {
				checkGamestate = GameState.PLAY_CARD;
				this.adminSendMessage("Personne n'a contesté. Passons au tour suivant.");
			}
		} else if (checkGamestate.equals(GameState.PLAY_CARD) && !player.equals(okPlayer)) {
			if (currentIndex == MAX_PLAYERS) {
				this.adminSendMessage("Ce n'est pas ton tour, c'est celui du joueur_0");
			} else {
				this.adminSendMessage("Ce n'est pas votre tour, c'est celui du joueur_" + (int) (currentIndex + 1));
			}
			log.warn("Ce n'est pas le tour du joueur: {}", player);
		}
	}

	public String[] getReponse(String reponse) {
		String[] rep = null;
		if (checkGamestate.equals(GameState.PLAY_CARD)) {
			rep = reponse.split("-");
		}
		if (checkGamestate.equals(GameState.CONTESTAITON)) {
			rep = new String[] { reponse };
		}
		return rep;
	}

	private boolean contradictok = false;
	private Player currentContradictPlayer = new Player(null);
	int currentContestIndex = 0;

	private void Contradiction(String player, TextMessage message) throws Exception {
		if (checkGamestate == GameState.CONTESTAITON) {
			if (message.getPayload().equalsIgnoreCase("moi")) {
				currentContradictPlayer = new Player(player);
				log.info("Le joueur {} conteste.", player);
				adminSendMessage(player + " conteste.");

				for (int k = 0; k < players.size(); k++) {
					if ((player).equals(players.get(k).getNamePlayer())) {
						currentContestIndex = k;
					}

					gamePlay.contradict(players.get(currentContestIndex), players.get(currentIndex), currentCard,
							currentPattern, cardsPlayed);
					
					adminSendMessageToPlayer(currentContradictPlayer);
					adminSendMessageToPlayer(currentPlayer);
					contradictok = true;
				}
				checkGamestate = GameState.PLAY_CARD; // Mettre à jour l'état du jeu

				for (Player f : players) {
					System.out.println(f.getNamePlayer() + " j'ai donc " + f.getHand().size());
				}
			}
		}
	}

	public String onePlayerCards(Player player) {
		StringBuilder cardsDisplay = new StringBuilder();
		for (Card card : player.getHand()) {
			cardsDisplay.append("+---------+\n");
			cardsDisplay.append("| ").append(String.format("%-2s", card.getNumber())).append("      |\n");
			cardsDisplay.append("|         |\n");
			cardsDisplay.append("|    ").append(String.format("%-2s", card.getPattern())).append("    |\n");
			cardsDisplay.append("|         |\n");
			cardsDisplay.append("|      ").append(String.format("%2s", card.getNumber())).append(" |\n");
			cardsDisplay.append("+---------+\n");
			cardsDisplay.append("\n");
		}
		return cardsDisplay.toString();
	}
	
	private WebSocketSession findSessionByPlayer(Player player) {
	    for (WebSocketSession session : sessions) {
	        String personName = (String) session.getAttributes().get(PLAYER_NAME_KEY);
	        if (personName != null && personName.equals(player.getNamePlayer())) {
	            return session;
	        }
	    }
	    return null;
	}

	@Override
	public void adminSendMessageToPlayer(Player player) throws Exception {
	    WebSocketSession session = findSessionByPlayer(player);
	    if (session != null && session.isOpen()) {
	        String message = onePlayerCards(player);
	        session.sendMessage(new TextMessage(ADMIN_NAME + ": voici l'etat de vos carte " + message));
	    } else {
	        log.warn("Session non trouvée ou fermée pour le joueur : {}", player.getNamePlayer());
	    }
	}
	

}