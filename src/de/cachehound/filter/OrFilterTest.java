package de.cachehound.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class OrFilterTest {
	private IFilter alwaysTrue = new TrivialFilter(true);
	private IFilter alwaysFalse = new TrivialFilter(false);

	private OrFilter orFilter;

	@Test
	public void testEmptyOrFilterFails() {
		orFilter = new OrFilter();
		assertFalse(orFilter.cacheIsVisible(null));
	}

	@Test
	public void testOneElementTrue() {
		orFilter = new OrFilter(alwaysTrue);
		assertTrue(orFilter.cacheIsVisible(null));
	}

	@Test
	public void testOneElementFalse() {
		orFilter = new OrFilter(alwaysFalse);
		assertFalse(orFilter.cacheIsVisible(null));
	}

	@Test
	public void testTwoElementsFalseTrue() {
		orFilter = new OrFilter(alwaysFalse, alwaysTrue);
		assertTrue(orFilter.cacheIsVisible(null));
	}

	@Test
	public void testTwoElementsTrueFalse() {
		orFilter = new OrFilter(alwaysTrue, alwaysFalse);
		assertTrue(orFilter.cacheIsVisible(null));
	}

	@Test
	public void testTwoElementsFalseFalse() {
		orFilter = new OrFilter(alwaysFalse, alwaysFalse);
		assertFalse(orFilter.cacheIsVisible(null));
	}

	@Test
	public void testTenElements() {
		orFilter = new OrFilter(alwaysFalse, alwaysFalse, alwaysFalse,
				alwaysFalse, alwaysFalse, alwaysFalse, alwaysFalse,
				alwaysFalse, alwaysFalse, alwaysTrue);
		assertTrue(orFilter.cacheIsVisible(null));
	}
}
