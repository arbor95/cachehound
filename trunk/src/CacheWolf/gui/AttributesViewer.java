package CacheWolf.gui;

import CacheWolf.beans.Attribute;
import CacheWolf.beans.Attributes;
import CacheWolf.util.MyLocale;
import ewe.fx.Rect;
import ewe.fx.mImage;
import ewe.graphics.AniImage;
import ewe.graphics.InteractivePanel;
import ewe.ui.CellConstants;
import ewe.ui.CellPanel;
import ewe.ui.mLabel;

public class AttributesViewer extends CellPanel {
	private static int TILESIZE = Attribute.getImageWidth() + 2;
	private final static int ICONS_PER_ROW = MyLocale.getScreenWidth()
			/ TILESIZE < Attributes.MAXATTRIBS ? MyLocale.getScreenWidth()
			/ TILESIZE : Attributes.MAXATTRIBS;
	private final static int ICONROWS = (Attributes.MAXATTRIBS + ICONS_PER_ROW - 1)
			/ ICONS_PER_ROW;
	protected mLabel mInfo;

	protected class attInteractivePanel extends InteractivePanel {
		public boolean imageMovedOn(AniImage which) {
			if (!((attAniImage) which).info.startsWith("*")) { // If text
				// starts
				// with * we
				// have no
				// explanation
				// yet
				mInfo.setText(((attAniImage) which).info);
				mInfo.repaintNow();
			}
			return true;
		}

		public boolean imageMovedOff(AniImage which) {
			mInfo.setText("");
			mInfo.repaintNow();
			return true;
		}
	}

	protected class attAniImage extends AniImage {
		public String info;

		attAniImage(mImage img) {
			super(img);
		}
	}

	public AttributesViewer() {
		Rect r = new Rect(0, 0, TILESIZE * ICONS_PER_ROW, TILESIZE * ICONROWS); // As
		// on
		// GC:
		// 6
		// wide,
		// 2
		// high
		iap.virtualSize = r;
		iap.setFixedSize(TILESIZE * ICONS_PER_ROW, TILESIZE * ICONROWS);
		addLast(iap, CellConstants.HSTRETCH, CellConstants.FILL);
		addLast(mInfo = new mLabel(""), HSTRETCH, HFILL);
	}

	protected InteractivePanel iap = new attInteractivePanel();

	public void showImages(Attributes att) {
		iap.images.clear();
		for (int i = 0; i < att.getCount(); i++) {
			attAniImage img = new attAniImage(att.getImage(i));
			img.info = att.getInfo(i);
			img.location = new Rect((i % ICONS_PER_ROW) * TILESIZE,
					(i / ICONS_PER_ROW) * TILESIZE, TILESIZE, TILESIZE);
			iap.addImage(img);
		}
		iap.repaintNow();
	}

	public void clear() {
		iap.images.clear();
	}
	/*
	 * public void resizeTo(int width, int height) {
	 * super.resizeTo(width,height); }
	 */
}
