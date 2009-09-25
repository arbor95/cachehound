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

import CacheWolf.beans.CWPoint;
import de.cachehound.beans.CacheHolderDummy;
import de.cachehound.beans.ICacheHolder;
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
	public LocExporter(IDomForCache cacheHandler, Writer w) {
		this.cacheHandler = cacheHandler;
		this.w = w;
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
	public LocExporter(IDomForCache cacheHandler, File f)
			throws FileNotFoundException {
		this.cacheHandler = cacheHandler;
		try {
			this.w = new OutputStreamWriter(new FileOutputStream(f), "utf-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("utf-8 is unsupported", e);
		}
	}

	public void doit(Collection<ICacheHolder> caches) throws IOException {
		w.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		w.write("<loc version=\"1.0\" src=\"CacheHound\">\n");
		
		try {
			TransformerFactory transfac = TransformerFactory.newInstance();
			Transformer trans = transfac.newTransformer();
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			trans.setOutputProperty(OutputKeys.INDENT, "yes");

			StreamResult result = new StreamResult(w);
			for (ICacheHolder ch : caches) {
				DOMSource source = new DOMSource(cacheHandler
						.getDomForCache(ch));
				trans.transform(source, result);
			}
		} catch (TransformerConfigurationException e) {
			logger.error("Error while transforming DOM tree", e);
		} catch (TransformerException e) {
			logger.error("Error while transforming DOM tree", e);
		}
		
		w.write("</loc>");
	}

	private IDomForCache cacheHandler;
	private Writer w;

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

		StringWriter sw = new StringWriter();
		LocExporter exp = new LocExporter(new LocDomForCacheAddDT(
				new LocDomForCache()), sw);
		try {
			exp.doit(caches);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(sw.toString());
	}
}
