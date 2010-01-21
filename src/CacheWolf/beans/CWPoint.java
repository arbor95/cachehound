package CacheWolf.beans;

import CacheWolf.navi.GeodeticCalculator;
import CacheWolf.navi.GkPoint;
import CacheWolf.navi.TransformCoordinates;
import CacheWolf.util.Common;
import CacheWolf.util.MyLocale;

import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.proj.coords.MGRSPoint;

import ewe.sys.Convert;

/**
 * Class for getting an setting coords in different formats and for doing
 * projection and calculation of bearing and distance
 * 
 */
public class CWPoint {
	private double latDec;
	private double lonDec;

	private MGRSPoint utm = new MGRSPoint();
	private boolean utmValid = false;

	public static final int DD = 0;
	public static final int DMM = 1;
	public static final int DMS = 2;
	public static final int UTM = 3;
	public static final int GK = 4;
	public static final int CW = 5;
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
		latDec = lat;
		lonDec = lon;
		this.utmValid = false;
	}

	/**
	 * Creates an empty CWPoint, use set methods for filling
	 */

	public CWPoint() {
		this(-361, -361); // construct with unvalid == unset lat/lon
		this.utmValid = false;

	}

	/**
	 * Create CWPoint by using a CWPoint
	 * 
	 * @param CWPoint
	 *            LatLonPoint
	 */

	public CWPoint(CWPoint cwPoint) {
		this(cwPoint.getLatDec(), cwPoint.getLonDec());
		this.utmValid = false;
	}

	public double getLatDec() {
		return latDec;
	}

	public double getLonDec() {
		return lonDec;
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

	public boolean equals(CWPoint tp) {
		return (Math.abs(latDec - tp.latDec) < 1e-10)
				&& (Math.abs(lonDec - tp.lonDec) < 1e-10);
	}

	/**
	 * Returns true if the coordinates are valid
	 */
	public boolean isValid() {
		return latDec <= 90.0 && latDec >= -90.0 && lonDec <= 360
				&& lonDec >= -360;
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
