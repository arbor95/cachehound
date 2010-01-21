package de.cachehound.gui.interfaces;

import java.util.LinkedList;
import java.util.List;

public class AbstractProgressTask implements IProgressTask {

	private List<ITaskListener> listeners;
	private String headLine;
	private String text;
	private double progress;

	public AbstractProgressTask() {
		listeners = new LinkedList<ITaskListener>();
		headLine = "";
		text = "";
		progress = 0;
	}

	@Override
	public void addTaskListener(ITaskListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeTaskListener(ITaskListener listener) {
		listeners.remove(listener);
	}

	protected void setHeadLine(String headLine) {
		this.headLine = headLine;
		for (ITaskListener listener : listeners) {
			listener.updateHeadLine(headLine);
		}
	}

	protected void setText(String text) {
		this.text = text;
		for (ITaskListener listener : listeners) {
			listener.updateText(text);
		}
	}

	protected void setProgress(double progress) {
		this.progress = progress;
		for (ITaskListener listener : listeners) {
			listener.updateProgress(progress);
		}
	}

	@Override
	public String getHeadLine() {
		return headLine;
	}

	@Override
	public double getProgress() {
		return progress;
	}

	@Override
	public String getText() {
		return text;
	}

}
