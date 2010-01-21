package de.cachehound.util.gpsbabel;

import java.io.File;

public class LocFile implements IBabelFormat {
	private File file;

	public LocFile(File file) {
		this.file = file;
	}

	@Override
	public String getFileName() {
		return file.getPath();
	}

	@Override
	public String getType() {
		return "geo";
	}
}
