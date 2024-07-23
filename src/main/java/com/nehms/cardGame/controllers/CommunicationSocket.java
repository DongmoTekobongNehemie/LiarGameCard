package com.nehms.cardGame.controllers;

import com.nehms.cardGame.entities.Player;

public interface CommunicationSocket {
	
		public void broadcastMessage(String message) throws Exception;
		
		public void adminSendMessage(String message) throws Exception;
		
		public void adminSendMessageToPlayer(Player player) throws Exception;
}
