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
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Card other = (Card) obj;
		return Objects.equals(number, other.number) && pattern == other.pattern;
	}

	@Override
	public int hashCode() {
		return Objects.hash(number, pattern);
	}

}
