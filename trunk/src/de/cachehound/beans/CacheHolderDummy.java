package de.cachehound.beans;

import de.cachehound.types.Bearing;
import de.cachehound.types.CacheSize;

/**
 * A dummy, non-working implementation of ICacheHolder. It is meant as a base
 * class for mock objects.
 */
public class CacheHolderDummy implements ICacheHolder {
	@Override
	public Bearing getBearing() {
		return null;
	}

	@Override
	public CacheSize getCacheSize() {
		return null;
	}

	@Override
	public double getKilom() {
		return Double.NaN;
	}
}
