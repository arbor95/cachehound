package de.cachehound.exporter.xml;

import org.w3c.dom.Document;

import de.cachehound.beans.ICacheHolder;

public interface IDomDecorator {
	Document getDomForCache(Document d, ICacheHolder cache);
}
