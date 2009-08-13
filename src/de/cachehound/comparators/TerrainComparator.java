package de.cachehound.comparators;

import java.util.Comparator;

import CacheWolf.beans.CacheHolder;

public class TerrainComparator implements Comparator<CacheHolder> {
	@Override
	public int compare(CacheHolder o1, CacheHolder o2) {
		return o1.getTerrain().ordinal() - o2.getTerrain().ordinal();
		// TODO: Check if it is useful to handle the problem Terrains extra
	}
}
