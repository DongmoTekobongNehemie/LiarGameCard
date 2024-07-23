package com.nehms.cardGame.controllers;
import com.nehms.cardGame.entities.*;
import com.nehms.cardGame.exceptions.EmptyListOfCardsException;

import java.util.List;

public interface PlayerCreator {

    void create(List<Player> players) throws EmptyListOfCardsException;
    
}
