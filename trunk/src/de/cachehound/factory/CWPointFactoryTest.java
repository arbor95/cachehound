package de.cachehound.factory;

import static de.cachehound.factory.CWPointFactory.EWHemisphere.E;
import static de.cachehound.factory.CWPointFactory.NSHemisphere.N;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import CacheWolf.beans.CWPoint;

public class CWPointFactoryTest {
	private CWPointFactory f = CWPointFactory.getInstance();

	@Test
	public void testCreateInvalid() {
		CWPoint p = f.createInvalid();
		assertFalse(p.isValid());
	}

	@Test
	public void testFromD() {
		CWPoint p = f.fromD(53.595683, 9.957217);
		assertTrue(p.isValid());
		assertEquals(53.595683, p.getLatDec(), 0.000001);
		assertEquals(9.957217, p.getLonDec(), 0.000001);
	}

	@Test
	public void testFromHD() {
		CWPoint p = f.fromHD(N, 53.595683, E, 9.957217);
		assertTrue(p.isValid());
		assertEquals(53.595683, p.getLatDec(), 0.000001);
		assertEquals(9.957217, p.getLonDec(), 0.000001);
	}

	@Test
	public void testFromHDM() {
		CWPoint p = f.fromHDM(N, 53, 35.741, E, 9, 57.433);
		assertTrue(p.isValid());
		assertEquals(53.595683, p.getLatDec(), 0.000001);
		assertEquals(9.957217, p.getLonDec(), 0.000001);
	}

	@Test
	public void testFromHDMS() {
		CWPoint p = f.fromHDMS(N, 53, 35, 44.46, E, 9, 57, 25.98);
		assertTrue(p.isValid());
		assertEquals(53.595683, p.getLatDec(), 0.000001);
		assertEquals(9.957217, p.getLonDec(), 0.000001);
	}

	@Test
	public void testFromGermanGK() {
		CWPoint p = f.fromGermanGK(3563446, 5940902);
		assertTrue(p.isValid());
		assertEquals(53.595683, p.getLatDec(), 0.00001);
		assertEquals(9.957217, p.getLonDec(), 0.00001);
	}

	@Test
	public void testFromUTM() {
		CWPoint p = f.fromUTM("32U", 563351, 5938965);
		assertTrue(p.isValid());
		assertEquals(53.595683, p.getLatDec(), 0.00001);
		assertEquals(9.957217, p.getLonDec(), 0.00001);
	}
}
