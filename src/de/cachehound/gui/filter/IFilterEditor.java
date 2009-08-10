package de.cachehound.gui.filter;

import de.cachehound.filter.IFilter;

public interface IFilterEditor<T extends IFilter> {
	public T getFilter();
	public void setState(T old);
}
