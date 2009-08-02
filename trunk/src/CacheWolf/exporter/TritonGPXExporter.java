package CacheWolf.exporter;

import de.cachehound.beans.CacheHolderDetail;
import de.cachehound.types.LogType;
import CacheWolf.beans.CacheHolder;
import CacheWolf.beans.CacheSize;
import CacheWolf.beans.CacheTerrDiff;
import CacheWolf.beans.CacheType;
import CacheWolf.beans.Preferences;
import CacheWolf.beans.Profile;
import CacheWolf.util.Common;
import CacheWolf.util.SafeXML;
import ewe.io.ByteArrayOutputStream;
import ewe.io.File;
import ewe.io.FileBase;
import ewe.io.FileInputStream;
import ewe.sys.Time;

public class TritonGPXExporter extends Exporter {
	public TritonGPXExporter() {
		setMask("*.gpx");
		setNeedCacheDetails(true);
		setHowManyParams(1);
		setTmpFileName(FileBase.getProgramDirectory() + "/temp.gpx");
	}

	public TritonGPXExporter(Preferences p, Profile prof) {
		setMask("*.gpx");
		setNeedCacheDetails(true);
		setHowManyParams(1);
		setTmpFileName(FileBase.getProgramDirectory() + "/temp.gpx");
	}

	public String header() {
		StringBuffer strBuf = new StringBuffer(200);
		Time tim = new Time();

		strBuf.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n");
		strBuf
				.append("<gpx xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"1.0\" creator=\"Groundspeak Pocket Query\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd http://www.groundspeak.com/cache/1/0 http://www.groundspeak.com/cache/1/0/cache.xsd\" xmlns=\"http://www.topografix.com/GPX/1/0\">\r\n");
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

	public String record(CacheHolder ch, String lat, String lon) {
		StringBuffer strBuf = new StringBuffer(1000);
		CacheHolderDetail chdetail = ch.getExistingDetails();
		try {
			strBuf
					.append("  <wpt lat=\"" + lat + "\" lon=\"" + lon
							+ "\">\r\n");

			String tim = (ch.getDateHidden().length() > 0) ? ch.getDateHidden()
					: "2000-01-01";
			strBuf.append("    <time>").append(tim.toString()).append(
					"T00:00:00.0000000-07:00</time>\r\n");
			if (ch.isAddiWpt())
				strBuf.append("    <name>").append(ch.mainCache.getWayPoint())
						.append(" - ").append(ch.getWayPoint()).append(
								"</name>\r\n");
			else {
				strBuf.append("    <name>").append(ch.getWayPoint()).append(
						"</name>\r\n");
			}

			strBuf.append(spoiler2GPX(chdetail));

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
			if (!(ch.isAddiWpt())) {
				strBuf.append("    <sym>Geocache</sym>\r\n");
				strBuf.append("    <type>Geocache|").append(
						CacheType.id2GpxString(ch.getType())).append(
						"</type>\r\n");
				String dummyAvailable = (ch.is_available()) ? "True" : "False";
				String dummyArchived = (ch.is_archived()) ? "True" : "False";
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
				strBuf.append("      <groundspeak:type>").append(
						CacheType.id2GpxString(ch.getType())).append(
						"</groundspeak:type>\r\n");
				strBuf.append("      <groundspeak:container>").append(
						CacheSize.cw2ExportString(ch.getCacheSize())).append(
						"</groundspeak:container>\r\n");
				// strBuf.append("
				// <groundspeak:difficulty>").append(ch.hard.replace(',',
				// '.')).append("</groundspeak:difficulty>\r\n");
				// strBuf.append("
				// <groundspeak:terrain>").append(ch.terrain.replace(',',
				// '.')).append("</groundspeak:terrain>\r\n");

				String s = "abbc";
				s.replace("bb", "b");

				String diff = CacheTerrDiff.shortDT(ch.getHard());
				strBuf.append("      <groundspeak:difficulty>").append(diff)
						.append("</groundspeak:difficulty>\r\n");
				String terr = CacheTerrDiff.shortDT(ch.getTerrain());
				strBuf.append("      <groundspeak:terrain>").append(terr)
						.append("</groundspeak:terrain>\r\n");

				strBuf.append("      <groundspeak:country/>\n");
				strBuf
						.append("      <groundspeak:state>Nil</groundspeak:state>\n");
				strBuf.append(
						"      <groundspeak:short_description html=\"false\">")
						.append(ch.getWayPoint()).append(" - ").append(
								CacheSize.cw2ExportString(ch.getCacheSize()))
						.append(" D:").append(diff).append("/T:").append(terr)
						.append("</groundspeak:short_description>\r\n");
				strBuf
						.append("      <groundspeak:long_description html=\"false\">\r\n");

				strBuf.append("      ").append(
						SafeXML.removeHtml(SafeXML
								.strxmldecode(chdetail.getLongDescription())))
						.append("\r\n");

				if (!(chdetail.getHints().length() == 0))
					strBuf.append(" ######HINT!####### \r\n").append(
							SafeXML.removeHtml(SafeXML.strxmldecode(Common
									.rot13(chdetail.getHints()))));

				strBuf.append("\r\n ######LOGS!###### \r\n");
				int logCount = chdetail.getCacheLogs().size();
				if (logCount > 5) {
					logCount = 5;
				}
				for (int i = 0; i < logCount; ++i) {
					if (chdetail.getCacheLogs().getLog(i).getLogType() == LogType.FOUND) {
						strBuf.append("[FOUND]");
					} else if (chdetail.getCacheLogs().getLog(i).getLogType() == LogType.DID_NOT_FOUND) {
						strBuf.append("[DNF]");
					} else {
						strBuf.append("[NOTE]");
					}
					strBuf.append(chdetail.getCacheLogs().getLog(i).getDate());
					strBuf.append("").append("from:\"").append(
							SafeXML.strxmldecode(chdetail.getCacheLogs().getLog(i)
									.getLogger())).append("\" \r\n");
					strBuf.append("").append(
							SafeXML.removeHtml(SafeXML
									.strxmldecode(chdetail.getCacheLogs().getLog(i)
											.getMessage()))).append(" \r\n");
				}

				strBuf.append("      \n</groundspeak:long_description>\r\n");

				if (chdetail.getHints().length() == 0)
					// strBuf.append("\t <groundspeak:encoded_hints>No " +
					// ch.LatLon.replace(" ", "") +
					// "</groundspeak:encoded_hints>\r\n");
					strBuf.append("\t  <groundspeak:encoded_hints>No "
							+ ch.LatLon + "</groundspeak:encoded_hints>\r\n");
				else {
					// strBuf.append("\t <groundspeak:encoded_hints>Yes " +
					// ch.LatLon.replace(" ", "") +
					// "</groundspeak:encoded_hints>\r\n");
					strBuf.append("\t  <groundspeak:encoded_hints>Yes "
							+ ch.LatLon + "</groundspeak:encoded_hints>\r\n");
				}
				strBuf.append("      <groundspeak:logs>\r\n");
				strBuf.append("      </groundspeak:logs>\r\n");
				strBuf.append("      <groundspeak:travelbugs />\r\n");
				strBuf.append("    </groundspeak:cache>\r\n");
			} else {
				strBuf.append("    <cmt>").append(
						SafeXML.cleanGPX(chdetail.getLongDescription())).append(
						"</cmt>\r\n");
				strBuf.append("    <sym>").append(
						CacheType.id2GpxString(ch.getType())).append(
						"</sym>\r\n");
				strBuf.append("    <type>Waypoint|").append(
						CacheType.id2GpxString(ch.getType())).append(
						"</type>\r\n");
			}
			strBuf.append("  </wpt>\r\n");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return strBuf.toString();
	}

	public String trailer() {
		return "</gpx>";
	}

	public String spoiler2GPX(CacheHolderDetail ch) {
		String GPX = "";
		String Type = "";
		String GPXImages = "";
		String GPXextenion = "";
		String imagePath = "";
		String imageName = "";
		String oldImageName = "";
		for (int spoiler = 0; spoiler < ch.getImages().size(); ++spoiler) {
			imageName = ch.getImages().get(spoiler).toString();
			if (imageName.equals(oldImageName)) {
				continue;
			}

			imagePath = this.profile.dataDir;

			imagePath = imagePath + "/" + imageName;
			File file = new File(imagePath);
			GPX = addFileBase64(file);
			oldImageName = imageName;

			if (GPX.length() == 0) {
				continue;
			}
			if (imageName.endsWith("jpg")) {
				GPXextenion = ".jpg";
				Type = "jpeg";
			} else {
				GPXextenion = ".gif";
				Type = "gif";
			}

			GPXImages = GPXImages + "    <multimedia name=\""
					+ imageName.replace(GPXextenion, "") + "\" type=\"image/"
					+ Type + "\">\r\n <data><![CDATA[" + GPX
					+ "]]></data>\r\n</multimedia>\r\n";
		}

		return GPXImages;
	}

	public String addFileBase64(File file) {
		byte[] buf = new byte[16384];
		try {
			int help;
			FileInputStream inputFile = new FileInputStream(file);
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();

			while ((help = inputFile.read(buf)) != -1)
				outStream.write(buf, 0, help);
			inputFile.close();

			String encoded = new String(Base64Coder.encode(outStream
					.toByteArray()));
			return encoded;
		} catch (Exception e) {
		}
		return "";
	}

}