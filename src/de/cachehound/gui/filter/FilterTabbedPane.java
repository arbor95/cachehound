package de.cachehound.gui.filter;

import java.awt.Component;

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
	
	@SuppressWarnings("unchecked")
	public void showFilter(IFilter filter) {
		for (Component c : getComponents()) {
			if (((IFilterEditor)c).canHandle(filter)) {
				((IFilterEditor)c).setState(filter);
				this.setSelectedComponent(c);
				break;
			}
		}
	}
}
