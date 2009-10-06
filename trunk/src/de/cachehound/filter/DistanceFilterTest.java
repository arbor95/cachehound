package de.cachehound.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.junit.Before;
import org.junit.Test;

import de.cachehound.beans.CacheHolderDummy;
import de.cachehound.beans.ICacheHolder;

public class DistanceFilterTest {
	private static class CHWithDistance extends CacheHolderDummy {
		double distance;

		public CHWithDistance(double d) {
			this.distance = d;
		}

		@Override
		public double getKilom() {
			return distance;
		}
	}

	private IFilter filter;
	private ICacheHolder inside;
	private ICacheHolder outside;

	@Before
	public void setUp() {
		filter = new DistanceFilter(5);
		inside = new CHWithDistance(3);
		outside = new CHWithDistance(8);
	}

	@Test
	public void testInside() {
		assertTrue(filter.cacheIsVisible(inside));
	}

	@Test
	public void testOutside() {
		assertFalse(filter.cacheIsVisible(outside));
	}
	
	@Test
	public void testToXML() {
		Element expected = new Element("distance");
		expected.setAttribute("maxdistance", "5.0");
		
		assertEquals((new XMLOutputter()).outputString(expected),
				(new XMLOutputter()).outputString(filter.toXML()));
	}
}
