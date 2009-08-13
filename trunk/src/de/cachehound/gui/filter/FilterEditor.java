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
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import de.cachehound.filter.AndFilter;
import de.cachehound.filter.BearingFilter;
import de.cachehound.filter.DistanceFilter;
import de.cachehound.filter.IFilter;
import de.cachehound.filter.ListFilter;
import de.cachehound.filter.OrFilter;
import de.cachehound.filter.SizeFilter;
import de.cachehound.gui.filter.nodes.AbstractFilterTreeNode;
import de.cachehound.gui.filter.nodes.FilterTreeNodeFactory;
import de.cachehound.types.Bearing;
import de.cachehound.types.CacheSize;

public class FilterEditor extends JDialog {
	/** A return status code - returned if Cancel button has been pressed */
	public static final int RET_CANCEL = 0;
	/** A return status code - returned if OK button has been pressed */
	public static final int RET_OK = 1;

	public FilterEditor(IFilter old, JFrame parent, boolean modal) {
		super(parent, modal);

		this.root = (new FilterTreeNodeFactory()).doCreate(old);

		initComponents();
	}

	/** @return the return status of this dialog - one of RET_OK or RET_CANCEL */
	public int getReturnStatus() {
		return returnStatus;
	}

	public IFilter getFilter() {
		return root.getFilter().clone();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 */
	private void initComponents() {
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				closeDialog();
			}
		});

		treePanel = createTreePanel();
		buttonPanel = createButtonPanel();
		detailsPanel = new FilterTabbedPane();

		this.setLayout(new BorderLayout());
		this.add(new JScrollPane(treePanel), BorderLayout.CENTER);
		this.add(detailsPanel, BorderLayout.EAST);
		this.add(buttonPanel, BorderLayout.SOUTH);

		treeSelectionChanged();

		pack();
	}

	private JPanel createTreePanel() {
		JPanel treePanel = new JPanel();

		model = new DefaultTreeModel(root);
		tree = new JTree(model);
		
		// Alles aufklappen
		for (Enumeration<?> e = root.depthFirstEnumeration(); e
				.hasMoreElements();) {
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
			model.insertNodeInto((new FilterTreeNodeFactory())
					.doCreate(detailsPanel.getFilter()), parent, parent
					.getChildCount());
		}
	}

	private void replaceButtonActionPerformed() {
		if (tree.getSelectionPath() != null) {
			if (tree.getSelectionPath().getPathCount() == 1) {
				model.setRoot((new FilterTreeNodeFactory())
						.doCreate(detailsPanel.getFilter()));
			} else {
				DefaultMutableTreeNode oldnode = (DefaultMutableTreeNode) tree
						.getSelectionPath().getLastPathComponent();
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode) oldnode
						.getParent();
				int index = model.getIndexOfChild(parent, oldnode);
				model.removeNodeFromParent((DefaultMutableTreeNode) tree
						.getSelectionPath().getLastPathComponent());
				model.insertNodeInto((new FilterTreeNodeFactory())
						.doCreate(detailsPanel.getFilter()), parent, index);
			}
		}
	}

	private void deleteButtonActionPerformed() {
		if (tree.getSelectionPath() != null) {
			if (tree.getSelectionPath().getPathCount() != 1) {
				model.removeNodeFromParent((DefaultMutableTreeNode) tree
						.getSelectionPath().getLastPathComponent());
			}
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
		((ListFilter) f.get(0)).add(new DistanceFilter(5));
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

	private AbstractFilterTreeNode root;
	private DefaultTreeModel model;

	private JTree tree;
	private JPanel treePanel;
	private JPanel buttonPanel;
	private FilterTabbedPane detailsPanel;
}
