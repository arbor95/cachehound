package de.cachehound.comparators;

import java.util.Comparator;

import CacheWolf.beans.CacheHolder;

public class OwnerComparator implements Comparator<CacheHolder> {
	@Override
	public int compare(CacheHolder o1, CacheHolder o2) {
		return o1.getCacheOwnerSimplified().compareTo(
				o2.getCacheOwnerSimplified());
	}
}
