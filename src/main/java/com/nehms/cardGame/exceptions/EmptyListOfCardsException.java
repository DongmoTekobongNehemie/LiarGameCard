package com.nehms.cardGame.exceptions;

public class EmptyListOfCardsException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public EmptyListOfCardsException(String message) {
		super(message);
	}
}
