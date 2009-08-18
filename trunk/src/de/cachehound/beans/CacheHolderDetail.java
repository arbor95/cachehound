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

	/* (non-Javadoc)
	 * @see de.cachehound.beans.ICacheHolderDetail#addUserImage(CacheWolf.beans.ImageInfo)
	 */
	public void addUserImage(ImageInfo userImage) {
		this.getUserImages().add(userImage);
	}

	/* (non-Javadoc)
	 * @see de.cachehound.beans.ICacheHolderDetail#getAttributes()
	 */
	public Attributes getAttributes() {
		return attributes;
	}

	/* (non-Javadoc)
	 * @see de.cachehound.beans.ICacheHolderDetail#getCacheLogs()
	 */
	public LogList getCacheLogs() {
		return cacheLogs;
	}

	/* (non-Javadoc)
	 * @see de.cachehound.beans.ICacheHolderDetail#getCacheNotes()
	 */
	public String getCacheNotes() {
		return this.cacheNotes;
	}

	/* (non-Javadoc)
	 * @see de.cachehound.beans.ICacheHolderDetail#getCountry()
	 */
	public String getCountry() {
		return country;
	}

	/* (non-Javadoc)
	 * @see de.cachehound.beans.ICacheHolderDetail#getHints()
	 */
	public String getHints() {
		return hints;
	}

	/* (non-Javadoc)
	 * @see de.cachehound.beans.ICacheHolderDetail#getImages()
	 */
	public CacheImages getImages() {
		return images;
	}

	/* (non-Javadoc)
	 * @see de.cachehound.beans.ICacheHolderDetail#getLastUpdate()
	 */
	public String getLastUpdate() {
		return lastUpdate;
	}

	/* (non-Javadoc)
	 * @see de.cachehound.beans.ICacheHolderDetail#getLogImages()
	 */
	public CacheImages getLogImages() {
		return logImages;
	}

	/* (non-Javadoc)
	 * @see de.cachehound.beans.ICacheHolderDetail#getLongDescription()
	 */
	public String getLongDescription() {
		return longDescription;
	}

	/* (non-Javadoc)
	 * @see de.cachehound.beans.ICacheHolderDetail#getOwnLog()
	 */
	public Log getOwnLog() {
		return ownLog;
	}

	/* (non-Javadoc)
	 * @see de.cachehound.beans.ICacheHolderDetail#getOwnLogId()
	 */
	public String getOwnLogId() {
		return ownLogId;
	}

	/* (non-Javadoc)
	 * @see de.cachehound.beans.ICacheHolderDetail#getParent()
	 */
	public CacheHolder getParent() {
		return parent;
	}

	/* (non-Javadoc)
	 * @see de.cachehound.beans.ICacheHolderDetail#getSolver()
	 */
	public String getSolver() {
		return this.solver;
	}

	/* (non-Javadoc)
	 * @see de.cachehound.beans.ICacheHolderDetail#getState()
	 */
	public String getState() {
		return state;
	}

	/* (non-Javadoc)
	 * @see de.cachehound.beans.ICacheHolderDetail#getTravelbugs()
	 */
	public TravelbugList getTravelbugs() {
		return travelbugs;
	}

	/* (non-Javadoc)
	 * @see de.cachehound.beans.ICacheHolderDetail#getUrl()
	 */
	public String getUrl() {
		return url;
	}

	/* (non-Javadoc)
	 * @see de.cachehound.beans.ICacheHolderDetail#getUserImages()
	 */
	public CacheImages getUserImages() {
		return userImages;
	}

	/* (non-Javadoc)
	 * @see de.cachehound.beans.ICacheHolderDetail#hasImageInfo()
	 */
	public boolean hasImageInfo() {
		for (int i = this.getImages().size() - 1; i >= 0; i--)
			if (this.getImages().get(i).getComment() != null)
				return true;
		return false;
	}

	/* (non-Javadoc)
	 * @see de.cachehound.beans.ICacheHolderDetail#hasUnsavedChanges()
	 */
	public boolean hasUnsavedChanges() {
		return unsavedChanges;
	}

	/* (non-Javadoc)
	 * @see de.cachehound.beans.ICacheHolderDetail#setAttributes(CacheWolf.beans.Attributes)
	 */
	public void setAttributes(Attributes attributes) {
		this.attributes = attributes;
	}

	/* (non-Javadoc)
	 * @see de.cachehound.beans.ICacheHolderDetail#addCacheLogs(de.cachehound.beans.LogList)
	 */
	public void addCacheLogs(LogList newLogs) {
		int size = newLogs.size();
		for (int i = size - 1; i >= 0; i--) { // Loop over all new logs, must
			// start with oldest log
			if (cacheLogs.add(newLogs.getLog(i)) >= 0)
				getParent().setLog_updated(true);
		}
		getParent().setNoFindLogs(cacheLogs.countNotFoundLogs());
	}

	/* (non-Javadoc)
	 * @see de.cachehound.beans.ICacheHolderDetail#stripLogsToMaximum(int)
	 */
	public void stripLogsToMaximum(int maxSize) {
		boolean keepOwn = Global.getPref().alwaysKeepOwnLogs;
		if (cacheLogs.purgeLogs(maxSize, keepOwn) > 0) {
			setUnsavedChanges(true);
			getParent().setLog_updated(true);
		}
	}

	/* (non-Javadoc)
	 * @see de.cachehound.beans.ICacheHolderDetail#setCacheNotes(java.lang.String)
	 */
	public void setCacheNotes(String notes) {
		if (!cacheNotes.equals(notes))
			getParent().setUpdated(true);
		getParent().setHasNote(!notes.trim().equals(""));
		cacheNotes = notes;
	}

	/* (non-Javadoc)
	 * @see de.cachehound.beans.ICacheHolderDetail#setCountry(java.lang.String)
	 */
	public void setCountry(String country) {
		this.country = country;
	}

	/* (non-Javadoc)
	 * @see de.cachehound.beans.ICacheHolderDetail#setHints(java.lang.String)
	 */
	public void setHints(String hints) {
		if (!hints.equals(hints))
			getParent().setUpdated(true);
		this.hints = hints;
	}

	/* (non-Javadoc)
	 * @see de.cachehound.beans.ICacheHolderDetail#setImages(CacheWolf.beans.CacheImages)
	 */
	public void setImages(CacheImages images) {
		this.images = images;
	}

	/* (non-Javadoc)
	 * @see de.cachehound.beans.ICacheHolderDetail#setLastUpdate(java.lang.String)
	 */
	public void setLastUpdate(String lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	/* (non-Javadoc)
	 * @see de.cachehound.beans.ICacheHolderDetail#setLogImages(CacheWolf.beans.CacheImages)
	 */
	public void setLogImages(CacheImages logImages) {
		this.logImages = logImages;
	}

	/* (non-Javadoc)
	 * @see de.cachehound.beans.ICacheHolderDetail#setLongDescription(java.lang.String)
	 */
	public void setLongDescription(String longDescription) {
		if (longDescription.equals(""))
			getParent().setNew(true);
		else if (!stripControlChars(this.longDescription).equals(
				stripControlChars(longDescription)))
			getParent().setUpdated(true);
		this.longDescription = longDescription;
	}

	/* (non-Javadoc)
	 * @see de.cachehound.beans.ICacheHolderDetail#setOwnLog(de.cachehound.beans.Log)
	 */
	public void setOwnLog(Log ownLog) {
		this.ownLog = ownLog;
	}

	/* (non-Javadoc)
	 * @see de.cachehound.beans.ICacheHolderDetail#setOwnLogId(java.lang.String)
	 */
	public void setOwnLogId(String ownLogId) {
		this.ownLogId = ownLogId;
	}

	/* (non-Javadoc)
	 * @see de.cachehound.beans.ICacheHolderDetail#setParent(CacheWolf.beans.CacheHolder)
	 */
	public void setParent(CacheHolder parent) {
		this.parent = parent;
	}

	/* (non-Javadoc)
	 * @see de.cachehound.beans.ICacheHolderDetail#setSolver(java.lang.String)
	 */
	public void setSolver(String solver) {
		if (!this.solver.equals(solver))
			getParent().setUpdated(true);
		this.solver = solver;
		getParent().setHasSolver(!this.solver.trim().equals(""));
	}

	/* (non-Javadoc)
	 * @see de.cachehound.beans.ICacheHolderDetail#setState(java.lang.String)
	 */
	public void setState(String state) {
		this.state = state;
	}

	/* (non-Javadoc)
	 * @see de.cachehound.beans.ICacheHolderDetail#setTravelbugs(CacheWolf.beans.TravelbugList)
	 */
	public void setTravelbugs(TravelbugList travelbugs) {
		this.travelbugs = travelbugs;
	}

	/* (non-Javadoc)
	 * @see de.cachehound.beans.ICacheHolderDetail#setUnsavedChanges(boolean)
	 */
	public void setUnsavedChanges(boolean unsavedChanges) {
		this.unsavedChanges = unsavedChanges;
	}

	/* (non-Javadoc)
	 * @see de.cachehound.beans.ICacheHolderDetail#setUrl(java.lang.String)
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/* (non-Javadoc)
	 * @see de.cachehound.beans.ICacheHolderDetail#setUserImages(CacheWolf.beans.CacheImages)
	 */
	public void setUserImages(CacheImages userImages) {
		this.userImages = userImages;
	}

	private String stripControlChars(String desc) {
		StringBuilder sb = new StringBuilder(desc.length());
		for (int i = 0; i < desc.length(); i++) {
			char c = desc.charAt(i);
			if (c > ' ')
				sb.append(c);
		}
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see de.cachehound.beans.ICacheHolderDetail#update(de.cachehound.beans.ICacheHolderDetail)
	 */
	public void update(ICacheHolderDetail newCh) {
		// flags
		// TODO: nicht so toll hier, CacheStatus sollte zu eine Enum werden.
		if (getParent().is_found() && getParent().getCacheStatus().equals("")) {
			getParent().setCacheStatus(MyLocale.getMsg(318, "Found"));
		}
		// travelbugs:GPX-File contains all actual travelbugs but not the
		// missions
		// we need to check whether the travelbug is already in the existing
		// list
		getParent().setHas_bugs(newCh.getTravelbugs().size() > 0);
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
