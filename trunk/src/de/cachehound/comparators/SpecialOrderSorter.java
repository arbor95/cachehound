package de.cachehound.comparators;

import java.util.Comparator;
import java.util.List;

import CacheWolf.beans.CacheHolder;

/**
 * This class sorts CacheHolder objects by the order they appear in a List.
 * CacheHolders that don't appear at all are placed at the end.
 */
public class SpecialOrderSorter implements Comparator<CacheHolder> {
	private List<CacheHolder> l;
	
	public SpecialOrderSorter(List<CacheHolder> l) {
		this.l = l;
	}

	@Override
	public int compare(CacheHolder o1, CacheHolder o2) {
		if (l.contains(o1)) {
			if (l.contains(o2)) {
				return l.indexOf(o1) - l.indexOf(o2);
			} else {
				return -1;
			}
		} else {
			if (l.contains(o2)) {
				return 1;
			} else {
				return 0;
			}
		}
	}

}
