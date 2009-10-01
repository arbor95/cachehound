package de.cachehound.exporter.xml;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import de.cachehound.beans.ICacheHolder;

public class LocDomForCache implements IDomForCache {
	private static Logger logger = LoggerFactory.getLogger(LocExporter.class);

	@Override
	public Document getDomForCache(ICacheHolder cache) {
		try {
			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
			Document doc = docBuilder.newDocument();

			Element root = doc.createElement("waypoint");
			doc.appendChild(root);

			Element name = doc.createElement("name");
			name.setAttribute("id", cache.getWayPoint());
			root.appendChild(name);

			CDATASection nameData = doc.createCDATASection(cache.getCacheName()
					+ " by " + cache.getCacheOwner());
			name.appendChild(nameData);

			Element coord = doc.createElement("coord");
			coord.setAttribute("lat", Double.toString(cache.getPos().latDec));
			coord.setAttribute("lon", Double.toString(cache.getPos().lonDec));
			root.appendChild(coord);

			Element type = doc.createElement("type");
			root.appendChild(type);

			Text typeText = doc.createTextNode("Geocache");
			type.appendChild(typeText);

			return doc;
		} catch (ParserConfigurationException e) {
			logger.error("Error while creating DOM tree", e);
			return null;
		}
	}
}
