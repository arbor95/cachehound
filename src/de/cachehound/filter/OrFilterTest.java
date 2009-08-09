package de.cachehound.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;


public class OrFilterTest {
	private IFilter alwaysTrue = new TrivialFilter(true);
	private IFilter alwaysFalse = new TrivialFilter(false);

	private OrFilter orFilter;

	@Before
	public void setUp() {
		orFilter = new OrFilter();
	}

	@Test
	public void testEmptyOrFilterFails() {
		assertFalse(orFilter.cacheIsVisible(null));
	}

	@Test
	public void testOneElementTrue() {
		orFilter.add(alwaysTrue);
		assertTrue(orFilter.cacheIsVisible(null));
	}

	@Test
	public void testOneElementFalse() {
		orFilter.add(alwaysFalse);
		assertFalse(orFilter.cacheIsVisible(null));
	}

	@Test
	public void testTwoElementsFalseTrue() {
		orFilter.add(alwaysFalse);
		orFilter.add(alwaysTrue);
		assertTrue(orFilter.cacheIsVisible(null));
	}

	@Test
	public void testTwoElementsTrueFalse() {
		orFilter.add(alwaysTrue);
		orFilter.add(alwaysFalse);
		assertTrue(orFilter.cacheIsVisible(null));
	}

	@Test
	public void testTwoElementsFalseFalse() {
		orFilter.add(alwaysFalse);
		orFilter.add(alwaysFalse);
		assertFalse(orFilter.cacheIsVisible(null));
	}

	@Test
	public void testTenElements() {
		orFilter.add(alwaysFalse);
		orFilter.add(alwaysFalse);
		orFilter.add(alwaysFalse);
		orFilter.add(alwaysFalse);
		orFilter.add(alwaysFalse);
		orFilter.add(alwaysFalse);
		orFilter.add(alwaysFalse);
		orFilter.add(alwaysFalse);
		orFilter.add(alwaysFalse);
		orFilter.add(alwaysTrue);
		assertTrue(orFilter.cacheIsVisible(null));
	}
}
