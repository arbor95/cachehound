package de.cachehound.util.gpsbabel;

import java.io.File;

public class GPXFile implements IBabelFormat {
	private File file;
	
	public GPXFile(File file) {
		this.file = file;
	}

	@Override
	public String getFileName() {
		return file.getPath();
	}

	@Override
	public String getType() {
		return "gpx";
	}
}
