package de.cachehound.exporter.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.cachehound.beans.ICacheHolder;

public class LocDomForCacheAddDT extends DomForCacheDecorator {
	public LocDomForCacheAddDT(IDomForCache decoratee) {
		super(decoratee);
	}

	@Override
	public Document getDomForCache(ICacheHolder cache) {
		Document doc = super.getDomForCache(cache);

		NodeList names = doc.getElementsByTagName("name");
		// sollte eigentlich nur genau 1x vorkommen, aber man weiss ja nie...

		for (int i = 0; i < names.getLength(); i++) {
			Element e = (Element) names.item(i);

			String oldValue = e.getAttribute("id");
			e.setAttribute("id", oldValue
					+ cache.getType().getShortExport().toLowerCase()
					+ cache.getDifficulty().getFullRepresentation().replace(
							".", "")
					+ "/"
					+ cache.getTerrain().getFullRepresentation().replace(".",
							"")
					+ cache.getCacheSize().getAsChar());
		}

		return doc;
	}
}
