package de.cachehound.filter;

import org.jdom.Element;

import de.cachehound.beans.ICacheHolder;

public class DisabledFilter extends SimpleFilter {
	@Override
	public boolean cacheIsVisible(ICacheHolder ch) {
		// Achtung: CacheHolder hat nur isAvailable(), nicht isDisabled().
		// IMHO macht es aber mehr Sinn, analog zum ArchivedFilter einen
		// DisabledFilter zu haben.
		return !ch.isAvailable();
	}
	
	@Override
	public String toString() {
		return "Disabled";
	}

	@Override
	public Element toXML() {
		return new Element("disabled");
	}
}
