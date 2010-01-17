package CacheWolf.beans;

import CacheWolf.navi.GeodeticCalculator;
import CacheWolf.navi.GkPoint;
import CacheWolf.navi.TrackPoint;
import CacheWolf.navi.TransformCoordinates;
import CacheWolf.util.Common;
import CacheWolf.util.MyLocale;
import CacheWolf.util.ParseLatLon;

import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.proj.coords.MGRSPoint;
import com.stevesoft.ewe_pat.Regex;

import ewe.sys.Convert;

/**
 * Class for getting an setting coords in different formats and for doing
 * projection and calculation of bearing and distance
 * 
 */
public class CWPoint extends TrackPoint {
	private MGRSPoint utm = new MGRSPoint();
	private boolean utmValid = false;

	public static final int DD = 0;
	public static final int DMM = 1;
	public static final int DMS = 2;
	public static final int UTM = 3;
	public static final int GK = 4;
	public static final int CW = 5;
	private static final int REGEX = 6;
	private static final int LAT_LON = 7;
	private static final int LON_LAT = 8;

	/**
	 * Create CWPoint by using lat and lon
	 * 
	 * @param lat
	 *            Latitude as decimal
	 * @param lon
	 *            Longitude as decimal
	 */
	public CWPoint(double lat, double lon) {
		super(lat, lon);
		this.utmValid = false;
	}

	/**
	 * Creates an empty CWPoint, use set methods for filling
	 */

	public CWPoint() {
		super(-361, -361); // construct with unvalid == unset lat/lon
		this.utmValid = false;

	}

	/**
	 * Create CWPoint by using a CWPoint
	 * 
	 * @param CWPoint
	 *            LatLonPoint
	 */

	public CWPoint(TrackPoint cwPoint) {
		super(cwPoint.getLatDec(), cwPoint.getLonDec());
		this.utmValid = false;
	}

	/**
	 * set lat and lon by parsing coordinates with Regex
	 * 
	 * @param coord
	 *            String like N 49° 33.167 E 011° 21.608
	 */
	public CWPoint(String coord) {
		set(coord);
	}

	/**
	 * set lat and lon by parsing coordinates with regular expression
	 * 
	 * @param coord
	 *            String of type N 49° 33.167 E 011° 21.608 or -12.3456 23.4567
	 *            or 32U 2345234 8902345
	 */
	public void set(String coord) {
		// replace non-breaking-spaces by normal spaces
		coord = coord.replace((char) 0xA0, ' ');
		/*
		 * (?:
		 * ([NSns])\s*([0-9]{1,2})[\s°]+([0-9]{1,2})(?:\s+([0-9]{1,2}))?[,.](
		 * [0-9]{1,8})\s*
		 * ([EWewOo])\s*([0-9]{1,3})[\s°]+([0-9]{1,2})(?:\s+([0-9]
		 * {1,2}))?[,.]([0-9]{1,8}) )|(?:
		 * ([+-NnSs]?[0-9]{1,2})[,.]([0-9]{1,8})(?
		 * :(?=\+)|(?=-)|\s+|\s*°\s*)([+-WwEeOo
		 * ]?[0-9]{1,3})[,.]([0-9]{1,8})\s*[°]? )|(?:
		 * ([0-9]{1,2}[C-HJ-PQ-X])\s*[
		 * EeOo]?\s*([0-9]{1,7})\s+[Nn]?\s*([0-9]{1,7}) )
		 */
		Regex rex = new Regex(
				"(?:"
						+ "([NSns])\\s*([0-9]{1,2})(?:[°\uC2B0]\\s*|\\s+[°\uC2B0]?\\s*)([0-9]{1,2})(?:(?:['’]\\s*|\\s+['’]?\\s*)([0-9]{1,2}))?(?:[,.]([0-9]{1,8}))?\\s*['’\"]?\\s*"
						+ "[,./_;+:-]*\\s*"
						+ // allow N xx xx.xxx / E xxx xx.xxx
						"([EWewOo])\\s*([0-9]{1,3})(?:[°\uC2B0]\\s*|\\s+[°\uC2B0]?\\s*)([0-9]{1,2})(?:(?:['’]\\s*|\\s+['’]?\\s*)([0-9]{1,2}))?(?:[,.]([0-9]{1,8}))?\\s*['’\"]?"
						+ ")|(?:"
						+ "(?:([NnSs])\\s*(?![+-]))?"
						+ "([+-]?[0-9]{1,2})[,.]([0-9]{1,8})(?:(?=[+-EeWwOo])|\\s+|\\s*[°\uC2B0]\\s*)"
						+ "(?:([EeWwOo])\\s*(?![+-]))?"
						+ "([+-]?[0-9]{1,3})[,.]([0-9]{1,8})\\s*[°\uC2B0]?"
						+ ")|(?:"
						+ "([0-9]{1,2}[C-HJ-PQ-X])\\s*[EeOo]?\\s*([0-9]{1,7})\\s+[Nn]?\\s*([0-9]{1,7})"
						+ ")|(?:"
						+ "[Rr]:?\\s*([+-]?[0-9]{1,7})\\s+[Hh]:?\\s*([+-]?[0-9]{1,7})"
						+ ")");
		this.setLatDec(-91); // return unset / unvalid values if parsing was not
		// successfull
		this.setLonDec(-361);
		rex.search(coord);
		if (rex.stringMatched(1) != null) { // Std format
			// Handle "E" oder "O" for longitiude
			String strEW = rex.stringMatched(6).toUpperCase();
			if (!strEW.equals("W"))
				strEW = "E";
			if (rex.stringMatched(4) != null) { // Seconds available
				set(rex.stringMatched(1).toUpperCase(), rex.stringMatched(2),
						rex.stringMatched(3), rex.stringMatched(4) + "."
								+ rex.stringMatched(5), strEW, rex
								.stringMatched(7), rex.stringMatched(8), rex
								.stringMatched(9)
								+ "." + rex.stringMatched(10), DMS);
			} else {
				set(rex.stringMatched(1).toUpperCase(), rex.stringMatched(2),
						rex.stringMatched(3) + "." + rex.stringMatched(5),
						null, strEW, rex.stringMatched(7), rex.stringMatched(8)
								+ "." + rex.stringMatched(10), null, DMM);
			}

		} else if (rex.stringMatched(12) != null) { // Decimal

			set(rex.stringMatched(11) == null ? "N" : rex.stringMatched(11)
					.toUpperCase(), rex.stringMatched(12) + "."
					+ rex.stringMatched(13), null, null,
					rex.stringMatched(14) == null ? "E" : rex.stringMatched(14)
							.toUpperCase(), rex.stringMatched(15) + "."
							+ rex.stringMatched(16), null, null, DD);
		} else if (rex.stringMatched(17) != null) { // UTM
			set(rex.stringMatched(17), rex.stringMatched(19), rex
					.stringMatched(18)); // parse sequence is E N, but set
			// needs
			// N E
		} else if (rex.stringMatched(20) != null) { // GK
			set(rex.stringMatched(20), rex.stringMatched(21));
		}
		// else Vm.debug("CWPoint: "+coord+" could not be parsed");
	}

	/**
	 * set lat and lon
	 * 
	 * @param strLatNS
	 *            "N" or "S"
	 * @param strLatDeg
	 *            Degrees of Latitude
	 * @param strLatMin
	 *            Minutes of Latitude
	 * @param strLatSec
	 *            Seconds of Latitude
	 * @param strLonEW
	 *            "E" or "W"
	 * @param strLonDeg
	 *            Degrees of Longitude
	 * @param strLonMin
	 *            Minutes of Longitude
	 * @param strLonSec
	 *            Seconds of Longitude
	 * @param format
	 *            Format: DD, DMM, DMS
	 */
	public void set(String strLatNS, String strLatDeg, String strLatMin,
			String strLatSec, String strLonEW, String strLonDeg,
			String strLonMin, String strLonSec, int format) {
		switch (format) {
		case DD:
			this.setLatDec(Common.parseDouble(strLatDeg));
			this.setLonDec(Common.parseDouble(strLonDeg));
			break;
		case DMM:
			this.setLatDec(Math.abs(Common.parseDouble(strLatDeg))
					+ Math.abs((Common.parseDouble(strLatMin) / 60)));
			this.setLonDec(Math.abs(Common.parseDouble(strLonDeg))
					+ Math.abs((Common.parseDouble(strLonMin) / 60)));
			break;
		case DMS:
			this.setLatDec(Math.abs(Common.parseDouble(strLatDeg))
					+ Math.abs((Common.parseDouble(strLatMin) / 60))
					+ Math.abs((Common.parseDouble(strLatSec) / 3600)));
			this.setLonDec(Math.abs(Common.parseDouble(strLonDeg))
					+ Math.abs((Common.parseDouble(strLonMin) / 60))
					+ Math.abs((Common.parseDouble(strLonSec) / 3600)));
			break;

		default:
			this.setLatDec(91);
			this.setLonDec(361);
		}
		// makeValid();
		// To avoid changing sign twice if we have something like W -34.2345
		if (strLatNS.trim().equals("S") && this.getLatDec() > 0)
			this.setLatDec(this.getLatDec() * (-1));
		if (strLonEW.trim().equals("W") && this.getLonDec() > 0)
			this.setLonDec(this.getLonDec() * (-1));
		this.utmValid = false;
	}

	/**
	 * mark the Point as invalid
	 * 
	 */
	public void makeInvalid() {
		setLatDec(-361);
		setLonDec(91);
	}

	/**
	 * set lat and lon by using UTM coordinates
	 * 
	 * @param strZone
	 *            UTM-zone, e.g. 32U
	 * @param strNorthing
	 *            Northing component
	 * @param strEasting
	 *            Easting component
	 */
	public void set(String strZone, String strNorthing, String strEasting) {
		LatLonPoint ll = new LatLonPoint();

		utm.zone_letter = strZone.charAt(strZone.length() - 1);
		utm.zone_number = Convert.toInt(strZone.substring(0,
				strZone.length() - 1));
		utm.northing = (float) Common.parseDouble(strNorthing);
		utm.easting = (float) Common.parseDouble(strEasting);

		ll = utm.toLatLonPoint(); // returns null if unvalit UTM-coordinates
		if (ll != null) {
			this.utmValid = true;
			this.setLatDec(ll.getLatitude());
			this.setLonDec(ll.getLongitude());
		} else {
			this.setLatDec(91);
			this.setLonDec(361);
		}
	}

	/**
	 * set lat and lon by using GK coordinates
	 * 
	 * @param strEasting
	 *            Easting component
	 * @param strNorthing
	 *            Northing component
	 */
	public void set(String strEasting, String strNorthing) {
		GkPoint gk = new GkPoint(Common.parseDouble(strEasting), Common
				.parseDouble(strNorthing), GkPoint.GERMAN_GK);

		this.setLatDec(TransformCoordinates.germanGkToWgs84(gk).getLatDec());
		this.setLonDec(TransformCoordinates.germanGkToWgs84(gk).getLonDec());
		this.utmValid = false;
	}

	/**
	 * Get degrees of latitude in different formats
	 * 
	 * @param format
	 *            Format: DD, DMM, DMS,
	 */
	public String getLatDeg(int format) {
		switch (format) {
		case DD:
			return Double.toString(this.getLatDec());
		case CW:
		case DMM:
		case DMS:
			return getDMS(getLatDec(), 0, format);
		default:
			return "";
		}
	}

	/**
	 * Get degrees of longitude in different formats
	 * 
	 * @param format
	 *            Format: DD, DMM, DMS,
	 */
	public String getLonDeg(int format) {
		switch (format) {
		case DD:
			return Double.toString(this.getLonDec());
		case CW:
		case DMM:
		case DMS:
			return (((getLonDec() < 100.0) && (getLonDec() > -100.0)) ? "0" : "")
					+ getDMS(getLonDec(), 0, format);
		default:
			return "";
		}
	}

	/**
	 * Get minutes of latitude in different formats
	 * 
	 * @param format
	 *            Format: DD, DMM, DMS,
	 */
	public String getLatMin(int format) {
		return getDMS(getLatDec(), 1, format);
	}

	/**
	 * Get minutes of longitude in different formats
	 * 
	 * @param format
	 *            Format: DD, DMM, DMS,
	 */
	public String getLonMin(int format) {
		return getDMS(getLonDec(), 1, format);
	}

	/**
	 * Get seconds of latitude in different formats
	 * 
	 * @param format
	 *            Format: DD, DMM, DMS,
	 */
	public String getLatSec(int format) {
		return getDMS(getLatDec(), 2, format);
	}

	/**
	 * Get seconds of longitude in different formats
	 * 
	 * @param format
	 *            Format: DD, DMM, DMS,
	 */
	public String getLonSec(int format) {
		return getDMS(getLonDec(), 2, format);
	}

	/**
	 * Returns the degrees or minutes or seconds (depending on parameter what)
	 * formatted as a string To determine the degrees, we need to calculate the
	 * minutes (and seconds) just in case rounding errors propagate. Equally we
	 * need to know the seconds to determine the minutes value.
	 * 
	 * @param deg
	 *            The coordinate in degrees
	 * @param what
	 *            0=deg, 1=min, 2=sec
	 * @param format
	 *            DD,CW,DMM,DMS
	 * @return
	 */
	private String getDMS(double deg, int what, int format) {
		deg = Math.abs(deg);
		long iDeg = (int) deg;
		double tmpMin, tmpSec;
		tmpMin = (deg - iDeg) * 60.0;
		switch (format) {
		case DD:
			return "";
		case CW:
		case DMM:
			// Need to check if minutes would round up to 60
			if (java.lang.Math.round(tmpMin * 1000.0) == 60000) {
				tmpMin = 0;
				iDeg++;
			}
			switch (what) {
			case 0:
				return MyLocale.formatLong(iDeg, "00");
			case 1:
				return MyLocale.formatDouble(tmpMin, "00.000")
						.replace(',', '.');
			case 2:
				return "";
			}
		case DMS:
			tmpSec = (tmpMin - (int) tmpMin) * 60.0;
			tmpMin = (int) tmpMin;
			// Check if seconds round up to 60
			if (java.lang.Math.round(tmpSec * 10.0) == 600) {
				tmpSec = 0;
				tmpMin = tmpMin + 1.0;
			}
			// Check if minutes round up to 60
			if (java.lang.Math.round(tmpMin) == 60) {
				tmpMin = 0;
				iDeg++;
			}
			switch (what) {
			case 0:
				return MyLocale.formatLong(iDeg, "00");
			case 1:
				return MyLocale.formatDouble(tmpMin, "00");
			case 2:
				return MyLocale.formatDouble(tmpSec, "00.0").replace(',', '.');
			}
		}
		return ""; // Dummy to keep compiler happy
	}

	/**
	 * Get "N" or "S" letter for latitude
	 */
	public String getNSLetter() {
		String result = "N";
		if (this.getLatDec() >= -90 && this.getLatDec() < 0) {
			result = "S";
		}
		return result;
	}

	/**
	 * Get "E" or "W" letter for latitude
	 */
	public String getEWLetter() {
		String result = "E";
		if (this.getLonDec() >= -180 && this.getLonDec() < 0) {
			result = "W";
		}
		return result;
	}

	/**
	 * Get UTMzonenumber, e.g. 32U
	 */
	public String getUTMZone() {
		checkUTMvalid();
		return Convert.toString(utm.zone_number) + utm.zone_letter;
	}

	/**
	 * Get UTM northing
	 */
	public String getUTMNorthing() {
		checkUTMvalid();
		return Convert.toString((long) utm.northing).replace(',', '.');
	}

	/**
	 * Get UTM easting
	 */
	public String getUTMEasting() {
		checkUTMvalid();
		return Convert.toString((long) utm.easting).replace(',', '.');
	}

	/**
	 * Get GK northing
	 */
	public String getGKNorthing(int decimalplaces) {
		double gkNorthing = TransformCoordinates.wgs84ToGermanGk(this)
				.getNorthing();

		ewe.sys.Double n = new ewe.sys.Double();
		n.set(gkNorthing);
		n.decimalPlaces = decimalplaces;
		return n.toString().replace(',', '.');
	}

	/**
	 * Get GK easting
	 */
	public String getGKEasting(int decimalplaces) {
		double gkEasting = TransformCoordinates.wgs84ToGermanGk(this)
				.getGkEasting(GkPoint.GERMAN_GK);

		ewe.sys.Double e = new ewe.sys.Double();
		e.set(gkEasting);
		e.decimalPlaces = decimalplaces;
		return e.toString().replace(',', '.');
	}

	public String getGermanGkCoordinates() {
		return TransformCoordinates.wgs84ToGermanGk(this).toString(0, "R:",
				" H:", GkPoint.GERMAN_GK);
	}

	/**
	 * Method to calculate a projected waypoint
	 * 
	 * @param degrees
	 *            Bearing
	 * @param distance
	 *            Distance in km
	 * @return projected waypoint
	 */
	public CWPoint project(double degrees, double distance) {
		return new CWPoint(GeodeticCalculator.calculateEndingGlobalCoordinates(
				TransformCoordinates.WGS84, this, degrees, distance * 1000.0));
	}

	/**
	 * Method to calculate the bearing of a waypoint
	 * 
	 * @param dest
	 *            waypoint
	 * @return bearing of waypoint 361 if this or dest is not valid
	 */
	public double getBearing(CWPoint dest) {
		if (!this.isValid() || dest == null || !dest.isValid())
			return 361;

		return GeodeticCalculator.calculateBearing(TransformCoordinates.WGS84,
				this, dest);
	}

	/**
	 * Method to calculate the distance to a waypoint
	 * 
	 * @param dest
	 *            waypoint
	 * @return distance to waypoint in KM
	 */
	public double getDistance(CWPoint dest) {
		return GeodeticCalculator.calculateDistance(TransformCoordinates.WGS84,
				this, dest) / 1000.0;
	}

	/**
	 * Returns the string reprenstation of the CWPoint Format ist CacheWolf (N
	 * 49° 33.167 E 011° 21.608), which can be used with parseLatLon
	 * 
	 * @return string like N 49° 33.167 E 011° 21.608
	 */
	public String toString() {
		return toString(CW);

	}

	/**
	 * Returns the string representation of the CWPoint Formats DD, DMM (same as
	 * CW), DMS, UTM
	 * 
	 * @return string representation of CWPoint
	 */
	public String toString(int format) {
		if (!isValid())
			return MyLocale.getMsg(999, "not set");
		switch (format) {
		case DD:
			return getNSLetter() + " " + getLatDeg(format).replace("-", "")
					+ "° " + getEWLetter() + " "
					+ getLonDeg(format).replace("-", "") + "°";
		case CW:
			format = DMM;
			return getNSLetter() + " " + getLatDeg(format) + "° "
					+ getLatMin(format) + " " + getEWLetter() + " "
					+ getLonDeg(format) + "° " + getLonMin(format);
		case DMM:
			return getNSLetter() + " " + getLatDeg(format) + "° "
					+ getLatMin(format) + " " + getEWLetter() + " "
					+ getLonDeg(format) + "° " + getLonMin(format);
		case DMS:
			return getNSLetter() + " " + getLatDeg(format) + "° "
					+ getLatMin(format) + "\' " + getLatSec(format) + "\" "
					+ getEWLetter() + " " + getLonDeg(format) + "° "
					+ getLonMin(format) + "\' " + getLonSec(format) + "\"";
		case UTM:
			return getUTMZone() + " E " + getUTMEasting() + " N "
					+ getUTMNorthing();
		case LON_LAT:
			return Common.DoubleToString(getLonDec(), 8) + ","
					+ Common.DoubleToString(getLatDec(), 8);
		case LAT_LON:
			return Common.DoubleToString(getLatDec(), 8) + ","
					+ Common.DoubleToString(getLonDec(), 8);
		case GK:
			return getGermanGkCoordinates();
		default:
			return "Unknown Format: " + format;

		}

	}

	/**
	 * Checks, if the data of utm is valid, if not, utm ist calculated
	 */
	private void checkUTMvalid() {
		if (this.utmValid)
			return;
		this.utm = MGRSPoint
				.LLtoMGRS(new LatLonPoint(this.getLatDec(), this.getLonDec()));
		this.utmValid = true;
	}
}
