package com.nehms.cardGame.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
public class GameStateObject {
	
	private Player currentPlayer;
	private Player currentContestPlayer;
	private Card currrentCard;
	private Pattern currentPattern;
	
}
