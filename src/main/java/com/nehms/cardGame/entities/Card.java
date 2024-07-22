package com.nehms.cardGame.entities;

import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Card {
	
	private Pattern pattern;
	private String number;

	@Override
	public final boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Card card)) return false;
        return pattern == card.pattern && Objects.equals(number, card.number);
	}

	@Override
	public int hashCode() {
		int result = Objects.hashCode(pattern);
		result = 31 * result + Objects.hashCode(number);
		return result;
	}
}
