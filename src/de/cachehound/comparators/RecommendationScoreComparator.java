package de.cachehound.comparators;

import java.util.Comparator;

import CacheWolf.beans.CacheHolder;

public class RecommendationScoreComparator implements Comparator<CacheHolder> {
	@Override
	public int compare(CacheHolder o1, CacheHolder o2) {
		if (o1.getWayPoint().startsWith("OC")) {
			if (o2.getWayPoint().startsWith("OC")) {
				return o1.recommendationScore - o2.recommendationScore;
			} else {
				return -1;
			}
		} else {
			if (o2.getWayPoint().startsWith("OC")) {
				return 1;
			} else {
				return 0;
			}
		}
	}
}
