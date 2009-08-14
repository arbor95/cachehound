package de.cachehound.gui.filter;

import java.util.Enumeration;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import de.cachehound.gui.filter.nodes.AbstractFilterTreeNode;
import de.cachehound.gui.filter.nodes.NotFilterTreeNode;

public class TreePanel extends JPanel {
	private MyTreeModel model;
	private JTree tree;

	public TreePanel(AbstractFilterTreeNode node) {
		model = new MyTreeModel(node);
		tree = new JTree(model);

		// Alles aufklappen
		for (Enumeration<?> e = ((AbstractFilterTreeNode) model.getRoot())
				.depthFirstEnumeration(); e.hasMoreElements();) {
			tree.makeVisible(new TreePath(((DefaultMutableTreeNode) e
					.nextElement()).getPath()));
		}

		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setSelectionRow(0);

		this.add(tree);
	}

	public void addTreeSelectionListener(TreeSelectionListener l) {
		tree.addTreeSelectionListener(l);
	}

	/**
	 * This SHOULD always be true - but isn't (during some event handlers).
	 */
	public boolean isSomethingSelected() {
		return tree.getSelectionPath() != null;
	}
	
	public AbstractFilterTreeNode getCurrentSelection() {
		return (AbstractFilterTreeNode) tree.getSelectionPath()
				.getLastPathComponent();
	}

	public AbstractFilterTreeNode getRoot() {
		return (AbstractFilterTreeNode) model.getRoot();
	}

	public void addNode(AbstractFilterTreeNode newNode) {
		DefaultMutableTreeNode parent = getCurrentSelection();
		if (parent.isLeaf()) {
			parent = (DefaultMutableTreeNode) parent.getParent();
		}
		model.insertNodeInto(newNode, parent, parent.getChildCount());
		tree.setSelectionPath(new TreePath(newNode.getPath()));
	}

	public void replaceSelectionWithNode(AbstractFilterTreeNode newNode) {
		DefaultMutableTreeNode oldNode = (DefaultMutableTreeNode) tree
				.getSelectionPath().getLastPathComponent();
		model.replaceNode(oldNode, newNode);
		tree.setSelectionPath(new TreePath(newNode.getPath()));
	}

	public void deleteSelection() {
		if (tree.getSelectionPath().getPathCount() != 1) {
			DefaultMutableTreeNode oldNode = (DefaultMutableTreeNode) tree
					.getSelectionPath().getLastPathComponent();
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode) oldNode
					.getParent();
			model.removeNodeFromParent(oldNode);
			tree.setSelectionPath(new TreePath(parent.getPath()));
		}
	}

	private void cleanUpDoubleNots() {
		DefaultMutableTreeNode oldNode = (DefaultMutableTreeNode) tree
				.getSelectionPath().getLastPathComponent();

		if (oldNode instanceof NotFilterTreeNode) {
			oldNode = (DefaultMutableTreeNode) oldNode.getChildAt(0);
		}

		if (oldNode.getParent().getParent() instanceof NotFilterTreeNode) {
			model.replaceNode(
					(MutableTreeNode) oldNode.getParent().getParent(), oldNode);
		}

		tree.setSelectionPath(new TreePath(oldNode.getPath()));
		tree.makeVisible(new TreePath(oldNode.getPath()));
	}

	public void negateSelection() {
		DefaultMutableTreeNode oldNode = (DefaultMutableTreeNode) tree
				.getSelectionPath().getLastPathComponent();
		DefaultMutableTreeNode newNode = new NotFilterTreeNode();
		tree.setSelectionPath(new TreePath(getRoot()));
		model.replaceNode(oldNode, newNode);
		model.insertNodeInto(oldNode, newNode, 0);
		tree.setSelectionPath(new TreePath(oldNode.getPath()));
		tree.makeVisible(new TreePath(oldNode.getPath()));

		cleanUpDoubleNots();

	}
}
