package de.cachehound.beans;

import CacheWolf.beans.Attributes;
import CacheWolf.beans.CacheHolder;
import CacheWolf.beans.CacheImages;
import CacheWolf.beans.ImageInfo;
import CacheWolf.beans.TravelbugList;

public interface ICacheHolderDetail {

	/**
	 * Adds a user image to the cache data
	 * 
	 * @param profile
	 */
	public void addUserImage(ImageInfo userImage);

	public Attributes getAttributes();

	public LogList getCacheLogs();

	public String getCacheNotes();

	public String getCountry();

	public String getHints();

	public CacheImages getImages();

	public String getLastUpdate();

	public CacheImages getLogImages();

	public String getLongDescription();

	public Log getOwnLog();

	public String getOwnLogId();

	public CacheHolder getParent();

	public String getSolver();

	public String getState();

	public TravelbugList getTravelbugs();

	public String getUrl();

	public CacheImages getUserImages();

	/**
	 * Return true if this cache has additional info for some pictures
	 * 
	 * @return true if cache has additional info, false otherwise
	 */
	public boolean hasImageInfo();

	public boolean hasUnsavedChanges();

	public void setAttributes(Attributes attributes);

	public void addCacheLogs(LogList newLogs);

	public void stripLogsToMaximum(int maxSize);

	public void setCacheNotes(String notes);

	public void setCountry(String country);

	public void setHints(String hints);

	public void setImages(CacheImages images);

	public void setLastUpdate(String lastUpdate);

	public void setLogImages(CacheImages logImages);

	public void setLongDescription(String longDescription);

	public void setOwnLog(Log ownLog);

	public void setOwnLogId(String ownLogId);

	public void setParent(CacheHolder parent);

	public void setSolver(String solver);

	public void setState(String state);

	public void setTravelbugs(TravelbugList travelbugs);

	public void setUnsavedChanges(boolean unsavedChanges);

	public void setUrl(String url);

	public void setUserImages(CacheImages userImages);

	/**
	 * Method to update an existing cache with new data. This is necessary to
	 * avoid missing old logs. Called from GPX Importer
	 * 
	 * @param newCh
	 *            new cache data
	 * @return CacheHolder with updated data
	 */
	public void update(ICacheHolderDetail newCh);

}