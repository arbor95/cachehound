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
		E, W
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
	
	private static Pattern hdmPattern = Pattern.compile("\\s*([NSns])\\s*"
			// Hemisphere
			+ "([0-9]{1,2})\\s*[°\\p{Space}]\\s*"
			// Degrees
			+ "([0-9]{1,2}(?:[,.][0-9]{1,8})?)\\s*['’]?\\s*"
			// Minutes
			+ "[,./_;+:-]*\\s*"
			// allow N xx xx.xxx / E xxx xx.xxx
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
					NSHemisphere.valueOf(matcher.group(1).toUpperCase()),
					Integer.parseInt(matcher.group(2)), Double
							.parseDouble(matcher.group(3).replace(',', '.')),
					EWHemisphere.valueOf(matcher.group(4).toUpperCase()),
					Integer.parseInt(matcher.group(5)), Double
							.parseDouble(matcher.group(6).replace(',', '.')));
		}

		return createInvalid();
	}
}
