package de.cachehound.filter;

import org.jdom.Element;

import de.cachehound.beans.ICacheHolder;

public class FoundFilter extends SimpleFilter {
	@Override
	public boolean cacheIsVisible(ICacheHolder ch) {
		return ch.isFound();
	}

	@Override
	public String toString() {
		return "Found";
	}

	@Override
	public Element toXML() {
		return new Element("found");
	}
}
