package de.cachehound.gui.filter.nodes;

import de.cachehound.filter.IFilter;
import de.cachehound.filter.NotFilter;

public class NotFilterTreeNode extends AbstractFilterTreeNode {
	@Override
	public boolean isLeaf() {
		return false;
	}

	@Override
	public IFilter getFilter() {
		return new NotFilter(((AbstractFilterTreeNode) this.getFirstChild())
				.getFilter());
	}

	public String toString() {
		return "NOT";
	}
}
