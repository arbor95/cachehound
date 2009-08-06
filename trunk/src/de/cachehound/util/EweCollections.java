package de.cachehound.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import ewe.util.Comparer;
import ewe.util.Vector;

@Deprecated
public class EweCollections {
	@SuppressWarnings("unchecked")
	public static <T> List<T> vectorToList(Vector v) {
		List<T> l = new ArrayList<T>();
		for (int i = 0; i < v.size(); i++) {
			l.add((T) v.get(i));
		}
		return l;
	}

	public static <T> Comparator<T> comparerToComparator(final Comparer c) {
		return new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {
				return c.compare(o1, o2);
			}
		};
	}
}
