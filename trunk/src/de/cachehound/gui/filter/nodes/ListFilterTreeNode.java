package de.cachehound.gui.filter.nodes;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import de.cachehound.filter.IFilter;

public abstract class ListFilterTreeNode extends AbstractFilterTreeNode {
	@Override
	public boolean isLeaf() {
		return false;
	}

	protected List<IFilter> getChildFilter() {
		List<IFilter> ret = new LinkedList<IFilter>();
		for (Enumeration<?> e = children(); e.hasMoreElements();) {
			ret.add(((AbstractFilterTreeNode) e.nextElement()).getFilter());
		}
		return ret;
	}
}
