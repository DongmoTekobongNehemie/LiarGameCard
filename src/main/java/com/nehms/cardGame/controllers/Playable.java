package com.nehms.cardGame.controllers;

import java.util.List;
import com.nehms.cardGame.entities.Card;
import com.nehms.cardGame.entities.Pattern;
import com.nehms.cardGame.entities.Player;
import com.nehms.cardGame.exceptions.EmptyListOfCardsException;

public interface Playable {

	void ConfigurePlay() throws EmptyListOfCardsException;

	void playOneCard(int number, Player joueur, List<Card> cardsPlayed, Card currentCard, Pattern pattern);

	public void contradict(Player contestPlayer, Player player, Card currentCard, Pattern pattern,
			List<Card> cardsPlayed) throws EmptyListOfCardsException;

}
