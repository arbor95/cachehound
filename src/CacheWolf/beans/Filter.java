package CacheWolf.beans;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import CacheWolf.Global;
import CacheWolf.imp.KMLImporter;
import CacheWolf.util.Common;

import com.stevesoft.ewe_pat.Regex;

import de.cachehound.factory.CWPointFactory;
import de.cachehound.types.Bearing;
import de.cachehound.types.CacheSize;
import de.cachehound.types.CacheType;
import ewe.io.File;
import ewe.io.FileNotFoundException;
import ewe.io.FileReader;
import ewe.io.IOException;
import ewe.sys.Convert;
import ewe.ui.FormBase;
import ewe.ui.MessageBox;
import ewe.util.Hashtable;

/**
 * Class that actually filters the cache database.<br>
 * The class that uses this filter must set the different public variables.
 * 
 * @author BilboWolf (optimiert von salzkammergut)
 */
public class Filter {
	public static final int FILTER_INACTIVE = 0;
	public static final int FILTER_ACTIVE = 1;
	public static final int FILTER_CACHELIST = 2;
	public static final int FILTER_MARKED_ONLY = 3;

	/** Indicator whether a filter is inverted */
	// public static boolean filterInverted=false;
	/**
	 * Indicator whether a filter is active. Used in status bar to indicate
	 * filter status
	 */
	// public static int filterActive=FILTER_INACTIVE;
	private static final int SMALLER = -1;
	private static final int EQUAL = 0;
	private static final int GREATER = 1;

	private static final int TRADITIONAL = 1;
	private static final int MULTI = 2;
	private static final int VIRTUAL = 4;
	private static final int LETTER = 8;
	private static final int EVENT = 16;
	private static final int WEBCAM = 32;
	private static final int MYSTERY = 64;
	private static final int LOCLESS = 128;
	private static final int CUSTOM = 256;
	private static final int MEGA = 512;
	private static final int EARTH = 1024;
	private static final int PARKING = 2048;
	private static final int STAGE = 4096;
	private static final int QUESTION = 8192;
	private static final int FINAL = 16384;
	private static final int TRAILHEAD = 32768;
	private static final int REFERENCE = 65536;
	private static final int CITO = 131072;
	private static final int WHERIGO = 262144;
	private static final int TYPE_ALL = TRADITIONAL | MULTI | VIRTUAL | LETTER
			| EVENT | WEBCAM | MYSTERY | LOCLESS | CUSTOM | MEGA | EARTH
			| PARKING | STAGE | QUESTION | FINAL | TRAILHEAD | REFERENCE | CITO
			| WHERIGO;
	private static final int TYPE_MAIN = TRADITIONAL | MULTI | VIRTUAL | LETTER
			| EVENT | WEBCAM | MYSTERY | LOCLESS | CUSTOM | MEGA | EARTH | CITO
			| WHERIGO;

	private int distdirec = 0;
	private int diffdirec = 0;
	private int terrdirec = 0;

	String[] byVec;

	private Set<Bearing> roseMatchPattern;
	private boolean hasRoseMatchPattern;
	private int typeMatchPattern;
	private boolean hasTypeMatchPattern;
	private int sizeMatchPattern;
	private boolean hasSizeMatchPattern;

	private boolean foundByMe;
	private boolean notFoundByMe;

	private String cacheStatus;
	private boolean useRegexp;

	private boolean ownedByMe;
	private boolean notOwnedByMe;

	double fscDist;
	double fscTerr;
	double fscDiff;

	private boolean archived = false;
	private boolean notArchived = false;

	private boolean available = false;
	private boolean notAvailable = false;
	double pi180 = java.lang.Math.PI / 180.0;

	private long attributesYesPattern = 0;
	private long attributesNoPattern = 0;
	private int attributesChoice = 0;

	/**
	 * Apply a route filter. Each waypoint is on a seperate line. We use a regex
	 * method to allow for different formats of waypoints: possible is
	 * currently: DD MM.mmm
	 */
	public void doFilterRoute(File routeFile, double distance) {
		Global.getProfile().selectionChanged = true;
		CacheDB cacheDB = Global.getProfile().cacheDB;
		// load file into a vector:
		List<CWPoint> wayPoints;
		Regex rex = new Regex(
				"(N|S).*?([0-9]{1,2}).*?([0-9]{1,3})(,|.)([0-9]{1,3}).*?(E|W).*?([0-9]{1,2}).*?([0-9]{1,3})(,|.)([0-9]{1,3})");
		CWPoint cwp, fromPoint, toPoint;
		CacheHolder ch;
		double lat, lon, calcDistance = 0;
		try {
			if ((routeFile.getFullPath()).indexOf(".kml") > 0) {
				KMLImporter kml = new KMLImporter(new java.io.File(routeFile
						.getFullPath()));
				kml.importFile();
				wayPoints = kml.getPoints();
			} else {
				wayPoints = new ArrayList<CWPoint>();

				FileReader in = new FileReader(routeFile);
				String line;
				while ((line = in.readLine()) != null) {
					rex.search(line);
					// parse the route file
					if (rex.didMatch()) {
						lat = Convert.toDouble(rex.stringMatched(2))
								+ Convert.toDouble(rex.stringMatched(3)) / 60
								+ Convert.toDouble(rex.stringMatched(5))
								/ 60000;
						lon = Convert.toDouble(rex.stringMatched(7))
								+ Convert.toDouble(rex.stringMatched(8)) / 60
								+ Convert.toDouble(rex.stringMatched(10))
								/ 60000;

						if (rex.stringMatched(1).equals("S")
								|| rex.stringMatched(1).equals("s"))
							lat = -lat;
						if (rex.stringMatched(6).equals("W")
								|| rex.stringMatched(6).equals("w"))
							lon = -lon;

						cwp = CWPointFactory.getInstance().fromD(lat, lon);

						wayPoints.add(cwp);
					}
				}
			}
			// initialise database
			for (int i = cacheDB.size() - 1; i >= 0; i--) {
				ch = cacheDB.get(i);
				ch.setIn_range(false);
				// cacheDB.set(i, ch);
			}
			// for each segment of the route...
			for (int z = 0; z < wayPoints.size() - 1; z++) {
				fromPoint = wayPoints.get(z);
				toPoint = wayPoints.get(z + 1);
				// ... go through the current cache database
				for (int i = cacheDB.size() - 1; i >= 0; i--) {
					ch = cacheDB.get(i);
					cwp = new CWPoint(ch.getPos());
					calcDistance = DistToSegment(fromPoint, toPoint, cwp);
					calcDistance = (calcDistance * 180 * 60)
							/ java.lang.Math.PI;
					calcDistance = calcDistance * 1.852;
					// Vm.debug("Distcalc: " + calcDistance + "Cache: "
					// +ch.CacheName + " / z is = " + z);
					if (calcDistance <= distance) {
						// Vm.debug("Distcalc: " + calcDistance + "Cache: "
						// +ch.CacheName + " / z is = " + z);
						ch.setIn_range(true);
					}
					// cacheDB.set(i, ch);
				} // for database
			} // for segments
			for (int i = cacheDB.size() - 1; i >= 0; i--) {
				ch = cacheDB.get(i);
				if (ch.isFiltered() == false && ch.isInRange() == false)
					ch.setFiltered(true);
			}
		} catch (FileNotFoundException fnex) {
			(new MessageBox("Error", "File not found", FormBase.OKB)).execute();
		} catch (IOException ioex) {
			(new MessageBox("Error", "Problem reading file!", FormBase.OKB))
					.execute();
		}
	}

	/**
	 * Method to calculate the distance of a point to a segment
	 */
	private double DistToSegment(CWPoint fromPoint, CWPoint toPoint, CWPoint cwp) {

		/*
		 * double XTD = 0; double dist = 0;
		 * 
		 * double crs_AB = fromPoint.getBearing(toPoint); crs_AB = crs_AB *
		 * java.lang.Math.PI / 180; double crs_AD = fromPoint.getBearing(cwp);
		 * crs_AD = crs_AD * java.lang.Math.PI / 180; double dist_AD =
		 * fromPoint.getDistance(cwp); Vm.debug("Distance: "+dist_AD); dist_AD =
		 * dist_AD / 1.852; dist_AD = (java.lang.Math.PI/(180*60))*dist_AD; XTD
		 * =
		 * java.lang.Math.asin(java.lang.Math.sin(dist_AD)*java.lang.Math.sin(crs_AD
		 * -crs_AB)); return java.lang.Math.abs(XTD);
		 */
		double dist = 0;
		double px = cwp.getLonDec() * pi180;
		double py = cwp.getLatDec() * pi180;
		double X1 = fromPoint.getLonDec() * pi180;
		double Y1 = fromPoint.getLatDec() * pi180;
		double X2 = toPoint.getLonDec() * pi180;
		double Y2 = toPoint.getLatDec() * pi180;
		double dx = X2 - X1;
		double dy = Y2 - Y1;
		if (dx == 0 && dy == 0) {
			// have a point and not a segment!
			dx = px - X1;
			dy = py - Y1;
			return java.lang.Math.sqrt(dx * dx + dy * dy);
		}
		dist = Matrix.cross(X1, Y1, X2, Y2, px, py)
				/ Matrix.dist(X1, Y1, X2, Y2);
		double dot1 = Matrix.dot(X1, Y1, X2, Y2, px, py);
		if (dot1 > 0)
			return Matrix.dist(X2, Y2, px, py);
		double dot2 = Matrix.dot(X2, Y2, X1, Y1, px, py);
		if (dot2 > 0)
			return Matrix.dist(X1, Y1, px, py);
		dist = java.lang.Math.abs(dist);
		return dist;

	}

	/**
	 * Set the filter from the filter data stored in the profile (the
	 * filterscreen also updates the profile)
	 */
	public void setFilter() {
		Profile profile = Global.getProfile();
		archived = profile.getFilterVar().charAt(0) == '1';
		available = profile.getFilterVar().charAt(1) == '1';
		foundByMe = profile.getFilterVar().charAt(2) == '1';
		ownedByMe = profile.getFilterVar().charAt(3) == '1';
		notArchived = profile.getFilterVar().charAt(4) == '1';
		notAvailable = profile.getFilterVar().charAt(5) == '1';
		notFoundByMe = profile.getFilterVar().charAt(6) == '1';
		notOwnedByMe = profile.getFilterVar().charAt(7) == '1';
		typeMatchPattern = 0;
		cacheStatus = profile.getFilterStatus();
		useRegexp = profile.getFilterUseRegexp();

		String filterType = profile.getFilterType();
		if (filterType.charAt(0) == '1')
			typeMatchPattern |= TRADITIONAL;
		if (filterType.charAt(1) == '1')
			typeMatchPattern |= MULTI;
		if (filterType.charAt(2) == '1')
			typeMatchPattern |= VIRTUAL;
		if (filterType.charAt(3) == '1')
			typeMatchPattern |= LETTER;
		if (filterType.charAt(4) == '1')
			typeMatchPattern |= EVENT;
		if (filterType.charAt(5) == '1')
			typeMatchPattern |= WEBCAM;
		if (filterType.charAt(6) == '1')
			typeMatchPattern |= MYSTERY;
		if (filterType.charAt(7) == '1')
			typeMatchPattern |= EARTH;
		if (filterType.charAt(8) == '1')
			typeMatchPattern |= LOCLESS;
		if (filterType.charAt(9) == '1')
			typeMatchPattern |= MEGA;
		if (filterType.charAt(10) == '1')
			typeMatchPattern |= CUSTOM;
		if (filterType.charAt(11) == '1')
			typeMatchPattern |= PARKING;
		if (filterType.charAt(12) == '1')
			typeMatchPattern |= STAGE;
		if (filterType.charAt(13) == '1')
			typeMatchPattern |= QUESTION;
		if (filterType.charAt(14) == '1')
			typeMatchPattern |= FINAL;
		if (filterType.charAt(15) == '1')
			typeMatchPattern |= TRAILHEAD;
		if (filterType.charAt(16) == '1')
			typeMatchPattern |= REFERENCE;
		if (filterType.charAt(17) == '1')
			typeMatchPattern |= CITO;
		if (filterType.charAt(18) == '1')
			typeMatchPattern |= WHERIGO;
		hasTypeMatchPattern = typeMatchPattern != TYPE_ALL;

		roseMatchPattern = profile.getFilterRose();
		hasRoseMatchPattern = roseMatchPattern != EnumSet.allOf(Bearing.class);

		sizeMatchPattern = 0;
		String filterSize = profile.getFilterSize();
		if (filterSize.charAt(0) == '1')
			sizeMatchPattern |= CacheSize.MICRO.getFilterPattern();
		if (filterSize.charAt(1) == '1')
			sizeMatchPattern |= CacheSize.SMALL.getFilterPattern();
		if (filterSize.charAt(2) == '1')
			sizeMatchPattern |= CacheSize.REGULAR.getFilterPattern();
		if (filterSize.charAt(3) == '1')
			sizeMatchPattern |= CacheSize.LARGE.getFilterPattern();
		if (filterSize.charAt(4) == '1')
			sizeMatchPattern |= CacheSize.VERY_LARGE.getFilterPattern();
		if (filterSize.charAt(5) == '1')
			sizeMatchPattern |= CacheSize.NOT_CHOSEN.getFilterPattern();
		hasSizeMatchPattern = sizeMatchPattern != CacheSize
				.getAllFilterPatterns();
		distdirec = profile.getFilterDist().charAt(0) == 'L' ? SMALLER
				: GREATER;
		fscDist = Common.parseDouble(profile.getFilterDist().substring(1)); // Distance
		diffdirec = profile.getFilterDiff().charAt(0) == 'L' ? SMALLER
				: (profile.getFilterDiff().charAt(0) == '=' ? EQUAL : GREATER);
		fscDiff = Common.parseDouble(profile.getFilterDiff().substring(1)); // Difficulty
		terrdirec = profile.getFilterTerr().charAt(0) == 'L' ? SMALLER
				: (profile.getFilterTerr().charAt(0) == '=' ? EQUAL : GREATER);
		fscTerr = Common.parseDouble(profile.getFilterTerr().substring(1)); // Terrain
		attributesYesPattern = profile.getFilterAttrYes();
		attributesNoPattern = profile.getFilterAttrNo();
		attributesChoice = profile.getFilterAttrChoice();
	}

	/**
	 * Apply the filter. Caches that match a criteria are flagged is_filtered =
	 * true. The table model is responsible for displaying or not displaying a
	 * cache that is filtered.
	 */
	public void doFilter() {
		CacheDB cacheDB = Global.getProfile().cacheDB;
		Hashtable examinedCaches;
		if (cacheDB.size() == 0)
			return;
		if (!hasFilter()) { // If the filter was completely reset, we can just
			// clear it
			clearFilter();
			return;
		}
		Global.getProfile().selectionChanged = true;
		CacheHolder ch;
		examinedCaches = new Hashtable(cacheDB.size());

		for (int i = cacheDB.size() - 1; i >= 0; i--) {
			ch = cacheDB.get(i);
			if (examinedCaches.containsKey(ch))
				continue;

			boolean filterCache = excludedByFilter(ch);
			if (!filterCache && ch.getMainCache() != null
					&& ((typeMatchPattern & TYPE_MAIN) != 0)) {
				if (examinedCaches.containsKey(ch.getMainCache())) {
					filterCache = ch.getMainCache().isFiltered();
				} else {
					ch.getMainCache().setFiltered(
							excludedByFilter(ch.getMainCache()));
					filterCache = ch.getMainCache().isFiltered();
					examinedCaches.put(ch.getMainCache(), null);
				}
			}
			ch.setFiltered(filterCache);
		}
		Global.getProfile().setFilterActive(FILTER_ACTIVE);
		examinedCaches = null;
		// Global.getProfile().hasUnsavedChanges=true;
	}

	public boolean excludedByFilter(CacheHolder ch) {
		// Match once against type pattern and once against rose pattern
		// Default is_filtered = false, means will be displayed!
		// If cache does not match type or rose pattern then is_filtered is set
		// to true
		// and we proceed to next cache (no further tests needed)
		// Then we check the other filter criteria one by one: As soon as one is
		// found that
		// eliminates the cache (i.e. sets is_filtered to true), we can skip the
		// other tests
		// A cache is only displayed (i.e. is_filtered = false) if it meets all
		// 9 filter criteria
		double dummyd1;
		boolean cacheFiltered = false;
		do {
			// /////////////////////////////
			// Filter criterium 1: Cache type
			// /////////////////////////////
			if (hasTypeMatchPattern) { // Only do the checks if we have a
				// filter
				int cacheTypePattern = 0;
				// As each cache can only have one type, we can use else if and
				// set the type
				if (ch.getType() == CacheType.CUSTOM)
					cacheTypePattern = CUSTOM;
				else if (ch.getType() == CacheType.TRADITIONAL)
					cacheTypePattern = TRADITIONAL;
				else if (ch.getType() == CacheType.MULTI)
					cacheTypePattern = MULTI;
				else if (ch.getType() == CacheType.VIRTUAL)
					cacheTypePattern = VIRTUAL;
				else if (ch.getType() == CacheType.LETTERBOX)
					cacheTypePattern = LETTER;
				else if (ch.getType() == CacheType.EVENT)
					cacheTypePattern = EVENT;
				else if (ch.getType() == CacheType.UNKNOWN)
					cacheTypePattern = MYSTERY;
				else if (ch.getType() == CacheType.WEBCAM)
					cacheTypePattern = WEBCAM;
				else if (ch.getType() == CacheType.LOCATIONLESS)
					cacheTypePattern = LOCLESS;
				else if (ch.getType() == CacheType.EARTH)
					cacheTypePattern = EARTH;
				else if (ch.getType() == CacheType.MEGA_EVENT)
					cacheTypePattern = MEGA;
				else if (ch.getType() == CacheType.PARKING)
					cacheTypePattern = PARKING;
				else if (ch.getType() == CacheType.STAGE)
					cacheTypePattern = STAGE;
				else if (ch.getType() == CacheType.QUESTION)
					cacheTypePattern = QUESTION;
				else if (ch.getType() == CacheType.FINAL)
					cacheTypePattern = FINAL;
				else if (ch.getType() == CacheType.TRAILHEAD)
					cacheTypePattern = TRAILHEAD;
				else if (ch.getType() == CacheType.REFERENCE)
					cacheTypePattern = REFERENCE;
				else if (ch.getType() == CacheType.CITO)
					cacheTypePattern = CITO;
				else if (ch.getType() == CacheType.WHEREIGO)
					cacheTypePattern = WHERIGO;
				if ((cacheTypePattern & typeMatchPattern) == 0) {
					cacheFiltered = true;
					break;
				}
			}
			// /////////////////////////////
			// Filter criterium 2: Bearing from centre
			// /////////////////////////////
			if (hasRoseMatchPattern) {
				if (!roseMatchPattern.contains(ch.getBearing())) {
					cacheFiltered = true;
					break;
				}
			}
			// /////////////////////////////
			// Filter criterium 3: Distance
			// /////////////////////////////
			if (fscDist > 0.0) {
				dummyd1 = ch.getKilom();
				if (distdirec == SMALLER && dummyd1 > fscDist) {
					cacheFiltered = true;
					break;
				}
				if (distdirec == GREATER && dummyd1 < fscDist) {
					cacheFiltered = true;
					break;
				}
			}
			// /////////////////////////////
			// Filter criterium 4: Difficulty
			// /////////////////////////////
			if (fscDiff > 0.0) {
				dummyd1 = ch.getDifficulty().getOldCWValue() / 10D;
				if (diffdirec == SMALLER && dummyd1 > fscDiff) {
					cacheFiltered = true;
					break;
				}
				if (diffdirec == EQUAL && dummyd1 != fscDiff) {
					cacheFiltered = true;
					break;
				}
				if (diffdirec == GREATER && dummyd1 < fscDiff) {
					cacheFiltered = true;
					break;
				}
			}
			// /////////////////////////////
			// Filter criterium 5: Terrain
			// /////////////////////////////
			if (fscTerr > 0.0) {
				dummyd1 = ch.getTerrain().getOldCWValue() / 10D;
				if (terrdirec == SMALLER && dummyd1 > fscTerr) {
					cacheFiltered = true;
					break;
				}
				if (terrdirec == EQUAL && dummyd1 != fscTerr) {
					cacheFiltered = true;
					break;
				}
				if (terrdirec == GREATER && dummyd1 < fscTerr) {
					cacheFiltered = true;
					break;
				}
			}

			// /////////////////////////////
			// Filter criterium 6: Found by me
			// /////////////////////////////
			if ((ch.isFound() && !foundByMe)
					|| (!ch.isFound() && !notFoundByMe)) {
				cacheFiltered = true;
				break;
			}
			// /////////////////////////////
			// Filter criterium 7: Owned by me
			// /////////////////////////////
			if ((ch.isOwned() && !ownedByMe)
					|| (!ch.isOwned() && !notOwnedByMe)) {
				cacheFiltered = true;
				break;
			}
			// /////////////////////////////
			// Filter criterium 8: Archived
			// /////////////////////////////
			if ((ch.isArchived() && !archived)
					|| (!ch.isArchived() && !notArchived)) {
				cacheFiltered = true;
				break;
			}
			// /////////////////////////////
			// Filter criterium 9: Unavailable
			// /////////////////////////////
			if ((ch.isAvailable() && !available)
					|| (!ch.isAvailable() && !notAvailable)) {
				cacheFiltered = true;
				break;
			}
			// /////////////////////////////
			// Filter criterium 10: Size
			// /////////////////////////////
			if (hasSizeMatchPattern) {
				int cacheSizePattern = ch.getCacheSize().getFilterPattern();
				if ((cacheSizePattern & sizeMatchPattern) == 0) {
					cacheFiltered = true;
					break;
				}
			}
			// /////////////////////////////
			// Filter criterium 11: Attributes
			// /////////////////////////////
			if ((attributesYesPattern != 0 || attributesNoPattern != 0)
					&& ch.getMainCache() == null) {
				if (attributesChoice == 0) {
					// AND-condition:
					if ((ch.getAttributesYes() & attributesYesPattern) != attributesYesPattern
							|| (ch.getAttributesNo() & attributesNoPattern) != attributesNoPattern) {
						cacheFiltered = true;
						break;
					}
				} else if (attributesChoice == 1) {
					// OR-condition:
					if ((ch.getAttributesYes() & attributesYesPattern) == 0
							&& (ch.getAttributesNo() & attributesNoPattern) == 0) {
						cacheFiltered = true;
						break;
					}
				} else {
					// NOT-condition:
					if ((ch.getAttributesYes() & attributesYesPattern) != 0
							|| (ch.getAttributesNo() & attributesNoPattern) != 0) {
						cacheFiltered = true;
						break;
					}
				}
			}
			if (!cacheStatus.equals("")) {
				if (!useRegexp) {
					if (ch.getCacheStatus().toLowerCase().indexOf(
							cacheStatus.toLowerCase()) < 0) {
						cacheFiltered = true;
						break;
					}
				} else {
					Regex rex = new Regex(cacheStatus.toLowerCase());
					rex.search(ch.getCacheStatus().toLowerCase());
					if (rex.stringMatched() == null) {
						cacheFiltered = true;
						break;
					}
				}
			}
			break;
		} while (true);
		return cacheFiltered;
	}

	/**
	 * Switches flag to invert filter property.
	 */
	public void invertFilter() {
		Global.getProfile().setFilterInverted(
				!Global.getProfile().isFilterInverted());
	}

	/**
	 * Clear the is_filtered flag from the cache database.
	 */
	public void clearFilter() {
		Global.getProfile().selectionChanged = true;
		CacheDB cacheDB = Global.getProfile().cacheDB;
		for (int i = cacheDB.size() - 1; i >= 0; i--) {
			CacheHolder ch = cacheDB.get(i);
			ch.setFiltered(false);
		}
		Global.getProfile().setFilterActive(FILTER_INACTIVE);
	}

	public boolean hasFilter() {
		Profile prof = Global.getProfile();
		return !(prof.getFilterType().equals(FilterData.FILTERTYPE)
				&& prof.getFilterRose().equals(EnumSet.allOf(Bearing.class))
				&& prof.getFilterVar().equals(FilterData.FILTERVAR)
				&& prof.getFilterSize().equals(FilterData.FILTERSIZE)
				&& prof.getFilterDist().equals("L")
				&& prof.getFilterDiff().equals("L")
				&& prof.getFilterTerr().equals("L")
				&& prof.getFilterAttrYes() == 0l
				&& prof.getFilterAttrNo() == 0l && prof.getFilterStatus()
				.equals(""));
	}

}
