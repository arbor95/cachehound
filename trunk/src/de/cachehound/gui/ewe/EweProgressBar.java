package de.cachehound.gui.ewe;

import de.cachehound.gui.interfaces.IProgressBar;
import de.cachehound.gui.interfaces.IProgressTask;
import de.cachehound.gui.interfaces.ITaskListener;
import CacheWolf.gui.InfoBox;

public class EweProgressBar implements ITaskListener, IProgressBar {

	private IProgressTask task;
	private InfoBox infoBox;

	public void show() {
		if (infoBox != null) {
			infoBox.close(0);
		}
		infoBox = new InfoBox(task.getHeadLine(), task.getText() + "\n\n" + (int) (task.getProgress() * 100) + " %", 4);
		infoBox.exec();
		infoBox.waitUntilPainted(100);
	}

	public void close() {
		if (infoBox != null) {
			infoBox.close(0);
		}
		task.removeTaskListener(this);
	}
	
	@Override
	public void updateHeadLine(String headLine) {
		infoBox.setTitle(headLine);
	}

	@Override
	public void updateProgress(double progress) {
		infoBox.setInfo(task.getText() + "\n\n" + (int) (progress * 100) + " %");
	}

	@Override
	public void updateText(String statusText) {
		infoBox.setInfo(statusText + "\n\n" + (int) (task.getProgress() * 100) + " %");
	}

	@Override
	public void setProgressTask(IProgressTask task) {
		if (task != null) {
			task.removeTaskListener(this);
		}
		this.task = task;
		task.addTaskListener(this);
	}	
	
}
