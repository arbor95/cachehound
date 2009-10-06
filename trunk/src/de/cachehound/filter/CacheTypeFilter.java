package de.cachehound.filter;

import java.util.EnumSet;
import java.util.Set;

import de.cachehound.beans.ICacheHolder;
import de.cachehound.types.CacheType;

public class CacheTypeFilter extends AbstractEnumBasedFilter<CacheType> {
	public CacheTypeFilter() {
		super(EnumSet.noneOf(CacheType.class));
	}
	
	public CacheTypeFilter(Set<CacheType> mask) {
		super(mask);
	}

	@Override
	protected CacheType getProperty(ICacheHolder ch) {
		return ch.getType();
	}
	
	@Override
	public String toString() {
		return "CacheType: is one of " + super.toString();
	}

	@Override
	protected String xmlElementName() {
		return "cachetype";
	}
}
