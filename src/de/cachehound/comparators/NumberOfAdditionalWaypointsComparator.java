package de.cachehound.comparators;

import java.util.Comparator;

import CacheWolf.beans.CacheHolder;

public class NumberOfAdditionalWaypointsComparator implements
		Comparator<CacheHolder> {
	@Override
	public int compare(CacheHolder o1, CacheHolder o2) {
		return o1.getAddiWpts().size() - o2.getAddiWpts().size();
	}
}
