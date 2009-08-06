package de.cachehound.filter;

import java.util.EnumSet;
import java.util.Set;

import de.cachehound.beans.ICacheHolder;

/**
 * Abstrakte Basisklasse fuer Filter, die von genau einem CacheHolder-Attribut
 * abhaengen. Dieses muss einen Enum-Typ ha.ben
 */
public abstract class AbstractEnumBasedFilter<T extends Enum<T>> implements IFilter {
	private Set<T> mask;

	protected void init(Set<T> mask) {
		this.mask = EnumSet.copyOf(mask);
	}

	protected abstract T getProperty(ICacheHolder ch);

	@Override
	public boolean cacheIsVisible(ICacheHolder ch) {
		return mask.contains(getProperty(ch));
	}
	
	public Set<T> getMask () {
		return EnumSet.copyOf(mask);
	}
}
