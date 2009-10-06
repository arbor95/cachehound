package de.cachehound.gui.filter.panels;

import de.cachehound.filter.IFilter;

public class DummyFilterPanel extends AbstractFilterPanel<IFilter> {
	private IFilter filter;

	@Override
	public IFilter getFilter() {
		return filter;
	}

	@Override
	public void setState(IFilter old) {
		if (old != null) {
			filter = old;
		}
	}

	@Override
	public boolean canHandle(IFilter f) {
		return true;
	}
}
