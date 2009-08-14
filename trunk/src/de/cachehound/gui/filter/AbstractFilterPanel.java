package de.cachehound.gui.filter;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.cachehound.filter.IFilter;

public abstract class AbstractFilterPanel<T extends IFilter> extends JPanel
		implements IFilterEditor<T> {

	private static Logger logger = LoggerFactory
			.getLogger(AbstractFilterPanel.class);

	private List<IFilterChangedListener> listeners = new ArrayList<IFilterChangedListener>();

	public abstract T getFilter();

	public abstract void setState(T old);

	public void addFilterChangedListener(IFilterChangedListener l) {
		listeners.add(l);
	}

	public void removeFilterChangedListener(IFilterChangedListener l) {
		listeners.remove(l);
	}

	public void notifyFilterChangedListeners() {
		for (IFilterChangedListener l : listeners) {
			l.filterChanged();
		}
		logger.debug("fired.");
	}
}
