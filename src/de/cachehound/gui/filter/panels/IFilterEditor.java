package de.cachehound.gui.filter.panels;

import de.cachehound.filter.IFilter;
import de.cachehound.gui.filter.IFilterChangedListener;

public interface IFilterEditor<T extends IFilter> {
	public T getFilter();

	public void setState(T old);

	public boolean canHandle(IFilter f);

	public void addFilterChangedListener(IFilterChangedListener l);

	public void removeFilterChangedListener(IFilterChangedListener l);
}
