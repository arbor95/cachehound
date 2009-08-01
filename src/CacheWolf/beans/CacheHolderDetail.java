package CacheWolf.beans;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import CacheWolf.util.DataMover;
import CacheWolf.util.Extractor;
import CacheWolf.util.MyLocale;
import CacheWolf.util.SafeXML;

import de.cachehound.types.LogType;
import de.cachehound.util.AllReader;
import ewe.filechooser.FileChooser;
import ewe.filechooser.FileChooserBase;
import ewe.ui.FormBase;
import ewe.ui.InputBox;

public class CacheHolderDetail {

	private static Logger logger = LoggerFactory.getLogger(CacheHolderDetail.class);
	
	/**
	 * CacheHolder which holds the detail. <b>Only</b> set by CacheHolder when
	 * creating detail!
	 */
	private CacheHolder parent = null;
	public String LongDescription = CacheHolder.EMPTY;
	public String LastUpdate = CacheHolder.EMPTY;
	public String Hints = CacheHolder.EMPTY;
	public LogList CacheLogs = new LogList();
	private String CacheNotes = CacheHolder.EMPTY;
	public CacheImages images = new CacheImages();
	public CacheImages logImages = new CacheImages();
	public CacheImages userImages = new CacheImages();
	public Attributes attributes = new Attributes();
	public TravelbugList Travelbugs = new TravelbugList();
	// public String Bugs = EMPTY; Superceded by Travelbugs
	public String URL = CacheHolder.EMPTY;
	private String Solver = CacheHolder.EMPTY;
	public String OwnLogId = CacheHolder.EMPTY;
	public Log OwnLog = null;
	public String Country = CacheHolder.EMPTY;
	public String State = CacheHolder.EMPTY;
	/**
	 * For faster cache import (from opencaching) changes are only written when
	 * the details are freed from memory If you want to save the changes
	 * automatically when the details are unloaded, set this to true
	 */
	public boolean hasUnsavedChanges = false;

	public CacheHolderDetail(CacheHolder ch) {
		parent = ch;
	}

	public CacheHolder getParent() {
		return parent;
	}

	public void setLongDescription(String longDescription) {
		if (LongDescription.equals(""))
			getParent().setNew(true);
		else if (!stripControlChars(LongDescription).equals(
				stripControlChars(longDescription)))
			getParent().setUpdated(true);
		LongDescription = longDescription;
	}

	private String stripControlChars(String desc) {
		StringBuffer sb = new StringBuffer(desc.length());
		for (int i = 0; i < desc.length(); i++) {
			char c = desc.charAt(i);
			if (c > ' ')
				sb.append(c);
		}
		return sb.toString();
	}

	public void setHints(String hints) {
		if (!Hints.equals(hints))
			getParent().setUpdated(true);
		Hints = hints;
	}

	public void setSolver(String solver) {
		if (!Solver.equals(solver))
			getParent().setUpdated(true);
		getParent().setHasSolver(!solver.trim().equals(""));
		Solver = solver;
	}

	public String getSolver() {
		return this.Solver;
	}

	public void setCacheNotes(String notes) {
		if (!CacheNotes.equals(notes))
			getParent().setUpdated(true);
		getParent().setHasNote(!notes.trim().equals(""));
		CacheNotes = notes;
	}

	public String getCacheNotes() {
		return this.CacheNotes;
	}

	public void setCacheLogs(LogList newLogs) {
		int size = newLogs.size();
		for (int i = size - 1; i >= 0; i--) { // Loop over all new logs, must
			// start with oldest log
			if (CacheLogs.add(newLogs.getLog(i)) >= 0)
				getParent().setLog_updated(true);
		}
		if (CacheLogs.purgeLogs() > 0)
			hasUnsavedChanges = true;
		getParent().setNoFindLogs(CacheLogs.countNotFoundLogs());
	}

	/**
	 * Method to update an existing cache with new data. This is necessary to
	 * avoid missing old logs. Called from GPX Importer
	 * 
	 * @param newCh
	 *            new cache data
	 * @return CacheHolder with updated data
	 */
	public CacheHolderDetail update(CacheHolderDetail newCh) {
		// flags
		if (getParent().is_found() && getParent().getCacheStatus().equals(""))
			getParent().setCacheStatus(MyLocale.getMsg(318, "Found"));

		// travelbugs:GPX-File contains all actual travelbugs but not the
		// missions
		// we need to check whether the travelbug is already in the existing
		// list
		getParent().setHas_bugs(newCh.Travelbugs.size() > 0);
		for (int i = newCh.Travelbugs.size() - 1; i >= 0; i--) {
			Travelbug tb = newCh.Travelbugs.getTB(i);
			Travelbug oldTB = this.Travelbugs.find(tb.getName());
			// If the bug is already in the cache, we keep it
			if (oldTB != null)
				newCh.Travelbugs.replace(i, oldTB);

		}
		this.Travelbugs = newCh.Travelbugs;

		if (newCh.attributes.getCount() > 0)
			this.attributes = newCh.attributes;

		// URL
		this.URL = newCh.URL;

		// Images
		this.images = newCh.images;

		setLongDescription(newCh.LongDescription);
		setHints(newCh.Hints);
		setCacheLogs(newCh.CacheLogs);

		if (newCh.OwnLogId.length() > 0)
			this.OwnLogId = newCh.OwnLogId;
		if (newCh.OwnLog != null)
			this.OwnLog = newCh.OwnLog;

		if (newCh.Country.length() > 0)
			this.Country = newCh.Country;
		if (newCh.State.length() > 0)
			this.State = newCh.State;

		if (newCh.getSolver().length() > 0)
			this.setSolver(newCh.getSolver());
		return this;
	}

	/**
	 * Adds a user image to the cache data
	 * 
	 * @param profile
	 */
	public void addUserImage(Profile profile) {
		ewe.io.File imgFile;
		String imgDesc, imgDestName;

		// Get Image and description
		FileChooser fc = new FileChooser(FileChooserBase.OPEN, profile.dataDir);
		fc.setTitle("Select image file:");
		if (fc.execute() != FormBase.IDCANCEL) {
			imgFile = fc.getChosenFile();
			imgDesc = new InputBox("Description").input("", 10);
			// Create Destination Filename
			String ext = imgFile.getFileExt().substring(
					imgFile.getFileExt().lastIndexOf("."));
			imgDestName = getParent().getWayPoint() + "_U_"
					+ (this.userImages.size() + 1) + ext;

			ImageInfo userImageInfo = new ImageInfo();
			userImageInfo.setFilename(imgDestName);
			userImageInfo.setTitle(imgDesc);
			this.userImages.add(userImageInfo);
			// Copy File
			DataMover
					.copy(imgFile.getFullPath(), profile.dataDir + imgDestName);
			// Save Data
			saveCacheDetails(profile.dataDir);
		}
	}

	/**
	 * Method to parse a specific cache.xml file. It fills information on cache
	 * details, hints, logs, notes and images.
	 */
	void readCache(String dir) throws IOException {
		String dummy;
		ImageInfo imageInfo;
		// If parent cache has empty waypoint then don't do anything. This might
		// happen
		// when a cache object is freshly created to serve as container for
		// imported data
		if (this.getParent().getWayPoint().equals(CacheHolder.EMPTY))
			return;

		File cacheFile = new File(dir, getParent().getWayPoint().toLowerCase()
				+ ".xml");
		logger.debug("Reading file {}", cacheFile.getPath());

		AllReader in = new AllReader(new FileReader(cacheFile));
		String text = in.readAll();
		in.close();

		Extractor ex = new Extractor(text, "<DETAILS><![CDATA[",
				"]]></DETAILS>", 0, true);
		LongDescription = ex.findNext();
		ex = new Extractor(text, "<COUNTRY><![CDATA[", "]]></COUNTRY>", 0, true);
		Country = ex.findNext();
		ex = new Extractor(text, "<STATE><![CDATA[", "]]></STATE>", 0, true);
		State = ex.findNext();
		// Attributes
		ex = new Extractor(text, "<ATTRIBUTES>", "</ATTRIBUTES>", 0, true);
		attributes.XmlAttributesEnd(ex.findNext());

		ex = new Extractor(text, "<HINTS><![CDATA[", "]]></HINTS>", 0, true);
		Hints = ex.findNext();
		ex = new Extractor(text, "<LOGS>", "</LOGS>", 0, true);
		dummy = ex.findNext();
		ex = new Extractor(dummy, "<OWNLOGID>", "</OWNLOGID>", 0, true);
		OwnLogId = ex.findNext();
		ex = new Extractor(dummy, "<OWNLOG><![CDATA[", "]]></OWNLOG>", 0, true);
		String ownLogText = ex.findNext();
		if (ownLogText.length() > 0) {
			if (ownLogText.indexOf("<img src='") >= 0) {
				OwnLog = new Log(ownLogText + "]]>");
			} else {
				OwnLog = new Log(LogType.FOUND, "1900-01-01",
						Global.getPref().myAlias, ownLogText);
			}
		} else {
			OwnLog = null;
		}
		CacheLogs.clear();
		ex = new Extractor(dummy, "<LOG>", "</LOG>", 0, true);

		dummy = ex.findNext();
		while (ex.endOfSearch() == false) {
			CacheLogs.add(new Log(dummy));
			dummy = ex.findNext();
		}
		ex = new Extractor(text, "<NOTES><![CDATA[", "]]></NOTES>", 0, true);
		CacheNotes = ex.findNext();
		images.clear();
		ex = new Extractor(text, "<IMG>", "</IMG>", 0, true);
		dummy = ex.findNext();
		while (ex.endOfSearch() == false) {
			imageInfo = new ImageInfo();
			int pos = dummy.indexOf("<URL>");
			if (pos > 0) {
				imageInfo.setFilename(SafeXML.strxmldecode(dummy.substring(0,
						pos)));
				imageInfo.setURL(SafeXML.strxmldecode((dummy.substring(pos + 5,
						dummy.indexOf("</URL>")))));
			} else {
				imageInfo.setFilename(SafeXML.strxmldecode(dummy));
			}
			this.images.add(imageInfo);
			dummy = ex.findNext();
		}
		ex = new Extractor(text, "<IMGTEXT>", "</IMGTEXT>", 0, true);
		dummy = ex.findNext();
		int imgNr = 0;
		while (ex.endOfSearch() == false) {
			imageInfo = this.images.get(imgNr);
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
		// Logimages
		logImages.clear();
		ex = new Extractor(text, "<LOGIMG>", "</LOGIMG>", 0, true);
		dummy = ex.findNext();
		while (ex.endOfSearch() == false) {
			imageInfo = new ImageInfo();
			imageInfo.setFilename(dummy);
			logImages.add(imageInfo);
			dummy = ex.findNext();
		}
		ex = new Extractor(text, "<LOGIMGTEXT>", "</LOGIMGTEXT>", 0, true);
		dummy = ex.findNext();
		imgNr = 0;
		while (ex.endOfSearch() == false) {
			imageInfo = logImages.get(imgNr++);
			imageInfo.setTitle(dummy);
			dummy = ex.findNext();
		}

		userImages.clear();
		ex = new Extractor(text, "<USERIMG>", "</USERIMG>", 0, true);
		dummy = ex.findNext();
		while (ex.endOfSearch() == false) {
			imageInfo = new ImageInfo();
			imageInfo.setFilename(dummy);
			userImages.add(imageInfo);
			dummy = ex.findNext();
		}
		ex = new Extractor(text, "<USERIMGTEXT>", "</USERIMGTEXT>", 0, true);
		dummy = ex.findNext();
		imgNr = 0;
		while (ex.endOfSearch() == false) {
			imageInfo = userImages.get(imgNr++);
			imageInfo.setTitle(dummy);
			dummy = ex.findNext();
		}

		ex = new Extractor(text, "<TRAVELBUGS>", "</TRAVELBUGS>", 0, false);
		dummy = ex.findNext();
		if (ex.endOfSearch()) {
			ex = new Extractor(text, "<BUGS><![CDATA[", "]]></BUGS>", 0, true);
			String Bugs = ex.findNext();
			Travelbugs.addFromHTML(Bugs);
		} else
			Travelbugs.addFromXML(dummy);

		ex = new Extractor(text, "<URL><![CDATA[", "]]></URL>", 0, true);
		// if no URL is stored, set default URL (at this time only possible for
		// gc.com)
		dummy = ex.findNext();
		if (dummy.length() > 10) {
			URL = dummy;
		} else {
			if (getParent().getWayPoint().startsWith("GC")) {
				URL = "http://www.geocaching.com/seek/cache_details.aspx?wp="
						+ getParent().getWayPoint();
			}
		}
		ex = new Extractor(text, "<SOLVER><![CDATA[", "]]></SOLVER>", 0, true);
		this.setSolver(ex.findNext());
	}

	/**
	 * Method to save a cache.xml file.
	 */
	public void saveCacheDetails(String dir) {
		BufferedWriter detfile;

		File cacheFile = new File(dir, getParent().getWayPoint().toLowerCase()
				+ ".xml");
		cacheFile.delete();

		try {
			detfile = new BufferedWriter(new FileWriter(cacheFile));
		} catch (IOException e) {
			logger.error("Problem creating details file for cache " + getParent().getWayPoint(), e);
			return;
		}
		try {
			if (getParent().getWayPoint().length() > 0) {
				detfile
						.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
				detfile.newLine();
				detfile.write("<CACHEDETAILS>");
				detfile.newLine();
				detfile.write("<VERSION value = \"3\"/>");
				detfile.newLine();
				detfile.write("<DETAILS><![CDATA[" + LongDescription
						+ "]]></DETAILS>");
				detfile.newLine();
				detfile.write("<COUNTRY><![CDATA[" + Country + "]]></COUNTRY>");
				detfile.newLine();
				detfile.write("<STATE><![CDATA[" + State + "]]></STATE>");
				detfile.newLine();
				detfile.write(attributes.XmlAttributesWrite());
				detfile.newLine();
				detfile.write("<HINTS><![CDATA[" + Hints + "]]></HINTS>");
				detfile.newLine();
				detfile.write("<LOGS>");
				detfile.newLine();
				detfile.write("<OWNLOGID>" + OwnLogId + "</OWNLOGID>");
				detfile.newLine();
				if (OwnLog != null) {
					detfile.write("<OWNLOG><![CDATA[" + OwnLog.toHtml()
							+ "]]></OWNLOG>");
					detfile.newLine();
				} else {
					detfile.write("<OWNLOG><![CDATA[]]></OWNLOG>");
					detfile.newLine();
				}
				for (int i = 0; i < CacheLogs.size(); i++) {
					detfile.write(CacheLogs.getLog(i).toXML());
					detfile.newLine();
				}
				detfile.write("</LOGS>");
				detfile.newLine();
				detfile.write("<NOTES><![CDATA[" + CacheNotes + "]]></NOTES>");
				detfile.newLine();
				detfile.write("<IMAGES>\n");
				detfile.newLine();
				String stbuf = new String();
				for (int i = 0; i < images.size(); i++) {
					stbuf = images.get(i).getFilename();
					String urlBuf = images.get(i).getURL();
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
				int iis = images.size();
				for (int i = 0; i < iis; i++) {
					stbuf = images.get(i).getTitle();
					if (i < iis && images.get(i).getComment() != null) {
						detfile.write("    <IMGTEXT>" + stbuf + "<DESC>"
								+ images.get(i).getComment()
								+ "</DESC></IMGTEXT>");
						detfile.newLine();
					} else {
						detfile.write("    <IMGTEXT>" + stbuf + "</IMGTEXT>");
						detfile.newLine();
					}
				}

				for (int i = 0; i < logImages.size(); i++) {
					stbuf = logImages.get(i).getFilename();
					detfile.write("    <LOGIMG>" + stbuf + "</LOGIMG>");
					detfile.newLine();
				}
				for (int i = 0; i < logImages.size(); i++) {
					stbuf = logImages.get(i).getTitle();
					detfile.write("    <LOGIMGTEXT>" + stbuf + "</LOGIMGTEXT>");
					detfile.newLine();
				}
				for (int i = 0; i < userImages.size(); i++) {
					stbuf = userImages.get(i).getFilename();
					detfile.write("    <USERIMG>" + stbuf + "</USERIMG>");
					detfile.newLine();
				}
				for (int i = 0; i < userImages.size(); i++) {
					stbuf = userImages.get(i).getTitle();
					detfile.write("    <USERIMGTEXT>" + stbuf
							+ "</USERIMGTEXT>");
					detfile.newLine();
				}

				detfile.write("</IMAGES>");
				detfile.newLine();
				// detfile.write("<BUGS><![CDATA[");
				// detfile.write(Bugs+"");
				// detfile.write("]]></BUGS>");
				detfile.write(Travelbugs.toXML());
				detfile.newLine();
				detfile.write("<URL><![CDATA[" + URL + "]]></URL>");
				detfile.newLine();
				detfile.write("<SOLVER><![CDATA[" + getSolver()
						+ "]]></SOLVER>");
				detfile.newLine();
				detfile.write(getParent().toXML()); // This will allow
				// restoration of index.xml
				detfile.newLine();
				detfile.write("</CACHEDETAILS>");
				detfile.newLine();
				logger.debug ("Writing file: {}.xml",getParent().getWayPoint().toLowerCase()); 
			} // if length
		} catch (Exception e) {
			logger. error("Problem waypoint " + getParent().getWayPoint()
							+ " writing to a details file. ", e);
		}
		try {
			detfile.close();
		} catch (IOException e) {
			logger.error("Problem at closing the writing for waypoint " + getParent().getWayPoint()
							+ " to a details file: ", e);
		}
		hasUnsavedChanges = false;
	}

	/**
	 * Return true if this cache has additional info for some pictures
	 * 
	 * @return true if cache has additional info, false otherwise
	 */
	public boolean hasImageInfo() {
		for (int i = this.images.size() - 1; i >= 0; i--)
			if (this.images.get(i).getComment() != null)
				return true;
		return false;
	}
}
