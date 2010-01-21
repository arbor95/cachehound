package de.cachehound.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import CacheWolf.beans.CWPoint;

public class CWPointFactoryTestRegexp {
	private CWPointFactory f = CWPointFactory.getInstance();
	
	private void testHD(String in, double lat, double lon) {
		CWPoint p = f.fromHDString(in);
		assertTrue(p.isValid());
		assertEquals(lat, p.getLatDec(), 0.000001);
		assertEquals(lon, p.getLonDec(), 0.000001);
		
		p = f.fromString(in);
		assertTrue(p.isValid());
		assertEquals(lat, p.getLatDec(), 0.000001);
		assertEquals(lon, p.getLonDec(), 0.000001);
	}

	@Test
	public void testHD() {
		testHD("N 53.595683° E 9.957217°", 53.595683, 9.957217);
	}
	
	private void testHDM(String in, double lat, double lon) {
		CWPoint p = f.fromHDMString(in);
		assertTrue(p.isValid());
		assertEquals(lat, p.getLatDec(), 0.000001);
		assertEquals(lon, p.getLonDec(), 0.000001);
		
		p = f.fromString(in);
		assertTrue(p.isValid());
		assertEquals(lat, p.getLatDec(), 0.000001);
		assertEquals(lon, p.getLonDec(), 0.000001);
	}
	
	@Test
	public void testHDM() {
		testHDM("N 53° 35.741 E 009° 57.433", 53.595683, 9.957217);
		// Format used by Groundspeak
		
		testHDM(" N 53 ° 35.741 ' / E 009 ° 57.433 ' ", 53.595683, 9.957217);
		// With all signs and spaces
		
		testHDM("N53°35.741'/E9°57.433'", 53.595683, 9.957217);
		// With signs, but shorter

		testHDM("N53 35.741 E9 57.433", 53.595683, 9.957217);
		// Without signs

		testHDM("N53 30 E9 30", 53.5, 9.5);
		// Missing fractions

		testHDM("N 53° 35,741 E 009° 57,433", 53.595683, 9.957217);
		// Komma instead of Dot as a decimal separator
	}
	
	private void testHDMS(String in, double lat, double lon) {
		CWPoint p = f.fromHDMSString(in);
		assertTrue(p.isValid());
		assertEquals(lat, p.getLatDec(), 0.000001);
		assertEquals(lon, p.getLonDec(), 0.000001);
		
		p = f.fromString(in);
		assertTrue(p.isValid());
		assertEquals(lat, p.getLatDec(), 0.000001);
		assertEquals(lon, p.getLonDec(), 0.000001);
	}
	
	@Test
	public void testHDMS() {
		testHDMS("N 53° 35' 44.46\" E 9° 57' 25.98\"", 53.595683, 9.957217);

		testHDMS("N53°35'44.46\"/E9°57'25.98\"", 53.595683, 9.957217);

		testHDMS("N 53° 35' 44.46'' E 9° 57' 25.98’’", 53.595683, 9.957217);
	}
}
