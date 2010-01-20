package de.cachehound.factory;

import static de.cachehound.factory.CWPointFactory.EWHemisphere.W;
import static de.cachehound.factory.CWPointFactory.NSHemisphere.S;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import CacheWolf.beans.CWPoint;
import CacheWolf.navi.GkPoint;
import CacheWolf.navi.TransformCoordinates;

import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.proj.coords.MGRSPoint;

import ewe.sys.Convert;

public class CWPointFactory {
	// private static Logger logger = LoggerFactory
	// .getLogger(CWPointFactory.class);

	private static CWPointFactory cwPointFactory = new CWPointFactory();

	private CWPointFactory() {
		// Singleton Pattern
	}

	public enum NSHemisphere {
		N, S
	}

	public enum EWHemisphere {
		E, W;

		public static EWHemisphere fromString(String s) {
			String s_ = s.toUpperCase();
			if (s_.equals("W")) {
				return W;
			} else if (s_.equals("E")) {
				return E;
			} else if (s_.equals("O")) {
				return E;
			} else {
				throw new IllegalArgumentException(s
						+ " is not a valid EWHemisphere.");
			}
		}
	}

	public static CWPointFactory getInstance() {
		return cwPointFactory;
	}

	public CWPoint createInvalid() {
		return new CWPoint();
	}

	public CWPoint fromD(double lat, double lon) {
		return new CWPoint(lat, lon);
	}

	public CWPoint fromHD(NSHemisphere ns, double lat, EWHemisphere ew,
			double lon) {
		double latmul = ns == S ? -1 : 1;
		double lonmul = ew == W ? -1 : 1;

		return fromD(lat * latmul, lon * lonmul);
	}

	public CWPoint fromHDM(NSHemisphere ns, int latdec, double latmin,
			EWHemisphere es, int londec, double lonmin) {
		return fromHD(ns, latdec + latmin / 60, es, londec + lonmin / 60);
	}

	public CWPoint fromHDMS(NSHemisphere ns, int latdec, int latmin,
			double latsec, EWHemisphere es, int londec, int lonmin,
			double lonsec) {
		return fromHDM(ns, latdec, latmin + latsec / 60, es, londec, lonmin
				+ lonsec / 60);
	}

	public CWPoint fromGermanGK(double easting, double northing) {
		GkPoint gk = new GkPoint(easting, northing, GkPoint.GERMAN_GK);

		return TransformCoordinates.germanGkToWgs84(gk);
	}

	public CWPoint fromUTM(String zone, double easting, double northing) {
		MGRSPoint utm = new MGRSPoint();

		utm.zone_letter = zone.charAt(zone.length() - 1);
		utm.zone_number = Convert.toInt(zone.substring(0, zone.length() - 1));
		utm.northing = (float) northing;
		utm.easting = (float) easting;

		LatLonPoint ll = utm.toLatLonPoint();
		// returns null if invalid UTM-coordinates

		if (ll != null) {
			return new CWPoint(ll.getLatitude(), ll.getLongitude());
		} else {
			return createInvalid();
		}
	}
	
	private static Pattern hdPattern = Pattern.compile("\\s*([NSns])\\s*"
	// Hemisphere
			+ "([0-9]{1,2}(?:[,.][0-9]{1,8})?)\\s*[°\\p{Space}]\\s*"
			// Degrees
			+ "[,./_;+:-]*\\s*"
			// Different possible dividers
			+ "([EWewOo])\\s*"
			// Hemisphere
			+ "([0-9]{1,3}(?:[,.][0-9]{1,8})?)\\s*[°\\p{Space}]\\s*"
			// Degrees
			+ "");

	public CWPoint fromHDString(String in) {
		Matcher matcher = hdPattern.matcher(in);

		if (matcher.find()) {
			return fromHD(
			// Trick Eclipse Autoformatter into sane line breaks
					NSHemisphere.valueOf(matcher.group(1).toUpperCase()), //
					Double.parseDouble(matcher.group(2).replace(',', '.')), //
					EWHemisphere.fromString(matcher.group(3)), //
					Double.parseDouble(matcher.group(4).replace(',', '.')));
		}

		return createInvalid();
	}

	private static Pattern hdmPattern = Pattern.compile("\\s*([NSns])\\s*"
	// Hemisphere
			+ "([0-9]{1,2})\\s*[°\\p{Space}]\\s*"
			// Degrees
			+ "([0-9]{1,2}(?:[,.][0-9]{1,8})?)\\s*['’]?\\s*"
			// Minutes
			+ "[,./_;+:-]*\\s*"
			// Different possible dividers
			+ "([EWewOo])\\s*"
			// Hemisphere
			+ "([0-9]{1,3})\\s*[°\\p{Space}]\\s*"
			// Degrees
			+ "([0-9]{1,2}(?:[,.][0-9]{1,8})?)\\s*['’]?\\s*"
			// Minutes
			+ "");

	public CWPoint fromHDMString(String in) {
		Matcher matcher = hdmPattern.matcher(in);

		if (matcher.find()) {
			return fromHDM(
			// Trick Eclipse Autoformatter into sane line breaks
					NSHemisphere.valueOf(matcher.group(1).toUpperCase()), //
					Integer.parseInt(matcher.group(2)), //
					Double.parseDouble(matcher.group(3).replace(',', '.')), //
					EWHemisphere.fromString(matcher.group(4)), //
					Integer.parseInt(matcher.group(5)), //
					Double.parseDouble(matcher.group(6).replace(',', '.')));
		}

		return createInvalid();
	}

	private static Pattern hdmsPattern = Pattern.compile("\\s*([NSns])\\s*"
	// Hemisphere
			+ "([0-9]{1,2})\\s*[°\\p{Space}]\\s*"
			// Degrees
			+ "([0-9]{1,2})\\s*['’\\p{Space}]\\s*"
			// Minutes
			+ "([0-9]{1,2}(?:[,.][0-9]{1,8})?)\\s*(?:''|’’|\"|\\s)?\\s*"
			// Seconds
			+ "[,./_;+:-]*\\s*"
			// Different possible dividers
			+ "([EWewOo])\\s*"
			// Hemisphere
			+ "([0-9]{1,3})\\s*[°\\p{Space}]\\s*"
			// Degrees
			+ "([0-9]{1,2})\\s*['’\\p{Space}]\\s*"
			// Minutes
			+ "([0-9]{1,2}(?:[,.][0-9]{1,8})?)\\s*(?:''|’’|\"|\\s)?\\s*"
			// Seconds
			+ "");

	public CWPoint fromHDMSString(String in) {
		Matcher matcher = hdmsPattern.matcher(in);

		if (matcher.find()) {
			return fromHDMS(
			// Trick Eclipse Autoformatter into sane line breaks
					NSHemisphere.valueOf(matcher.group(1).toUpperCase()), //
					Integer.parseInt(matcher.group(2)), //
					Integer.parseInt(matcher.group(3)), //
					Double.parseDouble(matcher.group(4).replace(',', '.')), //
					EWHemisphere.fromString(matcher.group(5)), //
					Integer.parseInt(matcher.group(6)), //
					Integer.parseInt(matcher.group(7)), //
					Double.parseDouble(matcher.group(8).replace(',', '.')));
		}

		return createInvalid();
	}
}
