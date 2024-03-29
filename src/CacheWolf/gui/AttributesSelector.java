package CacheWolf.gui;

import CacheWolf.beans.Attribute;
import CacheWolf.util.MyLocale;
import ewe.fx.Image;
import ewe.fx.Point;
import ewe.fx.Rect;
import ewe.fx.mImage;
import ewe.graphics.AniImage;
import ewe.graphics.InteractivePanel;
import ewe.ui.CellConstants;
import ewe.ui.DataChangeEvent;
import ewe.ui.Panel;
import ewe.ui.mLabel;

public class AttributesSelector extends Panel {
	protected static int TILESIZE = 22; // Here we always use the small icons
	// thus tilesize=22
	public long selectionMaskYes = 0;
	public long selectionMaskNo = 0;
	protected mLabel mInfo;

	public AttributesSelector() {
		// Rect r = new Rect(0,0,TILESIZE * ICONS_PER_ROW,TILESIZE * ICONROWS);
		// // As on GC: 6 wide, 2 high
		iap.virtualSize = new Rect(0, 0, 200, 200);
		iap.setPreferredSize(170, 155);
		addLast(iap, CellConstants.STRETCH, FILL);
		addLast(mInfo = new mLabel(""), HSTRETCH, HFILL);
	}

	public void setSelectionMasks(long yes, long no) {
		selectionMaskYes = yes;
		selectionMaskNo = no;
		showAttributePalette();
	}

	protected class attAniImage extends AniImage {
		public String info;
		public String attrName;
		public String value;
		public int attrNr;
		public long bitMask;

		attAniImage(mImage img) {
			super(img);
		}

		attAniImage(attAniImage cp, String val) {
			// super(null);
			mImage rawImg = new mImage(Attribute.getImageDir() + cp.attrName
					+ val);
			setMImage(rawImg.getHeight() != 20 ? rawImg.scale(20, 20, null,
					Image.FOR_DISPLAY) : rawImg);
			value = val;
			info = MyLocale.getMsg(
					value.equals("-no.gif") ? (2500 + cp.attrNr - 1)
							: 2500 + cp.attrNr, "No attribute info found");
			attrName = cp.attrName;
			location = cp.location;
			attrNr = cp.attrNr;
			bitMask = cp.bitMask;
		}
	}

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

		public boolean imagePressed(AniImage which, Point pos) {
			if (which != null) {
				String value = ((attAniImage) which).value;
				long bit = ((attAniImage) which).bitMask;
				if (value.equals("-non.gif")) {
					selectionMaskYes |= bit;
					selectionMaskNo &= ~bit;
					value = "-yes.gif";
				} else if (value.equals("-yes.gif")) {
					selectionMaskYes &= ~bit;
					selectionMaskNo |= bit;
					value = "-no.gif";
				} else {
					selectionMaskYes &= ~bit;
					selectionMaskNo &= ~bit;
					value = "-non.gif";
				}
				attAniImage tmpImg = new attAniImage((attAniImage) which, value);
				removeImage(which);
				addImage(tmpImg);
				// System.out.println ("AniImage pressed: " +
				// ((attAniImage)which).info);
				refresh();
				notifyDataChange(new DataChangeEvent(
						DataChangeEvent.DATA_CHANGED, this));
			}
			return true;
		}
	}

	protected InteractivePanel iap = new attInteractivePanel();

	public void showAttributePalette() {
		iap.images.clear();
		int myWidth = 170;
		int myX = 2;
		int myY = 2;
		long bitMask = 0;
		String attrName;
		String value;
		for (int i = 0; i < Attribute.attributeNames.length; ++i) {
			if (Attribute.attributeNames[i].endsWith("-yes.gif")) {
				attrName = Attribute.attributeNames[i].substring(0,
						Attribute.attributeNames[i].length() - 8);
				bitMask = (1l << ((long) (java.lang.Math.ceil(i / 2.0) - 1.0)));
				if ((selectionMaskYes & bitMask) != 0)
					value = "-yes.gif";
				else if ((selectionMaskNo & bitMask) != 0)
					value = "-no.gif";
				else
					value = "-non.gif";
				mImage rawImg = new mImage(Attribute.getImageDir() + attrName
						+ value);
				attAniImage img = new attAniImage(
						rawImg.getHeight() != 20 ? rawImg.scale(20, 20, null,
								Image.FOR_DISPLAY) : rawImg);
				img.info = MyLocale.getMsg(2500 + i, "No attribute info found");
				img.value = value;
				img.attrName = attrName;
				img.attrNr = i;
				img.bitMask = bitMask;

				if (myX + TILESIZE > myWidth) {
					myX = 2;
					myY += TILESIZE;
				}
				img.location = new Rect(myX, myY, TILESIZE, TILESIZE);
				// System.out.println("img.location=new
				// Rect("+x+","+y+","+TILESIZE+","+TILESIZE+");");
				iap.addImage(img);
				myX += TILESIZE;
			}
		}
		iap.repaintNow();
	}

	/*
	 * public void resizeTo(int width, int height) {
	 * super.resizeTo(width,height); }
	 */
}
