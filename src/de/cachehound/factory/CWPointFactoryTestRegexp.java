package de.cachehound.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import CacheWolf.beans.CWPoint;

public class CWPointFactoryTestRegexp {
	private CWPointFactory f = CWPointFactory.getInstance();
	
	@Test
	public void testHDMGroundspeak() {
		CWPoint p = f.fromHDMString("N 53° 35.741 E 009° 57.433");
		assertTrue(p.isValid());
		assertEquals(53.595683, p.getLatDec(), 0.000001);
		assertEquals(9.957217, p.getLonDec(), 0.000001);
	}
	
	@Test
	public void testHDMSpacesSlashMinutesign() {
		CWPoint p = f.fromHDMString(" N 53 ° 35.741 ' / E 009 ° 57.433 ' ");
		assertTrue(p.isValid());
		assertEquals(53.595683, p.getLatDec(), 0.000001);
		assertEquals(9.957217, p.getLonDec(), 0.000001);
	}
	
	@Test
	public void testHDMCompactWithSigns() {
		CWPoint p = f.fromHDMString("N53°35.741'/E9°57.433'");
		assertTrue(p.isValid());
		assertEquals(53.595683, p.getLatDec(), 0.000001);
		assertEquals(9.957217, p.getLonDec(), 0.000001);
	}
	
	@Test
	public void testHDMCompactWithoutSigns() {
		CWPoint p = f.fromHDMString("N53 35.741 E9 57.433");
		assertTrue(p.isValid());
		assertEquals(53.595683, p.getLatDec(), 0.000001);
		assertEquals(9.957217, p.getLonDec(), 0.000001);
	}
	
	@Test
	public void testHDMWithoutFraction() {
		CWPoint p = f.fromHDMString("N53 30 E9 30");
		assertTrue(p.isValid());
		assertEquals(53.5, p.getLatDec(), 0.000001);
		assertEquals(9.5, p.getLonDec(), 0.000001);
	}
	
	@Test
	public void testHDMKomma() {
		CWPoint p = f.fromHDMString("N 53° 35,741 E 009° 57,433");
		assertTrue(p.isValid());
		assertEquals(53.595683, p.getLatDec(), 0.000001);
		assertEquals(9.957217, p.getLonDec(), 0.000001);
	}
}
