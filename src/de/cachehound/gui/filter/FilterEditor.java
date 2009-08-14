package de.cachehound.gui.filter;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.EnumSet;
import java.util.Enumeration;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import de.cachehound.filter.AndFilter;
import de.cachehound.filter.BearingFilter;
import de.cachehound.filter.DistanceFilter;
import de.cachehound.filter.IFilter;
import de.cachehound.filter.ListFilter;
import de.cachehound.filter.NotFilter;
import de.cachehound.filter.OrFilter;
import de.cachehound.filter.SizeFilter;
import de.cachehound.gui.filter.nodes.AbstractFilterTreeNode;
import de.cachehound.gui.filter.nodes.FilterTreeNodeFactory;
import de.cachehound.gui.filter.nodes.NotFilterTreeNode;
import de.cachehound.types.Bearing;
import de.cachehound.types.CacheSize;

public class FilterEditor extends JDialog {
	/** A return status code - returned if Cancel button has been pressed */
	public static final int RET_CANCEL = 0;
	/** A return status code - returned if OK button has been pressed */
	public static final int RET_OK = 1;

	public FilterEditor(IFilter old, JFrame parent, boolean modal) {
		super(parent, modal);

		initComponents(old);
	}

	/** @return the return status of this dialog - one of RET_OK or RET_CANCEL */
	public int getReturnStatus() {
		return returnStatus;
	}

	public IFilter getFilter() {
		return ((AbstractFilterTreeNode) model.getRoot()).getFilter().clone();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 */
	private void initComponents(IFilter f) {
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				closeDialog();
			}
		});

		treePanel = createTreePanel(f);
		buttonPanel = createButtonPanel();
		detailsPanel = new FilterTabbedPane();
		detailsPanel.addFilterChangedListener(new IFilterChangedListener() {
			@Override
			public void filterChanged() {
				replaceButtonActionPerformed();
			}
		});

		this.setLayout(new BorderLayout());
		this.add(new JScrollPane(treePanel), BorderLayout.CENTER);
		this.add(detailsPanel, BorderLayout.EAST);
		this.add(buttonPanel, BorderLayout.SOUTH);

		treeSelectionChanged();

		pack();
	}

	private JPanel createTreePanel(IFilter f) {
		JPanel treePanel = new JPanel();

		model = new MyTreeModel((new FilterTreeNodeFactory()).doCreate(f));
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
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				treeSelectionChanged();
			}
		});

		treePanel.add(tree);

		return treePanel;
	}

	private JPanel createButtonPanel() {
		JPanel buttonPanel = new JPanel();

		JButton addButton = new JButton();
		addButton.setText("Add...");
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				addButtonActionPerformed();
			}
		});
		buttonPanel.add(addButton);

		JButton replaceButton = new JButton();
		replaceButton.setText("Replace...");
		replaceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				replaceButtonActionPerformed();
			}
		});
		buttonPanel.add(replaceButton);

		JButton deleteButton = new JButton();
		deleteButton.setText("Delete");
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				deleteButtonActionPerformed();
			}
		});
		buttonPanel.add(deleteButton);

		JButton negateButton = new JButton();
		negateButton.setText("Negate");
		negateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				negateButtonActionPerformed();
			}
		});
		buttonPanel.add(negateButton);

		JButton okButton = new JButton();
		okButton.setText("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				okButtonActionPerformed();
			}
		});
		buttonPanel.add(okButton);

		JButton cancelButton = new JButton();
		cancelButton.setText("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				cancelButtonActionPerformed();
			}
		});
		buttonPanel.add(cancelButton);

		return buttonPanel;
	}

	private IFilter getCurrentFilter() {
		return ((AbstractFilterTreeNode) tree.getSelectionPath()
				.getLastPathComponent()).getFilter();
	}

	private void treeSelectionChanged() {
		if (tree.getSelectionPath() != null) {
			detailsPanel.showFilter(getCurrentFilter());
		}
	}

	private void addButtonActionPerformed() {
		if (tree.getSelectionPath() != null) {
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode) tree
					.getSelectionPath().getLastPathComponent();
			if (parent.isLeaf()) {
				parent = (DefaultMutableTreeNode) parent.getParent();
			}
			DefaultMutableTreeNode newNode = (new FilterTreeNodeFactory())
					.doCreate(detailsPanel.getFilter());
			model.insertNodeInto(newNode, parent, parent.getChildCount());
			tree.setSelectionPath(new TreePath(newNode.getPath()));
		}
	}

	private void replaceButtonActionPerformed() {
		if (tree.getSelectionPath() != null) {
			DefaultMutableTreeNode newNode = (new FilterTreeNodeFactory())
					.doCreate(detailsPanel.getFilter());
			if (tree.getSelectionPath().getPathCount() == 1) {
				model.setRoot(newNode);
			} else {
				DefaultMutableTreeNode oldNode = (DefaultMutableTreeNode) tree
						.getSelectionPath().getLastPathComponent();
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode) oldNode
						.getParent();
				int index = model.getIndexOfChild(parent, oldNode);
				model.removeNodeFromParent(oldNode);
				model.insertNodeInto(newNode, parent, index);
				tree.setSelectionPath(new TreePath(newNode.getPath()));
			}
		}
	}

	private void deleteButtonActionPerformed() {
		if (tree.getSelectionPath() != null) {
			if (tree.getSelectionPath().getPathCount() != 1) {
				DefaultMutableTreeNode oldNode = (DefaultMutableTreeNode) tree
						.getSelectionPath().getLastPathComponent();
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode) oldNode
						.getParent();
				model.removeNodeFromParent(oldNode);
				tree.setSelectionPath(new TreePath(parent.getPath()));
			}
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

	private void negateButtonActionPerformed() {
		if (tree.getSelectionPath() != null) {
			DefaultMutableTreeNode oldNode = (DefaultMutableTreeNode) tree
					.getSelectionPath().getLastPathComponent();
			DefaultMutableTreeNode newNode = new NotFilterTreeNode();
			if (tree.getSelectionPath().getPathCount() != 1) {
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode) oldNode
						.getParent();
				int index = model.getIndexOfChild(parent, oldNode);
				model.removeNodeFromParent(oldNode);
				model.insertNodeInto(newNode, parent, index);
			} else {
				model.setRoot(newNode);
			}
			model.insertNodeInto(oldNode, newNode, 0);
			tree.setSelectionPath(new TreePath(oldNode.getPath()));
			tree.makeVisible(new TreePath(oldNode.getPath()));

			cleanUpDoubleNots();
		}
	}

	private void okButtonActionPerformed() {
		doClose(RET_OK);
	}

	private void cancelButtonActionPerformed() {
		doClose(RET_CANCEL);
	}

	/** Closes the dialog */
	private void closeDialog() {
		doClose(RET_CANCEL);
	}

	private void doClose(int retStatus) {
		returnStatus = retStatus;
		setVisible(false);
		dispose();
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String args[]) {
		final ListFilter f = new AndFilter();
		f.add(new OrFilter());
		f.add(new BearingFilter(EnumSet.noneOf(Bearing.class)));
		((ListFilter) f.get(0)).add(new NotFilter(new DistanceFilter(5)));
		((ListFilter) f.get(0)).add(new SizeFilter(EnumSet
				.noneOf(CacheSize.class)));

		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				FilterEditor dialog = new FilterEditor(f, new JFrame(), true);
				dialog.addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						System.exit(0);
					}
				});
				dialog.setVisible(true);
			}
		});
	}

	private int returnStatus = RET_CANCEL;

	private MyTreeModel model;

	private JTree tree;
	private JPanel treePanel;
	private JPanel buttonPanel;
	private FilterTabbedPane detailsPanel;
}
