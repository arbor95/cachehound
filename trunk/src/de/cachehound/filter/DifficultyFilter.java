package de.cachehound.filter;

import java.util.Set;

import de.cachehound.beans.ICacheHolder;
import de.cachehound.types.Difficulty;

public class DifficultyFilter extends AbstractEnumBasedFilter<Difficulty> {
	protected DifficultyFilter(Set<Difficulty> mask) {
		super(mask);
	}

	@Override
	protected Difficulty getProperty(ICacheHolder ch) {
		return ch.getDifficulty();
	}
	
	@Override
	public String toString() {
		return "Difficulty is one of " + super.toString();
	}
}
