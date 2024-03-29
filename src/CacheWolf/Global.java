package CacheWolf;

import CacheWolf.beans.Preferences;
import CacheWolf.beans.Profile;
import CacheWolf.gui.MainForm;
import CacheWolf.gui.MainTab;

/**
 * Global data: Preferences and Profile
 * 
 * @author salzkammergut
 */
public class Global {
	private static Preferences pref = Preferences.getPrefObject();
	private static Profile profile = new Profile();

	static public Preferences getPref() {
		return pref;
	}

	static public Profile getProfile() {
		return profile;
	}

	// A bit of a hack to allow access from one panel to another
	static public MainTab mainTab;
	static public MainForm mainForm;
}
