package com.nehms.cardGame.entities;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private String namePlayer;
    private List<Card> hand = new ArrayList<>();
    

    public Player(String name) {
        this.namePlayer = name;
    }

    public String getNamePlayer() {
        return namePlayer;
    }

    public void setNamePlayer(String namePlayer) {
        this.namePlayer = namePlayer;
    }

    @Override
    public String toString() {
        return "Player{namePlayer='" + namePlayer + "'}";
    }

	public List<Card> getHand() {
		return hand;
	}

	public void setHand(List<Card> hand) {
		this.hand = hand;
	}
}