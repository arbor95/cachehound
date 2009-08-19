package CacheWolf.beans;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import CacheWolf.Global;
import CacheWolf.gui.GuiImageBroker;
import CacheWolf.gui.myTableModel;
import CacheWolf.navi.Metrics;
import CacheWolf.util.DateFormat;
import CacheWolf.util.MyLocale;
import CacheWolf.util.SafeXML;

import com.stevesoft.ewe_pat.Regex;

import de.cachehound.beans.CacheHolderDetailSoft;
import de.cachehound.beans.ICacheHolder;
import de.cachehound.beans.ICacheHolderDetail;
import de.cachehound.beans.LogList;
import de.cachehound.factory.CacheHolderDetailFactory;
import de.cachehound.types.Bearing;
import de.cachehound.types.CacheSize;
import de.cachehound.types.CacheType;
import de.cachehound.types.Difficulty;
import de.cachehound.types.Terrain;
import ewe.fx.FontMetrics;
import ewe.fx.IconAndText;
import ewe.sys.Convert;

/**
 * A class to hold information on a cache.<br>
 * Not all attributes are filled at once. You will have to look at other classes
 * and methods to get more information.
 * 
 */
public class CacheHolder implements ICacheHolder {

	private static Logger logger = LoggerFactory.getLogger(CacheHolder.class);

	private static final String NOBEARING = "?";
	public static final String EMPTY = "";

	/**
	 * Cachestatus is Found, Not found or a date in format yyyy-mm-dd hh:mm for
	 * found date
	 */
	// TODO: Um das ganze Typsicher und auch ein Profil international zu machens
	// sollte hier ne Enum stellt werden.
	private String cacheStatus = EMPTY;
	/**
	 * The name of the waypoint typicall GC.... or OC.... or CW...... (can be
	 * any characters)
	 */
	private String wayPoint = EMPTY;
	/** The name of the cache (short description) */
	private String cacheName = EMPTY;
	/** The alias of the owner */
	private String cacheOwner = EMPTY;
	/** The coordinates of the cache */
	private CWPoint pos = new CWPoint();
	/** The date when the cache was hidden in format yyyy-mm-dd */
	private String dateHidden = EMPTY;
	/** The size of the cache (as per GC cache sizes Micro, Small, ....) */
	private CacheSize cacheSize = CacheSize.NOT_CHOSEN;
	/** The distance from the centre in km */
	private double kilom = -1;
	/** The angle (0=North, 180=South) from the current centre to this point */
	private double degrees = 0;
	/** The difficulty of the cache from 1 to 5 in .5 incements */
	private Difficulty difficulty = Difficulty.DIFFICULTY_ERROR;
	/** The terrain rating of the cache from 1 to 5 in .5 incements */
	private Terrain terrain = Terrain.TERRAIN_ERROR;
	/** The cache type (@see CacheType for translation table) */
	private CacheType type = CacheType.ERROR;
	/** True if the cache has been archived */
	private boolean archived = false;
	/** True if the cache is available for searching */
	private boolean available = true;
	/** True if we own this cache */
	private boolean owned = false;
	/** True if we have found this cache */
	private boolean found = false;
	/** If this is true, the cache has been filtered (is currently invisible) */
	private boolean filtered = false;
	/** True if the number of logs for this cache has changed */
	private boolean log_updated = false;
	/** True if cache details have changed: longDescription, Hints, */
	private boolean cache_updated = false;
	/**
	 * True if the cache data is incomplete (e.g. an error occurred during
	 * spidering
	 */
	private boolean incomplete = false;
	/** True if the cache is blacklisted */
	private boolean black = false;
	/** True if the cache is new */
	private boolean newCache = false;
	/** True if the cache is part of the results of a search */
	private boolean is_flaged = false;
	/**
	 * True if additional waypoints for this cache should be displayed
	 * regardless of the filter settings
	 */
	private boolean showAddis = false;
	/** True if the cache has been selected using the tick box in the list view */
	private boolean is_Checked = false;
	/** The unique OC cache ID */
	private String ocCacheID = EMPTY;
	/** The number of times this cache has not been found (max. 5) */
	private byte noFindLogs = 0;
	/** Number of recommendations (from the opencaching logs) */
	private int numRecommended = 0;
	/** Number of Founds since start of recommendations system */
	private int numFoundsSinceRecommendation = 0;
	/**
	 * Recommendation score: calculated as rations numRecommended /
	 * numLogsSinceRecommendation * 100
	 */
	private int recommendationScore = 0;
	/** True if this cache has travelbugs */
	private boolean bugs = false;
	/** True if the cache description is stored in HTML format */
	private boolean html = true;
	/** List of additional waypoints associated with this waypoint */
	private List<CacheHolder> addiWpts = new ArrayList<CacheHolder>();
	/**
	 * in range is used by the route filter to identify caches in range of a
	 * segment
	 */
	private boolean in_range = false;
	/** If this is an additional waypoint, this links back to the main waypoint */
	private CacheHolder mainCache;
	/** The date this cache was last synced with OC in format yyyyMMddHHmmss */
	private String lastSync = EMPTY;
	/** True if cache has solver entry */
	private boolean hasSolver = false;
	/** True if a note is entered for the cache */
	private boolean hasNote = false;
	private CacheHolderDetailSoft details = new CacheHolderDetailSoft(this);

	private long attributesYes = 0;
	private long attributesNo = 0;

	private IconAndText iconAndTextWP = null;
	private int iconAndTextWPLevel = 0;

	private static char decSep, notDecSep;
	static {
		decSep = MyLocale.getDigSeparator().charAt(0);
		notDecSep = decSep == '.' ? ',' : '.';
	}

	public CacheHolder() { // Just a public constructor
	}

	public CacheHolder(String wp) {
		this.wayPoint = wp;
	}

	public CacheHolder(String xmlString, int version) {
		int start, end;
		try {

			if (version == Profile.CURRENTFILEFORMAT) {
				start = xmlString.indexOf('"');
				end = xmlString.indexOf('"', start + 1);
				setCacheName(SafeXML.cleanback(xmlString.substring(start + 1,
						end)));

				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				setCacheOwner(SafeXML.cleanback(xmlString.substring(start + 1,
						end)));

				// Assume coordinates are in decimal format
				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				double lat = Convert.parseDouble(xmlString.substring(start + 1,
						end).replace(notDecSep, decSep));
				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				double lon = Convert.parseDouble(xmlString.substring(start + 1,
						end).replace(notDecSep, decSep));
				setPos(new CWPoint(lat, lon));

				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				setDateHidden(xmlString.substring(start + 1, end));
				// Convert the US format to YYYY-MM-DD if necessary
				if (getDateHidden().indexOf('/') > -1)
					setDateHidden(DateFormat.MDY2YMD(getDateHidden()));

				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				setWayPoint(SafeXML.cleanback(xmlString.substring(start + 1,
						end)));

				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				setCacheStatus(xmlString.substring(start + 1, end));

				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				setOcCacheID(xmlString.substring(start + 1, end));

				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				setLastSync(xmlString.substring(start + 1, end));

				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				setNumRecommended(Convert.toInt(xmlString.substring(start + 1,
						end)));

				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				setNumFoundsSinceRecommendation(Convert.toInt(xmlString
						.substring(start + 1, end)));
				setRecommendationScore(LogList.getScore(getNumRecommended(),
						getNumFoundsSinceRecommendation()));

				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				if (start > -1 && end > -1) {
					setAttributesYes(Convert.parseLong(xmlString.substring(
							start + 1, end)));

					start = xmlString.indexOf('"', end + 1);
					end = xmlString.indexOf('"', start + 1);
					if (start > -1 && end > -1)
						setAttributesNo(Convert.parseLong(xmlString.substring(
								start + 1, end)));
				}

				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);
				this.long2boolFields(Convert.parseLong(xmlString.substring(
						start + 1, end)));

				start = xmlString.indexOf('"', end + 1);
				end = xmlString.indexOf('"', start + 1);

				long2byteFields(Convert.parseLong(xmlString.substring(
						start + 1, end)));

			}
			if (version < Profile.CURRENTFILEFORMAT) {
				logger
						.warn(
								"Unsupported Version of CacheWolf Profile. Please use a CacheWolf to convert it to Version {}.",
								Profile.CURRENTFILEFORMAT);
				// make sure details get (re)written in new format
				getCacheDetails().setUnsavedChanges(true);
				// update information on notes and solver info
				setHasNote(!getCacheDetails().getCacheNotes().equals(""));
				setHasSolver(!getCacheDetails().getSolver().equals(""));
			}
		} catch (Throwable ex) {
			logger.error(
					"Ignored and unexpected exception in CacheHolder(String, int): "
							+ wayPoint, ex);
		}
	}

	/**
	 * Returns the distance in formatted output. Using kilometers when metric
	 * system is active, using miles when imperial system is active.
	 * 
	 * @return The current distance.
	 */
	public String getDistance() {
		String result = null;
		String newUnit = null;

		if (this.getKilom() >= 0) {
			double newValue = 0;
			switch (Global.getPref().metricSystem) {
			case Metrics.IMPERIAL:
				newValue = Metrics.convertUnit(this.getKilom(),
						Metrics.KILOMETER, Metrics.MILES);
				newUnit = Metrics.getUnit(Metrics.MILES);
				break;
			case Metrics.METRIC:
			default:
				newValue = this.getKilom();
				newUnit = Metrics.getUnit(Metrics.KILOMETER);
				break;
			}
			result = MyLocale.formatDouble(newValue, "0.00") + " " + newUnit;
		} else {
			result = "? "
					+ (Global.getPref().metricSystem == Metrics.IMPERIAL ? Metrics
							.getUnit(Metrics.MILES)
							: Metrics.getUnit(Metrics.KILOMETER));
		}
		return result;
	}

	public void update(CacheHolder ch) {
		update(ch, false);
	}

	/**
	 * Updates Cache information with information provided by cache given as
	 * argument. This is used to update the cache with the information retrieved
	 * from files or web: The argument cache is the one that is filled with the
	 * read information, <code>this</code> is the cache that is already in the
	 * database and subject to update.
	 * 
	 * @param ch
	 *            The cache who's information is updating the current one
	 * @param overwrite
	 *            If <code>true</code>, then <i>status</i>, <i>is_found</i> and
	 *            <i>position</i> is updated, otherwise not.
	 */
	private void update(CacheHolder ch, boolean overwrite) {
		this.setRecommendationScore(ch.getRecommendationScore());
		this.setNumFoundsSinceRecommendation(ch
				.getNumFoundsSinceRecommendation());
		this.setNumRecommended(ch.getNumRecommended());
		if (overwrite) {
			this.setCacheStatus(ch.getCacheStatus());
			this.setFound(ch.is_found());
			this.setPos(ch.getPos());
		} else {
			/*
			 * Here we have to distinguish several cases: this.is_found this ch
			 * Update 'this'
			 * ----------------------------------------------------
			 * ---------------- false empty yyyy-mm-dd yes true "Found"
			 * yyyy-mm-dd yes true yyyy-mm-dd yyyy-mm-dd no (or yes) true
			 * yyyy-mm-dd hh:mm yyyy-mm-dd no any any empty no
			 */
			if (!this.is_found() || this.getCacheStatus().indexOf(":") < 0) {
				// don't overwrite with empty data
				if (!ch.getCacheStatus().trim().equals("")) {
					this.setCacheStatus(ch.getCacheStatus());
				}
				this.setFound(ch.is_found());
			}
			// Don't overwrite valid coordinates with invalid ones
			if (ch.getPos().isValid() || !this.getPos().isValid()) {
				this.setPos(ch.getPos());
			}
		}
		this.setWayPoint(ch.getWayPoint());
		this.setCacheName(ch.getCacheName());
		this.setCacheOwner(ch.getCacheOwner());

		this.setDateHidden(ch.getDateHidden());
		this.setCacheSize(ch.getCacheSize());
		this.setKilom(ch.getKilom());
		this.setDegrees(ch.getDegrees());
		this.setDifficulty(ch.getDifficulty());
		this.setTerrain(ch.getTerrain());
		this.setType(ch.getType());
		this.setArchived(ch.is_archived());
		this.setAvailable(ch.is_available());
		this.setOwned(ch.is_owned());
		this.setFiltered(ch.is_filtered());
		this.setIncomplete(ch.is_incomplete());
		this.setAddiWpts(ch.getAddiWpts());
		this.setMainCache(ch.getMainCache());
		this.setOcCacheID(ch.getOcCacheID());
		this.setNoFindLogs(ch.getNoFindLogs());
		this.setHas_bugs(ch.has_bugs());
		this.setHTML(ch.is_HTML());
		this.setLastSync(ch.getLastSync());

		this.setAttributesYes(ch.getAttributesYes());
		this.setAttributesNo(ch.getAttributesNo());
		this.getCacheDetails().update(ch.getCacheDetails());
	}

	/**
	 * Call it only when necessary, it takes time, because all logs must be
	 * parsed
	 */
	private void calcRecommendationScore() {
		if (getWayPoint().toLowerCase().startsWith("oc")) {
			ICacheHolderDetail chD = getCacheDetails();
			setRecommendationScore(chD.getCacheLogs().getRecommendationRating());
			setNumFoundsSinceRecommendation(chD.getCacheLogs()
					.getFoundsSinceRecommendation());
			setNumRecommended(chD.getCacheLogs().getNumRecommended());
		} else {
			setRecommendationScore(-1);
			setNumFoundsSinceRecommendation(-1);
			// setNumRecommended(-1);
		}
	}

	/**
	 * Return a XML string containing all the cache data for storing in
	 * index.xml
	 */
	public String toXML() {
		calcRecommendationScore();

		StringBuilder sb = new StringBuilder(530); // Used in toXML()

		sb.append("    <CACHE ");
		sb.append(" name = \"");
		sb.append(SafeXML.clean(getCacheName()));
		sb.append("\" owner = \"");
		sb.append(SafeXML.clean(getCacheOwner()));
		sb.append("\" lat = \"");
		sb.append(getPos().latDec);
		sb.append("\" lon = \"");
		sb.append(getPos().lonDec);
		sb.append("\" hidden = \"");
		sb.append(getDateHidden());
		sb.append("\" wayp = \"");
		sb.append(SafeXML.clean(getWayPoint()));
		sb.append("\" status = \"");
		sb.append(getCacheStatus());
		sb.append("\" ocCacheID = \"");
		sb.append(getOcCacheID());
		sb.append("\" lastSyncOC = \"");
		sb.append(getLastSync());
		sb.append("\" num_recommended = \"");
		sb.append(Convert.formatInt(getNumRecommended()));
		sb.append("\" num_found = \"");
		sb.append(Convert.formatInt(getNumFoundsSinceRecommendation()));
		sb.append("\" attributesYes = \"");
		sb.append(Convert.formatLong(getAttributesYes()));
		sb.append("\" attributesNo = \"");
		sb.append(Convert.formatLong(getAttributesNo()));
		sb.append("\" boolFields=\"");
		sb.append(Convert.formatLong(this.boolFields2long()));
		sb.append("\" byteFields=\"");
		sb.append(Convert.formatLong(this.byteFields2long()));
		sb.append("\" />\n");
		return sb.toString();
	}

	public boolean isAddiWpt() {
		return getType().isAdditionalWaypoint();
	}

	public boolean isCustomWpt() {
		return getType() == CacheType.CUSTOM;
	}

	public boolean isCacheWpt() {
		return getType().isCacheWaypoint();
	}

	public boolean hasAddiWpt() {
		if (this.getAddiWpts().size() > 0)
			return true;
		else
			return false;
	}

	public void calcDistance(CWPoint toPoint) {
		if (getPos().isValid()) {
			setKilom(getPos().getDistance(toPoint));
			setDegrees(toPoint.getBearing(getPos()));
		} else {
			setKilom(-1);
		}
	}

	public void setAttributesFromMainCache() {
		CacheHolder mainCh = this.getMainCache();
		this.setCacheOwner(mainCh.getCacheOwner());
		this.setCacheStatus(mainCh.getCacheStatus());
		this.setArchived(mainCh.is_archived());
		this.setAvailable(mainCh.is_available());
		this.setBlack(mainCh.is_black());
		this.setOwned(mainCh.is_owned());
		this.setNew(mainCh.is_new());
		this.setFound(mainCh.is_found());
	}

	public void setAttributesToAddiWpts() {
		if (this.hasAddiWpt()) {
			CacheHolder addiWpt;
			for (int i = this.getAddiWpts().size() - 1; i >= 0; i--) {
				addiWpt = this.getAddiWpts().get(i);
				addiWpt.setAttributesFromMainCache();
			}
		}
	}

	/**
	 * True if ch and this belong to the same main cache.
	 * 
	 * @param ch
	 * @return
	 */
	public boolean hasSameMainCache(CacheHolder ch) {
		if (this == ch)
			return true;
		if (ch == null)
			return false;
		if ((!this.isAddiWpt()) && (!ch.isAddiWpt()))
			return false;
		CacheHolder main1, main2;
		if (this.isAddiWpt())
			main1 = this.getMainCache();
		else
			main1 = this;
		if (ch.isAddiWpt())
			main2 = ch.getMainCache();
		else
			main2 = ch;
		return main1 == main2;
	}

	/**
	 * Gets the detail object of a cache. The detail object stores information
	 * which is not needed for every cache instantaneously, but can be loaded if
	 * the user decides to look at this cache. If the cache object is already
	 * existing, the method will return this object, otherwise it will create it
	 * and try to read it from the corresponding <waypoint>.xml file. Depending
	 * on the parameters it is allowed that the <waypoint>.xml file does not yet
	 * exist, or the user is warned that the file doesn't exist. If more than
	 * <code>maxdetails</code> details are loaded, then the 5 last recently
	 * loaded caches are unloaded (to save ram).
	 * 
	 * @param maybenew
	 *            If true and the cache file could not be read, then an empty
	 *            detail object is returned.
	 * @return The respective CacheHolderDetail, or null
	 */

	public ICacheHolderDetail getCacheDetails() {
		return details;
	}

	/**
	 * Saves the cache to the corresponding <waypoint>.xml file, located in the
	 * profiles directory. The waypoint of the cache should be set to do so.
	 */
	public void save() {
		CacheHolderDetailFactory.getInstance().saveCacheDetails(
				this.getCacheDetails(), Global.getProfile().getDataDir());
	}

	public void releaseCacheDetails() {
		if (details.isDirty()) {
			save();
		}
	}

	/**
	 * when importing caches you can set details.saveChanges = true when the
	 * import is finished call this method to save the pending changes
	 */
	public static void saveAllModifiedDetails() {
		CacheDB db = Global.getProfile().cacheDB;

		for (CacheHolder ch : db) {
			ch.releaseCacheDetails();
		}
	}

	public String GetStatusDate() {
		String statusDate = "";

		if (is_found()) {
			Regex rexDate = new Regex("([0-9]{4}-[0-9]{2}-[0-9]{2})");
			rexDate.search(getCacheStatus());
			if (rexDate.stringMatched(1) != null) {
				statusDate = rexDate.stringMatched(1);
			}
		}

		return statusDate;
	}

	public String GetStatusTime() {
		String statusTime = "";

		if (is_found()) {
			Regex rexTime = new Regex("([0-9]{1,2}:[0-9]{2})");
			rexTime.search(getCacheStatus());
			if (rexTime.stringMatched(1) != null) {
				statusTime = rexTime.stringMatched(1);
			} else {
				Regex rexDate = new Regex("([0-9]{4}-[0-9]{2}-[0-9]{2})");
				rexDate.search(getCacheStatus());
				if (rexDate.stringMatched(1) != null) {
					statusTime = "00:00";
				}
			}
		}

		return statusTime;
	}

	public String getCacheID() {
		String result = "";

		if (getWayPoint().toUpperCase().startsWith("GC")) {
			int gcId = 0;

			String sequence = "0123456789ABCDEFGHJKMNPQRTVWXYZ";

			String rightPart = getWayPoint().substring(2).toUpperCase();

			int base = 31;
			if ((rightPart.length() < 4)
					|| (rightPart.length() == 4 && sequence.indexOf(rightPart
							.charAt(0)) < 16)) {
				base = 16;
			}

			for (int p = 0; p < rightPart.length(); p++) {
				gcId *= base;
				gcId += sequence.indexOf(rightPart.charAt(p));
			}

			if (base == 31) {
				gcId += java.lang.Math.pow(16, 4) - 16
						* java.lang.Math.pow(31, 3);
			}

			result = Integer.toString(gcId);
		} else if (getWayPoint().toUpperCase().startsWith("OC")) {
			result = getOcCacheID();
		}

		return result;
	}

	/**
	 * Initializes the caches states (and its addis) before updating, so that
	 * the "new", "updated", "log_updated" and "incomplete" properties are
	 * properly set.
	 * 
	 * @param pNewCache
	 *            <code>true</code> if it is a new cache (i.e. a cache not
	 *            existing in CacheDB), <code>false</code> otherwise.
	 */
	public void initStates(boolean pNewCache) {
		this.setNew(pNewCache);
		this.setUpdated(false);
		this.setLog_updated(false);
		this.setIncomplete(false);
		if (!pNewCache && this.hasAddiWpt()) {
			for (int i = 0; i < this.getAddiWpts().size(); i++) {
				this.getAddiWpts().get(i).initStates(pNewCache);
			}
		}
	}

	/**
	 * Creates a bit field of boolean values of the cache, represented as a long
	 * value. Boolean value of <code>true</code> results in <code>1</code> in
	 * the long values bits, and, vice versa, 0 for false.
	 * 
	 * @return long value representing the boolean bit field
	 */
	private long boolFields2long() {
		// To get the same list of visible caches after loading a profile,
		// the property isVisible() is saved instead of is_filtered(), but at
		// the place where is_filtered() is read.
		long value = bool2BitMask(!this.isVisible(), 1)
				| bool2BitMask(this.is_available(), 2)
				| bool2BitMask(this.is_archived(), 3)
				| bool2BitMask(this.has_bugs(), 4)
				| bool2BitMask(this.is_black(), 5)
				| bool2BitMask(this.is_owned(), 6)
				| bool2BitMask(this.is_found(), 7)
				| bool2BitMask(this.is_new(), 8)
				| bool2BitMask(this.is_log_updated(), 9)
				| bool2BitMask(this.is_updated(), 10)
				| bool2BitMask(this.is_HTML(), 11)
				| bool2BitMask(this.is_incomplete(), 12)
				| bool2BitMask(this.hasNote(), 13)
				| bool2BitMask(this.hasSolver(), 14);
		return value;
	}

	/**
	 * Creates a field of byte values of certain properties of the cache,
	 * represented as a long value. As a long is 8 bytes wide, one might pack 8
	 * bytes into a long, one every 8 bits. The position indicates the group of
	 * bits where the byte is packed, counting starting from one by the right
	 * side of the long.
	 * 
	 * @return long value representing the byte field
	 */
	private long byteFields2long() {
		long value = byteBitMask(difficulty.getOldCWValue(), 1)
				| byteBitMask(terrain.getOldCWValue(), 2)
				| byteBitMask(type.getOldCWByte(), 3)
				| byteBitMask(cacheSize.getOldCwId(), 4)
				| byteBitMask(this.noFindLogs, 5);
		return value;
	}

	/**
	 * Evaluates byte values from a long value for certain properties of the
	 * cache.
	 * 
	 * @param value
	 *            The long value which contains up to 8 bytes.
	 */
	private void long2byteFields(long value) {
		setDifficulty(Difficulty.fromOldCWByte(byteFromLong(value, 1)));
		setTerrain(Terrain.fromOldCWByte(byteFromLong(value, 2)));
		setType(CacheType.fromOldCWByte(byteFromLong(value, 3)));
		setCacheSize(CacheSize.fromOldCwId(byteFromLong(value, 4)));
		setNoFindLogs((byteFromLong(value, 5)));
		if ((getDifficulty() == Difficulty.DIFFICULTY_ERROR)
				|| (getTerrain() == Terrain.TERRAIN_ERROR)
				// || getCacheSize() == cacheSize.CW_SIZE_ERROR kann eigentlich
				// nie erreicht werden
				|| getType() == CacheType.ERROR) {
			setIncomplete(true);
		}
	}

	/**
	 * Extracts a byte from a long value. The position is the number of the
	 * 8-bit block of the long (which contains 8 8-bit blocks), counted from 1
	 * to 8, starting from the right side of the long.
	 * 
	 * @param value
	 *            The long value which contains the bytes
	 * @param position
	 *            The position of the byte, from 1 to 8
	 * @return The decoded byte value
	 */
	private byte byteFromLong(long value, int position) {
		byte b = -1; // = 11111111
		return (byte) ((value & this.byteBitMask(b, position)) >>> (position - 1) * 8);
	}

	/**
	 * Evaluates boolean values from a long value, which is seen as bit field.
	 * 
	 * @param value
	 *            The bit field as long value
	 */
	private void long2boolFields(long value) {
		this.setFiltered((value & this.bool2BitMask(true, 1)) != 0);
		this.setAvailable((value & this.bool2BitMask(true, 2)) != 0);
		this.setArchived((value & this.bool2BitMask(true, 3)) != 0);
		this.setHas_bugs((value & this.bool2BitMask(true, 4)) != 0);
		this.setBlack((value & this.bool2BitMask(true, 5)) != 0);
		this.setOwned((value & this.bool2BitMask(true, 6)) != 0);
		this.setFound((value & this.bool2BitMask(true, 7)) != 0);
		this.setNew((value & this.bool2BitMask(true, 8)) != 0);
		this.setLog_updated((value & this.bool2BitMask(true, 9)) != 0);
		this.setUpdated((value & this.bool2BitMask(true, 10)) != 0);
		this.setHTML((value & this.bool2BitMask(true, 11)) != 0);
		this.setIncomplete(((value & this.bool2BitMask(true, 12)) != 0)
				|| this.is_incomplete());
		this.setHasNote((value & this.bool2BitMask(true, 13)) != 0);
		this.setHasSolver((value & this.bool2BitMask(true, 14)) != 0);
	}

	/**
	 * Represents a bit mask as long value for a boolean value which is saved at
	 * a specified position in the long field.
	 * 
	 * @param value
	 *            The boolean value we want to code
	 * @param position
	 *            Position of the value in the bit mask
	 * @return The corresponding bit mask: A long value where all bits are set
	 *         to 0 except for the one we like to represent: This is 1 if the
	 *         value is true, 0 if not.
	 */
	private long bool2BitMask(boolean value, int position) {
		if (value) {
			return (1L << (position - 1));
		} else {
			return 0L;
		}
	}

	/**
	 * Coding a long field which has only the bits of the byte value set. The
	 * position is the number (from 1 to 8) of the byte block which is used from
	 * the long.
	 * 
	 * @param value
	 *            Byte to encode
	 * @param position
	 *            Position of the byte value in the long
	 * @return Encoded byte value as long
	 */
	private long byteBitMask(byte value, int position) {
		long result = (0xFF & (long) value) << ((position - 1) * 8);
		return result;
	}

	/**
	 * Returns <code>true</code> if the waypoint should appear in the cache
	 * list, <code>false</code> if it should not appear.<br>
	 * The method takes into account blacklist, filters, search results -
	 * everything that determines if a cache is visible in the list or not.
	 * 
	 * @return
	 */
	public boolean isVisible() {
		Profile profile = Global.getProfile();
		int filter = profile.getFilterActive();
		boolean noShow = ((profile.showBlacklisted() != this.is_black())
				|| (profile.showSearchResult() && !this.isIs_flaged())
				|| ((filter == Filter.FILTER_ACTIVE || filter == Filter.FILTER_MARKED_ONLY) && (this
						.is_filtered())
						^ profile.isFilterInverted()) || (filter == Filter.FILTER_CACHELIST)
				&& !Global.mainForm.cacheList.contains(this.getWayPoint()));
		boolean showAddi = this.showAddis() && this.getMainCache() != null
				&& this.getMainCache().isVisible();
		noShow = noShow && !showAddi;
		return !noShow;
	}

	// Getter and Setter for private properties

	/**
	 * Gets an IconAndText object for the cache. If the level of the Icon is
	 * equal to the last call of the method, the same (cached) object is
	 * returned. If the object is null or the level is different, a new object
	 * is created.<br>
	 * 
	 * @param level
	 *            4=is_incomplete(), 3=is_new(), 2=is_updated(),
	 *            1=is_log_updated
	 * @param fm
	 *            Font metrics
	 * @return New or old IconAndText object
	 */
	public IconAndText getIconAndTextWP(int level, FontMetrics fm) {
		if (level != iconAndTextWPLevel || iconAndTextWP == null) {
			switch (level) {
			case 4:
				iconAndTextWP = new IconAndText(GuiImageBroker.getInstance()
						.getErrorImage(), this.getWayPoint(), fm);
				break;
			case 3:
				iconAndTextWP = new IconAndText(myTableModel.yellow, this
						.getWayPoint(), fm);
				break;
			case 2:
				iconAndTextWP = new IconAndText(myTableModel.red, this
						.getWayPoint(), fm);
				break;
			case 1:
				iconAndTextWP = new IconAndText(myTableModel.blue, this
						.getWayPoint(), fm);
				break;
			}
			iconAndTextWPLevel = level;
		}
		return iconAndTextWP;
	}

	public String getCacheStatus() {
		return cacheStatus;
	}

	public void setCacheStatus(String cacheStatus) {
		Global.getProfile().notifyUnsavedChanges(
				!cacheStatus.equals(this.cacheStatus));
		this.cacheStatus = cacheStatus;
	}

	public String getWayPoint() {
		return wayPoint;
	}

	public void setWayPoint(String wayPoint) {
		Global.getProfile().notifyUnsavedChanges(
				!wayPoint.equals(this.wayPoint));
		this.wayPoint = wayPoint;
	}

	public String getCacheName() {
		return cacheName;
	}

	/**
	 * @return the name of the cache in simplified form. That is: without
	 *         punctuation, without leading spaces, all-lowercase.
	 */
	public String getCacheNameSimplified() {
		return cacheName.toLowerCase().replaceAll("[\\p{Punct}]", "")
				.replaceAll("^\\s*", "");
	}

	public void setCacheName(String cacheName) {
		Global.getProfile().notifyUnsavedChanges(
				!cacheName.equals(this.cacheName));
		this.cacheName = cacheName;
	}

	public String getCacheOwner() {
		return cacheOwner;
	}

	/**
	 * @return the name of the owner in simplified form. That is: without
	 *         punctuation, without leading spaces, all-lowercase.
	 */
	public String getCacheOwnerSimplified() {
		return cacheOwner.toLowerCase().replaceAll("[\\p{Punct}]", "")
				.replaceAll("^\\s*", "");
	}

	public void setCacheOwner(String cacheOwner) {
		Global.getProfile().notifyUnsavedChanges(
				!cacheOwner.equals(this.cacheOwner));
		this.cacheOwner = cacheOwner;
	}

	public String getDateHidden() {
		return dateHidden;
	}

	public void setDateHidden(String dateHidden) {
		Global.getProfile().notifyUnsavedChanges(
				!dateHidden.equals(this.dateHidden));
		this.dateHidden = dateHidden;
	}

	public CacheSize getCacheSize() {
		return cacheSize;
	}

	public void setCacheSize(CacheSize cacheSize) {
		Global.getProfile().notifyUnsavedChanges(cacheSize != this.cacheSize);
		this.cacheSize = cacheSize;
	}

	public Difficulty getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(Difficulty hard) {
		Global.getProfile().notifyUnsavedChanges(hard != this.difficulty);
		this.difficulty = hard;
	}

	public Terrain getTerrain() {
		return terrain;
	}

	public void setTerrain(Terrain terrain) {
		Global.getProfile().notifyUnsavedChanges(terrain != this.terrain);
		this.terrain = terrain;
	}

	/**
	 * Gets the type of cache as integer.
	 * 
	 * @return Cache type
	 */
	public CacheType getType() {
		return type;
	}

	/**
	 * Sets the type of the cache. As the cache type values are int for the rest
	 * of CacheWolf and byte internally of CacheHolder, some conversion has to
	 * be done.
	 * 
	 * @param type
	 *            Cache Type
	 */
	public void setType(CacheType type) {
		Global.getProfile().notifyUnsavedChanges(this.type != type);
		this.type = type;
	}

	public boolean is_archived() {
		return archived;
	}

	public void setArchived(boolean is_archived) {
		Global.getProfile().notifyUnsavedChanges(is_archived != this.archived);
		this.archived = is_archived;
	}

	public boolean is_available() {
		return available;
	}

	public void setAvailable(boolean is_available) {
		Global.getProfile()
				.notifyUnsavedChanges(is_available != this.available);
		this.available = is_available;
	}

	public boolean is_owned() {
		return owned;
	}

	public void setOwned(boolean is_owned) {
		Global.getProfile().notifyUnsavedChanges(is_owned != this.owned);
		this.owned = is_owned;
	}

	public boolean is_found() {
		return found;
	}

	public void setFound(boolean is_found) {
		Global.getProfile().notifyUnsavedChanges(is_found != this.found);
		this.found = is_found;
	}

	/**
	 * If this returns <code>true</code>, then the additional waypoints for this
	 * cache should be displayed regardless how the filter is set. If it is
	 * <code>false</code>, then the normal filter settings apply.<br>
	 * This property is not saved in index.xml, so if you reload the data, then
	 * this information is gone.
	 * 
	 * @return <code>True</code>: Always display additional waypoints for cache.
	 */
	public boolean showAddis() {
		return this.showAddis;
	}

	/**
	 * Setter for <code>showAddis()</code>. If this returns <code>true</code>,
	 * then the additional waypoints for this cache should be displayed
	 * regardless how the filter is set. If it is <code>false</code>, then the
	 * normal filter settings apply.<br>
	 * This property is not saved in index.xml, so if you reload the data, then
	 * this information is gone.
	 * 
	 * @param value
	 *            <code>True</code>: Always display additional waypoints for
	 *            cache.
	 */
	public void setShowAddis(boolean value) {
		// This value is always stored in the main cache and all addis.
		CacheHolder mc = null;
		if (this.getMainCache() == null) {
			mc = this;
		} else {
			mc = this.getMainCache();
		}
		if (mc.showAddis != value) {
			mc.showAddis = value;
			for (int i = 0; i < mc.getAddiWpts().size(); i++) {
				CacheHolder ac = mc.getAddiWpts().get(i);
				ac.showAddis = value;
			}
		}
	}

	/**
	 * <b><u>Important</u></b>: This flag no longer indicates if a cache is
	 * visible in the list. Instead, it now <u>only</u> flags if the cache is
	 * filtered out by filter criteria. Use <code>isVisible()</code> instead.<br>
	 * This property is affected by the following features:
	 * <ul>
	 * <li>"Defining and applying" a filter</li>
	 * <li>Filtering out checked or unchecked caches</li>
	 * </ul>
	 * It is <u>not</u> affected by:
	 * <ul>
	 * <li>Inverting a filter</li>
	 * <li>Removing a filter</li>
	 * <li>Applying a filter</li>
	 * <li>Applying a cache tour filter</li>
	 * <li>Switching between normal view and blacklist view</li>
	 * <li>Performing searches</li>
	 * <li>Anything else that isn't directly connected to filters in it's proper
	 * sense.</li>
	 * </ul>
	 * The new method for deciding if a cache is visible or not is
	 * <code>isVisible()
	 * </code>.
	 * 
	 * @return <code>True</code> if filter criteria are matched
	 */
	public boolean is_filtered() {
		return filtered;
	}

	public void setFiltered(boolean is_filtered) {
		Global.getProfile().notifyUnsavedChanges(is_filtered != this.filtered);
		this.filtered = is_filtered;
	}

	public boolean is_log_updated() {
		return log_updated;
	}

	public void setLog_updated(boolean is_log_updated) {
		Global.getProfile().notifyUnsavedChanges(
				is_log_updated != this.log_updated);
		if (is_log_updated && iconAndTextWPLevel == 1)
			iconAndTextWP = null;
		this.log_updated = is_log_updated;
	}

	public boolean is_updated() {
		return cache_updated;
	}

	public void setUpdated(boolean is_updated) {
		Global.getProfile().notifyUnsavedChanges(
				is_updated != this.cache_updated);
		if (is_updated && iconAndTextWPLevel == 2)
			iconAndTextWP = null;
		this.cache_updated = is_updated;
	}

	public boolean is_incomplete() {
		return incomplete;
	}

	public void setIncomplete(boolean is_incomplete) {
		Global.getProfile().notifyUnsavedChanges(
				is_incomplete != this.incomplete);
		if (is_incomplete && iconAndTextWPLevel == 4)
			iconAndTextWP = null;
		this.incomplete = is_incomplete;
	}

	public boolean checkIncomplete() {
		boolean ret;
		if (isCacheWpt()) {
			if (getWayPoint().length() < 3
					|| getDifficulty() == Difficulty.DIFFICULTY_ERROR
					|| getDifficulty() == Difficulty.DIFFICULTY_UNSET
					|| getTerrain() == Terrain.TERRAIN_ERROR
					|| getTerrain() == Terrain.TERRAIN_UNSET
					// || getCacheSize() == CacheSize.CW_SIZE_ERROR kann
					// eigentlich nie erreicht werden
					|| getCacheOwner().length() == 0
					|| getDateHidden().length() == 0
					|| getCacheName().length() == 0)
				ret = true;
			else
				ret = false;
		} else if (isAddiWpt()) {
			if (getMainCache() == null
					|| getDifficulty() != Difficulty.DIFFICULTY_UNSET
					|| getCacheSize() != CacheSize.NOT_CHOSEN
					|| getTerrain() != Terrain.TERRAIN_UNSET
					|| getWayPoint().length() < 2
					// || getCacheOwner().length() > 0
					// || getDateHidden().length() > 0
					|| getCacheName().length() == 0)
				ret = true;
			else
				ret = false;
		} else if (isCustomWpt()) {
			if (getDifficulty() != Difficulty.DIFFICULTY_UNSET
					|| getTerrain() != Terrain.TERRAIN_UNSET
					|| getCacheSize() != CacheSize.NOT_CHOSEN
					|| getWayPoint().length() < 2
					// || getCacheOwner().length() > 0
					// || getDateHidden().length() > 0
					|| getCacheName().length() == 0)
				ret = true;
			else
				ret = false;
		} else {
			// we should not get here, so let's set a warning just in case
			ret = true;
		}
		setIncomplete(ret);
		return ret;
	}

	/**
	 * Determines if the blacklist status is set for the cache. Do not use this
	 * method to check if the cache should be displayed. Use
	 * <code>isVisible()</code> for this, which already does this (and other)
	 * checks.<br>
	 * Only use this method if you really want to inform yourself about the
	 * black status of the cache!
	 * 
	 * @return <code>true</code> if he black status of the cache is set.
	 */
	public boolean is_black() {
		return black;
	}

	public void setBlack(boolean is_black) {
		Global.getProfile().notifyUnsavedChanges(is_black != this.black);
		this.black = is_black;
	}

	public boolean is_new() {
		return newCache;
	}

	public void setNew(boolean is_new) {
		Global.getProfile().notifyUnsavedChanges(is_new != this.newCache);
		if (is_new && iconAndTextWPLevel == 3)
			iconAndTextWP = null;
		this.newCache = is_new;
	}

	public String getOcCacheID() {
		return ocCacheID;
	}

	public void setOcCacheID(String ocCacheID) {
		Global.getProfile().notifyUnsavedChanges(
				!ocCacheID.equals(this.ocCacheID));
		this.ocCacheID = ocCacheID;
	}

	public byte getNoFindLogs() {
		return noFindLogs;
	}

	public void setNoFindLogs(byte noFindLogs) {
		Global.getProfile().notifyUnsavedChanges(noFindLogs != this.noFindLogs);
		this.noFindLogs = noFindLogs;
	}

	public int getNumRecommended() {
		return numRecommended;
	}

	public void setNumRecommended(int numRecommended) {
		Global.getProfile().notifyUnsavedChanges(
				numRecommended != this.numRecommended);
		this.numRecommended = numRecommended;
	}

	public int getNumFoundsSinceRecommendation() {
		return numFoundsSinceRecommendation;
	}

	public void setNumFoundsSinceRecommendation(int numFoundsSinceRecommendation) {
		Global
				.getProfile()
				.notifyUnsavedChanges(
						numFoundsSinceRecommendation != this.numFoundsSinceRecommendation);
		this.numFoundsSinceRecommendation = numFoundsSinceRecommendation;
	}

	public boolean has_bugs() {
		return bugs;
	}

	public void setHas_bugs(boolean has_bug) {
		Global.getProfile().notifyUnsavedChanges(has_bug != this.bugs);
		this.bugs = has_bug;
	}

	public boolean is_HTML() {
		return html;
	}

	public void setHTML(boolean is_HTML) {
		Global.getProfile().notifyUnsavedChanges(is_HTML != this.html);
		this.html = is_HTML;
	}

	public String getLastSync() {
		return lastSync;
	}

	public void setLastSync(String lastSync) {
		Global.getProfile().notifyUnsavedChanges(
				!lastSync.equals(this.lastSync));
		this.lastSync = lastSync;
	}

	public long getAttributesYes() {
		return attributesYes;
	}

	public void setAttributesYes(long attributesYes) {
		Global.getProfile().notifyUnsavedChanges(
				attributesYes != this.attributesYes);
		this.attributesYes = attributesYes;
	}

	public long getAttributesNo() {
		return attributesNo;
	}

	public void setAttributesNo(long attributesNo) {
		Global.getProfile().notifyUnsavedChanges(
				attributesNo != this.attributesNo);
		this.attributesNo = attributesNo;
	}

	public boolean hasSolver() {
		return hasSolver;
	}

	public void setHasSolver(boolean hasSolver) {
		Global.getProfile().notifyUnsavedChanges(hasSolver != this.hasSolver);
		this.hasSolver = hasSolver;
	}

	public boolean hasNote() {
		return hasNote;
	}

	public void setHasNote(boolean hasNote) {
		Global.getProfile().notifyUnsavedChanges(hasNote != this.hasNote);
		this.hasNote = hasNote;
	}

	/**
	 * @return null if !pos.isValid(), the Bearing from the current centre to
	 *         this cache otherwise.
	 */
	public Bearing getBearing() {
		return Bearing.fromDeg(getDegrees());
	}

	/**
	 * @return NOBEARING if !pos.isValid(), the Bearing from the current centre
	 *         to this cache otherwise.
	 */
	public String getBearingAsString() {
		if (getBearing() == null) {
			return NOBEARING;
		} else {
			return this.getBearing().toString();
		}
	}

	public void setPos(CWPoint pos) {
		this.pos = pos;
	}

	public CWPoint getPos() {
		return pos;
	}

	public String getLatLon() {
		return pos.toString();
	}

	public void setKilom(double kilom) {
		this.kilom = kilom;
	}

	public double getKilom() {
		return kilom;
	}

	public void setDegrees(double degrees) {
		this.degrees = degrees;
	}

	public double getDegrees() {
		return degrees;
	}

	public void setIs_flaged(boolean is_flaged) {
		this.is_flaged = is_flaged;
	}

	public boolean isIs_flaged() {
		return is_flaged;
	}

	public void setIs_Checked(boolean is_Checked) {
		this.is_Checked = is_Checked;
	}

	public boolean isIs_Checked() {
		return is_Checked;
	}

	public void setRecommendationScore(int recommendationScore) {
		this.recommendationScore = recommendationScore;
	}

	public int getRecommendationScore() {
		return recommendationScore;
	}

	public void setAddiWpts(List<CacheHolder> addiWpts) {
		this.addiWpts = addiWpts;
	}

	public List<CacheHolder> getAddiWpts() {
		return addiWpts;
	}

	public void setIn_range(boolean in_range) {
		this.in_range = in_range;
	}

	public boolean isIn_range() {
		return in_range;
	}

	public void setMainCache(CacheHolder mainCache) {
		this.mainCache = mainCache;
	}

	public CacheHolder getMainCache() {
		return mainCache;
	}
}
