package CacheWolf.gui;

import CacheWolf.Global;
import CacheWolf.beans.CacheHolder;
import CacheWolf.beans.CacheImages;
import CacheWolf.util.MyLocale;

import com.stevesoft.ewe_pat.Regex;

import de.cachehound.beans.ICacheHolderDetail;
import ewe.fx.Font;
import ewe.sys.Vm;
import ewe.ui.CellConstants;
import ewe.ui.CellPanel;
import ewe.ui.ControlEvent;
import ewe.ui.Event;
import ewe.ui.HtmlDisplay;
import ewe.ui.ScrollBarPanel;
import ewe.ui.mButton;

/**
 * This class shows the long description on a cache. Test with GC1CC5T - Final
 * GC19DDX -
 */
public class DescriptionPanel extends CellPanel {
	HtmlDisplay disp = new HtmlDisplay();
	mButton btnPlus, btnMinus;
	CacheHolder currCache;

	CellPanel buttonP = new CellPanel();
	CellPanel descP = new CellPanel();

	private String desc;

	public DescriptionPanel() {
		buttonP.addNext(btnPlus = new mButton("+"), CellConstants.HSTRETCH,
				(CellConstants.HFILL));
		buttonP.addLast(btnMinus = new mButton("-"), CellConstants.HSTRETCH,
				(CellConstants.HFILL));
		ScrollBarPanel sbp = new MyScrollBarPanel(disp, 0);
		// sbp.setScrollBarSize(40,40, 20);
		descP.addLast(sbp);
		this.addLast(descP);
		this.addLast(buttonP, CellConstants.HSTRETCH, CellConstants.HFILL);
		clear();
	}

	/**
	 * Set the text to display. Text should be HTML formated.
	 */
	// String description = null;
	public void setText(CacheHolder cache) {
		boolean isHtml;
		if (currCache == cache)
			return;
		int scrollto = 0;
		if (cache == null) {
			desc = "";
			isHtml = false;
		} else {
			if (cache.hasSameMainCache(currCache))
				scrollto = disp.getTopLine();
			isHtml = cache.is_HTML();
			if (cache.isAddiWpt()) {
				isHtml = cache.getMainCache().is_HTML();
				if (cache.getCacheDetails(false, true).getLongDescription() != null
						&& cache.getCacheDetails(false, true).getLongDescription()
								.length() > 0)
					desc = cache.getCacheDetails(false, true).getLongDescription()
							+ (isHtml ? "<hr>\n" : "\n")
							+ cache.getMainCache().getCacheDetails(false, true)
									.getLongDescription();
				else
					desc = cache.getMainCache().getCacheDetails(false, true)
							.getLongDescription();
			} else
				// not an addi-wpt
				desc = cache.getCacheDetails(false, true).getLongDescription();
		}
		// HtmlDisplay does not show the <sup> tag correctly, so we need to
		// replace with ^
		if (desc.indexOf("<sup>") >= 0) {
			desc = desc.replace("<sup>", "^(");
			desc = desc.replace("</sup>", ")");
		}
		Vm.showWait(true);
		if (cache != null && isHtml) {
			int imageNo = 0;
			if (Global.getPref().descShowImg) {
				CacheImages Images;
				CacheHolder chImages; // cache which supplies the images
				// (could
				// be main cache)
				if (cache.isAddiWpt()) {
					chImages = cache.getMainCache();
				} else {
					chImages = cache;
				}
				Images = chImages.getCacheDetails(false, true).getImages();
				StringBuilder s = new StringBuilder(desc.length()
						+ Images.size() * 100);
				int start = 0;
				int pos;
				Regex imgRex = new Regex(
						"src=(?:\\s*[^\"|']*?)(?:\"|')(.*?)(?:\"|')");
				if (Images.size() > 0) {
					while (start >= 0
							&& (pos = desc.indexOf("<img", start)) > 0) {
						s.append(desc.substring(start, pos));
						imgRex.searchFrom(desc, pos);
						String imgUrl = imgRex.stringMatched(1);
						if (imgUrl == null)
							break; // Remaining pictures are from image span
						// Vm.debug("imgUrl "+imgUrl);
						if (imgUrl.lastIndexOf('.') > 0
								&& imgUrl.toLowerCase().startsWith("http")) {
							String imgType = (imgUrl.substring(
									imgUrl.lastIndexOf(".")).toLowerCase() + "    ")
									.substring(0, 4).trim();
							// If we have an image which we stored when
							// spidering, we can display it
							if (imgType.startsWith(".png")
									|| imgType.startsWith(".jpg")
									|| imgType.startsWith(".gif")) {
								s.append("<img src=\""
										+
										// Global.getProfile().dataDir+
										Images.get(imageNo).getFilename()
										+ "\">");
								imageNo++;
							}
						}
						start = desc.indexOf(">", pos);
						if (start >= 0)
							start++;
						if (imageNo >= Images.size())
							break;
					}
				}
				if (start >= 0)
					s.append(desc.substring(start));
				desc = s.toString();
				if (imageNo < Images.size()) {
					desc += getPicDesc(imageNo, chImages.getCacheDetails(false, true));
				}
			}
			// disp.setHtml(desc);
			disp.startHtml();
			disp.getDecoderProperties().set("documentroot",
					Global.getProfile().getDataDir());
			disp.addHtml(desc, new ewe.sys.Handle());
			disp.endHtml();

		} else {
			disp.startHtml(); // To clear the old HTML display
			disp.endHtml();
			disp.setPlainText(desc);
		}
		disp.scrollTo(scrollto, false);
		// description = desc;
		Vm.showWait(false);
		// }
		currCache = cache;
	}

	/**
	 * Get the descriptions for the pictures (if they exist)
	 * 
	 * @param imagesShown
	 *            images already shown as part of long description (don't show
	 *            again)
	 * @param chD
	 */
	private String getPicDesc(int imagesShown, ICacheHolderDetail chD) {
		StringBuilder sb = new StringBuilder(1000);
		sb.append("<hr><font size=\"+1\" color=\"red\">").append(
				MyLocale.getMsg(202, "IMAGES").toUpperCase()).append("</font>");
		sb.append("<br><br>");
		for (int i = imagesShown; i < chD.getImages().size(); i++) {
			sb.append(chD.getImages().get(i).getTitle()).append("<br>");
			// Show the additional text if there is one
			if (chD.getImages().get(i).getComment() != null)
				sb.append("<font color='blue'>").append(
						chD.getImages().get(i).getComment()).append("</font>");
			// Only show the image if images are enabled
			if (Global.getPref().descShowImg)
				sb.append("<img src=\"" + chD.getImages().get(i).getFilename()
						+ "\"><br>");
			sb.append("<br><br><hr>");
		}
		return sb.toString();
	}

	public void clear() {
		disp.setPlainText("loading ...");
		currCache = null;
	}

	/**
	 * Eventhandler
	 */
	public void onEvent(Event ev) {

		if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
			if (ev.target == btnPlus) {
				Font currFont = disp.getFont();
				currFont = currFont.changeNameAndSize(null,
						currFont.getSize() + 2);
				disp.setFont(currFont);
				disp.displayPropertiesChanged();
				// redraw();
			}

			if (ev.target == btnMinus) {
				Font currFont = disp.getFont();
				currFont = currFont.changeNameAndSize(null,
						currFont.getSize() - 2);
				disp.setFont(currFont);
				disp.displayPropertiesChanged();
				// redraw();
			}
		}
		super.onEvent(ev);
	}

}
