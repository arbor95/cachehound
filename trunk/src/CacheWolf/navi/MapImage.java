package CacheWolf.navi;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ewe.fx.Color;
import ewe.fx.Dimension;
import ewe.fx.Image;
import ewe.fx.ImageDecodingException;
import ewe.fx.ImageNotFoundException;
import ewe.fx.Point;
import ewe.fx.UnsupportedImageFormatException;
import ewe.fx.mImage;
import ewe.graphics.AniImage;
import ewe.util.ByteArray;

/**
 * class that can be used with any x and any y it will save taht location and
 * make itself automatically invisible if it is not on the screen. Call
 * setscreensize to set the screensize
 * 
 * @author pfeffer
 * 
 */
public class MapImage extends AniImage {

	private static Logger logger = LoggerFactory.getLogger(MapImage.class);

	public Point locAlways = new Point(); // contains the theoretical location
	// even if it the location is out of
	// the screen. If the image is on
	// the screen, it contains the same
	// as location
	public static Dimension screenDim;
	boolean hidden = false;

	public MapImage() {
		super();
		if (screenDim == null)
			screenDim = new Dimension(0, 0);
	}

	public MapImage(File file) throws ImageDecodingException,
			UnsupportedImageFormatException, ImageNotFoundException,
			ewe.sys.SystemResourceException {
		if (screenDim == null)
			screenDim = new Dimension(0, 0);
		try {
			BufferedInputStream in = new BufferedInputStream(
					new FileInputStream(file));
			byte[] buf = new byte[4096];
			int length;
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			while (-1 != (length = in.read(buf))) {
				out.write(buf, 0, length);
			}
			// copied from super()
			setImage(new Image(new ByteArray(out.toByteArray()), 0), 0);
			freeSource(); // copied from super()
		} catch (IOException e) {
			logger.error("IOException at creating Loading image "
					+ file.getAbsolutePath(), e);
			// in order to behave the
			// same way as super would have
			throw new ImageNotFoundException(file.getAbsolutePath()); 
		}
	}

	public MapImage(mImage im) {
		super(im);
		if (screenDim == null)
			screenDim = new Dimension(0, 0);
	}

	/**
	 * Best you call this routine before you make any instance of MapImage If
	 * the windows size changes after instantiation call screenDimChanged() for
	 * every symbol.
	 * 
	 */
	public static void setScreenSize(int w, int h) {
		screenDim = new Dimension(w, h);
	}

	public void setImage(Image im, Color c) {
		super.setImage(im, c);
		if (screenDim == null)
			screenDim = new Dimension(0, 0);
	}

	public void setLocation(int x, int y) {
		locAlways.x = x;
		locAlways.y = y;
		if (!hidden && isOnScreen()) {
			super.setLocation(x, y);
			properties &= ~mImage.IsInvisible;
		} else {
			properties |= mImage.IsInvisible;
			super.move(0, 0);
		}
	}

	public void move(int x, int y) {
		locAlways.x = x;
		locAlways.y = y;
		if (!hidden && isOnScreen()) {
			super.move(x, y);
			properties &= ~mImage.IsInvisible;
		} else {
			properties |= mImage.IsInvisible;
			super.move(0, 0);
		}
	}

	public boolean isOnScreen() {
		if ((locAlways.x + location.width > 0 && locAlways.x < screenDim.width)
				&& (locAlways.y + location.height > 0 && locAlways.y < screenDim.height))
			return true;
		else
			return false;
	}

	public void screenDimChanged() {
		move(locAlways.x, locAlways.y);
		// if (!hidden && isOnScreen()) properties &= ~AniImage.IsInvisible;
		// else properties |= AniImage.IsInvisible;
	}

	public void hide() {
		hidden = true;
		properties |= mImage.IsInvisible;
	}

	public void unhide() {
		hidden = false;
		move(locAlways.x, locAlways.y);
	}
}
