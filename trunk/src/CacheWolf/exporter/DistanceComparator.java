package CacheWolf.exporter;

import java.util.Comparator;

import CacheWolf.beans.CWPoint;
import CacheWolf.beans.CacheHolder;

public class DistanceComparator implements Comparator<CacheHolder> {
	private CWPoint centre;

	public DistanceComparator(CWPoint centre) {
		this.centre = centre;
	}

	public int compare(CacheHolder ch1, CacheHolder ch2) {
		return (int) ((ch1.pos.getDistance(centre) - ch2.pos
				.getDistance(centre)) * 1000);
	}
}
