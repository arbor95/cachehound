package CacheWolf.beans;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import CacheWolf.Global;
import CacheWolf.util.DataMover;
import CacheWolf.util.Extractor;
import CacheWolf.util.MyLocale;
import CacheWolf.util.SafeXML;

import de.cachehound.beans.Log;
import de.cachehound.beans.LogList;
import de.cachehound.factory.CacheHolderDetailFactory;
import de.cachehound.factory.LogFactory;
import de.cachehound.types.LogType;
import de.cachehound.util.AllReader;
import ewe.filechooser.FileChooser;
import ewe.filechooser.FileChooserBase;
import ewe.ui.FormBase;
import ewe.ui.InputBox;

public class CacheHolderDetail {

	private static Logger logger = LoggerFactory
			.getLogger(CacheHolderDetail.class);

	/**
	 * CacheHolder which holds the detail. <b>Only</b> set by CacheHolder when
	 * creating detail!
	 */
	private CacheHolder parent = null;
	private String longDescription = CacheHolder.EMPTY;
	private String lastUpdate = CacheHolder.EMPTY;
	private String hints = CacheHolder.EMPTY;
	private LogList cacheLogs = new LogList();
	private String cacheNotes = CacheHolder.EMPTY;
	private CacheImages images = new CacheImages();
	private CacheImages logImages = new CacheImages();
	private CacheImages userImages = new CacheImages();
	private Attributes attributes = new Attributes();
	private TravelbugList travelbugs = new TravelbugList();
	// public String Bugs = EMPTY; Superceded by Travelbugs
	private String url = CacheHolder.EMPTY;
	private String solver = CacheHolder.EMPTY;
	private String ownLogId = CacheHolder.EMPTY;
	private Log ownLog = null;
	private String country = CacheHolder.EMPTY;
	private String state = CacheHolder.EMPTY;
	/**
	 * For faster cache import (from opencaching) changes are only written when
	 * the details are freed from memory If you want to save the changes
	 * automatically when the details are unloaded, set this to true
	 */
	private boolean unsavedChanges = false;

	/**
	 * Use CacheHolderDetailFactroy.create... instead;
	 */
	public CacheHolderDetail() {
		// empty bean constructor
	}
	
	/**
	 * Adds a user image to the cache data
	 * 
	 * @param profile
	 */
	// TODO: Move me somewhere else
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
					+ (this.getUserImages().size() + 1) + ext;

			ImageInfo userImageInfo = new ImageInfo();
			userImageInfo.setFilename(imgDestName);
			userImageInfo.setTitle(imgDesc);
			this.getUserImages().add(userImageInfo);
			// Copy File
			DataMover
					.copy(imgFile.getFullPath(), profile.dataDir + imgDestName);
			// Save Data
			CacheHolderDetailFactory.getInstance().saveCacheDetails(this, Global.getProfile().getDataDir());
		}
	}

	public Attributes getAttributes() {
		return attributes;
	}

	public LogList getCacheLogs() {
		return cacheLogs;
	}

	public String getCacheNotes() {
		return this.cacheNotes;
	}

	public String getCountry() {
		return country;
	}

	public String getHints() {
		return hints;
	}

	public CacheImages getImages() {
		return images;
	}

	public String getLastUpdate() {
		return lastUpdate;
	}

	public CacheImages getLogImages() {
		return logImages;
	}

	public String getLongDescription() {
		return longDescription;
	}

	public Log getOwnLog() {
		return ownLog;
	}

	public String getOwnLogId() {
		return ownLogId;
	}

	public CacheHolder getParent() {
		return parent;
	}

	public String getSolver() {
		return this.solver;
	}

	public String getState() {
		return state;
	}

	public TravelbugList getTravelbugs() {
		return travelbugs;
	}

	public String getUrl() {
		return url;
	}

	public CacheImages getUserImages() {
		return userImages;
	}

	/**
	 * Return true if this cache has additional info for some pictures
	 * 
	 * @return true if cache has additional info, false otherwise
	 */
	public boolean hasImageInfo() {
		for (int i = this.getImages().size() - 1; i >= 0; i--)
			if (this.getImages().get(i).getComment() != null)
				return true;
		return false;
	}


	
	public void setAttributes(Attributes attributes) {
		this.attributes = attributes;
	}

	public void setCacheLogs(LogList newLogs) {
		int size = newLogs.size();
		for (int i = size - 1; i >= 0; i--) { // Loop over all new logs, must
			// start with oldest log
			if (cacheLogs.add(newLogs.getLog(i)) >= 0)
				getParent().setLog_updated(true);
		}
		int maxKeep = Global.getPref().maxLogsToKeep;
		boolean keepOwn = Global.getPref().alwaysKeepOwnLogs;
		if (cacheLogs.purgeLogs(maxKeep, keepOwn) > 0) {
			setUnsavedChanges(true);
		}
		getParent().setNoFindLogs(cacheLogs.countNotFoundLogs());
	}

	public void setCacheNotes(String notes) {
		if (!cacheNotes.equals(notes))
			getParent().setUpdated(true);
		getParent().setHasNote(!notes.trim().equals(""));
		cacheNotes = notes;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public void setHints(String hints) {
		if (!hints.equals(hints))
			getParent().setUpdated(true);
		this.hints = hints;
	}

	public void setImages(CacheImages images) {
		this.images = images;
	}

	public void setLastUpdate(String lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public void setLogImages(CacheImages logImages) {
		this.logImages = logImages;
	}

	public void setLongDescription(String longDescription) {
		if (longDescription.equals(""))
			getParent().setNew(true);
		else if (!stripControlChars(longDescription).equals(
				stripControlChars(longDescription)))
			getParent().setUpdated(true);
		this.longDescription = longDescription;
	}

	public void setOwnLog(Log ownLog) {
		this.ownLog = ownLog;
	}

	public void setOwnLogId(String ownLogId) {
		this.ownLogId = ownLogId;
	}

	public void setSolver(String solver) {
		if (!this.solver.equals(solver))
			getParent().setUpdated(true);
		this.solver = solver;
		getParent().setHasSolver(!this.solver.trim().equals(""));
	}

	public void setState(String state) {
		this.state = state;
	}

	public void setTravelbugs(TravelbugList travelbugs) {
		this.travelbugs = travelbugs;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setUserImages(CacheImages userImages) {
		this.userImages = userImages;
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
		getParent().setHas_bugs(newCh.getTravelbugs().size() > 0);
		for (int i = newCh.getTravelbugs().size() - 1; i >= 0; i--) {
			Travelbug tb = newCh.getTravelbugs().getTB(i);
			Travelbug oldTB = this.getTravelbugs().find(tb.getName());
			// If the bug is already in the cache, we keep it
			if (oldTB != null)
				newCh.getTravelbugs().replace(i, oldTB);

		}
		this.setTravelbugs(newCh.getTravelbugs());

		if (newCh.getAttributes().getCount() > 0)
			this.setAttributes(newCh.getAttributes());

		// URL
		this.setUrl(newCh.getUrl());

		// Images
		this.setImages(newCh.getImages());

		setLongDescription(newCh.getLongDescription());
		setHints(newCh.getHints());
		setCacheLogs(newCh.getCacheLogs());

		if (newCh.getOwnLogId().length() > 0)
			this.setOwnLogId(newCh.getOwnLogId());
		if (newCh.getOwnLog() != null)
			this.setOwnLog(newCh.getOwnLog());

		if (newCh.getCountry().length() > 0)
			this.setCountry(newCh.getCountry());
		if (newCh.getState().length() > 0)
			this.setState(newCh.getState());

		if (newCh.getSolver().length() > 0)
			this.setSolver(newCh.getSolver());
		return this;
	}

	public void setUnsavedChanges(boolean unsavedChanges) {
		this.unsavedChanges = unsavedChanges;
	}

	public boolean hasUnsavedChanges() {
		return unsavedChanges;
	}

	public void setParent(CacheHolder parent) {
		this.parent = parent;
	}
}
