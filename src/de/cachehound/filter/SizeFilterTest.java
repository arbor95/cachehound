package de.cachehound.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import de.cachehound.beans.CacheHolderDummy;
import de.cachehound.beans.ICacheHolder;
import de.cachehound.types.CacheSize;


public class SizeFilterTest {
	private static class CHWithSize extends CacheHolderDummy {
		CacheSize size;
		
		public CHWithSize(CacheSize size) {
			this.size = size;
		}

		@Override
		public CacheSize getCacheSize() {
			return size;
		}
	}
	
	private Set<CacheSize> mask;
	
	private ICacheHolder chMicro;
	private ICacheHolder chSmall;
	private ICacheHolder chRegular;
	private ICacheHolder chLarge;
	
	@Before
	public void setUp() {
		mask = EnumSet.noneOf(CacheSize.class);
		
		chMicro = new CHWithSize(CacheSize.MICRO);
		chSmall= new CHWithSize(CacheSize.SMALL);
		chRegular = new CHWithSize(CacheSize.REGULAR);
		chLarge= new CHWithSize(CacheSize.LARGE);
	}
	
	@Test
	public void testFilterMicro() {
		mask.add(CacheSize.MICRO);
		SizeFilter filter = new SizeFilter(mask);
		
		assertTrue(filter.cacheIsVisible(chMicro));
		assertFalse(filter.cacheIsVisible(chSmall));
	}
	
	@Test
	public void testFilterSouth() {
		mask.add(CacheSize.SMALL);
		SizeFilter filter = new SizeFilter(mask);
		
		assertFalse(filter.cacheIsVisible(chMicro));
		assertTrue(filter.cacheIsVisible(chSmall));
	}
	
	@Test
	public void testFilterWithMultipleDirections() {
		mask.add(CacheSize.MICRO);
		mask.add(CacheSize.SMALL);
		SizeFilter filter = new SizeFilter(mask);
		
		assertTrue(filter.cacheIsVisible(chMicro));
		assertTrue(filter.cacheIsVisible(chSmall));
		assertFalse(filter.cacheIsVisible(chRegular));
		assertFalse(filter.cacheIsVisible(chLarge));
	}
}
