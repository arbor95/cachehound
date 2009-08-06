package de.cachehound.filter;

import java.util.Set;

import de.cachehound.beans.ICacheHolder;
import de.cachehound.types.Bearing;

public class BearingFilter extends AbstractEnumBasedFilter<Bearing> {
	public BearingFilter(Set<Bearing> mask) {
		init(mask);
	}

	@Override
	protected Bearing getProperty(ICacheHolder ch) {
		return ch.getBearing();
	}
}
