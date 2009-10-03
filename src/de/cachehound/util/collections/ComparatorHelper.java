package de.cachehound.util.collections;

import java.util.Comparator;

public class ComparatorHelper {
	public static <T> Comparator<T> invert(final Comparator<T> comp) {
		return new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {
				return -comp.compare(o1, o2);
			}
		};
	}
}
