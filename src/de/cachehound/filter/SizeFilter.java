package de.cachehound.filter;

import java.util.Set;

import de.cachehound.beans.ICacheHolder;
import de.cachehound.types.CacheSize;

public class SizeFilter extends AbstractEnumBasedFilter<CacheSize> {
	public SizeFilter(Set<CacheSize> mask) {
		init(mask);
	}

	@Override
	protected CacheSize getProperty(ICacheHolder ch) {
		return ch.getCacheSize();
	}
}
