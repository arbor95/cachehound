package de.cachehound.filter;

import de.cachehound.beans.ICacheHolder;

public class AndFilter extends ListFilter {

	@Override
	public boolean cacheIsVisible(ICacheHolder ch) {
		for (IFilter f : this) {
			if (!f.cacheIsVisible(ch)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return "AND";
	}
}
