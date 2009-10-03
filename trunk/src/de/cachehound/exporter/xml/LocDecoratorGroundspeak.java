package de.cachehound.exporter.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import de.cachehound.beans.ICacheHolder;

public class LocDecoratorGroundspeak implements IDomDecorator {
	@Override
	public void decorateDomTree(Document doc, ICacheHolder cache) {
		Node root = doc.getFirstChild();

		Element link = doc.createElement("link");
		root.appendChild(link);

		Text linkText = doc
				.createTextNode("http://www.geocaching.com/seek/cache_details.aspx?wp="
						+ cache.getWayPoint());
		link.appendChild(linkText);
	}
}
