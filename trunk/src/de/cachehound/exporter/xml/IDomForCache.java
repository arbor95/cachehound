package de.cachehound.exporter.xml;

import org.w3c.dom.Document;

import de.cachehound.beans.ICacheHolder;

public interface IDomForCache {
	Document getDomForCache(ICacheHolder cache);
}
