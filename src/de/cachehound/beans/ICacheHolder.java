package de.cachehound.beans;

import de.cachehound.types.Bearing;
import de.cachehound.types.CacheSize;
import de.cachehound.types.Difficulty;
import de.cachehound.types.Terrain;

public interface ICacheHolder {
	public Bearing getBearing();
	public double getKilom();

	public CacheSize getCacheSize();

	public Difficulty getDifficulty();
	public Terrain getTerrain();
	
	public boolean isArchived();
	public boolean isAvailable();
	public boolean isFound();
	public boolean isOwned();
}
