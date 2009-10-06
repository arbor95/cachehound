package de.cachehound.exporter.loc;

import org.jdom.Element;

import de.cachehound.beans.ICacheHolder;

public interface ILocDecorator {
	void decorateDomTree(Element waypoint, ICacheHolder cache);
}
