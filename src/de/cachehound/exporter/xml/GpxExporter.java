package de.cachehound.exporter.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import CacheWolf.beans.CWPoint;
import de.cachehound.beans.CacheHolderDummy;
import de.cachehound.beans.ICacheHolder;
import de.cachehound.beans.ICacheHolderDetail;
import de.cachehound.beans.LogList;
import de.cachehound.types.CacheSize;
import de.cachehound.types.CacheType;
import de.cachehound.types.Difficulty;
import de.cachehound.types.Terrain;

/**
 * Diese Klasse dient dazu die Caches und Waypoints als (Groundspeak-kompatible)
 * Gpx-Datei zu exportieren.
 * 
 * 
 * Probleme können sein: 
 * 
 * @author tweety
 * 
 */
// TODO: Das Time Tag kann nur mit aktuelle Zeit gefüllt werden - wird bisher nicht gespeichert.
// TODO: Beim Import müssten sowohl short als auch Long Description gefüllt werden - bisher wird alles in Long Description geschrieben.  
// TODO: LoggerId noch richtig setzen.
// TODO: Log ID auch nocht nicht richtig gesetzt.

public class GpxExporter {
	private static Logger logger = LoggerFactory.getLogger(GpxExporter.class);

	private Writer w;
	private List<IDomDecorator> decorators;

	/**
	 * Erstellt einen GpxExporter, der auf den übergebenen Writer schreibt.
	 * 
	 * Die Zeichenkodierung des Writers muss utf-8 sein!
	 * 
	 * @param w
	 */
	public GpxExporter(Writer w) {
		this.w = w;
		this.decorators = new LinkedList<IDomDecorator>();
	}

	/**
	 * Bequemlichkeits-Methode. Erstellt einen neue OutputStreamWriter, der in
	 * die übergebene Datei schreibt.
	 * 
	 * @param f
	 *            Datei, in die geschrieben werden soll.
	 * 
	 * @throws FileNotFoundException
	 *             wenn der OutputStreamWriter diese wirft.
	 */
	public GpxExporter(File f) throws FileNotFoundException {
		try {
			this.w = new OutputStreamWriter(new FileOutputStream(f), "utf-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("utf-8 is unsupported", e);
		}
		this.decorators = new LinkedList<IDomDecorator>();
	}

	/**
	 * Decorator hinzufügen.
	 */
	public void addDecorator(IDomDecorator dec) {
		decorators.add(dec);
	}

	/**
	 * Schreibt ein Gpx-Datei bestehend aus den übergebenen Caches auf den im
	 * Konstruktor übergebenen Stream bzw. in die übergebene Datei.
	 * 
	 * @param caches
	 * @throws IOException
	 */
	public void doit(Collection<? extends ICacheHolder> caches)
			throws IOException {
		/*
		 * Ein DomTree für die gesamte .gpx-Datei kann (zu) gross werden. Also
		 * generieren wir für jeden Cache einen einzelnen DomTree und schreiben
		 * diese hintereinander weg. Damit das ganze wieder ein gültiges
		 * XML-Dokument wird, müssen wir uns um das Wurzelelement selber
		 * kümmern.
		 */
		createHeader(caches);

		try {
			TransformerFactory transfac = TransformerFactory.newInstance();
			Transformer trans = transfac.newTransformer();
			// Wir transformieren nur Teilbäume, also soll nicht für jeden
			// Teilbaum eine XML-Deklaration erzeugt werden.
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			// Aber hübsch solls sein!
			trans.setOutputProperty(OutputKeys.INDENT, "yes");

			StreamResult result = new StreamResult(w);
			for (ICacheHolder ch : caches) {
				// DomTree erzeugen.
				Document doc;
				if (ch.isCacheWpt()) {
					doc = getDomForGeocache(ch);
				} else {
					doc = getDomForWaypoint(ch);
				}

				// Jeder Decorator darf was dran ändern
				for (IDomDecorator dec : decorators) {
					dec.decorateDomTree(doc, ch);
				}

				// Und das Ergebnis ausgeben.
				DOMSource source = new DOMSource(doc);
				trans.transform(source, result);
			}
		} catch (TransformerConfigurationException e) {
			logger.error("Error while transforming DOM tree", e);
		} catch (TransformerException e) {
			logger.error("Error while transforming DOM tree", e);
		}

		// Wir haben das Wurzelement von Hand aufgemacht, also müssen wir es
		// auch von Hand schliessen.
		createFooter();
		w.flush();
	}

	/**
	 * Erzeugt den Header eine (Groundspeak)-kompatiblen Gpx-Datei. Es werden
	 * die zu exportierenden Caches benötigt um die Bounding Box der Caches zu
	 * berechnen. Dies kostet zwar einen weiteren durchlauf durch alle Caches,
	 * da aber nur einige double-Vergleiche gemacht werden dürften sich die
	 * "Kosten" im Rahmen halten.
	 * 
	 * @param caches
	 *            Die Geocaches für diese Gpx-Datei.
	 * @throws IOException
	 */
	private void createHeader(Collection<? extends ICacheHolder> caches)
			throws IOException {
		w.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		w
				.write("<gpx xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
						+ "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" "
						+ "version=\"1.0\" creator=\"Groundspeak Pocket Query\" "
						+ "xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd "
						+ "http://www.groundspeak.com/cache/1/0 http://www.groundspeak.com/cache/1/0/cache.xsd\" "
						+ "xmlns=\"http://www.topografix.com/GPX/1/0\">\n");

		w.write("<name>CacheHound Gpx-Export</name>\n");
		w.write("<desc>Geocache file generated by Groundspeak</desc>\n");
		w.write("<author>Groundspeak</author>\n");
		w.write("<email>contact@groundspeak.com</email>\n");
		// Format: 2009-10-03T00:07:21.5541424-07:00
		SimpleDateFormat sdf = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss.S'0000-07:00'");
		w.write("<time>" + sdf.format(new Date()) + "</time>\n");
		w.write("<keywords>cache, geocache, groundspeak</keywords>\n");

		double minLat = 90;
		double maxLat = -90;
		double minLon = 180;
		double maxLon = -180;
		for (ICacheHolder cache : caches) {
			if (cache.getPos().latDec < minLat) {
				minLat = cache.getPos().latDec;
			}
			if (cache.getPos().latDec > maxLat) {
				maxLat = cache.getPos().latDec;
			}
			if (cache.getPos().lonDec < minLon) {
				minLon = cache.getPos().lonDec;
			}
			if (cache.getPos().lonDec > maxLon) {
				maxLon = cache.getPos().lonDec;
			}
		}
		w
				.write("<bounds minlat=\"" + minLat + "\" minlon=\"" + minLon
						+ "\" maxlat=\"" + maxLat + "\" maxlon=\"" + maxLon
						+ "\" />\n");
	}

	/**
	 * Erzeugt den Footer einer Gpx-Datei. Dieser besteht allerdings nur aus
	 * einem schließenen Tag.
	 * 
	 * @throws IOException
	 */
	private void createFooter() throws IOException {
		w.write("</gpx>");
	}

	/**
	 * Erstellt einen rudimentären Dom-Tree zu einem WayPoint.
	 * 
	 * Das <wpt>-Tag enthält dabei nur die Untertags <time>, <name>, <cmt>,
	 * <desc>, <url>, <urlname>, <sym> und <type>
	 * 
	 * Alle weiteren Ergänzungen sollten über jeweils einen IDomDecorator
	 * erfolgen.
	 */
	private Document getDomForWaypoint(ICacheHolder ch) {
		try {
			// Man kanns mit'm Factory-Pattern auch übertreiben...
			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
			Document doc = docBuilder.newDocument();

			Element root = doc.createElement("wpt");
			root.setAttribute("lat", Double.toString(ch.getPos().latDec));
			root.setAttribute("lon", Double.toString(ch.getPos().lonDec));
			doc.appendChild(root);

			// TODO: Auf korrektes Datum stellen
			SimpleDateFormat sdf = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ss.S");
			Element time = doc.createElement("time");
			time.setTextContent(sdf.format(new Date()));
			root.appendChild(time);

			Element name = doc.createElement("name");
			name.setTextContent(ch.getWayPoint());
			root.appendChild(name);

			Element cmt = doc.createElement("cmt");
			cmt.setTextContent(ch.getDetails().getLongDescription());
			root.appendChild(cmt);

			Element desc = doc.createElement("desc");
			desc.setTextContent(ch.getCacheName());
			root.appendChild(desc);

			Element url = doc.createElement("url");
			url.setTextContent(ch.getDetails().getUrl());
			root.appendChild(url);

			Element urlName = doc.createElement("urlname");
			urlName.setTextContent(ch.getCacheName());
			root.appendChild(urlName);

			Element sym = doc.createElement("sym");
			sym.setTextContent(ch.getType().getGcGpxString());
			root.appendChild(sym);

			Element type = doc.createElement("type");
			type.setTextContent("Waypoint|" + ch.getType().getGcGpxString());
			root.appendChild(type);

			return doc;
		} catch (ParserConfigurationException e) {
			logger.error("Error while creating DOM tree", e);
			return null;
		}
	}

	/**
	 * Erstellt einen rudimentären Dom-Tree zu einem WayPoint.
	 * 
	 * Das <wpt>-Tag enthält dabei nur die Untertags <time>, <name>, <cmt>,
	 * <desc>, <url>, <urlname>, <sym> und <type>
	 * 
	 * Alle weiteren Ergänzungen sollten über jeweils einen IDomDecorator
	 * erfolgen.
	 */
	private Document getDomForGeocache(ICacheHolder ch) {
		try {
			// Man kanns mit'm Factory-Pattern auch übertreiben...
			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
			Document doc = docBuilder.newDocument();

			// Allgemeiner Waypoint-Header:			
			Element root = doc.createElement("wpt");
			root.setAttribute("lat", Double.toString(ch.getPos().latDec));
			root.setAttribute("lon", Double.toString(ch.getPos().lonDec));
			doc.appendChild(root);

			// TODO: Auf korrektes Datum stellen
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T00:00:00'");
			Element time = doc.createElement("time");
			time.setTextContent(sdf.format(new Date()));
			root.appendChild(time);

			Element name = doc.createElement("name");
			name.setTextContent(ch.getWayPoint());
			root.appendChild(name);

			Element desc = doc.createElement("desc");
			desc.setTextContent(ch.getCacheName() + " by " + ch.getCacheOwner()
					+ ", " + ch.getType().getGcGpxString() + " ("
					+ ch.getDifficulty().getShortRepresentation() + "/"
					+ ch.getTerrain().getShortRepresentation() + ")");
			root.appendChild(desc);

			Element url = doc.createElement("url");
			url.setTextContent(ch.getDetails().getUrl());
			root.appendChild(url);

			Element urlName = doc.createElement("urlname");
			urlName.setTextContent(ch.getCacheName());
			root.appendChild(urlName);

			Element sym = doc.createElement("sym");
			sym.setTextContent("Geocache");
			root.appendChild(sym);

			Element type = doc.createElement("type");
			type.setTextContent("Geocache|" + ch.getType().getGcGpxString());
			root.appendChild(type);
			
			return doc;
		} catch (ParserConfigurationException e) {
			logger.error("Error while creating DOM tree", e);
			return null;
		}
	}

	// Ab hier ist manueller Testcode - zum ausprobieren.
	public static void main(String... args) {
		ICacheHolder cache = new CacheHolderDummy() {
			@Override
			public String getCacheName() {
				return "TestCache";
			}

			@Override
			public String getCacheOwner() {
				return "TestOwner";
			}

			@Override
			public String getWayPoint() {
				return "GC12345";
			}

			@Override
			public CWPoint getPos() {
				return new CWPoint(-52.1, 8.12345678);
			}

			@Override
			public Difficulty getDifficulty() {
				return Difficulty.DIFFICULTY_1_0;
			}

			@Override
			public Terrain getTerrain() {
				return Terrain.TERRAIN_1_5;
			}

			@Override
			public CacheType getType() {
				return CacheType.MULTI;
			}

			@Override
			public String getCacheID() {
				return "1234567";
			}
			
			@Override
			public boolean isCacheWpt() {
				return true;
			}
			
			@Override
			public CacheSize getCacheSize() {
				return CacheSize.MICRO;
			}

			@Override
			public boolean isHTML() {
				return true;
			}
			
			@Override
			public ICacheHolderDetail getDetails() {
				return new ICacheHolderDetail() {
					@Override
					public String getLongDescription() {
						return "Beschreibung des Wegpunktes";
					}

					@Override
					public String getUrl() {
						return "http://www.geocaching.com/seek/wpt.aspx?WID=a70708a9-dd9a-4375-8b57-afec8d547ae0";
					}
					@Override
					public String getCountry() {
						return "Germany";
					}

					@Override
					public String getState() {
						return "Hamburg";
					}

					@Override
					public String getShortDescription() {
						return "<br/> Short <br/>";
					}

					@Override
					public String getHints() {
						return "[Stage1]NOPQRST";
					}

					@Override
					public LogList getCacheLogs() {
						// TODO Auto-generated method stub
						return null;
					}
					
				};
			}
		};
		ICacheHolder cache2 = new CacheHolderDummy() {
			@Override
			public String getCacheName() {
				return "TestCache2";
			}

			@Override
			public String getCacheOwner() {
				return "TestOwner2";
			}

			@Override
			public String getWayPoint() {
				return "GC13456";
			}

			@Override
			public CWPoint getPos() {
				return new CWPoint(53.12345678, 10.12345678);
			}

			@Override
			public Difficulty getDifficulty() {
				return Difficulty.DIFFICULTY_4_0;
			}

			@Override
			public Terrain getTerrain() {
				return Terrain.TERRAIN_4_0;
			}

			@Override
			public CacheType getType() {
				return CacheType.QUESTION;
			}

			@Override
			public CacheSize getCacheSize() {
				return CacheSize.REGULAR;
			}

			@Override
			public ICacheHolderDetail getDetails() {
				return new ICacheHolderDetail() {
					@Override
					public String getLongDescription() {
						return "Mal <ganz ganz fieser C&de! in Däütsch";
					}

					@Override
					public String getUrl() {
						return "http://www.öpnv-karte.de";
					}
					@Override
					public String getCountry() {
						return "Germany";
					}

					@Override
					public String getState() {
						return "Hamburg";
					}

					@Override
					public String getShortDescription() {
						return null;
					}

					@Override
					public String getHints() {
						return null;
					}

					@Override
					public LogList getCacheLogs() {
						// TODO Auto-generated method stub
						return null;
					}
				};
			}
		};
		Collection<ICacheHolder> caches = new ArrayList<ICacheHolder>();
		caches.add(cache);
		caches.add(cache2);

		StringWriter sw = new StringWriter();
		GpxExporter exp = new GpxExporter(sw);

		try {
			exp.doit(caches);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(sw.toString());
	}

}
