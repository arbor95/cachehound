package CacheWolf.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import CacheWolf.Global;
import CacheWolf.beans.CacheDB;
import CacheWolf.beans.CacheHolder;
import CacheWolf.beans.Preferences;
import CacheWolf.beans.Profile;
import CacheWolf.gui.DataMoverForm;
import ewe.filechooser.FileChooser;
import ewe.filechooser.FileChooserBase;
import ewe.sys.Handle;
import ewe.ui.FormBase;
import ewe.ui.Gui;
import ewe.ui.ProgressBarForm;

/**
 * This class moves or copies the database files of selected caches from one
 * directory to another. It provides also the possibility to delete cachefiles.
 */
public class DataMover {

	private static Logger logger = LoggerFactory.getLogger(DataMover.class);

	CacheDB srcDB, dstDB;
	Preferences pref;
	Profile profile;

	public DataMover() {
		pref = Global.getPref();
		profile = Global.getProfile();
		srcDB = profile.cacheDB;
	}

	public void deleteCaches() {

		int mode = showMessageBox(251, "All waypoints will be deleted");
		if (mode == -1) {
			return;
		}

		processCaches(new Deleter(MyLocale.getMsg(143, "Delete")), mode);
		// write indexfiles
		profile.saveIndex(pref, Profile.NO_SHOW_PROGRESS_BAR);
	}

	public void copyCaches() {
		Profile dstProfile = new Profile();
		dstProfile.setDataDir(selectTargetDir());

		if (dstProfile.getDataDir() == null
				|| dstProfile.getDataDir().equals(profile.getDataDir())) {
			return;
		}
		// Von Andi P
		int mode = showMessageBox(253, "All waypoints will be copied");
		if (mode == -1) {
			return;
		}
		// Ende

		// Read indexfile of destination, if one exists
		File ftest = new File(dstProfile.getDataDir(), "index.xml");
		if (ftest.exists()) {
			dstProfile.readIndex();
		}
		processCaches(new Copier(MyLocale.getMsg(141, "Copy"), dstProfile),
				mode);
		// write indexfiles and keep the filter status
		dstProfile.saveIndex(pref, Profile.NO_SHOW_PROGRESS_BAR);
		// Now repair the cache-Vector:
		for (int i = 0; i < srcDB.size(); i++) {
			CacheHolder holder = srcDB.get(i);
			for (Iterator<CacheHolder> j = holder.getAddiWpts().iterator(); j
					.hasNext();) {
				CacheHolder element = j.next();
				element.setMainCache(holder);
			}
		}
	}

	/**
	 * Shows the messagebox before copying/moving/deleting waypoints. It returns
	 * the mode selected by the user. 0 means all visible 1 means all ticked 2
	 * means all visible and ticked cache -1 means the user has pressed `Cancel'
	 * and no action has to be taken.
	 * 
	 * @return mode selected by the user
	 */
	private int showMessageBox(int actionTextNr, String defaultValue) {
		DataMoverForm cpf = new DataMoverForm();
		cpf.setTickedText(makeTickedText());
		cpf.setVisibleText(makeVisibleText());
		cpf.setTickedVisibleText(makeVisibleTickedText());
		cpf.setFirstLineText(MyLocale.getMsg(actionTextNr, defaultValue));
		int dialogResult = cpf.execute(null, Gui.CENTER_FRAME);
		if (dialogResult != FormBase.IDOK) {
			return -1;
		}
		int mode = cpf.getMode();
		return mode;
	}

	private String makeTickedText() {
		int size = srcDB.size();
		int countMainWP = 0;
		int countAddiWP = 0;
		// Count the number of caches to move/delete/copy
		for (int i = 0; i < size; i++) {
			if (srcDB.get(i).isChecked() && !srcDB.get(i).isAddiWpt()) {
				countMainWP++;
			} else if (srcDB.get(i).isChecked() && srcDB.get(i).isAddiWpt()) {
				countAddiWP++;
			}
		}
		return MyLocale.getMsg(255, "All ticked") + " (" + countMainWP + ' '
				+ MyLocale.getMsg(257, "Main") + ", " + countAddiWP
				+ MyLocale.getMsg(258, " Addi") + ')';
	}

	private String makeVisibleText() {
		int size = srcDB.size();
		int countMainWP = 0;
		int countAddiWP = 0;
		// Count the number of caches to move/delete/copy
		for (int i = 0; i < size; i++) {
			if (srcDB.get(i).isVisible() && !srcDB.get(i).isAddiWpt()) {
				countMainWP++;
			} else if (srcDB.get(i).isVisible() && srcDB.get(i).isAddiWpt()) {
				countAddiWP++;
			}
		}
		return MyLocale.getMsg(254, "All visible") + " (" + countMainWP + ' '
				+ MyLocale.getMsg(257, "Main") + ", " + countAddiWP
				+ MyLocale.getMsg(258, " Addi") + ')';
	}

	private String makeVisibleTickedText() {
		int size = srcDB.size();
		int countMainWP = 0;
		int countAddiWP = 0;
		// Count the number of caches to move/delete/copy
		for (int i = 0; i < size; i++) {
			if (srcDB.get(i).isVisible() && srcDB.get(i).isChecked()
					&& !srcDB.get(i).isAddiWpt()) {
				countMainWP++;
			} else if (srcDB.get(i).isVisible() && srcDB.get(i).isChecked()
					&& srcDB.get(i).isAddiWpt()) {
				countAddiWP++;
			}
		}
		return MyLocale.getMsg(256, "All visible and ticked") + " ("
				+ countMainWP + ' ' + MyLocale.getMsg(257, "Main") + ", "
				+ countAddiWP + MyLocale.getMsg(258, " Addi") + ')';
	}

	public void moveCaches() {
		Profile dstProfile = new Profile();
		// Select destination directory
		dstProfile.setDataDir(selectTargetDir());
		if (dstProfile.getDataDir().equals(profile.getDataDir())
				|| dstProfile.getDataDir().equals(""))
			return;

		int mode = showMessageBox(252, "All waypoints will be moved");
		if (mode == -1) {
			return;
		}

		// Read indexfile of destination, if one exists
		File ftest = new File(dstProfile.getDataDir() + "index.xml");
		if (ftest.exists()) {
			dstProfile.readIndex();
		}
		processCaches(new Mover(MyLocale.getMsg(142, "Move"), dstProfile), mode);
		// write indexfiles
		dstProfile.saveIndex(pref, Profile.NO_SHOW_PROGRESS_BAR);
		profile.saveIndex(pref, Profile.NO_SHOW_PROGRESS_BAR);
	}

	/**
	 * This function carries out the copy/delete/move with a progress bar. The
	 * Executor class defines what operation is to be carried out. mode defines
	 * if visible/marked or visible and markes caches are be processed. 0 means
	 * all visible 1 means all marked 2 means all visible and marked
	 * 
	 * @param exec
	 * @param tickeOnly
	 *            if set to
	 *            <code>true</true> only caches with the checked-flag will be handled.
	 */
	private void processCaches(Executor exec, int mode) {
		// First empty the cache so that the correct cache details are on disk
		CacheHolder.saveAllModifiedDetails();
		int size = srcDB.size();
		int count = 0;
		// Count the number of caches to move/delete/copy
		// and remember the index of the files process, makes it a little bit
		// easier
		boolean processSet[] = new boolean[size];
		for (int i = 0; i < size; i++) {
			switch (mode) {
			case 0:
				if (srcDB.get(i).isVisible()) {
					count++;
					processSet[i] = true;
				}
				break;
			case 1:
				if (srcDB.get(i).isChecked()) {
					count++;
					processSet[i] = true;
				}
				break;
			case 2:
				if (srcDB.get(i).isVisible() && srcDB.get(i).isChecked()) {
					count++;
					processSet[i] = true;
				}
				break;
			}
		}
		myProgressBarForm pbf = new myProgressBarForm();
		Handle h = new Handle();
		pbf.setTask(h, exec.title);
		pbf.exec();

		int nProcessed = 0;
		// Now do the actual work
		for (int i = size - 1; i >= 0; i--) {
			CacheHolder srcHolder = srcDB.get(i);
			if (processSet[i]) {
				h.progress = ((float) nProcessed++) / (float) count;
				h.changed();
				// Now do the copy/delete/move of the cache
				exec.doIt(i, srcHolder);
			}
			if (pbf.isClosed)
				break;
		}
		pbf.exit(0);
	}

	class myProgressBarForm extends ProgressBarForm {
		boolean isClosed = false;

		protected boolean canExit(int exitCode) {
			isClosed = true;
			return true;
		}
	}

	// ////////////////////////////////////////////////////////////////////
	// Utility functions
	// ////////////////////////////////////////////////////////////////////

	public File selectTargetDir() {
		// Select destination directory
		FileChooser fc = new FileChooser(FileChooserBase.DIRECTORY_SELECT, pref
				.getBaseDir().getAbsolutePath());
		fc.setTitle(MyLocale.getMsg(148, "Select Target directory"));
		if (fc.execute() != FormBase.IDCANCEL) {
			return new File(fc.getChosen());
		} else {
			return null;
		}
	}

	public void deleteCacheFiles(String wpt, File dir) {
		// delete files in dstDir to clean up trash
		File[] files = dir.listFiles(new CacheFileNameFilter(wpt));
		for (File f : files) {
			f.delete();
		}
	}

	public void moveCacheFiles(String wpt, File srcDir, File dstDir) {
		File[] files = srcDir.listFiles(new CacheFileNameFilter(wpt));
		for (File f : files) {
			f.renameTo(new File(dstDir, f.getName()));
		}
	}

	public void copyCacheFiles(final String wpt, File srcDir, File dstDir) {
		File[] files = srcDir.listFiles(new CacheFileNameFilter(wpt));
		for (File f : files) {
			copy(f, new File(dstDir, f.getName()));
		}
	}

	/**
	 * copy a file
	 * 
	 * @param sFileSrc
	 *            source file name
	 * @param sFileDst
	 *            destination file name
	 * @return true on success, false if an error occurred
	 */
	public static boolean copy(File src, File dst) {
		try {
			int len = 32768;
			byte[] buff = new byte[(int) Math.min(len, src.length())];
			FileInputStream fis = new FileInputStream(src);
			FileOutputStream fos = new FileOutputStream(dst);
			while (0 < (len = fis.read(buff)))
				fos.write(buff, 0, len);
			fos.flush();
			fos.close();
			fis.close();
		} catch (Exception ex) {
			logger.error("Filecopy failed: " + src.getAbsolutePath() + "=>"
					+ dst.getAbsolutePath(), ex);
			return false;
		}
		return true;
	}

	/**
	 * Filter to get only files which start with den waypoint-name of a given
	 * cache
	 * 
	 * @author tweety
	 * 
	 */
	private class CacheFileNameFilter implements FilenameFilter {

		private String wpt;

		public CacheFileNameFilter(String wpt) {
			this.wpt = wpt;
		}

		@Override
		public boolean accept(File dir, String name) {
			// TODO: Check if it is necessary to have only files and no
			// directories
			return name.startsWith(wpt);
		}

	}

	// ////////////////////////////////////////////////////////////////////
	// Executor
	// ////////////////////////////////////////////////////////////////////

	private abstract class Executor {
		String title;
		Profile dstProfile;

		public abstract void doIt(int i, CacheHolder srcHolder);
	}

	private class Deleter extends Executor {
		Deleter(String title) {
			this.title = title;
		}

		public void doIt(int i, CacheHolder srcHolder) {
			srcDB.removeElementAt(i);
			deleteCacheFiles(srcHolder.getWayPoint(), profile.getDataDir());
		}
	}

	private class Copier extends Executor {
		Copier(String title, Profile dstProfile) {
			this.title = title;
			this.dstProfile = dstProfile;
		}

		public void doIt(int i, CacheHolder srcHolder) {
			srcHolder.save();
			deleteCacheFiles(srcHolder.getWayPoint(), dstProfile.getDataDir());
			copyCacheFiles(srcHolder.getWayPoint(), profile.getDataDir(),
					dstProfile.getDataDir());
			// does cache exists in destDB ?
			// Update database
			// *wall* when copying addis without their maincache, the maincache
			// in the srcDB will be set to null on saving the dstProfile later.
			// Therefore it will be shown twice in the cachelist.
			// To prevent this, addis will be cloned and to save memory only
			// addis will be clones.
			// TODO clone addis only when the maincache will not be copied.
			// if (srcHolder.isAddiWpt()){
			// try {
			// srcHolder = (CacheHolder) srcHolder.clone();
			// } catch (CloneNotSupportedException e) {
			// //ignore, CacheHolder implements Cloneable ensures this methods
			// }
			// }
			int dstPos = dstProfile.getCacheIndex(srcHolder.getWayPoint());
			if (dstPos >= 0) {
				dstProfile.cacheDB.set(dstPos, srcHolder);
			} else {
				dstProfile.cacheDB.add(srcHolder);
			}
		}
	}

	private class Mover extends Executor {
		Mover(String title, Profile dstProfile) {
			this.title = title;
			this.dstProfile = dstProfile;
		}

		public void doIt(int i, CacheHolder srcHolder) {
			srcDB.removeElementAt(i);
			deleteCacheFiles(srcHolder.getWayPoint(), dstProfile.getDataDir());
			moveCacheFiles(srcHolder.getWayPoint(), profile.getDataDir(),
					dstProfile.getDataDir());
			// does cache exists in destDB ?
			// Update database
			int dstPos = dstProfile.getCacheIndex(srcHolder.getWayPoint());
			if (dstPos >= 0) {
				dstProfile.cacheDB.set(dstPos, srcHolder);
			} else {
				// Update database
				dstProfile.cacheDB.add(srcHolder);
			}
		}
	}
}
