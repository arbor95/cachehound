package de.cachehound.filter;

import org.jdom.Element;

import de.cachehound.beans.ICacheHolder;

/**
 * Interface, das alle Filter implementieren muessen.
 */
public interface IFilter extends Cloneable {
	/**
	 * @return {@code false}, wenn der Cache ausgefiltert wird, {@code true}
	 *         sonst.
	 */
	public boolean cacheIsVisible(ICacheHolder ch);
	
	public IFilter clone();
	
	public Element toXML();
}
