package de.cachehound.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;


public class AndFilterTest {
	private IFilter alwaysTrue = new TrivialFilter(true);
	private IFilter alwaysFalse = new TrivialFilter(false);
	
	private AndFilter andFilter;
	
	@Before
	public void setUp() {
		andFilter = new AndFilter();
	}
	
	@Test
	public void testEmptyAndFilterSucceeds() {
		assertTrue(andFilter.cacheIsVisible(null));
	}
	
	@Test
	public void testOneElementTrue() {
		andFilter.add(alwaysTrue);
		assertTrue(andFilter.cacheIsVisible(null));
	}
	
	@Test
	public void testOneElementFalse() {
		andFilter.add(alwaysFalse);
		assertFalse(andFilter.cacheIsVisible(null));
	}
	
	@Test
	public void testTwoElementsTrueFalse() {
		andFilter.add(alwaysTrue);
		andFilter.add(alwaysFalse);
		assertFalse(andFilter.cacheIsVisible(null));
	}
	
	@Test
	public void testTwoElementsFalseTrue() {
		andFilter.add(alwaysFalse);
		andFilter.add(alwaysTrue);
		assertFalse(andFilter.cacheIsVisible(null));
	}
	
	@Test
	public void testTwoElementsTrueTrue() {
		andFilter.add(alwaysTrue);
		andFilter.add(alwaysTrue);
		assertTrue(andFilter.cacheIsVisible(null));
	}
	
	@Test
	public void testTenElements() {
		andFilter.add(alwaysTrue);
		andFilter.add(alwaysTrue);
		andFilter.add(alwaysTrue);
		andFilter.add(alwaysTrue);
		andFilter.add(alwaysTrue);
		andFilter.add(alwaysTrue);
		andFilter.add(alwaysTrue);
		andFilter.add(alwaysTrue);
		andFilter.add(alwaysTrue);
		andFilter.add(alwaysFalse);
		assertFalse(andFilter.cacheIsVisible(null));
	}
}
