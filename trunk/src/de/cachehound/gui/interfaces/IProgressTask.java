package de.cachehound.gui.interfaces;

public interface IProgressTask {

	public void addTaskListener(ITaskListener listener);

	public void removeTaskListener(ITaskListener listener);

	public String getHeadLine();

	public String getText();

	public double getProgress();
}
