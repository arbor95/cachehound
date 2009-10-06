package de.cachehound.filter;

import java.util.EnumSet;
import java.util.Set;

import de.cachehound.beans.ICacheHolder;
import de.cachehound.types.CacheSize;

public class SizeFilter extends AbstractEnumBasedFilter<CacheSize> {
	public SizeFilter() {
		super(EnumSet.noneOf(CacheSize.class));
	}
	
	public SizeFilter(Set<CacheSize> mask) {
		super(mask);
	}

	@Override
	protected CacheSize getProperty(ICacheHolder ch) {
		return ch.getCacheSize();
	}
	
	@Override
	public String toString() {
		return "Size: is one of " + super.toString();
	}

	@Override
	protected String xmlElementName() {
		return "size";
	}
}
