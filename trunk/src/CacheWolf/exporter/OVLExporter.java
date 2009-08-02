package CacheWolf.exporter;

import CacheWolf.beans.CWPoint;
import CacheWolf.beans.CacheHolder;
import CacheWolf.util.Common;
import ewe.sys.Convert;

/**
 * Class to export the cache database (index) to an ascii overlay file for the
 * TOP50 map products (mainly available in german speaking countries).
 */
public class OVLExporter extends Exporter {

	public OVLExporter() {
		super();
		this.setMask("*.ovl");
		this.setHowManyParams(LAT_LON | COUNT);
	}

	public String record(CacheHolder ch, String lat, String lon, int counter) {
		StringBuffer str = new StringBuffer(200);
		double tmp;
		str.append("[Symbol " + Convert.toString(2 * counter + 1) + "]\r\n");
		str.append("Typ=6\r\n");
		str.append("Width=15\r\n");
		str.append("Height=15\r\n");
		str.append("Col=1\r\n");
		str.append("Zoom=1\r\n");
		str.append("Size=2\r\n");
		str.append("Area=2\r\n");
		str.append("XKoord=" + lon + "\r\n");
		str.append("YKoord=" + lat + "\r\n");
		// the text
		str.append("[Symbol " + Convert.toString(2 * counter + 2) + "]\r\n");
		str.append("Typ=2\r\n");
		str.append("Col=1\r\n");
		str.append("Zoom=1\r\n");
		str.append("Size=2\r\n");
		str.append("Area=2\r\n");
		str.append("Font=3\r\n");
		str.append("Dir=1\r\n");
		tmp = Common.parseDouble(lon);
		tmp += 0.002;
		str
				.append("XKoord=" + Convert.toString(tmp).replace(',', '.')
						+ "\r\n");
		tmp = Common.parseDouble(lat);
		tmp += 0.002;
		str
				.append("YKoord=" + Convert.toString(tmp).replace(',', '.')
						+ "\r\n");
		str.append("Text=" + ch.getWayPoint() + "\r\n");

		return str.toString();
	}

	public String trailer(int counter) {
		StringBuffer str = new StringBuffer(200);

		str.append("[Overlay]\r\n");
		str.append("Symbols=" + Convert.toString(counter * 2) + "\r\n");
		// maplage section
		str.append("[MapLage]\r\n");
		str.append("MapName=Gesamtes Bundesgebiet (D1000)\r\n");
		str.append("DimmFc=100\r\n");
		str.append("ZoomFc=100\r\n");
		str.append("CenterLat=" + pref.curCentrePt.getLatDeg(CWPoint.CW)
				+ ".00\r\n");
		str.append("CenterLong=" + pref.curCentrePt.getLonDeg(CWPoint.CW)
				+ ".00\r\n");
		str.append("RefColor=255\r\n");
		str.append("RefRad=58\r\n");
		str.append("RefLine=6\r\n");
		str.append("RefOn=0\r\n");
		str.append("\r\n");
		return str.toString();
	}
}
