package CacheWolf.gui;

import ewe.fx.mImage;
import ewe.graphics.AniImage;

/**
 * The ImagePanelImage extends AniImage by a fileName. This is an easy way to
 * identify the image clicked, what is needed to display the full image from the
 * thumbnail.
 */
public class ImagePanelImage extends AniImage {
	public String fileName = new String();
	public String imageText = null;

	public ImagePanelImage(mImage i) {
		super(i);
	}
}
