/*
    CacheWolf is a software for PocketPC, Win and Linux that
    enables paperless caching.
    It supports the sites geocaching.com and opencaching.de

    Copyright (C) 2006  CacheWolf development team
    See http://developer.berlios.de/projects/cachewolf/
    for more information.
    Contact: 	bilbowolf@users.berlios.de
		kalli@users.berlios.de

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation version 2 of the License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package CacheWolf.imp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import CacheWolf.Global;
import CacheWolf.beans.CWPoint;
import CacheWolf.beans.CacheDB;
import CacheWolf.beans.CacheHolder;
import CacheWolf.beans.CacheImages;
import CacheWolf.beans.ImageInfo;
import CacheWolf.beans.Preferences;
import CacheWolf.beans.Profile;
import CacheWolf.beans.Travelbug;
import CacheWolf.gui.InfoBox;
import CacheWolf.navi.Metrics;
import CacheWolf.util.Common;
import CacheWolf.util.DateFormat;
import CacheWolf.util.Extractor;
import CacheWolf.util.HttpConnection;
import CacheWolf.util.MyLocale;
import CacheWolf.util.SafeXML;

import com.stevesoft.ewe_pat.Regex;

import de.cachehound.beans.CacheHolderDetail;
import de.cachehound.beans.LogList;
import de.cachehound.factory.CWPointFactory;
import de.cachehound.factory.LogFactory;
import de.cachehound.types.CacheSize;
import de.cachehound.types.CacheType;
import de.cachehound.types.Difficulty;
import de.cachehound.types.LogType;
import de.cachehound.types.Terrain;
import ewe.data.Property;
import ewe.data.PropertyList;
import ewe.io.FileBase;
import ewe.io.FileInputStream;
import ewe.io.IOException;
import ewe.io.JavaUtf8Codec;
import ewe.net.Socket;
import ewe.net.URL;
import ewe.net.UnknownHostException;
import ewe.sys.Convert;
import ewe.sys.Double;
import ewe.sys.Time;
import ewe.sys.Vm;
import ewe.ui.FormBase;
import ewe.ui.MessageBox;
import ewe.util.ByteArray;
import ewe.util.CharArray;
import ewe.util.Enumeration;
import ewe.util.Hashtable;
import ewe.util.Properties;
import ewe.util.Utils;
import ewe.util.Vector;

/**
 * Class to spider caches from gc.com
 */
public class SpiderGC {
	private static Logger logger = LoggerFactory.getLogger(SpiderGC.class);

	/**
	 * The maximum number of logs that will be stored
	 */
	public static String passwort = ""; // Can be pre-set from preferences
	public static boolean loggedIn = false;

	// Return values for spider action
	/**
	 * Ignoring a premium member cache when spidering from a non premium account
	 */
	public static int SPIDER_IGNORE_PREMIUM = -2;
	/** Canceling spider process */
	public static int SPIDER_CANCEL = -1;
	/** Error occured while spidering */
	public static int SPIDER_ERROR = 0;
	/** Cache was spidered without problems */
	public static int SPIDER_OK = 1;

	private static int ERR_LOGIN = -10;
	private static Preferences pref;
	private Profile profile;
	private static String viewstate = "";
	private static String viewstate1 = "";
	private static String eventvalidation = "";
	private static String cookieID = "";
	private static String cookieSession = "";
	private static double distance = 0;
	private Regex inRex = new Regex();
	private CacheDB cacheDB;
	private Vector cachesToLoad = new Vector();
	private InfoBox infB;
	private static SpiderProperties p = null;

	/**
	 * Use the new Class de.cachehound.util.SpiderService instead.
	 */
	@Deprecated
	public SpiderGC(Preferences prf, Profile profile) {
		this.profile = profile;
		this.cacheDB = profile.cacheDB;
		pref = prf;
		if (p == null) {
			p = new SpiderProperties();
		}
	}

	/**
	 * Method to login the user to gc.com It will request a password and use the
	 * alias defined in preferences If the login page cannot be fetched, the
	 * password is cleared. If the login fails, an appropriate message is
	 * displayed.
	 */
	private int login() {
		loggedIn = false;
		String start, loginPage, loginSuccess, nextPage;
		try {
			loginPage = p.getProp("loginPage");
			loginSuccess = p.getProp("loginSuccess");
			nextPage = p.getProp("nextPage");
		} catch (Exception ex) { // Tag not found in spider.def
			logger.error("Tag noct found in spider.def", ex);
			return ERR_LOGIN;
		}
		// Get password
		InfoBox localInfB = new InfoBox(MyLocale.getMsg(5506, "Password"),
				MyLocale.getMsg(5505, "Enter Password"), InfoBox.INPUT);
		localInfB.feedback.setText(passwort); // Remember the PWD for next
		// time
		localInfB.feedback.isPassword = true;
		int code = FormBase.IDOK;
		if (passwort.equals("")) {
			code = localInfB.execute();
			passwort = localInfB.getInput();
		}
		localInfB.close(0);
		if (code != FormBase.IDOK)
			return code;
		// Now start the login proper
		localInfB = new InfoBox(MyLocale.getMsg(5507, "Status"), MyLocale
				.getMsg(5508, "Logging in..."));
		localInfB.exec();
		try {
			logger.debug("[login]:Fetching login page");
			// Access the page once to get a viewstate
			start = fetch(loginPage); // http://www.geocaching.com/login/Default.aspx
			if (start.equals("")) {
				localInfB.close(0);
				(new MessageBox(
						MyLocale.getMsg(5500, "Error"),
						MyLocale
								.getMsg(5499,
										"Error loading login page.%0aPlease check your internet connection."),
						FormBase.OKB)).execute();
				logger.error("[login]:Could not fetch: gc.com login page");
				return ERR_LOGIN;
			}
		} catch (Exception ex) {
			localInfB.close(0);
			(new MessageBox(
					MyLocale.getMsg(5500, "Error"),
					MyLocale
							.getMsg(5499,
									"Error loading login page.%0aPlease check your internet connection."),
					FormBase.OKB)).execute();
			logger.error("[login]:Could not fetch: gc.com login page", ex);
			return ERR_LOGIN;
		}
		if (!localInfB.isClosed) { // If user has not aborted, we continue
			Regex rexCookieID = new Regex("(?i)Set-Cookie: userid=(.*?);.*");
			Regex rexViewstate = new Regex(
					"id=\"__VIEWSTATE\" value=\"(.*?)\" />");
			Regex rexViewstate1 = new Regex(
					"id=\"__VIEWSTATE1\" value=\"(.*?)\" />");
			Regex rexEventvalidation = new Regex(
					"id=\"__EVENTVALIDATION\" value=\"(.*?)\" />");
			Regex rexCookieSession = new Regex(
					"(?i)Set-Cookie: ASP.NET_SessionId=(.*?);.*");
			rexViewstate.search(start);
			if (rexViewstate.didMatch()) {
				viewstate = rexViewstate.stringMatched(1);
				// Vm.debug("ViewState: " + viewstate);
			} else
				logger.error("[login]:Viewstate not found before login");

			if (start.indexOf(loginSuccess) > 0)
				logger.debug("[login]:Already logged in");
			else {
				rexEventvalidation.search(start);
				if (rexEventvalidation.didMatch()) {
					eventvalidation = rexEventvalidation.stringMatched(1);
					// Vm.debug("EVENTVALIDATION: " + eventvalidation);
				} else
					logger
							.warn("[login]:Eventvalidation not found before login");
				// Ok now login!
				try {
					logger.debug("[login]:Logging in as " + pref.myAlias);
					StringBuilder sb = new StringBuilder(1000);
					sb.append(URL.encodeURL("__VIEWSTATE", false));
					sb.append("=");
					sb.append(URL.encodeURL(viewstate, false));
					sb.append("&ctl00%24ContentBody%24");
					sb.append(URL.encodeURL("myUsername", false));
					sb.append("=");
					sb.append(encodeUTF8URL(Utils
							.encodeJavaUtf8String(pref.myAlias)));
					sb.append("&ctl00%24ContentBody%24");
					sb.append(URL.encodeURL("myPassword", false));
					sb.append("=");
					sb.append(encodeUTF8URL(Utils
							.encodeJavaUtf8String(passwort)));
					sb.append("&ctl00%24ContentBody%24");
					sb.append(URL.encodeURL("cookie", false));
					sb.append("=");
					sb.append(URL.encodeURL("on", false));
					sb.append("&ctl00%24ContentBody%24");
					sb.append(URL.encodeURL("Button1", false));
					sb.append("=");
					sb.append(URL.encodeURL("Login", false));
					// sb.append("&");
					// sb.append(URL.encodeURL("__EVENTVALIDATION",false));
					// sb.append("=");
					// sb.append(URL.encodeURL(eventvalidation,false));
					start = fetch_post(loginPage, sb.toString(), nextPage); // /login/default.aspx
					if (start.indexOf(loginSuccess) > 0)
						logger.debug("[login]:Login successful");
					else {
						logger
								.error("[login]:Login failed. Wrong Account or Password?");
						logger.error("[login.LoginUrl]:" + sb.toString());
						logger.error("[login.Answer]:" + start);
						localInfB.close(0);
						(new MessageBox(
								MyLocale.getMsg(5500, "Error"),
								MyLocale
										.getMsg(5501,
												"Login failed! Wrong account or password?"),
								FormBase.OKB)).execute();
						return ERR_LOGIN;
					}
				} catch (Exception ex) {
					logger.error("[login]:Login failed with exception.", ex);
					localInfB.close(0);
					(new MessageBox(
							MyLocale.getMsg(5500, "Error"),
							MyLocale
									.getMsg(5501,
											"Login failed. Error loading page after login."),
							FormBase.OKB)).execute();
					return ERR_LOGIN;
				}
			}

			rexViewstate.search(start);
			if (!rexViewstate.didMatch()) {
				logger.warn("[login]:Viewstate not found");
			}
			viewstate = rexViewstate.stringMatched(1);

			rexViewstate1.search(start);
			if (!rexViewstate1.didMatch()) {
				logger.warn("[login]:Viewstate1 not found");
			}
			viewstate1 = rexViewstate1.stringMatched(1);

			rexCookieID.search(start);
			if (!rexCookieID.didMatch()) {
				logger.warn("[login]:CookieID not found. Using old one.");
			} else
				cookieID = rexCookieID.stringMatched(1);
			// Vm.debug(cookieID);
			rexCookieSession.search(start);
			if (!rexCookieSession.didMatch()) {
				logger.warn("[login]:CookieSession not found. Using old one.");
				// cookieSession="";
			} else
				cookieSession = rexCookieSession.stringMatched(1);
			// Vm.debug("cookieSession = " + cookieSession);
		}
		boolean loginAborted = localInfB.isClosed;
		localInfB.close(0);
		if (loginAborted)
			return FormBase.IDCANCEL;
		else {
			loggedIn = true;
			return FormBase.IDOK;
		}
	}

	/**
	 * Method to spider a single cache. It assumes a login has already been
	 * performed!
	 * 
	 * @return 1 if spider was successful, -1 if spider was cancelled by closing
	 *         the infobox, 0 error, but continue with next cache
	 */
	public int spiderSingle(int number, InfoBox pInfB, boolean forceLogin,
			boolean loadAllLogs) {
		int ret = -1;
		this.infB = pInfB;
		CacheHolder ch = new CacheHolder(); // cacheDB.get(number);
		ch.setWayPoint(cacheDB.get(number).getWayPoint());
		if (ch.isAddiWpt())
			return -1; // No point re-spidering an addi waypoint, comes with
		// parent

		// check if we need to login
		if (!loggedIn || forceLogin) {
			if (this.login() != FormBase.IDOK)
				return -1;
			// loggedIn is already set by this.login()
		}
		try {
			// Read the cache data from GC.COM and compare to old data
			ret = getCacheByWaypointName(ch, true, pref.downloadPics,
					pref.downloadTBs, false, loadAllLogs);
			// Save the spidered data
			if (ret == SPIDER_OK) {
				CacheHolder cacheInDB = cacheDB.get(number);
				cacheInDB.initStates(false);
				if (cacheInDB.isFound() && !ch.isFound() && !loadAllLogs) {
					// If the number of logs to spider is 5 or less, then the
					// "not found" information
					// of the spidered cache is not credible. In this case it
					// should not overwrite
					// the "found" state of an existing cache.
					ch.setFound(true);
				}
				// preserve rating information
				ch.setNumRecommended(cacheInDB.getNumRecommended());
				if (pref.downloadPics) {
					// delete obsolete images when we have current set
					CacheImages.cleanupOldImages(cacheInDB.getExistingDetails()
							.getImages(), ch.getFreshDetails().getImages());
				} else {
					// preserve images if not downloaded
					// Änderung Florian für Mailupdate
					// ch.getFreshDetails().images =
					// cacheInDB.getExistingDetails().images;
					ch.getFreshDetails().setImages(
							cacheInDB.getFreshDetails().getImages());
				}
				cacheInDB.update(ch);
				cacheInDB.save();
			}
		} catch (Exception ex) {
			logger.error("Error spidering " + ch.getWayPoint()
					+ " in spiderSingle", ex);
		}
		return ret;
	} // spiderSingle

	/**
	 * Fetch the coordinates of a waypoint from GC
	 * 
	 * @param wayPoint
	 *            the name of the waypoint
	 * @return the cache coordinates
	 */
	public String getCacheCoordinates(String wayPoint) {
		String completeWebPage;
		// Check whether spider definitions could be loaded, if not issue
		// appropriate message and terminate
		// Try to login. If login fails, issue appropriate message and terminate
		if (!loggedIn || Global.getPref().forceLogin) {
			if (login() != FormBase.IDOK) {
				return "";
			}
		}
		InfoBox localInfB = new InfoBox("Info", "Loading",
				InfoBox.PROGRESS_WITH_WARNINGS);
		localInfB.exec();
		try {
			String doc = p.getProp("waypoint") + wayPoint;
			logger.debug("Fetching: " + wayPoint);
			completeWebPage = fetch(doc);
		} catch (Exception ex) {
			localInfB.close(0);
			logger.error("Could not fetch " + wayPoint, ex);
			return "";
		}
		localInfB.close(0);
		try {
			return getLatLon(completeWebPage);
		} catch (Exception ex) {
			logger.error("????", ex);
			return "????";
		}
	}

	/**
	 * Method to start the spider for a search around the centre coordinates
	 */
	public void doIt() {
		doIt(false);
	}

	public void doIt(boolean spiderAllFinds) {
		String postStr, dummy, ln, wpt;
		Regex lineRex;
		CacheHolder holder;
		CWPoint origin = pref.getCurCenter();
		// No need to copy curCentrePt as it is only read and not written
		if (!spiderAllFinds && !origin.isValid()) {
			(new MessageBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(
					5509, "Coordinates for centre must be set"), FormBase.OKB))
					.execute();
			return;
		}
		if (System.getProperty("os.name") != null)
			logger.debug("Operating system: " + System.getProperty("os.name")
					+ "/" + System.getProperty("os.arch"));
		if (System.getProperty("java.vendor") != null)
			logger.debug("Java: " + System.getProperty("java.vendor") + "/"
					+ System.getProperty("java.version"));
		CacheHolder ch;
		// Reset states for all caches when spidering
		// (http://tinyurl.com/dzjh7p)
		for (int i = 0; i < cacheDB.size(); i++) {
			ch = cacheDB.get(i);
			if (ch.getMainCache() == null)
				ch.initStates(false);
		}
		String start = "";
		Regex rexViewstate = new Regex("id=\"__VIEWSTATE\" value=\"(.*)\" />");
		Regex rexViewstate1 = new Regex("id=\"__VIEWSTATE1\" value=\"(.*)\" />");
		Regex rexEventvalidation = new Regex(
				"id=\"__EVENTVALIDATION\" value=\"(.*)\" />");
		String doc = "";

		if (!loggedIn || Global.getPref().forceLogin) {
			if (login() != FormBase.IDOK)
				return;
		}

		boolean doNotgetFound = false;

		OCXMLImporterScreen options;
		if (spiderAllFinds) {
			options = new OCXMLImporterScreen(MyLocale.getMsg(5510,
					"Spider Options"), OCXMLImporterScreen.MAXNUMBER
					| OCXMLImporterScreen.IMAGES | OCXMLImporterScreen.ISGC
					| OCXMLImporterScreen.TRAVELBUGS
					| OCXMLImporterScreen.MAXLOGS | OCXMLImporterScreen.TYPE);
			if (options.execute() == FormBase.IDCANCEL) {
				return;
			}

			distance = 1;
		} else {
			options = new OCXMLImporterScreen(MyLocale.getMsg(5510,
					"Spider Options"), OCXMLImporterScreen.MAXNUMBER
					| OCXMLImporterScreen.INCLUDEFOUND
					| OCXMLImporterScreen.DIST | OCXMLImporterScreen.IMAGES
					| OCXMLImporterScreen.ISGC | OCXMLImporterScreen.TRAVELBUGS
					| OCXMLImporterScreen.MAXLOGS | OCXMLImporterScreen.TYPE);
			if (options.execute() == FormBase.IDCANCEL) {
				return;
			}
			String dist = options.distanceInput.getText();
			if (dist.length() == 0)
				return;
			distance = Common.parseDouble(dist);

			// save last radius to profile
			Double distDouble = new Double();
			distDouble.value = distance;
			dist = distDouble.toString(0, 1, 0).replace(',', '.');
			profile.setDistGC(dist);

			doNotgetFound = options.foundCheckBox.getState();
		}

		int maxNumber = -1;
		String maxNumberString = options.maxNumberInput.getText();
		if (maxNumberString.length() != 0) {
			maxNumber = Common.parseInt(maxNumberString);
		}
		if (maxNumber != pref.maxSpiderNumber) {
			pref.maxSpiderNumber = maxNumber;
			pref.savePreferences();
		}
		if (maxNumber == 0)
			return;
		boolean maxNumberAbort = false;

		boolean getImages = options.imagesCheckBox.getState();
		boolean getTBs = options.travelbugsCheckBox.getState();

		String cacheTypeRestriction = options.getCacheTypeRestriction(p);

		options.close(0);

		// max distance in miles for URL, so we can get more than 80km
		double saveDistanceInMiles = distance;
		if (Global.getPref().metricSystem != Metrics.IMPERIAL) {
			saveDistanceInMiles = Metrics.convertUnit(distance,
					Metrics.KILOMETER, Metrics.MILES);
		}
		// add a mile to be save from different distance calculations in CW and
		// at GC
		saveDistanceInMiles = java.lang.Math.ceil(saveDistanceInMiles) + 1;

		Hashtable cachesToUpdate = new Hashtable(cacheDB.size());

		if (pref.spiderUpdates != Preferences.NO) {
			double distanceInKm = distance;
			if (Global.getPref().metricSystem == Metrics.IMPERIAL) {
				distanceInKm = Metrics.convertUnit(distance, Metrics.MILES,
						Metrics.KILOMETER);
			}
			for (int i = 0; i < cacheDB.size(); i++) {
				ch = cacheDB.get(i);
				if (spiderAllFinds) {
					if ((ch.getWayPoint().substring(0, 2)
							.equalsIgnoreCase("GC"))) {
						cachesToUpdate.put(ch.getWayPoint(), ch);
					}
				} else {
					if ((!ch.isArchived())
							&& (ch.getKilom() <= distanceInKm)
							&& !(doNotgetFound && ch.isFound())
							&& (ch.getWayPoint().substring(0, 2)
									.equalsIgnoreCase("GC"))) {
						cachesToUpdate.put(ch.getWayPoint(), ch);
					}
				}
			}
		}

		// =======
		// Prepare list of all caches that are to be spidered
		// =======
		Vm.showWait(true);
		infB = new InfoBox("Status", MyLocale.getMsg(5502,
				"Fetching first page..."));
		infB.exec();
		// Get first page
		try {
			if (spiderAllFinds) {
				ln = p.getProp("firstPageFinds")
						+ encodeUTF8URL(Utils
								.encodeJavaUtf8String(pref.myAlias));
			} else {
				ln = p.getProp("firstPage") + origin.getLatDeg(CWPoint.DD)
						+ p.getProp("firstPage2")
						+ origin.getLonDeg(CWPoint.DD)
						+ p.getProp("maxDistance")
						+ Integer.toString((int) saveDistanceInMiles);
				if (doNotgetFound)
					ln = ln + p.getProp("showOnlyFound");
			}
			ln = ln + cacheTypeRestriction;
			logger.debug("Getting first page: " + ln);
			start = fetch(ln);
			logger.debug("Got first page");
		} catch (Exception ex) {
			logger.error("Error fetching first list page", ex, true);
			Vm.showWait(false);
			infB.close(0);
			(new MessageBox(MyLocale.getMsg(5500, "Error"), MyLocale.getMsg(
					5503, "Error fetching first list page."), FormBase.OKB))
					.execute();
			return;
		}
		dummy = "";
		// String lineBlck = "";
		int page_number = 1;
		try {
			lineRex = new Regex(p.getProp("lineRex")); // "<tr
			// bgcolor=((?s).*?)</tr>"
		} catch (Exception ex) {
			logger.error("", ex);
			infB.close(0);
			Vm.showWait(false);
			return;
		}
		int page = 0;
		int found_on_page = 0;
		try {
			// Loop till maximum distance has been found or no more caches are
			// in the list
			while (distance > 0) {
				if (infB.isClosed)
					break;

				rexViewstate.search(start);
				if (rexViewstate.didMatch()) {
					viewstate = rexViewstate.stringMatched(1);
					// Vm.debug("ViewState: " + viewstate);
				} else {
					viewstate = "";
					logger.warn("Viewstate not found");
				}

				rexViewstate1.search(start);
				if (rexViewstate1.didMatch()) {
					viewstate1 = rexViewstate1.stringMatched(1);
					// Vm.debug("ViewState: " + viewstate);
				} else {
					viewstate1 = "";
					logger.warn("Viewstate1 not found");
				}

				rexEventvalidation.search(start);
				if (rexEventvalidation.didMatch()) {
					eventvalidation = rexEventvalidation.stringMatched(1);
					// Vm.debug("EVENTVALIDATION: " + eventvalidation);
				} else {
					eventvalidation = "";
					logger.warn("Eventvalidation not found");
				}

				// Vm.debug("In loop");
				Regex listBlockRex = new Regex(p.getProp("listBlockRex")); // "<table
				// id=\"dlResults\"((?s).*?)</table>"
				listBlockRex.search(start);
				dummy = listBlockRex.stringMatched(1);
				try {
					lineRex.search(dummy);
				} catch (NullPointerException nex) {
					logger.error("Ignored Exception", nex, true);
				}
				while (lineRex.didMatch()) {
					// Vm.debug(getDist(lineRex.stringMatched(1)) + " / "
					// +getWP(lineRex.stringMatched(1)));
					found_on_page++;
					if (getDist(lineRex.stringMatched(1)) <= distance) {
						String waypoint = getWP(lineRex.stringMatched(1));
						CacheHolder existingCache;
						if ((existingCache = cacheDB.get(waypoint)) == null) {
							if ((maxNumber > 0)
									&& (cachesToLoad.size() >= maxNumber)) {
								maxNumberAbort = true;

								// add no more caches
								distance = 0;

								// don't update existing caches, because list is
								// not correct when aborting
								cachesToUpdate.clear();
							} else {
								cachesToLoad.add(waypoint);

								// if we don't want to update caches, we can
								// stop directly after adding the maximum of new
								// caches.
								if ((pref.spiderUpdates == Preferences.NO)
										&& (maxNumber > 0)
										&& (cachesToLoad.size() >= maxNumber)) {
									maxNumberAbort = true;

									// add no more caches
									distance = 0;

									// don't update existing caches, because
									// list is not correct when aborting
									cachesToUpdate.clear();
								}
							}
						} else {
							logger.debug(waypoint + " already in DB");
							ch = existingCache;
							// If the <strike> tag is used, the cache is marked
							// as unavailable or archived
							boolean is_archived_GC = lineRex.stringMatched(1)
									.indexOf("<strike><font color=\"red\">") != -1;
							boolean is_available_GC = lineRex.stringMatched(1)
									.indexOf("<strike>") == -1;
							if (ch.isArchived() != is_archived_GC) {
								// Update the database with the cache status
								logger.debug("Updating status of "
										+ waypoint
										+ " to "
										+ (is_archived_GC ? "archived"
												: "not archived"));
								if (ch.isArchived()) {
									cachesToUpdate.put(ch.getWayPoint(), ch);
								}
								ch.setArchived(is_archived_GC);
							} else if (ch.isAvailable() != is_available_GC) {
								// Update the database with the cache status
								logger.debug("Updating status of "
										+ waypoint
										+ " to "
										+ (is_available_GC ? "available"
												: "not available"));
								ch.setAvailable(is_available_GC);
							} else if (spiderAllFinds && !ch.isFound()) {
								// Update the database with the cache status
								logger.debug("Updating status of " + waypoint
										+ " to found");
								ch.setFound(true);
							} else {
								cachesToUpdate.remove(ch.getWayPoint());
							}
						}
					} else
						distance = 0;
					lineRex.searchFrom(dummy, lineRex.matchedTo());
				}

				page++;
				infB.setInfo(MyLocale.getMsg(5521, "Page ") + page + "\n"
						+ MyLocale.getMsg(5511, "Found ") + cachesToLoad.size()
						+ MyLocale.getMsg(5512, " caches"));

				if (found_on_page < 20)
					distance = 0;
				if (spiderAllFinds) {
					postStr = p.getProp("firstLine");
				} else {
					// postStr = p.getProp("firstLine") + "lat="
					// + origin.getLatDeg(CWPoint.DD) + "&lng="
					// + origin.getLonDeg(CWPoint.DD)
					// + p.getProp("maxDistance")
					// + Integer.toString((int) saveDistanceInMiles);
					postStr = p.getProp("firstLine")
							+ origin.getLatDeg(CWPoint.DD)
							+ p.getProp("firstLine2")
							+ origin.getLonDeg(CWPoint.DD)
							+ p.getProp("maxDistance")
							+ Integer.toString((int) saveDistanceInMiles);
					if (doNotgetFound)
						postStr = postStr + p.getProp("showOnlyFound");
				}
				postStr = postStr + cacheTypeRestriction;
				if (distance > 0) {
					page_number++;
					String strNextPage;
					// if (page_number >= 15)
					// page_number = 5;
					// if (page_number < 10) {
					// strNextPage = "ctl00$ContentBody$pgrTop$ctl0"
					// + page_number;
					// } else {
					// strNextPage = "ctl00$ContentBody$pgrTop$ctl"
					// + page_number;
					// }
					strNextPage = "ctl00$ContentBody$pgrTop$ctl08";

					doc = URL.encodeURL("__EVENTTARGET", false) + "="
							+ URL.encodeURL(strNextPage, false) + "&"
							+ URL.encodeURL("__EVENTARGUMENT", false) + "="
							+ URL.encodeURL("", false)
							+ "&"
							// + URL.encodeURL("__VIEWSTATEFIELDCOUNT", false) +
							// "=2"
							+ "&" + URL.encodeURL("__VIEWSTATE", false) + "="
							+ URL.encodeURL(viewstate, false);
					// + "&" + URL.encodeURL("__VIEWSTATE1", false) + "=" +
					// URL.encodeURL(viewstate1, false);
					// + "&" + URL.encodeURL("__EVENTVALIDATION",false) +"="+
					// URL.encodeURL(eventvalidation,false);
					try {
						start = "";
						logger.debug("Fetching next list page:" + doc);
						start = fetch_post(postStr, doc, p
								.getProp("nextListPage"));
					} catch (Exception ex) {
						// Vm.debug("Couldn't get the next page");
						logger.error("Error getting next page", ex);
					}
				}
				// Vm.debug("Distance is now: " + distance);
				found_on_page = 0;
			}
		} catch (Exception ex) { // Some tag missing from spider.def
			logger.error("Some tag missing from spider.def", ex);
			infB.close(0);
			Vm.showWait(false);
			return;
		}
		logger.debug("Found " + cachesToLoad.size() + " new caches");
		logger.debug("Found " + cachesToUpdate.size() + " caches for update");
		if (!infB.isClosed)
			infB.setInfo(MyLocale.getMsg(5511, "Found ") + cachesToLoad.size()
					+ MyLocale.getMsg(5512, " caches"));

		// =======
		// Now ready to spider each cache in the list
		// =======
		boolean loadAllLogs = (pref.maxLogsToSpider > 5) || spiderAllFinds;

		int spiderErrors = 0;

		if (cachesToUpdate.size() > 0) {
			switch (pref.spiderUpdates) {
			case Preferences.NO:
				cachesToUpdate.clear();
				break;
			case Preferences.ASK:
				MessageBox mBox = new MessageBox(
						MyLocale.getMsg(5517, "Spider Updates?"),
						cachesToUpdate.size()
								+ MyLocale
										.getMsg(5518,
												" caches in database need an update. Update now?"),
						FormBase.IDYES | FormBase.IDNO);
				if (mBox.execute() != FormBase.IDOK) {
					cachesToUpdate.clear();
				}
				break;
			}
		}

		int totalCachesToLoad = cachesToLoad.size() + cachesToUpdate.size();

		for (int i = 0; i < cachesToLoad.size(); i++) {
			if (infB.isClosed)
				break;

			wpt = (String) cachesToLoad.get(i);
			// Get only caches not already available in the DB
			if (cacheDB.getIndex(wpt) == -1) {
				infB.setInfo(MyLocale.getMsg(5513, "Loading: ") + wpt + " ("
						+ (i + 1) + " / " + totalCachesToLoad + ")");
				holder = new CacheHolder();
				holder.setWayPoint(wpt);
				int test = getCacheByWaypointName(holder, false, getImages,
						getTBs, doNotgetFound, loadAllLogs);
				if (test == SPIDER_CANCEL) {
					infB.close(0);
					break;
				} else if (test == SPIDER_ERROR) {
					spiderErrors++;
				} else if (test == SPIDER_OK) {
					if (!holder.isFound() || !doNotgetFound) {
						cacheDB.add(holder);
						holder.save();
					}
				} // For test==SPIDER_IGNORE_PREMIUM: Nothing to do
			}
		}

		if (!infB.isClosed) {
			int j = 1;
			for (Enumeration e = cachesToUpdate.elements(); e.hasMoreElements(); j++) {
				ch = (CacheHolder) e.nextElement();
				infB.setInfo(MyLocale.getMsg(5513, "Loading: ")
						+ ch.getWayPoint() + " (" + (cachesToLoad.size() + j)
						+ " / " + totalCachesToLoad + ")");
				infB.redisplay();

				int test = spiderSingle(cacheDB.getIndex(ch), infB, false,
						loadAllLogs);
				if (test == SPIDER_CANCEL) {
					break;
				} else if (test == SPIDER_ERROR) {
					spiderErrors++;
					logger.error("SpiderGC: could not spider "
							+ ch.getWayPoint());
				} else {
					// profile.hasUnsavedChanges=true;
				}
			}
		}

		infB.close(0);
		Vm.showWait(false);
		if (spiderErrors > 0) {
			new MessageBox(MyLocale.getMsg(5500, "Error"), spiderErrors
					+ MyLocale.getMsg(5516,
							" cache descriptions%0acould not be loaded."),
					FormBase.DEFOKB).execute();
		}
		if (maxNumberAbort) {
			new MessageBox(
					MyLocale.getMsg(5519, "Information"),
					MyLocale
							.getMsg(
									5520,
									"Only the given maximum of caches were loaded.%0aRepeat spidering later to load more caches.%0aNo already existing caches were updated."),
					FormBase.DEFOKB).execute();
		}
		Global.getProfile().restoreFilter();
		Global.getProfile().saveIndex(Global.getPref(), true);
	}

	/**
	 * Read a complete cachepage from geocaching.com including all logs. This is
	 * used both when updating already existing caches (via spiderSingle) and
	 * when spidering around a centre. It is also used when reading a GPX file
	 * and fetching the images.
	 * 
	 * This is the workhorse function of the spider.
	 * 
	 * @param CacheHolderDetail
	 *            chD The element wayPoint must be set to the name of a waypoint
	 * @param boolean isUpdate True if an existing cache is being updated, false
	 *        if it is a new cache
	 * @param boolean fetchImages True if the pictures are to be fetched
	 * @param boolean fetchTBs True if the TBs are to be fetched
	 * @param boolean doNotGetFound True if the cache is not to be spidered if
	 *        it has already been found
	 * @param boolean fetchAllLogs True if all logs are to be fetched (by adding
	 *        option '&logs=y' to command line). This is normally false when
	 *        spidering from GPXImport as the logs are part of the GPX file, and
	 *        true otherwise
	 * @return -1 if the infoBox was closed (cancel spidering), 0 if there was
	 *         an error (continue with next cache), 1 if everything ok
	 */
	private int getCacheByWaypointName(CacheHolder ch, boolean isUpdate,
			boolean fetchImages, boolean fetchTBs, boolean doNotGetFound,
			boolean fetchAllLogs) {
		int ret = SPIDER_OK; // initialize value;
		while (true) {
			String completeWebPage;
			int spiderTrys = 0;
			int MAX_SPIDER_TRYS = 3;
			while (spiderTrys++ < MAX_SPIDER_TRYS) {
				ret = SPIDER_OK; // initialize value;
				try {
					String doc = p.getProp("getPageByName") + ch.getWayPoint()
							+ (fetchAllLogs ? p.getProp("fetchAllLogs") : "");
					logger.debug("Fetching: " + ch.getWayPoint());
					completeWebPage = fetch(doc);
					if (completeWebPage.equals("")) {
						logger.error("Could not fetch " + ch.getWayPoint());
						if (!infB.isClosed) {
							continue;
						} else {
							ch.setIncomplete(true);
							return SPIDER_CANCEL;
						}
					}
				} catch (Exception ex) {
					logger.error("Could not fetch " + ch.getWayPoint(), ex);
					if (!infB.isClosed) {
						continue;
					} else {
						ch.setIncomplete(true);
						return SPIDER_CANCEL;
					}
				}
				// Only analyse the cache data and fetch pictures if user has
				// not closed the progress window
				if (!infB.isClosed) {
					try {
						ch.initStates(!isUpdate);

						// first check if coordinates are available to prevent
						// deleting existing coorinates
						String latLon = getLatLon(completeWebPage);
						if (latLon.equals("???")) {
							if (completeWebPage.indexOf(p
									.getProp("premiumCachepage")) > 0) {
								// Premium cache spidered by non premium member
								logger.debug("Ignoring premium member cache: "
										+ ch.getWayPoint());
								spiderTrys = MAX_SPIDER_TRYS;
								ret = SPIDER_IGNORE_PREMIUM;
								continue;
							} else {
								logger
										.error(">>>> Failed to spider Cache. Retry.");
								ret = SPIDER_ERROR;
								continue; // Restart the spider
							}
						}

						ch.setHTML(true);
						ch.setAvailable(true);
						ch.setArchived(false);
						ch.setIncomplete(true);
						// Save size of logs to be able to check whether any new
						// logs were added
						// int logsz = chD.CacheLogs.size();
						// chD.CacheLogs.clear();
						ch.getAddiWpts().clear();
						ch.getFreshDetails().getImages().clear();

						if (completeWebPage.indexOf(p
								.getProp("cacheUnavailable")) >= 0)
							ch.setAvailable(false);
						if (completeWebPage.indexOf(p.getProp("cacheArchived")) >= 0)
							ch.setArchived(true);
						// ==========
						// General Cache Data
						// ==========
						ch.setPos(CWPointFactory.getInstance().fromString(latLon));
						logger.debug("LatLon: " + ch.getLatLon());

						logger.debug("Trying description");
						ch.getFreshDetails().setLongDescription(
								getLongDesc(completeWebPage));
						logger.debug("Got description");

						logger.debug("Getting cache name");
						ch.setCacheName(SafeXML
								.cleanback(getName(completeWebPage)));
						logger.debug("Name: " + ch.getCacheName());

						logger.debug("Trying location (country/state)");
						String location = getLocation(completeWebPage);
						if (location.length() != 0) {
							int countryStart = location.indexOf(",");
							if (countryStart > -1) {
								ch.getFreshDetails().setCountry(
										SafeXML.cleanback(location.substring(
												countryStart + 1).trim()));
								ch.getFreshDetails().setState(
										SafeXML.cleanback(location.substring(0,
												countryStart).trim()));
							} else {
								ch.getFreshDetails()
										.setCountry(location.trim());
								ch.getFreshDetails().setState("");
							}
							logger.debug("Got location (country/state)");
						} else {
							ch.getFreshDetails().setCountry("");
							ch.getFreshDetails().setState("");
							logger.debug("No location (country/state) found");
						}

						logger.debug("Trying owner");
						ch.setCacheOwner(SafeXML.cleanback(
								getOwner(completeWebPage)).trim());
						if (pref.isMyAlias(ch.getCacheOwner())) {
							ch.setOwned(true);
						}
						logger.debug("Owner: " + ch.getCacheOwner()
								+ "; is_owned = " + ch.isOwned());

						logger.debug("Trying date hidden");
						ch.setDateHidden(DateFormat
								.MDY2YMD(getDateHidden(completeWebPage)));
						logger.debug("Hidden: " + ch.getDateHidden());

						logger.debug("Trying hints");
						ch.getFreshDetails()
								.setHints(getHints(completeWebPage));
						logger.debug("Hints: "
								+ ch.getFreshDetails().getHints());

						logger.debug("Trying size");
						ch
								.setCacheSize(CacheSize
										.fromNormalStringRepresentation(getSize(completeWebPage)));
						logger.debug("Size: " + ch.getCacheSize());

						logger.debug("Trying difficulty");
						ch.setDifficulty(Difficulty
								.fromString(getDifficulty(completeWebPage)));
						logger.debug("Hard: " + ch.getDifficulty());

						logger.debug("Trying terrain");
						ch.setTerrain(Terrain
								.fromString(getTerrain(completeWebPage)));
						logger.debug("Terr: " + ch.getTerrain());

						logger.debug("Trying cache type");
						ch.setType(getType(completeWebPage));
						logger.debug("Type: " + ch.getType());

						// ==========
						// Logs
						// ==========
						logger.debug("Trying logs");
						ch.getFreshDetails().addCacheLogs(
								getLogs(completeWebPage, ch.getFreshDetails()));
						logger.debug("Found logs");

						// If the switch is set to not store found caches and we
						// found the cache => return
						if (ch.isFound() && doNotGetFound) {
							if (infB.isClosed) {
								return SPIDER_CANCEL;
							} else {
								return SPIDER_OK;
							}
						}

						// ==========
						// Bugs
						// ==========
						// As there may be several bugs, we check whether the
						// user has aborted
						if (!infB.isClosed && fetchTBs)
							getBugs(ch.getFreshDetails(), completeWebPage);
						ch.setHasBugs(ch.getFreshDetails().getTravelbugs()
								.size() > 0);

						// ==========
						// Images
						// ==========
						if (fetchImages) {
							logger.debug("Trying images");
							getImages(completeWebPage, ch.getFreshDetails());
							logger.debug("Got images");
						}
						// ==========
						// Addi waypoints
						// ==========

						logger.debug("Getting additional waypoints");
						getAddWaypoints(completeWebPage, ch.getWayPoint(), ch
								.isFound());
						logger.debug("Got additional waypoints");

						// ==========
						// Attributes
						// ==========
						logger.debug("Getting attributes");
						getAttributes(completeWebPage, ch.getFreshDetails());
						logger.debug("Got attributes");
						// if (ch.is_new()) ch.setUpdated(false);
						// ==========
						// Last sync date
						// ==========
						ch.setLastSync((new Time()).format("yyyyMMddHHmmss"));

						ch.setIncomplete(false);
						break;
					} catch (Exception ex) {
						logger
								.error("Error reading cache: "
										+ ch.getWayPoint());
						logger.error("Exception in getCacheByWaypointName: ",
								ex);
					}
				} else {
					break;
				}
			} // spiderTrys
			if ((spiderTrys >= MAX_SPIDER_TRYS) && (ret == SPIDER_OK)) {
				logger
						.error(">>> Failed to spider cache. Number of retrys exhausted.");
				int decision = (new MessageBox(
						MyLocale.getMsg(5500, "Error"),
						MyLocale
								.getMsg(5515,
										"Failed to load cache.%0aPleas check your internet connection.%0aRetry?"),
						FormBase.DEFOKB | FormBase.NOB | FormBase.CANCELB))
						.execute();
				if (decision == FormBase.IDOK) {
					continue;
				} else if (decision == FormBase.IDNO) {
					ret = SPIDER_ERROR;
				} else {
					ret = SPIDER_CANCEL;
				}
			}
			break;
		}// while(true)
		if (infB.isClosed) {// If the infoBox was closed before getting here, we
			// return -1
			return SPIDER_CANCEL;
		}
		return ret;
	} // getCacheByWaypointName

	/**
	 * Get the Distance to the centre
	 * 
	 * @param doc
	 *            A previously fetched cachepage
	 * @return Distance
	 */
	private double getDist(String doc) {
		inRex = new Regex(p.getProp("distRex"));
		inRex.search(doc);
		if (doc.indexOf("Here") >= 0)
			return (0);
		if (!inRex.didMatch())
			return 0;
		if (MyLocale.getDigSeparator().equals(","))
			return Convert.toDouble(inRex.stringMatched(1).replace('.', ','));
		return Convert.toDouble(inRex.stringMatched(1));
	}

	/**
	 * Get the waypoint name
	 * 
	 * @param doc
	 *            A previously fetched cachepage
	 * @return Name of waypoint to add to list
	 */
	private String getWP(String doc) {
		inRex = new Regex(p.getProp("waypointRex"));
		inRex.search(doc);
		if (!inRex.didMatch())
			return "???";
		return "GC" + inRex.stringMatched(1);
	}

	/**
	 * Get the coordinates of the cache
	 * 
	 * @param doc
	 *            A previously fetched cachepage
	 * @return Cache coordinates
	 */
	private String getLatLon(String doc) {
		inRex = new Regex(p.getProp("latLonRex"));
		inRex.search(doc);
		if (!inRex.didMatch())
			return "???";
		return inRex.stringMatched(1);
	}

	/**
	 * Get the long description
	 * 
	 * @param doc
	 *            A previously fetched cachepage
	 * @return the long description
	 */
	private String getLongDesc(String doc) {
		String res = "";
		inRex = new Regex(p.getProp("shortDescRex"));
		Regex rex2 = new Regex(p.getProp("longDescRex"));
		inRex.search(doc);
		rex2.search(doc);
		res = ((inRex.stringMatched(1) == null) ? "" : inRex.stringMatched(1))
				+ "<br>";
		res += rex2.stringMatched(1);
		return res; // SafeXML.cleanback(res);
	}

	/**
	 * Get the cache location (country and state)
	 * 
	 * @param doc
	 *            A previously fetched cachepage
	 * @return the location (country and state) of the cache
	 */
	private String getLocation(String doc) {
		inRex = new Regex(p.getProp("cacheLocationRex"));
		inRex.search(doc);
		if (!inRex.didMatch())
			return "";

		return inRex.stringMatched(1);
	}

	/**
	 * Get the cache name
	 * 
	 * @param doc
	 *            A previously fetched cachepage
	 * @return the name of the cache
	 */
	private String getName(String doc) {
		inRex = new Regex(p.getProp("cacheNameRex"));
		inRex.search(doc);
		if (!inRex.didMatch())
			return "???";
		return inRex.stringMatched(1);
	}

	/**
	 * Get the cache owner
	 * 
	 * @param doc
	 *            A previously fetched cachepage
	 * @return the cache owner
	 */
	private String getOwner(String doc) {
		inRex = new Regex(p.getProp("cacheOwnerRex"));
		inRex.search(doc);
		if (!inRex.didMatch())
			return "???";
		return inRex.stringMatched(1);
	}

	/**
	 * Get the date when the cache was hidden
	 * 
	 * @param doc
	 *            A previously fetched cachepage
	 * @return Hidden date
	 */
	private String getDateHidden(String doc) {
		inRex = new Regex(p.getProp("dateHiddenRex"));
		inRex.search(doc);
		if (!inRex.didMatch())
			return "???";
		return inRex.stringMatched(1);
	}

	/**
	 * Get the hints
	 * 
	 * @param doc
	 *            A previously fetched cachepage
	 * @return Cachehints
	 */
	private String getHints(String doc) {
		inRex = new Regex(p.getProp("hintsRex"));
		inRex.search(doc);
		if (!inRex.didMatch())
			return "";
		return inRex.stringMatched(1);
	}

	/**
	 * Get the cache size
	 * 
	 * @param doc
	 *            A previously fetched cachepage
	 * @return Cache size
	 */
	private String getSize(String doc) {
		inRex = new Regex(p.getProp("sizeRex"));
		inRex.search(doc);
		if (inRex.didMatch())
			return inRex.stringMatched(1);
		else
			return "None";
	}

	/**
	 * Get the Difficulty
	 * 
	 * @param doc
	 *            A previously fetched cachepage
	 * @return The cache difficulty
	 */
	private String getDifficulty(String doc) {
		inRex = new Regex(p.getProp("difficultyRex"));
		inRex.search(doc);
		if (inRex.didMatch())
			return inRex.stringMatched(1);
		else
			return "";
	}

	/**
	 * Get the terrain rating
	 * 
	 * @param doc
	 *            A previously fetched cachepage
	 * @return Terrain rating
	 */
	private String getTerrain(String doc) {
		inRex = new Regex(p.getProp("terrainRex"));
		inRex.search(doc);
		if (inRex.didMatch())
			return inRex.stringMatched(1);
		else
			return "";
	}

	/**
	 * Get the waypoint type
	 * 
	 * @param doc
	 *            A previously fetched cachepage
	 * @return the waypoint type (Tradi, Multi, etc.)
	 */
	private CacheType getType(String doc) throws Exception {
		inRex = new Regex(p.getProp("cacheTypeRex"));
		inRex.search(doc);
		if (inRex.didMatch())
			return CacheType.fromGcWebsiteId(inRex.stringMatched(1));
		else
			return CacheType.CUSTOM;
	}

	/**
	 * Get the logs
	 * 
	 * @param doc
	 *            A previously fetched cachepage
	 * @param chD
	 *            Cache Details
	 * @return A HTML string containing the logs
	 */
	private LogList getLogs(String doc, CacheHolderDetail chD) {
		LogType type;
		String name = "";
		String logText = "";
		String logId = "";
		LogList reslts = new LogList();
		Regex blockRex = new Regex(p.getProp("blockRex"));
		blockRex.search(doc);
		doc = blockRex.stringMatched(1);
		String singleLog = "";
		Extractor exSingleLog = new Extractor(doc, p
				.getProp("singleLogExStart"), p.getProp("singleLogExEnd"), 0,
				false); // maybe here is some change neccessary because findnext
		// now gives the whole endstring back???
		singleLog = exSingleLog.findNext();
		Extractor exIcon = new Extractor(singleLog, p.getProp("iconExStart"), p
				.getProp("iconExEnd"), 0, true);
		Extractor exNameTemp = new Extractor(singleLog, p
				.getProp("nameTempExStart"), p.getProp("nameTempExEnd"), 0,
				true);
		String nameTemp = "";
		nameTemp = exNameTemp.findNext();
		Extractor exName = new Extractor(nameTemp, p.getProp("nameExStart"), p
				.getProp("nameExEnd"), 0, true);
		Extractor exDate = new Extractor(singleLog, p.getProp("dateExStart"), p
				.getProp("dateExEnd"), 0, true);
		Extractor exLog = new Extractor(singleLog, p.getProp("logExStart"), p
				.getProp("logExEnd"), 0, true);
		Extractor exLogId = new Extractor(singleLog, p.getProp("logIdExStart"),
				p.getProp("logIdExEnd"), 0, true);
		// Vm.debug("Log Block: " + singleLog);
		int nLogs = 0;
		while (exSingleLog.endOfSearch() == false) {
			nLogs++;
			// Vm.debug("--------------------------------------------");
			// Vm.debug("Log Block: " + singleLog);
			// Vm.debug("Icon: "+exIcon.findNext());
			// Vm.debug(exName.findNext());
			// Vm.debug(exDate.findNext());
			// Vm.debug(exLog.findNext());
			// Vm.debug("--------------------------------------------");

			type = LogType.getLogTypeFromIconString(exIcon.findNext());
			name = exName.findNext();
			logText = exLog.findNext();
			logId = exLogId.findNext();
			String d = DateFormat.logdate2YMD(exDate.findNext());
			if ((type == LogType.FOUND || type == LogType.PHOTO_TAKEN || type == LogType.ATTENDED)
					&& pref.isMyAliasXML(name)) {
				chD.getParent().setFound(true);
				chD.getParent().setCacheStatus(d);
				chD.setOwnLogId(logId);
				chD.setOwnLog(LogFactory.getInstance().createLog(type, d, name,
						logText));
			}
			if (nLogs <= pref.maxLogsToSpider) {
				reslts.add(LogFactory.getInstance().createLog(type, d, name,
						logText));
			}
			singleLog = exSingleLog.findNext();
			exIcon.setSource(singleLog);
			exNameTemp.setSource(singleLog);
			nameTemp = exNameTemp.findNext();
			exName.setSource(nameTemp);
			exDate.setSource(singleLog);
			exLog.setSource(singleLog);
			exLogId.setSource(singleLog);
			// We cannot simply stop if we have reached MAXLOGS just in case we
			// are waiting for
			// a log by our alias that happened earlier.
			if (nLogs >= pref.maxLogsToSpider && chD.getParent().isFound()
					&& (chD.getOwnLogId().length() != 0)
					&& (chD.getOwnLog() != null)
					&& !(chD.getOwnLog().getDate().equals("1900-01-01")))
				break;
		}
		if (nLogs > pref.maxLogsToSpider) {

			reslts.add(LogFactory.getInstance().createMaxLog());

			logger.debug("Too many logs. MAXLOGS reached ("
					+ pref.maxLogsToSpider + ")");
		} else
			logger.debug(nLogs + " logs found");
		return reslts;
	}

	/**
	 * Read the travelbug names from a previously fetched Cache page and then
	 * read the travelbug purpose for each travelbug
	 * 
	 * @param doc
	 *            The previously fetched cachepage
	 * @return A HTML formatted string with bug names and there purpose
	 */
	private void getBugs(CacheHolderDetail chD, String doc) {
		Extractor exBlock = new Extractor(doc, p.getProp("blockExStart"), p
				.getProp("blockExEnd"), 0, Extractor.EXCLUDESTARTEND);
		String bugBlock = exBlock.findNext();
		// Vm.debug("Bugblock: "+bugBlock);
		Extractor exBug = new Extractor(bugBlock, p.getProp("bugExStart"), p
				.getProp("bugExEnd"), 0, Extractor.EXCLUDESTARTEND);
		String link, bug, linkPlusBug, bugDetails;
		String oldInfoBox = infB.getInfo();
		chD.getTravelbugs().clear();
		while (exBug.endOfSearch() == false) {
			if (infB.isClosed)
				break; // Allow user to cancel by closing progress form
			linkPlusBug = exBug.findNext();
			int idx = linkPlusBug.indexOf("'>");
			if (idx < 0)
				break; // No link/bug pair found
			link = linkPlusBug.substring(0, idx);
			bug = linkPlusBug.substring(idx + 2);
			if (bug.length() > 0) { // Found a bug, get its details
				Travelbug tb = new Travelbug(bug);
				try {
					infB.setInfo(oldInfoBox
							+ MyLocale.getMsg(5514, "\nGetting bug: ")
							+ SafeXML.cleanback(bug));
					logger.debug("Fetching bug details: " + bug);
					bugDetails = fetch(link);
					Extractor exDetails = new Extractor(bugDetails, p
							.getProp("bugDetailsStart"), p
							.getProp("bugDetailsEnd"), 0,
							Extractor.EXCLUDESTARTEND);
					tb.setMission(exDetails.findNext());
					Extractor exGuid = new Extractor(bugDetails,
							"details.aspx?guid=", "\" id=\"aspnetForm", 0,
							Extractor.EXCLUDESTARTEND); // TODO Replace with
					// spider.def see also
					// further down
					tb.setGuid(exGuid.findNext());
					chD.getTravelbugs().add(tb);
				} catch (Exception ex) {
					logger.error("Could not fetch bug details", ex);
				}
			}
			// Vm.debug("B: " + bug);
			// Vm.debug("End? " + exBug.endOfSearch());
		}
		infB.setInfo(oldInfoBox);
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
		int imgCounter = 0;
		int spiderCounter = 0;
		String fileName, imgName, imgType, imgUrl, imgComment;
		Vector spideredUrls = new Vector(15);
		ImageInfo imageInfo = null;
		Extractor exImgBlock, exImgComment;
		int idxUrl; // Index of already spidered Url in list of spideredUrls
		CacheImages lastImages = null;

		// First: Get current image object of waypoint before spidering images.
		CacheHolder oldCh = Global.getProfile().cacheDB.get(chD.getParent()
				.getWayPoint());
		if (oldCh != null) {
			lastImages = oldCh.getFreshDetails().getImages();
		}

		// ========
		// In the long description
		// ========
		String longDesc = "";
		try {
			if (chD.getParent().getWayPoint().startsWith("TC"))
				longDesc = doc;
			else
				longDesc = getLongDesc(doc);
			longDesc = longDesc.replace("<img", "<IMG");
			longDesc = longDesc.replace("src=", "SRC=");
			longDesc = longDesc.replace("'", "\"");
			exImgBlock = new Extractor(longDesc, p.getProp("imgBlockExStart"),
					p.getProp("imgBlockExEnd"), 0, false);
		} catch (Exception ex) {// Missing property in spider.def
			logger.error("Missing property in spider.def", ex);
			return;
		}
		// Vm.debug("In getImages: Have longDesc" + longDesc);
		String tst;
		tst = exImgBlock.findNext();
		// Vm.debug("Test: \n" + tst);
		Extractor exImgSrc = new Extractor(tst, "http://", "\"", 0, true);
		while (exImgBlock.endOfSearch() == false) {
			imgUrl = exImgSrc.findNext();
			// Vm.debug("Img Url: " +imgUrl);
			if (imgUrl.length() > 0) {
				// Optimize: img.groundspeak.com -> img.geocaching.com (for
				// better caching purposes)
				imgUrl = CacheImages.optimizeLink("http://" + imgUrl);
				try {
					imgType = (imgUrl.substring(imgUrl.lastIndexOf("."))
							.toLowerCase() + "    ").substring(0, 4).trim();
					// imgType is now max 4 chars, starting with .
					if (imgType.startsWith(".png")
							|| imgType.startsWith(".jpg")
							|| imgType.startsWith(".gif")) {
						// Check whether image was already spidered for this
						// cache
						idxUrl = spideredUrls.find(imgUrl);
						imgName = chD.getParent().getWayPoint() + "_"
								+ Convert.toString(imgCounter);
						imageInfo = null;
						if (idxUrl < 0) { // New image
							fileName = chD.getParent().getWayPoint() + "_"
									+ Convert.toString(spiderCounter);
							if (lastImages != null) {
								imageInfo = lastImages.needsSpidering(imgUrl,
										fileName + imgType);
							}
							if (imageInfo == null) {
								imageInfo = new ImageInfo();
								logger.debug("Loading image: " + imgUrl
										+ " as " + fileName + imgType);
								spiderImage(imgUrl, fileName + imgType);
								imageInfo.setFilename(fileName + imgType);
								imageInfo.setURL(imgUrl);
							} else {
								logger.debug("Already exising image: " + imgUrl
										+ " as " + imageInfo.getFilename());
							}
							spideredUrls.add(imgUrl);
							spiderCounter++;
						} else { // Image already spidered as
							// wayPoint_'idxUrl'
							fileName = chD.getParent().getWayPoint() + "_"
									+ Convert.toString(idxUrl);
							logger.debug("Already loaded image: " + imgUrl
									+ " as " + fileName + imgType);
							imageInfo = new ImageInfo();
							imageInfo.setFilename(fileName + imgType);
							imageInfo.setURL(imgUrl);
						}
						imageInfo.setTitle(imgName);
						imageInfo.setComment(null);
						imgCounter++;
						chD.getImages().add(imageInfo);
					}
				} catch (IndexOutOfBoundsException e) {
					// Vm.debug("IndexOutOfBoundsException not in image
					// span"+e.toString()+"imgURL:"+imgUrl);
					logger.error("Problem loading image. imgURL:" + imgUrl, e);
				}
			}
			exImgSrc.setSource(exImgBlock.findNext());
		}
		// ========
		// In the image span
		// ========
		Extractor spanBlock, exImgName;
		try {
			spanBlock = new Extractor(doc, p.getProp("imgSpanExStart"), p
					.getProp("imgSpanExEnd"), 0, true);
			tst = spanBlock.findNext();
			exImgName = new Extractor(tst, p.getProp("imgNameExStart"), p
					.getProp("imgNameExEnd"), 0, true);
			exImgSrc = new Extractor(tst, p.getProp("imgSrcExStart"), p
					.getProp("imgSrcExEnd"), 0, true);
			exImgComment = new Extractor(tst, p.getProp("imgCommentExStart"), p
					.getProp("imgCommentExEnd"), 0, true);
		} catch (Exception ex) { // Missing property in spider.def
			logger.error("Missing property in spider.def", ex);
			return;
		}
		while (exImgSrc.endOfSearch() == false) {
			imgUrl = exImgSrc.findNext();
			imgComment = exImgComment.findNext();
			// Vm.debug("Img Url: " +imgUrl);
			if (imgUrl.length() > 0) {
				imgUrl = "http://" + imgUrl;
				try {
					imgType = (imgUrl.substring(imgUrl.lastIndexOf("."))
							.toLowerCase() + "    ").substring(0, 4).trim();
					// imgType is now max 4 chars, starting with .
					if (imgType.startsWith(".png")
							|| imgType.startsWith(".jpg")
							|| imgType.startsWith(".gif")) {
						// Check whether image was already spidered for this
						// cache
						idxUrl = spideredUrls.find(imgUrl);
						imgName = chD.getParent().getWayPoint() + "_"
								+ Convert.toString(imgCounter);
						imageInfo = null;
						if (idxUrl < 0) { // New image
							fileName = chD.getParent().getWayPoint() + "_"
									+ Convert.toString(spiderCounter);
							if (lastImages != null) {
								imageInfo = lastImages.needsSpidering(imgUrl,
										fileName + imgType);
							}
							if (imageInfo == null) {
								imageInfo = new ImageInfo();
								logger.debug("Loading image: " + imgUrl
										+ " as " + fileName + imgType);
								spiderImage(imgUrl, fileName + imgType);
								imageInfo.setFilename(fileName + imgType);
								imageInfo.setURL(imgUrl);
							} else {
								logger.debug("Already exising image: " + imgUrl
										+ " as " + imageInfo.getFilename());
							}
							spideredUrls.add(imgUrl);
							spiderCounter++;
						} else { // Image already spidered as
							// wayPoint_'idxUrl'
							fileName = chD.getParent().getWayPoint() + "_"
									+ Convert.toString(idxUrl);
							logger.debug("Already loaded image: " + imgUrl
									+ " as " + fileName + imgType);
							imageInfo = new ImageInfo();
							imageInfo.setFilename(fileName + imgType);
							imageInfo.setURL(imgUrl);
						}
						imageInfo.setTitle(exImgName.findNext());
						while (imgComment.startsWith("<br />"))
							imgComment = imgComment.substring(6);
						while (imgComment.endsWith("<br />"))
							imgComment = imgComment.substring(0, imgComment
									.length() - 6);
						imageInfo.setComment(imgComment);
						chD.getImages().add(imageInfo);
					}
				} catch (IndexOutOfBoundsException e) {
					logger.error(
							"IndexOutOfBoundsException in image span. imgURL:"
									+ imgUrl, e);
				}
			}
		}
		// ========
		// Final sweep to check for images in hrefs
		// ========
		Extractor exFinal = new Extractor(longDesc, "http://", "\"", 0, true);
		while (exFinal.endOfSearch() == false) {
			imgUrl = exFinal.findNext();
			if (imgUrl.length() > 0) {
				// Optimize: img.groundspeak.com -> img.geocaching.com (for
				// better caching purposes)
				imgUrl = CacheImages.optimizeLink("http://" + imgUrl);
				try {
					imgType = (imgUrl.substring(imgUrl.lastIndexOf("."))
							.toLowerCase() + "    ").substring(0, 4).trim();
					// imgType is now max 4 chars, starting with . Delete
					// characters in URL after the image extension
					imgUrl = imgUrl.substring(0, imgUrl.lastIndexOf(".")
							+ imgType.length());
					if (imgType.startsWith(".jpg")
							|| imgType.startsWith(".bmp")
							|| imgType.startsWith(".png")
							|| imgType.startsWith(".gif")) {
						// Check whether image was already spidered for this
						// cache
						idxUrl = spideredUrls.find(imgUrl);
						if (idxUrl < 0) { // New image
							imgName = chD.getParent().getWayPoint() + "_"
									+ Convert.toString(imgCounter);
							fileName = chD.getParent().getWayPoint() + "_"
									+ Convert.toString(spiderCounter);
							if (lastImages != null) {
								imageInfo = lastImages.needsSpidering(imgUrl,
										fileName + imgType);
							}
							if (imageInfo == null) {
								imageInfo = new ImageInfo();
								logger.debug("Loading image: " + imgUrl
										+ " as " + fileName + imgType);
								spiderImage(imgUrl, fileName + imgType);
								imageInfo.setFilename(fileName + imgType);
								imageInfo.setURL(imgUrl);
							} else {
								logger.debug("Already exising image: " + imgUrl
										+ " as " + imageInfo.getFilename());
							}
							spideredUrls.add(imgUrl);
							spiderCounter++;
							imageInfo.setTitle(imgName);
							imgCounter++;
							chD.getImages().add(imageInfo);
						}
					}
				} catch (IndexOutOfBoundsException e) {
					logger.error("Problem loading image. imgURL:" + imgUrl);
				}
			}
		}
	}

	/**
	 * Read an image from the server
	 * 
	 * @param imgUrl
	 *            The Url of the image
	 * @param target
	 *            The bytes of the image
	 */
	private void spiderImage(String imgUrl, String target) {
		// TODO implement a fetch(URL, filename) in HttpConnection and use that
		// one
		HttpConnection connImg;
		Socket sockImg;
		// InputStream is;
		java.io.FileOutputStream fos;
		// int bytes_read;
		// byte[] buffer = new byte[9000];
		ByteArray daten;
		java.io.File datei = new java.io.File(profile.getDataDir(), target);
		connImg = new HttpConnection(imgUrl);
		if (imgUrl.indexOf('%') >= 0)
			connImg.documentIsEncoded = true;
		connImg.setRequestorProperty("Connection", "close");
		// connImg.setRequestorProperty("User-Agent","Mozilla/5.0 (Windows; U;
		// Windows NT 5.1; en-US; rv:1.8.1.12) Gecko/20080201
		// Firefox/2.0.0.12");
		// connImg.setRequestorProperty("Accept","text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
		try {
			logger.debug("Trying to fetch image from: " + imgUrl);
			String redirect = null;
			do {
				sockImg = connImg.connect();
				redirect = connImg.getRedirectTo();
				if (redirect != null) {
					connImg = connImg.getRedirectedConnection(redirect);
					logger.debug("Redirect to " + redirect);
				}
			} while (redirect != null); // TODO this can end up in an endless
			// loop if trying to load from a
			// malicous site
			daten = connImg.readData(sockImg);
			fos = new java.io.FileOutputStream(datei);
			fos.write(daten.toBytes());
			fos.close();
			sockImg.close();
		} catch (UnknownHostException e) {
			logger.error("Host not there...", e);
		} catch (IOException ioex) {
			logger.error("File not found!", ioex);
		} catch (Exception ex) {
			logger.error("Some other problem while fetching image", ex);
		} finally {
			// Continue with the spider
		}
	}

	/**
	 * Read all additional waypoints from a previously fetched cachepage.
	 * 
	 * @param doc
	 *            The previously fetched cachepage
	 * @param wayPoint
	 *            The name of the cache
	 * @param is_found
	 *            Found status of the cached (is inherited by the additional
	 *            waypoints)
	 */
	private void getAddWaypoints(String doc, String wayPoint, boolean is_found) {
		Extractor exWayBlock = new Extractor(doc, p.getProp("wayBlockExStart"),
				p.getProp("wayBlockExEnd"), 0, false);
		String wayBlock = "";
		String rowBlock = "";
		wayBlock = exWayBlock.findNext();
		Regex nameRex = new Regex(p.getProp("nameRex"));
		Regex koordRex = new Regex(p.getProp("koordRex"));
		Regex descRex = new Regex(p.getProp("descRex"));
		Regex typeRex = new Regex(p.getProp("typeRex"));
		int counter = 0;
		if (exWayBlock.endOfSearch() == false
				&& wayBlock.indexOf("No additional waypoints to display.") < 0) {
			Extractor exRowBlock = new Extractor(wayBlock, p
					.getProp("rowBlockExStart"), p.getProp("rowBlockExEnd"), 0,
					false);
			rowBlock = exRowBlock.findNext();
			rowBlock = exRowBlock.findNext();
			while (exRowBlock.endOfSearch() == false) {
				CacheHolder hd = null;
				Extractor exPrefix = new Extractor(rowBlock, p
						.getProp("prefixExStart"), p.getProp("prefixExEnd"), 0,
						true);
				String prefix = exPrefix.findNext();
				String adWayPoint;
				if (prefix.length() == 2)
					adWayPoint = prefix + wayPoint.substring(2);
				else
					adWayPoint = MyLocale.formatLong(counter, "00")
							+ wayPoint.substring(2);
				counter++;
				int idx = profile.getCacheIndex(adWayPoint);
				if (idx >= 0) {
					// Creating new CacheHolder, but accessing old cache.xml
					// file
					hd = new CacheHolder();
					hd.setWayPoint(adWayPoint);
					hd.getExistingDetails(); // Accessing Details reads file
					// if
					// not yet done
				} else {
					hd = new CacheHolder();
					hd.setWayPoint(adWayPoint);
				}
				hd.initStates(idx < 0);
				nameRex.search(rowBlock);
				koordRex.search(rowBlock);
				typeRex.search(rowBlock);
				hd.setCacheName(nameRex.stringMatched(1));
				if (koordRex.didMatch()) {
					hd.setPos(CWPointFactory.getInstance().fromHDMString(
							koordRex.stringMatched(1)));
				}
				if (typeRex.didMatch())
					hd.setType(CacheType.fromGcGpxString(typeRex
							.stringMatched(1)));
				rowBlock = exRowBlock.findNext();
				descRex.search(rowBlock);
				hd.getFreshDetails().setLongDescription(
						descRex.stringMatched(1));
				hd.setFound(is_found);
				hd.setCacheSize(CacheSize.NOT_CHOSEN);
				hd.setDifficulty(Difficulty.DIFFICULTY_UNSET);
				hd.setTerrain(Terrain.TERRAIN_UNSET);
				if (idx < 0) {
					cacheDB.add(hd);
					hd.save();
				} else {
					CacheHolder cx = cacheDB.get(idx);
					if (cx.isChecked() && // Only re-spider existing addi
							// waypoints that are ticked
							cx.isVisible()) { // and are visible (i.e. not
						// filtered)
						cx.initStates(false);
						cx.update(hd);
						cx.setIs_Checked(true);
						cx.save();
					}
				}
				rowBlock = exRowBlock.findNext();

			}
		}
	}

	public void getAttributes(String doc, CacheHolderDetail chD) {
		Extractor attBlock = new Extractor(doc, p.getProp("attBlockExStart"), p
				.getProp("attBlockExEnd"), 0, true);
		String atts = attBlock.findNext();
		Extractor attEx = new Extractor(atts, p.getProp("attExStart"), p
				.getProp("attExEnd"), 0, true);
		String attribute = attEx.findNext();
		chD.getAttributes().clear();
		while (attEx.endOfSearch() == false) {
			chD.getAttributes().add(attribute);
			attribute = attEx.findNext();
		}
		chD.getParent().setAttributesYes(chD.getAttributes().attributesYes);
		chD.getParent().setAttributesNo(chD.getAttributes().attributesNo);
	}

	/**
	 * Performs an initial fetch to a given address. In this case it will be a
	 * gc.com address. This method is used to obtain the result of a search for
	 * caches screen.
	 */
	public String fetch(String address) {
		CharArray c_data;
		try {
			HttpConnection conn;
			if (pref.myproxy.length() > 0 && pref.proxyActive) {
				logger.debug("[fetch]:Using proxy: " + pref.myproxy + " / "
						+ pref.myproxyport);
			}
			conn = new HttpConnection(address);
			conn
					.setRequestorProperty(
							"User-Agent",
							"Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.7.5) Gecko/20041107 Firefox/1.0");
			if (cookieSession.length() > 0) {
				conn.setRequestorProperty("Cookie", "ASP.NET_SessionId="
						+ cookieSession + "; userid=" + cookieID);
				logger.debug("[fetch]:Cookie Zeug: "
						+ "Cookie: ASP.NET_SessionId=" + cookieSession
						+ "; userid=" + cookieID);
			} else
				logger.debug("[fetch]:No Cookie found");
			conn.setRequestorProperty("Connection", "close");
			conn.documentIsEncoded = true;
			logger.debug("[fetch]:Connecting");
			Socket sock = conn.connect();
			logger.debug("[fetch]:Connect ok!");
			ByteArray daten = conn.readData(sock);
			logger.debug("[fetch]:Read data ok");
			JavaUtf8Codec codec = new JavaUtf8Codec();
			c_data = codec.decodeText(daten.data, 0, daten.length, true, null);
			sock.close();
			return getResponseHeaders(conn) + c_data.toString();
		} catch (IOException ioex) {
			logger.error("IOException in fetch", ioex);
		} finally {
			// continue
		}
		return "";
	}

	/**
	 * After a fetch to gc.com the next fetches have to use the post method.
	 * This method does exactly that. Actually this method is generic in the
	 * sense that it can be used to post to a URL using http post.
	 */
	private static String fetch_post(String address, String document,
			String path) {
		HttpConnection conn;
		try {
			conn = new HttpConnection(address);
			JavaUtf8Codec codec = new JavaUtf8Codec();
			conn.documentIsEncoded = true;
			conn
					.setRequestorProperty(
							"User-Agent",
							"Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.7.5) Gecko/20041107 Firefox/1.0");
			conn.setPostData(codec.encodeText(document.toCharArray(), 0,
					document.length(), true, null));
			conn.setRequestorProperty("Content-Type",
					"application/x-www-form-urlencoded");
			if (cookieSession.length() > 0) {
				conn.setRequestorProperty("Cookie", "ASP.NET_SessionId="
						+ cookieSession + "; userid=" + cookieID);
				logger.debug("[fetch]:Cookie Zeug: "
						+ "Cookie: ASP.NET_SessionId=" + cookieSession
						+ "; userid=" + cookieID);
			} else {
				logger.debug("[fetch]:No Cookie found");
			}
			conn.setRequestorProperty("Connection", "close");
			logger.debug("[fetch]:Connecting");
			Socket sock = conn.connect();
			logger.debug("[fetch]:Connect ok!");
			ByteArray daten = conn.readData(sock);
			logger.debug("[fetch]:Read data ok");
			CharArray c_data = codec.decodeText(daten.data, 0, daten.length,
					true, null);
			sock.close();
			return getResponseHeaders(conn) + c_data.toString();
		} catch (Exception e) {
			logger.error("Ignored Exception", e, true);
		}
		return "";
	}

	private static String getResponseHeaders(HttpConnection conn) {
		PropertyList pl = conn.documentProperties;
		if (pl != null) {
			StringBuilder sb = new StringBuilder(1000);
			boolean gotany = false;

			for (int i = 0; i < pl.size(); i++) {
				Property currProp = (Property) pl.get(i);
				if (currProp.value != null) {
					sb.append(currProp.name).append(": ")
							.append(currProp.value).append("\r\n");
					gotany = true;
				}
			}
			if (gotany)
				return sb.toString() + "\r\n";
		}
		return "";
	}

	final static String hex = ewe.util.TextEncoder.hex;

	private String encodeUTF8URL(byte[] what) {
		int max = what.length;
		char[] dest = new char[6 * max]; // Assume each char is a UTF char
		// and
		// encoded into 6 chars
		char d = 0;
		for (int i = 0; i < max; i++) {
			char c = (char) what[i];
			if (c <= ' ' || c == '+' || c == '&' || c == '%' || c == '='
					|| c == '|' || c == '{' || c == '}' || c > 0x7f) {
				dest[d++] = '%';
				dest[d++] = hex.charAt((c >> 4) & 0xf);
				dest[d++] = hex.charAt(c & 0xf);
			} else
				dest[d++] = c;
		}
		return new String(dest, 0, d);
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
		String bugList;
		try {
			// infB.setInfo(oldInfoBox+"\nGetting bug: "+bug);
			logger.debug("Fetching bugId: " + name);
			bugList = fetch(p.getProp("getBugByName")
					+ SafeXML.clean(name).replace(" ", "+"));
		} catch (Exception ex) {
			logger.error("Could not fetch bug list", ex);
			bugList = "";
		}
		try {
			if (bugList.equals("")
					|| bugList.indexOf(p.getProp("bugNotFound")) >= 0) {
				(new MessageBox(MyLocale.getMsg(5500, "Error"), MyLocale
						.getMsg(6020, "Travelbug not found."), FormBase.OKB))
						.execute();
				return "";
			}
			if (bugList.indexOf(p.getProp("bugTotalRecords")) < 0) {
				(new MessageBox(
						MyLocale.getMsg(5500, "Error"),
						MyLocale
								.getMsg(6021,
										"More than one travelbug found. Specify name more precisely."),
						FormBase.OKB)).execute();
				return "";
			}
			Extractor exGuid = new Extractor(bugList, p
					.getProp("bugGuidExStart"), p.getProp("bugGuidExEnd"), 0,
					Extractor.EXCLUDESTARTEND); // TODO Replace with spider.def
			return exGuid.findNext();
		} catch (Exception ex) {
			logger.error("", ex);
			return "";
		}
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
		String bugDetails;
		try {
			// infB.setInfo(oldInfoBox+"\nGetting bug: "+bug);
			logger.debug("Fetching bug detailsById: " + guid);
			if (guid.length() > 10)
				bugDetails = fetch(p.getProp("getBugByGuid") + guid);
			else
				bugDetails = fetch(p.getProp("getBugById") + guid);
		} catch (Exception ex) {
			logger.error("Could not fetch bug details", ex);
			bugDetails = "";
		}
		try {
			if (bugDetails.indexOf(p.getProp("bugNotFound")) >= 0) {
				(new MessageBox(MyLocale.getMsg(5500, "Error"), MyLocale
						.getMsg(6020, "Travelbug not found."), FormBase.OKB))
						.execute();
				return "";
			}
			Extractor exDetails = new Extractor(bugDetails, p
					.getProp("bugDetailsStart"), p.getProp("bugDetailsEnd"), 0,
					Extractor.EXCLUDESTARTEND);
			return exDetails.findNext();
		} catch (Exception ex) {
			logger.error("", ex);
			return "";
		}
	}

	/**
	 * Fetch a bug's mission and namefor a given tracking number
	 * 
	 * @param TB
	 *            the travelbug
	 * @return true if suceeded
	 */
	public boolean getBugMissionAndNameByTrackNr(Travelbug TB) {
		String bugDetails;
		String trackNr = TB.getTrackingNo();
		try {
			logger.debug("Fetching bug detailsByTrackNr: " + trackNr);
			bugDetails = fetch(p.getProp("getBugByTrackNr") + trackNr);
		} catch (Exception ex) {
			logger.error("Could not fetch bug details", ex);
			bugDetails = "";
		}
		try {
			if (bugDetails.indexOf(p.getProp("bugNotFound")) >= 0) {
				// (new MessageBox(MyLocale.getMsg(5500,"Error"),
				// MyLocale.getMsg(6020,"Travelbug not found."),
				// MessageBox.OKB)).execute();
				return false;
			}
			Extractor exDetails = new Extractor(bugDetails, p
					.getProp("bugDetailsStart"), p.getProp("bugDetailsEnd"), 0,
					Extractor.EXCLUDESTARTEND);
			TB.setMission(exDetails.findNext());
			Extractor exName = new Extractor(bugDetails, p
					.getProp("bugNameStart"), p.getProp("bugNameEnd"), 0,
					Extractor.EXCLUDESTARTEND);
			TB.setName(exName.findNext());
			return true;
		} catch (Exception ex) {
			logger.error("", ex);
			return false;
		}
	}

	class SpiderProperties extends Properties {
		SpiderProperties() {
			super();
			try {
				load(new FileInputStream(FileBase.getProgramDirectory()
						+ "/spider.def"));
			} catch (Exception ex) {
				logger.error("Failed to load spider.def", ex);
				(new MessageBox(MyLocale.getMsg(5500, "Error"), MyLocale
						.getMsg(5504, "Could not load 'spider.def'"),
						FormBase.OKB)).execute();
			}
		}

		/**
		 * Gets an entry in spider.def by its key (tag)
		 * 
		 * @param key
		 *            The key which is attributed to a specific entry
		 * @return The value for the key
		 * @throws Exception
		 *             When a key is requested which doesn't exist
		 */
		public String getProp(String key) {
			String s = super.getProperty(key);
			if (s == null) {
				(new MessageBox(MyLocale.getMsg(5500, "Error"), MyLocale
						.getMsg(5497, "Error missing tag in spider.def")
						+ ": " + key, FormBase.OKB)).execute();
				logger.debug("Missing tag in spider.def: " + key);
				throw new RuntimeException("Missing tag in spider.def: " + key);
			}
			return s;
		}

	}
}
