package de.cachehound.gui.interfaces;

public interface ITaskListener {

	public void updateHeadLine(String headLine);
	
	public void updateText(String statusText);
	
	/**
	 * 
	 * @param progress between 0 and 1 (0 ... 100%)
	 */
	public void updateProgress(double progress);
	
}
