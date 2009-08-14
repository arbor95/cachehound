package de.cachehound.filter;

import java.util.EnumSet;
import java.util.Set;

import de.cachehound.beans.ICacheHolder;
import de.cachehound.types.CacheSize;

public class SizeFilter extends AbstractEnumBasedFilter<CacheSize> {
	public SizeFilter() {
		init(EnumSet.noneOf(CacheSize.class));
	}
	
	public SizeFilter(Set<CacheSize> mask) {
		init(mask);
	}

	@Override
	protected CacheSize getProperty(ICacheHolder ch) {
		return ch.getCacheSize();
	}
	
	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder("Size: is one of ");
		for (CacheSize b : getMask()) {
			ret.append(b.toString());
			ret.append(" ");
		}
		return ret.toString();
	}
}
