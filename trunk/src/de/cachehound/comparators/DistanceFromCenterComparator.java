package de.cachehound.comparators;

import java.util.Comparator;

import CacheWolf.beans.CacheHolder;

public class DistanceFromCenterComparator implements Comparator<CacheHolder> {
	@Override
	public int compare(CacheHolder o1, CacheHolder o2) {
		double difference = o1.getKilom() - o2.getKilom();
		// Yes, no epsilon here.
		if (difference < 0) {
			return -1;
		} else if (difference == 0) {
			return 0;
		} else {
			return 1;
		}
	}
}
