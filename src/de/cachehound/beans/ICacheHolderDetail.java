package de.cachehound.beans;

import CacheWolf.beans.TravelbugList;

public interface ICacheHolderDetail {

	public String getLongDescription();
	public String getShortDescription();
	public String getHints();
	
	public String getUrl();
	
	public String getCountry();
	
	public String getState();

	public LogList getCacheLogs();
	
	public TravelbugList getTravelbugs();
}
