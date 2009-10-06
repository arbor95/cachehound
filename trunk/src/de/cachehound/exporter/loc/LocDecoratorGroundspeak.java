package de.cachehound.exporter.loc;

import org.jdom.Element;

import de.cachehound.beans.ICacheHolder;

public class LocDecoratorGroundspeak implements ILocDecorator {
	@Override
	public void decorateDomTree(Element doc, ICacheHolder cache) {
		Element link = new Element("link");
		link.setText("http://www.geocaching.com/seek/cache_details.aspx?wp="
						+ cache.getWayPoint());
		doc.getChildren().add(link);
	}
}
