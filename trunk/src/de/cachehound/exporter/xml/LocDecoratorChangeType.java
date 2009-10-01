package de.cachehound.exporter.xml;

import java.util.LinkedHashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import de.cachehound.beans.ICacheHolder;
import de.cachehound.filter.IFilter;

public class LocDecoratorChangeType extends DomForCacheDecorator {
	private Map<IFilter, String> types;

	public LocDecoratorChangeType(Map<IFilter, String> types,
			IDomForCache decoratee) {
		super(decoratee);

		this.types = new LinkedHashMap<IFilter, String>(types);
	}

	private String getType(ICacheHolder cache) {
		for (IFilter f : types.keySet()) {
			if (f.cacheIsVisible(cache)) {
				return types.get(f);
			}
		}
		return "Geocache";
	}

	@Override
	public Document getDomForCache(ICacheHolder cache) {
		Document doc = super.getDomForCache(cache);
		Node root = doc.getFirstChild();

		NodeList types = doc.getElementsByTagName("type");
		// sollte eigentlich nur max 1x vorkommen, aber man weiss ja nie...

		for (int i = 0; i < types.getLength(); i++) {
			types.item(i).getParentNode().removeChild(types.item(i));
		}

		Node type = doc.createElement("type");
		root.appendChild(type);

		Text typeText = doc.createTextNode(getType(cache));
		type.appendChild(typeText);

		return doc;
	}
}
