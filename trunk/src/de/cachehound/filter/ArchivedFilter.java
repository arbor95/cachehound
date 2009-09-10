package de.cachehound.filter;

import de.cachehound.beans.ICacheHolder;

public class ArchivedFilter extends SimpleFilter {
	@Override
	public boolean cacheIsVisible(ICacheHolder ch) {
		return ch.isArchived();
	}
	
	@Override
	public String toString() {
		return "Archived";
	}
}
