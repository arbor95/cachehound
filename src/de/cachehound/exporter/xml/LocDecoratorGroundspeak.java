package de.cachehound.exporter.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import de.cachehound.beans.ICacheHolder;

public class LocDecoratorGroundspeak extends DomForCacheDecorator {
	public LocDecoratorGroundspeak(IDomForCache decoratee) {
		super(decoratee);
	}

	@Override
	public Document getDomForCache(ICacheHolder cache) {
		Document doc = super.getDomForCache(cache);
		Node root = doc.getFirstChild();

		Element link = doc.createElement("link");
		root.appendChild(link);

		Text linkText = doc
				.createTextNode("http://www.geocaching.com/seek/cache_details.aspx?wp="
						+ cache.getWayPoint());
		link.appendChild(linkText);

		return doc;
	}
}
