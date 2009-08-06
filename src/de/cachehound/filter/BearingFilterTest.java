package de.cachehound.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import de.cachehound.beans.CacheHolderDummy;
import de.cachehound.beans.ICacheHolder;
import de.cachehound.types.Bearing;

public class BearingFilterTest {
	private static class CHWithBearing extends CacheHolderDummy {
		Bearing b;

		public CHWithBearing(Bearing b) {
			this.b = b;
		}

		@Override
		public Bearing getBearing() {
			return b;
		}
	}

	private Set<Bearing> mask;

	private ICacheHolder chNorth;
	private ICacheHolder chSouth;
	private ICacheHolder chEast;
	private ICacheHolder chWest;

	@Before
	public void setUp() {
		mask = EnumSet.noneOf(Bearing.class);

		chNorth = new CHWithBearing(Bearing.N);
		chSouth = new CHWithBearing(Bearing.S);
		chEast = new CHWithBearing(Bearing.E);
		chWest = new CHWithBearing(Bearing.W);
	}

	@Test
	public void testFilterNorth() {
		mask.add(Bearing.N);
		BearingFilter filter = new BearingFilter(mask);

		assertTrue(filter.cacheIsVisible(chNorth));
		assertFalse(filter.cacheIsVisible(chSouth));
	}

	@Test
	public void testFilterSouth() {
		mask.add(Bearing.S);
		BearingFilter filter = new BearingFilter(mask);

		assertFalse(filter.cacheIsVisible(chNorth));
		assertTrue(filter.cacheIsVisible(chSouth));
	}

	@Test
	public void testFilterWithMultipleDirections() {
		mask.add(Bearing.N);
		mask.add(Bearing.S);
		BearingFilter filter = new BearingFilter(mask);

		assertTrue(filter.cacheIsVisible(chNorth));
		assertTrue(filter.cacheIsVisible(chSouth));
		assertFalse(filter.cacheIsVisible(chEast));
		assertFalse(filter.cacheIsVisible(chWest));
	}
}
