package de.cachehound.comparators;

import java.util.Comparator;

import CacheWolf.beans.CacheHolder;

public class GcVoteAverageComparator implements Comparator<CacheHolder> {

	@Override
	public int compare(CacheHolder o1, CacheHolder o2) {
		double d1 = 0;
		double d2 = 0;
		if (o1.getGcVote() != null) {
			d1 = o1.getGcVote().getAverage();
		}
		if (o2.getGcVote() != null) {
			d2 = o2.getGcVote().getAverage();
		}
		return Double.compare(d2, d1);
	}

}
