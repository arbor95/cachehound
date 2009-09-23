package CacheWolf.navi;

import CacheWolf.beans.CWPoint;

public class Area {
	public static double edgeTolerance = 3 * 360 / 40000000; // approx 3m
	// will
	// be seen as
	// the same

	CWPoint topleft;
	CWPoint buttomright;

	public Area(TrackPoint tl, TrackPoint br) {
		topleft = new CWPoint(tl);
		buttomright = new CWPoint(br);
	}

	public boolean isInBound(TrackPoint p) {
		if (topleft.latDec >= p.latDec && topleft.lonDec <= p.lonDec
				&& buttomright.latDec <= p.latDec
				&& buttomright.lonDec >= p.lonDec)
			return true;
		else
			return false;
	}

	public String toString() {
		return topleft.toString() + ", " + buttomright.toString();
	}
}
