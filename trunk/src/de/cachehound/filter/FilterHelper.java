package de.cachehound.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.cachehound.beans.ICacheHolder;

public class FilterHelper {
	/**
	 * Erzeugt eine Liste, die genau die Caches aus c enthalten, für die
	 * f.cacheIsVisible() wahr ist.
	 */
	public static List<ICacheHolder> applyFilter(IFilter f,
			Collection<? extends ICacheHolder> c) {
		List<ICacheHolder> ret = new ArrayList<ICacheHolder>();

		for (ICacheHolder ch : c) {
			if (f.cacheIsVisible(ch)) {
				ret.add(ch);
			}
		}

		return ret;
	}
}
