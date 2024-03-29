package CacheWolf.gui;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import CacheWolf.Global;
import CacheWolf.beans.CWPoint;
import CacheWolf.beans.CacheDB;
import CacheWolf.beans.CacheHolder;
import CacheWolf.beans.Preferences;
import CacheWolf.beans.Profile;
import CacheWolf.util.DataMover;
import CacheWolf.util.MyLocale;
import CacheWolf.util.ShowCacheInBrowser;
import de.cachehound.beans.CacheHolderDetail;
import ewe.fx.IconAndText;
import ewe.fx.Point;
import ewe.fx.Rect;
import ewe.sys.Handle;
import ewe.sys.Vm;
import ewe.ui.Control;
import ewe.ui.DragContext;
import ewe.ui.Event;
import ewe.ui.FormBase;
import ewe.ui.Gui;
import ewe.ui.IKeys;
import ewe.ui.KeyEvent;
import ewe.ui.Menu;
import ewe.ui.MenuItem;
import ewe.ui.MessageBox;
import ewe.ui.PenEvent;
import ewe.ui.ProgressBarForm;
import ewe.ui.TableControl;
import ewe.ui.TableEvent;
import ewe.ui.mList;

/**
 * Implements the user interaction of the list view. Works together with
 * myTableModel and TablePanel
 */
public class myTableControl extends TableControl {

	public Preferences pref;
	public Profile profile;
	public CacheDB cacheDB;
	public TablePanel tbp;

	private MenuItem miOpen, miCenter, miUnhideAddis;
	private MenuItem miOpenOnline, miOpenOffline;
	private MenuItem miDelete, miUpdate;
	private MenuItem miTickAll, miUntickAll;
	private MenuItem miSeparator;

	private Menu mFull;
	private Menu mSmall;

	myTableControl(TablePanel tablePanel) {
		profile = Global.getProfile();
		cacheDB = profile.cacheDB;
		pref = Global.getPref();
		tbp = tablePanel;
		allowDragSelection = false; // allow only one row to be selected at one
		// time

		MenuItem[] mnuFull = new MenuItem[12];
		mnuFull[0] = miOpen = new MenuItem(MyLocale.getMsg(1021,
				"Open description"));
		mnuFull[1] = miCenter = new MenuItem(MyLocale.getMsg(1019, "Center"));
		mnuFull[2] = miUnhideAddis = new MenuItem(MyLocale.getMsg(1042,
				"Unhide Addis"));
		mnuFull[3] = miSeparator = new MenuItem("-");
		mnuFull[4] = miOpenOnline = new MenuItem(MyLocale.getMsg(1020,
				"Open in $browser online"));
		mnuFull[5] = miOpenOffline = new MenuItem(MyLocale.getMsg(1018,
				"Open in browser offline"));
		mnuFull[6] = miSeparator;
		mnuFull[7] = miDelete = new MenuItem(MyLocale.getMsg(1012,
				"Delete selected"));
		mnuFull[8] = miUpdate = new MenuItem(MyLocale.getMsg(1014, "Update"));
		mnuFull[9] = miSeparator;
		mnuFull[10] = miTickAll = new MenuItem(MyLocale.getMsg(1015,
				"Select all"));
		mnuFull[11] = miUntickAll = new MenuItem(MyLocale.getMsg(1016,
				"De-select all"));
		mFull = new Menu(mnuFull, MyLocale.getMsg(1013, "With selection"));

		MenuItem[] mnuSmall = new MenuItem[6];
		mnuSmall[0] = miOpen;
		mnuSmall[1] = miCenter;
		mnuSmall[2] = miUnhideAddis;
		mnuSmall[3] = miSeparator;
		mnuSmall[4] = miOpenOnline;
		mnuSmall[5] = miOpenOffline;
		mSmall = new Menu(mnuSmall, MyLocale.getMsg(1013, "With selection"));
	}

	/** Full menu when listview includes checkbox */
	public void setMenuFull() {
		setMenu(mFull);
		// if (!Vm.getPlatform().equals("Win32") &&
		// !Vm.getPlatform().equals("Java"))
		// ((MenuItem)mFull.items.get(5)).modifiers|=MenuItem.Disabled;
	}

	public Menu getMenuFull() {
		return mFull;
	}

	/** Small menu when listview does not include checkbox */
	public void setMenuSmall() {
		setMenu(mSmall);
		// if (!Vm.getPlatform().equals("Win32") &&
		// !Vm.getPlatform().equals("Java"))
		// ((MenuItem)mSmall.items.get(5)).modifiers|=MenuItem.Disabled;
	}

	public void penRightReleased(Point p) {
		if (cacheDB.size() > 0) { // No context menu when DB is empty
			adjustAddiHideUnhideMenu();
			menuState.doShowMenu(p, true, null); // direct call (not through
			// doMenu) is neccesary
			// because it will exclude
			// the whole table

		}
	}

	public void penHeld(Point p) {
		if (cacheDB.size() > 0) // No context menu when DB is empty
			adjustAddiHideUnhideMenu();
		menuState.doShowMenu(p, true, null);
	}

	public void onKeyEvent(KeyEvent ev) {
		if (ev.type == KeyEvent.KEY_PRESS && ev.target == this) {
			if ((ev.modifiers & IKeys.CONTROL) > 0 && ev.key == 1) { // <ctrl-a>
				// gives
				// 1,
				// <ctrl-b>
				// == 2
				// select all on <ctrl-a>
				setSelectForAll(true);
				ev.consumed = true;
			} else {
				Global.mainTab.clearDetails();
				if (ev.key == IKeys.HOME)
					Global.mainTab.tbP.selectRow(0); // cursorTo(0,cursor.x+listMode,true);
				else if (ev.key == IKeys.END)
					Global.mainTab.tbP.selectRow(model.numRows - 1); // cursorTo(model.numRows-1,cursor.x+listMode,true);
				else if (ev.key == IKeys.PAGE_DOWN)
					Global.mainTab.tbP.selectRow(java.lang.Math.min(cursor.y
							+ getOnScreen(null).height - 1, model.numRows - 1)); // cursorTo(java.lang.Math.min(cursor.y+
				// getOnScreen(null).height-1,
				// model.numRows-1),cursor.x+listMode,true);
				// //
				// I
				// don't
				// know
				// why
				// this
				// doesn't
				// work:
				// tbp.doScroll(IScroll.Vertical,
				// IScroll.PageHigher,
				// 1);
				else if (ev.key == IKeys.PAGE_UP)
					Global.mainTab.tbP.selectRow(java.lang.Math.max(cursor.y
							- getOnScreen(null).height + 1, 0)); // cursorTo(java.lang.Math.max(cursor.y-getOnScreen(null).height+1,
				// 0),cursor.x+listMode,true);
				else if (ev.key == IKeys.ACTION || ev.key == IKeys.ENTER)
					Global.mainTab.select(Global.mainTab.descP);
				else if (ev.key == IKeys.DOWN)
					Global.mainTab.tbP.selectRow(java.lang.Math.min(
							cursor.y + 1, model.numRows - 1));
				else if (ev.key == IKeys.UP)
					Global.mainTab.tbP.selectRow(java.lang.Math.max(
							cursor.y - 1, 0));
				else if (ev.key == IKeys.LEFT
						&& Global.mainForm.cacheListVisible && cursor.y >= 0
						&& cursor.y < tbp.myMod.numRows)
					Global.mainForm.cacheList.addCache(cacheDB.get(cursor.y)
							.getWayPoint());
				else if (ev.key == IKeys.RIGHT) {
					CacheHolder ch;
					ch = cacheDB.get(tbp.getSelectedCache());
				} else if (ev.key == 6)
					MainMenu.search(); // (char)6 == ctrl + f
				else
					super.onKeyEvent(ev);
			}
		} else
			super.onKeyEvent(ev);
	}

	/**
	 * Set all caches either as selected or as deselected, depending on argument
	 */
	private void setSelectForAll(boolean selectStatus) {
		Global.getProfile().setSelectForAll(selectStatus);
		tbp.refreshTable();
	}

	/** always select a whole row */
	public boolean isSelected(int pRow, int pCol) {
		return pRow == selection.y;
	}

	public void popupMenuEvent(Object selectedItem) {
		if (selectedItem == null)
			return;
		CacheHolder ch;
		if (selectedItem == miTickAll) {
			setSelectForAll(true);
		} else

		if (selectedItem == miUntickAll) {
			setSelectForAll(false);
		} else

		if (selectedItem == miDelete) {
			Vm.showWait(true);
			// Count # of caches to delete
			int allCount = 0;
			int mainNonVisibleCount = 0;
			int addiNonVisibleCount = 0;
			int shouldDeleteCount = 0;
			boolean deleteFiltered = true; // Bisheriges Verhalten
			for (int i = cacheDB.size() - 1; i >= 0; i--) {
				CacheHolder currCache = cacheDB.get(i);
				if (currCache.isChecked()) {
					allCount++;
					if (!currCache.isVisible()) {
						if (currCache.isAddiWpt()) {
							addiNonVisibleCount++;
						} else {
							mainNonVisibleCount++;
						}
					}
				}
			}
			// Warn if there are ticked but invisible caches - and ask if they
			// should be deleted,
			// too.
			shouldDeleteCount = allCount;
			if (addiNonVisibleCount + mainNonVisibleCount > 0) {
				if ((new MessageBox(
						MyLocale.getMsg(144, "Warning"),
						MyLocale
								.getMsg(1029,
										"There are caches that are ticked but invisible.\n(Main caches: ")
								+ mainNonVisibleCount
								+ MyLocale.getMsg(1030,
										", additional Waypoints: ")
								+ addiNonVisibleCount
								+ ")\n"
								+ MyLocale.getMsg(1031, "Delete them, too?"),
						FormBase.YESB | FormBase.NOB)).execute() == FormBase.IDYES) {
					deleteFiltered = true;
				} else {
					deleteFiltered = false;
					shouldDeleteCount = allCount - mainNonVisibleCount
							- addiNonVisibleCount;
				}
			}
			if (shouldDeleteCount > 0) {
				if ((new MessageBox(MyLocale.getMsg(144, "Warning"), MyLocale
						.getMsg(1022, "Delete selected caches (")
						+ shouldDeleteCount + MyLocale.getMsg(1028, ") ?"),
						FormBase.YESB | FormBase.NOB)).execute() == FormBase.IDYES) {
					DataMover dm = new DataMover();
					myProgressBarForm pbf = new myProgressBarForm();
					Handle h = new Handle();
					pbf.setTask(h, MyLocale.getMsg(1012, "Delete selected"));
					pbf.exec();
					int nDeleted = 0;
					int size = cacheDB.size();
					for (int i = size - 1; i >= 0; i--) {// Start Counting
						// down,
						// as the size
						// decreases with
						// each deleted
						// cache
						ch = cacheDB.get(i);
						if (ch.isChecked()
								&& (ch.isVisible() || deleteFiltered)) {
							nDeleted++;
							h.progress = ((float) nDeleted) / (float) allCount;
							h.changed();
							cacheDB.removeElementAt(i);
							dm.deleteCacheFiles(ch.getWayPoint(), profile
									.getDataDir());
							ch = null;
							if (pbf.isClosed)
								break;
						}
					}
					pbf.exit(0);
					tbp.myMod.numRows -= nDeleted;
					profile.saveIndex(pref, true);
					tbp.refreshTable();
				}
			}
			Vm.showWait(false);
		} else

		if (selectedItem == miUpdate) {
			MainMenu.updateSelectedCaches(tbp);
		} else

		if (selectedItem == miCenter) {
			if (tbp.getSelectedCache() < 0) {
				Global.getPref().log("popupMenuEvent: getSelectedCache() < 0");
				return;
			}
			CacheHolder thisCache = cacheDB.get(tbp.getSelectedCache());
			CWPoint cp = new CWPoint(thisCache.getPos());
			if (!cp.isValid()) {
				MessageBox tmpMB = new MessageBox(
						MyLocale.getMsg(321, "Error"),
						MyLocale
								.getMsg(4111,
										"Coordinates must be entered in the format N DD MM.MMM E DDD MM.MMM"),
						FormBase.OKB);
				tmpMB.execute();
			} else {
				pref.setCurCenter(cp);
				Global.mainTab.updateBearDist(); // Update the distances with
				// a
				// warning message
				// tbp.refreshTable();
			}
		} else

		if (selectedItem == miUnhideAddis) {
			// This toggles the "showAddis" Flag
			ch = cacheDB.get(tbp.getSelectedCache());
			ch.setShowAddis(!ch.showAddis());
			if (ch.getAddiWpts().size() > 0) {
				tbp.refreshTable();
			} else {
				// This should never occur, as we check prior to activating the
				// menu if the
				// cache has addis. But just in case...
				new MessageBox(MyLocale.getMsg(4201, "Info"), MyLocale.getMsg(
						1043, "This cache has no additional waypoints."),
						FormBase.OKB).execute();
			}
		} else

		if (selectedItem == miOpenOnline) {
			ch = cacheDB.get(tbp.getSelectedCache());
			CacheHolderDetail chD = ch.getCacheDetails(false, true);
			if (chD != null) {

				if (Desktop.isDesktopSupported()) {
					try {
						Desktop.getDesktop().browse(new URI(chD.getUrl()));
					} catch (IOException e) {
						Global.getPref().log(
								"Fehler beim Aufrufen des Browsers.", e);
					} catch (URISyntaxException e) {
						Global.getPref().log(
								"Fehler beim Aufrufen des Browsers.", e);
					}

				} else {
					Global
							.getPref()
							.log(
									"Das System unterstützt das Java Feature 'Desktop' nicht");
				}
			}
		} else

		if (selectedItem == miOpenOffline) {
			ShowCacheInBrowser sc = new ShowCacheInBrowser();
			sc.showCache(cacheDB.get(tbp.getSelectedCache()));
		} else

		if (selectedItem == miOpen) {
			penDoubleClicked(null);
		}

	}

	public void penDoubleClicked(Point where) {
		Global.mainTab.select(Global.mainTab.descP);
	}

	public void onEvent(Event ev) {
		if (ev instanceof PenEvent && (ev.type == PenEvent.PEN_DOWN)) {
			Global.mainTab.tbP.myMod.penEventModifiers = ((PenEvent) ev).modifiers;
		}

		super.onEvent(ev);
	}

	/**
	 * Adjusting the menu item for hiding or unhiding additional waypoints. If
	 * the cache has no addis, then the menu is deactivated. If it has addis,
	 * then the menu text is adapted according to the current value of the
	 * property <code>showAddis()</code>.
	 * 
	 */
	public void adjustAddiHideUnhideMenu() {
		if (tbp.getSelectedCache() < 0) {
			return;
		}
		CacheHolder selCache = cacheDB.get(tbp.getSelectedCache());
		if (selCache != null) {
			// Depending if it has Addis and the ShowAddis-Flag the menu item to
			// unhide
			// addis is properly named and activated or disabled.
			if (selCache.getAddiWpts().size() > 0) {
				miUnhideAddis.modifiers &= ~MenuItem.Disabled;
				if (!selCache.showAddis()) {
					miUnhideAddis
							.setText(MyLocale.getMsg(1042, "Unhide Addis"));
				} else {
					miUnhideAddis.setText(MyLocale.getMsg(1045, "Hide Addis"));
				}
			} else {
				miUnhideAddis.setText(MyLocale.getMsg(1042, "Unhide Addis"));
				miUnhideAddis.modifiers |= MenuItem.Disabled;
			}
		}
	}

	// /////////////////////////////////////////////////
	// Allow the caches to be dragged into a cachelist
	// /////////////////////////////////////////////////

	IconAndText imgDrag;
	String wayPoint;
	int row;

	public void startDragging(DragContext dc) {
		Point p = cellAtPoint(dc.start.x, dc.start.y, null);
		wayPoint = null;
		if (p.y >= 0) {
			if (!Global.mainForm.cacheListVisible) {
				dc.cancelled = true;
				return;
			}
			row = p.y;
			CacheHolder ch = cacheDB.get(p.y);
			wayPoint = ch.getWayPoint();
			// Vm.debug("Waypoint : "+ch.wayPoint);
			imgDrag = new IconAndText();
			imgDrag.addColumn(GuiImageBroker.getInstance().getTypeImage(
					ch.getType()));
			imgDrag.addColumn(ch.getWayPoint());
			dc.dragData = dc.startImageDrag(imgDrag, new Point(8, 8), this);
		} else
			super.startDragging(dc);
	}

	public void stopDragging(DragContext dc) {
		if (wayPoint != null && !dc.cancelled) {
			// Vm.debug("Stop Dragging"+dc.curPoint.x+"/"+dc.curPoint.y);
			dc.stopImageDrag(true);
			Point p = Gui.getPosInParent(this, getWindow());
			p.x += dc.curPoint.x;
			p.y += dc.curPoint.y;
			Control c = getWindow().findChild(p.x, p.y);
			if (c instanceof mList && c.text.equals("CacheList")) {
				if (Global.mainForm.cacheList.addCache(wayPoint)) {
					c.repaintNow();
					((mList) c).makeItemVisible(((mList) c).itemsSize() - 1);
				}
			}
			Global.mainTab.tbP.selectRow(row);
			// Vm.debug("Control "+c.toString()+"/"+c.text);
		} else
			super.stopDragging(dc);
	}

	public void dragged(DragContext dc) {
		if (wayPoint != null)
			dc.imageDrag();
		else
			super.dragged(dc);
	}

	public void cursorTo(int pRow, int pCol, boolean selectNew) {
		if (pRow != -2 && pCol != -2 && !canSelect(pRow, pCol))
			return;
		cursor.set(pCol, pRow);
		if (selectNew) {
			clearSelectedCells(oldExtendedSelection);
			paintCells(null, oldExtendedSelection);
			if (pRow != -2 && pCol != -2) {
				if (scrollToVisible(pRow, pCol))
					repaintNow();
				addToSelection(Rect.buff.set(0, pRow, model.numCols, 1), true);
				// fireSelectionEvent(TableEvent.FLAG_SELECTED_BY_ARROWKEY);
				clickedFlags = TableEvent.FLAG_SELECTED_BY_ARROWKEY;
				if (clickMode)
					clicked(pRow, pCol);
				clickedFlags = 0;
			}
		}
	}

	/**
	 * this is only necessary to hinder the user to unselect
	 */
	public void penReleased(Point p, boolean isDouble) {
		Point p2 = cellAtPoint(p.x, p.y, null);
		super.penReleased(p, isDouble);
		Rect sel = getSelection(null);
		if ((sel.height == 0 || sel.height == 0) && p2 != null)
			cursorTo(p2.y, p2.x, true); // if the selection is gone -> reselect
		// it

	}

	class myProgressBarForm extends ProgressBarForm {

		boolean isClosed = false;

		protected boolean canExit(int exitCode) {
			isClosed = true;
			return true;
		}

	}

	public Menu getMenuSmall() {
		return mSmall;
	}
}
