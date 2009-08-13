package de.cachehound.gui.filter;

import javax.swing.JPanel;

import de.cachehound.filter.IFilter;

public abstract class AbstractFilterPanel<T extends IFilter> extends
		JPanel implements IFilterEditor<T> {

	public abstract T getFilter();

	public abstract void setState(T old);
}
