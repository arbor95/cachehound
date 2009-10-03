package de.cachehound.exporter.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
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
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import CacheWolf.beans.CWPoint;
import de.cachehound.beans.CacheHolderDummy;
import de.cachehound.beans.ICacheHolder;
import de.cachehound.filter.CacheTypeFilter;
import de.cachehound.filter.IFilter;
import de.cachehound.types.CacheSize;
import de.cachehound.types.CacheType;
import de.cachehound.types.Difficulty;
import de.cachehound.types.Terrain;

public class LocExporter {
	private static Logger logger = LoggerFactory.getLogger(LocExporter.class);

	/**
	 * Erstellt einen LocExporter, der auf den übergebenen Writer schreibt.
	 * 
	 * Die Zeichenkodierung des Writers muss utf-8 sein!
	 * 
	 * @param w
	 */
	public LocExporter(Writer w) {
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
	public LocExporter(File f) throws FileNotFoundException {
		try {
			this.w = new OutputStreamWriter(new FileOutputStream(f), "utf-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("utf-8 is unsupported", e);
		}
		this.decorators = new LinkedList<IDomDecorator>();
	}

	/**
	 * Erstellt einen rudimentären Dom-Tree zu einem Cache.
	 * 
	 * Das <waypoint>-Tag enthält dabei nur die Untertags <name>, <coord> und
	 * <type> - letzteres ist immer "Geocache".
	 * 
	 * Alle weiteren Ergänzungen sollten über jeweils einen IDomDecorator
	 * erfolgen.
	 */
	private Document getBaseDom(ICacheHolder ch) {
		try {
			// Man kanns mit'm Factory-Pattern auch übertreiben...
			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
			Document doc = docBuilder.newDocument();

			Element root = doc.createElement("waypoint");
			doc.appendChild(root);

			Element name = doc.createElement("name");
			name.setAttribute("id", ch.getWayPoint());
			root.appendChild(name);

			CDATASection nameData = doc.createCDATASection(ch.getCacheName()
					+ " by " + ch.getCacheOwner());
			name.appendChild(nameData);

			Element coord = doc.createElement("coord");
			coord.setAttribute("lat", Double.toString(ch.getPos().latDec));
			coord.setAttribute("lon", Double.toString(ch.getPos().lonDec));
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

	/**
	 * Schreibt ein .loc bestehend aus den übergebenen Caches auf den im
	 * Konstruktor übergebenen Stream bzw. in die übergebene Datei.
	 * 
	 * @param caches
	 * @throws IOException
	 */
	public void doit(Collection<? extends ICacheHolder> caches)
			throws IOException {
		/*
		 * Ein DomTree für die gesamte .loc-Datei kann (zu) gross werden. Also
		 * generieren wir für jeden Cache einen einzelnen DomTree und schreiben
		 * diese hintereinander weg. Damit das ganze wieder ein gültiges
		 * XML-Dokument wird, müssen wir uns um das Wurzelelement selber
		 * kümmern.
		 */
		w.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		w.write("<loc version=\"1.0\" src=\"CacheHound\">\n");

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
				Document doc = getBaseDom(ch);

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
		w.write("</loc>");
		w.flush();
	}

	/**
	 * Decorator hinzufügen.
	 */
	public void addDecorator(IDomDecorator dec) {
		decorators.add(dec);
	}

	private Writer w;
	private List<IDomDecorator> decorators;

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
				return new CWPoint(53.12345678, 10.12345678);
			}

			@Override
			public Difficulty getDifficulty() {
				return Difficulty.DIFFICULTY_1_0;
			}

			@Override
			public Terrain getTerrain() {
				return Terrain.TERRAIN_1_0;
			}

			@Override
			public CacheType getType() {
				return CacheType.TRADITIONAL;
			}

			@Override
			public CacheSize getCacheSize() {
				return CacheSize.MICRO;
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
				return CacheType.UNKNOWN;
			}

			@Override
			public CacheSize getCacheSize() {
				return CacheSize.REGULAR;
			}
		};
		Collection<ICacheHolder> caches = new ArrayList<ICacheHolder>();
		caches.add(cache);
		caches.add(cache2);

		LinkedHashMap<IFilter, String> mappings = new LinkedHashMap<IFilter, String>();

		EnumSet<CacheType> tradis = EnumSet.of(CacheType.TRADITIONAL);
		mappings.put(new CacheTypeFilter(tradis), "Traditional");
		EnumSet<CacheType> multis = EnumSet.of(CacheType.MULTI);
		mappings.put(new CacheTypeFilter(multis), "Multi");

		StringWriter sw = new StringWriter();
		LocExporter exp = new LocExporter(sw);

		exp.addDecorator(new LocDecoratorAddDT());
		exp.addDecorator(new LocDecoratorChangeType(mappings));

		try {
			exp.doit(caches);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(sw.toString());
	}
}
