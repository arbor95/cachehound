package de.cachehound.comparators;

import java.util.Comparator;

import CacheWolf.beans.CacheHolder;

public class BearingComparator implements Comparator<CacheHolder> {
	@Override
	public int compare(CacheHolder o1, CacheHolder o2) {
		//FIXME: is this really the way bearings should be compared?
		return o1.getBearingAsString().compareTo(o2.getBearingAsString());
	}
}
