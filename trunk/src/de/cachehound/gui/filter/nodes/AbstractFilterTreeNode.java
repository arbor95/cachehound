package de.cachehound.gui.filter.nodes;

import javax.swing.tree.DefaultMutableTreeNode;

import de.cachehound.filter.IFilter;

public abstract class AbstractFilterTreeNode extends DefaultMutableTreeNode {
	public abstract IFilter getFilter();
}
