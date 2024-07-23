package com.nehms.cardGame.controllers.liardgame;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import com.nehms.cardGame.controllers.Configurer;
import com.nehms.cardGame.controllers.Playable;
import com.nehms.cardGame.entities.Card;
import com.nehms.cardGame.entities.Pattern;
import com.nehms.cardGame.entities.Player;
import com.nehms.cardGame.exceptions.EmptyListOfCardsException;
import com.nehms.cardGame.handle.GameHandler;

public class LiarGamePlay implements Playable {

	private static final Logger log = Logger.getLogger(LiarGamePlay.class.getName());
	List<Player> players;
	private GameHandler gameHandler;

	public LiarGamePlay(List<Card> cards, List<Player> players, GameHandler gameHandler) {
		this.players = players;
		this.gameHandler = gameHandler;
	}

	@Override
	public void playOneCard(int number, Player joueur, List<Card> cardsPlayed, Card currentCard, Pattern avis) {

		cardsPlayed.add(joueur.getHand().get(number));
		currentCard.setPattern(joueur.getHand().get(number).getPattern());
		currentCard.setNumber(joueur.getHand().get(number).getNumber());
		joueur.getHand().remove(number);

		log.log(Level.FINE, "=> Le {0} dit {1} \n", new Object[] { joueur.getNamePlayer(), avis });
		log.log(Level.FINE, "La carte qui a ete jouer est la carte => {0} \n", currentCard);

		int i = 1;
		log.log(Level.FINE, "La main du {0} est désormais \n", joueur.getNamePlayer());
		for (Card carte : joueur.getHand()) {
			log.log(Level.FINE, "{0} - {1}", new Object[] { i, carte });
			i++;
		}
	}
	
	@Override
	public void contradict(Player contestPlayer, Player player, Card currentCard, Pattern pattern,
			List<Card> cardsPlayed) throws EmptyListOfCardsException {
		if (player == null || players == null || currentCard == null || pattern == null || cardsPlayed == null) {
			throw new EmptyListOfCardsException("something is null in the parameter");
		}

		if (currentCard.getPattern().equals(pattern)) {
			contestPlayer.getHand().addAll(cardsPlayed);
			cardsPlayed.clear();
		} else {
			player.getHand().addAll(cardsPlayed);
			cardsPlayed.clear();
		}
	}

	private static List<Card> cards;

	@Override
	public void ConfigurePlay() throws EmptyListOfCardsException {
		Configurer configurer = new LiarGameConfigurer();
		cards = new ArrayList<>();
		configurer.createCards(cards);
		configurer.mixCards(cards);

		if (players.isEmpty()) {
			throw new EmptyListOfCardsException("No players available to play the game.");
		}

		// Exclude admin from players list before distribution
		List<Player> playersWithoutAdmin = new ArrayList<>(players);
		playersWithoutAdmin.removeIf(player -> player.getNamePlayer().equals("admin"));

		configurer.distribute(cards, playersWithoutAdmin);
	}
}
