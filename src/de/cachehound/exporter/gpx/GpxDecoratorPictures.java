package de.cachehound.exporter.gpx;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

import org.jdom.CDATA;
import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import CacheWolf.Global;
import CacheWolf.beans.ImageInfo;
import CacheWolf.exporter.Base64Coder;
import de.cachehound.beans.ICacheHolder;

public class GpxDecoratorPictures implements IGpxDecorator {
	private static Logger logger = LoggerFactory
			.getLogger(GpxDecoratorPictures.class);

	@Override
	public void decorateDomTree(Element doc, ICacheHolder ch) {
		// Guard: Only works for Caches, not for Waypoints
		if (!ch.isCacheWpt()) {
			return;
		}
		Element wpt = doc;

		String oldImageName = null;
		for (ImageInfo image : ch.getDetails().getImages()) {
			if (image.getFilename().equals(oldImageName)) {
				continue;
			}
			oldImageName = image.getFilename();

			Element multimedia = new Element("multimedia");
			String imageName = image.getFilename().substring(0,
					image.getFilename().indexOf('.'));
			multimedia.setAttribute("name", imageName);
			if (image.getFilename().endsWith("jpg")
					|| image.getFilename().endsWith("jpeg")) {
				multimedia.setAttribute("type", "image/jpeg");
			} else if (image.getFilename().endsWith("gif")) {
				multimedia.setAttribute("type", "image/gif");
			} else {
				logger.warn("Unknown Type of Image: {}" + image.getFilename());
			}
			wpt.addContent(multimedia);

			Element data = new Element("data");
			File file = new File(Global.getProfile().getDataDir(), image
					.getFilename());
			CDATA cdata = new CDATA(getFileInBase64(file));
			data.addContent(cdata);
			multimedia.addContent(data);
		}
	}

	private String getFileInBase64(File file) {
		byte[] buf = new byte[16384];
		try {
			int help;
			FileInputStream inputFile = new FileInputStream(file);
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();

			while ((help = inputFile.read(buf)) != -1)
				outStream.write(buf, 0, help);
			inputFile.close();

			String encoded = new String(Base64Coder.encode(outStream
					.toByteArray()));
			return encoded;
		} catch (Exception e) {
			logger.error("Error while writing File " + file.getAbsolutePath()
					+ " into a String (Base 64 encoded)", e);
		}
		return "";
	}

}
