package de.cachehound.gui.filter;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.EnumSet;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeSelectionModel;

import de.cachehound.filter.AndFilter;
import de.cachehound.filter.BearingFilter;
import de.cachehound.filter.DistanceFilter;
import de.cachehound.filter.IFilter;
import de.cachehound.filter.ListFilter;
import de.cachehound.filter.OrFilter;
import de.cachehound.filter.SizeFilter;
import de.cachehound.types.Bearing;
import de.cachehound.types.CacheSize;

public class FilterEditor extends JDialog {
	/** A return status code - returned if Cancel button has been pressed */
	public static final int RET_CANCEL = 0;
	/** A return status code - returned if OK button has been pressed */
	public static final int RET_OK = 1;

	public FilterEditor(IFilter old, JFrame parent, boolean modal) {
		super(parent, modal);

		this.model = new FilterTreeModel(old.clone());

		initComponents();
	}

	/** @return the return status of this dialog - one of RET_OK or RET_CANCEL */
	public int getReturnStatus() {
		return returnStatus;
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

		pack();
	}

	private JPanel createTreePanel() {
		JPanel treePanel = new JPanel();

		tree = new JTree(model);
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
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

	private void treeSelectionChanged() {
		if (tree.getSelectionPath() != null) {
			detailsPanel.showFilter(FilterTreeModel.object2IFilter(tree
					.getSelectionPath().getLastPathComponent()));
		}
	}

	private void addButtonActionPerformed() {
		if (tree.getSelectionPath() != null) {
			model.addFilter(tree.getSelectionPath(), detailsPanel.getFilter());
		}
	}

	private void replaceButtonActionPerformed() {
		if (tree.getSelectionPath() != null) {
			model.replaceFilter(tree.getSelectionPath(), detailsPanel
					.getFilter());
		}
	}

	private void deleteButtonActionPerformed() {
		if (tree.getSelectionPath() != null) {
			model.deleteFilter(tree.getSelectionPath());
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

	private FilterTreeModel model;

	private JTree tree;
	private JPanel treePanel;
	private JPanel buttonPanel;
	private FilterTabbedPane detailsPanel;
}
