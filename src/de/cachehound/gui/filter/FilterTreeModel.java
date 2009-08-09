package de.cachehound.gui.filter;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import de.cachehound.filter.IFilter;
import de.cachehound.filter.ListFilter;
import de.cachehound.filter.SimpleFilter;
import de.cachehound.gui.util.TreeModelSupport;

public class FilterTreeModel extends TreeModelSupport implements TreeModel {
	private IFilter filter;
	
	public FilterTreeModel(IFilter filter) {
		super();
		this.filter = filter;
	}

	@Override
	public IFilter getRoot() {
		return filter;
	}

	@Override
	public IFilter getChild(Object parent, int index) {
		return ((ListFilter)parent).get(index);
	}

	@Override
	public int getChildCount(Object parent) {
		return ((ListFilter)parent).size();
	}

	@Override
	public int getIndexOfChild(Object parent, Object child) {
		return ((ListFilter)parent).indexOf(child);
	}

	@Override
	public boolean isLeaf(Object node) {
		if (node instanceof ListFilter) {
			return false;
		} else if (node instanceof SimpleFilter) {
			return true;
		} else {
			throw new ClassCastException("node is neither a ListFilter nor a SimpleFilter");
		}
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		// TODO Auto-generated method stub

	}


}
