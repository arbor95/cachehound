package de.cachehound.gui.filter;

import javax.swing.event.TreeModelEvent;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import de.cachehound.filter.AndFilter;
import de.cachehound.filter.IFilter;
import de.cachehound.filter.ListFilter;
import de.cachehound.filter.SimpleFilter;
import de.cachehound.filter.TrivialFilter;
import de.cachehound.gui.util.IdentityProxy;
import de.cachehound.gui.util.TreeModelSupport;

public class FilterTreeModel extends TreeModelSupport implements TreeModel {
	private IFilter filter;

	public FilterTreeModel(IFilter filter) {
		super();
		this.filter = filter;
	}

	@Override
	public IdentityProxy<IFilter> getRoot() {
		return new IdentityProxy<IFilter>(filter);
	}

	@SuppressWarnings("unchecked")
	public static IFilter object2IFilter(Object o) {
		return ((IdentityProxy<IFilter>) o).get();
	}

	@Override
	public IdentityProxy<IFilter> getChild(Object parent, int index) {
		IFilter parentFilter = object2IFilter(parent);
		IFilter childFilter = ((ListFilter) parentFilter).get(index);
		return new IdentityProxy<IFilter>(childFilter);
	}

	@Override
	public int getChildCount(Object parent) {
		if (isLeaf(parent)) {
			return 0;
		} else {
			IFilter parentFilter = object2IFilter(parent);
			return ((ListFilter) parentFilter).size();
		}
	}

	@Override
	public int getIndexOfChild(Object parent, Object child) {
		IFilter parentFilter = object2IFilter(parent);
		IFilter childFilter = object2IFilter(child);
		return ((ListFilter) parentFilter).indexOf(childFilter);
	}

	@Override
	public boolean isLeaf(Object node) {
		IFilter nodeFilter = object2IFilter(node);
		if (nodeFilter instanceof ListFilter) {
			return false;
		} else if (nodeFilter instanceof SimpleFilter) {
			return true;
		} else {
			throw new ClassCastException(
					"node is neither a ListFilter nor a SimpleFilter");
		}
	}

	public void addFilter(TreePath path, IFilter filter) {
		if (!isLeaf(path.getLastPathComponent())) {
			ListFilter parentFilter = (ListFilter) object2IFilter(path
					.getLastPathComponent());
			parentFilter.add(filter);
			fireTreeNodesInserted(new TreeModelEvent(this, path,
					new int[] { parentFilter.size() - 1 },
					new Object[] { new IdentityProxy<IFilter>(filter) }));
		} else if (path.getPathCount() > 1) {
			ListFilter parentFilter = (ListFilter) object2IFilter(path
					.getParentPath().getLastPathComponent());
			parentFilter.add(filter);
			fireTreeNodesInserted(new TreeModelEvent(this,
					path.getParentPath(),
					new int[] { parentFilter.size() - 1 },
					new Object[] { new IdentityProxy<IFilter>(filter) }));
		} else {
			IFilter oldRoot = object2IFilter(getRoot());
			AndFilter newRoot = new AndFilter();
			newRoot.add(oldRoot);
			newRoot.add(filter);
			this.filter = newRoot;
			fireTreeStructureChanged(new TreeModelEvent(this,
					new Object[] { this.filter }));
		}
	}

	public void replaceFilter(TreePath path, IFilter filter) {
		if (path.getPathCount() > 1) {
			IFilter oldFilter = object2IFilter(path.getLastPathComponent());
			ListFilter parentFilter = (ListFilter) object2IFilter(path
					.getParentPath().getLastPathComponent());
			int index = parentFilter.indexOf(oldFilter);
			parentFilter.set(index, filter);
			fireTreeNodesChanged(new TreeModelEvent(this, path.getParentPath(),
					new int[] { index },
					new Object[] { new IdentityProxy<IFilter>(filter) }));
		} else {
			this.filter = filter;
			fireTreeStructureChanged(new TreeModelEvent(this,
					new Object[] { this.filter }));
		}
	}

	public void deleteFilter(TreePath path) {
		if (path.getPathCount() > 1) {
			IFilter filter = object2IFilter(path.getLastPathComponent());
			ListFilter parentFilter = (ListFilter) object2IFilter(path
					.getParentPath().getLastPathComponent());
			int index = parentFilter.indexOf(filter);
			parentFilter.remove(index);
			fireTreeNodesRemoved(new TreeModelEvent(this, path.getParentPath(),
					new int[] { index },
					new Object[] { new IdentityProxy<IFilter>(filter) }));
		} else {
			this.filter = new TrivialFilter(true);
			fireTreeStructureChanged(new TreeModelEvent(this,
					new Object[] { this.filter }));
		}
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		// TODO Auto-generated method stub

	}

}
