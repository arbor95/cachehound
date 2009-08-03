package de.cachehound.comparators;

import java.util.Comparator;

import CacheWolf.beans.CacheHolder;

public class HiddenComparator implements Comparator<CacheHolder> {
	@Override
	public int compare(CacheHolder o1, CacheHolder o2) {
		//FIXME: Datum als String... unschoen.
		return o1.getDateHidden().compareTo(o2.getDateHidden());
	}
}
