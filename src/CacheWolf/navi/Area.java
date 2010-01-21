package CacheWolf.navi;

import CacheWolf.beans.CWPoint;

public class Area {
	CWPoint topleft;
	CWPoint buttomright;

	public Area(CWPoint tl, CWPoint br) {
		topleft = new CWPoint(tl);
		buttomright = new CWPoint(br);
	}

	public boolean isInBound(CWPoint p) {
		if (topleft.getLatDec() >= p.getLatDec() && topleft.getLonDec() <= p.getLonDec()
				&& buttomright.getLatDec() <= p.getLatDec()
				&& buttomright.getLonDec() >= p.getLonDec())
			return true;
		else
			return false;
	}

	public String toString() {
		return topleft.toString() + ", " + buttomright.toString();
	}
}
