package de.cachehound.gui.filter;

import de.cachehound.filter.IFilter;

public interface IFilterEditor<T extends IFilter> {
	public T getFilter();
	public void setState(T old);
	public boolean canHandle(IFilter f);
	
	public void addFilterChangedListener(IFilterChangedListener l);
	public void removeFilterChangedListener(IFilterChangedListener l);
}
