package de.cachehound.filter;

import java.util.EnumSet;
import java.util.Set;

import de.cachehound.beans.ICacheHolder;
import de.cachehound.types.Bearing;

public class BearingFilter extends AbstractEnumBasedFilter<Bearing> {
	public BearingFilter() {
		super(EnumSet.noneOf(Bearing.class));
	}
	
	public BearingFilter(Set<Bearing> mask) {
		super(mask);
	}

	@Override
	protected Bearing getProperty(ICacheHolder ch) {
		return ch.getBearing();
	}
	
	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder("Bearing: is one of ");
		for (Bearing b : getMask()) {
			ret.append(b.toString());
			ret.append(" ");
		}
		return ret.toString();
	}
}
