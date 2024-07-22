package com.nehms.cardGame.controllers;
import com.nehms.cardGame.entities.*;
import java.util.List;

public interface Configurer {

    void createCards(List<Card> cards);

    void mixCards(List<Card> cards);

    void removeCards(Card card, List<Card> cards);

    void distribute(List<Card> cards, List<Player> players);

}
