package de.cachehound.filter;

import org.jdom.Element;

import de.cachehound.beans.ICacheHolder;

public class ArchivedFilter extends SimpleFilter {
	@Override
	public boolean cacheIsVisible(ICacheHolder ch) {
		return ch.isArchived();
	}
	
	@Override
	public String toString() {
		return "Archived";
	}

	@Override
	public Element toXML() {
		return new Element("archieved");
	}
}
