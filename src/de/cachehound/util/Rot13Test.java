package de.cachehound.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class Rot13Test {
	@Test
	public void testSingleLowercaseChars() {
		assertEquals('n', Rot13.encodeRot13('a'));
		assertEquals('z', Rot13.encodeRot13('m'));
		assertEquals('a', Rot13.encodeRot13('n'));
		assertEquals('m', Rot13.encodeRot13('z'));
	}

	@Test
	public void testSingleUppercaseChars() {
		assertEquals('N', Rot13.encodeRot13('A'));
		assertEquals('Z', Rot13.encodeRot13('M'));
		assertEquals('A', Rot13.encodeRot13('N'));
		assertEquals('M', Rot13.encodeRot13('Z'));
	}

	@Test
	public void testSingleSpecialChars() {
		assertEquals(' ', Rot13.encodeRot13(' '));
		assertEquals('.', Rot13.encodeRot13('.'));
		assertEquals('[', Rot13.encodeRot13('['));
		assertEquals(']', Rot13.encodeRot13(']'));
	}

	@Test
	public void testBaum() {
		assertEquals("Onhz", Rot13.encodeRot13("Baum"));
	}

	@Test
	public void testMagnetisch() {
		assertEquals("zntargvfpu", Rot13.encodeRot13("magnetisch"));
	}

	@Test
	public void testPlainText() {
		// Alles was zwischen [] steht, wird NICHT dekodiert.
		assertEquals("nop[def]tuv", Rot13.encodeRot13("abc[def]ghi"));
	}
}
