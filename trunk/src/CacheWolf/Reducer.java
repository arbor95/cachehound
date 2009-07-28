package CacheWolf;

import ewe.util.mString;

/**
 * Class to reduce a string to its main components, so that a human may still
 * understand what it means. This is usefull for displaying cache names in a
 * GPSR or for POIs in a navigation software.
 * 
 * The idea is: 1) Throw out all 3 letter words, 2) Remove all vowels from a
 * word, except if it is the first letter of a word 3) Remove all Whitespace 4)
 * If requested truncate the string to a given number of characters.
 */
public class Reducer {

	public static String convert(String origStr, boolean trun, int len) {
		String[] parts = mString.split(origStr, ' ');
		String dummy = new String();
		String finalStr = new String();
		for (int i = 0; i < parts.length; i++) {
			if (parts[i].length() != 3) {
				dummy = removeVow(parts[i]);
				finalStr = finalStr + dummy;
			}
		}// for
		// Vm.debug(Convert.toString(trun));
		if (trun == true) {
			finalStr = finalStr + "                                         ";
			finalStr = finalStr.substring(0, len);
			// Vm.debug(finalStr);
		}
		return finalStr;
	}

	private static String removeVow(String str) {
		String dummy = str.substring(1);
		dummy = dummy.replace("a", "");
		dummy = dummy.replace("e", "");
		dummy = dummy.replace("i", "");
		dummy = dummy.replace("o", "");
		dummy = dummy.replace("u", "");
		dummy = dummy.replace("A", "");
		dummy = dummy.replace("E", "");
		dummy = dummy.replace("I", "");
		dummy = dummy.replace("O", "");
		dummy = dummy.replace("U", "");
		dummy = dummy.replace(",", "");
		dummy = str.substring(0, 1) + dummy;
		return dummy;
	}
}
