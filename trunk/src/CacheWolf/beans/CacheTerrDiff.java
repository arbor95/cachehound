package CacheWolf.beans;

/**
 * Handles all aspects of converting terrain and difficulty informations from
 * legacy file versions and various im- and exporters
 * 
 * Only use the class in a static way, do not instantiate it
 */
public class CacheTerrDiff {

	/** terrain or difficulty 1.0 */
	public static final byte CW_DT_10 = 10;
	/** terrain or difficulty 1.5 */
	public static final byte CW_DT_15 = 15;
	/** terrain or difficulty 2.0 */
	public static final byte CW_DT_20 = 20;
	/** terrain or difficulty 2.5 */
	public static final byte CW_DT_25 = 25;
	/** terrain or difficulty 3.0 */
	public static final byte CW_DT_30 = 30;
	/** terrain or difficulty 3.5 */
	public static final byte CW_DT_35 = 35;
	/** terrain or difficulty 4.0 */
	public static final byte CW_DT_40 = 40;
	/** terrain or difficulty 4.5 */
	public static final byte CW_DT_45 = 45;
	/** terrain or difficulty 5.0 */
	public static final byte CW_DT_50 = 50;
	/** wrong terrain or difficulty */
	public static final byte CW_DT_ERROR = -1;
	/** terrain or difficulty for additional/custom waypoints */
	public static final byte CW_DT_UNSET = 0;

	/** constructor dies nothing */
	private CacheTerrDiff() { // no instantiation needed
	}

	/**
	 * convert "old style" terrain and difficulty information to the new format.
	 * 
	 * since it is also used by the importers it is not flagged as depreciated
	 * 
	 * @param asString
	 *            a string representation of terrain or difficulty
	 * @return internal representation of terrain or difficulty
	 * @throws IllegalArgumentException
	 *             if <code>v1TerrDiff</code> can not be mapped
	 */
	public static final byte stringToByteRepresentation(String asString)
			throws IllegalArgumentException {
		if (asString == null) {
			throw new IllegalArgumentException(
					"error mapping terrain or difficulty");
		}
		asString = asString.replace(',', '.');
		if (asString.equals("1") || asString.equals("1.0"))
			return CW_DT_10;
		if (asString.equals("2") || asString.equals("2.0"))
			return CW_DT_20;
		if (asString.equals("3") || asString.equals("3.0"))
			return CW_DT_30;
		if (asString.equals("4") || asString.equals("4.0"))
			return CW_DT_40;
		if (asString.equals("5") || asString.equals("5.0"))
			return CW_DT_50;

		if (asString.equals("1.5"))
			return CW_DT_15;
		if (asString.equals("2.5"))
			return CW_DT_25;
		if (asString.equals("3.5"))
			return CW_DT_35;
		if (asString.equals("4.5"))
			return CW_DT_45;

		throw new IllegalArgumentException(
				"error mapping terrain or difficulty");
	}

	/**
	 * generate strings of terrain and difficulty for general use
	 * 
	 * @param td
	 *            internal terrain or difficulty value
	 * @return long version of terrain or difficulty (including .0)
	 * @throws IllegalArgumentException
	 */
	public static final String longDT(byte td) throws IllegalArgumentException {
		switch (td) {
		case CW_DT_10:
			return "1.0";
		case CW_DT_15:
			return "1.5";
		case CW_DT_20:
			return "2.0";
		case CW_DT_25:
			return "2.5";
		case CW_DT_30:
			return "3.0";
		case CW_DT_35:
			return "3.5";
		case CW_DT_40:
			return "4.0";
		case CW_DT_45:
			return "4.5";
		case CW_DT_50:
			return "5.0";
		default:
			throw new IllegalArgumentException("unmapped terrain or diffulty "
					+ td);
		}
	}

	/**
	 * generate strings of terrain and difficulty information for GC.com-like
	 * GPX exports
	 * 
	 * @param td
	 *            internal terrain or difficulty value
	 * @return short version of terrain or difficulty (omit .0)
	 * @throws IllegalArgumentException
	 */
	public static final String shortDT(byte td) throws IllegalArgumentException {
		switch (td) {
		case CW_DT_10:
			return "1";
		case CW_DT_15:
			return "1.5";
		case CW_DT_20:
			return "2";
		case CW_DT_25:
			return "2.5";
		case CW_DT_30:
			return "3";
		case CW_DT_35:
			return "3.5";
		case CW_DT_40:
			return "4";
		case CW_DT_45:
			return "4.5";
		case CW_DT_50:
			return "5";
		default:
			throw new IllegalArgumentException("unmapped terrain or diffulty "
					+ td);
		}
	}

	/**
	 * check if a given difficulty or terrain is valid takes about 1/20th of the
	 * time a try {} catch{} block needs so use this function instead
	 * 
	 * @param td
	 *            terrain or difficulty to check
	 * @return true if terrain or difficulty is valid, false otherwise
	 */
	public static final boolean isValidTD(byte td) {
		switch (td) {
		case CW_DT_10:
		case CW_DT_15:
		case CW_DT_20:
		case CW_DT_25:
		case CW_DT_30:
		case CW_DT_35:
		case CW_DT_40:
		case CW_DT_45:
		case CW_DT_50:
			return true;
		default:
			return false;
		}
	}
}
