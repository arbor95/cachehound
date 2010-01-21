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
		return "Bearing: is one of " + super.toString();
	}

	@Override
	protected String xmlElementName() {
		return "bearing";
	}
}
