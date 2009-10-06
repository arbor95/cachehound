package de.cachehound.filter;

import org.jdom.Element;

import de.cachehound.beans.ICacheHolder;

/**
 * Trivialer Filter, der entweder alle Caches anzeigt ({@code
 * TrivialFilter(true)}) oder ausblendet ({@code TrivialFilter(false)}).
 * 
 * @author uo
 * 
 */
public class TrivialFilter extends SimpleFilter {
	private boolean result;

	public TrivialFilter(boolean result) {
		this.result = result;
	}

	@Override
	public boolean cacheIsVisible(ICacheHolder ch) {
		return result;
	}
	
	@Override
	public String toString() {
		if (result) {
			return "All Caches";
		} else {
			return "No Caches";
		}
	}

	@Override
	public Element toXML() {
		Element ret = new Element("trivial");
		
		ret.setAttribute("value", Boolean.toString(result));

		return ret;
	}
}
