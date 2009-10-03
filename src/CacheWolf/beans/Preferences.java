package CacheWolf.beans;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import CacheWolf.Global;
import CacheWolf.gui.ProfilesForm;
import CacheWolf.gui.myTableModel;
import CacheWolf.imp.SpiderGC;
import CacheWolf.navi.Metrics;
import CacheWolf.util.Common;
import CacheWolf.util.MyLocale;
import CacheWolf.util.SafeXML;
import de.cachehound.util.EweReader;
import ewe.filechooser.FileChooser;
import ewe.filechooser.FileChooserBase;
import ewe.sys.Convert;
import ewe.sys.Vm;
import ewe.ui.FormBase;
import ewe.ui.MessageBox;
import ewe.ui.Window;
import ewe.ui.WindowConstants;
import ewe.util.StringTokenizer;
import ewesoft.xml.MinML;
import ewesoft.xml.sax.AttributeList;

/**
 * A class to hold the preferences that were loaded upon start up of CacheWolf.
 * This class is also capable of parsing the prefs.xml file as well as saving
 * the current settings of preferences.
 */
public class Preferences extends MinML {

	private static Logger logger = LoggerFactory.getLogger(Preferences.class);

	public final int DEFAULT_MAX_LOGS_TO_SPIDER = 250;
	public final int DEFAULT_LOGS_PER_PAGE = 5;
	public final int DEFAULT_INITIAL_HINT_HEIGHT = 10;

	public static final int YES = 0;
	public static final int NO = 1;
	public static final int ASK = 2;
	// Hashtable is saving filter data objects the user wants to save
	private Map<String, FilterData> filterList = new HashMap<String, FilterData>();

	// ////////////////////////////////////////////////////////////////////////////////////
	// Constructor
	// ////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Singleton pattern - return reference to Preferences
	 * 
	 * @return Singleton Preferences object
	 */
	public static Preferences getPrefObject() {
		if (_reference == null)
			// it's ok, we can call this constructor
			_reference = new Preferences();
		return _reference;
	}

	private static Preferences _reference;

	private File configFile;

	/**
	 * Call this method to set the path of the config file <br>
	 * If you call it with null it defaults to [program-dir]/pref.xml if p is a
	 * directory "pref.xml" will automatically appended
	 * 
	 * @param p
	 */
	public void setPathToConfigFile(String p) {
		if (p == null) {
			/*
			 * String test; test = Vm.getenv("APPDATA", "/"); // returns in
			 * java-vm on win xp: c:\<dokumente und
			 * Einstellungen>\<username>\<application data>
			 * log("Vm.getenv(APPDATA: " + test); // this works also in
			 * win32.exe (ewe-vm on win xp) test = Vm.getenv("HOME", "/"); //
			 * This should return on *nix system the home dir
			 * log("Vm.getenv(HOME: " + test); test =
			 * System.getProperty("user.dir"); // return in java-vm on win xp:
			 * <working dir> or maybe <program dir>
			 * log("System.getProperty(user.dir: " + test); // in win32.exe ->
			 * null test = System.getProperty("user.home"); // in MS-java-VM env
			 * variable $HOME is ignored and always <windir>\java returned, see
			 * http://support.microsoft.com/kb/177181/en-us/
			 * log("System.getProperty(user.home: " + test); // in win32.exe ->
			 * null // "user.dir" User's current working directory //
			 * "user.home" User home directory (taken from
			 * http://scv.bu.edu/Doc/Java/tutorial/java/system/properties.html )
			 */
			configFile = new File("pref.xml");
		} else {
			configFile = new File(p);
			if (configFile.isDirectory()) {
				configFile = new File(configFile, "pref.xml");
			}
		}
	}

	/**
	 * Constructor is private for a singleton object
	 */
	private Preferences() {
		if (((ewe.fx.Rect) (Window.getGuiInfo(WindowConstants.INFO_SCREEN_RECT,
				null, new ewe.fx.Rect(), 0))).height > 400) {
			if (Vm.getPlatform().equals("Unix"))
				fontSize = 12;
			else {
				// Default on VGA-PDAs: fontSize 21 + adjust ColWidth
				if (Vm.isMobile()) {
					fontSize = 21;
					listColWidth = "20,20,30,30,92,177,144,83,60,105,50,104,22,30,30";
				} else
					fontSize = 16;
			}
		} else {
			fontSize = 11;
		}
		setPathToConfigFile(null);
	}

	// ////////////////////////////////////////////////////////////////////////////////////
	// Public fields stored in pref.xml
	// ////////////////////////////////////////////////////////////////////////////////////

	/** The base directory contains one subdirectory for each profile */
	private File baseDir;
	/** Name of last used profile */
	public String lastProfile = "";
	/** If true, the last profile is reloaded automatically without a dialogue */
	public boolean autoReloadLastProfile = false;
	/** This is the login alias for geocaching.com and opencaching.de */
	public String myAlias = "";
	/** Optional password */
	public String password = "";
	/**
	 * This is an alternative alias used to identify found caches (i.e. if using
	 * multiple IDs)
	 */
	public String myAlias2 = "";
	/** The path to the browser */
	private String browser = ""; // remove? still in here for holding this
	// information in pref.xml for cachewolf
	// Profiles
	/** Name of proxy for spidering */
	public String myproxy = "";
	/** Proxyport when spidering */
	public String myproxyport = "";
	/** Flag whether proxy is to be used */
	public boolean proxyActive = false;
	/** The default font size */
	public int fontSize = 11;
	// These settings govern where the menu and the tabs are displayed and
	// whether the statusbas is shown
	/** True if the menu is to be displayed at the top of the screen */
	public boolean menuAtTop = true;
	/** True if the tabs are to be displayed at the top of the screen */
	public boolean tabsAtTop = true;
	/** True if the status bar is to be displayed (hidden if false) */
	public boolean showStatus = true;

	/** The list of visible columns in the list view */
	public String listColMap = "0,1,2,3,4,5,6,7,8,9,10,11,12";
	/** The widths for each column in list view */
	public String listColWidth = "15,20,20,25,92,177,144,83,60,105,50,104,22,30,30,30,30,30,30,30,30,30,30";
	/**
	 * The columns which are to be displayed in TravelbugsJourneyScreen. See
	 * also TravelbugJourney
	 */
	public String travelbugColMap = "1,4,5,6,8,9,10,7";
	/** The column widths for the travelbug journeys. */
	public String travelbugColWidth = "212,136,62,90,50,56,90,38,50,50,94,50";
	/** If this flag is true, only non-logged travelbug journeys will be shown */
	public boolean travelbugShowOnlyNonLogged = false;
	/** If this is true, deleted images are shown with a ? in the imagepanel */
	public boolean showDeletedImages = true;
	/**
	 * This setting determines how many logs are shown per page of hintlogs
	 * (default 5)
	 */
	public int logsPerPage = DEFAULT_LOGS_PER_PAGE;
	/** Initial height of hints field (set to 0 to hide them initially) */
	public int initialHintHeight = DEFAULT_INITIAL_HINT_HEIGHT;
	/** Maximum logs to spider */
	public int maxLogsToSpider = DEFAULT_MAX_LOGS_TO_SPIDER;
	/** True if the Solver should ignore the case of variables */
	public boolean solverIgnoreCase = true;
	/**
	 * True if the solver expects arguments for trigonometric functions in
	 * degrees
	 */
	public boolean solverDegMode = true;
	/** True if the description panel should show images */
	public boolean descShowImg = true;
	/** The type of connection which GPSBABEL uses: com1 OR usb. */
	public String garminConn = "com1";
	/** Additional options for GPSBabel, i.e. -s to synthethise short names */
	public String garminGPSBabelOptions = "";
	/**
	 * Max. length for Garmin waypoint names (for etrex which can only accept 6
	 * chars)
	 */
	public int garminMaxLen = 0;
	public boolean downloadmissingOC = false;
	/**
	 * The currently used centre point, can be different from the profile's
	 * centrepoint. This is used for spidering
	 */
	public CWPoint curCentrePt = new CWPoint();
	/** True if a login screen is displayed on each spider operation */
	public boolean forceLogin = true;
	/** True if the goto panel is North centered */
	public boolean northCenteredGoto = true;
	/** If not null, a customs map path has been specified by the user */
	private File customMapsPath;
	/** Number of CacheHolder details that are kept in memory */
	public int maxDetails = 50;
	/**
	 * Number of details to delete when maxDetails have been stored in
	 * cachesWithLoadedDetails
	 */
	public int deleteDetails = 5;
	/** The locale code (DE, EN, ...) */
	public String language = "";
	/** The metric system to use */
	public int metricSystem = Metrics.METRIC;
	/** Load updated caches while spidering */
	public int spiderUpdates = ASK;
	/** Maximum number of new caches to spider */
	public int maxSpiderNumber = 200;
	/** Add short details to waypoint on export */
	public boolean addDetailsToWaypoint = false;
	/** Add short details to name on export */
	public boolean addDetailsToName = false;
	/** The own GC member ID */
	public String gcMemberId = "";
	/** The maximum number of logs to export */
	public int numberOfLogsToExport = 5;
	/** Add Travelbugs when exporting */
	public boolean exportTravelbugs = false;
	/** Try to make a MyFinds GPX when exporting to GPX */
	public boolean exportGpxAsMyFinds = true;
	/** Download images when loading cache data */
	public boolean downloadPics = true;
	/** Download TB information when loading cache data */
	public boolean downloadTBs = true;
	/** Last mode select in the DataMover for processing cache */
	public int processorMode = 0;
	/** maximum number of logs to store in cache details */
	public int maxLogsToKeep = DEFAULT_MAX_LOGS_TO_SPIDER;
	/** keep own logs even when excessing <code>maxLogsToKeep</code> */
	public boolean alwaysKeepOwnLogs = true;

	public String mailHost = "";
	public String mailLoginName = "";
	public String mailPassword = "";
	public String mailProtocol = "";
	public String mailInbox = "INBOX";
	public String mailMoveBox = "CacheHoundReaded";
	public boolean mailMoveMessages = false;
	public boolean mailMarkMailsAsReaded = true;
	public boolean mailDeleteMessages = false;

	// ////////////////////////////////////////////
	/**
	 * The debug switch (Can be used to activate dormant code) by adding the
	 * line:
	 * 
	 * <pre>
	 * &lt;debug value=&quot;true&quot; /&gt;
	 * </pre>
	 * 
	 * to the pref.xml file.
	 */
	@Deprecated
	public boolean debug = true;
	// ////////////////////////////////////////////

	// ////////////////////////////////////////////////////////////////////////////////////
	// Public fields not stored in pref.xml
	// ////////////////////////////////////////////////////////////////////////////////////

	/** The height of the application */
	public int myAppHeight = 600;
	/** The width of the application */
	public int myAppWidth = 800;
	/** True if the preferences were changed and need to be saved */
	public boolean dirty = false;

	// ////////////////////////////////////////////////////////////////////////////////////
	// Read pref.xml file
	// ////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method to open and parse the config file (pref.xml). Results are stored
	 * in the public variables of this class. If you want to specify a non
	 * default config file call setPathToConfigFile() first.
	 */
	public void readPrefFile() {
		// TODO: parsing without EWE stuff ...
		try {
			Reader r = new InputStreamReader(
					new FileInputStream(configFile.getAbsolutePath()));
			parse(new EweReader(r));
			r.close();
		} catch (IOException e) {
			logger.warn("IOException reading config file: "
					+ configFile.getAbsolutePath(), e);
			(new MessageBox(
					MyLocale.getMsg(327, "Information"),
					MyLocale
							.getMsg(
									176,
									"First start - using default preferences \n For experts only: \n Could not read preferences file:\n")
							+ configFile.getAbsolutePath(), FormBase.OKB))
					.execute();
		} catch (NullPointerException e) {
			logger.error("Error reading pref.xml: NullPointerException in Element "
					+ lastName + ". Wrong attribute?", e);
		} catch (Exception e) {
			logger.error("Error reading pref.xml in path " + configFile, e);
		}
	}

	/** Helper variables for XML parser */
	private StringBuilder collectElement = null;
	private String lastName; // The string to the last XML that was processed

	/**
	 * Method that gets called when a new element has been identified in
	 * pref.xml
	 */
	public void startElement(String name, AttributeList atts) {
		// Vm.debug("name = "+name);
		lastName = name;
		String tmp;
		if (name.equals("browser")) {
			browser = atts.getValue("name");
		} else if (name.equals("font"))
			fontSize = Convert.toInt(atts.getValue("size"));
		else if (name.equals("alias")) {
			myAlias = SafeXML.cleanback(atts.getValue("name"));
			tmp = SafeXML.cleanback(atts.getValue("password"));
			if (tmp != null)
				password = tmp;
			SpiderGC.passwort = password;
		} else if (name.equals("alias2"))
			myAlias2 = SafeXML.cleanback(atts.getValue("name"));
		else if (name.equals("gcmemberid"))
			gcMemberId = atts.getValue("name");
		else if (name.equals("location")) {
			curCentrePt.set(atts.getValue("lat") + " " + atts.getValue("long"));
		} else if (name.equals("lastprofile")) {
			collectElement = new StringBuilder(50);
			if (atts.getValue("autoreload").equals("true"))
				autoReloadLastProfile = true;
		}

		else if (name.equals("basedir")) {
			baseDir = new File(atts.getValue("dir"));
		} else if (name.equals("opencaching")) {
			downloadmissingOC = Boolean.valueOf(
					atts.getValue("downloadmissing")).booleanValue();

		} else if (name.equals("listview")) {
			listColMap = atts.getValue("colmap");
			listColWidth = atts.getValue("colwidths");
			while ((new StringTokenizer(listColWidth, ",")).countTokens() < myTableModel.N_COLUMNS)
				listColWidth += ",30"; // for older versions
		} else if (name.equals("proxy")) {
			myproxy = atts.getValue("prx");
			myproxyport = atts.getValue("prt");
			tmp = atts.getValue("active");
			if (tmp != null)
				proxyActive = Boolean.valueOf(tmp).booleanValue();
		} else if (name.equals("garmin")) {
			garminConn = atts.getValue("connection");
			tmp = atts.getValue("GPSBabelOptions");
			if (tmp != null)
				garminGPSBabelOptions = tmp;
			tmp = atts.getValue("MaxWaypointLength");
			if (tmp != null)
				garminMaxLen = Convert.toInt(tmp);
			tmp = atts.getValue("addDetailsToWaypoint");
			if (tmp != null)
				addDetailsToWaypoint = Boolean.valueOf(tmp).booleanValue();
			tmp = atts.getValue("addDetailsToName");
			if (tmp != null)
				addDetailsToName = Boolean.valueOf(tmp).booleanValue();
		} else if (name.equals("imagepanel")) {
			showDeletedImages = Boolean.valueOf(
					atts.getValue("showdeletedimages")).booleanValue();
		} else if (name.equals("descpanel")) {
			descShowImg = Boolean.valueOf(atts.getValue("showimages"))
					.booleanValue();
		} else if (name.equals("screen")) {
			menuAtTop = Boolean.valueOf(atts.getValue("menuattop"))
					.booleanValue();
			tabsAtTop = Boolean.valueOf(atts.getValue("tabsattop"))
					.booleanValue();
			showStatus = Boolean.valueOf(atts.getValue("showstatus"))
					.booleanValue();
			if (atts.getValue("h") != null) {
				myAppHeight = Convert.toInt(atts.getValue("h"));
				myAppWidth = Convert.toInt(atts.getValue("w"));
			}
		} else if (name.equals("hintlogpanel")) {
			logsPerPage = Convert.parseInt(atts.getValue("logsperpage"));
			String strInitialHintHeight = atts.getValue("initialhintheight");
			if (strInitialHintHeight != null)
				initialHintHeight = Convert.parseInt(strInitialHintHeight);
			String strMaxLogsToSpider = atts.getValue("maxspiderlogs");
			if (strMaxLogsToSpider != null)
				maxLogsToSpider = Convert.parseInt(strMaxLogsToSpider);
		} else if (name.equals("solver")) {
			solverIgnoreCase = Boolean.valueOf(
					atts.getValue("ignorevariablecase")).booleanValue();
			tmp = atts.getValue("degMode");
			if (tmp != null)
				solverDegMode = Boolean.valueOf(tmp).booleanValue();
		} else if (name.equals("mapspath")) {
			customMapsPath = new File(atts.getValue("dir"));
		} else if (name.equals("debug"))
			debug = Boolean.valueOf(atts.getValue("value")).booleanValue();

		else if (name.equals("expPath")) {
			exporterPaths.put(atts.getValue("key"), new File(atts
					.getValue("value")));
		} else if (name.equals("impPath")) {
			importerPaths.put(atts.getValue("key"), new File(atts
					.getValue("value")));
		} else if (name.equals("travelbugs")) {
			travelbugColMap = atts.getValue("colmap");
			travelbugColWidth = atts.getValue("colwidths");
			travelbugShowOnlyNonLogged = Boolean.valueOf(
					atts.getValue("shownonlogged")).booleanValue();
		} else if (name.equals("gotopanel")) {
			northCenteredGoto = Boolean.valueOf(atts.getValue("northcentered"))
					.booleanValue();
		} else if (name.equals("spider")) {
			forceLogin = Boolean.valueOf(atts.getValue("forcelogin"))
					.booleanValue();
			tmp = atts.getValue("spiderUpdates");
			if (tmp != null)
				spiderUpdates = Convert.parseInt(tmp);
			tmp = atts.getValue("maxSpiderNumber");
			if (tmp != null)
				maxSpiderNumber = Convert.parseInt(tmp);
			tmp = atts.getValue("downloadPics");
			if (tmp != null)
				downloadPics = Boolean.valueOf(tmp).booleanValue();
			tmp = atts.getValue("downloadTBs");
			if (tmp != null)
				downloadTBs = Boolean.valueOf(tmp).booleanValue();
		} else if (name.equals("details")) {
			maxDetails = Common.parseInt(atts.getValue("cacheSize"));
			deleteDetails = Common.parseInt(atts.getValue("delete"));
			if (maxDetails < 2)
				maxDetails = 2;
			if (deleteDetails < 1)
				deleteDetails = 1;
		} else if (name.equals("metric")) {
			metricSystem = Common.parseInt(atts.getValue("type"));
			if (metricSystem != Metrics.METRIC
					&& metricSystem != Metrics.IMPERIAL) {
				metricSystem = Metrics.METRIC;
			}
		} else if (name.equals("export")) {
			tmp = atts.getValue("numberOfLogsToExport");
			if (tmp != null)
				numberOfLogsToExport = Convert.parseInt(tmp);
			tmp = atts.getValue("exportTravelbugs");
			if (tmp != null)
				exportTravelbugs = Boolean.valueOf(tmp).booleanValue();
			tmp = atts.getValue("exportGpxAsMyFinds");
			if (tmp != null)
				exportGpxAsMyFinds = Boolean.valueOf(tmp).booleanValue();
		} else if (name.equals("locale")) {
			language = atts.getValue("language");
		} else if (name.equals("FILTERDATA")) {
			// Creating a filter object and reading the saved data
			String id = SafeXML.strxmldecode(atts.getValue("id"));
			FilterData data = new FilterData();
			data.setFilterRose(atts.getValue("rose"));
			data.setFilterType(atts.getValue("type"));
			data.setFilterVar(atts.getValue("var"));
			data.setFilterDist(atts.getValue("dist"));
			data.setFilterDiff(atts.getValue("diff"));
			data.setFilterTerr(atts.getValue("terr"));
			data.setFilterSize(atts.getValue("size"));
			data.setFilterAttrYes(Convert.parseLong(atts
					.getValue("attributesYes")));
			data.setFilterAttrNo(Convert.parseLong(atts
					.getValue("attributesNo")));
			data.setFilterAttrChoice(Convert.parseInt(atts
					.getValue("attributesChoice")));
			data.setFilterStatus(SafeXML.strxmldecode(atts.getValue("status")));
			data.setUseRegexp(Boolean.valueOf(atts.getValue("useRegexp"))
					.booleanValue());
			// Filter object is remembered under the given ID
			this.addFilter(id, data);
		} else if (name.equals("datamover")) {
			tmp = atts.getValue("processorMode");
			if (tmp != null) {
				processorMode = Convert.parseInt(tmp);
			}
		} else if (name.equals("logkeeping")) {
			tmp = atts.getValue("maximum");
			if (tmp != null)
				maxLogsToKeep = Convert.parseInt(tmp);
			if (maxLogsToKeep < 0)
				maxLogsToKeep = 0;

			tmp = atts.getValue("keepown");
			if (tmp != null)
				alwaysKeepOwnLogs = Boolean.valueOf(tmp).booleanValue();
		}

		else if (name.equals("mailhost")) {
			mailHost = atts.getValue("value");
		} else if (name.equals("maillogin")) {
			mailLoginName = atts.getValue("value");
		} else if (name.equals("mailpassword")) {
			mailPassword = atts.getValue("value");
		} else if (name.equals("mailprotocol")) {
			mailProtocol = atts.getValue("value");
		} else if (name.equals("mailInbox")) {
			mailInbox = atts.getValue("value");
		} else if (name.equals("mailMoveBox")) {
			mailMoveBox = atts.getValue("value");
		} else if (name.equals("mailMoveMessages")) {
			mailMoveMessages = Boolean.parseBoolean(atts.getValue("value"));
		} else if (name.equals("mailMarkMailsAsReaded")) {
			mailMarkMailsAsReaded = Boolean
					.parseBoolean(atts.getValue("value"));
		} else if (name.equals("mailDeleteMessages")) {
			mailDeleteMessages = Boolean.parseBoolean(atts.getValue("value"));
		}
	}

	public void characters(char ch[], int start, int length) {
		if (collectElement != null) {
			collectElement.append(ch, start, length); // Collect the name of
			// the
			// last profile
		}
	}

	/**
	 * Method that gets called when the end of an element has been identified in
	 * pref.xml
	 */
	public void endElement(String tag) {
		if (tag.equals("lastprofile")) {
			if (collectElement != null)
				lastProfile = collectElement.toString();
		}
		collectElement = null;
	}

	// ////////////////////////////////////////////////////////////////////////////////////
	// Write pref.xml file
	// ////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method to save current preferences in the pref.xml file
	 */
	public void savePreferences() {
		try {
			PrintWriter outp = new PrintWriter(new BufferedWriter(
					new FileWriter(configFile)));
			outp.print("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
			outp.print("<preferences>\n");
			outp.print("    <locale language=\""
					+ SafeXML.strxmlencode(language) + "\"/>\n");
			outp.print("    <basedir dir = \""
					+ SafeXML.strxmlencode(baseDir.getAbsolutePath())
					+ "\"/>\n");
			outp.print("    <lastprofile autoreload=\""
					+ SafeXML.strxmlencode(autoReloadLastProfile) + "\">"
					+ SafeXML.strxmlencode(lastProfile) + "</lastprofile>\n"); // RB
			outp.print("    <alias name =\"" + SafeXML.clean(myAlias)
					+ "\" password=\"" + SafeXML.clean(password) + "\" />\n");
			outp.print("    <alias2 name =\"" + SafeXML.clean(myAlias2)
					+ "\"/>\n");
			outp.print("    <gcmemberid name =\"" + SafeXML.clean(gcMemberId)
					+ "\"/>\n");
			outp.print("    <browser name = \"" + SafeXML.strxmlencode(browser)
					+ "\"/>\n");
			outp.print("    <proxy prx = \"" + SafeXML.strxmlencode(myproxy)
					+ "\" prt = \"" + SafeXML.strxmlencode(myproxyport)
					+ "\" active = \"" + SafeXML.strxmlencode(proxyActive)
					+ "\" />\n");
			outp.print("    <font size =\"" + SafeXML.strxmlencode(fontSize)
					+ "\"/>\n");
			outp.print("    <screen menuattop=\"" + menuAtTop
					+ "\" tabsattop=\"" + tabsAtTop + "\" showstatus=\""
					+ showStatus 
					+ "\" h=\"" + myAppHeight + "\" w=\"" + myAppWidth
					+ "\" />\n");
			outp.print("    <listview colmap=\""
					+ SafeXML.strxmlencode(listColMap) + "\" colwidths=\""
					+ SafeXML.strxmlencode(listColWidth) + "\" />\n");
			outp.print("    <travelbugs colmap=\""
					+ SafeXML.strxmlencode(travelbugColMap) + "\" colwidths=\""
					+ SafeXML.strxmlencode(travelbugColWidth)
					+ "\" shownonlogged=\""
					+ SafeXML.strxmlencode(travelbugShowOnlyNonLogged)
					+ "\" />\n");
			outp.print("    <descpanel showimages=\""
					+ SafeXML.strxmlencode(descShowImg) + "\" />\n");
			outp.print("    <imagepanel showdeletedimages=\""
					+ SafeXML.strxmlencode(showDeletedImages) + "\"/>\n");
			outp.print("    <hintlogpanel logsperpage=\""
					+ SafeXML.strxmlencode(logsPerPage)
					+ "\" initialhintheight=\""
					+ SafeXML.strxmlencode(initialHintHeight)
					+ "\"  maxspiderlogs=\""
					+ SafeXML.strxmlencode(maxLogsToSpider) + "\" />\n");
			outp.print("    <solver ignorevariablecase=\""
					+ SafeXML.strxmlencode(solverIgnoreCase) + "\" degMode=\""
					+ SafeXML.strxmlencode(solverDegMode) + "\" />\n");
			outp.print("    <garmin connection = \""
					+ SafeXML.strxmlencode(garminConn)
					+ "\" GPSBabelOptions = \""
					+ SafeXML.strxmlencode(garminGPSBabelOptions)
					+ "\" MaxWaypointLength = \""
					+ SafeXML.strxmlencode(garminMaxLen)
					+ "\" addDetailsToWaypoint = \""
					+ SafeXML.strxmlencode(addDetailsToWaypoint)
					+ "\" addDetailsToName = \""
					+ SafeXML.strxmlencode(addDetailsToName) + "\" />\n");
			outp.print("    <opencaching downloadMissing=\""
					+ SafeXML.strxmlencode(downloadmissingOC) + "\"/>\n");
			outp.print("    <location lat = \""
					+ SafeXML.strxmlencode(curCentrePt.getLatDeg(CWPoint.DD))
					+ "\" long = \""
					+ SafeXML.strxmlencode(curCentrePt.getLonDeg(CWPoint.DD))
					+ "\"/>\n");
			outp.print("    <spider forcelogin=\""
					+ SafeXML.strxmlencode(forceLogin) + "\" spiderUpdates=\""
					+ SafeXML.strxmlencode(spiderUpdates)
					+ "\" maxSpiderNumber=\""
					+ SafeXML.strxmlencode(maxSpiderNumber)
					+ "\" downloadPics=\"" + SafeXML.strxmlencode(downloadPics)
					+ "\" downloadTBs=\"" + SafeXML.strxmlencode(downloadTBs)
					+ "\"/>\n");
			outp.print("    <gotopanel northcentered=\""
					+ SafeXML.strxmlencode(northCenteredGoto) + "\" />\n");
			outp.print("    <details cacheSize=\""
					+ SafeXML.strxmlencode(maxDetails) + "\" delete=\""
					+ SafeXML.strxmlencode(deleteDetails) + "\"/>\n");
			outp.print("    <metric type=\""
					+ SafeXML.strxmlencode(metricSystem) + "\"/>\n");
			outp.print("    <export numberOfLogsToExport=\""
					+ SafeXML.strxmlencode(numberOfLogsToExport)
					+ "\" exportTravelbugs=\""
					+ SafeXML.strxmlencode(exportTravelbugs)
					+ "\" exportGpxAsMyFinds=\""
					+ SafeXML.strxmlencode(exportGpxAsMyFinds) + "\"/>\n");
			outp.print("    <datamover processorMode=\""
					+ SafeXML.strxmlencode(processorMode) + "\" />\n");
			if (customMapsPath != null)
				outp.print("    <mapspath dir = \""
						+ SafeXML
								.strxmlencode(customMapsPath.getAbsolutePath())
						+ "\"/>\n");
			// Saving filters
			String[] filterIDs = this.getFilterIDs();
			for (int i = 0; i < filterIDs.length; i++) {
				outp.print(this.getFilter(filterIDs[i]).toXML(filterIDs[i]));
			}
			if (debug)
				outp.print("    <debug value=\"true\" />\n"); // Keep the
			// debug
			// switch if it
			// is set
			// save last path of different exporters
			Set<Map.Entry<String, File>> expEntrys = exporterPaths
					.entrySet();
			for (Map.Entry<String, File> entry : expEntrys) {
				outp.print("    <expPath key = \""
						+ SafeXML.strxmlencode(entry.getKey().toString())
						+ "\" value = \""
						+ SafeXML.strxmlencode(entry.getValue()
								.getAbsolutePath()) + "\"/>\n");
			}
			Set<Map.Entry<String, File>> impEntrys = importerPaths.entrySet();
			for (Map.Entry<String, File> entry : impEntrys) {
				outp.print("    <impPath key = \""
						+ SafeXML.strxmlencode(entry.getKey().toString())
						+ "\" value = \""
						+ SafeXML.strxmlencode(entry.getValue()
								.getAbsolutePath()) + "\"/>\n");
			}
			outp.print("    <logkeeping maximum=\""
					+ SafeXML.strxmlencode(maxLogsToKeep) + "\" keepown=\""
					+ SafeXML.strxmlencode(alwaysKeepOwnLogs) + "\" />\n");

			outp.print("    <mailhost value=\""
					+ SafeXML.strxmlencode(mailHost) + "\"/>\n");
			outp.print("    <maillogin value=\""
					+ SafeXML.strxmlencode(mailLoginName) + "\"/>\n");
			outp.print("    <mailpassword value=\""
					+ SafeXML.strxmlencode(mailPassword) + "\"/>\n");
			outp.print("    <mailprotocol value=\""
					+ SafeXML.strxmlencode(mailProtocol) + "\"/>\n");

			outp.print("    <mailInbox value=\""
					+ SafeXML.strxmlencode(mailInbox) + "\"/>\n");
			outp.print("    <mailMoveBox value=\""
					+ SafeXML.strxmlencode(mailMoveBox) + "\"/>\n");
			outp.print("    <mailMoveMessages value=\""
					+ SafeXML.strxmlencode(mailMoveMessages) + "\"/>\n");
			outp.print("    <mailMarkMailsAsReaded value=\""
					+ SafeXML.strxmlencode(mailMarkMailsAsReaded) + "\"/>\n");
			outp.print("    <mailDeleteMessages value=\""
					+ SafeXML.strxmlencode(mailDeleteMessages) + "\"/>\n");

			outp.print("</preferences>");
			outp.close();
		} catch (Exception e) {
			log("Problem saving: " + configFile.getAbsolutePath(), e, true);
		}
	}

	// ////////////////////////////////////////////////////////////////////////////////////
	// Maps
	// ////////////////////////////////////////////////////////////////////////////////////

	// TODO: Muss das auskommentierte wieder rein?
	private static final String mapsPath = "maps";  // + File.separator + "standard";

	/**
	 * custom = set by the user
	 * 
	 * @return custom Maps Path, null if not set
	 */
	public File getCustomMapsPath() {
		return customMapsPath;
	}

	public void saveCustomMapsPath(File mapspath) {
		if (customMapsPath == null || !customMapsPath.equals(mapspath)) {
			customMapsPath = mapspath;
			savePreferences();
		}
	}

	/**
	 * gets the path to the calibrated maps it first tries if there are manually
	 * imported maps in <baseDir>/maps/standard then it tries the legacy dir:
	 * <program-dir>/maps In case in both locations are no .wfl-files it returns
	 * <baseDir>/maps/expedia - the place where the automatically downloaded
	 * maps are placed.
	 * 
	 * 
	 */
	public File getMapLoadPath() {
		saveCustomMapsPath(getMapManuallySavePath(true));
		return getCustomMapsPath();
	}

	
	/**
	 * @param create
	 *            if true the directory if it doesn't exist will be created
	 * @return the path where manually imported maps should be stored this
	 *         should be adjustable in preferences...
	 */
	public File getMapManuallySavePath(boolean create) {
		File mapsDir = new File(baseDir, mapsPath);
		if (create && !(mapsDir.isDirectory())) { // dir
			// exists?
			if (! mapsDir.mkdirs()) {// dir creation
				// failed?
				(new MessageBox(MyLocale.getMsg(321, "Error"), MyLocale.getMsg(
						172, "Error: cannot create maps directory: \n")
						+ mapsDir, FormBase.OKB)).exec();
				return null;
			}
		}
		return mapsDir;
	}

	/**
	 * to this path the automatically downloaded maps are saved
	 */
	public File getMapDownloadSavePath(String mapkind) {
		//String subdir = Global.getProfile().getDataDir().getAbsolutePath()
		//		.substring(Global.getPref().baseDir.length())
		//		.replace('\\', '/');
		//String mapsDir = Global.getPref().baseDir + "maps/"
		//		+ Common.ClearForFileName(mapkind) + "/" + subdir;
		File mapsDir = new File(new File(new File(baseDir, "maps"), Common.ClearForFileName(mapkind)), Global.getProfile().name);   
		if (!mapsDir.isDirectory()) { // dir exists?
			if (!mapsDir.mkdirs()) // dir creation
			// failed?
			{
				(new MessageBox(MyLocale.getMsg(321, "Error"), MyLocale.getMsg(
						172, "Error: cannot create maps directory: \n")
						+ mapsDir.getAbsolutePath(), FormBase.OKB))
						.exec();
				return null;
			}
		}
		return mapsDir;
	}

	public File getMapExpediaLoadPath() {
		return new File(new File(baseDir, "maps"), "expedia");
		// has
		// trailing
		// /
	}

	// ////////////////////////////////////////////////////////////////////////////////////
	// Profile Selector
	// ////////////////////////////////////////////////////////////////////////////////////

	public static final int PROFILE_SELECTOR_FORCED_ON = 0;
	static protected final int PROFILE_SELECTOR_FORCED_OFF = 1;
	public static final int PROFILE_SELECTOR_ONOROFF = 2;

	/**
	 * Open Profile selector screen
	 * 
	 * @param prof
	 * @param showProfileSelector
	 * @return True if a profile was selected
	 */
	public boolean selectProfile(Profile prof, int showProfileSelector,
			boolean hasNewButton) {
		// If datadir is empty, ask for one
		if (baseDir == null || !baseDir.exists() || !baseDir.isDirectory()) {
			do {
				FileChooser fc = new FileChooser(
						FileChooserBase.DIRECTORY_SELECT, "/");
				fc.title = MyLocale.getMsg(170,
						"Select base directory for cache data");
				// If no base directory given, terminate
				if (fc.execute() == FormBase.IDCANCEL)
					ewe.sys.Vm.exit(0);
				baseDir = new File(fc.getChosenFile().getFullPath());
			} while (!baseDir.exists());
		}
		boolean profileExists = true; // Assume that the profile exists
		do {
			if (!profileExists
					|| (showProfileSelector == PROFILE_SELECTOR_FORCED_ON)
					|| (showProfileSelector == PROFILE_SELECTOR_ONOROFF && !autoReloadLastProfile)) { // Ask
				// for
				// the
				// profile
				ProfilesForm f = new ProfilesForm(baseDir, lastProfile,
						!profileExists || hasNewButton);
				int code = f.execute();
				// If no profile chosen (includes a new one), terminate
				if (code == -1)
					return false; // Cancel pressed
				CWPoint savecenter = new CWPoint(prof.centre);
				prof.clearProfile();
				prof.setCenterCoords(savecenter);
				// prof.hasUnsavedChanges = true;
				// curCentrePt.set(0,0); // No centre yet
				lastProfile = f.newSelectedProfile;
			}
			profileExists = (new File(baseDir, lastProfile)).exists();
			if (!profileExists)
				(new MessageBox(MyLocale.getMsg(144, "Warning"), MyLocale
						.getMsg(171, "Profile does not exist: ")
						+ lastProfile, FormBase.MBOK)).execute();
		} while (profileExists == false);
		// Now we are sure that baseDir exists and basDir+profile exists
		prof.name = lastProfile;
		prof.setDataDir(new File(baseDir, lastProfile));
		// prof.setDataDir(prof.getDataDir().replace('\\', '/'));
		// if (!prof.getDataDir().endsWith("/"))
		// prof.setDataDir(prof.getDataDir() + '/');
		savePreferences();
		return true;
	}

	// ////////////////////////////////////////////////////////////////////////////////////
	// Log functions
	// ////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method to log messages to a file called log.txt It will always append to
	 * an existing file. To show the message on the console, the global variable
	 * debug must be set. This can be done by adding
	 * 
	 * <pre>
	 * &lt;debug value=&quot;true&quot;&gt;
	 * </pre>
	 * 
	 * to the pref.xml file
	 * 
	 * @param text
	 *            to log
	 */
	@Deprecated
	public void log(String text) {
		if (logger.isWarnEnabled()) {
			try {
				throw new Exception();
			} catch (Throwable e) {
				e = e.fillInStackTrace();
				StackTraceElement element = e.getStackTrace()[1];
				logger
						.warn("OldLogging ({}): {}", element.getClassName(),
								text);
			}
		}
	}

	/**
	 * Log an exception to the log file with or without a stack trace
	 * 
	 * @param text
	 *            Optional message (Can be empty string)
	 * @param e
	 *            The exception
	 * @param withStackTrace
	 *            If true and the debug switch is true, the stack trace is
	 *            appended to the log The debug switch can be set by including
	 *            the line <i>&lt;debug value="true"&gt;&lt;/debug&gt;</i> in
	 *            the pref.xml file or by manually setting it (i.e. in BE
	 *            versions or RC versions) by including the line
	 * 
	 *            <pre>
	 * Global.getPref().debug = true;
	 * </pre>
	 * 
	 *            in Version.getRelease()
	 */
	@Deprecated
	public void log(String text, Throwable e, boolean withStackTrace) {
		if (logger.isErrorEnabled()) {
			try {
				throw new Exception();
			} catch (Throwable e2) {
				e2 = e2.fillInStackTrace();
				StackTraceElement element = e2.getStackTrace()[1];
				logger.error("OldLogging (" + element.getClassName()
						+ "): text", e);
			}
		}
	}

	/**
	 * Log an exception to the log file without a stack trace, i.e. where a
	 * stack trace is not needed because the location/cause of the error is
	 * clear
	 * 
	 * @param message
	 *            Optional message (Can be empty string)
	 * @param e
	 *            The exception
	 */
	@Deprecated
	public void log(String message, Throwable e) {

		if (logger.isErrorEnabled()) {
			try {
				throw new Exception();
			} catch (Throwable e2) {
				e2 = e2.fillInStackTrace();
				StackTraceElement element = e2.getStackTrace()[1];
				logger.error("OldLogging (" + element.getClassName()
						+ "): text", e);
			}
		}
	}

	// ////////////////////////////////////////////////////////////////////////////////////
	// Exporter path functions
	// ////////////////////////////////////////////////////////////////////////////////////

	/** Hashtable for storing the last export path */
	private Map<String, File> exporterPaths = new HashMap<String, File>();

	public void setExportPath(String exporter, File path) {
		exporterPaths.put(exporter, path);
		savePreferences();
	}

	public void setExportPathFromFileName(String exporter, File file) {
		exporterPaths.put(exporter, file.getParentFile());
		savePreferences();
	}

	public File getExportPath(String exporter) {
		File path = exporterPaths.get(exporter);
		if (path == null) {
			path = Global.getProfile().getDataDir();
		}
		return path;
	}

	private Map<String, File> importerPaths = new HashMap<String, File>();

	public void setImporterPath(String importer, File directory) {
		importerPaths.put(importer, directory);
		savePreferences();
	}

	public File getImporterPath(String importer) {
		File dir = importerPaths.get(importer);
		if (null == dir) {
			dir = Global.getProfile().getDataDir();
		}
		return dir;
	}

	/**
	 * <code>True</code> or <code>false</code>, depending if a filter with the
	 * given ID is saved in the preferences.
	 * 
	 * @param filterID
	 *            ID of the filter to check
	 * @return True or false
	 */
	public boolean hasFilter(String filterID) {
		return this.filterList.containsKey(filterID);
	}

	/**
	 * Returns the FilterData object saved with the given ID. The ID is not
	 * saved in the object, so it may be resaved under another ID.
	 * 
	 * @param filterID
	 *            ID of the FilterData object
	 * @return FilterData object
	 */
	public FilterData getFilter(String filterID) {
		return this.filterList.get(filterID);
	}

	/**
	 * Adds a FilterData object to the list. If a FilterData object is already
	 * saved unter the given ID, the old object is removed and the new one is
	 * set at its place.
	 * 
	 * @param filterID
	 *            ID to associate with the filter object
	 * @param filter
	 *            FilterData object
	 */
	public void addFilter(String filterID, FilterData filter) {
		this.filterList.put(filterID, filter);
	}

	/**
	 * Removed the FilterData object which is saved with the given ID. If no
	 * such FilterData object exists, nothing happens.
	 * 
	 * @param filterID
	 *            ID of FilterData object to remove
	 */
	public void removeFilter(String filterID) {
		this.filterList.remove(filterID);
	}

	/**
	 * Returns a alphabetically sorted array of ID of saved FilterData objects.
	 * 
	 * @return Array of IDs
	 */
	public String[] getFilterIDs() {
		String[] result = this.filterList.keySet().toArray(new String[0]);
		// Now sorting the array of filter IDs
		Comparator<String> comp = new Comparator<String>() {
			public int compare(String s1, String s2) {
				return s1.compareTo(s2);
			}
		};
		Arrays.sort(result, comp);
		return result;
	}

	/**
	 * Returns true, if name is either of the stored aliases.
	 */
	public boolean isMyAlias(String name) {
		return name.equalsIgnoreCase(myAlias)
				|| name.equalsIgnoreCase(myAlias2);
	}

	/**
	 * Returns true, if name is the xml encoded version of either of the stored
	 * aliases.
	 */
	public boolean isMyAliasXML(String name) {
		return name.equalsIgnoreCase(SafeXML.clean(myAlias))
				|| name.equalsIgnoreCase(SafeXML.clean(myAlias2));
	}

	public File getBaseDir() {
		return baseDir;
	}

	public void setBaseDir(File baseDir) {
		this.baseDir = baseDir;
	}
}
