package com.nehms.cardGame.handle;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.nehms.cardGame.controllers.liardgame.LiarGamePlay;
import com.nehms.cardGame.entities.Card;
import com.nehms.cardGame.entities.GameState;
import com.nehms.cardGame.entities.Player;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class Communication extends TextWebSocketHandler {

	private boolean gameIsStillPlaying = false;
	private List<WebSocketSession> sessions = new ArrayList<>();
	private List<Player> players = new ArrayList<>();
	private static int num = 1;
	private static final int MAX_PLAYERS = 5;
	private static final String PLAYER_NAME_KEY = "personName";
	private static final String ADMIN_NAME = "admin";
	private static int nberOfOK = 0;
	private List<Card> cards;
	private LiarGamePlay gamePlay = new LiarGamePlay(cards, players);
	private boolean gameCanStart = false;
	private GameState checkGamestate = GameState.PLAY_CARD;

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
			// a revoir
			if (checkGamestate.equals(GameState.PLAY_CARD)) {
				// letPlay(personName, message);
			} else if (checkGamestate.equals(GameState.CONTESTAITON)) {
				// Contradiction(personName, message);
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

	public void broadcastMessage(String message) throws Exception {
		for (WebSocketSession session : sessions) {
			if (session.isOpen()) {
				session.sendMessage(new TextMessage(message));
			}
		}
	}

	public void adminSendMessage(String message) throws Exception {
		log.info("Admin dit : {}", message);
		broadcastMessage(ADMIN_NAME + ": " + message);
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

}
