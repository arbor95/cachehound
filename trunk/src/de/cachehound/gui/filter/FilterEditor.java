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
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

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
		return treePanel.getRoot().getFilter().clone();
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

	private TreePanel createTreePanel(IFilter f) {
		TreePanel treePanel = new TreePanel((new FilterTreeNodeFactory())
				.doCreate(f));

		treePanel.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				treeSelectionChanged();
			}
		});

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

	private void treeSelectionChanged() {
		if (treePanel.isSomethingSelected()) {
			detailsPanel
					.showFilter(treePanel.getCurrentSelection().getFilter());
		}
	}

	private void addButtonActionPerformed() {
		AbstractFilterTreeNode newNode = (new FilterTreeNodeFactory())
				.doCreate(detailsPanel.getFilter());
		treePanel.addNode(newNode);
	}

	private void replaceButtonActionPerformed() {
		AbstractFilterTreeNode newNode = (new FilterTreeNodeFactory())
				.doCreate(detailsPanel.getFilter());
		treePanel.replaceSelectionWithNode(newNode);
	}

	private void deleteButtonActionPerformed() {
		treePanel.deleteSelection();
	}

	private void negateButtonActionPerformed() {
		treePanel.negateSelection();
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

	private TreePanel treePanel;
	private JPanel buttonPanel;
	private FilterTabbedPane detailsPanel;
}
