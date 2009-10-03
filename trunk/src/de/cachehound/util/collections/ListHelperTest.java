package de.cachehound.util.collections;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

public class ListHelperTest {
	@Test
	public void testDivideAndSwap() {
		assertEquals(Arrays.asList(3, 4, 1, 2), ListHelper.divideAndSwap(Arrays
				.asList(1, 2, 3, 4), 3));
	}
}
