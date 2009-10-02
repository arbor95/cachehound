package de.cachehound.beans;

import CacheWolf.beans.CWPoint;
import de.cachehound.types.Bearing;
import de.cachehound.types.CacheSize;
import de.cachehound.types.CacheType;
import de.cachehound.types.Difficulty;
import de.cachehound.types.Terrain;

public interface ICacheHolder {
	public Bearing getBearing();
	public double getKilom();

	public CacheSize getCacheSize();
	public CacheType getType();
	public Difficulty getDifficulty();
	public Terrain getTerrain();
	
	public boolean isArchived();
	public boolean isAvailable();
	public boolean isFound();
	public boolean isOwned();
	public boolean isCacheWpt();
	
	public String getWayPoint();
	public String getCacheName();
	public String getCacheOwner();
	
	public GcVote getGcVote();
	public void setGcVote(GcVote gcVote);
	
	public CWPoint getPos();
}
