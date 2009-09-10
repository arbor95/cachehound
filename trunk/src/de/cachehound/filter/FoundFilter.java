package de.cachehound.filter;

import de.cachehound.beans.ICacheHolder;

public class FoundFilter extends SimpleFilter {
	@Override
	public boolean cacheIsVisible(ICacheHolder ch) {
		return ch.isFound();
	}
	
	@Override
	public String toString() {
		return "Found";
	}
}
