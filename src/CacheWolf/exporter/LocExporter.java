package CacheWolf.exporter;

import CacheWolf.Global;
import CacheWolf.beans.CWPoint;
import CacheWolf.beans.CacheHolder;
import de.cachehound.beans.CacheHolderDetail;
import de.cachehound.util.Rot13;
import ewe.io.File;
import ewe.io.FileBase;

/**
 * Class to export the cache database into an geocaching .loc file that may be
 * exported by GPSBabel to a Garmin GPS.
 * 
 * Now includes mapping of cachetypes to user defined icons (as defined in file
 * garminmap.xml).
 */
public class LocExporter extends Exporter {
	public static int MODE_AUTO = TMP_FILE;
	/**
	 * Defines how certain cachetypes are mapped to user icons
	 */
	private static GarminMap gm = null;

	public LocExporter() {
		super();
		this.setMask("*.loc");
		this.setHowManyParams(NO_PARAMS);
		if (Global.getPref().addDetailsToName) {
			this.setNeedCacheDetails(true);
		}
		if ((new File(FileBase.getProgramDirectory() + "/garminmap.xml"))
				.exists()) {
			gm = new GarminMap();
			gm.readGarminMap();
		}
	}

	public String header() {
		return "<?xml version=\"1.0\"?><loc version=\"1.0\" src=\"EasyGPS\">\r\n";
	}

	public String record(CacheHolder ch) {

		// filter out not valid coords
		if (!ch.getPos().isValid())
			return null;
		StringBuilder strBuf = new StringBuilder(200);
		strBuf.append("<waypoint>\r\n   <name id=\"");
		String wptName = simplifyString(ch.getWayPoint());
		if (Global.getPref().addDetailsToWaypoint) {
			wptName += getShortDetails(ch);
		}
		if (Global.getPref().garminMaxLen == 0)
			strBuf.append(wptName);
		else {
			try {
				strBuf.append(wptName.substring(wptName.length()
						- Global.getPref().garminMaxLen));
			} catch (Exception ex) {
				pref.log("Invalid value for garmin.MaxWaypointLength");
			}
		}
		strBuf.append("\"><![CDATA[");
		strBuf.append(simplifyString(ch.getCacheName()));
		if (Global.getPref().addDetailsToName) {
			if (!Global.getPref().addDetailsToWaypoint) {
				strBuf.append(getShortDetails(ch));
			}
			CacheHolderDetail det = ch.getExistingDetails();
			if ((!det.getHints().equals("null"))
					&& (det.getHints().length() > 0)) {
				strBuf.append(":");
				strBuf
						.append(simplifyString(Rot13
								.encodeRot13(det.getHints())));
			}
		}
		strBuf.append("]]></name>\r\n   <coord lat=\"");
		strBuf.append(ch.getPos().getLatDeg(CWPoint.DD));
		strBuf.append("\" lon=\"");
		strBuf.append(ch.getPos().getLonDeg(CWPoint.DD));
		strBuf.append("\"/>\r\n   <type>");
		if (gm != null) {
			strBuf.append(gm.getIcon(ch));
		} else {
			if (ch.isFound())
				strBuf.append("Geocache Found");
			else
				strBuf.append("Geocache");
		}
		strBuf.append("</type>\r\n</waypoint>\r\n");
		return strBuf.toString();
	}

	public String trailer() {
		return "</loc>\r\n";
	}
}
