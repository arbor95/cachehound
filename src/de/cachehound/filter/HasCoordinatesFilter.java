package de.cachehound.filter;

import de.cachehound.beans.ICacheHolder;

public class HasCoordinatesFilter extends SimpleFilter {

	@Override
	public boolean cacheIsVisible(ICacheHolder ch) {
		return ch.getPos().isValid();
	}

	@Override
	public String toString() {
		return "Has valid coordinates";
	}
}
