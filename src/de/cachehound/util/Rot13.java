package de.cachehound.util;

public class Rot13 {

	/**
	 * (De)codes the given text with rot13. Text in [] won't be (de)coded.
	 * 
	 * @param text
	 *            will be (de)coded in rot13
	 * @return rot13 of text
	 */
	public static String encodeRot13(String text) {
		char[] dummy = new char[text.length()];
		boolean convert = true;
		char c;
		for (int i = 0; i < text.length(); i++) {
			c = text.charAt(i);

			if (convert && ((c >= 'a' && c <= 'm') || (c >= 'A' && c <= 'M'))) {
				dummy[i] = (char) (c + 13);
			} else if (convert
					&& ((c >= 'n' && c <= 'z') || (c >= 'N' && c <= 'Z'))) {
				dummy[i] = (char) (c - 13);
			} else if (c == '[') {
				convert = false;
				dummy[i] = '[';
			} else if (c == ']') {
				convert = true;
				dummy[i] = ']';
			} else {
				dummy[i] = c;
			}
		}// for
		return new String(dummy);
	}

}
