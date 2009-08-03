package de.cachehound.comparators;

import java.util.Comparator;

import CacheWolf.beans.CacheHolder;

public class HasNoteComparator implements Comparator<CacheHolder> {
	@Override
	public int compare(CacheHolder o1, CacheHolder o2) {
		if (o1.hasNote() && !o2.hasNote()) {
			return -1;
		} else if (o1.hasNote() == o2.hasNote()) {
			return 0;
		} else {
			return 1;
		}
	}
}
