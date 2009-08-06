package de.cachehound.filter;

import de.cachehound.beans.ICacheHolder;

/**
 * Trivialer Filter, der entweder alle Caches anzeigt ({@code
 * TrivialFilter(true)}) oder ausblendet ({@code TrivialFilter(false)}).
 * 
 * @author uo
 * 
 */
public class TrivialFilter implements IFilter {
	private boolean result;

	public TrivialFilter(boolean result) {
		this.result = result;
	}

	@Override
	public boolean cacheIsVisible(ICacheHolder ch) {
		return result;
	}
}
