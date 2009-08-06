package de.cachehound.types;

public enum CacheSize {

	NOT_CHOSEN("Not chosen", "sizeNonPhysical.png", Integer.MIN_VALUE,
			Integer.MIN_VALUE, 'n', (byte) 0, (byte) (0x01 << 5)), NONE("None",
			"sizeNonPhysical.png", 7, Integer.MIN_VALUE, 'n', (byte) 7,
			(byte) (0x01 << 5)), VIRTUAL("Virtual", "sizeNonPhysical.png",
			Integer.MIN_VALUE, Integer.MIN_VALUE, 'n', (byte) 8,
			(byte) (0x01 << 5)), OTHER("Other", "unset", 1, Integer.MIN_VALUE,
			'n', (byte) 1, (byte) (0x01 << 5)), MICRO("Micro", "sizeMicro.png",
			2, 1, 'm', (byte) 2, (byte) (0x01 << 0)), SMALL("Small",
			"sizeSmall.png", 3, 2, 's', (byte) 3, (byte) (0x01 << 1)), REGULAR(
			"Regular", "sizeReg.png", 4, 3, 'r', (byte) 4, (byte) (0x01 << 2)), LARGE(
			"Large", "sizeLarge.png", 5, 4, 'l', (byte) 5, (byte) (0x01 << 3)), VERY_LARGE(
			"Very large", "sizeVLarge.png", 6, Integer.MIN_VALUE, 'v',
			(byte) 6, (byte) (0x01 << 4));

	/**
	 * OpenCaching Size IDs see
	 * http://oc-server.svn.sourceforge.net/viewvc/oc-server
	 * /doc/sql/static-data/data.sql?view=markup
	 */
	private int ocSizeId;

	/**
	 * TerraCaching Size IDs taken from old GPXimporter (?? reliable source ??)
	 */
	private int tcSizeId;
	private byte oldCwId;

	/**
	 * bit masks to be used with the filter function
	 */
	private byte filterPattern; // TODO: Refactor filtering
	private char shortId;
	private String gcSizeString;

	/**
	 * images to show in CW index panel we use less images than sizes since all
	 * non physical caches are represented by the same symbol
	 */
	private String cwSizeGuiFileName;

	private CacheSize(String gcSizeString, String cwSizeGuiFileName,
			int ocSizeId, int tcSizeId, char shortId, byte oldCwId,
			byte filterPattern) {
		this.gcSizeString = gcSizeString;
		this.cwSizeGuiFileName = cwSizeGuiFileName;
		this.ocSizeId = ocSizeId;
		this.tcSizeId = tcSizeId;
		this.shortId = shortId;
		this.oldCwId = oldCwId;
		this.filterPattern = filterPattern;
	}

	public String getSizeImage() {
		return cwSizeGuiFileName;
	}

	public String getAsString() {
		return gcSizeString;
	}

	public char getAsChar() {
		return shortId;
	}

	public byte getOldCwId() {
		return oldCwId;
	}

	public byte getFilterPattern() {
		return filterPattern;
	}

	public static byte getAllFilterPatterns() {
		byte result = 0x00;
		for (CacheSize size : CacheSize.values()) {
			result |= size.getFilterPattern();
		}
		return result;
	}

	public static CacheSize fromOldCwId(byte id) {
		for (CacheSize size : CacheSize.values()) {
			if (size.oldCwId == id) {
				return size;
			}
		}
		throw (new IllegalArgumentException("unmatched argument " + id
				+ " in CacheSize fromOldCwId()"));
	}

	public static CacheSize fromTcGpxString(String tcString) {
		for (CacheSize size : CacheSize.values()) {
			if (Integer.valueOf(size.tcSizeId).toString().equals(tcString)) {
				return size;
			}
		}
		throw (new IllegalArgumentException("unmatched argument " + tcString
				+ " in CacheSize fromTcGpxString()"));
	}

	public static CacheSize fromNormalStringRepresentation(String gcString) {
		for (CacheSize size : CacheSize.values()) {
			if (size.gcSizeString.equalsIgnoreCase(gcString)) {
				return size;
			}
		}
		throw (new IllegalArgumentException("unmatched argument " + gcString
				+ " in CacheSize fromGcGpxString()"));
	}

	public static CacheSize fromOcString(String ocString) {
		for (CacheSize size : CacheSize.values()) {
			if (Integer.valueOf(size.ocSizeId).equals(ocString)) {
				return size;
			}
		}
		throw (new IllegalArgumentException("unmatched argument " + ocString
				+ " in CacheSize fromOcString()"));

	}

}
