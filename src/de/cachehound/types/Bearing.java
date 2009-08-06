package de.cachehound.types;

public enum Bearing {
	N, NNE, NE, ENE, E, ESE, SE, SSE, S, SSW, SW, WSW, W, WNW, NW, NNW;

	/**
	 * Erstellt ein Bearing-Objekt aus einer Winkelangabe.
	 * 
	 * @param d
	 *            Der Winkel
	 * @return
	 */
	public static Bearing fromDeg(double d) {
		if (d > 360.5 || d < -0.5) {
			return null;
		}

		int i = 1;
		for (Bearing b : Bearing.values()) {
			if (d < 360.0d * i / (Bearing.values().length * 2)) {
				return b;
			}
			i += 2;
		}
		return Bearing.N;
	}
}
