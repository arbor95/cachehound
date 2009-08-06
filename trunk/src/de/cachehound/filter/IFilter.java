package de.cachehound.filter;

import de.cachehound.beans.ICacheHolder;

/**
 * Interface, das alle Filter implementieren muessen.
 */
public interface IFilter {
	/**
	 * @return {@code false}, wenn der Cache ausgefiltert wird, {@code true}
	 *         sonst.
	 */
	public boolean cacheIsVisible(ICacheHolder ch);
}
