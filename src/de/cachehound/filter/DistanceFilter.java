package de.cachehound.filter;

import de.cachehound.beans.ICacheHolder;

public class DistanceFilter implements IFilter {
	private double limit;
	
	public DistanceFilter(double d) {
		this.limit = d;
	}

	@Override
	public boolean cacheIsVisible(ICacheHolder ch) {
		return ch.getKilom() < limit; 
	}
}
