package de.cachehound.exporter.loc;

import java.util.LinkedHashMap;
import java.util.Map;

import org.jdom.Element;

import de.cachehound.beans.ICacheHolder;
import de.cachehound.filter.IFilter;

public class LocDecoratorChangeType implements ILocDecorator {
	private Map<IFilter, String> types;

	public LocDecoratorChangeType(Map<IFilter, String> types) {
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
	public void decorateDomTree(Element waypoint, ICacheHolder cache) {
		Element type = waypoint.getChild("type");
		type.setText(getType(cache));
	}
}
