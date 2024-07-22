package com.nehms.cardGame.controllers.liardgame;

import com.nehms.cardGame.controllers.*;
import com.nehms.cardGame.exceptions.*;
import com.nehms.cardGame.entities.*;

import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

public class LiarGamePlayerCreator implements PlayerCreator {

	private static final Logger log = Logger.getLogger(LiarGamePlayerCreator.class.getName());

	@Override
	public void create(List<Player> players) throws EmptyListOfCardsException {
		
		if (players==null) {
			throw new EmptyListOfCardsException("the players List is null");
		}

		Scanner clavier = new Scanner(System.in);
		int nbreJoueur;

		log.info("Entre le nombre de joueurs => ");
		nbreJoueur = clavier.nextInt();

		while (nbreJoueur <= 1) {

			log.info("Entre le nombre de joueurs => ");
			nbreJoueur = clavier.nextInt();
		}

		for (int i = 0; i < nbreJoueur; i++) {
			players.add(new Player("Joueur_"+(i + 1)));
		}
		clavier.close();
	}
}
