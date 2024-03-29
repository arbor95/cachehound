package CacheWolf.gui;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

import CacheWolf.Global;
import CacheWolf.beans.Filter;
import CacheWolf.util.MyLocale;
import ewe.fx.Graphics;
import ewe.fx.Insets;
import ewe.fx.Rect;
import ewe.ui.CellConstants;
import ewe.ui.ControlConstants;
import ewe.ui.ControlEvent;
import ewe.ui.Event;
import ewe.ui.Form;
import ewe.ui.Gui;
import ewe.ui.IKeys;
import ewe.ui.ScrollablePanel;
import ewe.ui.mButton;
import ewe.ui.mLabel;
import ewe.ui.mList;

/**
 * This form displays the list of profiles for a user to choose from, when
 * CacheWolf starts up. Also allows to open a new profile. ClassID = 1300
 */
public class ProfilesForm extends Form {

	// A subclassed mList which allows the highlighting of an entry
	// Maybe there is an easier way of making this happen, but I could not find
	// it.
	private class MyList extends mList {
		private int first = 1;
		private int select;

		public MyList() {
			super(1, 1, false);
		}

		public void selectLastProfile(String selectedItem) {
			selectItem(selectedItem);
			select = getSelectedIndex(0);
		}

		public void doPaint(Graphics gr, Rect area) {
			if (first == 1) {
				first = 0;
				selectAndView(select);
				makeVisible(select);
			}
			super.doPaint(gr, area);
		}

		// Copied from BasicList.getScrollablePanel(), but exchanging
		// the standard scroll bar with the fontsize sensitive one.
		public ScrollablePanel getScrollablePanel() {
			dontAutoScroll = amScrolling = true;
			ScrollablePanel sp = new MyScrollBarPanel(this);
			sp.modify(0, TakeControlEvents);
			return sp;
		}

	}

	private MyList choice;
	private ScrollablePanel spMList;
	private mButton btnCancel, btnNew, btnOK;
	private File baseDir;
	public String newSelectedProfile; // This is only used if a new profile is

	// being created

	/**
	 * Constructor to create a form to select profiles. It requires that the
	 * preferences have been loaded so that the calling parameters can be set.
	 * 
	 * @param baseDir
	 *            The base directory which holds one subdirectory per profile
	 * @param oldProfiles
	 *            List of names of old profiles
	 * @param selectedProfile
	 *            Name of the last used profile
	 */
	public ProfilesForm(File baseDir, String selectedProfile,
			boolean hasNewButton) {
		super();
		resizable = false;
		int w = MyLocale.getScreenWidth();
		int h = MyLocale.getScreenHeight();
		if (w > 240)
			w = 240;
		if (h > 320)
			h = 320;
		setPreferredSize(w, h);
		defaultTags.set(CellConstants.INSETS, new Insets(2, 2, 2, 2));
		title = MyLocale.getMsg(1301, "Select Profile:");
		if (hasNewButton) {
			addNext(new mLabel(MyLocale.getMsg(1106, "Choose profile or New")),
					DONTSTRETCH, DONTSTRETCH | LEFT);
			addLast(btnNew = new mButton(MyLocale.getMsg(1107, "New")),
					HSTRETCH, HFILL | RIGHT);
		} else {
			addLast(new mLabel(MyLocale.getMsg(1108, "Choose profile")),
					DONTSTRETCH, DONTSTRETCH | LEFT);
		}

		choice = new MyList();
		// Get all subdirectories in the base directory
		File[] existingProfiles = baseDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});
		// Now add these subdirectories to the list of profiles but
		// exclude the "maps" directory which will contain the moving maps
		Arrays.sort(existingProfiles);
		for (File f : existingProfiles)
			if (!f.getName().equalsIgnoreCase("maps")) {
				choice.addItem(f.getName());
			}
		// Highlight the profile that was used last
		choice.selectLastProfile(selectedProfile);
		// Add a scroll bar to the list of profiles
		spMList = choice.getScrollablePanel();
		spMList.setOptions(ScrollablePanel.NeverShowHorizontalScrollers);
		choice.setServer(spMList);
		addLast(spMList);
		addNext(btnCancel = new mButton(MyLocale.getMsg(1604, "Cancel")),
				DONTSTRETCH, DONTFILL | LEFT);
		addNext(btnOK = new mButton(MyLocale.getMsg(1605, "OK")), DONTSTRETCH,
				HFILL | RIGHT);
		if (choice.getListItems().length == 0)
			btnOK.modify(Disabled, 0);
		btnOK.setHotKey(0, IKeys.ENTER);
		btnCancel.setHotKey(0, IKeys.ESCAPE);
		this.baseDir = baseDir;
		choice.takeFocus(ControlConstants.ByKeyboard);
	}

	/**
	 * Ask for a new profile directory. If it exists, cancel. If it does not
	 * exist, create it
	 * 
	 * @return Name of directory (just the part below baseDir)
	 */
	public String createNewProfile() {
		NewProfileForm f = new NewProfileForm(baseDir);
		int code = f.execute(getFrame(), Gui.CENTER_FRAME);
		if (code == 0) {
			return f.getProfileDir().getName();
		} else
			return "";
	}

	/**
	 * The event handler to react to a users selection. A return value is
	 * created and passed back to the calling form while it closes itself.
	 */
	public void onEvent(Event ev) {
		if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
			if (ev.target == btnCancel) {
				close(-1);
			}
			if (ev.target == btnOK || ev.target == choice) {
				Global.getProfile().setFilterActive(Filter.FILTER_INACTIVE);
				if (choice.getSelectedItem() != null) {
					newSelectedProfile = choice.getSelectedItem().toString();
					close(1);
				}
			}
			if (ev.target == btnNew) {
				if (NewProfileWizard.startNewProfileWizard(getFrame())) {
					newSelectedProfile = Global.getProfile().name;
					close(1);
				}
			}
		}
		super.onEvent(ev);
	}

}
