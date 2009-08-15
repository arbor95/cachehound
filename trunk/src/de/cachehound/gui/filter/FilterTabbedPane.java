package de.cachehound.gui.filter;

import java.awt.Component;

import javax.swing.JTabbedPane;

import de.cachehound.filter.IFilter;

public class FilterTabbedPane extends JTabbedPane {
	public FilterTabbedPane() {
		addTab("Bearing", new BearingFilterPanel());
		addTab("Distance", new DistanceFilterPanel());
		addTab("Size", new SizeFilterPanel());

		addTab("Dummy", new DummyFilterPanel());
	}

	public void addFilterChangedListener(IFilterChangedListener l) {
		for (Component c : getComponents()) {
			((IFilterEditor<?>) c).addFilterChangedListener(l);
		}
	}

	public IFilter getFilter() {
		return ((IFilterEditor<?>) getComponentAt(getSelectedIndex())).getFilter()
				.clone();
	}

	@SuppressWarnings("unchecked")
	public void showFilter(IFilter filter) {
		for (Component c : getComponents()) {
			if (((IFilterEditor) c).canHandle(filter)) {
				this.setSelectedComponent(c);
				((IFilterEditor) c).setState(filter);
				break;
			}
		}
	}
}
