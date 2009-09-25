package de.cachehound.gui.filter.nodes;

import java.util.Enumeration;

import de.cachehound.filter.AndFilter;
import de.cachehound.filter.IFilter;

public class AndFilterTreeNode extends AbstractFilterTreeNode {
	@Override
	public boolean isLeaf() {
		return false;
	}

	@Override
	public IFilter getFilter() {
		AndFilter ret = new AndFilter();
		for (Enumeration<?> e = children(); e.hasMoreElements();) {
			ret.add(((AbstractFilterTreeNode) e.nextElement())
					.getFilter());
		}
		return ret;
	}

	@Override
	public String toString() {
		return "AND";
	}
}
