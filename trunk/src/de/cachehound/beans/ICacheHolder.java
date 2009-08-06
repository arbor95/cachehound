package de.cachehound.beans;

import de.cachehound.types.Bearing;
import de.cachehound.types.CacheSize;

public interface ICacheHolder {
	public Bearing getBearing();

	public CacheSize getCacheSize();

	public double getKilom();
}
