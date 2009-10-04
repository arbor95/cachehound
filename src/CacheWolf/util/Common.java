package CacheWolf.util;

import java.io.File;

import ewe.sys.Convert;

public final class Common {

	private static char digSep = MyLocale.getDigSeparator().charAt(0);
	private static char notDigSep = MyLocale.getDigSeparator().charAt(0) == '.' ? ','
			: '.';

	/**
	 * get double value from string. It interpretes "." and "," as decimal
	 * separator when the string cannot be interpreted, return 0.
	 * 
	 * @param value
	 * @return
	 */
	public static double parseDouble(String value) {
		// returns 0 for invalid arguments
		try {
			return parseDoubleException(value);
		} catch (Exception e) {
			return 0.0;
		}
	}

	/**
	 * throws some exception if the string could not be converted to double
	 * 
	 * @param value
	 * @return
	 */
	public static double parseDoubleException(String value) {
		return java.lang.Double.parseDouble(value.replace(notDigSep, digSep));
	}

	public static int parseInt(String value) {
		try {
			return java.lang.Integer.parseInt(value);
		} catch (Exception e) {
			return 0;
		}
	}



	public static String stringToHex(String str) {
		StringBuilder strBuf = new StringBuilder();
		StringBuilder strHex = new StringBuilder();
		StringBuilder strTxt = new StringBuilder();
		for (int i = 0; i < str.length(); i++) {
			strHex.append(Convert.longToHexString(str.charAt(i)) + " ");
			strTxt.append(str.charAt(i) + "  ");
		}
		strBuf.append(strTxt);
		strBuf.append("\n");
		strBuf.append(strHex);
		return strBuf.toString();
	}

	public static String ClearForFileName(String str) {
		String ret = str.replace('?', '_');
		ret = ret.replace(' ', '-');
		ret = ret.replace(':', '-');
		return ret;
	}

	/**
	 * finds the correct (existing) extension to an image filename
	 * 
	 * @param filename
	 *            without extension
	 * @return filename with extension
	 */
	static public File getImageFile(String namePrefix) {
		File file;
		String[] t = { ".png", ".gif", ".jpg", ".bmp", ".jpeg" };
		int i;
		for (i = 0; i < t.length; i++) {
			file =new File(namePrefix + t[i]); 
			if (file.exists())
				return file;
		}
		return null;
	}

	/**
	 * get the extension of a filename, including "." remark:
	 * ewe.io.File.getFileExtension return name + extension
	 * 
	 * @param fn
	 * @return
	 */
	public static String getFilenameExtension(String fn) {
		if (fn == null || fn.length() == 0)
			return "";
		int dot = fn.lastIndexOf(".");
		if (dot < 0)
			return "";
		return fn.substring(dot, fn.length());
	}

	public static String DoubleToString(double d, int decimalplaces) {
		ewe.sys.Double e = new ewe.sys.Double();
		e.set(d);
		e.decimalPlaces = decimalplaces;
		return e.toString().replace(',', '.');

	}

	public static String fixSerialPortName(String name) {
		if (name.startsWith("/"))
			return new String(".." + name); // on linux (*nix) machines it is
		// quite usual to give the complete
		// file path to the serial port, but
		// ewe expects only "ttyS0" or
		// similar
		else
			return name;
	}

}
