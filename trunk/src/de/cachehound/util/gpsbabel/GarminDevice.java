package de.cachehound.util.gpsbabel;

public class GarminDevice implements IBabelFormat {
	private String port;

	public GarminDevice(String port) {
		this.port = port;
	}

	@Override
	public String getFileName() {
		return port;
	}

	@Override
	public String getType() {
		return "garmin";
	}
}
