package de.cachehound.gui.filter.nodes;

import de.cachehound.filter.IFilter;
import de.cachehound.filter.OrFilter;

public class OrFilterTreeNode extends ListFilterTreeNode {
	@Override
	public IFilter getFilter() {
		return new OrFilter(getChildFilter());
	}
	
	@Override
	public String toString() {
		return "OR";
	}
}
