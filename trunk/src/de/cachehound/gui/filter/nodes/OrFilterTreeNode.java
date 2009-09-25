package de.cachehound.gui.filter.nodes;

import java.util.Enumeration;

import de.cachehound.filter.IFilter;
import de.cachehound.filter.OrFilter;

public class OrFilterTreeNode extends AbstractFilterTreeNode {
	@Override
	public boolean isLeaf() {
		return false;
	}

	@Override
	public IFilter getFilter() {
		OrFilter ret = new OrFilter();
		for (Enumeration<?> e = children(); e.hasMoreElements();) {
			ret.add(((AbstractFilterTreeNode) e.nextElement())
					.getFilter());
		}
		return ret;
	}
	
	@Override
	public String toString() {
		return "OR";
	}
}
