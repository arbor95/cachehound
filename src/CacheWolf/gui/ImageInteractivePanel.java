package CacheWolf.gui;

import ewe.fx.Point;
import ewe.fx.Rect;
import ewe.fx.mImage;
import ewe.graphics.AniImage;
import ewe.graphics.InteractivePanel;
import ewe.ui.ControlEvent;
import ewe.ui.Menu;

/**
 * This class handles the resizing im images
 */
public class ImageInteractivePanel extends InteractivePanel {
	private int state = -1; // 0 = nothing, -1 = scaled to window, 1 = scaled to
	// original size
	private String imgLoc = new String();
	private AniImage pic = null;
	private Menu mClose = new Menu(new String[] { "Close" }, "");

	public ImageInteractivePanel() {
		super();
		this.setMenu(mClose);
	}

	public void resizeTo(int w, int h) {
		this.width = w;
		this.height = h;
		if (state == -1)
			fitImageToWindow();
		virtualSize = new Rect(0, 0, java.lang.Math.max(w, pic.getWidth()),
				java.lang.Math.max(h, pic.getHeight()));
		checkScrolls();
		super.resizeTo(w, h);
	}

	public void setImage(String filename) {
		imgLoc = filename;
		mImage mI = new mImage(imgLoc);
		if (pic != null) {
			this.removeImage(pic);
			pic.freeSource();
			pic.free();
		}
		pic = new AniImage(mI);
		pic.setLocation(0, 0);
		mI.freeSource();
		// mI.free(); this works in the java-VM, but it will delete the image in
		// the ewe-vm --> leave it commeted out
		this.addImage(pic);
		virtualSize = new Rect(pic.getSize(null));
		checkScrolls();
	}

	public void fitImageToWindow() {
		Rect s = this.parent.getRect();
		int ww = pic.getWidth();
		int wh = pic.getHeight();
		double scale = java.lang.Math.max((double) ww / (double) s.width,
				(double) wh / (double) s.height);
		if (scale != 1) {
			this.removeImage(pic);
			AniImage tmp = new AniImage(pic.scale((int) (ww / scale),
					(int) (wh / scale), null, 0));
			pic.freeSource();
			pic.free();
			pic = tmp;
			pic.setLocation(0, 0);
			this.addImage(pic);
			virtualSize = new Rect(pic.getSize(null));
			checkScrolls();
		}

	}

	public void imageClicked(AniImage which, Point pos) {
		state = -state;
		if (state == 1) {
			setImage(imgLoc);
			this.repaintNow();
		}
		if (state == -1) {
			fitImageToWindow();
			this.repaintNow();
		}
	}

	public void penRightReleased(Point p) {
		menuState.doShowMenu(p, true, null); // direct call (not through
		// doMenu)
		// is neccesary because it will
		// exclude the whole table
	}

	public void penHeld(Point p) {
		menuState.doShowMenu(p, true, null);
	}

	public void popupMenuEvent(Object selectedItem) {
		postEvent(new ControlEvent(ControlEvent.EXITED, this));
	}

	public void formClosing() {
		super.formClosing();
		if (pic != null) {
			pic.freeSource();
			pic.free();
		}

	}
}
