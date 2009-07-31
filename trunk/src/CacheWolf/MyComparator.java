package CacheWolf;

import java.util.Comparator;

/**
 * This class handles the sorting for most of the sorting tasks. If a cache is
 * to be displayed in the table or not is handled in the table model
 * 
 * @see MyTableModel
 * @see DistComparer
 */
public class MyComparator implements Comparator<CacheHolder> {
	public MyComparator(CacheDB cacheDB, int colToCompare, int visibleSize) {
		// visibleSize=Global.mainTab.tbP.myMod.numRows;
		if (visibleSize < 2)
			return;
		for (int i = visibleSize; i < cacheDB.size(); i++) {
			CacheHolder ch = cacheDB.get(i);
			ch.sort = "\uFFFF";
		}
		if (colToCompare == 1) {
			for (int i = 0; i < visibleSize; i++) {
				CacheHolder ch = cacheDB.get(i);
				ch.sort = String.valueOf(ch.getType());
			}
		} else if (colToCompare == 2) {
			for (int i = 0; i < visibleSize; i++) {
				CacheHolder ch = cacheDB.get(i);
				ch.sort = String.valueOf(ch.getHard());
			}
		} else if (colToCompare == 3) {
			for (int i = 0; i < visibleSize; i++) {
				CacheHolder ch = cacheDB.get(i);
				ch.sort = String.valueOf(ch.getTerrain());
			}
		} else if (colToCompare == 4) {
			for (int i = 0; i < visibleSize; i++) {
				CacheHolder ch = cacheDB.get(i);
				// We sort...
				// * first by the two-letter-prefix
				// (so that GC caches and OC caches aren't mixed)
				// * then by the _length_ of the id
				// (so that GCFFFF sorts before GC10000)
				// * then the rest of the id
				ch.sort = (ch.getWayPoint().substring(0, 2)
						+ ch.getWayPoint().length() + ch.getWayPoint()
						.substring(2)).toUpperCase();
			}
		} else if (colToCompare == 5) {
			for (int i = 0; i < visibleSize; i++) {
				CacheHolder ch = cacheDB.get(i);
				// Ignore some funny characters - users expect e.g. "Finde mich"
				// to sort under F and not under "
				ch.sort = ch.getCacheName().toLowerCase().replaceAll(
						"[\\p{Punct}]", "").replaceAll("^\\s*", "");
			}
		} else if (colToCompare == 6) {
			for (int i = 0; i < visibleSize; i++) {
				CacheHolder ch = cacheDB.get(i);
				ch.sort = ch.LatLon;
			}
		} else if (colToCompare == 7) {
			for (int i = 0; i < visibleSize; i++) {
				CacheHolder ch = cacheDB.get(i);
				// Ignore some funny characters - users expect e.g. "Finde mich"
				// to sort under F and not under "
				ch.sort = ch.getCacheOwner().toLowerCase().replaceAll(
						"[\\p{Punct}]", "").replaceAll("^\\s*", "");
			}
		} else if (colToCompare == 8) {
			for (int i = 0; i < visibleSize; i++) {
				CacheHolder ch = cacheDB.get(i);
				ch.sort = ch.getDateHidden();
			}
		} else if (colToCompare == 9) {
			for (int i = 0; i < visibleSize; i++) {
				CacheHolder ch = cacheDB.get(i);
				ch.sort = ch.getCacheStatus();
			}
		} else if (colToCompare == 10) {
			for (int i = 0; i < visibleSize; i++) {
				CacheHolder ch = cacheDB.get(i);
				// CHECK Is the formatting correctly done?
				ch.sort = MyLocale
						.formatDouble(ch.kilom * 1000, "000000000000");
			}
		} else if (colToCompare == 11) {
			for (int i = 0; i < visibleSize; i++) {
				CacheHolder ch = cacheDB.get(i);
				ch.sort = ch.bearing;
			}

		} else if (colToCompare == 12) {
			for (int i = 0; i < visibleSize; i++) {
				CacheHolder ch = cacheDB.get(i);
				ch.sort = Byte.toString(ch.getCacheSize());
			}
		} else if (colToCompare == 13) {
			for (int i = 0; i < visibleSize; i++) {
				CacheHolder ch = cacheDB.get(i);
				ch.sort = MyLocale.formatLong(ch.getNumRecommended(), "00000");
			}
		} else if (colToCompare == 14) {
			for (int i = 0; i < visibleSize; i++) {
				CacheHolder ch = cacheDB.get(i);
				if (ch.getWayPoint().startsWith("OC"))
					ch.sort = MyLocale.formatLong(ch.recommendationScore,
							"00000");
				else
					ch.sort = "\uFFFF";
			}
		} else if (colToCompare == 15) {
			for (int i = 0; i < visibleSize; i++) {
				CacheHolder ch = cacheDB.get(i);
				if (ch.hasSolver()) {
					ch.sort = "1";
				} else {
					ch.sort = "2";
				}
			}
		} else if (colToCompare == 16) {
			for (int i = 0; i < visibleSize; i++) {
				CacheHolder ch = cacheDB.get(i);
				if (ch.hasNote()) {
					ch.sort = "1";
				} else {
					ch.sort = "2";
				}
			}
		} else if (colToCompare == 17) {
			for (int i = 0; i < visibleSize; i++) {
				CacheHolder ch = cacheDB.get(i);
				ch.sort = MyLocale.formatLong(ch.addiWpts.size(), "000");
			}
		} else if (colToCompare == 18) {
			for (int i = 0; i < visibleSize; i++) {
				CacheHolder ch = cacheDB.get(i);
				ch.sort = MyLocale.formatLong(ch.getNoFindLogs(), "000");
			}
		} else if (colToCompare == 19) {
			for (int i = 0; i < visibleSize; i++) {
				CacheHolder ch = cacheDB.get(i);
				ch.sort = ch.getLastSync();
			}
		}
	}

	public int compare(CacheHolder ch1, CacheHolder ch2) {
		return ch1.sort.compareTo(ch2.sort);
	}
}
