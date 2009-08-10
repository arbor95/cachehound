package de.cachehound.gui.filter;

import javax.swing.event.TreeModelEvent;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import de.cachehound.filter.AndFilter;
import de.cachehound.filter.IFilter;
import de.cachehound.filter.ListFilter;
import de.cachehound.filter.SimpleFilter;
import de.cachehound.filter.TrivialFilter;
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
		return ((ListFilter) parent).get(index);
	}

	@Override
	public int getChildCount(Object parent) {
		return ((ListFilter) parent).size();
	}

	@Override
	public int getIndexOfChild(Object parent, Object child) {
		return ((ListFilter) parent).indexOf(child);
	}

	@Override
	public boolean isLeaf(Object node) {
		if (node instanceof ListFilter) {
			return false;
		} else if (node instanceof SimpleFilter) {
			return true;
		} else {
			throw new ClassCastException(
					"node is neither a ListFilter nor a SimpleFilter");
		}
	}

	public void addFilter(TreePath path, IFilter filter) {
		if (isLeaf(path.getLastPathComponent()) && path.getPathCount() == 1) {
			IFilter oldRoot = getRoot();
			
			AndFilter newRoot = new AndFilter();
			newRoot.add(oldRoot);
			newRoot.add(filter);

			this.filter = newRoot;
		} else {
			if (isLeaf(path.getLastPathComponent())) {
				path = path.getParentPath();
			}

			ListFilter parent = (ListFilter) path.getLastPathComponent();

			parent.add(filter);
		}

		// Beim einfuegen eines Kindes wird auch der Eltern-Node veraendert.
		// Also muessen wir immer den kompletten Baum invalidieren.
		// FIXME: Irgendwie muss das auch anders gehen...
		fireTreeStructureChanged(new TreeModelEvent(this,
				new Object[] { this.filter }));
	}

	public void replaceFilter(TreePath path, IFilter filter) {
		if (path.getPathCount() == 1) {
			this.filter = filter;
		} else {
			ListFilter parent = (ListFilter) path.getParentPath()
					.getLastPathComponent();

			parent.set(parent.indexOf(path.getLastPathComponent()), filter);
		}

		// FIXME: siehe oben
		fireTreeStructureChanged(new TreeModelEvent(this,
				new Object[] { this.filter }));
	}

	public void deleteFilter(TreePath path) {
		if (path.getPathCount() == 1) {
			this.filter = new TrivialFilter(true);
		} else {
			ListFilter parent = (ListFilter) path.getParentPath()
					.getLastPathComponent();

			parent.remove(path.getLastPathComponent());
		}

		// FIXME: siehe oben
		fireTreeStructureChanged(new TreeModelEvent(this,
				new Object[] { this.filter }));
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		// TODO Auto-generated method stub

	}

}
