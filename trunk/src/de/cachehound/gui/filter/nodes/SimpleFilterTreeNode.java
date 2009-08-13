package de.cachehound.gui.filter.nodes;

import de.cachehound.filter.IFilter;
import de.cachehound.filter.SimpleFilter;

public class SimpleFilterTreeNode extends AbstractFilterTreeNode {
	private SimpleFilter f;
	
	SimpleFilterTreeNode(SimpleFilter f) {
		this.f = f;
	}
	
	@Override
	public boolean isLeaf() {
		return true;
	}

	public void setFilter(SimpleFilter f) {
		this.f = f;
	}

	@Override
	public IFilter getFilter() {
		return f;
	}
	
	public String toString() {
		return f.toString();
	}
}
