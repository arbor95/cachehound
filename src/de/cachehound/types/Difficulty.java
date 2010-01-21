package de.cachehound.types;

public enum Difficulty {

	DIFFICULTY_1_0("1", "1.0", (byte) 10, true),

	DIFFICULTY_1_5("1.5", "1.5", (byte) 15, true),

	DIFFICULTY_2_0("2", "2.0", (byte) 20, true),

	DIFFICULTY_2_5("2.5", "2.5", (byte) 25, true),

	DIFFICULTY_3_0("3", "3.0", (byte) 30, true),

	DIFFICULTY_3_5("3.5", "3.5", (byte) 35, true),

	DIFFICULTY_4_0("4", "4.0", (byte) 40, true),

	DIFFICULTY_4_5("4.5", "4.5", (byte) 45, true),

	DIFFICULTY_5_0("5", "5.0", (byte) 50, true),

	DIFFICULTY_UNSET("unset", "unset", (byte) 0, false),

	DIFFICULTY_ERROR("error", "error", (byte) -1, false);

	private String shortRepresenation;
	private String fullRepresentation;
	private byte oldCWValue;
	private boolean valid;

	private Difficulty(String shortRepresenation, String fullRepresentation,
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

	public static Difficulty fromString(String value) {
		for (Difficulty actualValue : values()) {
			if (actualValue.fullRepresentation.equals(value)
					|| actualValue.shortRepresenation.equals(value)) {
				return actualValue;
			}
		}
		throw new IllegalArgumentException("Error mapping Difficulty. Found: "
				+ value);
	}

	public static Difficulty fromOldCWByte(byte value) {
		for (Difficulty actualValue : values()) {
			if (actualValue.oldCWValue == value) {
				return actualValue;
			}
		}
		throw new IllegalArgumentException(
				"Error mapping Difficulty. Found byte: " + value);
	}

}
