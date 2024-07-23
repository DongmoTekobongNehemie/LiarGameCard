package com.nehms.cardGame.controllers;

import org.springframework.web.socket.TextMessage;

public interface Play {
	public void letPlay(String player, TextMessage message) throws Exception;
}
