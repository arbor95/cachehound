package de.cachehound.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.junit.Test;

public class AndFilterTest {
	private IFilter alwaysTrue = new TrivialFilter(true);
	private IFilter alwaysFalse = new TrivialFilter(false);

	private AndFilter andFilter;

	@Test
	public void testEmptyAndFilterSucceeds() {
		andFilter = new AndFilter();
		assertTrue(andFilter.cacheIsVisible(null));
	}

	@Test
	public void testOneElementTrue() {
		andFilter = new AndFilter(alwaysTrue);
		assertTrue(andFilter.cacheIsVisible(null));
	}

	@Test
	public void testOneElementFalse() {
		andFilter = new AndFilter(alwaysFalse);
		assertFalse(andFilter.cacheIsVisible(null));
	}

	@Test
	public void testTwoElementsTrueFalse() {
		andFilter = new AndFilter(alwaysTrue, alwaysFalse);
		assertFalse(andFilter.cacheIsVisible(null));
	}

	@Test
	public void testTwoElementsFalseTrue() {
		andFilter = new AndFilter(alwaysFalse, alwaysTrue);
		assertFalse(andFilter.cacheIsVisible(null));
	}

	@Test
	public void testTwoElementsTrueTrue() {
		andFilter = new AndFilter(alwaysTrue, alwaysTrue);
		assertTrue(andFilter.cacheIsVisible(null));
	}

	@Test
	public void testTenElements() {
		andFilter = new AndFilter(alwaysTrue, alwaysTrue, alwaysTrue,
				alwaysTrue, alwaysTrue, alwaysTrue, alwaysTrue, alwaysTrue,
				alwaysTrue, alwaysFalse);
		assertFalse(andFilter.cacheIsVisible(null));
	}

	@Test
	public void testToXML() {
		andFilter = new AndFilter(alwaysTrue, alwaysFalse);

		Element expected = new Element("and");
		expected.addContent(alwaysTrue.toXML());
		expected.addContent(alwaysFalse.toXML());

		assertEquals((new XMLOutputter()).outputString(expected),
				(new XMLOutputter()).outputString(andFilter.toXML()));
	}
}
