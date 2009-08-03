package de.cachehound.comparators;

import java.util.Comparator;

import CacheWolf.beans.CacheHolder;

public class HasSolverComparator implements Comparator<CacheHolder> {
	@Override
	public int compare(CacheHolder o1, CacheHolder o2) {
		if (o1.hasSolver() && !o2.hasSolver()) {
			return -1;
		} else if (o1.hasSolver() == o2.hasSolver()) {
			return 0;
		} else {
			return 1;
		}
	}
}
