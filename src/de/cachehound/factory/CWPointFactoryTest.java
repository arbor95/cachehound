package de.cachehound.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import static de.cachehound.factory.CWPointFactory.NSHemisphere.N;
import static de.cachehound.factory.CWPointFactory.EWHemisphere.E;

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
		assertEquals(53.595683, p.getLatDec(), 0.000001);
		assertEquals(9.957217, p.getLonDec(), 0.000001);
		assertTrue(p.isValid());
	}

	@Test
	public void testFromHD() {
		CWPoint p = f.fromHD(N, 53.595683, E, 9.957217);
		assertEquals(53.595683, p.getLatDec(), 0.000001);
		assertEquals(9.957217, p.getLonDec(), 0.000001);
		assertTrue(p.isValid());
	}

	@Test
	public void testFromHDM() {
		CWPoint p = f.fromHDM(N, 53, 35.741, E, 9, 57.433);
		assertEquals(53.595683, p.getLatDec(), 0.000001);
		assertEquals(9.957217, p.getLonDec(), 0.000001);
		assertTrue(p.isValid());
	}

	@Test
	public void testFromHDMS() {
		CWPoint p = f.fromHDMS(N, 53, 35, 44.46, E, 9, 57, 25.98);
		assertEquals(53.595683, p.getLatDec(), 0.000001);
		assertEquals(9.957217, p.getLonDec(), 0.000001);
		assertTrue(p.isValid());
	}

	@Test
	public void testFromGermanGK() {
		CWPoint p = f.fromGermanGK(3563446, 5940902);
		assertEquals(53.595683, p.getLatDec(), 0.00001);
		assertEquals(9.957217, p.getLonDec(), 0.00001);
		assertTrue(p.isValid());
	}

	@Test
	public void testFromUTM() {
		CWPoint p = f.fromUTM("32U", 563351, 5938965);
		assertEquals(53.595683, p.getLatDec(), 0.00001);
		assertEquals(9.957217, p.getLonDec(), 0.00001);
		assertTrue(p.isValid());
	}
}
