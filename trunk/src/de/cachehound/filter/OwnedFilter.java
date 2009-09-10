package de.cachehound.filter;

import de.cachehound.beans.ICacheHolder;

public class OwnedFilter extends SimpleFilter {
	@Override
	public boolean cacheIsVisible(ICacheHolder ch) {
		return ch.isOwned();
	}
	
	@Override
	public String toString() {
		return "Owned";
	}
}
