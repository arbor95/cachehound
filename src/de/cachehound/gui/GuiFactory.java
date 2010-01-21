package de.cachehound.gui;

import de.cachehound.gui.ewe.EweProgressBar;
import de.cachehound.gui.interfaces.IProgressBar;
import de.cachehound.gui.interfaces.IProgressTask;

public class GuiFactory {

	private static GuiFactory instance = new GuiFactory();

	private GuiFactory() {
		// singleton
	}

	public static GuiFactory getInstance() {
		return instance;
	}

	public IProgressBar getProgressBar(IProgressTask task) {
		IProgressBar bar = new EweProgressBar();
		bar.setProgressTask(task);
		return bar;
	}

}
