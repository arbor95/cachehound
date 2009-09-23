package CacheWolf.exporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import CacheWolf.beans.CWPoint;
import CacheWolf.beans.CacheHolder;
import CacheWolf.util.SafeXML;
import de.cachehound.beans.CacheHolderDetail;
import de.cachehound.types.CacheType;
import ewe.io.BufferedWriter;
import ewe.io.File;
import ewe.io.FileBase;
import ewe.io.FileOutputStream;
import ewe.io.FileWriter;
import ewe.io.IOException;
import ewe.io.InputStream;
import ewe.io.PrintWriter;
import ewe.sys.Handle;
import ewe.ui.ProgressBarForm;
import ewe.util.Hashtable;
import ewe.util.Iterator;
import ewe.util.Vector;
import ewe.util.Map.MapEntry;
import ewe.util.zip.ZipEntry;
import ewe.util.zip.ZipException;
import ewe.util.zip.ZipFile;

/**
 * Class to export the cache database (index) to an KML-File which can be read
 * by Google Earth
 * 
 */
public class KMLExporter extends Exporter {
	
	private static Logger logger = LoggerFactory.getLogger(KMLExporter.class);
	
	private static final String COLOR_FOUND = "ff98fb98";
	private static final String COLOR_OWNED = "ffffaa55";
	private static final String COLOR_AVAILABLE = "ffffffff";
	private static final String COLOR_NOT_AVAILABLE = "ff0000ff";

	private static final int AVAILABLE = 0;
	private static final int FOUND = 1;
	private static final int OWNED = 2;
	private static final int NOT_AVAILABLE = 3;
	private static final int UNKNOWN = 4;

	private String[] categoryNames = { "Available", "Found", "Owned", "Not Available",
			"UNKNOWN" };
	private Hashtable[] outCacheDB = new Hashtable[categoryNames.length];

	public KMLExporter() {
		super();
		this.setMask("*.kml");
	}

	public void doIt(int variant) {
		File outFile;
		String str;
		CacheHolder ch;
		CacheHolder addiWpt;
		ProgressBarForm pbf = new ProgressBarForm();
		Handle h = new Handle();

		if (variant == ASK_FILE) {
			outFile = getOutputFile();
			if (outFile == null)
				return;
		} else {
			outFile = new File(tmpFileName);
		}

		pbf.showMainTask = false;
		pbf.setTask(h, "Exporting ...");
		pbf.exec();

		int counter = cacheDB.countVisible();
		int expCount = 0;
		copyIcons(outFile.getParent());
		buildOutDB();

		try {
			PrintWriter outp = new PrintWriter(new BufferedWriter(
					new FileWriter(outFile)));
			str = this.header();
			if (str != null)
				outp.print(str);
			for (int cat = 0; cat < categoryNames.length; cat++) {
				// skip over empty categories
				if (outCacheDB[cat] == null)
					continue;

				Iterator outLoop = outCacheDB[cat].entries();
				outp.print(startFolder(categoryNames[cat]));

				Vector tmp;
				MapEntry entry;
				while (outLoop.hasNext()) {
					entry = (MapEntry) outLoop.next();
					tmp = (Vector) entry.getValue();
					// skip over empty cachetypes
					if (tmp.size() == 0)
						continue;
					outp.print(startFolder(((CacheType) entry
									.getKey()).getGcGpxString()));

					for (int i = 0; i < tmp.size(); i++) {
						ch = (CacheHolder) tmp.get(i);
						if (ch.isAddiWpt())
							continue;
						expCount++;
						h.progress = (float) expCount / (float) counter;
						h.changed();

						if (ch.getPos().isValid()) {
							str = record(ch, ch.getPos().getLatDeg(CWPoint.DD)
									.replace('.', this.decimalSeparator), ch
									.getPos().getLonDeg(CWPoint.DD).replace(
											'.', this.decimalSeparator));
							if (str != null)
								outp.print(str);
						}
						if (ch.hasAddiWpt()) {
							boolean createdAdditionalWaypointsFolder = false;
							for (int j = 0; j < ch.getAddiWpts().size(); j++) {
								addiWpt = ch.getAddiWpts().get(j);
								expCount++;
								if (ch.getPos().isValid()
										&& addiWpt.isVisible()) {
									if (!createdAdditionalWaypointsFolder) {
										outp.print(startFolder(
												"Additional Waypoints", false));
										createdAdditionalWaypointsFolder = true;
									}
									str = record(addiWpt, addiWpt.getPos()
											.getLatDeg(CWPoint.DD).replace('.',
													this.decimalSeparator),
											addiWpt.getPos().getLonDeg(
													CWPoint.DD).replace('.',
													this.decimalSeparator));
									if (str != null)
										outp.print(str);
								}

							}
							if (createdAdditionalWaypointsFolder) {
								outp.print(endFolder());// addi wpts
							}
						}
					}
					outp.print(endFolder());// cachetype
				}
				outp.print(endFolder());// category
			}

			str = trailer();
			if (str != null)
				outp.print(str);
			outp.close();
			pbf.exit(0);
		} catch (IOException e) {
			logger.error("Error opening " + outFile.getName(), e);
		}
		// try

	}

	private void buildOutDB() {
		CacheHolder ch;
		Vector tmp;
		Iterator categoryLoop;
		MapEntry entry;
		boolean foundOne;

		// create the roots for the different categories
		for (int i = 0; i < categoryNames.length; i++) {
			outCacheDB[i] = new Hashtable();
			// create the roots for the cachetypes
			for (CacheType type : CacheType.values()) {
				outCacheDB[i].put(type, new Vector());
			}
		}

		// fill structure with data from cacheDB
		for (int i = 0; i < cacheDB.size(); i++) {
			ch = cacheDB.get(i);
			// TODO Das Argument nach STring zu casten gefďż˝llt mir nicht
			// ganz...
			if (ch.isVisible() && !ch.isAddiWpt()) {
				if (ch.isFound()) {
					tmp = (Vector) outCacheDB[FOUND].get(ch
							.getType());
				} else if (ch.isOwned()) {
					tmp = (Vector) outCacheDB[OWNED].get(ch
							.getType());
				} else if (ch.isArchived() || !ch.isAvailable()) {
					tmp = (Vector) outCacheDB[NOT_AVAILABLE].get(ch.getType());
				} else if (ch.isAvailable()) {
					tmp = (Vector) outCacheDB[AVAILABLE].get(ch
							.getType());
				} else {
					tmp = (Vector) outCacheDB[UNKNOWN].get(ch
							.getType());
				}

				tmp.add(ch);
			}
		}

		// eleminate empty categories
		for (int i = 0; i < categoryNames.length; i++) {
			categoryLoop = outCacheDB[i].entries();
			foundOne = false;
			// look if all vectors for cachetypes are filled
			while (categoryLoop.hasNext()) {
				entry = (MapEntry) categoryLoop.next();
				tmp = (Vector) entry.getValue();
				if (tmp.size() > 0) {
					foundOne = true;
					break;
				}
			}
			// set hashtable for that category to null
			if (!foundOne)
				outCacheDB[i] = null;
		}

	}

	private String startFolder(String name) {
		return startFolder(name, true);
	}

	private String startFolder(String name, boolean open) {
		StringBuilder strBuf = new StringBuilder(200);
		strBuf.append("<Folder>\r\n");
		strBuf.append("<name>" + name + "</name>\r\n");
		strBuf.append("<open>" + (open ? "1" : "0") + "</open>\r\n");

		return strBuf.toString();
	}

	private String endFolder() {

		return "</Folder>\r\n";
	}

	public void copyIcons(String dir) {
		try {
			ZipFile zif = new ZipFile(FileBase.getProgramDirectory()
					+ FileBase.separator + "exporticons" + FileBase.separator
					+ "GoogleEarth.zip");
			ZipEntry zipEnt;
			int len;
			String fileName;

			for (CacheType cacheType : CacheType.values()) {
				fileName = cacheType.getGuiImage();
				zipEnt = zif.getEntry(fileName);
				if (zipEnt == null)
					continue;
				byte[] buff = new byte[zipEnt.getSize()];
				InputStream fis = zif.getInputStream(zipEnt);
				FileOutputStream fos = new FileOutputStream(dir + "/"
						+ fileName);
				while (0 < (len = fis.read(buff)))
					fos.write(buff, 0, len);
				fos.flush();
				fos.close();
				fis.close();
			}

		} catch (ZipException e) {
			logger.error("Problem copying Icon", e);
		} catch (IOException e) {
			logger.error("Problem copying Icon", e);
		}
	}

	public String header() {
		StringBuilder strBuf = new StringBuilder(200);

		strBuf.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n");
		strBuf.append("<kml xmlns=\"http://earth.google.com/kml/2.0\">\r\n");
		strBuf.append("<Folder>\r\n");
		strBuf.append("<name>CacheWolf</name>\r\n");
		strBuf.append("<open>1</open>\r\n");

		return strBuf.toString();
	}

	public String record(CacheHolder ch, String lat, String lon) {
		StringBuilder strBuf = new StringBuilder(200);
		CacheHolderDetail det = ch.getExistingDetails();

		strBuf.append("   <Placemark>\r\n");
		if (det.getUrl() != null) {
			strBuf.append("      <description>" + SafeXML.clean(det.getUrl())
					+ "</description>\r\n");
		}
		strBuf.append("      <name>" + ch.getWayPoint() + " - "
				+ SafeXML.clean(ch.getCacheName()) + "</name>\r\n");
		strBuf.append("      <LookAt>\r\n");
		strBuf.append("         <latitude>" + lat + "</latitude>\r\n");
		strBuf.append("         <longitude>" + lon + "</longitude>\r\n");
		strBuf
				.append("         <range>10000</range><tilt>0</tilt><heading>0</heading>\r\n");
		strBuf.append("      </LookAt>\r\n");
		strBuf.append("      <Point>\r\n");
		strBuf.append("         <coordinates>" + lon + "," + lat
				+ "</coordinates>\r\n");
		strBuf.append("      </Point>\r\n");
		strBuf.append("      <Style>\r\n");
		strBuf.append("      <IconStyle>\r\n");
		strBuf.append("         <Icon>\r\n");
		// strBuf.append(" <href>"+ File.getProgramDirectory()+ "/" +
		// CacheType.type2pic(Convert.parseInt(ch.type))+ "</href>\r\n");
		strBuf.append("            <href>"
				+ ch.getType().getGuiImage() + "</href>\r\n");
		// Die Grafiken sind eigentlich schöner, aber leider nicht alle
		// verfügbar bzw. müssten korrekt kopiert werden.
		// strBuf.append("            <href>"
		// + ch.getType() + ".gif" + "</href>\r\n");
		strBuf.append("         </Icon>\r\n");
		strBuf.append("      </IconStyle>\r\n");
		strBuf.append("      <LabelStyle>\r\n");
		strBuf.append("         <color>" + getColor(ch) + "</color>\r\n");
		strBuf.append("      </LabelStyle>\r\n");
		strBuf.append("      </Style>\r\n");
		strBuf.append("   </Placemark>\r\n");

		return strBuf.toString();
	}

	public String trailer() {
		StringBuilder strBuf = new StringBuilder(50);

		strBuf.append("</Folder>\r\n");
		strBuf.append("</kml>\r\n");

		return strBuf.toString();
	}

	private String getColor(CacheHolder ch) {
		if (ch.isFound())
			return COLOR_FOUND;
		if (ch.isOwned())
			return COLOR_OWNED;
		if (ch.isArchived() || !ch.isAvailable())
			return COLOR_NOT_AVAILABLE;

		return COLOR_AVAILABLE;
	}

}
