package CacheWolf.util;

/**
 *	A class to replace unsafe XML characters with characters that a user
 *	"can read", and vice versa
 * 20061222: skg Modified cleanback to speed up the new index.xml reader
 */

import ewe.util.Hashtable;

public class SafeXML {
	private static final char ENTITY_START = '&';
	private static final char ENTITY_END = ';';

	private static Hashtable iso2htmlMappings = new Hashtable(300);
	static {
		String[] mappingArray = new String[] {
				"&apos;",
				"'", // Added 20061227 - not a valid HTML entity but
				// sometimes
				// used
				"&quot;", "\"", "&amp;", "&", "&lt;", "<", "&gt;", ">",
				"&nbsp;", " ", "&iexcl;", "ˇ", "&cent;", "˘", "&pound;", "Ł",
				"&curren;", "¤", "&yen;", "Ą", "&brvbar;", "¦", "&sect;", "§",
				"&uml;", "¨", "&copy;", "©", "&ordf;", "Ş", "&laquo;", "«",
				"&not;", "¬", "&shy;", "­", "&reg;", "®", "&macr;", "Ż",
				"&deg;", "°", "&plusmn;", "±", "&sup2;", "˛", "&sup3;", "ł",
				"&acute;", "´", "&micro;", "µ", "&para;", "¶", "&middot;", "·",
				"&cedil;", "¸", "&sup1;", "ą", "&ordm;", "ş", "&raquo;", "»",
				"&frac14;", "Ľ", "&frac12;", "˝", "&frac34;", "ľ", "&iquest;",
				"ż", "&Agrave;", "Ŕ", "&Aacute;", "Á", "&Acirc;", "Â",
				"&Atilde;", "Ă", "&Auml;", "Ä", "&Aring;", "Ĺ", "&AElig;", "Ć",
				"&Ccedil;", "Ç", "&Egrave;", "Č", "&Eacute;", "É", "&Ecirc;",
				"Ę", "&Euml;", "Ë", "&Igrave;", "Ě", "&Iacute;", "Í",
				"&Icirc;", "Î", "&Iuml;", "Ď", "&ETH;", "Đ", "&Ntilde;", "Ń",
				"&Ograve;", "Ň", "&Oacute;", "Ó", "&Ocirc;", "Ô", "&Otilde;",
				"Ő", "&Ouml;", "Ö", "&times;", "×", "&Oslash;", "Ř",
				"&Ugrave;", "Ů", "&Uacute;", "Ú", "&Ucirc;", "Ű", "&Uuml;",
				"Ü", "&Yacute;", "Ý", "&THORN;", "Ţ", "&szlig;", "ß",
				"&agrave;", "ŕ", "&aacute;", "á", "&acirc;", "â", "&atilde;",
				"ă", "&auml;", "ä", "&aring;", "ĺ", "&aelig;", "ć", "&ccedil;",
				"ç", "&egrave;", "č", "&eacute;", "é", "&ecirc;", "ę",
				"&euml;", "ë", "&igrave;", "ě", "&iacute;", "í", "&icirc;",
				"î", "&iuml;", "ď", "&eth;", "đ", "&ntilde;", "ń", "&ograve;",
				"ň", "&oacute;", "ó", "&ocirc;", "ô", "&otilde;", "ő",
				"&ouml;", "ö", "&divide;", "÷", "&oslash;", "ř", "&ugrave;",
				"ů", "&uacute;", "ú", "&ucirc;", "ű", "&uuml;", "ü",
				"&yacute;", "ý", "&thorn;", "ţ", "&yuml;", "˙",

				"&#34;", "\"", "&#38;", "&", "&#60;", "<", "&#62;", ">",
				"&#160;", " ", "&#161;", "ˇ", "&#162;", "˘", "&#163;", "Ł",
				"&#164;", "¤", "&#165;", "Ą", "&#166;", "¦", "&#167;", "§",
				"&#168;", "¨", "&#169;", "©", "&#170;", "Ş", "&#171;", "«",
				"&#172;", "¬", "&#173;", "­", "&#174;", "®", "&#175;", "Ż",
				"&#176;", "°", "&#177;", "±", "&#178;", "˛", "&#179;", "ł",
				"&#180;", "´", "&#181;", "µ", "&#182;", "¶", "&#183;", "·",
				"&#184;", "¸", "&#185;", "ą", "&#186;", "ş", "&#187;", "»",
				"&#188;", "Ľ", "&#189;", "˝", "&#190;", "ľ", "&#191;", "ż",
				"&#192;", "Ŕ", "&#193;", "Á", "&#194;", "Â", "&#195;", "Ă",
				"&#196;", "Ä", "&#197;", "Ĺ", "&#198;", "Ć", "&#199;", "Ç",
				"&#200;", "Č", "&#201;", "É", "&#202;", "Ę", "&#203;", "Ë",
				"&#204;", "Ě", "&#205;", "Í", "&#206;", "Î", "&#207;", "Ď",
				"&#208;", "Đ", "&#209;", "Ń", "&#210;", "Ň", "&#211;", "Ó",
				"&#212;", "Ô", "&#213;", "Ő", "&#214;", "Ö", "&#215;", "×",
				"&#216;", "Ř", "&#217;", "Ů", "&#218;", "Ú", "&#219;", "Ű",
				"&#220;", "Ü", "&#221;", "Ý", "&#222;", "Ţ", "&#223;", "ß",
				"&#224;", "ŕ", "&#225;", "á", "&#226;", "â", "&#227;", "ă",
				"&#228;", "ä", "&#229;", "ĺ", "&#230;", "ć", "&#231;", "ç",
				"&#232;", "č", "&#233;", "é", "&#234;", "ę", "&#235;", "ë",
				"&#236;", "ě", "&#237;", "í", "&#238;", "î", "&#239;", "ď",
				"&#240;", "đ", "&#241;", "ń", "&#242;", "ň", "&#243;", "ó",
				"&#244;", "ô", "&#245;", "ő", "&#246;", "ö", "&#247;", "÷",
				"&#248;", "ř", "&#249;", "ů", "&#250;", "ú", "&#251;", "ű",
				"&#252;", "ü", "&#253;", "ý", "&#254;", "ţ", "&#255;", "˙",
				"&#8208;", "-", "&#8209;", "-", "&#8210;", "-", "&#8211;", "-",
				"&#8212;", "-", "&#8213;", "-", "&#8216;", "'", "&#8217;", "'",
				"&#8218;", "'", "&#8219;", "'", "&#8220;", "\"", "&#8221;",
				"\"", "&#8222;", "\"", "&#8223;", "\"", "&#8226;", "•",
				"&#8242;", "'", "&#8243;", "\"" };
		for (int i = 0; i < mappingArray.length; i = i + 2) {
			iso2htmlMappings.put(mappingArray[i], mappingArray[i + 1]);
		}
	}

	/**
	 * Converts a <code>String</code> containing HTML entities to a
	 * <code>String</code> containing only ISO8859-1 characters.
	 * 
	 * Uses <a href="http://www.ramsch.org/martin/uni/fmi-hp/iso8859-1.html">ISO
	 * 8859-1 table by Martin Ramsch</a>.
	 * 
	 * @author <a href="mailto:ey@inweb.de">Christian Ey</a>
	 * 
	 * @version 1.0
	 * @param htmlString
	 *            The <code>String</code> containing HTML entities
	 * @return A <code>String</code> containing only ISO8859-1 characters
	 */
	public static String cleanback(String htmlString) {
		int indexStart;
		// return immediately if string is null or does not contain &
		if (htmlString != null
				&& (indexStart = htmlString.indexOf(ENTITY_START)) >= 0) {
			// copy everything from the beginning to entity start into buffer
			StringBuilder isoBuffer = new StringBuilder(htmlString.substring(0,
					indexStart));
			while (indexStart >= 0) {
				int indexEnd = htmlString.indexOf(ENTITY_END, indexStart + 1);
				if (indexEnd >= 0) {
					int alternativeStart = htmlString.indexOf(ENTITY_START,
							indexStart + 1);
					if ((alternativeStart > indexStart)
							&& (alternativeStart < indexEnd)) {
						// a second index start is found inbetween current index
						// start
						// and index end

						// flush the html string inbetween
						isoBuffer.append(htmlString.substring(indexStart,
								alternativeStart));

						// use the second index start and loop again
						indexStart = alternativeStart;
					} else {
						String entity = htmlString.substring(indexStart,
								indexEnd + 1);
						String isoCharacter = (String) iso2htmlMappings
								.get(entity);
						if (isoCharacter != null) {
							// insert iso character instead of html entity
							isoBuffer.append(isoCharacter);
						} else {
							// illegal entity detected, ignore gracefully
							isoBuffer.append(entity);
						}
						indexStart = htmlString.indexOf(ENTITY_START,
								indexEnd + 1);
						if (indexStart >= 0) {
							// another entity start detected, flush the html
							// string inbetween
							isoBuffer.append(htmlString.substring(indexEnd + 1,
									indexStart));
						} else {
							// no further entity start detected, flush rest of
							// html string
							isoBuffer
									.append(htmlString.substring(indexEnd + 1));
						}
					}
				} else {
					// entity start without matching entity end detected, ignore
					// gracefully
					isoBuffer.append(htmlString.substring(indexStart));
					break;
				}
			}
			return isoBuffer.toString();
		} else {
			// nothing to do
			return htmlString;
		}
	}

	/**
	 * convert a single char to its equivalent HTML entity. Ordinary chars are
	 * not changed. 160 -> &nbsp;
	 * 
	 * @param c
	 *            Char to convert
	 * 
	 * @return equivalent string eg. &amp;, null means leave char as is.
	 */
	protected static String charToEntity(char c) {
		switch (c) {
		case 34:
			return "&quot;";
		case 38:
			return "&amp;";
		case 60:
			return "&lt;";
		case 62:
			return "&gt;";
		default:
			if (c < 127) {
				// leave alone as equivalent string.
				return null;
				// faster than String.valueOf( c ).intern();
			} else {
				// use the &#nnn; form
				return "&#" + Integer.toString(c) + ";";
			}
		} // end switch
	} // end charToEntity

	/**
	 * Converts text to HTML by quoting dangerous characters. Text must not
	 * already contain entities. e.g. " ==> &quot; < ==> &lt; ordinary text
	 * passes unchanged. Does not convert space to &nbsp;
	 * 
	 * @param text
	 *            raw text to be processed. Must not be null.
	 * 
	 * @return translated text, or null if input is null.
	 */
	public static String clean(String text) {
		if (text == null)
			return null;
		int originalTextLength = text.length();
		StringBuilder sb = new StringBuilder(originalTextLength * 110 / 100);
		int charsToAppend = 0;
		for (int i = 0; i < originalTextLength; i++) {
			char c = text.charAt(i);
			String entity = charToEntity(c);
			if (entity == null) {
				// we could sb.append( c ), but that would be slower
				// than saving them up for a big append.
				charsToAppend++;
			} else {
				if (charsToAppend != 0) {
					sb.append(text.substring(i - charsToAppend, i));
					charsToAppend = 0;
				}
				sb.append(entity);
			}
		} // end for
		// append chars to the right of the last entity.
		if (charsToAppend != 0) {
			sb.append(text.substring(originalTextLength - charsToAppend,
					originalTextLength));
		}

		// if result is not longer, we did not do anything. Save RAM.
		return (sb.length() == originalTextLength) ? text : sb.toString();
	} // end insertEntities

	public static String cleanGPX(String str) {
		String dummy = new String();

		dummy = str.replace("&", "&amp;");
		dummy = dummy.replace("<", "&lt;");
		dummy = dummy.replace(">", "&gt;");
		// dummy = replace(dummy, "&nbsp;", "&amp;nbsp;");
		dummy = dummy.replace("\"", "&quot;");
		dummy = dummy.replace("'", "&apos;");
		dummy = dummy.replace("]]>", "]] >");

		return dummy;
	}

	/**
	 * Converts a data string to something that is safe to use inside an XML
	 * file (like prefs.xml) - entities like &amp; are *NOT* valid XML unless
	 * declared specially, so we must use the numerical values here.
	 * 
	 * @param src
	 *            (String) raw text to be processed
	 * 
	 * @return (String) translated text, or null if input is null
	 */
	public static String strxmlencode(boolean src) {
		/* bools are always safe */
		return (src ? "true" : "false");
	}

	public static String strxmlencode(int src) {
		/* numbers are always safe */
		return (Integer.toString(src));
	}

	public static String strxmlencode(String src) {
		int i, slen;
		char tmp[];
		StringBuilder dst;

		if (src == null)
			return (null);

		slen = src.length();
		dst = new StringBuilder(slen);
		tmp = new char[slen];
		src.getChars(0, slen, tmp, 0);
		for (i = 0; i < slen; ++i)
			if (tmp[i] == '&' || tmp[i] == '<' || tmp[i] == '>'
					|| tmp[i] > 0x7E) {
				dst.append("&#");
				dst.append((int) tmp[i]);
				dst.append(';');
			} else
				dst.append(tmp[i]);
		return (dst.toString());
	}

	/**
	 * Converts a string that is safe to use inside an XML file (like prefs.xml)
	 * back to a data string - entities like &amp; are *NOT* valid XML unless
	 * declared specially, so we must use the numerical values here. We also try
	 * to decode non-standard entities.
	 * 
	 * @param src
	 *            (String) translated text to be processed
	 * 
	 * @return (String) raw text, or null if input is null
	 */
	public static String strxmldecode(String src) {
		int i, j, slen;
		char ch, tmp[];
		StringBuilder dst;
		boolean isinval;

		if (src == null)
			return (null);

		slen = src.length();
		dst = new StringBuilder(slen);
		tmp = new char[slen];
		src.getChars(0, slen, tmp, 0);
		i = 0;
		while (i < slen)
			if (tmp[i] == '&') {
				/* first scan if we have a trailing ; */
				if (src.indexOf(';', i) == -1) {
					/* no - ignore and proceed */
					i++;
					dst.append(0xFFFD);
				} else if (tmp[++i] == '#') {
					/* yes - numerical value? */
					i++;
					ch = 0;
					isinval = false;
					if (tmp[i] == 'x' || tmp[i] == 'X') {
						/* hexadecimal numeric */
						i++;
						while ((j = tmp[i++]) != ';') {
							ch *= 16;
							if (j < 0x30)
								isinval = true;
							else if (j < 0x3A)
								ch += j - 0x30;
							else if (j < 0x41)
								isinval = true;
							else if (j < 0x47)
								ch += j - 0x37;
							else if (j < 0x61)
								isinval = true;
							else if (j < 0x67)
								ch += j - 0x57;
							else
								isinval = true;
						}
					} else
						/* decimal numeric */
						while ((j = tmp[i++]) != ';') {
							ch *= 10;
							if (j < 0x30)
								isinval = true;
							else if (j < 0x3A)
								ch += j - 0x30;
							else
								isinval = true;
						}
					if (isinval)
						ch = 0xFFFD;
					dst.append(ch);
				} else {
					/* yes - string value */
					StringBuilder tconv = new StringBuilder("#");
					String tc;

					do {
						tconv.append(tmp[i]);
					} while (tmp[i++] != ';');

					if ((tc = (String) iso2htmlMappings.get(tconv)) == null)
						/* invalid entity, just retain it */
						dst.append(tconv);
					else
						dst.append(tc);
				}
			} else
				dst.append(tmp[i++]);
		return (dst.toString());
	}

	public static String removeHtml(String src) {
		int posStart = 0;

		if (src == null)
			return null;
		String dst = src;

		while (posStart >= 0) {
			if ((posStart = dst.indexOf("<")) >= 0) {
				int posEnd;
				if ((posEnd = dst.indexOf(">", posStart)) < 0)
					break;
				if (posEnd < dst.length()) {
					String temp = dst.substring(posStart, posEnd + 1);
					dst = dst.replace(temp, " ");
				}

			}

		}

		for (int i = 1; i <= 31; ++i) {
			if ((i == 10) || (i == 13)) {
				continue;
			}
			char chDelete = (char) i;
			dst = dst.replace(chDelete, ' ');
		}

		String chLF = "\n";
		dst = dst.replace(chLF, " \n");

		dst = dst.replace("", " ");

		dst = dst.replace("<", "lt");
		dst = dst.replace(">", "gt");

		dst = dst.replace("#nbsp;", " ");
		dst = dst.replace("#amp;", "+");
		dst = dst.replace("&", "+");

		return dst.toString();
	}

}
