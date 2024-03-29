package CacheWolf.gui;

import CacheWolf.Global;
import CacheWolf.util.MyLocale;
import de.cachehound.beans.CacheHolderDetail;
import de.cachehound.factory.LogFactory;
import de.cachehound.util.Rot13;
import ewe.fx.Dimension;
import ewe.fx.Graphics;
import ewe.fx.Image;
import ewe.fx.Point;
import ewe.fx.Rect;
import ewe.fx.mImage;
import ewe.graphics.AniImage;
import ewe.graphics.ImageDragContext;
import ewe.graphics.InteractivePanel;
import ewe.io.FileBase;
import ewe.sys.Vm;
import ewe.ui.CellConstants;
import ewe.ui.CellPanel;
import ewe.ui.ControlConstants;
import ewe.ui.ControlEvent;
import ewe.ui.Event;
import ewe.ui.HtmlDisplay;
import ewe.ui.IKeys;
import ewe.ui.IScroll;
import ewe.ui.KeyEvent;
import ewe.ui.PanelSplitter;
import ewe.ui.ScrollBarPanel;
import ewe.ui.ScrollablePanel;
import ewe.ui.SplittablePanel;
import ewe.ui.mButton;
import ewe.ui.mTextPad;

/**
 * Class to create the panel that holds hints and logs. It holds a method to
 * cryt and decrypt hints. Two buttons allow for navigation through the logs. 5
 * logs are displayed at together. This was implemented to allow for better
 * performance on the PocketPC. This number can be changed in the preferences.
 * Class ID=400
 */
public class HintLogPanel extends CellPanel {
	private int crntLogPosition = 0;
	private CacheHolderDetail currCache;
	private final int DEFAULT_STRINGBUFFER_SIZE = 8000;
	private mTextPad hint = new mTextPad();
	// mTextPad logs = new mTextPad();
	private HtmlDisplay logs = new HtmlDisplay();
	private AniImage htmlTxtImage;
	private fastScrollText htmlImagDisp = new fastScrollText();
	private String decodeCaption = MyLocale.getMsg(400, "Decode");
	private String encodeCaption = MyLocale.getMsg(401, "Encode");
	private mButton decodeButton = new mButton(this.decodeCaption);
	private mButton moreBt = new mButton(">>");
	private mButton prevBt = new mButton("<<");
	private MyScrollBarPanel sbplog;
	private int lastScrollbarWidth = 0;
	private boolean hintIsDecoded = false;

	public HintLogPanel() {
		SplittablePanel split = new SplittablePanel(PanelSplitter.VERTICAL);
		CellPanel logpane = split.getNextPanel();
		CellPanel hintpane = split.getNextPanel();
		split.setSplitter(PanelSplitter.AFTER | PanelSplitter.HIDDEN,
				PanelSplitter.BEFORE | PanelSplitter.HIDDEN, 0);
		int initialHintHeight = Global.getPref().initialHintHeight;
		if (initialHintHeight < 0 || initialHintHeight > 1000)
			initialHintHeight = Global.getPref().DEFAULT_INITIAL_HINT_HEIGHT;
		hintpane.setPreferredSize(100, initialHintHeight);
		ScrollBarPanel sbphint = new MyScrollBarPanel(hint);
		hintpane.addLast(sbphint, CellConstants.STRETCH,
				(CellConstants.FILL | CellConstants.WEST));
		hintpane.addNext(prevBt, CellConstants.DONTSTRETCH,
				(CellConstants.HFILL | CellConstants.WEST));
		hintpane.addNext(decodeButton, CellConstants.HSTRETCH,
				(CellConstants.HFILL | CellConstants.WEST));
		decodeButton.setMinimumSize(MyLocale.getScreenWidth() * 2 / 3, 10);
		hintpane.addLast(moreBt, CellConstants.DONTSTRETCH,
				(CellConstants.HFILL | CellConstants.EAST));
		hint.modify(ControlConstants.NotEditable, 0);

		sbplog = new MyScrollBarPanel(htmlImagDisp,
				ScrollablePanel.NeverShowHorizontalScrollers);
		// logpane.addLast(sbplog,CellConstants.STRETCH, CellConstants.FILL);
		Rect r = new Rect(new Dimension(Global.getPref().myAppWidth
				- sbplog.vbar.getRect().width, 20));
		htmlImagDisp.virtualSize = r;
		htmlImagDisp.checkScrolls();
		logpane.addLast(sbplog.getScrollablePanel(), CellConstants.STRETCH,
				CellConstants.FILL);
		this.addLast(split);
		clear();
	}

	public void setText(CacheHolderDetail cache) {
		if (currCache != cache) {
			this.currCache = cache;
			resetHintText();
			crntLogPosition = 0;
			setLogs(0);
			moreBt.modify(0, ControlConstants.Disabled);
			prevBt.modify(0, ControlConstants.Disabled);
		}
	}

	public void clear() {
		clearOutput();
		currCache = null;
	}

	private void clearOutput() {
		if (htmlTxtImage != null) {
			htmlImagDisp.removeImage(htmlTxtImage);
			htmlTxtImage.free();
		}
	}

	private void setLogs(int crntLogPosition) {
		Vm.showWait(true);
		StringBuilder dummy = new StringBuilder(DEFAULT_STRINGBUFFER_SIZE);
		int counter = 0;
		int nLogs = currCache.getCacheLogs().size();
		int logsPerPage = Global.getPref().logsPerPage;
		for (int i = crntLogPosition; i < nLogs; i++) {
			dummy.append(LogFactory.getInstance().toHtml(
					currCache.getCacheLogs().getLog(i)));
			dummy.append("<br>");
			if (++counter >= logsPerPage)
				break;
		}
		clearOutput();
		logs.resizeTo(width, 50);
		// The cache GCP0T6 crashes the HtmlDisplay
		// As a temporary fix
		try {
			logs.startHtml();
			// set documentroot to prevent html renderer from loading remote
			// images
			logs.getDecoderProperties().set("documentroot",
					FileBase.getProgramDirectory());
			logs.addHtml(dummy.toString(), new ewe.sys.Handle());
			logs.endHtml();
		} catch (Exception e) {
			logs = new HtmlDisplay();
			Global.getPref().log("Error rendering HTML", e, true);
			logs.setPlainText("Ewe VM: Internal error displaying logs");
		}
		int h = logs.getLineHeight() * logs.getNumLines();
		htmlTxtImage = new AniImage(new Image(width, h));
		htmlTxtImage.setLocation(0, 0);
		htmlTxtImage.properties |= mImage.IsMoveable;
		Graphics draw = new Graphics(htmlTxtImage.image);
		logs.resizeTo(htmlTxtImage.getWidth() - lastScrollbarWidth,
				htmlTxtImage.getHeight());
		logs.doPaint(draw, new Rect(0, 0, htmlTxtImage.getWidth(), htmlTxtImage
				.getHeight()));
		htmlImagDisp.addImage(htmlTxtImage);
		Rect r = new Rect(new Dimension(width, h));
		htmlImagDisp.virtualSize = r;
		htmlImagDisp.origin = new Point();
		htmlImagDisp.checkScrolls();
		// Can I get a reasonable value for scrollbarWidth before calling
		// checkScrolls()
		// and in a more reasonable way?
		// Now its ugly: I paint it, calculate the scrollbars and then resize
		// the panel...
		// Better: Now I only redo it when the scrollbar width changed, which is
		// not the case
		// normally.
		int scrollbarWidth = sbplog.vbar.getRect().width;
		if (scrollbarWidth != lastScrollbarWidth) {
			lastScrollbarWidth = scrollbarWidth;
			logs.resizeTo(htmlTxtImage.getWidth() - scrollbarWidth,
					htmlTxtImage.getHeight());
			logs.doPaint(draw, new Rect(0, 0, htmlTxtImage.getWidth(),
					htmlTxtImage.getHeight()));
		}
		htmlImagDisp.repaintNow();
		repaintNow();
		Vm.showWait(false);
	}

	/**
	 * Method that handles user input on this panel. It handles decryption of
	 * hints and navigation through the logs (always 5 at a time). Navigation of
	 * logs is required for performance reasons on the pocketpc.
	 */
	public void onEvent(Event ev) {
		if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
			int minLogs = java.lang.Math.min(Global.getPref().logsPerPage,
					currCache.getCacheLogs().size());
			if (ev.target == moreBt) {
				prevBt.modify(0, ControlConstants.Disabled);
				prevBt.repaintNow();
				crntLogPosition += minLogs;
				if (crntLogPosition >= currCache.getCacheLogs().size()) {
					// crntLogPosition = cache.CacheLogs.size()-5;
					crntLogPosition = currCache.getCacheLogs().size() - minLogs;
					moreBt.modify(ControlConstants.Disabled, 0);
					moreBt.repaintNow();
				}
				setLogs(crntLogPosition);
			} // = moreBt
			if (ev.target == prevBt) {
				moreBt.modify(0, ControlConstants.Disabled);
				moreBt.repaintNow();
				crntLogPosition -= minLogs;
				if (crntLogPosition <= 0) {
					prevBt.modify(ControlConstants.Disabled, 0);
					prevBt.repaintNow();
					crntLogPosition = 0;
				}
				setLogs(crntLogPosition);
			}
			if (ev.target == decodeButton) {
				if (!hintIsDecoded)
					decodeHintText();
				else
					resetHintText();
			}
		}
	}

	private void decodeHintText() {
		if (hint.getText().length() > 0) {
			Object selection = hint.getSelection();
			if (selection != null)
				hint.replaceSelection(Rot13.encodeRot13(selection.toString()));
			else
				hint.setText(Rot13.encodeRot13(hint.getText()));
			decodeButton.setText(this.encodeCaption);
			hintIsDecoded = true;
		}
	}

	private void resetHintText() {
		if (!currCache.getHints().equals("null"))
			hint.setText(this.currCache.getHints().replace("<br>", "\n"));
		else
			hint.setText("");
		decodeButton.setText(this.decodeCaption);
		hintIsDecoded = false;
	}

}

class fastScrollText extends InteractivePanel { // TODO extend this class in a
	// way that text can be marked
	// and copied
	public boolean scrollVertical = true;
	public boolean scrollHorizontal = false;

	public boolean imageNotDragged(ImageDragContext drag, Point where) {
		if (drag == null || drag.image == null)
			return super.imageNotDragged(drag, where);
		getDim(null);
		if (drag.image.location.y <= 0) {
			drag.image.move(0, drag.image.location.y);
		} else {
			drag.image.move(0, 0);
		}
		return super.imageNotDragged(drag, where);
	}

	// I copied it here because the original has a bug when scrolling
	// added the support for scrolling / draggin only vertically
	// rewrite to support for images bigger than the canvas
	// the return value is never used
	// ============================================================
	public boolean imageDragged(ImageDragContext dc, Point where)
	// ============================================================
	{
		dc.curPoint = new Point(where.x, where.y);
		AniImage moving = dc.image;
		Rect r = getDim(null);
		boolean didAutoScroll = false;
		Point to = new Point(where.x - dc.start.x, where.y - dc.start.y);
		if (!scrollHorizontal)
			to.x = 0;
		if (!scrollVertical)
			to.y = 0;
		// if (origin.y - to.y < 0 || origin.y - to.y + r.height >
		// moving.location.height) return true;
		if (moving == null) { // this is not used only copied
			if (!dragBackground)
				return true;
			int dx = dc.start.x - where.x, dy = dc.start.y - where.y;
			if (where.x < origin.x || where.x >= origin.x + r.width
					|| where.y < origin.y || where.y >= origin.y + r.height
					&& autoScrolling) {
				if (where.x <= origin.x)
					dx = scrollStep;
				if (where.x >= origin.x + r.width)
					dx = -scrollStep;
				if (where.y <= origin.y)
					dy = scrollStep;
				if (where.y >= origin.y + r.height)
					dy = -scrollStep; // here +/- is wrong in
				// InteractivePanel.java
				dc.start.x = where.x;
				dc.start.y = where.y;
			}
			// dc.start.move(where.x,where.y);
			if (dx != 0 || dy != 0)
				scroll(dx, dy);
			refresh();
			return true;
		} else if (autoScrolling) {
			didAutoScroll = true;
			scroll(-to.x, -to.y);
		}
		/*
		 * if (moving.canGo(to)) { moving.move(to.x,to.y); draggingImage(dc); if
		 * (ri != null) ri.dragEvent(this,ri.Drag,dc); }
		 */checkTouching(dc, false);
		if (didAutoScroll)
			refresh();
		else
			refresh(dc.image, null);// updateImage(dc.image);
		return (true);
	}

	public void onKeyEvent(KeyEvent ev) {
		if (ev.type == KeyEvent.KEY_PRESS) {
			if (ev.key == IKeys.DOWN) {
				doScroll(IScroll.Vertical, IScroll.ScrollHigher, 1);
				refresh();
			}
			if (ev.key == IKeys.UP) {
				doScroll(IScroll.Vertical, IScroll.ScrollLower, 1);
				refresh();
			}
			if (ev.key == IKeys.PAGE_DOWN) {
				doScroll(IScroll.Vertical, IScroll.PageHigher, 1);
				refresh();
			}
			if (ev.key == IKeys.PAGE_UP) {
				doScroll(IScroll.Vertical, IScroll.PageLower, 1);
				refresh();
			}
		}
	}

}
