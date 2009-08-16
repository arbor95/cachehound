package de.cachehound.comparators;

import java.util.Comparator;

import CacheWolf.beans.CacheHolder;

public class DifficultyComparator implements Comparator<CacheHolder> {
	@Override
	public int compare(CacheHolder o1, CacheHolder o2) {
		return o1.getDifficulty().ordinal() - o2.getDifficulty().ordinal();
		// TODO: Check if it is useful to handle the problem Difficultys extra
	}
}
