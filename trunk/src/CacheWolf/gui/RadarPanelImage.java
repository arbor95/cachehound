package CacheWolf.gui;

import ewe.fx.Image;
import ewe.fx.mImage;
import ewe.graphics.AniImage;

/**
 * The ImagePanelImage extends AniImage by a fileName. This is an easy way to
 * identify the image clicked, what is needed to display the full image from the
 * thumbnail.
 */
public class RadarPanelImage extends AniImage {
	public String wayPoint = new String();
	public int rownum;

	public RadarPanelImage(mImage i) {
		super(i);
	}

	public RadarPanelImage(Image i) {
		super(i);
	}
}
