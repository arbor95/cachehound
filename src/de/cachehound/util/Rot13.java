package de.cachehound.util;

public class Rot13 {
	public static char encodeRot13(char c) {
		if ((c >= 'a' && c <= 'm') || (c >= 'A' && c <= 'M')) {
			return (char) (c + 13);
		} else if ((c >= 'n' && c <= 'z') || (c >= 'N' && c <= 'Z')) {
			return (char) (c - 13);
		} else {
			return c;
		}
	}

	/**
	 * (De)codes the given text with rot13. Text in [] won't be (de)coded.
	 * 
	 * @param text
	 *            will be (de)coded in rot13
	 * @return rot13 of text
	 */
	public static String encodeRot13(String text) {
		StringBuilder ret = new StringBuilder(text.length());
		boolean convert = true;
		for (char c : text.toCharArray()) {
			if (convert) {
				ret.append(encodeRot13(c));
			} else {
				ret.append(c);
			}

			if (c == '[') {
				convert = false;
			} else if (c == ']') {
				convert = true;
			}
		}
		return ret.toString();
	}
}
