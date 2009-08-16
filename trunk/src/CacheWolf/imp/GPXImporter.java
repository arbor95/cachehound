package CacheWolf.imp;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import CacheWolf.Global;
import CacheWolf.beans.CWPoint;
import CacheWolf.beans.CacheDB;
import CacheWolf.beans.CacheHolder;
import CacheWolf.beans.CacheType;
import CacheWolf.beans.Filter;
import CacheWolf.beans.Preferences;
import CacheWolf.beans.Profile;
import CacheWolf.beans.Travelbug;
import CacheWolf.gui.InfoBox;
import CacheWolf.util.Common;
import CacheWolf.util.Extractor;
import CacheWolf.util.MyLocale;
import CacheWolf.util.ParseLatLon;
import CacheWolf.util.SafeXML;
import de.cachehound.factory.LogFactory;
import de.cachehound.types.CacheSize;
import de.cachehound.types.Difficulty;
import de.cachehound.types.LogType;
import de.cachehound.types.Terrain;
import de.cachehound.util.EweReader;
import de.cachehound.util.SpiderService;
import ewe.sys.Time;
import ewe.sys.Vm;
import ewe.ui.FormBase;
import ewe.ui.MessageBox;
import ewesoft.xml.MinML;
import ewesoft.xml.XMLElement;
import ewesoft.xml.sax.AttributeList;

/**
 * Class to import Data from an GPX File. If cache data exists, the data from
 * the GPX-File is ignored. Class ID = 4000
 */
public class GPXImporter extends MinML {

	static Preferences pref;
	Profile profile;
	CacheDB cacheDB;
	CacheHolder holder;
	String strData, saveDir, logData, logDate, logFinder, logId;
	LogType logType;
	boolean inWpt, inCache, inLogs, inBug;
	public XMLElement document;
	private List<File> files = new ArrayList<File>();
	private boolean debugGPX = false;
	InfoBox infB;
	boolean spiderOK = true;
	boolean doSpider = false;
	boolean fromOC = false;
	boolean fromTC = false;
	boolean nameFound = false;
	static final Time gpxDate = new Time();
	int zaehlerGel = 0;
	public static final int DOIT_ASK = 0;
	public static final int DOIT_NOSPOILER = 1;
	public static final int DOIT_WITHSPOILER = 2;
	boolean getMaps = false;
	SpiderService spider;
	StringBuilder strBuf;

	public GPXImporter(Preferences p, Profile prof, File f) {
		profile = prof;
		pref = p;
		cacheDB = profile.cacheDB;
		// file = f;
		files.add(f);
		saveDir = profile.getDataDir().getAbsolutePath();
		// msgA = msgArea;
		inWpt = false;
		inCache = false;
		inLogs = false;
		inBug = false;
		spider = SpiderService.getInstance();
	}

	public void doIt(int how) {
		Filter flt = new Filter();
		boolean wasFiltered = (profile.getFilterActive() == Filter.FILTER_ACTIVE);
		flt.clearFilter();
		try {
			Reader r;
			File file;

			OCXMLImporterScreen options = new OCXMLImporterScreen(MyLocale
					.getMsg(5510, "Spider Options"), OCXMLImporterScreen.IMAGES
					| OCXMLImporterScreen.ISGC);
			if (options.execute() == FormBase.IDCANCEL) {
				return;
			}
			// String dist = options.distanceInput.getText();
			// if (dist.length()== 0) return;
			// getMaps = options.mapsCheckBox.getState();
			boolean getImages = options.imagesCheckBox.getState();
			doSpider = false;
			if (getImages) {
				doSpider = true;
			}
			options.close(0);

			// Vm.debug("State of: " + doSpider);
			Vm.showWait(true);
			for (int i = 0; i < files.size(); i++) {
				// Test for zip.file
				file = files.get(i);
				if (file.getName().indexOf(".zip") > 0) {
					ZipFile zif = new ZipFile(file);
					ZipEntry zipEnt;
					Enumeration<? extends ZipEntry> zipEnum = zif.entries();
					// there could be more than one file in the archive
					while (zipEnum.hasMoreElements()) {
						zipEnt = zipEnum.nextElement();
						// skip over PRC-files
						if (zipEnt.getName().endsWith("gpx")) {
							r = new InputStreamReader(zif
									.getInputStream(zipEnt));
							infB = new InfoBox(
									zipEnt.toString(),
									(MyLocale.getMsg(4000, "Loaded caches: ") + zaehlerGel));
							infB.exec();
							if (r.read() != 65279)
								r = new InputStreamReader(zif
										.getInputStream(zipEnt));
							parse(new EweReader(r));
							r.close();
							infB.close(0);
						}
					}
				} else {
					r = new InputStreamReader(new FileInputStream(file));
					infB = new InfoBox("Info", (MyLocale.getMsg(4000,
							"Loaded caches: ") + zaehlerGel));
					infB.show();
					if (r.read() != 65279)
						r = new InputStreamReader(new FileInputStream(file));
					parse(new EweReader(r));
					r.close();
					infB.close(0);
				}
				// save Index
				profile.saveIndex(pref, Profile.SHOW_PROGRESS_BAR);
				infB.close(0);
			}
			Vm.showWait(false);
		} catch (Exception e) {
			e.printStackTrace();
			Vm.showWait(false);
		}
		if (wasFiltered) {
			flt.setFilter();
			flt.doFilter();
		}
	}

	public void startElement(String name, AttributeList atts) {
		strBuf = new StringBuilder(300);
		if (name.equals("gpx")) {
			// check for opencaching
			if (atts.getValue("creator").indexOf("opencaching") > 0)
				fromOC = true;
			else
				fromOC = false;
			if (atts.getValue("creator").startsWith("TerraCaching"))
				fromTC = true;
			else
				fromTC = false;

			if (fromOC && doSpider)
				(new MessageBox(
						"Warnung",
						MyLocale
								.getMsg(
										4001,
										"GPX files from opencaching don't contain information of images, they cannot be laoded. Best you get caches from opencaching by menu /Application/Import/Download from Opencaching"),
						FormBase.OKB)).execute();
			zaehlerGel = 0;
		}
		if (name.equals("wpt")) {
			holder = new CacheHolder();
			holder.setPos(new CWPoint(Common.parseDouble(atts.getValue("lat")),
					Common.parseDouble(atts.getValue("lon"))));
			inWpt = true;
			inLogs = false;
			inBug = false;
			nameFound = false;
			zaehlerGel++;
			infB.setInfo(MyLocale.getMsg(4000, "Loaded caches: ") + zaehlerGel);
			return;
		}

		if (name.equals("link") && inWpt) {
			holder.getFreshDetails().setUrl(atts.getValue("href"));
			return;
		}

		if (name.equals("groundspeak:cache")) {
			inCache = true;
			holder.setAvailable(atts.getValue("available").equals("True"));
			holder.setArchived(atts.getValue("archived").equals("True"));
			return;
		}

		if (name.equals("geocache")) {
			boolean available = false;
			boolean archived = false;
			inCache = true;
			// get status
			String status = new String(atts.getValue("status"));
			if (status.equals("Available"))
				available = true;
			else if (status.equals("Unavailable"))
				available = false;
			else if (status.equals("Draft"))
				available = false;
			else if (status.equals("Archived"))
				archived = true;
			holder.setArchived(archived);
			holder.setAvailable(available);
			return;
		}

		if (name.equals("terra:terracache")) {
			inCache = true;
		}

		if (name.equals("groundspeak:long_description")) {
			holder.setHTML(atts.getValue("html").toLowerCase().equals("true"));
		}
		if (name.equals("description") || name.equals("terra:description")) {
			// set HTML always to true if from oc.de or TC
			holder.setHTML(true);
		}

		if (name.equals("groundspeak:logs") || name.equals("logs")
				|| name.equals("terra:logs")) {
			inLogs = true;
			return;
		}
		if (name.equals("groundspeak:log") || name.equals("log")
				|| name.equals("terra:log")) {
			inLogs = true;
			logId = atts.getValue("id");
			return;
		}
		if (name.equals("groundspeak:travelbugs")) {
			inBug = true;
			return;
		}
		if (debugGPX) {
			for (int i = 0; i < atts.getLength(); i++) {
				Vm.debug("Type: " + atts.getType(i) + " Name: "
						+ atts.getName(i) + " Value: " + atts.getValue(i));
			}
		}
	}

	public void endElement(String name) {
		strData = strBuf.toString();
		// Vm.debug("Ende: " + name);

		// logs
		if (inLogs) {
			if (name.equals("groundspeak:date") || name.equals("time")
					|| name.equals("terra:date")) {
				logDate = new String(strData.substring(0, 10));
				return;
			}
			if (name.equals("groundspeak:type") || name.equals("type")
					|| name.equals("terra:type")) {
				logType = LogType.getLogTypeFromGcTypeText(strData);
				return;
			}
			if (name.equals("groundspeak:finder") || name.equals("geocacher")
					|| name.equals("terra:user")) {
				logFinder = new String(strData);
				return;
			}
			if (name.equals("groundspeak:text") || name.equals("text")
					|| name.equals("terra:entry")) {
				logData = new String(strData);
				return;
			}
			if (name.equals("groundspeak:log") || name.equals("log")
					|| name.equals("terra:log")) {
				holder.getFreshDetails().getCacheLogs().add(
						LogFactory.getInstance().createLog(logType, logDate,
								logFinder, logData));
				if ((logType == LogType.FOUND || logType == LogType.PHOTO_TAKEN || logType == LogType.ATTENDED)
						&& pref.isMyAlias(logFinder)) {
					holder.setCacheStatus(logDate);
					holder.setFound(true);
					holder.getFreshDetails().setOwnLogId(logId);
					holder.getFreshDetails().setOwnLog(
							LogFactory.getInstance().createLog(logType,
									logDate, logFinder, logData));
				}
				return;
			}
		}

		if (name.equals("wpt")) {
			// Add cache Data only, if waypoint not already in database
			// if (searchWpt(cacheDB, holder.wayPoint)== -1){
			int index = cacheDB.getIndex(holder.getWayPoint());
			// Vm.debug("here ?!?!?");
			// Vm.debug("could be new!!!!");
			if (index == -1) {
				holder.setNoFindLogs(holder.getFreshDetails().getCacheLogs()
						.countNotFoundLogs());
				holder.setNew(true);
				cacheDB.add(holder);
				// don't spider additional waypoints, so check
				// if waypoint starts with "GC"
				if (doSpider == true) {
					if (spiderOK == true && holder.is_archived() == false) {
						if (holder.getLatLon().length() > 1) {
							if (getMaps) {
								ParseLatLon pll = new ParseLatLon(holder
										.getLatLon(), ".");
								pll.parse();
								// MapLoader mpl = new MapLoader(pref.myproxy,
								// pref.myproxyport);
								// mpl.loadTo(profile.dataDir + "/" +
								// holder.wayPoint + "_map.gif", "3");
								// mpl.loadTo(profile.dataDir + "/" +
								// holder.wayPoint + "_map_2.gif", "10");
							}
						}
						if (holder.getWayPoint().startsWith("GC") || fromTC) {
							// spiderImages();
							spiderImagesUsingSpider();
							// Rename image sources
							String text;
							String orig;
							String imgName;
							orig = holder.getFreshDetails()
									.getLongDescription();
							Extractor ex = new Extractor(orig, "<img src=\"",
									">", 0, false);
							text = ex.findNext();
							int num = 0;
							while (ex.endOfSearch() == false
									&& spiderOK == true) {
								// Vm.debug("Replacing: " + text);
								if (num >= holder.getFreshDetails().getImages()
										.size())
									break;
								imgName = holder.getFreshDetails().getImages()
										.get(num).getTitle();
								holder.getFreshDetails().setLongDescription(
										replace(holder.getFreshDetails()
												.getLongDescription(), text,
												"[[Image: " + imgName + "]]"));
								num++;
								text = ex.findNext();
							}
						}
					}
				}
				holder.save();
				// crw.saveIndex(cacheDB,saveDir);
			}
			// Update cache data
			else {
				CacheHolder oldCh = cacheDB.get(index);
				// Preserve images: Copy images from old cache version because
				// here we didn't add
				// any image information to the holder object.
				holder.getFreshDetails().setImages(
						oldCh.getExistingDetails().getImages());
				oldCh.update(holder);
				oldCh.save();
			}

			inWpt = false;
			return;
		}
		if (name.equals("sym") && strData.endsWith("Found")) {
			holder.setFound(true);
			holder.setCacheStatus(MyLocale.getMsg(318, "Found"));
			return;
		}
		if (name.equals("groundspeak:travelbugs")) {
			inBug = false;
			return;
		}

		if (name.equals("groundspeak:name") && inBug) {
			Travelbug tb = new Travelbug(strData);
			holder.getFreshDetails().getTravelbugs().add(tb);
			holder.setHas_bugs(true);
			return;
		}

		if (name.equals("time") && !inWpt) {
			try {
				gpxDate
						.parse(strData.substring(0, 19),
								"yyyy-MM-dd'T'HH:mm:ss");
			} catch (IllegalArgumentException e) {
				gpxDate.setTime(0);
				Global.getPref().log(
						"Error parsing date: '" + strData + "'. Ignoring.");
			}
			return;
		}

		if (name.equals("time") && inWpt) {
			holder.setDateHidden(strData.substring(0, 10)); // Date;
			return;
		}
		// cache information
		if (name.equals("groundspeak:cache") || name.equals("geocache")
				|| name.equals("terra:terracache")) {
			inCache = false;
		}

		if (name.equals("name") && inWpt && !inCache) {
			holder.setWayPoint(strData);
			if (gpxDate.getTime() != 0) {
				holder.setLastSync(gpxDate.format("yyyyMMddHHmmss"));
			} else {
				holder.setLastSync("");
			}
			// msgA.setText("import " + strData);
			return;
		}
		// Vm.debug("Check: " + inWpt + " / " + fromOC);

		// fill name with contents of <desc>, in case of gc.com the name is
		// later replaced by the contents of <groundspeak:name> which is shorter
		if (name.equals("desc") && inWpt) {
			holder.setCacheName(strData);
			// Vm.debug("CacheName: " + strData);
			// msgA.setText("import " + strData);
			return;
		}
		if (name.equals("url") && inWpt) {
			holder.getFreshDetails().setUrl(strData);
			return;
		}

		// Text for additional waypoints, no HTML
		if (name.equals("cmt") && inWpt) {
			holder.getFreshDetails().setLongDescription(strData);
			holder.setHTML(false);
			return;
		}

		// aditional wapypoint
		if (name.equals("type") && inWpt && !inCache
				&& strData.startsWith("Waypoint")) {
			holder.setType(CacheType.gpxType2CwType(strData));
			holder.setCacheSize(CacheSize.NOT_CHOSEN);
			holder.setDifficulty(Difficulty.DIFFICULTY_UNSET);
			holder.setTerrain(Terrain.TERRAIN_UNSET);
			holder.setLastSync("");
		}

		if ((name.equals("groundspeak:name") || name.equals("terra:name"))
				&& inCache) {
			holder.setCacheName(strData);
			return;
		}
		if (name.equals("groundspeak:owner") || name.equals("owner")
				|| name.equals("terra:owner")) {
			holder.setCacheOwner(strData);
			if (pref.myAlias.equals(strData))
				holder.setOwned(true);
			return;
		}
		if (name.equals("groundspeak:difficulty") || name.equals("difficulty")
				|| name.equals("terra:mental_challenge")) {
			holder.setDifficulty(Difficulty.fromString(strData));
			return;
		}
		if (name.equals("groundspeak:terrain") || name.equals("terrain")
				|| name.equals("terra:physical_challenge")) {
			holder.setTerrain(Terrain.fromString(strData));
			return;
		}
		if ((name.equals("groundspeak:type") || name.equals("type") || name
				.equals("terra:style"))
				&& inCache) {
			holder.setType(CacheType.gpxType2CwType(strData));
			if (holder.isCustomWpt()) {
				holder.setCacheSize(CacheSize.NOT_CHOSEN);
				holder.setDifficulty(Difficulty.DIFFICULTY_UNSET);
				holder.setTerrain(Terrain.TERRAIN_UNSET);
			}
			return;
		}
		if (name.equals("groundspeak:container") || name.equals("container")) {
			holder.setCacheSize(CacheSize
					.fromNormalStringRepresentation(strData));
			return;
		}
		if (name.equals("groundspeak:country") || name.equals("country")) {
			holder.getFreshDetails().setCountry(strData);
			return;
		}
		if (name.equals("groundspeak:state") || name.equals("state")) {
			holder.getFreshDetails().setState(strData);
			return;
		}
		if (name.equals("terra:size")) {
			holder.setCacheSize(CacheSize.fromTcGpxString(strData));
		}

		if (name.equals("groundspeak:short_description")
				|| name.equals("summary")) {
			if (holder.is_HTML())
				holder.getFreshDetails().setLongDescription(
						SafeXML.cleanback(strData) + "<br>"); // <br> needed
			// because we
			// also use a
			// <br> in
			// SpiderGC. Without it the comparison in
			// ch.update fails
			else
				holder.getFreshDetails().setLongDescription(strData + "\n");
			return;
		}

		if (name.equals("groundspeak:long_description")
				|| name.equals("description")
				|| name.equals("terra:description")) {
			if (holder.is_HTML())
				holder.getFreshDetails().setLongDescription(
						holder.getFreshDetails().getLongDescription()
								+ SafeXML.cleanback(strData));
			else
				holder.getFreshDetails()
						.setLongDescription(
								holder.getFreshDetails().getLongDescription()
										+ strData);
			return;
		}
		if (name.equals("groundspeak:encoded_hints") || name.equals("hints")) {
			holder.getFreshDetails().setHints(Common.rot13(strData));
			return;
		}

		if (name.equals("terra:hint")) {
			// remove "&lt;br&gt;<br>" from the end
			int indexTrash = strData.indexOf("&lt;br&gt;<br>");
			if (indexTrash > 0)
				holder.getFreshDetails().setHints(
						Common.rot13(strData.substring(0, indexTrash)));
			return;
		}

	}

	public void characters(char[] ch, int start, int length) {
		strBuf.append(ch, start, length);
		if (debugGPX)
			Vm.debug("Char: " + strBuf.toString());
	}

	public static String TCSizetoText(String size) {
		if (size.equals("1"))
			return "Micro";
		if (size.equals("2"))
			return "Medium";
		if (size.equals("3"))
			return "Regular";
		if (size.equals("4"))
			return "Large";
		if (size.equals("5"))
			return "Very Large";

		return "None";
	}

	/**
	 * Method to iterate through cache database and look for waypoint. Returns
	 * value >= 0 if waypoint is found, else -1
	 */
	/*
	 * private int searchWpt(Vector db, String wpt){ if(wpt.length()>0){ wpt =
	 * wpt.toUpperCase(); CacheHolder ch = new CacheHolder(); //Search through
	 * complete database for(int i = 0;i < db.size();i++){ ch =
	 * (CacheHolder)db.get(i); if(ch.wayPoint.indexOf(wpt) >=0 ){ return i; } }
	 * // for } // if return -1; }
	 */

	private void spiderImagesUsingSpider() {
		String addr;
		String cacheText;

		// just to be sure to have a spider object

		if (fromTC) {
			spider.getImages(holder.getFreshDetails().getLongDescription(),
					holder.getFreshDetails());
		} else {
			addr = "http://www.geocaching.com/seek/cache_details.aspx?wp="
					+ holder.getWayPoint();
			// Vm.debug(addr + "|");
			cacheText = spider.fetchGCSite(addr);
			spider.getImages(cacheText, holder.getFreshDetails());
			try {
				spider.getAttributes(cacheText, holder.getFreshDetails());
			} catch (Exception e) {
				Global.getPref().log(
						"unable to fetch attrivbutes for "
								+ holder.getWayPoint(), e);
			}
		}
	}

	public static String replace(String source, String pattern, String replace) {
		if (source != null) {
			final int len = pattern.length();
			StringBuilder sb = new StringBuilder();
			int found = -1;
			int start = 0;

			while ((found = source.indexOf(pattern, start)) != -1) {
				sb.append(source.substring(start, found));
				sb.append(replace);
				start = found + len;
			}

			sb.append(source.substring(start));

			return sb.toString();
		} else
			return "";
	}
}
