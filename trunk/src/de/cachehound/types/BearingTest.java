package de.cachehound.types;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class BearingTest {
	@Test
	public void testZero() {
		assertEquals(Bearing.N, Bearing.fromDeg(0));
	}

	@Test
	public void testOneEighty() {
		assertEquals(Bearing.S, Bearing.fromDeg(180));
	}

	@Test
	public void testAlmostThreeSixty() {
		assertEquals(Bearing.N, Bearing.fromDeg(359.9));
	}
}
