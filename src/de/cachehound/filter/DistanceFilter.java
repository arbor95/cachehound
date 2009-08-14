package de.cachehound.filter;

import de.cachehound.beans.ICacheHolder;

public class DistanceFilter extends SimpleFilter {
	private double limit;

	public DistanceFilter() {
		this.limit = 5;
	}
	
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
	
	@Override
	public String toString() {
		return "Cache is nearer than " + limit + "km.";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(limit);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DistanceFilter other = (DistanceFilter) obj;
		if (Double.doubleToLongBits(limit) != Double
				.doubleToLongBits(other.limit))
			return false;
		return true;
	}
}
