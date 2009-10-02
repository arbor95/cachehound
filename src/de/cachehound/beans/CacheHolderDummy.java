package de.cachehound.beans;

import CacheWolf.beans.CWPoint;
import de.cachehound.types.Bearing;
import de.cachehound.types.CacheSize;
import de.cachehound.types.CacheType;
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
	public CacheType getType() {
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
	
	@Override
	public boolean isArchived() {
		return false;
	}

	@Override
	public boolean isAvailable() {
		return false;
	}

	@Override
	public boolean isFound() {
		return false;
	}

	@Override
	public boolean isOwned() {
		return false;
	}

	@Override
	public String getWayPoint() {
		return null;
	}

	@Override
	public String getCacheName() {
		return null;
	}

	@Override
	public String getCacheOwner() {
		return null;
	}

	@Override
	public CWPoint getPos() {
		return null;
	}

	@Override
	public boolean isCacheWpt() {
		return false;
	}
}
