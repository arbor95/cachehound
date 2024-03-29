package CacheWolf.gui;

import java.io.File;

import CacheWolf.Global;
import CacheWolf.beans.CWPoint;
import CacheWolf.beans.CacheDB;
import CacheWolf.beans.CacheHolder;
import CacheWolf.beans.ImageInfo;
import CacheWolf.beans.Preferences;
import CacheWolf.beans.Profile;
import CacheWolf.beans.Travelbug;
import CacheWolf.beans.TravelbugJourneyList;
import CacheWolf.beans.TravelbugList;
import CacheWolf.controller.TravelbugPickup;
import CacheWolf.util.DataMover;
import CacheWolf.util.MyLocale;
import de.cachehound.factory.CacheHolderDetailFactory;
import de.cachehound.types.CacheSize;
import de.cachehound.types.CacheType;
import de.cachehound.types.Difficulty;
import de.cachehound.types.Terrain;
import ewe.filechooser.FileChooser;
import ewe.filechooser.FileChooserBase;
import ewe.fx.Color;
import ewe.fx.Dimension;
import ewe.fx.Point;
import ewe.fx.mImage;
import ewe.sys.Convert;
import ewe.sys.Time;
import ewe.sys.Vm;
import ewe.ui.CellConstants;
import ewe.ui.CellPanel;
import ewe.ui.Control;
import ewe.ui.ControlConstants;
import ewe.ui.ControlEvent;
import ewe.ui.DataChangeEvent;
import ewe.ui.DateChooser;
import ewe.ui.Event;
import ewe.ui.Form;
import ewe.ui.FormBase;
import ewe.ui.Gui;
import ewe.ui.HtmlDisplay;
import ewe.ui.InputBox;
import ewe.ui.Menu;
import ewe.ui.MenuItem;
import ewe.ui.MessageBox;
import ewe.ui.PenEvent;
import ewe.ui.ScrollBarPanel;
import ewe.ui.ScrollablePanel;
import ewe.ui.mButton;
import ewe.ui.mChoice;
import ewe.ui.mComboBox;
import ewe.ui.mInput;
import ewe.ui.mLabel;
import ewe.ui.mTextPad;

/**
 * Class to create the panel to show the cache details.<br>
 * Also allows for creation of a custom waypoint.<br>
 */
public class DetailsPanel extends CellPanel {

	private mInput inpWaypoint = new mInput();
	private mInput inpName = new mInput();
	private mButton btnWayLoc = new mButton();
	private mInput inpHidden = new mInput();
	private mInput inpOwner = new mInput();
	private mButton btnCenter, btnAddDateTime;

	private String[] cacheTypeGuiStrings;
	private mChoice chcType;
	private mChoice chcSize;

	mComboBox chcStatus = new mComboBox(new String[] { "",
			MyLocale.getMsg(313, "Flag 1"), MyLocale.getMsg(314, "Flag 2"),
			MyLocale.getMsg(315, "Flag 3"), MyLocale.getMsg(316, "Flag 4"),
			MyLocale.getMsg(317, "Search"), MyLocale.getMsg(318, "Found"),
			MyLocale.getMsg(319, "Not Found"), MyLocale.getMsg(320, "Owner") },
			0);

	mButton btnNewWpt, btnShowBug, btnAddPicture, btnBlack, btnNotes, btnSave,
			btnCancel;
	mButton btnFoundDate, btnHiddenDate;
	CellPanel pnlTools = new CellPanel();
	mTextPad mNotes;

	mImage imgBlack;
	mImage imgBlackNo;
	mImage imgShowBug, imgShowBugNo, imgNewWpt, imgGoto;
	mImage imgShowMaps, imgAddImages, imgNotes;
	mLabel lblAddiCount;
	mButton btnTerr, btnDiff;

	CacheDB cacheDB;
	CacheHolder thisCache;
	int dbIndex = -1;

	AttributesViewer attV;

	private boolean dirty_notes = false;
	private boolean dirty_details = false;
	private boolean blackStatus = false;
	private boolean blackStatusChanged = false;
	private boolean needsTableUpdate = false;
	private boolean isBigScreen = false;

	Preferences pref; // Test
	Profile profile;

	public DetailsPanel() {

		// No Error Type here
		cacheTypeGuiStrings = new String[CacheType.values().length - 1];
		int j = 0;
		for (CacheType type : CacheType.values()) {
			if (type != CacheType.ERROR) {
				cacheTypeGuiStrings[j] = type.getGuiString();
				j++;
			}
		}
		chcType = new mChoice(cacheTypeGuiStrings, 0);

		pref = Global.getPref();
		profile = Global.getProfile();
		cacheDB = profile.cacheDB;

		// Initialize Size Array:
		String[] sizes = new String[CacheSize.values().length];
		for (int i = 0; i < sizes.length; i++) {
			sizes[i] = CacheSize.values()[i].getAsString();
		}
		chcSize = new mChoice(sizes, 0);

		// //////////////////
		// Tools
		// //////////////////
		// Use larger Button-Icons on VGA-mobiles
		int sw = MyLocale.getScreenWidth();
		String imagesize = "";
		if (Vm.isMobile() && sw >= 400)
			imagesize = "_vga";
		// Button 1: New Waypoint
		pnlTools.addNext(btnNewWpt = new mButton(imgNewWpt = new mImage(
				"newwpt" + imagesize + ".png")));
		btnNewWpt.setToolTip(MyLocale.getMsg(311, "Create Waypoint"));
		PenEvent.wantPenMoved(btnNewWpt, PenEvent.WANT_PEN_MOVED_ONOFF, true);
		imgNewWpt.transparentColor = new Color(255, 0, 0);
		// Button 3: Travelbugs
		imgShowBug = new mImage("bug" + imagesize + ".gif");
		imgShowBugNo = new mImage("bug_no" + imagesize + ".gif");
		pnlTools.addNext(btnShowBug = new mButton(imgShowBugNo));
		// btnShowBug.modify(Control.Disabled,0);
		btnShowBug.setToolTip(MyLocale.getMsg(346, "Show travelbugs"));
		// Button 5: Add images
		pnlTools.addNext(btnAddPicture = new mButton(imgAddImages = new mImage(
				"images" + imagesize + ".gif")));
		btnAddPicture.setToolTip(MyLocale.getMsg(348, "Add user pictures"));
		// Button 6: Toggle blacklist status
		imgBlackNo = new mImage("no_black" + imagesize + ".png");
		imgBlackNo.transparentColor = Color.Black;
		imgBlack = new mImage("is_black" + imagesize + ".png");
		imgBlack.transparentColor = Color.White;
		pnlTools.addNext(btnBlack = new mButton(imgBlackNo));
		btnBlack.setToolTip(MyLocale.getMsg(349, "Toggle Blacklist status"));
		// Button 7: Notes
		pnlTools.addNext(btnNotes = new mButton(imgNotes = new mImage("notes"
				+ imagesize + ".gif")));
		imgNotes.transparentColor = Color.DarkBlue;
		btnNotes.setToolTip(MyLocale.getMsg(351, "Add/Edit notes"));
		// Button 8: Date/time stamp
		pnlTools.addLast(btnAddDateTime = new mButton(new mImage("date_time"
				+ imagesize + ".gif")));
		btnAddDateTime.setToolTip(MyLocale
				.getMsg(350, "Add timestamp to notes"));
		// showMap.modify(Control.Disabled,0);
		pnlTools.stretchFirstRow = true;
		this.addLast(pnlTools, CellConstants.DONTSTRETCH, CellConstants.WEST)
				.setTag(SPAN, new Dimension(3, 1));

		// //////////////////
		// Main body of screen
		// //////////////////

		this.addNext(new mLabel(MyLocale.getMsg(300, "Type:")),
				CellConstants.DONTSTRETCH,
				(CellConstants.DONTFILL | CellConstants.NORTHWEST));
		CellPanel line1Panel = new CellPanel();
		chcType.alwaysDrop = true;
		line1Panel.addNext(chcType, CellConstants.HSTRETCH,
				(CellConstants.HFILL | CellConstants.WEST));

		line1Panel.addLast(btnDiff = new mButton(MyLocale.getMsg(1000, "D")
				+ ": 5.5"), CellConstants.DONTSTRETCH,
				(CellConstants.DONTFILL | CellConstants.EAST));
		btnDiff.setPreferredSize(pref.fontSize * 3, chcSize
				.getPreferredSize(null).height);

		this.addLast(line1Panel, DONTSTRETCH, HFILL).setTag(CellConstants.SPAN,
				new Dimension(2, 1));

		this.addNext(new mLabel(MyLocale.getMsg(301, "Size:")),
				CellConstants.DONTSTRETCH,
				(CellConstants.DONTFILL | CellConstants.WEST));
		CellPanel line2Panel = new CellPanel();
		chcSize.alwaysDrop = true;
		line2Panel.addNext(chcSize, CellConstants.HSTRETCH,
				(CellConstants.HFILL | CellConstants.WEST));

		line2Panel.addLast(btnTerr = new mButton(MyLocale.getMsg(1001, "T")
				+ ": 5.5"), CellConstants.DONTSTRETCH,
				(CellConstants.DONTFILL | CellConstants.EAST));
		btnTerr.setPreferredSize(pref.fontSize * 3, chcSize
				.getPreferredSize(null).height);
		this.addLast(line2Panel, DONTSTRETCH, HFILL).setTag(CellConstants.SPAN,
				new Dimension(2, 1));

		this.addNext(new mLabel(MyLocale.getMsg(302, "Waypoint:")),
				CellConstants.DONTSTRETCH,
				(CellConstants.DONTFILL | CellConstants.WEST));
		line2Panel = new CellPanel();
		line2Panel.addNext(inpWaypoint, CellConstants.HSTRETCH,
				(CellConstants.HFILL | CellConstants.WEST));
		line2Panel.addLast(lblAddiCount = new mLabel(MyLocale.getMsg(1044,
				"Addis")
				+ ": 888"), CellConstants.DONTSTRETCH,
				(CellConstants.DONTFILL | CellConstants.EAST));
		this.addLast(line2Panel, DONTSTRETCH, HFILL).setTag(CellConstants.SPAN,
				new Dimension(2, 1));

		this.addNext(new mLabel(MyLocale.getMsg(303, "Name:")),
				CellConstants.DONTSTRETCH,
				(CellConstants.DONTFILL | CellConstants.WEST));
		this.addLast(inpName.setTag(CellConstants.SPAN, new Dimension(2, 1)),
				CellConstants.DONTSTRETCH,
				(CellConstants.HFILL | CellConstants.WEST));

		this.addNext(new mLabel(MyLocale.getMsg(304, "Location:")),
				CellConstants.DONTSTRETCH,
				(CellConstants.DONTFILL | CellConstants.WEST));
		this.addLast(btnWayLoc.setTag(CellConstants.SPAN, new Dimension(2, 1)),
				CellConstants.HSTRETCH,
				(CellConstants.HFILL | CellConstants.WEST));

		this.addNext(new mLabel(MyLocale.getMsg(307, "Status:")),
				CellConstants.DONTSTRETCH,
				(CellConstants.DONTFILL | CellConstants.WEST));
		CellPanel cp = new CellPanel();
		cp.addNext(chcStatus, CellConstants.HSTRETCH,
				(CellConstants.HFILL | CellConstants.WEST));
		cp.addLast(btnFoundDate = new mButton(new mImage("calendar" + imagesize
				+ ".png")), DONTSTRETCH, DONTFILL);
		this.addLast(cp, DONTSTRETCH, HFILL).setTag(CellConstants.SPAN,
				new Dimension(2, 1));

		this.addNext(new mLabel(MyLocale.getMsg(306, "Owner:")),
				CellConstants.DONTSTRETCH,
				(CellConstants.DONTFILL | CellConstants.WEST));
		this.addLast(inpOwner.setTag(CellConstants.SPAN, new Dimension(2, 1)),
				CellConstants.DONTSTRETCH,
				(CellConstants.HFILL | CellConstants.WEST));

		this.addNext(new mLabel(MyLocale.getMsg(305, "Hidden on:")),
				CellConstants.DONTSTRETCH,
				(CellConstants.DONTFILL | CellConstants.WEST));
		CellPanel ip = new CellPanel();
		ip.addNext(inpHidden, CellConstants.HSTRETCH,
				(CellConstants.HFILL | CellConstants.WEST));
		ip.addLast(btnHiddenDate = new mButton(new mImage("calendar"
				+ imagesize + ".png")), DONTSTRETCH, DONTFILL);
		this.addLast(ip, DONTSTRETCH, HFILL).setTag(CellConstants.SPAN,
				new Dimension(2, 1));
		inpHidden.modifyAll(DisplayOnly, 0);

		attV = new AttributesViewer();
		this.addLast(attV);

		if ((MyLocale.getScreenWidth() >= 400)
				&& (MyLocale.getScreenHeight() >= 600)) {
			isBigScreen = true;
			this.addLast(new mLabel(MyLocale.getMsg(308, "Notes:")),
					CellConstants.DONTSTRETCH,
					(CellConstants.DONTFILL | CellConstants.WEST));
			mNotes = new mTextPad();
			mNotes.modify(ControlConstants.NotEditable, 0);
			this.addLast(new MyScrollBarPanel(mNotes));
		}
	}

	public void clear() {
		attV.clear();
	}

	public void setNeedsTableUpdate(boolean tableUpdate) {
		needsTableUpdate = tableUpdate;
	}

	public boolean needsTableUpdate() {
		return needsTableUpdate;
	}

	public boolean isDirty() {
		return dirty_notes || dirty_details || needsTableUpdate;
	}

	public boolean hasBlackStatusChanged() {
		return blackStatusChanged;
	}

	/**
	 * @param chD
	 *            details of the cache to display
	 * @param dbindex
	 *            index in cacheDB, in which changes will be saved
	 */
	public void setDetails(CacheHolder ch) {
		thisCache = ch;
		dirty_notes = false;
		dirty_details = false;
		inpWaypoint.setText(ch.getWayPoint());
		inpName.setText(ch.getCacheName());
		btnWayLoc.setText(ch.getPos().toString());
		inpHidden.setText(ch.getDateHidden());
		inpOwner.setText(ch.getCacheOwner());
		if (ch.getCacheStatus().length() >= 10
				&& ch.getCacheStatus().charAt(4) == '-') {
			chcStatus.setText(MyLocale.getMsg(318, "Found") + " "
					+ ch.getCacheStatus());
		} else {
			chcStatus.setText(ch.getCacheStatus());
			// If the cache status contains a date, do not overwrite it with
			// 'found' message
			if (ch.isFound() == true)
				chcStatus.setText(MyLocale.getMsg(318, "Found"));
		}
		chcType.setInt(ch.getType().ordinal());
		if (ch.isBlack()) {
			btnBlack.image = imgBlack;
		} else {
			btnBlack.image = imgBlackNo;
		}
		blackStatus = ch.isBlack();
		blackStatusChanged = false;
		btnBlack.repaintNow();
		if (inpWaypoint.getText().length() == 0)
			createWptName();
		if (ch.hasBugs() == true) {
			// btnShowBug.modify(Control.Disabled,1);
			btnShowBug.image = imgShowBug;
		} else {
			// btnShowBug.modify(Control.Disabled,0);
			btnShowBug.image = imgShowBugNo;
		}
		btnShowBug.repaintNow();
		chcSize.setInt(ch.getCacheSize().ordinal());

		attV.showImages(ch.getCacheDetails(true).getAttributes());
		if (ch.isAddiWpt() || ch.isCustomWpt()) {
			btnTerr.setText(MyLocale.getMsg(1001, "T") + ": -.-");
			btnDiff.setText(MyLocale.getMsg(1000, "D") + ": -.-");
			deactivateControl(btnTerr);
			deactivateControl(btnDiff);
			deactivateControl(chcSize);
			chcSize.select(0);
		} else {
			activateControl(btnTerr);
			activateControl(btnDiff);
			activateControl(chcSize);
			if (ch.getTerrain().isValid()) {
				btnTerr.setText(MyLocale.getMsg(1001, "T") + ": "
						+ ch.getTerrain().getFullRepresentation());
			} else {
				btnTerr.setText("T: -.-");
				ch.setIncomplete(true);
				if (Global.getPref().debug)
					Global.getPref().log(
							ch.getWayPoint() + " has wrong terrain "
									+ ch.getTerrain());
			}
			if (ch.getDifficulty().isValid()) {
				btnDiff.setText(MyLocale.getMsg(1000, "D") + ": "
						+ ch.getDifficulty().getFullRepresentation());
			} else {
				btnDiff.setText("D: -.-");
				ch.setIncomplete(true);
				if (Global.getPref().debug)
					Global.getPref().log(
							ch.getWayPoint() + " has wrong difficulty "
									+ ch.getDifficulty());
			}
		}
		int addiCount = 0;
		if (ch.getMainCache() == null) {
			addiCount = ch.getAddiWpts().size();
		} else {
			addiCount = ch.getMainCache().getAddiWpts().size();
		}
		lblAddiCount.setText(MyLocale.getMsg(1044, "Addis") + ": "
				+ String.valueOf(addiCount));

		if (isBigScreen)
			mNotes.setText(ch.getExistingDetails().getCacheNotes());
	}

	/**
	 * if is addi -> returns the respective AddiWpt if is main -> returns the
	 * respective MainWpt
	 */
	public void createWptName() {
		String wpt = inpWaypoint.getText().toUpperCase();
		if (CacheType.values()[chcType.getInt()].isAdditionalWaypoint()
				&& (Global.mainTab.mainCache.startsWith("GC")
						|| Global.mainTab.mainCache.startsWith("OC") || Global.mainTab.mainCache
						.startsWith("CW")) && wpt.startsWith("CW")) {
			// for what was this?:
			Global.mainTab.lastselected = Global.mainTab.mainCache; // I don't
			// know
			// exactly,
			// but it's
			// needed
			// for
			// creating
			// a series
			// of Addis

			inpWaypoint.setText(Global.getProfile().getNewAddiWayPointName(
					Global.mainTab.mainCache));
		}
		if (!CacheType.values()[chcType.getInt()].isAdditionalWaypoint()
				&& !(wpt.startsWith("GC") || wpt.startsWith("OC") || wpt
						.startsWith("CW"))) {
			inpWaypoint.setText(Global.getProfile().getNewWayPointName());
		}
	}

	/**
	 * Method to react to a user input.
	 */
	public void onEvent(Event ev) {
		if (ev instanceof DataChangeEvent) {
			if (ev.target == inpWaypoint) {
				// If user used lower case -> convert directly to upper case
				inpWaypoint.setText(inpWaypoint.getText().toUpperCase());
				// FIXME: if name was changed, we should rename the waypoint.xml
				// file. how? where?
			} else if (ev.target == chcType) {
				createWptName();
				if (CacheType.values()[chcType.getInt()].isCacheWaypoint()) {
					activateControl(btnTerr);
					activateControl(btnDiff);
					activateControl(chcSize);
				} else {
					deactivateControl(btnTerr);
					deactivateControl(btnDiff);
					deactivateControl(chcSize);
					chcSize.select(0);
					btnTerr.setText(MyLocale.getMsg(1001, "T") + ": -.-");
					btnDiff.setText(MyLocale.getMsg(1000, "D") + ": -.-");
				}
			}
			// FIXME: check if something was actually changed, since datacachnge
			// events also occur if you just hop through the fileds with the tab
			// key (Why? don't know!)
			dirty_details = true;
			needsTableUpdate = true;
		}
		if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
			if (ev.target == btnNotes) {
				dirty_notes = true; // TODO I think this is redundant, because
				// the notes are saved separately by the notes screen itself
				NotesScreen nsc = new NotesScreen(thisCache
						.getCacheDetails(true));
				nsc.execute(this.getFrame(), Gui.CENTER_FRAME);
				if (isBigScreen)
					mNotes.setText(thisCache.getCacheDetails(true)
							.getCacheNotes());
			} else if (ev.target == btnShowBug) {
				// InfoScreen is = new InfoScreen(thisCache.Travelbugs.toHtml(),
				// "Travelbugs",
				// false, pref);
				// is.execute();
				TravelbugInCacheScreen ts = new TravelbugInCacheScreen(
						thisCache.getCacheDetails(true).getTravelbugs()
								.toHtml(), "Travelbugs");
				ts.execute(this.getFrame(), Gui.CENTER_FRAME);
			} else if (ev.target == btnCenter) {
				CWPoint cp = new CWPoint(thisCache.getPos());
				if (!cp.isValid()) {
					MessageBox tmpMB = new MessageBox(
							MyLocale.getMsg(312, "Error"),
							MyLocale
									.getMsg(4111,
											"Coordinates must be entered in the format N DD MM.MMM E DDD MM.MMM"),
							FormBase.OKB);
					tmpMB.exec();
				} else {
					pref.setCurCenter(cp);
					Global.mainTab.updateBearDist();
				}
			} else if (ev.target == btnAddDateTime) {
				dirty_notes = true;
				String note = thisCache.getCacheDetails(true).getCacheNotes();
				Time dtm = new Time();
				dtm.getTime();
				dtm.setFormat("E dd.MM.yyyy '/' HH:mm");
				if (note.length() > 0)
					note = note + "\n" + dtm.toString();
				else
					note = note + dtm.toString();
				note = note + "\n";
				thisCache.getCacheDetails(true).setCacheNotes(note);
				// FIXME: better use saveDirtyWaypoint()?
				thisCache.save();
			} else if (ev.target == btnAddPicture) {

				ewe.io.File imgFile;
				String imgDesc, imgDestName;

				// Get Image and description
				FileChooser fc = new FileChooser(FileChooserBase.OPEN, profile
						.getDataDir().getAbsolutePath());
				fc.setTitle("Select image file:");
				if (fc.execute() != FormBase.IDCANCEL) {
					imgFile = fc.getChosenFile();
					imgDesc = new InputBox("Description").input("", 10);
					// Create Destination Filename
					String ext = imgFile.getFileExt().substring(
							imgFile.getFileExt().lastIndexOf("."));
					imgDestName = thisCache.getWayPoint()
							+ "_U_"
							+ (thisCache.getFreshDetails().getUserImages()
									.size() + 1) + ext;

					ImageInfo userImageInfo = new ImageInfo();
					userImageInfo.setFilename(imgDestName);
					userImageInfo.setTitle(imgDesc);
					thisCache.getFreshDetails().addUserImage(userImageInfo);

					// Copy File
					DataMover.copy(new File(imgFile.getFullPath()), new File(
							profile.getDataDir(), imgDestName));
					// Save Data
					CacheHolderDetailFactory.getInstance().saveCacheDetails(
							thisCache.getFreshDetails(),
							Global.getProfile().getDataDir());
				}
			} else if (ev.target == btnBlack) {
				if (thisCache.isBlack()) {
					thisCache.setBlack(false);
					btnBlack.image = imgBlackNo;
				} else {
					thisCache.setBlack(true);
					btnBlack.image = imgBlack;
				}
				blackStatus = thisCache.isBlack();
				thisCache.setAttributesToAddiWpts();
				btnBlack.repaintNow();
				dirty_details = true;
				blackStatusChanged = true;
			} else if (ev.target == btnNewWpt) {
				CacheHolder ch = new CacheHolder();
				ch.setPos(new CWPoint(thisCache.getPos()));
				ch.setType(CacheType.STAGE);
				ch.setDifficulty(Difficulty.DIFFICULTY_UNSET);
				ch.setTerrain(Terrain.TERRAIN_UNSET);
				ch.setCacheSize(CacheSize.NOT_CHOSEN);
				Global.mainTab.newWaypoint(ch);
			} else if (ev.target == btnWayLoc) {
				CWPoint coords = new CWPoint(thisCache.getPos());
				CoordsScreen cs = new CoordsScreen(true);
				cs.setFields(coords, CWPoint.CW);
				if (cs.execute() == FormBase.IDOK) {
					dirty_details = true;
					coords = cs.getCoords();
					Global.getProfile().notifyUnsavedChanges(
							!thisCache.getPos().toString().equals(
									coords.toString()));
					thisCache.setPos(coords);
					btnWayLoc.setText(coords.toString());
					// If the current centre is valid, calculate the distance
					// and bearing to it
					CWPoint centre = Global.getPref().getCurCenter();
					if (centre.isValid())
						thisCache.calcDistance(centre);
				}
			} else if (ev.target == btnFoundDate) {
				// DateChooser.dayFirst=true;
				DateTimeChooser dc = new DateTimeChooser(Vm.getLocale());
				dc.title = MyLocale.getMsg(328, "Date found");
				dc.setPreferredSize(240, 240);
				String foundDate = chcStatus.getText();
				if (foundDate.startsWith(MyLocale.getMsg(318, "Found") + " "))
					foundDate = foundDate.substring(MyLocale.getMsg(318,
							"Found").length() + 1);
				Time t = new Time();
				try {
					t.parse(foundDate, "y-M-d H:m");
				} catch (IllegalArgumentException e) {
					try {
						t.parse(foundDate, "y-M-d");
					} catch (IllegalArgumentException e1) {
						// No parsable date given - should not appear
					}
				}

				dc.reset(t);
				if (dc.execute() == ewe.ui.FormBase.IDOK) {
					chcStatus
							.setText(MyLocale.getMsg(318, "Found") + " "
									+ Convert.toString(dc.year) + "-"
									+ MyLocale.formatLong(dc.month, "00") + "-"
									+ MyLocale.formatLong(dc.day, "00") + " "
									+ dc.time);
					dirty_details = true;
					// profile.hasUnsavedChanges=true;
				}
			} else if (ev.target == btnHiddenDate) {
				DateChooser.dayFirst = true;
				DateChooser dc = new DateChooser(Vm.getLocale());
				dc.title = MyLocale.getMsg(329, "Hidden date");
				dc.setPreferredSize(240, 240);
				if (inpHidden.getText().length() == 10)
					try {
						dc.setDate(new Time(Convert.parseInt(inpHidden
								.getText().substring(8)), Convert
								.parseInt(inpHidden.getText().substring(5, 7)),
								Convert.parseInt(inpHidden.getText().substring(
										0, 4))));
					} catch (NumberFormatException e) {
						dc.reset(new Time());
					}
				if (dc.execute() == ewe.ui.FormBase.IDOK) {
					inpHidden.setText(Convert.toString(dc.year) + "-"
							+ MyLocale.formatLong(dc.month, "00") + "-"
							+ MyLocale.formatLong(dc.day, "00"));
					dirty_details = true;
					// profile.hasUnsavedChanges=true;
				}
			} else if (ev.target == this.btnTerr) {
				int returnValue;
				TerrainForm tf = new TerrainForm(thisCache.getTerrain());
				returnValue = tf.execute();
				if (returnValue == 1
						&& tf.getTerrain() != thisCache.getTerrain()) {
					// FIXME: do this when waypoint is checked for saving
					thisCache.setTerrain(tf.getTerrain());
					btnTerr.setText(MyLocale.getMsg(1001, "T") + ": "
							+ thisCache.getTerrain().getFullRepresentation());
					dirty_details = true;
				}
			} else if (ev.target == this.btnDiff) {
				int returnValue;
				DifficultyForm df = new DifficultyForm(thisCache
						.getDifficulty());
				returnValue = df.execute();
				if (returnValue == 1
						&& df.getDifficulty() != thisCache.getDifficulty()) {
					// FIXME: do this when waypoint is checked for saving
					thisCache.setDifficulty(df.getDifficulty());
					btnDiff
							.setText(MyLocale.getMsg(1000, "D")
									+ ": "
									+ thisCache.getDifficulty()
											.getFullRepresentation());
					dirty_details = true;
				}
			}
			ev.consumed = true;
		}
	}

	/** allow user input on control item */
	private void activateControl(Control ctrl) {
		if (ctrl.change(0, ControlConstants.Disabled))
			ctrl.repaint();
	}

	/** block user input on control item */
	private void deactivateControl(Control ctrl) {
		if (ctrl.change(ControlConstants.Disabled, 0))
			ctrl.repaint();
	}

	public void saveDirtyWaypoint() {
		// FIXME: here we should check if the data is now different from what it
		// used to be when calling the details panel instead of relying on dirty
		// flags
		// FIXME: take care of renaming waypoints
		// FIXME: add method to convert back text of difficulty & terrain
		// buttons
		// FIXME: check if manual changes have converted a cache from incomplete
		// to complete

		// We have to update two objects: thisCache (a CacheHolderDetail) which
		// contains
		// the full cache which will be written to the cache.xml file AND
		// the CacheHolder object which sits in cacheDB
		// Strip the found message if the status contains a date
		if (chcStatus.getText().startsWith(MyLocale.getMsg(318, "Found"))
				&& chcStatus.getText().length() >= MyLocale
						.getMsg(318, "Found").length() + 11) {
			thisCache.setCacheStatus(chcStatus.getText().substring(
					MyLocale.getMsg(318, "Found").length() + 1));
		} else {
			thisCache.setCacheStatus(chcStatus.getText());
		}
		if (!thisCache.isFound() && thisCache.getCacheStatus().length() >= 10
				&& thisCache.getCacheStatus().charAt(4) == '-') {
			// Use same heuristic condition as in setDetails(CacheHolder) to
			// determine, if this
			// cache
			// has to considered as found.
			thisCache.setFound(true);
		} else {
			thisCache.setFound(chcStatus.getText().startsWith(
					MyLocale.getMsg(318, "Found")));
		}
		thisCache.setCacheOwner(inpOwner.getText().trim());
		thisCache.setOwned(thisCache.getCacheStatus().equals(
				MyLocale.getMsg(320, "Owner")));
		// Avoid setting is_owned if alias is empty and username is empty
		if (thisCache.isOwned() == false) {
			thisCache.setOwned(pref.isMyAlias(thisCache.getCacheOwner()));
		}
		thisCache.setBlack(blackStatus);
		String oldWaypoint = thisCache.getWayPoint();
		thisCache.setWayPoint(inpWaypoint.getText().toUpperCase().trim());
		// ToDo, geht auch einfach nur getInt() ??
		thisCache.setCacheSize(CacheSize.fromNormalStringRepresentation(chcSize
				.getText()));
		// If the waypoint does not have a name, give it one
		if (thisCache.getWayPoint().equals("")) {
			thisCache.setWayPoint(profile.getNewWayPointName());
		}
		// Don't allow single letter names=> Problems in updateBearingDistance
		// This is a hack but faster than slowing down the loop in
		// updateBearingDistance
		if (thisCache.getWayPoint().length() < 2)
			thisCache.setWayPoint(thisCache.getWayPoint() + " ");
		thisCache.setCacheName(inpName.getText().trim());
		thisCache.setDateHidden(inpHidden.getText().trim());
		CacheType oldType = thisCache.getType();
		thisCache.setType(CacheType.values()[chcType.getInt()]);
		// thisCache.saveCacheDetails(profile.dataDir); // this is redundant,
		// because all changes
		// affecting the details are immediately saved
		// Now update the table
		CacheHolder ch = thisCache; // TODO variable ch is redundant

		/*
		 * The references have to be rebuilt if: - the cachetype changed from
		 * addi->normal or normal->addi - the old cachetype or the new cachetype
		 * were 'addi' and the waypointname has changed
		 */
		if (ch.getType().isAdditionalWaypoint() != oldType
				.isAdditionalWaypoint()
				|| ((ch.getType().isAdditionalWaypoint() || oldType
						.isAdditionalWaypoint()) && !thisCache.getWayPoint()
						.equals(oldWaypoint))) {
			// If we changed the type to addi, check that a parent exists
			if (ch.getType().isAdditionalWaypoint()) {
				int idx;
				if (ch.getWayPoint().length() < 5)
					idx = -1;
				else {
					idx = profile.getCacheIndex("GC"
							+ ch.getWayPoint().substring(
									ch.getWayPoint().length() == 5 ? 1 : 2));
					if (idx < 0)
						idx = profile
								.getCacheIndex("OC"
										+ ch
												.getWayPoint()
												.substring(
														ch.getWayPoint()
																.length() == 5 ? 1
																: 2));
					if (idx < 0)
						idx = profile
								.getCacheIndex("CW"
										+ ch
												.getWayPoint()
												.substring(
														ch.getWayPoint()
																.length() == 5 ? 1
																: 2));
					if (idx < 0)
						(new MessageBox(
								MyLocale.getMsg(144, "Warning"),
								MyLocale
										.getMsg(734,
												"No main cache found for addi waypoint ")
										+ " "
										+ ch.getWayPoint()
										+ "\n"
										+ MyLocale
												.getMsg(
														735,
														"Addi Waypoints must have the format xxYYYY, where xx are any 2 chars and YYYY are the main cache's chars after the GC"),
								FormBase.OKB)).execute();
				}
				profile.buildReferences(); // TODO this takes quite long -> use
				// profile.setAddiRef
				// instead
			} else {
				profile.buildReferences(); // we have to do this to release the
				// link between the
				// two caches
			}
		}
		// set status also on addi wpts
		ch.setAttributesToAddiWpts();
		ch.checkIncomplete();
		dirty_notes = false;
		dirty_details = false;
		setNeedsTableUpdate(false);
		thisCache.getFreshDetails().setUnsavedChanges(true);
	}

	private class TravelbugInCacheScreen extends Form {

		private DispPanel disp = new DispPanel();
		private mButton btCancel;
		private TravelbugJourneyList tbjList;

		TravelbugInCacheScreen(String text, String title) {
			this.setTitle(title);
			this.setPreferredSize(pref.myAppWidth, pref.myAppHeight);
			disp.setHtml(text);
			ScrollBarPanel sbp = new MyScrollBarPanel(disp,
					ScrollablePanel.NeverShowHorizontalScrollers);
			this.addLast(sbp);
			this.addLast(
					btCancel = new mButton(MyLocale.getMsg(3000, "Close")),
					CellConstants.DONTSTRETCH, CellConstants.FILL);
		}

		public void onEvent(Event ev) {
			if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
				if (ev.target == btCancel) {
					this.close(0);
				}
			}
		}

		// Subclassed HtmlDisplay with Pop-up menu
		private class DispPanel extends HtmlDisplay {
			MenuItem mnuPickupTB, mnuDropTB;
			MenuItem[] TBMenuItems = new MenuItem[2];
			Menu mnuPopup;

			DispPanel() {
				TBMenuItems[0] = mnuPickupTB = new MenuItem(MyLocale.getMsg(
						6016, "Pick up Travelbug"));
				TBMenuItems[1] = mnuDropTB = new MenuItem(MyLocale.getMsg(6017,
						"Drop Travelbug"));
				mnuPopup = new Menu(TBMenuItems, "");
			}

			public void penRightReleased(Point p) {
				setMenu(mnuPopup);
				doShowMenu(p); // direct call (not through doMenu) is neccesary
				// because it will
				// exclude the whole table
			}

			public void penHeld(Point p) {
				setMenu(mnuPopup);
				doShowMenu(p);
			}

			public void popupMenuEvent(Object selectedItem) {
				if (selectedItem == mnuPickupTB) {
					Travelbug tb = TravelbugPickup.pickupTravelbug(thisCache
							.getCacheDetails(true).getTravelbugs());
					if (tb != null) {
						dirty_details = true;
						// Get the list of my travelbugs
						tbjList = new TravelbugJourneyList();
						tbjList.readTravelbugsFile();
						// Add the tb to this list
						tbjList.addTbPickup(tb, Global.getProfile().name,
								thisCache.getWayPoint());
						tbjList.saveTravelbugsFile();
						tbjList = null;
						setHtml(thisCache.getCacheDetails(true).getTravelbugs()
								.toHtml());
						repaint();
						thisCache.setHasBugs(thisCache.getCacheDetails(true)
								.getTravelbugs().size() > 0);
					}
				} else if (selectedItem == mnuDropTB) {
					tbjList = new TravelbugJourneyList();
					tbjList.readTravelbugsFile();
					TravelbugList tbl = tbjList.getMyTravelbugs();
					TravelbugScreen tbs = new TravelbugScreen(tbl, MyLocale
							.getMsg(6017, "Drop a travelbug"), false);
					tbs.execute();
					if (tbs.selectedItem >= 0) {
						Travelbug tb = tbl.getTB(tbs.selectedItem);
						thisCache.getCacheDetails(true).getTravelbugs().add(tb);
						tbjList.addTbDrop(tb, Global.getProfile().name,
								thisCache.getWayPoint());
					}
					tbjList.saveTravelbugsFile();
					tbjList = null;
					thisCache.setHasBugs(thisCache.getCacheDetails(true)
							.getTravelbugs().size() > 0);
					setHtml(thisCache.getCacheDetails(true).getTravelbugs()
							.toHtml());
					repaint();
					dirty_details = true;
				} else
					super.popupMenuEvent(selectedItem);
			}
		}
	}

	private class TerrainForm extends Form {
		private mChoice mcDT;
		private mButton btnOk, btnCancel;
		private String[] terrainStrings = new String[] { "1.0", "1.5", "2.0",
				"2.5", "3.0", "3.5", "4.0", "4.5", "5.0" };

		public TerrainForm(Terrain terrain) {
			mcDT = new mChoice(terrainStrings, (terrain.isValid()) ? (terrain
					.getOldCWValue() - 10) / 5 : 0);
			btnOk = new mButton(MyLocale.getMsg(1605, "OK"));
			btnCancel = new mButton(MyLocale.getMsg(1604, "Cancel"));

			resizable = false;
			setTitle(MyLocale.getMsg(31415, "D & T"));

			addNext(new mLabel(MyLocale.getMsg(31415, "Terrain")));
			addLast(mcDT);
			addButton(btnOk);
			addButton(btnCancel);
		}

		public void onEvent(Event ev) {
			if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
				if (ev.target == btnCancel) {
					close(-1);
				} else if (ev.target == btnOk) {
					close(1);
				}
			}
		}

		public Terrain getTerrain() {
			return Terrain.fromOldCWByte((byte) (mcDT.selectedIndex * 5 + 10));
		}
	}

	private class DifficultyForm extends Form {
		private mChoice mcDT;
		private mButton btnOk, btnCancel;
		private String[] terrainStrings = new String[] { "1.0", "1.5", "2.0",
				"2.5", "3.0", "3.5", "4.0", "4.5", "5.0" };

		public DifficultyForm(Difficulty difficulty) {
			mcDT = new mChoice(
					terrainStrings,
					(difficulty.isValid()) ? (difficulty.getOldCWValue() - 10) / 5
							: 0);
			btnOk = new mButton(MyLocale.getMsg(1605, "OK"));
			btnCancel = new mButton(MyLocale.getMsg(1604, "Cancel"));

			resizable = false;
			setTitle(MyLocale.getMsg(31415, "D & T"));

			addNext(new mLabel(MyLocale.getMsg(31415, "Difficulty")));
			addLast(mcDT);
			addButton(btnOk);
			addButton(btnCancel);
		}

		public void onEvent(Event ev) {
			if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
				if (ev.target == btnCancel) {
					close(-1);
				} else if (ev.target == btnOk) {
					close(1);
				}
			}
		}

		public Difficulty getDifficulty() {
			return Difficulty
					.fromOldCWByte((byte) (mcDT.selectedIndex * 5 + 10));
		}
	}

}
