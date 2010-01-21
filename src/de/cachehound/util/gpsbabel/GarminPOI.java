package de.cachehound.util.gpsbabel;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class GarminPOI implements IBabelFormat {
	private File file;
	private Map<String, String> options = new HashMap<String, String>();

	public GarminPOI(File file) {
		this.file = file;
	}

	@Override
	public String getFileName() {
		return file.getPath();
	}

	@Override
	public String getType() {
		StringBuilder ret = new StringBuilder("garmin_gpi");

		for (String option : options.keySet()) {
			ret.append(",");
			ret.append(option);
			ret.append("=");
			ret.append(options.get(option));
		}

		return ret.toString();
	}

	public void setSleep(int time) {
		options.put("sleep", Integer.toString(time));
	}

	public void setCategory(String cat) {
		options.put("category", cat);
	}

	public void setBitmap(File bitmap) {
		options.put("bitmap", bitmap.getPath());
	}
}
