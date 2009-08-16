package de.cachehound.beans;

import de.cachehound.types.Bearing;
import de.cachehound.types.CacheSize;
import de.cachehound.types.Difficulty;
import de.cachehound.types.Terrain;

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
	public double getKilom() {
		return Double.NaN;
	}

	@Override
	public CacheSize getCacheSize() {
		return null;
	}

	@Override
	public Difficulty getDifficulty() {
		return null;
	}

	@Override
	public Terrain getTerrain() {
		return null;
	}
}
