package CacheWolf.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import CacheWolf.Global;
import CacheWolf.beans.CWPoint;
import CacheWolf.beans.CacheDB;
import CacheWolf.beans.CacheHolder;
import CacheWolf.beans.Filter;
import CacheWolf.beans.Preferences;
import CacheWolf.beans.Profile;
import CacheWolf.beans.Version;
import CacheWolf.exporter.ASCExporter;
import CacheWolf.exporter.ExploristExporter;
import CacheWolf.exporter.GPXExporter;
import CacheWolf.exporter.GpxExportNg;
import CacheWolf.exporter.HTMLExporter;
import CacheWolf.exporter.KMLExporter;
import CacheWolf.exporter.LocExporter;
import CacheWolf.exporter.MSARCSVExporter;
import CacheWolf.exporter.OVLExporter;
import CacheWolf.exporter.OziExporter;
import CacheWolf.exporter.TPLExporter;
import CacheWolf.exporter.TomTomExporter;
import CacheWolf.exporter.TritonGPXExporter;
import CacheWolf.imp.GPXImporter;
import CacheWolf.imp.LOCXMLImporter;
import CacheWolf.imp.OCXMLImporter;
import CacheWolf.imp.OCXMLImporterScreen;
import CacheWolf.imp.SpiderGC;
import CacheWolf.util.DataMover;
import CacheWolf.util.MyLocale;
import CacheWolf.util.Rebuild;
import CacheWolf.util.SearchCache;
import de.cachehound.beans.ICacheHolder;
import de.cachehound.exporter.GarminWaypointExporter;
import de.cachehound.exporter.gpx.GpxDecoratorGroundspeak;
import de.cachehound.exporter.gpx.GpxDecoratorLogs;
import de.cachehound.exporter.gpx.GpxDecoratorPictures;
import de.cachehound.exporter.gpx.GpxDecoratorTravelbugs;
import de.cachehound.exporter.gpx.GpxExporter;
import de.cachehound.exporter.loc.LocDecoratorGroundspeak;
import de.cachehound.filter.FilterHelper;
import de.cachehound.filter.HasCoordinatesFilter;
import de.cachehound.gui.GuiFactory;
import de.cachehound.gui.interfaces.IProgressBar;
import de.cachehound.imp.mail.CacheWolfMailHandler;
import de.cachehound.imp.mail.GeocachingMailReader;
import de.cachehound.imp.mail.IGCMailHandler;
import de.cachehound.util.SpiderService;
import de.cachehound.util.gcvote.GcVoteImporter;
import de.cachehound.util.gpsbabel.GPSBabel;
import de.cachehound.util.gpsbabel.GarminDevice;
import de.cachehound.util.gpsbabel.LocFile;
import ewe.filechooser.FileChooser;
import ewe.filechooser.FileChooserBase;
import ewe.fx.Font;
import ewe.io.FileBase;
import ewe.sys.Vm;
import ewe.ui.ControlEvent;
import ewe.ui.Event;
import ewe.ui.Form;
import ewe.ui.FormBase;
import ewe.ui.Gui;
import ewe.ui.Menu;
import ewe.ui.MenuBar;
import ewe.ui.MenuEvent;
import ewe.ui.MenuItem;
import ewe.ui.MessageBox;
import ewe.ui.ProgressBarForm;
import ewe.ui.PullDownMenu;
import ewe.ui.mApp;
import ewe.util.Vector;

/**
 * This class creates the menu for cachewolf. It is also responsible for
 * reacting to user inputs in the menu.<br>
 * This class id=100
 * 
 * @see MainForm
 * @see MainTab Last change: 20061123 salzkammergut Tidied up, added MyLocale,
 *      added additional internationalisation, combine save/filter for small
 *      screens, garminConn
 */
public class MainMenu extends MenuBar {

	private static Logger logger = LoggerFactory.getLogger(MainMenu.class);

	private MenuItem preferences, mnuContext, loadcaches, loadOC, /* savenexit, */
	savenoxit, exit, search, searchAll, searchClr;
	private MenuItem spider, spiderAllFinds, update, chkVersion;
	private MenuItem about, wolflang, sysinfo, legend;
	private MenuItem exportGpxNg, exporthtml, exporttop50, exportGPX,
			exportASC, exportTomTom, exportMSARCSV;
	private MenuItem exportOZI, exportKML, exportTPL, exportExplorist,
			exportTriton;
	private MenuItem exportLOCNoEwe, exportGarminNoEwe, exportGpxDom;
	private MenuItem importMail;
	private MenuItem filtCreate, filtClear, filtInvert, filtSelected,
			filtNonSelected, filtBlack, filtApply;
	private MenuItem exportLOC, exportGPS, mnuSeparator;
	private MenuItem orgNewWP, orgCopy, orgMove, orgDelete, orgRebuild,
			orgCheckNotesAndSolver, orgGcVote;
	public MenuItem cacheTour, orgTravelbugs, mnuForceLogin;
	private MenuItem mnuNewProfile, mnuOpenProfile, mnuEditCenter;
	private Form father;
	private TablePanel tbp;
	private FilterScreen scnFilter = new FilterScreen();
	private static boolean searchInDescriptionAndNotes = false;
	private static boolean searchInLogs = false;

	public MainMenu(Form f) {

		father = f;

		// /////////////////////////////////////////////////////////////////////
		// subMenu for profiles, part of "Application" menu below
		// /////////////////////////////////////////////////////////////////////
		MenuItem[] mnuProfile = new MenuItem[2];
		mnuProfile[0] = mnuNewProfile = new MenuItem(MyLocale.getMsg(1107,
				"New"));
		mnuProfile[1] = mnuOpenProfile = new MenuItem(MyLocale.getMsg(1109,
				"Open"));
		Menu profileMenu = new Menu(mnuProfile, MyLocale
				.getMsg(121, "Profiles"));

		// /////////////////////////////////////////////////////////////////////
		// subMenu for import, part of "Application" menu below
		// /////////////////////////////////////////////////////////////////////
		MenuItem[] mnuImport = new MenuItem[8];
		mnuImport[0] = loadcaches = new MenuItem(MyLocale.getMsg(129,
				"Import GPX"));
		mnuImport[1] = loadOC = new MenuItem(MyLocale.getMsg(130,
				"Download von opencaching.de"));
		mnuImport[2] = spider = new MenuItem(MyLocale.getMsg(131,
				"Spider von geocaching.com"));
		mnuImport[3] = spiderAllFinds = new MenuItem(MyLocale.getMsg(217,
				"Spider all finds from geocaching.com"));
		mnuImport[4] = update = new MenuItem(MyLocale.getMsg(1014,
				"Update cache data"));
		mnuImport[5] = importMail = new MenuItem("import Mails");
		mnuImport[6] = mnuSeparator = new MenuItem("-");
		mnuImport[7] = mnuForceLogin = new MenuItem(MyLocale.getMsg(216,
				"Always login to GC"));

		Menu importMenu = new Menu(mnuImport, MyLocale.getMsg(175, "Import"));
		if (Global.getPref().forceLogin)
			mnuForceLogin.modifiers ^= MenuItem.Checked;

		// /////////////////////////////////////////////////////////////////////
		// subMenu for export, part of "Application" menu below
		// /////////////////////////////////////////////////////////////////////
		MenuItem[] exitems = new MenuItem[19];
		// Vm.debug("Hi in MainMenu "+lr);
		exitems[0] = exporthtml = new MenuItem(MyLocale.getMsg(100, "to HTML"));
		exitems[1] = exportGpxNg = new MenuItem(MyLocale.getMsg(101,
				"to GPX Test"));
		exitems[2] = exporttop50 = new MenuItem(MyLocale.getMsg(102,
				"to TOP50 ASCII"));
		exitems[3] = exportGPX = new MenuItem(MyLocale.getMsg(103, "to GPX"));
		exitems[4] = exportASC = new MenuItem(MyLocale.getMsg(104, "to CSV"));
		exitems[5] = exportTomTom = new MenuItem(MyLocale.getMsg(105,
				"to TomTom"));
		exitems[6] = exportMSARCSV = new MenuItem(MyLocale.getMsg(106,
				"to MS AutoRoute CSV"));
		exitems[7] = exportLOC = new MenuItem(MyLocale.getMsg(215, "to LOC"));
		exitems[8] = exportGPS = new MenuItem(MyLocale.getMsg(122, "to GPS"));
		if (!GPSBabel.isPresent()) {
			exitems[8].modifiers = MenuItem.Disabled;
		}
		exitems[9] = exportOZI = new MenuItem(MyLocale.getMsg(124, "to OZI"));
		exitems[10] = exportKML = new MenuItem(MyLocale.getMsg(125,
				"to Google Earth"));
		exitems[11] = exportExplorist = new MenuItem(MyLocale.getMsg(132,
				"to Explorist"));
		exitems[12] = exportTPL = new MenuItem(MyLocale.getMsg(128,
				"via Template"));
		exitems[13] = exportTriton = new MenuItem(MyLocale.getMsg(138,
				"GPX for VantagePoint (Triton)"));
		exitems[14] = mnuSeparator;
		exitems[15] = exportLOCNoEwe = new MenuItem("to LOC");
		exitems[16] = mnuSeparator;
		exitems[17] = exportGarminNoEwe = new MenuItem(
				"to Garmin (as Waypoints)");
		exitems[18] = exportGpxDom = new MenuItem("Gpx with DOM");
		if (!GarminWaypointExporter.isActive()) {
			exportGarminNoEwe.modifiers = MenuItem.Disabled;
		}

		Menu exportMenu = new Menu(exitems, MyLocale.getMsg(107, "Export"));

		// Now we start with the horizontal menu bar "Application", "Search",
		// "Filter", "Organise", "About"
		// /////////////////////////////////////////////////////////////////////
		// Create the "Application" pulldown menu
		// /////////////////////////////////////////////////////////////////////
		MenuItem[] appMenuItems = new MenuItem[10];
		appMenuItems[0] = new MenuItem(MyLocale.getMsg(121, "Profile"), 0,
				profileMenu);
		appMenuItems[1] = preferences = new MenuItem(MyLocale.getMsg(108,
				"Preferences"));
		appMenuItems[2] = mnuEditCenter = new MenuItem(MyLocale.getMsg(1110,
				"Centre"));
		appMenuItems[3] = mnuContext = new MenuItem(MyLocale.getMsg(134,
				"Current Cache"));
		appMenuItems[4] = mnuSeparator;
		appMenuItems[5] = new MenuItem(MyLocale.getMsg(175, "Import"), 0,
				importMenu);
		appMenuItems[6] = new MenuItem(MyLocale.getMsg(107, "Export"), 0,
				exportMenu);
		appMenuItems[7] = mnuSeparator;
		appMenuItems[8] = savenoxit = new MenuItem(MyLocale.getMsg(127, "Save"));
		// appMenuItems[10] = savenexit = new
		// MenuItem(MyLocale.getMsg(110,"Save & Exit"));
		appMenuItems[9] = exit = new MenuItem(MyLocale.getMsg(111, "Exit"));
		this.addMenu(new PullDownMenu(MyLocale.getMsg(120, "Application"),
				new Menu(appMenuItems, null)));

		// /////////////////////////////////////////////////////////////////////
		// Create the "Search" pulldown menu
		// /////////////////////////////////////////////////////////////////////
		MenuItem[] searchMenuItems = new MenuItem[3];
		searchMenuItems[0] = search = new MenuItem(MyLocale.getMsg(112,
				"Search$" + (char) 6)); // char 6 = ctrl +f
		searchMenuItems[1] = searchAll = new MenuItem(MyLocale.getMsg(133,
				"Search All"));
		searchMenuItems[2] = searchClr = new MenuItem(MyLocale.getMsg(113,
				"Clear search"));

		// /////////////////////////////////////////////////////////////////////
		// Create the "Filter" pulldown menu
		// /////////////////////////////////////////////////////////////////////
		MenuItem[] filterMenuItems = new MenuItem[9];
		filterMenuItems[0] = filtApply = new MenuItem(MyLocale.getMsg(709,
				"Apply"));
		filterMenuItems[1] = filtCreate = new MenuItem(MyLocale.getMsg(114,
				"Create"));
		filterMenuItems[2] = filtInvert = new MenuItem(MyLocale.getMsg(115,
				"Invert"));
		filterMenuItems[3] = filtClear = new MenuItem(MyLocale.getMsg(116,
				"Clear"));
		filterMenuItems[4] = mnuSeparator;
		filterMenuItems[5] = filtSelected = new MenuItem(MyLocale.getMsg(160,
				"Filter selected"));
		filterMenuItems[6] = filtNonSelected = new MenuItem(MyLocale.getMsg(
				1011, "Filter out non selected"));
		filterMenuItems[7] = mnuSeparator;
		filterMenuItems[8] = filtBlack = new MenuItem(MyLocale.getMsg(161,
				"Show Blacklist"));
		filtBlack.modifiers = Global.getProfile().showBlacklisted() ? filtBlack.modifiers
				| MenuItem.Checked
				: filtBlack.modifiers & ~MenuItem.Checked;
		// filterMenuItems[9] = mnuSeparator;
		// filterMenuItems[10] = cacheTour;

		// /////////////////////////////////////////////////////////////////////
		// Create a combined "Filter and Search" pulldown menu for devices with
		// small screens
		// /////////////////////////////////////////////////////////////////////
		MenuItem[] filterAndSearchMenuItems = new MenuItem[12];
		filterAndSearchMenuItems[0] = filtApply;
		filterAndSearchMenuItems[1] = filtCreate;
		filterAndSearchMenuItems[2] = filtInvert;
		filterAndSearchMenuItems[3] = filtClear;
		filterAndSearchMenuItems[4] = mnuSeparator;
		filterAndSearchMenuItems[5] = filtSelected;
		filterAndSearchMenuItems[6] = filtNonSelected;
		filterAndSearchMenuItems[7] = mnuSeparator;
		filterAndSearchMenuItems[8] = filtBlack;
		filterAndSearchMenuItems[9] = mnuSeparator;
		filterAndSearchMenuItems[10] = search;
		filterAndSearchMenuItems[11] = searchClr;
		// filterAndSearchMenuItems[12] = mnuSeparator;
		// filterAndSearchMenuItems[13] = cacheTour;

		// Depending on screen width display either filter and search menus or
		// the combined menu
		if (MyLocale.getScreenWidth() > 300) {
			this.addMenu(new PullDownMenu(MyLocale.getMsg(112, "Search"),
					new Menu(searchMenuItems, null)));
			this.addMenu(new PullDownMenu(MyLocale.getMsg(159, "Filter"),
					new Menu(filterMenuItems, null)));
		} else {
			this.addMenu(new PullDownMenu(MyLocale.getMsg(159, "Filter"),
					new Menu(filterAndSearchMenuItems, null)));
		}

		// /////////////////////////////////////////////////////////////////////
		// Create the "Organise" pulldown menu
		// /////////////////////////////////////////////////////////////////////
		MenuItem[] organiseMenuItems = new MenuItem[11];
		organiseMenuItems[0] = orgNewWP = new MenuItem(MyLocale.getMsg(214,
				"New Waypoint"));
		organiseMenuItems[1] = mnuSeparator;
		organiseMenuItems[2] = orgCopy = new MenuItem(MyLocale.getMsg(141,
				"Copy"));
		organiseMenuItems[3] = orgMove = new MenuItem(MyLocale.getMsg(142,
				"Move"));
		organiseMenuItems[4] = orgDelete = new MenuItem(MyLocale.getMsg(143,
				"Delete"));
		organiseMenuItems[5] = orgRebuild = new MenuItem(MyLocale.getMsg(208,
				"Rebuild Index"));
		organiseMenuItems[6] = orgCheckNotesAndSolver = new MenuItem(MyLocale
				.getMsg(220, "Check Notes/Solver"));
		organiseMenuItems[7] = orgGcVote = new MenuItem("GcVote");
		organiseMenuItems[8] = mnuSeparator;
		organiseMenuItems[9] = orgTravelbugs = new MenuItem(MyLocale.getMsg(
				139, "Manage travelbugs"));
		cacheTour = new MenuItem(MyLocale.getMsg(198, "Cachetour"));
		organiseMenuItems[10] = cacheTour;
		this.addMenu(new PullDownMenu(MyLocale.getMsg(140, "Organise"),
				new Menu(organiseMenuItems, null)));

		// /////////////////////////////////////////////////////////////////////
		// Create the "About" pulldown menu
		// /////////////////////////////////////////////////////////////////////
		MenuItem[] aboutMenuItems = new MenuItem[5];
		aboutMenuItems[0] = about = new MenuItem(MyLocale.getMsg(117, "About"));
		aboutMenuItems[1] = legend = new MenuItem(MyLocale
				.getMsg(155, "Legend"));
		aboutMenuItems[2] = wolflang = new MenuItem(MyLocale.getMsg(118,
				"WolfLanguage"));
		aboutMenuItems[3] = sysinfo = new MenuItem(MyLocale.getMsg(157,
				"System"));
		aboutMenuItems[4] = chkVersion = new MenuItem(MyLocale.getMsg(158,
				"Version Check"));
		this.addMenu(new PullDownMenu(MyLocale.getMsg(117, "About"), new Menu(
				aboutMenuItems, null)));
	}

	public void setTablePanel(TablePanel t) {
		tbp = t;
		if (mnuContext.subMenu == null) {
			if (("," + Global.getPref().listColMap + ",").indexOf(",0,") >= 0)
				mnuContext.subMenu = tbp.tc.getMenuFull();
			else
				mnuContext.subMenu = tbp.tc.getMenuSmall();
		}
	}

	public void allowProfileChange(boolean profileChangeAllowed) {
		if (profileChangeAllowed) {
			mnuNewProfile.modifiers &= ~MenuItem.Disabled;
			mnuOpenProfile.modifiers &= ~MenuItem.Disabled;
		} else {
			mnuNewProfile.modifiers |= MenuItem.Disabled;
			mnuOpenProfile.modifiers |= MenuItem.Disabled;
		}
	}

	public void setForceLogin() {
		mnuForceLogin.modifiers = Global.getPref().forceLogin ? Global.mainTab.mnuMain.modifiers
				| MenuItem.Checked
				: Global.mainTab.mnuMain.modifiers & ~MenuItem.Checked;
	}

	public static void search() {
		SearchBox inp = new SearchBox(MyLocale.getMsg(119, "Search for:"));
		String srch = inp.input(null, "", searchInDescriptionAndNotes,
				searchInLogs, 10);
		if (srch != null) {
			searchInDescriptionAndNotes = inp.useNoteDesc();
			searchInLogs = inp.useLogs();
			SearchCache ssc = new SearchCache(Global.getProfile().cacheDB);
			ssc.search(srch, searchInDescriptionAndNotes, searchInLogs);
			Global.mainTab.tbP.refreshTable();
		}
	}

	public void onEvent(Event ev) {
		Preferences pref = Global.getPref();
		Profile profile = Global.getProfile();
		CacheDB cacheDB = profile.cacheDB;
		Global.mainTab.updatePendingChanges();
		if (ev instanceof MenuEvent) { // && ev.type == MenuEvent.PRESSED
			MenuEvent mev = (MenuEvent) ev;
			// /////////////////////////////////////////////////////////////////////
			// subMenu for profiles, part of "Application" menu
			// /////////////////////////////////////////////////////////////////////
			if (mev.selectedItem == mnuNewProfile) {
				if (NewProfileWizard.startNewProfileWizard(getFrame())) {
					pref.setCurCenter(new CWPoint(profile.getCenter()));
					filtBlack.modifiers = Global.getProfile().showBlacklisted() ? filtBlack.modifiers
							| MenuItem.Checked
							: filtBlack.modifiers & ~MenuItem.Checked;
					tbp.refreshTable();
				}
			}
			if (mev.selectedItem == mnuOpenProfile) {
				Global.mainTab.saveUnsavedChanges(true);
				if (pref.selectProfile(profile,
						Preferences.PROFILE_SELECTOR_FORCED_ON, false)) {
					tbp.myMod.numRows = 0;
					CacheHolder.removeAllDetails();
					profile.cacheDB.clear();
					InfoBox infB = new InfoBox("CacheWolf", MyLocale.getMsg(
							5000, "Loading Cache-List"));
					infB.exec();
					infB.waitUntilPainted(1000);
					Vm.showWait(infB, true);
					profile.readIndex(infB);
					Vm.showWait(infB, false);
					pref.setCurCenter(profile.getCenter());
					filtBlack.modifiers = Global.getProfile().showBlacklisted() ? filtBlack.modifiers
							| MenuItem.Checked
							: filtBlack.modifiers & ~MenuItem.Checked;
					Global.mainForm.setTitle("Cachewolf "
							+ Version.getRelease() + " - " + profile.name);
					infB.close(0);
					tbp.resetModel();
				}
			}
			if (mev.selectedItem == mnuEditCenter) {
				ProfileDataForm f = new ProfileDataForm(pref, profile);
				f.execute(getFrame(), Gui.CENTER_FRAME);
				tbp.refreshTable();
				f.close(0);
			}
			// /////////////////////////////////////////////////////////////////////
			// subMenu for import, part of "Application" menu
			// /////////////////////////////////////////////////////////////////////
			if (mev.selectedItem == spider) {
				SpiderService spider = SpiderService.getInstance();
				Global.mainTab.saveUnsavedChanges(false);
				spider.spiderAroundCenterCoordinates(false);
				cacheDB.clear();
				profile.readIndex();
				tbp.resetModel();
			}
			if (mev.selectedItem == spiderAllFinds) {
				SpiderService spider = SpiderService.getInstance();
				Global.mainTab.saveUnsavedChanges(false);
				spider.spiderAroundCenterCoordinates(true);
				cacheDB.clear();
				profile.readIndex();
				tbp.resetModel();
			}
			if (mev.selectedItem == loadcaches) {
				File dir = pref.getImporterPath("LocGpxImporter");
				FileChooser fc = new FileChooser(FileChooserBase.OPEN
						| FileChooserBase.MULTI_SELECT, (dir == null ? null
						: dir.getAbsolutePath()));
				fc.addMask("*.gpx,*.zip,*.loc");
				fc.setTitle(MyLocale.getMsg(909, "Select file(s)"));
				if (fc.execute() != FormBase.IDCANCEL) {
					dir = new File(fc.getChosenDirectory().getFullPath());
					pref.setImporterPath("LocGpxImporter", new File(fc
							.getChosenDirectory().getFullPath()));
					String files[] = fc.getAllChosen();
					/*
					 * int how = GPXImporter.DOIT_ASK; if (files.length > 0){
					 * InfoBox iB = new InfoBox("Spider?", "Spider Images?",
					 * InfoBox.CHECKBOX); iB.execute(); boolean doSpider =
					 * iB.mCB_state; if (doSpider) how =
					 * GPXImporter.DOIT_WITHSPOILER; else how =
					 * GPXImporter.DOIT_NOSPOILER; }
					 */
					for (int i = 0; i < files.length; i++) {
						File file = new File(dir, files[i]);
						if (file.getName().endsWith("loc")) {
							LOCXMLImporter loc = new LOCXMLImporter(pref,
									profile, file);
							loc.doIt();
						} else {
							try {
								GPXImporter gpx = new GPXImporter(pref,
										profile, file);
								gpx.doIt(0);
							} catch (Throwable e) {
								logger
										.error(
												"Fehler beim Importieren von GPX Datei.",
												e);
							}

						}
					}
				}
				Global.getProfile().setShowBlacklisted(false);
				filtBlack.modifiers = Global.getProfile().showBlacklisted() ? filtBlack.modifiers
						| MenuItem.Checked
						: filtBlack.modifiers & ~MenuItem.Checked;
				tbp.resetModel();
			}
			if (mev.selectedItem == loadOC) {
				OCXMLImporter oc = new OCXMLImporter(pref, profile);
				oc.doIt();
				Global.getProfile().setShowBlacklisted(false);
				filtBlack.modifiers = Global.getProfile().showBlacklisted() ? filtBlack.modifiers
						| MenuItem.Checked
						: filtBlack.modifiers & ~MenuItem.Checked;
				tbp.resetModel();
			}
			if (mev.selectedItem == update)
				updateSelectedCaches(tbp);
			if (mev.selectedItem == mnuForceLogin) {
				mnuForceLogin.modifiers ^= MenuItem.Checked;
				Global.getPref().forceLogin = (mnuForceLogin.modifiers & MenuItem.Checked) != 0;
				Global.getPref().savePreferences();
			}

			if (mev.selectedItem == importMail) {
				try {
					IGCMailHandler handler = new CacheWolfMailHandler(pref,
							profile);
					GeocachingMailReader mailReader = new GeocachingMailReader(
							pref, handler);
					mailReader.connect(true);
					mailReader.readMessages(true);
					mailReader.disconnect(true);
				} catch (Exception e) {
					Vm.showWait(false);
					if (e.getMessage().toLowerCase().contains("bye")) {
						new MessageBox(
								"Failure at receiving Mails",
								"Mail import unsuccessful - it could be, that there was a timeout from the Server. Please try again.",
								FormBase.OKB).execute();
						logger.warn("Mail import unsuccessful", e);
					} else {
						new MessageBox(
								"Error",
								"Mail import unsuccessful - unknown Failure. Details see Logfile",
								FormBase.OKB).execute();
						logger.error("Mail import unsuccessful", e);
					}
				}
				tbp.resetModel();
			}

			// /////////////////////////////////////////////////////////////////////
			// subMenu for export, part of "Application" menu
			// /////////////////////////////////////////////////////////////////////
			if (mev.selectedItem == exporthtml) {
				HTMLExporter htm = new HTMLExporter(pref, profile);
				htm.doIt();
			}
			if (mev.selectedItem == exportGpxNg) {
				GpxExportNg gpx = new GpxExportNg();
				gpx.doit();
			}
			if (mev.selectedItem == exporttop50) {
				OVLExporter ovl = new OVLExporter();
				ovl.doIt();
			}
			if (mev.selectedItem == exportGPX) {
				GPXExporter gpx = new GPXExporter();
				gpx.doIt(1);
			}
			if (mev.selectedItem == exportASC) {
				ASCExporter asc = new ASCExporter();
				asc.doIt();
			}
			if (mev.selectedItem == exportTomTom) {
				TomTomExporter tt = new TomTomExporter();
				tt.doIt();
			}
			if (mev.selectedItem == exportMSARCSV) {
				MSARCSVExporter msar = new MSARCSVExporter();
				msar.doIt();
			}
			if (mev.selectedItem == exportLOC) {
				LocExporter loc = new LocExporter();
				loc.doIt();
			}
			if (mev.selectedItem == exportGPS) {
				Vm.showWait(true);
				LocExporter loc = new LocExporter();
				String tmpFileName = FileBase.getProgramDirectory()
						+ "/temp.loc";
				loc.setTmpFileName(tmpFileName);
				loc.doIt(LocExporter.MODE_AUTO);
				ProgressBarForm.display(MyLocale.getMsg(950, "Transfer"),
						MyLocale.getMsg(951, "Sending to GPS"), null);
				try {
					GPSBabel.convert(
							new LocFile(new java.io.File(tmpFileName)),
							new GarminDevice(pref.garminConn + ":"));
				} catch (java.io.IOException e) {
					(new MessageBox("Error", "Garmin export unsuccessful",
							FormBase.OKB)).execute();
				}
				ProgressBarForm.clear();
				Vm.showWait(false);
			}
			if (mev.selectedItem == exportOZI) {
				OziExporter ozi = new OziExporter();
				ozi.doIt();
			}
			if (mev.selectedItem == exportKML) {
				KMLExporter kml = new KMLExporter();
				kml.doIt();
			}
			if (mev.selectedItem == exportTPL) {
				FileChooser fc = new FileChooser(FileChooserBase.OPEN, FileBase
						.getProgramDirectory()
						+ FileBase.separator + "templates");
				fc.addMask("*.tpl");
				fc.setTitle(MyLocale.getMsg(910, "Select Template file"));
				if (fc.execute() != FormBase.IDCANCEL) {
					TPLExporter tpl = new TPLExporter(pref, profile, new File(
							fc.getChosenFile().getFullPath()));
					tpl.doIt();
				}
			}
			if (mev.selectedItem == exportExplorist) {
				ExploristExporter mag = new ExploristExporter(pref, profile);
				mag.doIt();
			}
			if (mev.selectedItem == exportTriton) {
				TritonGPXExporter mag = new TritonGPXExporter();
				mag.doIt();
			}
			if (mev.selectedItem == exportLOCNoEwe) {
				FileChooser fc = new FileChooser(FileChooserBase.SAVE, FileBase
						.getProgramDirectory());
				fc.addMask("*.loc");
				if (fc.execute() != FormBase.IDCANCEL) {
					try {
						Collection<ICacheHolder> caches = FilterHelper
								.applyFilter(new HasCoordinatesFilter(), Global
										.getProfile().cacheDB.toList());
						de.cachehound.exporter.loc.LocExporter exp = new de.cachehound.exporter.loc.LocExporter(
								new java.io.File(fc.getChosenFile()
										.getAbsolutePath()));
						exp.addDecorator(new LocDecoratorGroundspeak());
						exp.doit(caches);
					} catch (FileNotFoundException e) {
						logger.error("Exception thrown during export", e);
					} catch (IOException e) {
						logger.error("Exception thrown during export", e);
					}
				}
			}
			if (mev.selectedItem == exportGarminNoEwe) {
				GarminWaypointExporter exp = new GarminWaypointExporter();
				try {
					exp.doit(Global.getProfile().cacheDB.toList());
				} catch (IOException e) {
					logger.error("Exception thrown during export", e);
				}
			}

			if (mev.selectedItem == exportGpxDom) {
				FileChooser fc = new FileChooser(FileChooserBase.SAVE, FileBase
						.getProgramDirectory());
				fc.addMask("*.gpx");
				if (fc.execute() != FormBase.IDCANCEL) {
					try {
						Vm.showWait(true);
						FileWriter writer = new FileWriter(new java.io.File(fc
								.getChosenFile().getAbsolutePath()));
						GpxExporter exp = new GpxExporter(writer);
						exp.addDecorator(new GpxDecoratorGroundspeak());
						exp.addDecorator(new GpxDecoratorLogs());
						exp.addDecorator(new GpxDecoratorTravelbugs());
						exp.addDecorator(new GpxDecoratorPictures());
						exp.doit(Global.getProfile().cacheDB.toList());
						writer.close();
						Vm.showWait(false);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			// /////////////////////////////////////////////////////////////////////
			// "Application" pulldown menu
			// /////////////////////////////////////////////////////////////////////
			if (mev.selectedItem == preferences) {
				tbp.saveColWidth(pref);
				PreferencesScreen pfs = new PreferencesScreen(pref);
				pfs.execute(father.getFrame(), Gui.CENTER_FRAME);
				pref.readPrefFile();
			}
			if (mev.selectedItem == savenoxit) {
				profile.saveIndex(pref, Profile.SHOW_PROGRESS_BAR);
				tbp.saveColWidth(pref);
			}
			/*
			 * if(mev.selectedItem == savenexit){
			 * profile.saveIndex(pref,Profile.SHOW_PROGRESS_BAR);
			 * tbp.saveColWidth(pref); ewe.sys.Vm.exit(0); }
			 */
			if (mev.selectedItem == exit) {
				Global.mainTab.saveUnsavedChanges(true);
				ewe.sys.Vm.exit(0);
			}

			// /////////////////////////////////////////////////////////////////////
			// "Search" pulldown menu
			// /////////////////////////////////////////////////////////////////////
			if (mev.selectedItem == search) {
				search();
			}
			if (mev.selectedItem == searchAll) {
				SearchCache ssc = new SearchCache(cacheDB);
				ssc.clearSearch();
				tbp.refreshTable();
				search();
			}
			if (mev.selectedItem == searchClr) {
				SearchCache ssc = new SearchCache(cacheDB);
				ssc.clearSearch();
				tbp.refreshTable();
			}
			// /////////////////////////////////////////////////////////////////////
			// "Filter" pulldown menu
			// /////////////////////////////////////////////////////////////////////
			if (mev.selectedItem == filtApply) {
				Filter flt = new Filter();
				flt.setFilter();
				flt.doFilter();
				tbp.refreshTable();
			}
			if (mev.selectedItem == filtCreate) {
				scnFilter.setData(profile.getCurrentFilter());
				scnFilter.execute(father.getFrame(), Gui.CENTER_FRAME);
				tbp.refreshTable();
			}
			if (mev.selectedItem == filtInvert) {
				Filter flt = new Filter();
				flt.invertFilter();
				tbp.refreshTable();
			}
			if (mev.selectedItem == filtClear) {
				Filter flt = new Filter();
				flt.clearFilter();
				tbp.refreshTable();
			}
			if (mev.selectedItem == filtSelected) { // incremental filter
				Global.getProfile().selectionChanged = true;
				CacheHolder ch;
				boolean filterChanged = false;
				for (int i = cacheDB.size() - 1; i >= 0; i--) {
					ch = cacheDB.get(i);
					// This is an incremental filter, i.e. it keeps the existing
					// filter
					// status and only adds the marked caches to the filtered
					// set
					if (ch.isChecked() && ch.isVisible()) {
						ch.setFiltered(true);
						filterChanged = true;
					}
				}
				if (filterChanged
						&& Global.getProfile().getFilterActive() == Filter.FILTER_INACTIVE) {
					Global.getProfile().setFilterActive(
							Filter.FILTER_MARKED_ONLY);
				}
				tbp.refreshTable();
			}
			if (mev.selectedItem == filtNonSelected) {
				Global.getProfile().selectionChanged = true;
				CacheHolder ch;
				boolean filterChanged = false;
				for (int i = cacheDB.size() - 1; i >= 0; i--) {
					ch = cacheDB.get(i);
					// incremental filter. Keeps status of all marked caches and
					// adds unmarked caches to filtered list
					if (!ch.isChecked() && ch.isVisible()) {
						ch.setFiltered(true);
						filterChanged = true;
					}
				}
				if (filterChanged
						&& Global.getProfile().getFilterActive() == Filter.FILTER_INACTIVE) {
					Global.getProfile().setFilterActive(
							Filter.FILTER_MARKED_ONLY);
				}
				tbp.refreshTable();
			}
			if (mev.selectedItem == filtBlack) {
				// filtBlack.modifiers=filtBlack.modifiers|MenuItem.Checked;
				Global.getProfile().setShowBlacklisted(
						!Global.getProfile().showBlacklisted());
				filtBlack.modifiers = Global.getProfile().showBlacklisted() ? filtBlack.modifiers
						| MenuItem.Checked
						: filtBlack.modifiers & ~MenuItem.Checked;
				SearchCache ssc = new SearchCache(cacheDB);
				ssc.clearSearch();// Clear search & restore filter status
				tbp.refreshTable();
			}
			// /////////////////////////////////////////////////////////////////////
			// "Organise" pulldown menu
			// /////////////////////////////////////////////////////////////////////
			if (mev.selectedItem == orgNewWP) {
				if (Global.mainTab.tbP.getSelectedCache() >= 0)
					Global.mainTab.lastselected = cacheDB.get(
							Global.mainTab.tbP.getSelectedCache())
							.getWayPoint();
				Global.mainTab.newWaypoint(new CacheHolder());
			}

			if (mev.selectedItem == orgCopy) {
				profile.saveIndex(pref, Profile.NO_SHOW_PROGRESS_BAR);
				DataMover dm = new DataMover();
				dm.copyCaches();
				tbp.refreshTable();
			}

			if (mev.selectedItem == orgMove) {
				profile.saveIndex(pref, Profile.NO_SHOW_PROGRESS_BAR);
				DataMover dm = new DataMover();
				dm.moveCaches();
				tbp.refreshTable();
			}

			if (mev.selectedItem == orgDelete) {
				profile.saveIndex(pref, Profile.NO_SHOW_PROGRESS_BAR);
				DataMover dm = new DataMover();
				dm.deleteCaches();
				tbp.refreshTable();
			}
			if (mev.selectedItem == orgRebuild) {
				Rebuild rb = new Rebuild();
				rb.rebuild();
				tbp.refreshTable();
			}
			if (mev.selectedItem == orgCheckNotesAndSolver) {
				// Checking every cache if notes or solver data exist
				CWProgressBar cwp = new CWProgressBar(MyLocale.getMsg(219,
						"Searching..."), 0, cacheDB.size(), true);
				cwp.exec();
				cwp.allowExit(true);
				for (int i = 0; i < cacheDB.size(); i++) {
					cwp.setPosition(i);
					CacheHolder ch = cacheDB.get(i);
					if (ch.getMainCache() == null) {
						ch.setHasNote(!ch.getFreshDetails().getCacheNotes()
								.equals(""));
						ch.setHasSolver(!ch.getFreshDetails().getSolver()
								.equals(""));
					}
					if (cwp.isClosed())
						break;
				} // for
				cwp.exit(0);
				tbp.refreshTable();
			}
			if (mev.selectedItem == orgTravelbugs) {
				TravelbugJourneyScreen tbs = new TravelbugJourneyScreen();
				tbs.setPreferredSize(800, 600);
				tbs.execute(); // getFrame(), Gui.CENTER_FRAME);
				tbs.close(0);
			}
			if (mev.selectedItem == cacheTour) {
				cacheTour.modifiers ^= MenuItem.Checked;
				Global.mainForm.toggleCacheListVisible();
			}
			if (mev.selectedItem == orgGcVote) {
				// Vm.showWait(true);
				GcVoteImporter gcVoteImporter = GcVoteImporter.getInstance();
				IProgressBar progressBar = GuiFactory.getInstance()
						.getProgressBar(gcVoteImporter);
				progressBar.show();
				gcVoteImporter.refreshVotes(Global.getProfile().cacheDB
						.toList());
				progressBar.close();
				tbp.resetModel();
				// Vm.showWait(false);
			}

			// /////////////////////////////////////////////////////////////////////
			// "About" pulldown menu
			// /////////////////////////////////////////////////////////////////////
			if (mev.selectedItem == about) {
				InfoScreen is = new InfoScreen(MyLocale
						.getLocalizedFile("info.html"), MyLocale.getMsg(117,
						"About"), pref);
				is.execute(father.getFrame(), Gui.CENTER_FRAME);
			}
			if (mev.selectedItem == legend) {
				InfoScreen is = new InfoScreen(MyLocale
						.getLocalizedFile("legende.html"), MyLocale.getMsg(155,
						"Legend"), pref);
				is.execute(father.getFrame(), Gui.CENTER_FRAME);
			}
			if (mev.selectedItem == wolflang) {
				InfoScreen is = new InfoScreen(MyLocale
						.getLocalizedFile("wolflang.html"), MyLocale.getMsg(
						118, "WolfLanguage"), pref);
				is.execute(father.getFrame(), Gui.CENTER_FRAME);
			}
			if (mev.selectedItem == sysinfo) {
				// Vm.debug("Checking system...");
				StringBuilder sb = new StringBuilder(400);
				Font f = mApp.guiFont;
				sb.append(MyLocale.getMsg(121, "Profile"));
				sb.append(": ");
				sb.append(profile.getDataDir());
				sb.append("<br>");
				sb.append(MyLocale.getMsg(260, "Platform:"));
				sb.append(' ');
				sb.append(Vm.getPlatform());
				sb.append("<br>");
				sb.append(MyLocale.getMsg(261, "Locale lang is:"));
				sb.append(' ');
				sb.append(MyLocale.getLocaleLanguage());
				sb.append("<br>");
				sb.append(MyLocale.getMsg(262, "Locale country is:"));
				sb.append(' ');
				sb.append(MyLocale.getLocaleCountry());
				sb.append("<br>");
				sb.append(MyLocale.getMsg(263, "Decimal separator is:"));
				sb.append(" \"");
				sb.append(MyLocale.getDigSeparator());
				sb.append("\"<br>");
				sb.append(MyLocale.getMsg(264, "Device is PDA:"));
				sb.append(' ');
				sb.append(Vm.isMobile());
				sb.append("<br>");
				sb.append(MyLocale.getMsg(265, "Screen:"));
				sb.append(' ');
				sb.append(MyLocale.getScreenWidth());
				sb.append(" x ");
				sb.append(MyLocale.getScreenHeight());
				sb.append("<br>");
				sb.append(MyLocale.getMsg(266, "Font size:"));
				sb.append(' ');
				sb.append(f.getSize());
				sb.append("<br>");
				sb.append(MyLocale.getMsg(267, "Entries in DB:"));
				sb.append(' ');
				sb.append(cacheDB.size());
				sb.append("<br>");
				sb.append(MyLocale.getMsg(268, "File separator is:"));
				sb.append(" \"");
				sb.append(Vm.getProperty("file.separator", "def"));
				sb.append("\"<br>");
				sb.append(MyLocale.getMsg(269, "Programme directory is:"));
				sb.append(' ');
				sb.append(FileBase.getProgramDirectory());
				sb.append("<br>");
				sb.append(MyLocale.getMsg(270, "Number of details in RAM is"));
				sb.append(' ');
				sb.append(CacheHolder.cachesWithLoadedDetails.size());
				sb.append(' ');
				sb.append(MyLocale.getMsg(271, "Max.:"));
				sb.append(' ');
				sb.append(Global.getPref().maxDetails);
				sb.append("<br>");
				sb.append(MyLocale.getMsg(272, "CacheWolf version:"));
				sb.append(' ');
				sb.append(Version.getReleaseDetailed());
				sb.append("<br>");
				InfoScreen is = new InfoScreen(sb.toString(), "System", pref);
				is.execute(father.getFrame(), Gui.CENTER_FRAME);
			}
			if (mev.selectedItem == chkVersion) {
				(new MessageBox(MyLocale.getMsg(178, "Version Checking"),
						Version.getUpdateMessage(), FormBase.OKB)).execute();
			}

			// In case that the triggered event was due to one of the context
			// menu items, process
			// the event by the context menu handler
			tbp.tc.popupMenuEvent(mev.selectedItem);

		} else if (ev instanceof ControlEvent) {
			if (ev.type == ControlEvent.MENU_SHOWN) {
				Global.mainTab.tbP.tc.adjustAddiHideUnhideMenu();
			}
		}
	}

	public static void updateSelectedCaches(TablePanel tablePanel) {
		Preferences pref = Global.getPref();
		Profile profile = Global.getProfile();
		CacheDB cacheDB = profile.cacheDB;
		CacheHolder ch;

		OCXMLImporterScreen options = new OCXMLImporterScreen(MyLocale.getMsg(
				5003, "Options"), OCXMLImporterScreen.IMAGES
				| OCXMLImporterScreen.TRAVELBUGS | OCXMLImporterScreen.MAXLOGS);
		if (options.execute() == FormBase.IDCANCEL) {
			return;
		}

		SpiderService spider = SpiderService.getInstance();
		OCXMLImporter ocSync = new OCXMLImporter(pref, profile);
		// Vm.debug("ByPass? " + profile.byPassIndexActive);
		Vm.showWait(true);
		boolean alreadySaid = false;
		boolean alreadySaid2 = false;
		InfoBox infB = new InfoBox("Info", "Loading",
				InfoBox.PROGRESS_WITH_WARNINGS);
		infB.exec();

		boolean loadAllLogs = (pref.maxLogsToSpider > 5);

		Vector cachesToUpdate = new Vector();
		for (int i = 0; i < cacheDB.size(); i++) {
			ch = cacheDB.get(i);
			if (ch.isChecked() == true && ch.isVisible()) {
				if (ch.getWayPoint().length() > 1
						&& (ch.getWayPoint().substring(0, 2).equalsIgnoreCase(
								"GC") || ch.getWayPoint().substring(0, 2)
								.equalsIgnoreCase("OC")))
				// if ( (ch.wayPoint.length() > 1 &&
				// ch.wayPoint.substring(0,2).equalsIgnoreCase("GC")))
				// Notiz: Wenn es ein addi Wpt ist, sollte eigentlich der
				// Maincache gespidert werden
				// Alter code pr�ft aber nur ob ein Maincache von GC existiert
				// und versucht dann den addi direkt zu spidern, was nicht
				// funktioniert
				{
					cachesToUpdate.add(new Integer(i));
				} else {
					if (ch.isAddiWpt() && ch.getMainCache() != null
							&& !ch.getMainCache().isChecked() && !alreadySaid2) { // Is
						// the
						// father
						// ticked?
						alreadySaid2 = true;
						(new MessageBox(
								MyLocale.getMsg(327, "Information"),
								MyLocale
										.getMsg(5001,
												"Can't spider additional waypoint directly. Please check main cache."),
								FormBase.OKB)).execute();
					}
					if (!ch.isAddiWpt() && !alreadySaid) {
						alreadySaid = true;
						(new MessageBox(
								MyLocale.getMsg(327, "Information"),
								ch.getWayPoint()
										+ MyLocale
												.getMsg(
														5002,
														": At the moment this function is only applicable for geocaching.com and opencaching.de."),
								FormBase.OKB)).execute();
					}
				}

			}
		}

		int spiderErrors = 0;
		for (int j = 0; j < cachesToUpdate.size(); j++) {
			int i = ((Integer) cachesToUpdate.get(j)).intValue();
			ch = cacheDB.get(i);
			// infB.setInfo("Loading: " + ch.wayPoint);
			infB.setInfo(MyLocale.getMsg(5513, "Loading: ") + ch.getWayPoint()
					+ " (" + (j + 1) + " / " + cachesToUpdate.size() + ")");
			infB.redisplay();
			if (ch.getWayPoint().substring(0, 2).equalsIgnoreCase("GC")) {
				int test = spider.spiderSingle(ch, infB, loadAllLogs);
				if (test == SpiderGC.SPIDER_CANCEL) {
					infB.close(0);
					break;
				} else if (test == SpiderGC.SPIDER_ERROR) {
					spiderErrors++;
				} else {
					// profile.hasUnsavedChanges=true;
				}
			} else {
				if (!ocSync.syncSingle(i, infB)) {
					infB.close(0);
					break;
				} else {
					// profile.hasUnsavedChanges=true;
				}
			}

			// cacheDB.clear();
			// profile.readIndex();
		}
		infB.close(0);
		profile.saveIndex(pref, Profile.SHOW_PROGRESS_BAR);
		profile.restoreFilter();
		profile.updateBearingDistance();
		tablePanel.refreshTable();
		Vm.showWait(false);
		if (spiderErrors > 0) {
			new MessageBox(MyLocale.getMsg(5500, "Error"), spiderErrors
					+ MyLocale.getMsg(5516,
							" cache descriptions%0acould not be loaded."),
					FormBase.DEFOKB).execute();
		}
	}

}
