package de.cachehound.comparators;

import java.util.Comparator;

import CacheWolf.beans.CacheHolder;

/**
 * This class creates the different kinds of sorters used by the table model.
 * 
 * @see MyTableModel
 * @see DistComparer
 */
public class CacheHolderComparatorFactory {
	public static Comparator<CacheHolder> getComparator(int colToCompare) {
		switch (colToCompare) {
		case 1:
			return new TypeComparator();
		case 2:
			return new DifficultyComparator();
		case 3:
			return new TerrainComparator();
		case 4:
			return new WaypointComparator();
		case 5:
			return new NameComparator();
		case 6:
			return new LatLonComparator();
		case 7:
			return new OwnerComparator();
		case 8:
			return new HiddenComparator();
		case 9:
			return new StatusComparator();
		case 10:
			return new DistanceFromCenterComparator();
		case 11:
			return new BearingComparator();
		case 12:
			return new SizeComparator();
		case 13:
			return new NumberRecommendedComparator();
		case 14:
			return new RecommendationScoreComparator();
		case 15:
			return new HasSolverComparator();
		case 16:
			return new HasNoteComparator();
		case 17:
			return new NumberOfAdditionalWaypointsComparator();
		case 18:
			return new NumberOfDNFsComparator();
		case 19:
			return new LastSyncComparator();
		default:
			throw new IllegalArgumentException();
		}
	}
}
