package de.cachehound.filter;

import org.jdom.Element;

import de.cachehound.beans.ICacheHolder;

/**
 * Interface, das alle Filter implementieren muessen.
 * 
 * Alle Filter müssen immutable sein!
 */
public interface IFilter {
	/**
	 * @return {@code false}, wenn der Cache ausgefiltert wird, {@code true}
	 *         sonst.
	 */
	public boolean cacheIsVisible(ICacheHolder ch);

	public Element toXML();
}
