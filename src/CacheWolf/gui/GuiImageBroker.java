package CacheWolf.gui;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.cachehound.types.CacheType;
import ewe.fx.Image;

/**
 * hold preloaded versions of GUI images in a single place
 * 
 * Do not instantiate this class, only use it in a static way.
 */

public final class GuiImageBroker {

	private static Logger logger = LoggerFactory
			.getLogger(GuiImageBroker.class);

	// singleton
	private static GuiImageBroker instance = new GuiImageBroker();

	// TODO: check with Image and mImage

	/** image to be displayed in case of error */
	private Image imageError = new Image("guiError.png");

	/**
	 * images to be displayed for cache types in GUI
	 * 
	 * @see getTypeImage
	 * @see CacheTypes
	 */
	private final Image[] typeImages;

	/** constructor does nothing */
	private GuiImageBroker() { // no instantiation needed
		typeImages = new Image[CacheType.values().length];
		int i = 0;
		for (CacheType type : CacheType.values()) {
			typeImages[i] = new Image(type.getGuiImage());
			i++;
		}
	}

	public static GuiImageBroker getInstance() {
		return instance;
	}

	/**
	 * select image to be displayed for a given cache type
	 * 
	 * @param id
	 *            internal cache type id
	 * @return <code>Image</code> object to be displayed
	 */
	public Image getTypeImage(CacheType type) {
		return typeImages[type.ordinal()];
	}

	public Image getErrorImage() {
		return imageError;
	}

	/**
	 * Replaces the build-in symbols by images stored in /symbols: If the sub
	 * directory symbols exists in CW-directory *.png-files are read in and
	 * roughly checked for validity (names must be convertible to integers
	 * between 0 and 21). For every valid file x.png the corresponding
	 * typeImages[x] is replaced by the image in x.png. Images are NOT checked
	 * for size etc.
	 */
	public static void customizedSymbols() {
		File dir = new File("symbols");
		if (dir.isDirectory()) {
			logger.error("Customized Symbols are right now not Supported!");
			// 
			// int id;
			// String name = "";
			// String[] pngFiles;
			// pngFiles = dir.list("*.png", 0);
			// for (int i = 0; i < pngFiles.length; i++) {
			// name = pngFiles[i].substring(0, pngFiles[i].length() - 4);
			// try {
			// id = Integer.parseInt(name);
			// } catch (Exception E) {
			// id = -1; // filename invalid for symbols
			// }
			// if (0 <= id && id <= typeImages.length) {
			// typeImages[id] = new Image(FileBase.getProgramDirectory()
			// + "/symbols/" + pngFiles[i]);
			// }
			// }
		}
	}

}
