package CacheWolf.imp;

import CacheWolf.beans.CWPoint;
import CacheWolf.beans.CacheDB;
import CacheWolf.beans.CacheHolder;
import CacheWolf.beans.CacheType;
import CacheWolf.beans.ImageInfo;
import CacheWolf.beans.Preferences;
import CacheWolf.beans.Profile;
import CacheWolf.gui.InfoBox;
import CacheWolf.util.Common;
import CacheWolf.util.FileBugfix;
import CacheWolf.util.MyLocale;
import CacheWolf.util.SafeXML;
import CacheWolf.util.UrlFetcher;

import com.stevesoft.ewe_pat.Regex;

import de.cachehound.factory.LogFactory;
import de.cachehound.types.CacheSize;
import de.cachehound.types.Difficulty;
import de.cachehound.types.LogType;
import de.cachehound.types.Terrain;
import ewe.io.BufferedReader;
import ewe.io.File;
import ewe.io.FileOutputStream;
import ewe.io.IO;
import ewe.io.IOException;
import ewe.io.InputStreamReader;
import ewe.net.MalformedURLException;
import ewe.net.URL;
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
import ewe.util.zip.ZipEntry;
import ewe.util.zip.ZipException;
import ewe.util.zip.ZipFile;
import ewesoft.xml.MinML;
import ewesoft.xml.sax.AttributeList;

/**
 * Class to import Data from opencaching.de. It uses the lastmodified parameter
 * to identify new or changed caches. See here:
 * http://www.opencaching.com/phpBB2/viewtopic.php?t=281 (out-dated) See here:
 * http://www.opencaching.de/doc/xml/xml11.htm and
 * http://develforum.opencaching.
 * de/viewtopic.php?t=135&postdays=0&postorder=asc&start=0 for more information.
 */
public class OCXMLImporter extends MinML {
	private static final int STAT_INIT = 0;
	private static final int STAT_CACHE = 1;
	private static final int STAT_CACHE_DESC = 2;
	private static final int STAT_CACHE_LOG = 3;
	private static final int STAT_PICTURE = 4;

	private final static String OPENCACHING_HOST = "www.opencaching.de";
	private int state = STAT_INIT;
	private int numCacheImported, numDescImported, numLogImported = 0;

	private boolean debugGPX = false;
	private CacheDB cacheDB;
	private InfoBox inf;
	private CacheHolder ch;
	private CacheHolder holder;
	private Preferences pref;
	private Profile profile;
	private Time dateOfthisSync;
	private String strData = new String();
	private int picCnt;
	private boolean incUpdate = true; // complete or incremental Update
	private boolean ignoreDesc = false;
	private boolean askForOptions = true;
	private Hashtable DBindexID = new Hashtable();

	private String picUrl = new String();
	private String picTitle = new String();
	private String picID = new String();
	private String ocSeekUrl = new String("http://" + OPENCACHING_HOST
			+ "/viewcache.php?cacheid=");
	private String cacheID = new String();

	private String logData, logDate, logFinder, logId;
	private LogType logType;
	private boolean loggerRecommended;
	private int logTypeOC;
	private String user;
	private double longitude;

	/**
	 * true, if not the last syncdate shall be used, but the caches shall be
	 * reloaded only used in syncSingle
	 */
	private boolean reload;

	public OCXMLImporter(Preferences p, Profile prof) {
		pref = p;
		profile = prof;
		cacheDB = profile.cacheDB;
		if (profile.getLast_sync_opencaching() == null
				|| profile.getLast_sync_opencaching().length() < 12) {
			profile.setLast_sync_opencaching("20050801000000");
			incUpdate = false;
		}
		user = p.myAlias.toLowerCase();
		for (int i = 0; i < cacheDB.size(); i++) {
			ch = cacheDB.get(i);
			if (!ch.getOcCacheID().equals(""))
				DBindexID.put(ch.getOcCacheID(), new Integer(i));
		}// for
	}

	/**
	 * 
	 * @param number
	 * @param infB
	 * @return true, if some change was made to the cacheDB
	 */
	public boolean syncSingle(int number, InfoBox infB) {
		ch = cacheDB.get(number);
		holder = null; // new CacheHolderDetail(ch); //TODO is this still
		// correct? use getDetails ?

		if (infB.isClosed) {
			if (askForOptions)
				return false;
			else
				return true;
		}
		if (askForOptions) {
			OCXMLImporterScreen importOpt = new OCXMLImporterScreen(MyLocale
					.getMsg(1600, "Opencaching.de Download"),
					OCXMLImporterScreen.IMAGES | OCXMLImporterScreen.ALL);
			if (importOpt.execute() == FormBase.IDCANCEL) {
				return false;
			}
			askForOptions = false;
			reload = importOpt.missingCheckBox.getState();
		}

		// this is only a dummy-InfoBox for capturing the output
		inf = new InfoBox("Opencaching download", MyLocale.getMsg(1608,
				"downloading data\n from opencaching"),
				InfoBox.PROGRESS_WITH_WARNINGS, false);
		// inf.setPreferredSize(220, 300);
		// inf.relayout(false);
		// inf.exec();

		String lastS;
		if (reload)
			lastS = "20050801000000";
		else {
			if (ch.getLastSync().length() < 14)
				lastS = "20050801000000";
			else
				lastS = ch.getLastSync();
		}
		dateOfthisSync = new Time();
		dateOfthisSync.parse(lastS, "yyyyMMddHHmmss");

		String url = new String();
		picCnt = 0;
		// Build url
		url = "http://" + OPENCACHING_HOST + "/xml/ocxml11.php?"
				+ "modifiedsince=" + lastS + "&cache=1" + "&cachedesc=1";
		if (pref.downloadPics)
			url += "&picture=1";
		else
			url += "&picture=0";
		url += "&cachelog=1" + "&removedobject=0" + "&wp=" + ch.getWayPoint()
				+ "&charset=utf-8" + "&cdata=0" + "&session=0";
		syncOC(url);
		inf.close(0);
		return true;
	}

	public void doIt() {
		boolean success = true;
		String finalMessage;

		String url = new String();

		String lastS = profile.getLast_sync_opencaching();
		CWPoint centre = pref.curCentrePt; // No need to clone curCentrePt as
		// centre is only read
		if (!centre.isValid()) {
			(new MessageBox("Error", "Coordinates for centre must be set",
					FormBase.OKB)).execute();
			return;
		}
		OCXMLImporterScreen importOpt = new OCXMLImporterScreen(MyLocale
				.getMsg(1600, "Opencaching.de Download"),
				OCXMLImporterScreen.ALL | OCXMLImporterScreen.DIST
						| OCXMLImporterScreen.IMAGES);
		if (importOpt.execute() == FormBase.IDCANCEL) {
			return;
		}
		Vm.showWait(true);
		String dist = importOpt.distanceInput.getText();
		if (dist.length() == 0)
			return;

		Double distDouble = new Double();
		distDouble.value = Common.parseDouble(dist);
		dist = distDouble.toString(0, 1, 0).replace(',', '.');
		// check, if distance is greater than before
		if (Convert.toInt(dist) > Convert.toInt(profile.getDistOC())
				|| pref.downloadmissingOC) {
			// resysnc
			lastS = "20050801000000";
			incUpdate = false;
		}
		profile.setDistOC(dist);
		// Clear status of caches in db
		for (int i = cacheDB.size() - 1; i >= 0; i--) {
			ch = cacheDB.get(i);
			ch.setUpdated(false);
			ch.setNew(false);
			ch.setLog_updated(false);
		}
		picCnt = 0;
		// Build url
		url = "http://" + OPENCACHING_HOST + "/xml/ocxml11.php?"
				+ "modifiedsince=" + lastS + "&cache=1" + "&cachedesc=1";
		if (pref.downloadPics)
			url += "&picture=1";
		else
			url += "&picture=0";
		url += "&cachelog=1" + "&removedobject=0" + "&lat="
				+ centre.getLatDeg(CWPoint.DD) + "&lon="
				+ centre.getLonDeg(CWPoint.DD) + "&distance=" + dist
				+ "&charset=utf-8" + "&cdata=0" + "&session=0";
		inf = new InfoBox("Opencaching download", MyLocale.getMsg(1608,
				"downloading data\n from opencaching"),
				InfoBox.PROGRESS_WITH_WARNINGS, false);
		inf.setPreferredSize(220, 300);
		inf.relayout(false);
		inf.exec();

		success = syncOC(url);
		profile.saveIndex(pref, Profile.SHOW_PROGRESS_BAR);
		Vm.showWait(false);
		if (success) {
			profile.setLast_sync_opencaching(dateOfthisSync
					.format("yyyyMMddHHmmss"));
			// pref.savePreferences();
			finalMessage = MyLocale.getMsg(1607,
					"Update from opencaching successful");
			inf.addWarning("\nNumber of" + "\n...caches new/updated: "
					+ numCacheImported
					+ "\n...cache descriptions new/updated: " + numDescImported
					+ "\n...logs new/updated: " + numLogImported);
			inf.setInfo(finalMessage);
		}
		inf.addOkButton();
	}

	private boolean syncOC(String url) {
		String finalMessage = new String();
		boolean success = true;
		File tmpFile = null;
		BufferedReader r;
		String file = new String();

		// inf = new InfoBox("Opencaching download",
		// MyLocale.getMsg(1608,"downloading data\n from opencaching"),
		// InfoBox.PROGRESS_WITH_WARNINGS, false);

		picCnt = 0;
		try {
			holder = null;
			file = fetch(url, "dummy");

			// parse
			tmpFile = new FileBugfix(profile.getDataDir() + file);
			if (tmpFile.getLength() == 0) {
				throw new IOException("no updates available");
			}

			ZipFile zif = new ZipFile(profile.getDataDir() + file);
			ZipEntry zipEnt;
			Enumeration zipEnum = zif.entries();
			inf.setInfo("...unzipping update file");
			while (zipEnum.hasMoreElements()) {
				zipEnt = (ZipEntry) zipEnum.nextElement();
				// skip over PRC-files and empty files
				if (zipEnt.getSize() > 0 && zipEnt.getName().endsWith("xml")) {
					r = new BufferedReader(new InputStreamReader(zif
							.getInputStream(zipEnt), IO.JAVA_UTF8_CODEC));
					parse(r);
					r.close();
				}
			}
			zif.close();
		} catch (ZipException e) {
			finalMessage = MyLocale.getMsg(1614,
					"Error while unzipping udpate file");
			success = false;
		} catch (IOException e) {
			if (e.getMessage().equalsIgnoreCase("no updates available")) {
				finalMessage = "No updates available";
				success = false;
			} else {
				if (e.getMessage().equalsIgnoreCase("could not connect")
						|| e.getMessage().equalsIgnoreCase("unkown host")) { // is
					// there
					// a
					// better
					// way
					// to
					// find
					// out
					// what
					// happened?
					finalMessage = MyLocale
							.getMsg(1616,
									"Error: could not download udpate file from opencaching.de");
				} else {
					finalMessage = "IOException: " + e.getMessage();
				}
				success = false;
			}
		} catch (IllegalArgumentException e) {
			finalMessage = MyLocale
					.getMsg(
							1621,
							"Error parsing update file\n this is likely a bug in opencaching.de\nplease try again later\n, state:")
					+ " " + state + ", waypoint: " + holder.getWayPoint();
			success = false;
			Vm.debug("Parse error: " + state + " " + holder.getWayPoint());
			e.printStackTrace();
		} catch (Exception e) { // here should be used the correct exception
			if (holder != null)
				finalMessage = MyLocale.getMsg(1615,
						"Error parsing update file, state:")
						+ " " + state + ", waypoint: " + holder.getWayPoint();
			else
				finalMessage = MyLocale.getMsg(1615,
						"Error parsing update file, state:")
						+ " " + state + ", waypoint: <unkown>";
			success = false;
			Vm.debug("Parse error: " + state + " Exception:" + e.toString()
					+ "   " + holder.getOcCacheID());
			e.printStackTrace();
		} finally {
			if (tmpFile != null)
				tmpFile.delete();
		}
		/*
		 * for (int i=cacheDB.size()-1; i >=0; i--) { ch =
		 * (CacheHolder)cacheDB.get(i); if
		 * (ch.wayPoint.toUpperCase().startsWith("OC")) { //TODO only handle
		 * changed caches ch.calcRecommendationScore(); } }
		 */
		inf.setInfo(finalMessage);

		return success;
	}

	public void startElement(String name, AttributeList atts) {
		if (debugGPX) {
			for (int i = 0; i < atts.getLength(); i++) {
				Vm.debug(" Name: " + atts.getName(i) + " Value: "
						+ atts.getValue(i));
			}
		}
		strData = "";

		if (name.equals("oc11xml")) {
			Time lastSync = new Time();
			try {
				lastSync.parse(atts.getValue("date"), "yyyy-MM-dd HH:mm:ss");
			} catch (IllegalArgumentException e) { // TODO Fehler werfen
				Vm.debug(e.toString());
			}
			// reduce time at 1 second to avoid sync problems
			lastSync.setTime(lastSync.getTime() - 1000);
			dateOfthisSync = lastSync;
			state = STAT_INIT;
		}

		// look for changes in the state
		if (name.equals("cache")) {
			state = STAT_CACHE;
			numCacheImported++;
		}
		if (name.equals("cachedesc")) {
			state = STAT_CACHE_DESC;
			numDescImported++;
		}
		if (name.equals("cachelog")) {
			state = STAT_CACHE_LOG;
			numLogImported++;
			logTypeOC = 0;
		}
		if (name.equals("picture")) {
			state = STAT_PICTURE;
		}

		// examine data
		switch (state) {
		case STAT_CACHE:
			startCache(name, atts);
			break;
		case STAT_CACHE_DESC:
			startCacheDesc(name, atts);
			break;
		case STAT_CACHE_LOG:
			startCacheLog(name, atts);
			break;
		case STAT_PICTURE:
			startPicture(name, atts);
			break;
		}

	}

	public void endElement(String name) {
		// examine data
		switch (state) {
		case STAT_CACHE:
			endCache(name);
			break;
		case STAT_CACHE_DESC:
			endCacheDesc(name);
			break;
		case STAT_CACHE_LOG:
			endCacheLog(name);
			break;
		case STAT_PICTURE:
			endPicture(name);
			break;
		}

		// look for changes in the state
		if (name.equals("cache"))
			state = STAT_INIT;
		if (name.equals("cachedesc"))
			state = STAT_INIT;
		if (name.equals("cachelog"))
			state = STAT_INIT;
		if (name.equals("picture"))
			state = STAT_INIT;

	}

	public void characters(char[] ch2, int start, int length) {
		String chars = new String(ch2, start, length);
		strData += chars;
		if (debugGPX)
			Vm.debug(strData);
	}

	private void startCache(String name, AttributeList atts) {
		inf.setInfo(MyLocale.getMsg(1609, "Importing Cache:") + " "
				+ numCacheImported + "\n");
		if (name.equals("id")) {
			cacheID = atts.getValue("id");
		}
		if (name.equals("type")) {
			holder.setType(CacheType.ocType2CwType(atts.getValue("id")));
			return;
		}
		if (name.equals("status")) {
			if (atts.getValue("id").equals("1"))
				holder.setAvailable(true);
			if (atts.getValue("id").equals("2"))
				holder.setAvailable(false);
			if (atts.getValue("id").equals("3")) {
				holder.setArchived(true);
				holder.setAvailable(false);
			}
			if (atts.getValue("id").equals("4"))
				holder.setAvailable(false);
			return;
		}
		if (name.equals("size")) {
			holder.setCacheSize(CacheSize.fromOcString(atts.getValue("id")));
			return;
		}

		if (name.equals("waypoints")) {
			holder.setWayPoint(atts.getValue("oc"));
			if (holder.getWayPoint().length() == 0)
				throw new IllegalArgumentException("empty waypointname"); // this
			// should
			// not
			// happen
			// -
			// it
			// is
			// likey
			// a
			// bug
			// in
			// opencaching.de
			// /
			// it
			// happens
			// on
			// 27-12-2006
			// on
			// cache
			// OC143E
			return;
		}

	}

	private void startCacheDesc(String name, AttributeList atts) {
		inf.setInfo(MyLocale.getMsg(1611, "Importing cache description:") + " "
				+ numDescImported);
		if (name.equals("cachedesc")) {
			ignoreDesc = false;
		}

		if (name.equals("desc")) {
			holder.setHTML(atts.getValue("html").equals("1") ? true : false);
		}

		if (name.equals("language") && !atts.getValue("id").equals("DE")) {
			if (holder.getFreshDetails().getLongDescription().length() > 0)
				ignoreDesc = true; // TODO "DE" in preferences adjustable
			else
				ignoreDesc = false;
		}
	}

	private void startPicture(String name, AttributeList atts) {
		if (name.equals("picture")) {
			inf.setInfo(MyLocale.getMsg(1613, "Pictures:") + " " + ++picCnt);
		}
	}

	private void startCacheLog(String name, AttributeList atts) {
		inf.setInfo(MyLocale.getMsg(1612, "Importing Cachlog:") + " "
				+ numLogImported);
		if (name.equals("logtype")) {
			logTypeOC = Convert.toInt(atts.getValue("id"));
			switch (logTypeOC) {
			case 1:
				logType = LogType.FOUND;
				break;
			case 2:
				logType = LogType.DID_NOT_FOUND;
				holder.setNoFindLogs((byte) (holder.getNoFindLogs() + 1));
				break;
			case 3:
				logType = LogType.NOTE;
			}
			loggerRecommended = atts.getValue("recommended").equals("1");
			return;
		}

		if (name.equals("id")) {
			logId = atts.getValue("id");
		}
	}

	// TODO Do we have to release the "holder" cache details ?
	private void endCache(String name) {
		if (name.equals("cache")) {
			holder.setLastSync(dateOfthisSync.format("yyyyMMddHHmmss"));
			int index;
			index = cacheDB.getIndex(holder.getWayPoint());
			if (index == -1) {
				holder.setNew(true);
				cacheDB.add(holder);
				Integer indexInt = new Integer(cacheDB.size() - 1);
				DBindexID.put(holder.getOcCacheID(), indexInt);
			}
			// update (overwrite) data
			else {
				holder.setNew(false);
				holder.setIncomplete(false);
				cacheDB.get(index).update(holder);
				// save ocCacheID, in case, the previous data is from GPX
				DBindexID.put(holder.getOcCacheID(), new Integer(index));
			}
			// clear data (picture, logs) if we do a complete Update
			if (incUpdate == false) {
				holder.getFreshDetails().getCacheLogs().clear();
				holder.getFreshDetails().getImages().clear();
			}

			// save all
			holder.getFreshDetails().setUnsavedChanges(true); // this makes
			// CachHolder
			// save the
			// details in
			// case that
			// they are
			// unloaded from
			// memory
			// chD.saveCacheDetails(profile.dataDir);
			// profile.saveIndex(pref,Profile.NO_SHOW_PROGRESS_BAR); // this is
			// done after .xml is completly processed
			return;
		}
		if (name.equals("id")) { // </id>
			holder = getHolder(strData); // Allocate a new CacheHolder object
			holder.setOcCacheID(strData);
			holder.getFreshDetails().setUrl(ocSeekUrl + cacheID);
			return;
		}

		if (name.equals("name")) {
			holder.setCacheName(strData);
			return;
		}
		if (name.equals("userid")) {
			holder.setCacheOwner(strData);
			if (pref.isMyAlias(holder.getCacheOwner())) {
				holder.setOwned(true);
			}
			return;
		}

		if (name.equals("longitude")) {
			longitude = Common.parseDouble(strData);
			return;
		}
		if (name.equals("latitude")) {
			holder.setPos(new CWPoint(Common.parseDouble(strData), longitude));
			return;
		}
		if (name.equals("difficulty")) {
			holder.setDifficulty(Difficulty.fromString(strData));
			return;
		}
		if (name.equals("terrain")) {
			holder.setTerrain(Terrain.fromString(strData));
			return;
		}
		if (name.equals("datehidden")) {
			holder.setDateHidden(strData.substring(0, 10)); // Date;
			return;
		}
		if (name.equals("country")) {
			holder.getFreshDetails().setCountry(strData);
			return;
		}
	}

	private void endCacheDesc(String name) {

		if (!ignoreDesc) {
			if (name.equals("cachedesc")) {
				if (pref.downloadPics && holder.is_HTML()) {
					String fetchUrl, imgTag, imgAltText;
					Regex imgRegexUrl = new Regex(
							"(<img[^>]*src=[\"\']([^>^\"^\']*)[^>]*>|<img[^>]*src=([^>^\"^\'^ ]*)[^>]*>)"); // Ergebnis
					// enthält
					// keine
					// Anführungszeichen
					Regex imgRegexAlt = new Regex(
							"(?:alt=[\"\']([^>^\"^\']*)|alt=([^>^\"^\'^ ]*))"); // get
					// alternative
					// text
					// for
					// Pic
					imgRegexAlt.setIgnoreCase(true);
					imgRegexUrl.setIgnoreCase(true);
					int descIndex = 0;
					int numDownloaded = 1;
					while (imgRegexUrl.searchFrom(holder.getFreshDetails()
							.getLongDescription(), descIndex)) { // "img" found
						imgTag = imgRegexUrl.stringMatched(1); // (1) enthält
						// das gesamte
						// <img ...>-tag
						fetchUrl = imgRegexUrl.stringMatched(2); // URL in
						// Anführungszeichen
						// in (2)
						// falls
						// ohne in
						// (3)
						// Ergebnis
						// ist auf
						// jeden
						// Fall ohne
						// Anführungszeichen
						if (fetchUrl == null) {
							fetchUrl = imgRegexUrl.stringMatched(3);
						}
						if (fetchUrl == null) { // TODO Fehler ausgeben: nicht
							// abgedeckt ist der Fall, dass
							// in einem Cache Links auf
							// Bilder mit unterschiedlichen
							// URL, aber gleichem Dateinamen
							// sind.
							inf
									.addWarning(MyLocale
											.getMsg(
													1617,
													"Ignoriere Fehler in html-Cache-Description: \"<img\" without \"src=\" in cache "
															+ holder
																	.getWayPoint()));
							continue;
						}
						inf.setInfo(MyLocale.getMsg(1611,
								"Importing cache description:")
								+ " "
								+ numDescImported
								+ "\n"
								+ MyLocale.getMsg(1620,
										"downloading embedded images: ")
								+ numDownloaded++);
						if (imgRegexAlt.search(imgTag)) {
							imgAltText = imgRegexAlt.stringMatched(1);
							if (imgAltText == null)
								imgAltText = imgRegexAlt.stringMatched(2);
							// kein alternativer Text als Bildüberschrift ->
							// Dateiname
						} else {
							if (fetchUrl.toLowerCase()
									.indexOf("opencaching.de") > 0
									|| fetchUrl.toLowerCase().indexOf(
											"geocaching.com") > 0) // wenn von
								// Opencaching
								// oder
								// geocaching
								// ist
								// Dateiname
								// doch
								// nicht so
								// toll,
								// weil nur
								// aus
								// Nummer
								// bestehend
								imgAltText = new String("No image title");
							else
								imgAltText = fetchUrl.substring(fetchUrl
										.lastIndexOf("/") + 1);
						}
						descIndex = imgRegexUrl.matchedTo();
						getPic(fetchUrl, imgAltText);
					}
				}
				holder.getFreshDetails().setUnsavedChanges(true); // saveCacheDetails(profile.dataDir);
				return;
			}

			if (name.equals("cacheid")) {
				// load cachedata
				holder = getHolder(strData);
				holder.setUpdated(true);
				return;
			}

			if (name.equals("shortdesc")) {
				holder.getFreshDetails().setLongDescription(strData);
				return;
			}

			if (name.equals("desc")) { // </desc>
				if (holder.is_HTML())
					holder.getFreshDetails().setLongDescription(
							holder.getFreshDetails().getLongDescription()
									+ SafeXML.cleanback(strData));
				else
					holder.getFreshDetails().setLongDescription(
							holder.getFreshDetails().getLongDescription()
									+ strData);
				return;
			}
			if (name.equals("hint")) {
				holder.getFreshDetails().setHints(Common.rot13(strData));
				return;
			}
		}
	}

	private String createPicFilename(String fetchURL) {
		String fileName = holder.getWayPoint() + "_"
				+ fetchURL.substring(fetchURL.lastIndexOf("/") + 1);
		return Common.ClearForFileName(fileName);
	}

	private void getPic(String fetchURL, String picDesc) { // TODO handling of
		// relativ URLs
		try {
			if (!fetchURL.startsWith("http://"))
				fetchURL = new URL(new URL("http://" + OPENCACHING_HOST + "/"),
						fetchURL).toString(); // TODO this is not quite
			// correct:
			// actually the "base" URL must
			// be known... but anyway a
			// different baseURL should not
			// happen very often - it
			// doesn't in my area
			String fileName = createPicFilename(fetchURL);
			ImageInfo imageInfo = new ImageInfo();
			// add title
			imageInfo.setTitle(picDesc);
			holder.getFreshDetails().getImages().add(imageInfo);
			try {
				File ftest = new File(profile.getDataDir() + fileName);
				if (ftest.exists()) {
					imageInfo.setFilename(fileName);
				} else {
					if (pref.downloadPics) {
						imageInfo.setFilename(fetch(fetchURL, fileName));
					}
				}
			} catch (IOException e) {
				String ErrMessage = new String(MyLocale.getMsg(1618,
						"Ignoring error in cache: ")
						+ holder.getWayPoint()
						+ ": ignoring IOException: "
						+ e.getMessage()
						+ " while downloading picture:"
						+ fileName + " from URL:" + fetchURL);
				if (e.getMessage().toLowerCase().equalsIgnoreCase(
						"could not connect")
						|| e.getMessage().equalsIgnoreCase("unkown host")) {
					// is there a better way to find out what happened?
					ErrMessage = MyLocale.getMsg(1618,
							"Ignoring error in cache: ")
							+ holder.getCacheName()
							+ " ("
							+ holder.getWayPoint()
							+ ")"
							+ MyLocale.getMsg(1619,
									": could not download image from URL: ")
							+ fetchURL;
				}
				inf.addWarning("\n" + ErrMessage);
				// (new MessageBox(MyLocale.getMsg(144, "Warning"), ErrMessage,
				// MessageBox.OKB)).exec();
				pref.log(ErrMessage);
				e.printStackTrace();
			}
		} catch (MalformedURLException e) {
			String ErrMessage = new String(MyLocale.getMsg(1618,
					"Ignoring error in cache: ")
					+ holder.getWayPoint()
					+ ": ignoring MalformedUrlException: "
					+ e.getMessage()
					+ " while downloading from URL:" + fetchURL);
			inf.addWarning("\n" + ErrMessage);
			pref.log(ErrMessage);
		}

	}

	private void endPicture(String name) {
		if (name.equals("id")) {
			picID = strData;
		} else if (name.equals("url")) {
			picUrl = strData;
		} else if (name.equals("title")) {
			picTitle = strData;
		} else if (name.equals("object")) {
			// get cachedata
			holder = getHolder(strData);
		} else if (name.equals("picture")) {
			// String fileName = holder.wayPoint + "_" +
			// picUrl.substring(picUrl.lastIndexOf("/")+1);
			getPic(picUrl, picTitle);
			holder.getFreshDetails().setUnsavedChanges(true); // saveCacheDetails(profile.dataDir);
		}
	}

	private void endCacheLog(String name) {
		if (name.equals("cachelog")) { // </cachelog>
			holder.getFreshDetails().getCacheLogs().add(
					LogFactory.getInstance().createLog(logType, logDate,
							logFinder, logData, loggerRecommended));
			if (pref.isMyAlias(logFinder) && logTypeOC == 1) {
				holder.setCacheStatus(logDate);
				holder.setFound(true);
				holder.getFreshDetails().setOwnLogId(logId);
				holder.getFreshDetails().setOwnLog(
						LogFactory.getInstance().createLog(logType, logDate,
								logFinder, logData, loggerRecommended));
			}
			holder.getFreshDetails().setUnsavedChanges(true); // chD.saveCacheDetails(profile.dataDir);
			return;
		}

		if (name.equals("cacheid")) { // </cacheid>
			// load cachedata
			holder = getHolder(strData);
			return;
		}

		if (name.equals("date")) {
			logDate = new String(strData);
			return;
		}
		if (name.equals("userid")) {
			logFinder = new String(strData);
			return;
		}
		if (name.equals("text")) {
			logData = new String(strData);
			return;
		}

	}

	private String fetch(String addr, String fileName) throws IOException {
		// Vm.debug("Redirect: " + redirect);
		CharArray realurl = new CharArray();
		ByteArray daten = UrlFetcher.fetchByteArray(addr, realurl);
		String address = realurl.toString();
		if (holder != null)
			fileName = holder.getWayPoint()
					+ "_"
					+ Common.ClearForFileName(address.substring(address
							.lastIndexOf("/") + 1));
		// else fileName =
		// Common.ClearForFileName(address.substring(address.lastIndexOf("/")+1));

		// save file
		// Vm.debug("Save: " + myPref.mydatadir + fileName);
		// Vm.debug("Daten: " + daten.length);
		FileOutputStream outp = new FileOutputStream(profile.getDataDir()
				+ fileName);
		outp.write(daten.toBytes());
		outp.close();
		return fileName;
	}

	/**
	 * Method to iterate through cache database and look for cacheID. Returns
	 * value >= 0 if cacheID is found, else -1
	 */
	private int searchID(String cacxheID) {
		Integer INTR = (Integer) DBindexID.get(cacxheID);
		if (INTR != null) {
			return INTR.intValue();
		} else
			return -1;
	}

	private CacheHolder getHolder(String wpt) {// See also LOCXMLImporter
		CacheHolder chx;
		int index;

		index = cacheDB.getIndex(wpt);
		if (index == -1)
			index = searchID(wpt);
		if (index == -1) {
			chx = new CacheHolder();
		} else {
			chx = cacheDB.get(index);
		}
		return chx;
	}

}
