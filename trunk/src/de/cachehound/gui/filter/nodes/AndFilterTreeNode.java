package de.cachehound.gui.filter.nodes;

import de.cachehound.filter.AndFilter;
import de.cachehound.filter.IFilter;

public class AndFilterTreeNode extends ListFilterTreeNode {
	@Override
	public IFilter getFilter() {
		return new AndFilter(getChildFilter());
	}

	@Override
	public String toString() {
		return "AND";
	}
}
