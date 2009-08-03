package de.cachehound.comparators;

import java.util.Comparator;

import CacheWolf.beans.CacheHolder;

public class WaypointComparator implements Comparator<CacheHolder> {
	@Override
	public int compare(CacheHolder o1, CacheHolder o2) {
		// We sort...
		// * first by the two-letter-prefix
		// (so that GC caches and OC caches aren't mixed)
		// * then by the _length_ of the id
		// (so that GCFFFF sorts before GC10000)
		// * then the rest of the id
		int d1 = o1.getWayPoint().substring(0, 2).compareTo(
				o2.getWayPoint().substring(0, 2));
		if (d1 != 0) {
			return d1;
		}
		int d2 = o1.getWayPoint().length() - o2.getWayPoint().length();
		if (d2 != 0) {
			return d2;
		}
		int d3 = o1.getWayPoint().substring(2).compareTo(
				o2.getWayPoint().substring(2));
		return d3;
	}
}
