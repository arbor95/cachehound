package CacheWolf.gui;

import CacheWolf.Global;
import CacheWolf.beans.CacheDB;
import CacheWolf.beans.DBStats;
import CacheWolf.beans.Filter;
import CacheWolf.beans.Preferences;
import CacheWolf.util.MyLocale;
import ewe.fx.Color;
import ewe.fx.mImage;
import ewe.sys.Vm;
import ewe.ui.CellConstants;
import ewe.ui.CellPanel;
import ewe.ui.ControlConstants;
import ewe.ui.ControlEvent;
import ewe.ui.Event;
import ewe.ui.Gui;
import ewe.ui.mButton;
import ewe.ui.mLabel;

/**
 * Class ID = 4500
 * 
 * @author Marc Schnitzler
 * 
 */
public class StatusBar extends CellPanel {
	DBStats stats;
	mLabel disp, lblFlt, lblCenter;
	Preferences pref;
	mButton btnFlt;
	mImage imgFlt;
	mButton btnCacheTour;
	mImage imgCacheTour;
	boolean MobileVGA;

	public StatusBar(Preferences p, CacheDB db) {
		pref = p;
		int sw = MyLocale.getScreenWidth();
		MobileVGA = (Vm.isMobile() && sw >= 400);
		String imagesize = "";
		if (MobileVGA)
			imagesize = "_vga";
		addNext(btnCacheTour = new mButton(imgCacheTour = new mImage(
				"cachetour" + imagesize + ".png")), CellConstants.DONTSTRETCH,
				CellConstants.DONTFILL);
		imgCacheTour.transparentColor = Color.White;
		if (MobileVGA)
			btnCacheTour.setPreferredSize(28, 20);
		else
			btnCacheTour.setPreferredSize(20, 13);
		btnCacheTour.borderWidth = 0;
		btnCacheTour.setToolTip(MyLocale.getMsg(197, "Show/Hide cachetour"));
		addNext(btnFlt = new mButton(imgFlt = new mImage("filter" + imagesize
				+ ".png")), CellConstants.DONTSTRETCH, CellConstants.DONTFILL);
		btnFlt.backGround = new ewe.fx.Color(0, 255, 0);
		if (MobileVGA)
			btnFlt.setPreferredSize(28, 20);
		else
			btnFlt.setPreferredSize(20, 13);
		btnFlt.borderWidth = 0;
		imgFlt.transparentColor = Color.White;
		btnFlt.setToolTip("Filter status");
		// addNext(lblFlt= new mLabel("Flt"),CellConstants.DONTSTRETCH,
		// CellConstants.DONTFILL); lblFlt.backGround=new ewe.fx.Color(0,255,0);
		stats = new DBStats(db);
		addNext(disp = new mLabel(""), CellConstants.DONTSTRETCH,
				CellConstants.FILL);
		disp.setToolTip(MyLocale.getMsg(196,
				"Total # of caches (GC&OC)\nTotal # visible\nTotal # found"));
		addLast(lblCenter = new mLabel(""), CellConstants.STRETCH, WEST
				| CellConstants.FILL);
		lblCenter.setToolTip(MyLocale.getMsg(195, "Current centre"));
		// updateDisplay();
	}

	public void updateDisplay() {
		String strStatus, strCenter = "";
		strStatus = MyLocale.getMsg(4500, "Tot:") + " " + stats.total() + " "
				+ MyLocale.getMsg(4501, "Dsp:") + " " + stats.visible() + " "
				+ MyLocale.getMsg(4502, "Fnd:") + " " + stats.totalFound()
				+ "  ";
		disp.setText(strStatus);
		// Indicate that a filter is active in the status line
		if (Global.getProfile().getFilterActive() == Filter.FILTER_ACTIVE)
			btnFlt.backGround = new Color(0, 255, 0);
		else if (Global.getProfile().getFilterActive() == Filter.FILTER_CACHELIST)
			btnFlt.backGround = new Color(0, 0, 255);
		else if (Global.getProfile().getFilterActive() == Filter.FILTER_MARKED_ONLY)
			btnFlt.backGround = new Color(0, 255, 255);
		else
			btnFlt.backGround = null;
		// Current centre can only be displayed if screen is big
		// Otherwise it forces a scrollbar
		// This can happen even on bigger screens with big fonts
		if ((MyLocale.getScreenWidth() >= 320)
				&& !(MobileVGA && (pref.fontSize > 20)))
			strCenter = "  \u00a4 " + pref.getCurCenter().toString();

		lblCenter.setText(strCenter);
		relayout(true); // in case the numbers increased and need more space
	}

	public void onEvent(Event ev) {
		if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
			if (ev.target == btnFlt) {
				Filter flt = new Filter();
				if (Global.getProfile().getFilterActive() == Filter.FILTER_INACTIVE) {
					flt.setFilter();
					flt.doFilter();
				} else {
					flt.clearFilter();
				}
				Global.mainTab.tbP.refreshTable();
			}
			if (ev.target == btnCacheTour) {
				Global.mainForm.toggleCacheListVisible();
			}
			Gui.takeFocus(Global.mainTab.tbP.tc, ControlConstants.ByKeyboard);
		}
		super.onEvent(ev);
	}
}
