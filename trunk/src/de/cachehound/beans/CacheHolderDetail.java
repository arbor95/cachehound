package de.cachehound.beans;

import CacheWolf.Global;
import CacheWolf.beans.Attributes;
import CacheWolf.beans.CacheHolder;
import CacheWolf.beans.CacheImages;
import CacheWolf.beans.ImageInfo;
import CacheWolf.beans.Travelbug;
import CacheWolf.beans.TravelbugList;
import CacheWolf.util.MyLocale;

public class CacheHolderDetail implements ICacheHolderDetail {

	/**
	 * CacheHolder which holds the detail. <b>Only</b> set by CacheHolder when
	 * creating detail!
	 */
	private CacheHolder parent = null;
	private String longDescription = CacheHolder.EMPTY;
	private String shortDescription = CacheHolder.EMPTY;
	private String lastUpdate = CacheHolder.EMPTY;
	private String hints = CacheHolder.EMPTY;
	private LogList cacheLogs = new LogList();
	private String cacheNotes = CacheHolder.EMPTY;
	private CacheImages images = new CacheImages();
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
	public void addUserImage(ImageInfo userImage) {
		this.getUserImages().add(userImage);
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

	public boolean hasUnsavedChanges() {
		return unsavedChanges;
	}

	public void setAttributes(Attributes attributes) {
		this.attributes = attributes;
	}

	public String getShortDescription() {
		return shortDescription;
	}

	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}
	
	public void addCacheLogs(LogList newLogs) {
		int size = newLogs.size();
		for (int i = size - 1; i >= 0; i--) { // Loop over all new logs, must
			// start with oldest log
			if (cacheLogs.add(newLogs.getLog(i)) >= 0)
				getParent().setLog_updated(true);
		}
		getParent().setNoFindLogs(cacheLogs.countNotFoundLogs());
	}

	public void stripLogsToMaximum(int maxSize) {
		boolean keepOwn = Global.getPref().alwaysKeepOwnLogs;
		if (cacheLogs.purgeLogs(maxSize, keepOwn) > 0) {
			setUnsavedChanges(true);
			getParent().setLog_updated(true);
		}
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

	public void setLongDescription(String longDescription) {
		if (longDescription.equals(""))
			getParent().setNew(true);
		else if (!stripControlChars(this.longDescription).equals(
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

	public void setParent(CacheHolder parent) {
		this.parent = parent;
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

	public void setUnsavedChanges(boolean unsavedChanges) {
		this.unsavedChanges = unsavedChanges;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setUserImages(CacheImages userImages) {
		this.userImages = userImages;
	}

	private String stripControlChars(String desc) {
		StringBuilder sb = new StringBuilder(desc.length());
		for (int i = 0; i < desc.length(); i++) {
			char c = desc.charAt(i);
			if (c > ' ') {
				sb.append(c);
			}
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
	public void update(CacheHolderDetail newCh) {
		// flags
		// TODO: nicht so toll hier, CacheStatus sollte zu eine Enum werden.
		if (getParent().isFound() && getParent().getCacheStatus().equals("")) {
			getParent().setCacheStatus(MyLocale.getMsg(318, "Found"));
		}
		// travelbugs:GPX-File contains all actual travelbugs but not the
		// missions
		// we need to check whether the travelbug is already in the existing
		// list
		getParent().setHasBugs(newCh.getTravelbugs().size() > 0);
		for (int i = newCh.getTravelbugs().size() - 1; i >= 0; i--) {
			Travelbug tb = newCh.getTravelbugs().getTB(i);
			Travelbug oldTB = this.getTravelbugs().find(tb.getName());
			// If the bug is already in the cache, we keep it
			if (oldTB != null) {
				newCh.getTravelbugs().replace(i, oldTB);
			}

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
		addCacheLogs(newCh.getCacheLogs());

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
	}
}
