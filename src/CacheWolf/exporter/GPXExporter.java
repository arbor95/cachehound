package CacheWolf.exporter;

import CacheWolf.Global;
import CacheWolf.beans.CacheHolder;
import CacheWolf.util.SafeXML;
import de.cachehound.beans.CacheHolderDetail;
import de.cachehound.util.Rot13;
import ewe.io.FileBase;
import ewe.sys.Time;
import ewe.sys.Vm;

/**
 * Class to export the cache database to a GPX file with gc.com extensions.<br>
 * Export of logs is not that nice. The cause is that CacheWolf does not spider
 * logs individually, rather all logs as a single entity. ClassID = 2000
 */
public class GPXExporter extends Exporter {

	private final static String STRING_TRUE = "True";
	private final static String STRING_FALSE = "False";
	private final static String DEFAULT_DATE = "2000-01-01";

	public GPXExporter() {
		super();
		this.setMask("*.gpx");
		this.setNeedCacheDetails(true);
		this.setHowManyParams(LAT_LON | COUNT);
		this.setTmpFileName(FileBase.getProgramDirectory() + "/temp.gpx");
	}

	public String header() {
		StringBuilder strBuf = new StringBuilder(200);
		Time tim = new Time();

		strBuf.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n");
		strBuf
				.append("<gpx xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"1.0\" creator=\"Groundspeak Pocket Query\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd http://www.groundspeak.com/cache/1/0 http://www.groundspeak.com/cache/1/0/cache.xsd\" xmlns=\"http://www.topografix.com/GPX/1/0\">\r\n");
		if (Global.getPref().exportGpxAsMyFinds) {
			strBuf.append("  <name>My Finds Pocket Query</name>\r\n");
		}
		strBuf
				.append("  <desc>Geocache file generated by CacheWolf</desc>\r\n");
		strBuf.append("  <author>CacheWolf</author>\r\n");
		strBuf.append("  <email>test@test.com</email>\r\n");
		tim = tim.setFormat("yyyy-MM-dd");
		tim = tim.setToCurrentTime();
		strBuf.append(" <time>" + tim.toString()
				+ "T00:00:00.0000000-07:00</time>\r\n");

		return strBuf.toString();
	}

	public String record(CacheHolder ch, String lat, String lon, int counter) {
		StringBuilder strBuf = new StringBuilder(1000);
		CacheHolderDetail det = ch.getExistingDetails();
		try {
			strBuf
					.append("  <wpt lat=\"" + lat + "\" lon=\"" + lon
							+ "\">\r\n");

			String tim = ch.getDateHidden().length() > 0 ? ch.getDateHidden()
					: DEFAULT_DATE;
			strBuf.append("    <time>").append(tim.toString()).append(
					"T00:00:00.0000000-07:00</time>\r\n");
			strBuf.append("    <name>").append(ch.getWayPoint()).append(
					"</name>\r\n");
			if (ch.isAddiWpt()) {
				strBuf.append("    <cmt>").append(
						SafeXML.cleanGPX(det.getLongDescription())).append(
						"</cmt>\r\n");
			}
			strBuf.append("    <desc>").append(
					SafeXML.cleanGPX(ch.getCacheName())).append(" by ").append(
					SafeXML.cleanGPX(ch.getCacheOwner())).append("</desc>\r\n");
			strBuf
					.append(
							"    <url>http://www.geocaching.com/seek/cache_details.aspx?wp=")
					.append(ch.getWayPoint()).append(
							"&amp;Submit6=Find</url>\r\n");
			strBuf.append("    <urlname>").append(
					SafeXML.cleanGPX(ch.getCacheName())).append(" by ").append(
					SafeXML.cleanGPX(ch.getCacheOwner())).append(
					"</urlname>\r\n");
			if (!ch.isAddiWpt()) {
				if (ch.isFound()) {
					strBuf.append("    <sym>Geocache Found</sym>\r\n");
				} else {
					strBuf.append("    <sym>Geocache</sym>\r\n");
				}
				strBuf.append("    <type>Geocache|").append(
						ch.getType().getGcGpxString()).append("</type>\r\n");
				String dummyAvailable = ch.isAvailable() ? STRING_TRUE
						: STRING_FALSE;
				String dummyArchived = ch.isArchived() ? STRING_TRUE
						: STRING_FALSE;
				strBuf
						.append("    <groundspeak:cache id=\"")
						.append(ch.getCacheID())
						.append("\" available=\"")
						.append(dummyAvailable)
						.append("\" archived=\"")
						.append(dummyArchived)
						.append(
								"\" xmlns:groundspeak=\"http://www.groundspeak.com/cache/1/0\">\r\n");
				strBuf.append("      <groundspeak:name>").append(
						SafeXML.cleanGPX(ch.getCacheName())).append(
						"</groundspeak:name>\r\n");
				strBuf.append("      <groundspeak:placed_by>").append(
						SafeXML.cleanGPX(ch.getCacheOwner())).append(
						"</groundspeak:placed_by>\r\n");
				// todo low prio: correct owner-id
				strBuf.append("      <groundspeak:owner id=\"23\">").append(
						SafeXML.cleanGPX(ch.getCacheOwner())
								+ "</groundspeak:owner>\r\n");
				strBuf.append("      <groundspeak:type>").append(
						ch.getType().getGcGpxString()).append(
						"</groundspeak:type>\r\n");
				strBuf.append("      <groundspeak:container>").append(
						ch.getCacheSize().getAsString()).append(
						"</groundspeak:container>\r\n");
				// for Colorado/Oregon: 2.0 -> 2
				String diffTerr = ch.getDifficulty().getShortRepresentation();

				strBuf.append("      <groundspeak:difficulty>")
						.append(diffTerr).append(
								"</groundspeak:difficulty>\r\n");
				diffTerr = ch.getTerrain().getShortRepresentation();
				strBuf.append("      <groundspeak:terrain>").append(diffTerr)
						.append("</groundspeak:terrain>\r\n");

				strBuf.append("      <groundspeak:country>").append(
						SafeXML.cleanGPX(det.getCountry())
								+ "</groundspeak:country>\r\n");
				strBuf.append("      <groundspeak:state>").append(
						SafeXML.cleanGPX(det.getState())
								+ "</groundspeak:state>\r\n");

				String dummyHTML = ch.isHTML() ? STRING_TRUE : STRING_FALSE;
				strBuf.append("      <groundspeak:long_description html=\"")
						.append(dummyHTML).append("\">\r\n");
				strBuf.append("      ").append(
						SafeXML.cleanGPX(det.getLongDescription()));
				strBuf.append("      \n</groundspeak:long_description>\r\n");
				strBuf.append("	  <groundspeak:encoded_hints>").append(
						SafeXML.cleanGPX(Rot13.encodeRot13(det.getHints())))
						.append("</groundspeak:encoded_hints>\r\n");
				strBuf.append("      <groundspeak:logs>\r\n");
				if (Global.getPref().exportGpxAsMyFinds && ch.isFound()) {
					if (det.getOwnLogId().length() != 0) {
						strBuf.append("        <groundspeak:log id=\"").append(
								det.getOwnLogId()).append("\">\r\n");
					} else {
						strBuf.append("        <groundspeak:log id=\"").append(
								Integer.toString(counter)).append("\">\r\n");
					}
					strBuf.append("          <groundspeak:date>").append(
							SafeXML.cleanGPX(ch.GetStatusDate())).append("T")
							.append(SafeXML.cleanGPX(ch.GetStatusTime()))
							.append(":00</groundspeak:date>\r\n");
					if (det.getOwnLog() != null) {
						strBuf.append("          <groundspeak:type>").append(
								det.getOwnLog().getLogType().toGcComType())
								.append("</groundspeak:type>\r\n");
					} else {
						strBuf
								.append("          <groundspeak:type>Found it</groundspeak:type>\r\n");
					}
					strBuf
							.append("          <groundspeak:finder id=\"")
							.append(
									SafeXML
											.cleanGPX(Global.getPref().gcMemberId))
							.append("\">").append(
									SafeXML.cleanGPX(Global.getPref().myAlias))
							.append("</groundspeak:finder>\r\n");
					if (det.getOwnLog() != null) {
						strBuf
								.append(
										"          <groundspeak:text encoded=\"False\">")
								.append(
										SafeXML.cleanGPX(det.getOwnLog()
												.getMessage())).append(
										"</groundspeak:text>\r\n");
					} else {
						strBuf
								.append("          <groundspeak:text encoded=\"False\"></groundspeak:text>\r\n");
					}
					strBuf.append("        </groundspeak:log>\r\n");
				} else {
					int numberOfLogs = java.lang.Math.min(
							Global.getPref().numberOfLogsToExport, det
									.getCacheLogs().size());
					if (numberOfLogs < 0)
						numberOfLogs = det.getCacheLogs().size();
					for (int i = 0; i < numberOfLogs; i++) {
						strBuf.append("        <groundspeak:log id=\"").append(
								Integer.toString(i)).append("\">\r\n");
						strBuf.append("          <groundspeak:date>").append(
								SafeXML.cleanGPX(det.getCacheLogs().getLog(i)
										.getDate())).append(
								"T00:00:00</groundspeak:date>\r\n");
						strBuf.append("          <groundspeak:type>").append(
								det.getCacheLogs().getLog(i).getLogType()
										.toGcComType()).append(
								"</groundspeak:type>\r\n");
						strBuf.append("          <groundspeak:finder id=\"\">")
								.append(
										SafeXML.cleanGPX(det.getCacheLogs()
												.getLog(i).getLogger()))
								.append("</groundspeak:finder>\r\n");
						strBuf
								.append(
										"          <groundspeak:text encoded=\"False\">")
								.append(
										SafeXML.cleanGPX(det.getCacheLogs()
												.getLog(i).getMessage()))
								.append("</groundspeak:text>\r\n");
						strBuf.append("        </groundspeak:log>\r\n");
					}
				}
				strBuf.append("      </groundspeak:logs>\r\n");
				if (Global.getPref().exportTravelbugs
						&& (det.getTravelbugs().size() > 0)) {
					det.getTravelbugs().size();
					strBuf.append("      <groundspeak:travelbugs>\r\n");
					for (int i = 0; i < det.getTravelbugs().size(); i++) {
						strBuf.append("        <groundspeak:travelbug id=\"")
								.append(Integer.toString(i)).append(
										"\" ref=\"TB\">\r\n");
						strBuf.append("          <groundspeak:name>").append(
								SafeXML.cleanGPX(det.getTravelbugs().getTB(i)
										.getName())).append(
								"</groundspeak:name>\r\n");
						strBuf.append("        </groundspeak:travelbug>\r\n");
					}
					strBuf.append("      </groundspeak:travelbugs>\r\n");
				} else {
					strBuf.append("      <groundspeak:travelbugs />\r\n");
				}
				strBuf.append("    </groundspeak:cache>\r\n");
			} else {
				// there is no HTML in the description of addi wpts
				strBuf.append("    <sym>")
						.append(ch.getType().getGcGpxString()).append(
								"</sym>\r\n");
				strBuf.append("    <type>Waypoint|").append(
						ch.getType().getGcGpxString()).append("</type>\r\n");
			}
			strBuf.append("  </wpt>\r\n");
		} catch (Exception e) {
			Vm.debug(ch.getWayPoint());
			e.printStackTrace();
			return null;
		}// try

		return strBuf.toString();
	}

	public String trailer(int total) {
		return "</gpx>\r\n";
	}

}
