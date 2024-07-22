package com.nehms.cardGame.controllers;
import com.nehms.cardGame.exceptions.*;
import com.nehms.cardGame.entities.*;

import java.util.List;

public interface PlayerCreator {

    void create(List<Player> players) throws EmptyListOfCardsException;


}
