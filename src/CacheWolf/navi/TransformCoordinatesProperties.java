package CacheWolf.navi;

import CacheWolf.beans.CWPoint;
import CacheWolf.util.MyLocale;
import ewe.io.IOException;
import ewe.io.InputStream;
import ewe.sys.Convert;
import ewe.util.Properties;

/**
 * Class to load the parameters of a datum shift of a map and the projection
 * parameters from an Inputstream by the corresponding EPSG code After
 * instantiation you can simply use to and fromWGS84 to convert between WGS84
 * and the given Coordinate reference system, given by the EPSG code Start
 * offset in the language file: 4920
 * 
 * @author Pfeffer
 * 
 */
public class TransformCoordinatesProperties extends Properties {
	public int epsgCode;

	public TransformCoordinatesProperties(InputStream is) throws IOException {
		super();
		load(is);
		epsgCode = Convert.toInt(getProperty("EpsgCode", "-1"));
		if (epsgCode == -1)
			throw new IllegalArgumentException(MyLocale.getMsg(4922,
					"EPSG code missing in: ")
					+ is.getName());
	}

	public TransformCoordinatesProperties(int epsgcodei) {
		if (!TransformCoordinates.isSupported(epsgcodei))
			throw new IllegalArgumentException(MyLocale.getMsg(4920,
					"EPSG code ")
					+ epsgcodei + MyLocale.getMsg(4921, " not supported"));
		epsgCode = epsgcodei;
	}

	/**
	 * return ll transformed into the desired coordinate reference system if the
	 * prjection is Gauß-Krüger, easting will be put in lonDec and northing in
	 * latDec
	 * 
	 * @param ll
	 * @return
	 */
	public static CWPoint fromWgs84(CWPoint ll, int epsgCode) {
		CWPoint ret = null;
		switch (epsgCode) {
		case TransformCoordinates.EPSG_WGS84:
		case TransformCoordinates.EPSG_ETRS89:
			ret = ll;
		}
		if (ret == null) {
			int region = TransformCoordinates.getGkRegion(epsgCode);
			if (region > 0) {
				GkPoint xy = TransformCoordinates.wgs84ToGaussKrueger(ll,
						epsgCode);
				ret = xy.toCWPoint(region);
			} else {
				throw new IllegalArgumentException(MyLocale.getMsg(4923,
						"fromWgs84: EPSG code ")
						+ epsgCode + MyLocale.getMsg(4921, " not supported"));
			}
		}
		return ret;
	}

	/**
	 * convert any supported coordinate reference system WGS84 if p is a
	 * Gauß-Krüger point, put latdec = northing, londec = easting
	 * 
	 * @param p
	 * @return
	 */
	public static CWPoint toWgs84(CWPoint p, int epsgCode) {
		CWPoint ret = null;
		switch (epsgCode) {
		case TransformCoordinates.EPSG_WGS84:
		case TransformCoordinates.EPSG_ETRS89:
			ret = p;
			break;
		}
		if (ret == null) {
			int region = TransformCoordinates.getGkRegion(epsgCode);
			if (region > 0) {
				GkPoint xy = new GkPoint(p.getLonDec(), p.getLatDec(),
						TransformCoordinates.getGkRegion(epsgCode));
				ret = TransformCoordinates.GkToWgs84(xy, region);
			} else {
				throw new IllegalArgumentException(MyLocale.getMsg(4924,
						"ToWgs84: EPSG code ")
						+ epsgCode + MyLocale.getMsg(4921, " not supported"));
			}
		}
		return ret;
	}
}