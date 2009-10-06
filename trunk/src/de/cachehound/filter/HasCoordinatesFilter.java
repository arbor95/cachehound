package de.cachehound.filter;

import org.jdom.Element;

import de.cachehound.beans.ICacheHolder;

public class HasCoordinatesFilter extends SimpleFilter {
	@Override
	public boolean cacheIsVisible(ICacheHolder ch) {
		return ch.getPos().isValid();
	}

	@Override
	public String toString() {
		return "Has valid coordinates";
	}

	@Override
	public Element toXML() {
		return new Element("hascoordinates");
	}
}
