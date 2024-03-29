package CacheWolf.util;

import java.awt.Desktop;
import java.net.URI;

import CacheWolf.Global;
import CacheWolf.beans.Attribute;
import CacheWolf.beans.CacheHolder;
import HTML.Template;

import com.stevesoft.ewe_pat.Regex;

import de.cachehound.factory.LogFactory;
import de.cachehound.util.Rot13;
import ewe.io.BufferedWriter;
import ewe.io.FileBase;
import ewe.io.FileWriter;
import ewe.io.PrintWriter;
import ewe.sys.Vm;
import ewe.util.Hashtable;
import ewe.util.Vector;

public class ShowCacheInBrowser {
	private String pd = FileBase.getProgramDirectory();
	private String saveTo = pd + "/temp.html";
	private static Hashtable args = null;

	public ShowCacheInBrowser() {
		if (args == null) {
			args = new Hashtable();
			args.put("filename", pd + "/GCTemplate.html");
			args.put("case_sensitive", "true");
			args.put("loop_context_vars", Boolean.TRUE);
			args.put("max_includes", new Integer(5));
		}
	}

	public void showCache(CacheHolder chD) {
		if (chD == null)
			return;
		try {
			Template tpl = new Template(args);
			if (chD.isVisible()) {
				Vm.showWait(true);
				try {
					// if (chD.getWayPoint().startsWith("OC"))
					// tpl.setParam("TYPE",
					// "\"file://"+FileBase.getProgramDirectory()+"/"+CacheType.transOCType(chD.getType())+".gif\"");
					// else
					tpl.setParam("TYPE", "\"file://"
							+ FileBase.getProgramDirectory() + "/"
							+ chD.getType().getOldCWByte() + ".gif\"");
					tpl.setParam("SIZE", chD.getCacheSize().getAsString());
					tpl.setParam("WAYPOINT", chD.getWayPoint());
					tpl.setParam("CACHE_NAME", chD.getCacheName());
					tpl.setParam("OWNER", chD.getCacheOwner());
					tpl.setParam("DIFFICULTY", chD.getDifficulty()
							.getFullRepresentation());
					tpl.setParam("TERRAIN", chD.getTerrain()
							.getFullRepresentation());
					tpl.setParam("DISTANCE", chD.getDistance()
							.replace(',', '.'));
					tpl.setParam("BEARING", chD.getBearingAsString());
					if (chD.getPos() != null && chD.getPos().isValid()) {
						tpl.setParam("LATLON", chD.getLatLon());
					} else {
						tpl.setParam("LATLON", "unknown");
					}
					// If status is of format yyyy-mm-dd prefix it with a
					// "Found" message in local language
					if (chD.getCacheStatus().length() >= 10
							&& chD.getCacheStatus().charAt(4) == '-')
						tpl.setParam("STATUS", MyLocale.getMsg(318, "Found")
								+ " " + chD.getCacheStatus());
					else
						tpl.setParam("STATUS", chD.getCacheStatus());

					// Cache attributes
					if (chD.getExistingDetails().getAttributes().getCount() > 0) {
						Vector attVect = new Vector(chD.getExistingDetails()
								.getAttributes().getCount() + 1);
						for (int i = 0; i < chD.getExistingDetails()
								.getAttributes().getCount(); i++) {
							Hashtable atts = new Hashtable();
							atts.put("IMAGE", "<img src=\"file://"
									+ Attribute.getImageDir()
									+ chD.getExistingDetails().getAttributes()
											.getName(i)
									+ "\" border=0 alt=\""
									+ chD.getExistingDetails().getAttributes()
											.getInfo(i) + "\">");
							if (i % 5 == 4)
								atts.put("BR", "<br/>");
							else
								atts.put("BR", "");
							atts.put("INFO", chD.getExistingDetails()
									.getAttributes().getInfo(i));
							attVect.add(atts);
						}
						tpl.setParam("ATTRIBUTES", attVect);
					}

					tpl.setParam("DATE", chD.getDateHidden());
					tpl.setParam("URL", chD.getExistingDetails().getUrl());
					if (chD.getExistingDetails().getTravelbugs().size() > 0)
						tpl.setParam("BUGS", chD.getExistingDetails()
								.getTravelbugs().toHtml());
					if (chD.getExistingDetails().getCacheNotes().trim()
							.length() > 0)
						tpl.setParam("NOTES", chD.getExistingDetails()
								.getCacheNotes().replace("\n", "<br/>\n"));
					if (chD.getExistingDetails().getSolver() != null
							&& chD.getExistingDetails().getSolver().trim()
									.length() > 0)
						tpl.setParam("SOLVER", chD.getExistingDetails()
								.getSolver().replace("\n", "<br/>\n"));
					// Look for images

					StringBuilder s = new StringBuilder(chD
							.getExistingDetails().getLongDescription().length());
					int start = 0;
					int pos;
					int imageNo = 0;
					Regex imgRex = new Regex(
							"src=(?:\\s*[^\"|']*?)(?:\"|')(.*?)(?:\"|')");
					while (start >= 0
							&& (pos = chD.getExistingDetails()
									.getLongDescription()
									.indexOf("<img", start)) > 0) {
						if (imageNo >= chD.getExistingDetails().getImages()
								.size())
							break;
						s.append(chD.getExistingDetails().getLongDescription()
								.substring(start, pos));
						imgRex.searchFrom(chD.getExistingDetails()
								.getLongDescription(), pos);
						String imgUrl = imgRex.stringMatched(1);
						// Vm.debug("imgUrl "+imgUrl);
						if (imgUrl.lastIndexOf('.') > 0
								&& imgUrl.toLowerCase().startsWith("http")) {
							String imgType = (imgUrl.substring(
									imgUrl.lastIndexOf(".")).toLowerCase() + "    ")
									.substring(0, 4).trim();
							// If we have an image which we stored when
							// spidering, we can display it
							if (imgType.startsWith(".png")
									|| imgType.startsWith(".jpg")
									|| imgType.startsWith(".gif")) {
								s.append("<img src=\"file://"
										+ Global.getProfile().getDataDir()
										+ chD.getExistingDetails().getImages()
												.get(imageNo).getFilename()
										+ "\">");
								imageNo++;
							}
						}
						start = chD.getExistingDetails().getLongDescription()
								.indexOf(">", pos);
						if (start >= 0)
							start++;
					}
					if (start >= 0)
						s.append(chD.getExistingDetails().getLongDescription()
								.substring(start));
					tpl.setParam("DESCRIPTION", s.toString());

					// Do the remaining pictures which are not included in main
					// body of text
					// They will be hidden initially and can be displayed by
					// clicking on a link
					if (imageNo < chD.getExistingDetails().getImages().size()) {
						Vector imageVect = new Vector(chD.getExistingDetails()
								.getImages().size()
								- imageNo);
						for (; imageNo < chD.getExistingDetails().getImages()
								.size(); imageNo++) {
							Hashtable imgs = new Hashtable();
							imgs.put("IMAGE", "<img src=\"file://"
									+ Global.getProfile().getDataDir()
									+ chD.getExistingDetails().getImages().get(
											imageNo).getFilename()
									+ "\" border=0>");
							imgs.put("IMAGETEXT", chD.getExistingDetails()
									.getImages().get(imageNo).getTitle());
							if (imageNo < chD.getExistingDetails().getImages()
									.size()
									&& chD.getExistingDetails().getImages()
											.get(imageNo).getComment() != null)
								imgs.put("IMAGECOMMENT", chD
										.getExistingDetails().getImages().get(
												imageNo).getComment());
							else
								imgs.put("IMAGECOMMENT", "");
							imgs.put("I", "'img"
									+ new Integer(imageNo).toString() + "'");
							imageVect.add(imgs);
						}
						tpl.setParam("IMAGES", imageVect);
					}

					Vector logVect = new Vector(chD.getExistingDetails()
							.getCacheLogs().size());
					for (int i = 0; i < chD.getExistingDetails().getCacheLogs()
							.size(); i++) {
						Hashtable logs = new Hashtable();
						String log = LogFactory.getInstance().toHtml(
								chD.getExistingDetails().getCacheLogs().getLog(
										i)).replace(
								"http://www.geocaching.com/images/icons/", "");
						int posGt = log.indexOf('>'); // Find the icon which
						// defines the type of
						// log
						if (posGt < 0) {
							logs.put("LOG", log);
							logs.put("LOGTYPE", "");
						} else {
							int posBr = log.indexOf("<br>");
							if (posBr < 0) {
								logs.put("LOG", log);
								logs.put("LOGTYPE", "");
							} else {
								logs.put("LOG", log.substring(posBr));
								logs.put("LOGTYPE", log.substring(0, posGt)
										+ " border='0'"
										+ log.substring(posGt, posBr + 4));
							}
						}
						logs.put("I", "'log" + new Integer(i).toString() + "'");
						logVect.add(logs);
					}
					tpl.setParam("LOGS", logVect);
					if (!chD.isAvailable())
						tpl.setParam("UNAVAILABLE", "1");
					if (!chD.getExistingDetails().getHints().equals("null"))
						tpl.setParam("HINT", Rot13.encodeRot13(chD
								.getExistingDetails().getHints()));

					if (chD.hasAddiWpt()) {
						Vector addiVect = new Vector(chD.getAddiWpts().size());
						for (int i = 0; i < chD.getAddiWpts().size(); i++) {
							Hashtable addis = new Hashtable();
							CacheHolder ch = chD.getAddiWpts().get(i);
							addis.put("WAYPOINT", ch.getWayPoint());
							addis.put("NAME", ch.getCacheName());
							addis.put("LATLON", ch.getLatLon());
							addis.put("IMG", "<img src=\""
									+ ch.getType().getGuiImage() + "\">");
							addis.put("LONGDESC", ch.getExistingDetails()
									.getLongDescription());
							// Do we need to treat longDesc as above?
							addiVect.add(addis);
						}
						tpl.setParam("ADDIS", addiVect);
					}
				} catch (Exception e) {
					Vm.debug("Problem getting Parameter, Cache: "
							+ chD.getWayPoint());
					Global.getPref().log(
							"Problem getting parameter " + e.toString()
									+ ", Cache: " + chD.getWayPoint());
					e.printStackTrace();
				}
			}
			PrintWriter detfile;
			FileWriter fw = new FileWriter(saveTo);
			detfile = new PrintWriter(new BufferedWriter(fw));
			tpl.printTo(detfile);
			// detfile.print(tpl.output());
			detfile.close();

			if (Desktop.isDesktopSupported()) {
				Desktop.getDesktop().browse(new URI("file://" + saveTo));
			} else {
				Global
						.getPref()
						.log(
								"Das System unterstützt das Java Feature 'Desktop' nicht");
			}
		} catch (Exception e) {
			e.printStackTrace();
			Global.getPref().log("Error in ShowCache " + e.toString());
		} finally {
			Vm.showWait(false);
		}
	}
}
