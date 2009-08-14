package de.cachehound.gui.filter;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

public class MyTreeModel extends DefaultTreeModel {
	public MyTreeModel(TreeNode root) {
		super(root);
	}

	public void replaceNode(MutableTreeNode oldNode, MutableTreeNode newNode) {
		if (oldNode.getParent() == null) {
			newNode.setParent(null);
			setRoot(newNode);
		} else {
			MutableTreeNode parent = (MutableTreeNode)oldNode.getParent();
			int index = parent.getIndex(oldNode);
			removeNodeFromParent(oldNode);
			insertNodeInto(newNode, parent, index);
		}
	}
}
