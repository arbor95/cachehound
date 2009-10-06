package de.cachehound.factory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import CacheWolf.Global;
import CacheWolf.beans.CacheHolder;
import CacheWolf.beans.ImageInfo;
import CacheWolf.util.Extractor;
import CacheWolf.util.SafeXML;
import de.cachehound.beans.CacheHolderDetail;
import de.cachehound.types.LogType;
import de.cachehound.util.AllReader;

public class CacheHolderDetailFactory {

	private static CacheHolderDetailFactory instance = new CacheHolderDetailFactory();

	private static Logger logger = LoggerFactory
			.getLogger(CacheHolderDetailFactory.class);

	private CacheHolderDetailFactory() {
		// singleton pattern
	}

	public static CacheHolderDetailFactory getInstance() {
		return instance;
	}

	public CacheHolderDetail createEmptyCacheHolderDetail(CacheHolder ch) {
		CacheHolderDetail chd = new CacheHolderDetail();
		chd.setParent(ch);
		return chd;
	}

	/**
	 * Method to parse a specific cache.xml file. It fills information on cache
	 * details, hints, logs, notes and images.
	 */
	public CacheHolderDetail createCacheHolderDetailFromFile(CacheHolder ch,
			File dir) throws IOException {
		CacheHolderDetail chd = new CacheHolderDetail();
		chd.setParent(ch);
		String dummy;
		// If parent cache has empty waypoint then don't do anything. This might
		// happen
		// when a cache object is freshly created to serve as container for
		// imported data
		if (chd.getParent().getWayPoint().equals(CacheHolder.EMPTY))
			return chd; // return empfy chd
		// TODO: check if null would be better or not.

		File cacheFile = new File(dir, chd.getParent().getWayPoint()
				.toLowerCase()
				+ ".xml");
		logger.debug("Reading file {}", cacheFile.getPath());

		AllReader in = new AllReader(new FileReader(cacheFile));
		String text = in.readAll();
		in.close();

		Extractor ex = new Extractor(text, "<DETAILS><![CDATA[",
				"]]></DETAILS>", 0, true);
		chd.setLongDescription(ex.findNext());
		ex = new Extractor(text, "<COUNTRY><![CDATA[", "]]></COUNTRY>", 0, true);
		chd.setCountry(ex.findNext());
		ex = new Extractor(text, "<STATE><![CDATA[", "]]></STATE>", 0, true);
		chd.setState(ex.findNext());
		// Attributes
		ex = new Extractor(text, "<ATTRIBUTES>", "</ATTRIBUTES>", 0, true);
		chd.getAttributes().XmlAttributesEnd(ex.findNext());

		ex = new Extractor(text, "<HINTS><![CDATA[", "]]></HINTS>", 0, true);
		chd.setHints(ex.findNext());
		ex = new Extractor(text, "<LOGS>", "</LOGS>", 0, true);
		dummy = ex.findNext();
		ex = new Extractor(dummy, "<OWNLOGID>", "</OWNLOGID>", 0, true);
		chd.setOwnLogId(ex.findNext());
		ex = new Extractor(dummy, "<OWNLOG><![CDATA[", "]]></OWNLOG>", 0, true);
		String ownLogText = ex.findNext();
		if (ownLogText.length() > 0) {
			if (ownLogText.indexOf("<img src='") >= 0) {
				chd.setOwnLog(LogFactory.getInstance().createFromProfileLine(
						ownLogText + "]]>"));
			} else {
				chd.setOwnLog(LogFactory.getInstance().createLog(LogType.FOUND,
						"1900-01-01", Global.getPref().myAlias, ownLogText));
			}
		} else {
			chd.setOwnLog(null);
		}
		chd.getCacheLogs().clear();
		ex = new Extractor(dummy, "<LOG>", "</LOG>", 0, true);

		dummy = ex.findNext();
		while (ex.endOfSearch() == false) {
			chd.getCacheLogs().add(
					LogFactory.getInstance().createFromProfileLine(dummy));
			dummy = ex.findNext();
		}
		ex = new Extractor(text, "<NOTES><![CDATA[", "]]></NOTES>", 0, true);
		chd.setCacheNotes(ex.findNext());
		chd.getImages().clear();
		ex = new Extractor(text, "<IMG>", "</IMG>", 0, true);
		dummy = ex.findNext();
		while (ex.endOfSearch() == false) {
			ImageInfo imageInfo = new ImageInfo();
			int pos = dummy.indexOf("<URL>");
			if (pos > 0) {
				imageInfo.setFilename(SafeXML.strxmldecode(dummy.substring(0,
						pos)));
				imageInfo.setURL(SafeXML.strxmldecode((dummy.substring(pos + 5,
						dummy.indexOf("</URL>")))));
			} else {
				imageInfo.setFilename(SafeXML.strxmldecode(dummy));
			}
			chd.getImages().add(imageInfo);
			dummy = ex.findNext();
		}
		ex = new Extractor(text, "<IMGTEXT>", "</IMGTEXT>", 0, true);
		dummy = ex.findNext();
		int imgNr = 0;
		while (ex.endOfSearch() == false) {
			ImageInfo imageInfo = chd.getImages().get(imgNr);
			int pos = dummy.indexOf("<DESC>");
			if (pos > 0) {
				imageInfo.setTitle(dummy.substring(0, pos));
				imageInfo.setComment(dummy.substring(pos + 6, dummy
						.indexOf("</DESC>")));
			} else {
				imageInfo.setTitle(dummy);
			}
			dummy = ex.findNext();
			imgNr = imgNr + 1;
		}
		chd.getUserImages().clear();
		ex = new Extractor(text, "<USERIMG>", "</USERIMG>", 0, true);
		dummy = ex.findNext();
		while (ex.endOfSearch() == false) {
			ImageInfo imageInfo = new ImageInfo();
			imageInfo.setFilename(dummy);
			chd.getUserImages().add(imageInfo);
			dummy = ex.findNext();
		}
		ex = new Extractor(text, "<USERIMGTEXT>", "</USERIMGTEXT>", 0, true);
		dummy = ex.findNext();
		imgNr = 0;
		while (ex.endOfSearch() == false) {
			ImageInfo imageInfo = chd.getUserImages().get(imgNr++);
			imageInfo.setTitle(dummy);
			dummy = ex.findNext();
		}

		ex = new Extractor(text, "<TRAVELBUGS>", "</TRAVELBUGS>", 0, false);
		dummy = ex.findNext();
		if (ex.endOfSearch()) {
			ex = new Extractor(text, "<BUGS><![CDATA[", "]]></BUGS>", 0, true);
			String Bugs = ex.findNext();
			chd.getTravelbugs().addFromHTML(Bugs);
		} else
			chd.getTravelbugs().addFromXML(dummy);

		ex = new Extractor(text, "<URL><![CDATA[", "]]></URL>", 0, true);
		// if no URL is stored, set default URL (at this time only possible for
		// gc.com)
		dummy = ex.findNext();
		if (dummy.length() > 10) {
			chd.setUrl(dummy);
		} else {
			if (chd.getParent().getWayPoint().startsWith("GC")) {
				chd
						.setUrl("http://www.geocaching.com/seek/cache_details.aspx?wp="
								+ chd.getParent().getWayPoint());
			}
		}
		ex = new Extractor(text, "<SOLVER><![CDATA[", "]]></SOLVER>", 0, true);
		chd.setSolver(ex.findNext());

		return chd;
	}

	/**
	 * Method to save a cache.xml file.
	 */
	public void saveCacheDetails(CacheHolderDetail chd, File dir) {
		BufferedWriter detfile;

		File cacheFile = new File(dir, chd.getParent().getWayPoint()
				.toLowerCase()
				+ ".xml");
		cacheFile.delete();

		try {
			detfile = new BufferedWriter(new FileWriter(cacheFile));
		} catch (IOException e) {
			logger.error("Problem creating details file for cache "
					+ chd.getParent().getWayPoint(), e);
			return;
		}
		try {
			if (chd.getParent().getWayPoint().length() > 0) {
				detfile
						.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
				detfile.newLine();
				detfile.write("<CACHEDETAILS>");
				detfile.newLine();
				detfile.write("<VERSION value = \"3\"/>");
				detfile.newLine();
				detfile.write("<DETAILS><![CDATA[" + chd.getLongDescription()
						+ "]]></DETAILS>");
				detfile.newLine();
				detfile.write("<COUNTRY><![CDATA[" + chd.getCountry()
						+ "]]></COUNTRY>");
				detfile.newLine();
				detfile.write("<STATE><![CDATA[" + chd.getState()
						+ "]]></STATE>");
				detfile.newLine();
				detfile.write(chd.getAttributes().XmlAttributesWrite());
				detfile.newLine();
				detfile.write("<HINTS><![CDATA[" + chd.getHints()
						+ "]]></HINTS>");
				detfile.newLine();
				detfile.write("<LOGS>");
				detfile.newLine();
				detfile.write("<OWNLOGID>" + chd.getOwnLogId() + "</OWNLOGID>");
				detfile.newLine();
				if (chd.getOwnLog() != null) {
					detfile.write("<OWNLOG><![CDATA["
							+ LogFactory.getInstance().toHtml(chd.getOwnLog())
							+ "]]></OWNLOG>");
					detfile.newLine();
				} else {
					detfile.write("<OWNLOG><![CDATA[]]></OWNLOG>");
					detfile.newLine();
				}
				for (int i = 0; i < chd.getCacheLogs().size(); i++) {
					detfile.write(LogFactory.getInstance().toXMLSnippet(
							chd.getCacheLogs().getLog(i)));
					detfile.newLine();
				}
				detfile.write("</LOGS>");
				detfile.newLine();
				detfile.write("<NOTES><![CDATA[" + chd.getCacheNotes()
						+ "]]></NOTES>");
				detfile.newLine();
				detfile.write("<IMAGES>\n");
				detfile.newLine();
				String stbuf = new String();
				for (int i = 0; i < chd.getImages().size(); i++) {
					stbuf = chd.getImages().get(i).getFilename();
					String urlBuf = chd.getImages().get(i).getURL();
					if (urlBuf != null && !urlBuf.equals("")) {
						detfile.write("    <IMG>" + SafeXML.strxmlencode(stbuf)
								+ "<URL>" + SafeXML.strxmlencode(urlBuf)
								+ "</URL></IMG>");
						detfile.newLine();
					} else {
						detfile.write("    <IMG>" + SafeXML.strxmlencode(stbuf)
								+ "</IMG>");
						detfile.newLine();
					}
				}
				int iis = chd.getImages().size();
				for (int i = 0; i < iis; i++) {
					stbuf = chd.getImages().get(i).getTitle();
					if (i < iis && chd.getImages().get(i).getComment() != null) {
						detfile.write("    <IMGTEXT>" + stbuf + "<DESC>"
								+ chd.getImages().get(i).getComment()
								+ "</DESC></IMGTEXT>");
						detfile.newLine();
					} else {
						detfile.write("    <IMGTEXT>" + stbuf + "</IMGTEXT>");
						detfile.newLine();
					}
				}

				for (int i = 0; i < chd.getUserImages().size(); i++) {
					stbuf = chd.getUserImages().get(i).getFilename();
					detfile.write("    <USERIMG>" + stbuf + "</USERIMG>");
					detfile.newLine();
				}
				for (int i = 0; i < chd.getUserImages().size(); i++) {
					stbuf = chd.getUserImages().get(i).getTitle();
					detfile.write("    <USERIMGTEXT>" + stbuf
							+ "</USERIMGTEXT>");
					detfile.newLine();
				}

				detfile.write("</IMAGES>");
				detfile.newLine();
				// detfile.write("<BUGS><![CDATA[");
				// detfile.write(Bugs+"");
				// detfile.write("]]></BUGS>");
				detfile.write(chd.getTravelbugs().toXML());
				detfile.newLine();
				detfile.write("<URL><![CDATA[" + chd.getUrl() + "]]></URL>");
				detfile.newLine();
				detfile.write("<SOLVER><![CDATA[" + chd.getSolver()
						+ "]]></SOLVER>");
				detfile.newLine();
				detfile.write(chd.getParent().toXML()); // This will allow
				// restoration of index.xml
				detfile.newLine();
				detfile.write("</CACHEDETAILS>");
				detfile.newLine();
				logger.debug("Writing file: {}.xml", chd.getParent()
						.getWayPoint().toLowerCase());
				detfile.close();
				chd.setUnsavedChanges(false);
			} // if length
		} catch (Exception e) {
			logger.error("Problem waypoint " + chd.getParent().getWayPoint()
					+ " writing to a details file. ", e);
			try {
				detfile.close();
			} catch (IOException ioE) {
				logger.error("Problem at closing the writing for waypoint "
						+ chd.getParent().getWayPoint()
						+ " to a details file: ", ioE);
			}
		}
	}

}
