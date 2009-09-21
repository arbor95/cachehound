package de.cachehound.exporter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import CacheWolf.beans.CWPoint;

import com.sun.xml.txw2.output.DataWriter;

import de.cachehound.beans.CacheHolderDummy;
import de.cachehound.beans.ICacheHolder;

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
		this.w = new DataWriter(w);
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
			this.w = new DataWriter(new OutputStreamWriter(
					new FileOutputStream(f), "utf-8"));
		} catch (UnsupportedEncodingException e) {
			logger.error("utf-8 is unsupported", e);
		}
	}

	private void handleCache(ICacheHolder cache) throws SAXException {
		w.startElement("", "", "waypoint", new AttributesImpl());
		
		AttributesImpl attName = new AttributesImpl();
		attName.addAttribute("", "", "id", "", cache.getWayPoint());
		w.dataElement("", "", "name", attName, cache.getCacheName() + " by "
				+ cache.getCacheOwner());

		AttributesImpl attPos = new AttributesImpl();
		attPos.addAttribute("", "", "lat", "", cache.getPos().getLatDeg(
				CWPoint.DD));
		attPos.addAttribute("", "", "lon", "", cache.getPos().getLonDeg(
				CWPoint.DD));
		w.startElement("", "", "coord", attPos);
		w.endElement("", "", "coord");
		
		w.endElement("", "", "waypoint");
	}

	public void doit(Collection<ICacheHolder> caches) throws SAXException {
		w.setEncoding("utf-8");
		w.startDocument();
		w.setIndentStep("	");
		AttributesImpl att = new AttributesImpl();
		att.addAttribute("", "", "version", "", "1.0");
		att.addAttribute("", "", "src", "", "CacheHound");
		w.startElement("", "", "loc", att);
		for (ICacheHolder cache : caches) {
			handleCache(cache);
		}
		w.endElement("", "", "loc");
		w.endDocument();
	}

	private DataWriter w;

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
		};
		Collection<ICacheHolder> caches = new ArrayList<ICacheHolder>();
		caches.add(cache);

		StringWriter sw = new StringWriter();
		LocExporter exp = new LocExporter(sw);
		try {
			exp.doit(caches);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(sw.toString());
	}
}
