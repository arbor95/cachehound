package de.cachehound.gui.filter;

import javax.swing.JTabbedPane;

import de.cachehound.filter.IFilter;

public class FilterTabbedPane extends JTabbedPane {
	public FilterTabbedPane() {
		addTab("Bearing", new BearingFilterEditor());
		addTab("Size", new SizeFilterEditor());
	}
	
	@SuppressWarnings("unchecked")
	public IFilter getFilter() {
		return ((IFilterEditor)getComponentAt(getSelectedIndex())).getFilter().clone();
	}
}
