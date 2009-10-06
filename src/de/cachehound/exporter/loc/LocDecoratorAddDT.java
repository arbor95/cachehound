package de.cachehound.exporter.loc;

import org.jdom.Attribute;
import org.jdom.Element;

import de.cachehound.beans.ICacheHolder;

public class LocDecoratorAddDT implements ILocDecorator {
	@Override
	public void decorateDomTree(Element waypoint, ICacheHolder cache) {
		Element name = waypoint.getChild("name");

		Attribute id = name.getAttribute("id");
		id.setValue(id.getValue()
				+ cache.getType().getShortExport().toLowerCase()
				+ cache.getDifficulty().getFullRepresentation()
						.replace(".", "") + "/"
				+ cache.getTerrain().getFullRepresentation().replace(".", "")
				+ cache.getCacheSize().getAsChar());
	}
}
