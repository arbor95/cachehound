/*
    CacheWolf is a software for PocketPC, Win and Linux that 
    enables paperless caching. 
    It supports the sites geocaching.com and opencaching.de
    
    Copyright (C) 2006  CacheWolf development team
    See http://developer.berlios.de/projects/cachewolf/
    for more information.
    Contact: 	bilbowolf@users.berlios.de
		kalli@users.berlios.de

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation version 2 of the License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package CacheWolf.exporter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import CacheWolf.beans.CWPoint;
import CacheWolf.beans.CacheDB;
import CacheWolf.beans.CacheHolder;
import CacheWolf.beans.CacheType;
import CacheWolf.beans.Preferences;
import CacheWolf.beans.Profile;
import CacheWolf.util.Common;
import HTML.Template;

import com.stevesoft.ewe_pat.Regex;

import de.cachehound.beans.CacheHolderDetail;
import ewe.filechooser.FileChooser;
import ewe.filechooser.FileChooserBase;
import ewe.ui.FormBase;
import ewe.ui.ProgressBarForm;
import ewe.util.Hashtable;
import ewe.util.Vector;

public class TPLExporter {

	private static Logger logger = LoggerFactory.getLogger(TPLExporter.class);

	private CacheDB cacheDB;
	private Preferences pref;
	private Profile profile;
	private File tplFile;
	private String expName;
	private Regex rex = null;

	public TPLExporter(Preferences p, Profile prof, File tplFile) {
		pref = p;
		profile = prof;
		cacheDB = profile.cacheDB;
		this.tplFile = tplFile;
		expName = tplFile.getName();
		expName = expName.substring(0, expName.indexOf("."));
	}

	public void doIt() {
		CacheHolderDetail det;
		CacheHolder ch;
		ProgressBarForm pbf = new ProgressBarForm();
		ewe.sys.Handle h = new ewe.sys.Handle();

		File oldPath = pref.getExportPath(expName);
		String oldPathString = "";
		if (pref != null) {
			oldPathString = oldPath.getAbsolutePath();
		}

		FileChooser fc = new FileChooser(FileChooserBase.SAVE, oldPathString);
		fc.setTitle("Select target file:");
		if (fc.execute() == FormBase.IDCANCEL)
			return;
		File saveTo = new File(fc.getChosenFile().getFullPath());
		pref.setExportPath(expName, saveTo.getParentFile());
		int counter = cacheDB.countVisible();
		pbf.showMainTask = false;
		pbf.setTask(h, "Exporting ...");
		pbf.exec();
		try {
			Vector cache_index = new Vector(); // declare variables inside try
			// {} -> in case of
			// OutOfMemoryError, they can be
			// garbage collected - anyhow it
			// doesn't work :-(
			Hashtable varParams;
			TplFilter myFilter;
			Hashtable args = new Hashtable();
			myFilter = new TplFilter();
			// args.put("debug", "true");
			args.put("filename", tplFile.getAbsolutePath());
			args.put("case_sensitive", "true");
			args.put("loop_context_vars", Boolean.TRUE);
			args.put("max_includes", new Integer(5));
			args.put("filter", myFilter);
			Template tpl = new Template(args);

			for (int i = 0; i < counter; i++) {
				ch = cacheDB.get(i);
				det = ch.getExistingDetails();
				h.progress = (float) i / (float) counter;
				h.changed();
				if (ch.isVisible()) {
					if (ch.getPos().isValid() == false)
						continue;
					try {
						Regex dec = new Regex("[,.]", myFilter.decimalSeperator);
						if (myFilter.badChars != null)
							rex = new Regex("[" + myFilter.badChars + "]", "");
						varParams = new Hashtable();
						varParams.put("TYPE", CacheType.cw2ExportString(ch
								.getType()));
						varParams.put("SHORTTYPE", CacheType
								.getExportShortId(ch.getType()));
						varParams.put("SIZE", ch.getCacheSize().getAsString());
						varParams.put("SHORTSIZE", ch.getCacheSize()
								.getAsChar());
						varParams.put("WAYPOINT", ch.getWayPoint());
						varParams.put("OWNER", ch.getCacheOwner());
						varParams
								.put(
										"DIFFICULTY",
										(ch.isAddiWpt() || CacheType.CW_TYPE_CUSTOM == ch
												.getType()) ? ""
												: dec
														.replaceAll(ch
																.getDifficulty()
																.getFullRepresentation()));
						varParams
								.put(
										"TERRAIN",
										(ch.isAddiWpt() || CacheType.CW_TYPE_CUSTOM == ch
												.getType()) ? ""
												: dec
														.replaceAll(ch
																.getTerrain()
																.getFullRepresentation()));
						varParams.put("DISTANCE", dec.replaceAll(ch
								.getDistance()));
						varParams.put("BEARING", ch.getBearingAsString());
						varParams.put("LATLON", ch.getLatLon());
						varParams.put("LAT", dec.replaceAll(ch.getPos()
								.getLatDeg(CWPoint.DD)));
						varParams.put("LON", dec.replaceAll(ch.getPos()
								.getLonDeg(CWPoint.DD)));
						varParams.put("STATUS", ch.getCacheStatus());
						varParams.put("STATUS_DATE", ch.GetStatusDate());
						varParams.put("STATUS_TIME", ch.GetStatusTime());
						varParams.put("DATE", ch.getDateHidden());
						varParams.put("URL", det.getUrl());
						varParams.put("GC_LOGTYPE", (ch.is_found() ? "Found it"
								: "Didn't find it"));
						varParams.put("DESCRIPTION", det.getLongDescription());
						if (myFilter.badChars != null) {
							varParams.put("NAME", rex.replaceAll(ch
									.getCacheName()));
							varParams.put("NOTES", rex.replaceAll(det
									.getCacheNotes()));
							varParams.put("HINTS", rex.replaceAll(det
									.getHints()));
							varParams.put("DECRYPTEDHINTS", rex
									.replaceAll(Common.rot13(det.getHints())));
						} else {
							varParams.put("NAME", ch.getCacheName());
							varParams.put("NOTES", det.getCacheNotes());
							varParams.put("HINTS", det.getHints());
							varParams.put("DECRYPTEDHINTS", Common.rot13(det
									.getHints()));
						}
						cache_index.add(varParams);
					} catch (Exception e) {
						logger.error("Problem getting Parameter, Cache: "
								+ ch.getWayPoint(), e);
					}
				}
			}

			tpl.setParam("cache_index", cache_index);
			OutputStream outStream = new BufferedOutputStream(
					new FileOutputStream(saveTo));
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(
					outStream, myFilter.charset));
			writer.print(tpl.output());
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception in TplExporter", e);
		}
		pbf.exit(0);
	}

	/**
	 * @author Kalle class to export cachedata using a template
	 */
	class TplFilter implements HTML.Tmpl.Filter {
		private int type = SCALAR;
		private String newLine;
		private Charset charset;
		private String badChars;
		private String decimalSeperator = ".";

		public TplFilter() {
			newLine = "\n";
			charset = Charset.forName("UTF-8");
		}

		public int format() {
			return this.type;
		}

		public String parse(String t) {
			// Vm.debug(t);
			Regex rex, rex1;
			String param, value;
			// Filter newlines
			rex = new Regex("(?m)\n$", "");
			t = rex.replaceAll(t);

			// Filter comments <#-- and -->
			rex = new Regex("<#--.*-->", "");
			t = rex.replaceAll(t);

			// replace <br> or <br /> with newline
			rex = new Regex("<br.*>", "");
			rex.search(t);
			if (rex.didMatch()) {
				t = rex.replaceAll(t);
				t += newLine;
			}

			// search for parameters
			rex = new Regex("(?i)<tmpl_par.*>");
			rex.search(t);
			if (rex.didMatch()) {
				// get parameter
				rex1 = new Regex("(?i)name=\"(.*)\"\\svalue=\"(.*)\"[?\\s>]");
				rex1.search(t);
				param = rex1.stringMatched(1);
				value = rex1.stringMatched(2);
				// Vm.debug("param=" + param + "\nvalue=" + value);
				// clear t, because we allow only one parameter per line
				t = "";

				// get the values
				if (param.equals("charset")) {
					// legacy for old cachewolf Templates
					if (value.equals("ASCII")) {
						value = "US-ASCII";
					}
					if (value.equals("UTF8")) {
						value = "UTF-8";
					}
					charset = Charset.forName(value);
				}
				if (param.equals("badchars")) {
					badChars = value;
				}
				if (param.equals("newline")) {
					newLine = "";
					if (value.indexOf("CR") >= 0)
						newLine += "\r";
					if (value.indexOf("LF") >= 0)
						newLine += "\n";
				}
				if (param.equals("decsep")) {
					decimalSeperator = value;
				}

			}
			return t;
		}

		public String[] parse(String[] t) {
			throw new UnsupportedOperationException();
		}
	}

}
