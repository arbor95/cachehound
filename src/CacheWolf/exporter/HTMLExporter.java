package CacheWolf.exporter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import CacheWolf.Global;
import CacheWolf.beans.CacheDB;
import CacheWolf.beans.CacheHolder;
import CacheWolf.beans.Preferences;
import CacheWolf.beans.Profile;
import CacheWolf.util.Common;
import CacheWolf.util.DataMover;
import HTML.Template;

import com.stevesoft.ewe_pat.Regex;

import de.cachehound.beans.ICacheHolderDetail;
import de.cachehound.factory.LogFactory;
import de.cachehound.types.CacheType;
import ewe.filechooser.FileChooser;
import ewe.filechooser.FileChooserBase;
import ewe.sys.Convert;
import ewe.sys.Handle;
import ewe.sys.Vm;
import ewe.ui.FormBase;
import ewe.ui.MessageBox;
import ewe.ui.ProgressBarForm;
import ewe.util.Comparer;
import ewe.util.Hashtable;
import ewe.util.Vector;

/**
 * Class to export cache information to individual HTML files.<br>
 * It uses the HTML package to parse template files. This makes the export very
 * flexible; enabling the user to customise the HTML files according to thier
 * liking.
 */
public class HTMLExporter {
	// TODO Exportanzahl anpassen: Bug: 7351
	CacheDB cacheDB;
	Preferences pref;
	Profile profile;
	String[] template_init_index = { "filename",
			"templates" + File.separator + "index.tpl", "case_sensitive",
			"true", "max_includes", "5"
	// ,"debug", "true"
	};
	String[] template_init_page = { "filename",
			"templates" + File.separator + "page.tpl", "case_sensitive",
			"true", "max_includes", "5" };
	public final static String expName = "HTML";

	public HTMLExporter(Preferences p, Profile prof) {
		pref = p;
		profile = prof;
		cacheDB = profile.cacheDB;
	}

	public void doIt() {
		ICacheHolderDetail det;
		CacheHolder ch;
		ProgressBarForm pbf = new ProgressBarForm();
		Handle h = new Handle();
		int exportErrors = 0;

		new String();
		FileChooser fc = new FileChooser(FileChooserBase.DIRECTORY_SELECT, pref
				.getExportPath(expName).getAbsolutePath());
		fc.setTitle("Select target directory:");
		File targetDir;
		if (fc.execute() != FormBase.IDCANCEL) {
			targetDir = new File(fc.getChosenFile().getFullPath());
			pref.setExportPath(expName, targetDir);
			Vector cache_index = new Vector();
			Vector cacheImg = new Vector();
			Vector logImg = new Vector();
			Vector mapImg = new Vector();
			Vector usrImg = new Vector();
			Vector logIcons = new Vector(15);
			String icon;

			Hashtable varParams;
			Hashtable imgParams;
			Hashtable logImgParams;
			Hashtable usrImgParams;
			Hashtable mapImgParams;

			// Generate index page
			int counter = cacheDB.countVisible();

			pbf.showMainTask = false;
			pbf.setTask(h, "Exporting ...");
			pbf.exec();

			for (int i = 0; i < counter; i++) {
				h.progress = (float) (i + 1) / (float) counter;
				h.changed();

				ch = cacheDB.get(i);
				if (ch.isVisible()) {
					if (ch.is_incomplete()) {
						exportErrors++;
						Global.getPref().log(
								"HTMLExport: skipping export of incomplete waypoint "
										+ ch.getWayPoint());
						continue;
					}
					det = ch.getCacheDetails(false, false);
					varParams = new Hashtable();
					varParams.put("TYPE", ch
							.getType().getGcGpxString());
					varParams.put("WAYPOINT", ch.getWayPoint());
					varParams.put("NAME", ch.getCacheName());
					varParams.put("OWNER", ch.getCacheOwner());
					if (ch.isAddiWpt()
							|| ch.getType() == CacheType.CUSTOM) {
						varParams.put("SIZE", "");
						varParams.put("DIFFICULTY", "");
						varParams.put("TERRAIN", "");
					} else {
						varParams.put("SIZE", ch.getCacheSize().getAsString());
						varParams.put("DIFFICULTY", ch.getDifficulty().isValid() ? ch
								.getDifficulty().getFullRepresentation() : "");
						varParams.put("TERRAIN", ch.getTerrain().isValid() ? ch
								.getTerrain().getFullRepresentation() : "");
					}
					varParams.put("DISTANCE", ch.getDistance());
					varParams.put("BEARING", ch.getBearingAsString());
					varParams.put("LATLON", ch.getLatLon());
					varParams.put("STATUS", ch.getCacheStatus());
					varParams.put("DATE", ch.getDateHidden());
					cache_index.add(varParams);
					// We can generate the individual page here!
					try {
						Template page_tpl = new Template(template_init_page);
						page_tpl.setParam("TYPE", varParams.get("TYPE")
								.toString());
						page_tpl.setParam("SIZE", varParams.get("SIZE")
								.toString());
						page_tpl.setParam("WAYPOINT", ch.getWayPoint());
						page_tpl.setParam("NAME", ch.getCacheName());
						page_tpl.setParam("OWNER", ch.getCacheOwner());
						page_tpl.setParam("DIFFICULTY", varParams.get(
								"DIFFICULTY").toString());
						page_tpl.setParam("TERRAIN", varParams.get("TERRAIN")
								.toString());
						page_tpl.setParam("DISTANCE", ch.getDistance());
						page_tpl.setParam("BEARING", ch.getBearingAsString());
						page_tpl.setParam("LATLON", ch.getLatLon());
						page_tpl.setParam("STATUS", ch.getCacheStatus());
						page_tpl.setParam("DATE", ch.getDateHidden());
						if (det != null) {
							if (ch.is_HTML()) {
								page_tpl.setParam("DESCRIPTION",
										modifyLongDesc(det));
							} else {
								page_tpl.setParam("DESCRIPTION", det
										.getLongDescription().replace("\n",
												"<br>"));
							}
							page_tpl.setParam("HINTS", det.getHints());
							page_tpl.setParam("DECRYPTEDHINTS", Common
									.rot13(det.getHints()));

							StringBuilder sb = new StringBuilder(2000);
							for (int j = 0; j < det.getCacheLogs().size(); j++) {
								sb
										.append(LogFactory
												.getInstance()
												.toHtml(
														det.getCacheLogs()
																.getLog(j))
												.replace(
														"http://www.geocaching.com/images/icons/",
														""));
								sb.append("<br>");
								icon = det.getCacheLogs().getLog(j)
										.getLogType().toIconString();
								if (logIcons.find(icon) < 0)
									logIcons.add(icon); // Add the icon to list
								// of icons to copy to
								// dest directory
							}

							page_tpl.setParam("LOGS", sb.toString());
							page_tpl.setParam("NOTES", det.getCacheNotes()
									.replace("\n", "<br>"));

							cacheImg.clear();
							for (int j = 0; j < det.getImages().size(); j++) {
								imgParams = new Hashtable();
								String imgFile = new String(det.getImages()
										.get(j).getFilename());
								imgParams.put("FILE", imgFile);
								imgParams.put("TEXT", det.getImages().get(j)
										.getTitle());
								if (DataMover.copy(new java.io.File(profile
										.getDataDir(), imgFile),
										new java.io.File(targetDir, imgFile))) {
									cacheImg.add(imgParams);
								} else {
									exportErrors++;
								}
							}
							page_tpl.setParam("cacheImg", cacheImg);

							// Log images
							logImg.clear();
							for (int j = 0; j < det.getLogImages().size(); j++) {
								logImgParams = new Hashtable();
								String logImgFile = det.getLogImages().get(j)
										.getFilename();
								logImgParams.put("FILE", logImgFile);
								logImgParams.put("TEXT", det.getLogImages()
										.get(j).getTitle());
								if (DataMover
										.copy(new java.io.File(profile
												.getDataDir(), logImgFile),
												new java.io.File(targetDir,
														logImgFile))) {
									logImg.add(logImgParams);
								} else {
									exportErrors++;
								}
							}
							page_tpl.setParam("logImg", logImg);

							// User images
							usrImg.clear();
							for (int j = 0; j < det.getUserImages().size(); j++) {
								usrImgParams = new Hashtable();
								String usrImgFile = new String(det
										.getUserImages().get(j).getFilename());
								usrImgParams.put("FILE", usrImgFile);
								usrImgParams.put("TEXT", det.getUserImages()
										.get(j).getTitle());
								if (DataMover
										.copy(new java.io.File(profile
												.getDataDir(), usrImgFile),
												new java.io.File(targetDir,
														usrImgFile)))
									usrImg.add(usrImgParams);
								else
									exportErrors++;
							}
							page_tpl.setParam("userImg", usrImg);

							// Map images
							mapImg.clear();
							mapImgParams = new Hashtable();

							String mapImgFile = new String(ch.getWayPoint()
									+ "_map.gif");
							// check if map file exists
							File test = new File(profile.getDataDir(),
									mapImgFile);

							if (test.exists()) {
								mapImgParams.put("FILE", mapImgFile);
								mapImgParams.put("TEXT", mapImgFile);
								if (DataMover.copy(new File(profile
										.getDataDir(), mapImgFile), new File(
										targetDir, mapImgFile))) {
									mapImg.add(mapImgParams);
								} else {
									exportErrors++;
								}

								mapImgParams = new Hashtable();
								mapImgFile = ch.getWayPoint() + "_map_2.gif";
								mapImgParams.put("FILE", mapImgFile);
								mapImgParams.put("TEXT", mapImgFile);
								if (DataMover.copy(new File(profile
										.getDataDir(), mapImgFile), new File(
										targetDir, mapImgFile)))
									mapImg.add(mapImgParams);
								else
									exportErrors++;

								page_tpl.setParam("mapImg", mapImg);
							}
						} else {
							page_tpl.setParam("DESCRIPTION", "");
							page_tpl.setParam("HINTS", "");
							page_tpl.setParam("DECRYPTEDHINTS", "");
							page_tpl.setParam("LOGS", "");
							page_tpl.setParam("NOTES", "");
							page_tpl.setParam("cacheImg", cacheImg);
							page_tpl.setParam("logImg", ""); // ???
							page_tpl.setParam("userImg", ""); // ???
							page_tpl.setParam("mapImg", ""); // ???
							exportErrors++;
						}

						PrintWriter pagefile = new PrintWriter(
								new BufferedWriter(new FileWriter(new File(
										targetDir, ch.getWayPoint() + ".html"))));
						pagefile.print(page_tpl.output());
						pagefile.close();
					} catch (IllegalArgumentException e) {
						exportErrors++;
						ch.setIncomplete(true);
						Global.getPref().log(
								"HTMLExport: " + ch.getWayPoint()
										+ " is incomplete reason: ", e,
								Global.getPref().debug);
					} catch (Exception e) {
						exportErrors++;
						Global.getPref().log(
								"HTMLExport: error wehen exporting "
										+ ch.getWayPoint() + " reason: ", e,
								Global.getPref().debug);
					}
				}// if is black, filtered
			}

			// Copy the log-icons to the destination directory
			for (int j = 0; j < logIcons.size(); j++) {
				icon = (String) logIcons.elementAt(j);
				if (!DataMover.copy(new File(icon), new File(targetDir, icon))) {
					exportErrors++;
				}
			}
			if (!DataMover.copy(new File("recommendedlog.gif"), new File(
					targetDir, "recommendedlog.gif"))) {
				exportErrors++;
			}
			try {
				Template tpl = new Template(template_init_index);
				tpl.setParam("cache_index", cache_index);
				PrintWriter detfile;
				detfile = new PrintWriter(new BufferedWriter(new FileWriter(
						targetDir + "/index.html")));
				detfile.print(tpl.output());
				detfile.close();
				// sort by waypoint
				sortAndPrintIndex(tpl, cache_index, targetDir
						+ "/index_wp.html", "WAYPOINT");
				// sort by name
				sortAndPrintIndex(tpl, cache_index, targetDir
						+ "/index_alpha.html", "NAME", false);
				// sort by type
				sortAndPrintIndex(tpl, cache_index, targetDir
						+ "/index_type.html", "TYPE", true);
				// sort by size
				sortAndPrintIndex(tpl, cache_index, targetDir
						+ "/index_size.html", "SIZE", true);
				// sort by distance
				sortAndPrintIndex(tpl, cache_index, targetDir
						+ "/index_dist.html", "DISTANCE", 10.0);
			} catch (Exception e) {
				Vm.debug("Problem writing HTML files\n");
				e.printStackTrace();
			}// try

		}// if
		pbf.exit(0);

		if (exportErrors > 0) {
			new MessageBox("Export Error", exportErrors
					+ " errors during export. See log for details.",
					FormBase.OKB).execute();
		}

	}

	/**
	 * Modify the image links in the long description so that they point to
	 * image files in the local directory Also copy the image file to the target
	 * directory so that it can be displayed.
	 * 
	 * @param chD
	 *            CacheHolderDetail
	 * @return The modified long description
	 */
	private String modifyLongDesc(ICacheHolderDetail chD) {
		StringBuilder s = new StringBuilder(chD.getLongDescription().length());
		int start = 0;
		int pos;
		int imageNo = 0;
		Regex imgRex = new Regex("src=(?:\\s*[^\"|']*?)(?:\"|')(.*?)(?:\"|')");
		while (start >= 0
				&& (pos = chD.getLongDescription().indexOf("<img", start)) > 0) {
			s.append(chD.getLongDescription().substring(start, pos));
			imgRex.searchFrom(chD.getLongDescription(), pos);
			String imgUrl = imgRex.stringMatched(1);
			// Vm.debug("imgUrl "+imgUrl);
			if (imgUrl.lastIndexOf('.') > 0
					&& imgUrl.toLowerCase().startsWith("http")) {
				String imgType = (imgUrl.substring(imgUrl.lastIndexOf("."))
						.toLowerCase() + "    ").substring(0, 4).trim();
				// If we have an image which we stored when spidering, we can
				// display it
				if (!imgType.startsWith(".com") && !imgType.startsWith(".php")
						&& !imgType.startsWith(".exe")
						&& !imgType.startsWith(".pl")) {
					// It may occur that there are less local images than
					// image links in the description (eg. because of importing
					// GPX files). We have to allow for this situation.
					Object localImageSource = null;
					if (imageNo < chD.getImages().size()) {
						localImageSource = chD.getImages().get(imageNo)
								.getFilename();
					}
					if (localImageSource == null)
						localImageSource = imgUrl;
					s.append("<img src=\"" + localImageSource + "\">");
					// The actual immages are copied elswhere
					// DataMover.copy(profile.dataDir +
					// chD.Images.get(imageNo),targetDir +
					// chD.Images.get(imageNo));
					imageNo++;
				}
			}
			start = chD.getLongDescription().indexOf(">", pos);
			if (start >= 0)
				start++;
			if (imageNo >= chD.getImages().size())
				break;
		}
		if (start >= 0)
			s.append(chD.getLongDescription().substring(start));
		return s.toString();
	}

	private void sortAndPrintIndex(Template tmpl, Vector list, String file,
			String field) {
		PrintWriter detfile;

		list.sort(new HTMLComparer(field), false);
		try {
			detfile = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			detfile.print(tmpl.output());
			detfile.close();
		} catch (IOException e) {
			Vm.debug("Problem writing HTML files\n");
			e.printStackTrace();
		}
	}

	private void sortAndPrintIndex(Template tmpl, Vector list, String file,
			String field, boolean fullCompare) {
		Vector navi_index;
		PrintWriter detfile;

		list.sort(new HTMLComparer(field), false);
		navi_index = addAnchorString(list, field, fullCompare);
		if (navi_index != null) {
			tmpl.setParam("navi_index", navi_index);
		}
		try {
			detfile = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			detfile.print(tmpl.output());
			detfile.close();
		} catch (IOException e) {
			Vm.debug("Problem writing HTML files\n");
			e.printStackTrace();
		}
	}

	private void sortAndPrintIndex(Template tmpl, Vector list, String file,
			String field, double diff) {
		Vector navi_index;
		PrintWriter detfile;

		list.sort(new HTMLComparer(field), false);
		navi_index = addAnchorString(list, field, diff);
		if (navi_index != null) {
			tmpl.setParam("navi_index", navi_index);
		}
		try {
			detfile = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			detfile.print(tmpl.output());
			detfile.close();
		} catch (IOException e) {
			Vm.debug("Problem writing HTML files\n");
			e.printStackTrace();
		}

	}

	private Vector addAnchorString(Vector list, String field,
			boolean fullCompare) {
		Vector topIndex = new Vector();
		Hashtable topIndexParms, currEntry;
		String lastValue, currValue;

		if (list.size() == 0)
			return null;

		currEntry = (Hashtable) list.get(0);
		lastValue = (String) currEntry.get(field);
		if (lastValue == null || lastValue.length() == 0)
			lastValue = "  ";
		lastValue = lastValue.toUpperCase();

		for (int i = 1; i < list.size(); i++) {
			currEntry = (Hashtable) list.get(i);
			currValue = (String) currEntry.get(field);
			currValue = currValue.toUpperCase();
			if (currValue == null || currValue == "")
				continue;
			try {
				if (fullCompare) {
					if (lastValue.compareTo(currValue) != 0) {
						// Values for navigation line
						topIndexParms = new Hashtable();
						topIndexParms.put("HREF", Convert.toString(i));
						topIndexParms.put("TEXT", currValue);
						topIndex.add(topIndexParms);
						// add anchor entry to list
						currEntry.put("ANCHORNAME", Convert.toString(i));
						currEntry.put("ANCHORTEXT", currValue);
					} else {
						// clear value from previous run
						currEntry.put("ANCHORNAME", "");
						currEntry.put("ANCHORTEXT", "");
					}
				} else {
					if (lastValue.charAt(0) != currValue.charAt(0)) {
						// Values for navigation line
						topIndexParms = new Hashtable();
						topIndexParms.put("HREF", Convert.toString(i));
						topIndexParms.put("TEXT", currValue.charAt(0) + " ");
						topIndex.add(topIndexParms);
						// add anchor entry to list
						currEntry.put("ANCHORNAME", Convert.toString(i));
						currEntry.put("ANCHORTEXT", currValue.charAt(0) + " ");
					} else {
						// clear value from previous run
						currEntry.put("ANCHORNAME", "");
						currEntry.put("ANCHORTEXT", "");
					}
				}
				list.set(i, currEntry);
				lastValue = currValue;
			} catch (Exception e) {
				continue;
			}
		}
		return topIndex;
	}

	private Vector addAnchorString(Vector list, String field, double diff) {
		Vector topIndex = new Vector();
		Hashtable topIndexParms, currEntry;
		double lastValue, currValue;

		if (list.size() == 0)
			return null;

		currEntry = (Hashtable) list.get(0);
		lastValue = Common.parseDouble((String) currEntry.get(field)) + diff;

		for (int i = 1; i < list.size(); i++) {
			currEntry = (Hashtable) list.get(i);
			currValue = Common.parseDouble((String) currEntry.get(field));
			if (currValue >= lastValue) {
				// Values for navigation line
				topIndexParms = new Hashtable();
				topIndexParms.put("HREF", Convert.toString(i));
				topIndexParms.put("TEXT", Convert.toString(lastValue));
				topIndex.add(topIndexParms);
				// add anchor entry to list
				currEntry.put("ANCHORNAME", Convert.toString(i));
				currEntry.put("ANCHORTEXT", Convert.toString(lastValue));
				lastValue = currValue + diff;
			} else {
				// clear value from previous run
				currEntry.put("ANCHORNAME", "");
				currEntry.put("ANCHORTEXT", "");
			}
			list.set(i, currEntry);
		}
		return topIndex;
	}

	/**
	 * @author Kalle Comparer for sorting the vector for the index.html file
	 */
	private class HTMLComparer implements Comparer {
		String compareWhat;

		public HTMLComparer(String what) {
			this.compareWhat = what;
		}

		public int compare(Object o1, Object o2) {
			Hashtable hash1 = (Hashtable) o1;
			Hashtable hash2 = (Hashtable) o2;
			String str1, str2;
			double dbl1, dbl2;

			str1 = hash1.get(compareWhat).toString().toLowerCase();
			str2 = hash2.get(compareWhat).toString().toLowerCase();

			if (this.compareWhat.equals("WAYPOINT")) {
				str1 = hash1.get(compareWhat).toString().substring(2)
						.toLowerCase();
				str2 = hash2.get(compareWhat).toString().substring(2)
						.toLowerCase();
			}

			if (this.compareWhat.equals("DISTANCE")) {
				dbl1 = Common.parseDouble(str1.substring(0, str1.length() - 3));
				dbl2 = Common.parseDouble(str2.substring(0, str2.length() - 3));
				if (dbl1 > dbl2)
					return 1;
				if (dbl1 < dbl2)
					return -1;
				else
					return 0;
			} else {
				return str1.compareTo(str2);
			}
		}
	}

}
