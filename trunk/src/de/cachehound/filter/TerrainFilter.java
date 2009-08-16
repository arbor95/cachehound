package de.cachehound.filter;

import java.util.Set;

import de.cachehound.beans.ICacheHolder;
import de.cachehound.types.Terrain;

public class TerrainFilter extends AbstractEnumBasedFilter<Terrain> {
	protected TerrainFilter(Set<Terrain> mask) {
		super(mask);
	}

	@Override
	protected Terrain getProperty(ICacheHolder ch) {
		return ch.getTerrain();
	}

	@Override
	public String toString() {
		return "Terrain is one of " + super.toString();
	}
}
