package de.cachehound.exporter.gpx;

import org.jdom.Element;

import de.cachehound.beans.ICacheHolder;

public interface IGpxDecorator {
	void decorateDomTree(Element d, ICacheHolder cache);
}
