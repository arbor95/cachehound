package CacheWolf.gui;

import java.io.File;
import java.io.FileReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import CacheWolf.beans.Preferences;
import CacheWolf.util.MyLocale;
import de.cachehound.imp.mail.CacheWolfMailHandler;
import de.cachehound.util.AllReader;
import ewe.ui.CellConstants;
import ewe.ui.ControlEvent;
import ewe.ui.Event;
import ewe.ui.Form;
import ewe.ui.HtmlDisplay;
import ewe.ui.ScrollBarPanel;
import ewe.ui.ScrollablePanel;
import ewe.ui.mButton;

/**
 * This class displays an information screen. It loads the html text to display
 * from a file that is given upon creation of this class. It offers a cancel
 * button enabling the user to close the screen and return to wherever the user
 * was before Class ID = 3000
 */
public class InfoScreen extends Form {

	private static Logger logger = LoggerFactory
			.getLogger(CacheWolfMailHandler.class);

	private HtmlDisplay disp = new HtmlDisplay();
	private mButton closeButton;

	/**
	 * Shows the given html-Fil on the screen
	 * 
	 * @param text
	 *            The (html-formated) File to show
	 * @param title
	 *            The WindowTitle
	 * @param pref
	 *            The Preferences of CacheWolf
	 */
	public InfoScreen(File file, String title, Preferences pref) {
		String myText;
		try {
			AllReader in = new AllReader(new FileReader(file));
			myText = in.readAll();
			in.close();
		} catch (Exception ex) {
			logger.error("Can't open " + file.getAbsolutePath()
					+ " in InfoScreen.", ex);
			myText = "Failure at opening " + file.getAbsolutePath()
					+ " for this InfoScreen";
		}
		buildWindow(myText, title, pref);
	}

	/**
	 * Shows the given String on the screen
	 * 
	 * @param text
	 *            The (html-formated) Text to show
	 * @param title
	 *            The WindowTitle
	 * @param pref
	 *            The Preferences of CacheWolf
	 */
	public InfoScreen(String text, String title, Preferences prefs) {
		buildWindow(text, title, prefs);
	}

	/**
	 * Shows the given String on the screen
	 * 
	 * @param text
	 *            The (html-formated) Text to show
	 * @param title
	 *            The WindowTitle
	 * @param pref
	 *            The Preferences of CacheWolf
	 */
	private void buildWindow(String text, String title, Preferences pref) {
		this.setTitle(title);
		this.setPreferredSize(pref.myAppWidth, pref.myAppHeight);
		disp.setHtml(text);
		ScrollBarPanel sbp = new MyScrollBarPanel(disp,
				ScrollablePanel.NeverShowHorizontalScrollers);
		this.addLast(sbp);
		this.addLast(closeButton = new mButton(MyLocale.getMsg(3000, "Close")),
				CellConstants.DONTSTRETCH, CellConstants.FILL);
	}

	/**
	 * Closes the Window when the user clicks on the Close Button.
	 */
	public void onEvent(Event ev) {
		if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
			if (ev.target == closeButton) {
				this.close(0);
			}
		}
	}
}
