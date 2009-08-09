package de.cachehound.filter;

import de.cachehound.beans.ICacheHolder;

public class OrFilter extends ListFilter {

	@Override
	public boolean cacheIsVisible(ICacheHolder ch) {
		for (IFilter f : this) {
			if (f.cacheIsVisible(ch)) {
				return true;
			}
		}
		return false;
	}

}
