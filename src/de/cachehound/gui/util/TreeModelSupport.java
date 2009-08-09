package de.cachehound.gui.util;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

public class TreeModelSupport {
	private List<TreeModelListener> listeners = new ArrayList<TreeModelListener>();

	public void addTreeModelListener(TreeModelListener l) {
		listeners.add(l);
	}

	public void removeTreeModelListener(TreeModelListener l) {
		listeners.remove(l);
	}

	public void fireTreeNodesChanged(TreeModelEvent e) {
		for (TreeModelListener listener : listeners) {
			listener.treeNodesChanged(e);
		}
	}

	public void fireTreeNodesInserted(TreeModelEvent e) {
		for (TreeModelListener listener : listeners) {
			listener.treeNodesInserted(e);
		}
	}

	public void fireTreeNodesRemoved(TreeModelEvent e) {
		for (TreeModelListener listener : listeners) {
			listener.treeNodesRemoved(e);
		}
	}

	public void fireTreeStructureChanged(TreeModelEvent e) {
		for (TreeModelListener listener : listeners) {
			listener.treeStructureChanged(e);
		}
	}
}
