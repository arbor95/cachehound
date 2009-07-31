package de.cachehound.util;

import CacheWolf.CacheHolder;
import CacheWolf.CacheHolderDetail;
import CacheWolf.Global;
import CacheWolf.InfoBox;
import CacheWolf.Travelbug;
import CacheWolf.imp.SpiderGC;

public class SpiderService {

	private static SpiderService instance = new SpiderService();

	private SpiderGC spiderGC;
	private String logedInUser;
	private String lastProfileName;

	private SpiderService() {

	}

	public static SpiderService getInstance() {
		return instance;
	}

	/**
	 * Checks if there is already a spiderGC instance in here and if it is valid.
	 * If not it creates a new instance. 
	 */
	@SuppressWarnings("deprecation")
	private synchronized void checkSpiderGC() {
		// if there is no spider, the UserName changed or the Profile changed
		if (spiderGC == null || !Global.getPref().myAlias.equals(logedInUser)
				|| !Global.getProfile().name.equals(lastProfileName)) {
			spiderGC = new SpiderGC(Global.getPref(), Global.getProfile());
			logedInUser = Global.getPref().myAlias;
			lastProfileName = Global.getProfile().name;
		}
	}

	/**
	 * Method to spider a single cache. 
	 * 
	 * @return 1 if spider was successful, -1 if spider was cancelled by closing
	 *         the infobox, 0 error, but continue with next cache
	 */
	// TODO: InfoBox entfernen
	public int spiderSingle(CacheHolder ch, InfoBox pInfB, boolean loadAllLogs) {
		checkSpiderGC();
		int index = Global.getProfile().cacheDB.getIndex(ch);
		if (index == -1) {
			Global.getProfile().cacheDB.add(ch);
			index = Global.getProfile().cacheDB.getIndex(ch);
		}
		return spiderGC.spiderSingle(index, pInfB, false, loadAllLogs);
	}

	public int spiderSingle(String gcNumber, InfoBox pInfB, boolean loadAllLogs) {
		checkSpiderGC();
		int index = Global.getProfile().cacheDB.getIndex(gcNumber);
		if (index == -1) {
			CacheHolder holder = new CacheHolder(gcNumber);
			holder.getCacheDetails(true); // work around
			Global.getProfile().cacheDB.add(holder);
			index = Global.getProfile().cacheDB.getIndex(holder);
		}
		return spiderGC.spiderSingle(index, pInfB, false, loadAllLogs);
	}

	/**
	 * Legacy Method for CacheWolf.CoordsScreen
	 * 
	 * Fetch the coordinates of a waypoint from GC
	 * 
	 * @param wayPoint
	 *            the name of the waypoint
	 * @return the cache coordinates
	 */
	@Deprecated
	public String getCacheCoordinates(String wayPoint) {
		checkSpiderGC();
		return spiderGC.getCacheCoordinates(wayPoint);
	}

	/**
	 * Performs an initial fetch to a given address. In this case it should be a
	 * gc.com address. This method is used to obtain the result of a search for
	 * caches screen.
	 */
	public String fetchGCSite(String address) {
		checkSpiderGC();
		return spiderGC.fetch(address);
	}

	/**
	 * Method to start the spider for a search around the centre coordinates
	 */
	public void spiderAroundCenterCoordinates(boolean spiderAllLogs) {
		checkSpiderGC();
		spiderGC.doIt(spiderAllLogs);
	}
	
	/**
	 * Get the images for a previously fetched cache page. Images are extracted
	 * from two areas: The long description and the pictures section (including
	 * the spoiler)
	 * 
	 * @param doc
	 *            The previously fetched cachepage
	 * @param chD
	 *            The Cachedetails
	 */
	public void getImages(String doc, CacheHolderDetail chD) {
		checkSpiderGC();
		spiderGC.getImages(doc, chD);
	}
	
	/**
	 * Gets the attributes out of the given String and saves in in the cacheDetails
	 * @param doc The full webpage of the asked geocache.
	 * @param chD the Details for the asked geocache.
	 * @throws Exception 
	 */
	public void getAttributes(String doc, CacheHolderDetail chD) throws Exception {
		checkSpiderGC();
		spiderGC.getAttributes(doc, chD);
	}
	
	/**
	 * Load the bug id for a given name. This method is not ideal, as there are
	 * sometimes several bugs with identical names but different IDs. Normally
	 * the bug GUID is used which can be obtained from the cache page.<br>
	 * Note that each bug has both an ID and a GUID.
	 * 
	 * @param name
	 *            The name (or partial name) of a travelbug
	 * @return the id of the bug
	 */
	public String getBugId(String name) {
		checkSpiderGC();
		return spiderGC.getBugId(name);
	}
	
	/**
	 * Fetch a bug's mission for a given GUID or ID. If the guid String is
	 * longer than 10 characters it is assumed to be a GUID, otherwise it is an
	 * ID.
	 * 
	 * @param guid
	 *            the guid or id of the travelbug
	 * @return The mission
	 */
	public String getBugMissionByGuid(String guid) {
		checkSpiderGC();
		return spiderGC.getBugMissionByGuid(guid);
	}
	
	/**
	 * Fetch a bug's mission and namefor a given tracking number
	 * 
	 * @param TB
	 *            the travelbug
	 * @return true if suceeded
	 */
	public boolean getBugMissionAndNameByTrackNr(Travelbug tb) {
		checkSpiderGC();
		return spiderGC.getBugMissionAndNameByTrackNr(tb);		
	}
}
