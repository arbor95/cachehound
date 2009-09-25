package de.cachehound.types;

public enum Terrain {

	TERRAIN_1_0("1", "1.0", (byte) 10, true),

	TERRAIN_1_5("1.5", "1.5", (byte) 15, true),

	TERRAIN_2_0("2", "2.0", (byte) 20, true),

	TERRAIN_2_5("2.5", "2.5", (byte) 25, true),

	TERRAIN_3_0("3", "3.0", (byte) 30, true),

	TERRAIN_3_5("3.5", "3.5", (byte) 35, true),

	TERRAIN_4_0("4", "4.0", (byte) 40, true),

	TERRAIN_4_5("4.5", "4.5", (byte) 45, true),

	TERRAIN_5_0("5", "5.0", (byte) 50, true),

	TERRAIN_UNSET("unset", "unset", (byte) 0, false),

	TERRAIN_ERROR("error", "error", (byte) -1, false);

	private String shortRepresenation;
	private String fullRepresentation;
	private byte oldCWValue;
	private boolean valid;

	private Terrain(String shortRepresenation, String fullRepresentation,
			byte oldCWValue, boolean valid) {
		this.shortRepresenation = shortRepresenation;
		this.fullRepresentation = fullRepresentation;
		this.oldCWValue = oldCWValue;
		this.valid = valid;
	}

	public String getShortRepresentation() {
		return shortRepresenation;
	}
	
	public String getFullRepresentation() {
		return fullRepresentation;
	}

	@Override
	public String toString() {
		return shortRepresenation;
	}

	public boolean isValid() {
		return valid;
	}
	
	public byte getOldCWValue() {
		return oldCWValue;
	}

	public static Terrain fromString(String value) {
		for (Terrain actualValue : values()) {
			if (actualValue.fullRepresentation.equals(value)
					|| actualValue.shortRepresenation.equals(value)) {
				return actualValue;
			}
		}
		throw new IllegalArgumentException("Error mapping terrainy. Found: "
				+ value);
	}
	
	public static Terrain fromOldCWByte(byte value) {
		for (Terrain actualValue : values()) {
			if (actualValue.oldCWValue == value) {
				return actualValue;
			}
		}
		throw new IllegalArgumentException("Error mapping Terrain. Found byte: "
				+ value);
	}

}
