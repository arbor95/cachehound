package CacheWolf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.cachehound.util.ComparatorHelper;
import de.cachehound.util.EweCollections;
import ewe.util.Comparer;
import ewe.util.Vector;

/**
 * @author torsti
 *
 */
public class CacheDB {

	/**
	 * Stores the CacheHolder objects
	 */
	private List<CacheHolder> vectorDB = new ArrayList<CacheHolder>();
	/**
	 * Stores the reference of waypoints to index positions (in vectorDB).
	 */
	private Map<String, Integer> hashDB = new HashMap<String, Integer>();

	/**
	 * Gets the stored CacheHolder object by its position in the Cache List.
	 * 
	 * @param index
	 *            Index of cache
	 * @return CacheHolder object with corresponding index
	 */
	public CacheHolder get(int index) {
		return vectorDB.get(index);
	}

	/**
	 * Gets the stored CacheHolder object by its waypoint. If no such Cache
	 * exists, null is returned.
	 * 
	 * @param waypoint
	 *            Waypoint of cache we want
	 * @return CacheHolder object with corresponding waypoint
	 */
	public CacheHolder get(String waypoint) {
		if (hashDB.containsKey(waypoint)) {
			return vectorDB.get(hashDB.get(waypoint));
		} else {
			return null;
		}
	}

	/**
	 * Gets the index of the cache with a given waypoint.
	 * 
	 * @param waypoint
	 *            Waypoint of cache we want
	 * @return Index of CacheHolder object in cache list.
	 */
	public int getIndex(String waypoint) {
		if (hashDB.containsKey(waypoint)) {
			return hashDB.get(waypoint);
		} else {
			return -1;
		}
	}

	/**
	 * Gets the index of a certain CacheHolder object.
	 * 
	 * @param ch
	 *            CacheHolder object
	 * @return Index of CacheHolder object in cache list.
	 */
	public int getIndex(CacheHolder ch) {
		return getIndex(ch.getWayPoint());
	}

	/**
	 * Sets a CacheHolder object at a certain position in the cache list. If
	 * this position is already occupied by a cache object, this one discarded.
	 * 
	 * @param index
	 *            Index where to set object
	 * @param ch
	 *            CacheHolder object to set
	 */
	public void set(int index, CacheHolder ch) {
		CacheHolder oldObj = vectorDB.get(index);
		vectorDB.set(index, ch);
		hashDB.put(ch.getWayPoint(), index);
		if (oldObj != null
				&& !oldObj.getWayPoint().equals(oldObj.getWayPoint())) {
			hashDB.remove(oldObj.getWayPoint());
		}
	}

	/**
	 * Append a CacheHolder object at the end of the cache list. If a cache with
	 * same waypoint is already existant in the cache list, then the old object
	 * is overwritten and the new object is positioned at the position of the
	 * old object (so in this case <code>add</code> acts like <code>set</code>.
	 * 
	 * @param ch
	 *            CacheHolder object to append
	 */
	public void add(CacheHolder ch) {
		if (this.getIndex(ch) > 0) {
			this.set(this.getIndex(ch), ch);
		} else {
			vectorDB.add(ch);
			hashDB.put(ch.getWayPoint(), vectorDB.size() - 1);
		}
	}

	/**
	 * The number of caches in the cache list.
	 * 
	 * @return number
	 */
	public int size() {
		return vectorDB.size();
	}

	/**
	 * Removes all cache objects from the list.
	 */
	public void clear() {
		hashDB.clear();
		vectorDB.clear();
	}

	/**
	 * Same as <br>
	 * <code>clear();<br>addAll(cachesA);<br>addAll(cachesB);<br>
	 * Thus builds cacheDB out of caches of vectors cachesA and cachesB, added
	 * in this order.
	 * 
	 * @param cachesA
	 *            First List of CacheHolder object to add to CacheDB
	 * @param cachesB
	 *            Second List of CacheHolder object to add to CacheDB
	 */
	public void rebuild(List<CacheHolder> cachesA, List<CacheHolder> cachesB) {
		clear();
		addAll(cachesA);
		addAll(cachesB);
	}

	/**
	 * Same as <br>
	 * <code>clear();<br>addAll(cachesA);<br>addAll(cachesB);<br></code>but
	 * optimized to reduce object creation. <br>
	 * Thus builds cacheDB out of caches of vectors cachesA and cachesB, added
	 * in this order.
	 * 
	 * ... und natuerlich, wie aller CW-Code, vollkommen unlesbar...
	 * 
	 * @param cachesA
	 *            First Vector of CacheHolder object to add to CacheDB
	 * @param cachesB
	 *            Second Vector of CacheHolder object to add to CacheDB
	 */
	@Deprecated
	public void rebuild(Vector cachesA, Vector cachesB) {
		rebuild(EweCollections.<CacheHolder> vectorToList(cachesA),
				EweCollections.<CacheHolder> vectorToList(cachesB));
		// int vectorSize = vectorDB.size();
		// int cachesAsize = 0;
		// int cachesBsize = 0;
		// // First negate all hashtable position values, to distinguish the old
		// // from the new values
		// Iterator iter = hashDB.entries();
		// while (iter.hasNext()) {
		// MutableInteger mInt = (MutableInteger) ((MapEntry) iter.next())
		// .getValue();
		// mInt.setInt(-mInt.getInt());
		// }
		// // Then set all vector elements at the proper position
		// for (int abc = 1; abc <= 2; abc++) {
		// Vector cachesAB = null;
		// int offset = 0;
		// if (abc == 1) {
		// cachesAB = cachesA;
		// if (cachesA != null)
		// cachesAsize = cachesA.size();
		// } else {
		// cachesAB = cachesB;
		// if (cachesA != null)
		// offset = cachesA.size();
		// if (cachesB != null)
		// cachesBsize = cachesB.size();
		// }
		// if (cachesAB == null)
		// continue;
		// for (int i = offset; i < cachesAB.size() + offset; i++) {
		// CacheHolder ch = (CacheHolder) cachesAB.get(i - offset);
		// if (i < vectorSize) {
		// vectorDB.set(i, ch);
		// } else {
		// vectorDB.add(ch);
		// }
		// hashDB.put(ch.getWayPoint(), this
		// .getIntObj(ch.getWayPoint(), i));
		// }
		// }
		// // If there are more elements in vectorDB than in the sum of sizes of
		// // cachesA and cachesB
		// // then the rest has to be deleted.
		// for (int i = vectorDB.size() - 1; i >= cachesAsize + cachesBsize;
		// i--) {
		// vectorDB.del(i);
		// }
		// // Now delete any element from hashDB which still has a negative
		// // position value
		// Vector wpToDelete = null;
		// MapEntry me = null;
		// iter = hashDB.entries();
		// while (iter.hasNext()) {
		// me = (MapEntry) iter.next();
		// MutableInteger mInt = (MutableInteger) me.getValue();
		// if (mInt.getInt() < 0) {
		// if (wpToDelete == null)
		// wpToDelete = new Vector();
		// String wp = (String) me.getKey();
		// wpToDelete.add(wp);
		// }
		// }
		// if (wpToDelete != null) {
		// for (int i = 0; i < wpToDelete.size(); i++) {
		// String wp = (String) wpToDelete.get(i);
		// hashDB.remove(wp);
		// }
		// }
	}

	/**
	 * Removes a CacheHolder object at the specified position in the cache list.
	 * The following elements are renumbered.<br>
	 * Additionally the cache details are unloaded and saved to file, if
	 * necessary.
	 * 
	 * @param index
	 *            The index of element to remove
	 */
	public void removeElementAt(int index) {
		CacheHolder ch = this.get(index);
		ch.releaseCacheDetails();
		vectorDB.remove(index);
		hashDB.remove(ch.getWayPoint());
		// When one element has been removed, we have to update the index
		// references in the hashtable, as the indexes of waypoints changed.
		rebuildIndices(index);
	}

	/**
	 * Sorts the caches in the list
	 * 
	 * @param comp
	 *            Comparator
	 * @param descending
	 *            descending or not
	 */
	public void sort(Comparator<CacheHolder> comp, boolean descending) {
		// FIXME: is this the right way round?
		if (descending) {
			Collections.sort(vectorDB, ComparatorHelper.invert(comp));
		} else {
			Collections.sort(vectorDB, comp);
		}
		rebuildIndices(0);
	}

	/**
	 * Sorts the caches in the list
	 * 
	 * @param comparer
	 *            Comparer object
	 * @param descending
	 *            descending or not
	 */
	@Deprecated
	public void sort(Comparer comparer, boolean descending) {
		sort(EweCollections.<CacheHolder> comparerToComparator(comparer),
				descending);
	}

	/**
	 * Adds the caches of one CacheDB to current one. Caches are appended at the
	 * end.
	 * 
	 * @param caches
	 *            CacheDB to append
	 */
	public void addAll(CacheDB caches) {
		addAll(caches.vectorDB);
	}

	/**
	 * Adds a List of CacheHolder objects to current database. Caches are
	 * appended at the end.
	 * 
	 * @param caches
	 *            Vector of caches to append
	 */
	public void addAll(List<CacheHolder> caches) {
		int oldSize = vectorDB.size();
		vectorDB.addAll(caches);
		rebuildIndices(oldSize);
	}

	/**
	 * Adds a Vector of CacheHolder objects to current database. Caches are
	 * appended at the end.
	 * 
	 * @param caches
	 *            Vector of caches to append
	 */
	@Deprecated
	public void addAll(Vector caches) {
		addAll(EweCollections.<CacheHolder> vectorToList(caches));
	}

	/**
	 * Returns the number of currently visible waypoints. <br>
	 * As this number is not only dependent from CacheHolder properties, but
	 * also from the state of the filter and so on, the determination of this
	 * number always requires a count through all waypoints. So use with
	 * caution.
	 * 
	 * @return Number of currently visible waypoints.
	 */
	public int countVisible() {
		int c = 0;
		for (int i = 0; i < vectorDB.size(); i++) {
			if (this.get(i).isVisible())
				c++;
		}
		return c;
	}

	private void rebuildIndices(int startAt) {
		for (int i = startAt; i < vectorDB.size(); i++) {
			CacheHolder ch2 = this.get(i);
			hashDB.put(ch2.getWayPoint(), i);
		}
	}
}
