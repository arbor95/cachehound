package CacheWolf.gui;

import CacheWolf.Global;
import CacheWolf.beans.CWPoint;
import CacheWolf.beans.Preferences;
import CacheWolf.beans.Profile;
import CacheWolf.beans.Version;
import CacheWolf.util.MyLocale;
import ewe.fx.Font;
import ewe.fx.Graphics;
import ewe.fx.Rect;
import ewe.sys.Vm;
import ewe.ui.CellPanel;
import ewe.ui.Editor;
import ewe.ui.Event;
import ewe.ui.FormBase;
import ewe.ui.MenuItem;
import ewe.ui.MessageBox;
import ewe.ui.PanelSplitter;
import ewe.ui.SplittablePanel;
import ewe.ui.WindowConstants;
import ewe.ui.mApp;

/**
 * Mainform is responsible for building the user interface. Class ID = 5000
 */
public class MainForm extends Editor {
	// The next three declares are for the cachelist
	public boolean cacheListVisible = false;
	public CacheList cacheList;
	SplittablePanel split;

	StatusBar statBar = null;
	Preferences pref = Global.getPref(); // Singleton pattern
	Profile profile = Global.getProfile();
	MainTab mTab;
	MainMenu mMenu;

	/**
	 * Constructor for MainForm
	 * <p>
	 * Loads preferences and the cache index list. Then constructs a MainMenu
	 * and the tabbed Panel (MainTab). MainTab holds the different tab panels.
	 * MainMenu contains the menu entries.
	 * 
	 * @see MainMenu
	 * @see MainTab
	 */
	public MainForm() {
		doIt();
	}

	public MainForm(boolean dbg, String pathtoprefxml) {
		pref.debug = dbg;
		pref.setPathToConfigFile(pathtoprefxml); // in case pathtoprefxml ==
		// null the preferences will
		// determine the path itself
		doIt();
	}

	protected boolean canExit(int exitCode) {
		mTab.saveUnsavedChanges(true);
		return true;
	}

	public void doIt() {
		// CellPanel [] p = addToolbar();
		Global.mainForm = this;
		// this.title = "CacheWolf " + Version.getRelease();
		this.exitSystemOnClose = true;
		this.resizable = true;
		this.moveable = true;
		this.windowFlagsToSet = WindowConstants.FLAG_MAXIMIZE_ON_PDA;
		// Rect screen = ((ewe.fx.Rect)
		// (Window.getGuiInfo(WindowConstants.INFO_SCREEN_RECT,null,new
		// ewe.fx.Rect(),0)));
		// if ( screen.height >= 600 && screen.width >= 800)
		// this.setPreferredSize(800, 600);
		this.resizeOnSIP = true;
		InfoBox infB = null;
		try {
			pref.readPrefFile();
			if (MyLocale.initErrors.length() != 0) {
				new MessageBox("Error", MyLocale.initErrors, FormBase.OKB)
						.execute();
			}
			if (Vm.isMobile() == true) {
				// this.windowFlagsToSet |=Window.FLAG_FULL_SCREEN;
				this.resizable = false;
				this.moveable = false;
			} else {
				int h, w;
				h = pref.myAppHeight;
				if (h > MyLocale.getScreenHeight())
					h = MyLocale.getScreenHeight();
				w = pref.myAppWidth;
				if (w > MyLocale.getScreenWidth())
					w = MyLocale.getScreenWidth();
				this.setPreferredSize(w, h);
			}
			addGuiFont();
			if (!pref.selectProfile(profile,
					Preferences.PROFILE_SELECTOR_ONOROFF, true))
				ewe.sys.Vm.exit(0); // User MUST select or create a profile
			Vm.showWait(true);

			// Replace buildt-in symbols with customized images
			GuiImageBroker.customizedSymbols();

			// Load CacheList
			infB = new InfoBox("CacheHound", MyLocale.getMsg(5000,
					"Loading Cache-List"));
			infB.exec();
			infB.waitUntilPainted(100);
			profile.readIndex(infB);
			pref.setCurCenter(new CWPoint(profile.getCenter()));
			profile.updateBearingDistance();
			setTitle("CacheHound " + Version.getRelease() + " - "
					+ profile.name);
		} catch (Exception e) {
			if (pref.debug == true)
				Vm.debug("MainForm:: Exception:: " + e.toString());
		}

		if (pref.showStatus)
			statBar = new StatusBar(pref, profile.cacheDB);
		mMenu = new MainMenu(this);
		mTab = new MainTab(mMenu, statBar);
		split = new SplittablePanel(PanelSplitter.HORIZONTAL);
		split.theSplitter.thickness = 0;
		split.theSplitter.modify(Invisible, 0);
		CellPanel pnlCacheList = split.getNextPanel();
		CellPanel pnlMainTab = split.getNextPanel();
		split.setSplitter(PanelSplitter.MIN_SIZE | PanelSplitter.BEFORE,
				PanelSplitter.HIDDEN | PanelSplitter.BEFORE,
				PanelSplitter.CLOSED);
		pnlCacheList.addLast(cacheList = new CacheList(), STRETCH, FILL);
		pnlMainTab.addLast(mTab, STRETCH, FILL);

		mTab.dontAutoScroll = true;

		this.addLast(split, STRETCH, FILL);
		/*
		 * if (pref.menuAtTop) { this.addLast(mMenu,CellConstants.DONTSTRETCH,
		 * CellConstants.FILL); this.addLast(split,STRETCH,FILL); } else {
		 * this.addLast(split,STRETCH,FILL);
		 * this.addLast(mMenu,CellConstants.DONTSTRETCH, CellConstants.FILL); }
		 */
		mMenu.setTablePanel(mTab.getTablePanel());
		if (infB != null)
			infB.close(0);
		mTab.tbP.refreshTable();
		mTab.tbP.selectFirstRow();
		// mTab.tbP.tc.paintSelection();
		Vm.showWait(false);
	}

	private void addGuiFont() {
		Font defaultGuiFont = mApp.findFont("gui");
		int sz = (pref.fontSize);
		Font newGuiFont = new Font(defaultGuiFont.getName(), defaultGuiFont
				.getStyle(), sz);
		mApp.addFont(newGuiFont, "gui");
		mApp.fontsChanged();
		mApp.mainApp.font = newGuiFont;
	}

	public void doPaint(Graphics g, Rect r) {
		pref.myAppHeight = this.height;
		pref.myAppWidth = this.width;
		super.doPaint(g, r);
	}

	public void onEvent(Event ev) { // Preferences have been changed by
		// PreferencesScreen
		if (pref.dirty == true) {
			mTab.getTablePanel().myMod.setColumnNamesAndWidths();
			mTab.getTablePanel().refreshControl();
			// mTab.getTablePanel().refreshTable();
			pref.dirty = false;
		}
		super.onEvent(ev);
	}

	public void toggleCacheListVisible() {
		cacheListVisible = !cacheListVisible;
		if (cacheListVisible) {
			// Make the splitterbar visible with a width of 6
			split.theSplitter.modify(0, Invisible);
			split.theSplitter.resizeTo(6, split.theSplitter.getRect().height);
			Global.mainForm.mMenu.cacheTour.modifiers |= MenuItem.Checked;
		} else {
			// Hide the splitterbar and set width to 0
			split.theSplitter.modify(Invisible, 0);
			split.theSplitter.resizeTo(0, split.theSplitter.getRect().height);
			Global.mainForm.mMenu.cacheTour.modifiers &= ~MenuItem.Checked;
		}
		split.theSplitter.doOpenClose(cacheListVisible);
		Global.mainForm.mMenu.repaint();

	}

}
