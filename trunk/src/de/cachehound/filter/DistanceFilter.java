package de.cachehound.filter;

import de.cachehound.beans.ICacheHolder;

public class DistanceFilter extends SimpleFilter {
	private double limit;

	public DistanceFilter(double d) {
		this.limit = d;
	}
	
	public double getLimit() {
		return limit;
	}

	@Override
	public boolean cacheIsVisible(ICacheHolder ch) {
		return ch.getKilom() < limit;
	}
}
