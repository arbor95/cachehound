package de.cachehound.gui.filter;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;

import de.cachehound.filter.IFilter;

public class FilterEditor extends JDialog {
	/** A return status code - returned if Cancel button has been pressed */
	public static final int RET_CANCEL = 0;
	/** A return status code - returned if OK button has been pressed */
	public static final int RET_OK = 1;

	public FilterEditor(IFilter old, JFrame parent, boolean modal) {
		super(parent, modal);
		initComponents();
		// setState(old);
	}

	/** @return the return status of this dialog - one of RET_OK or RET_CANCEL */
	public int getReturnStatus() {
		return returnStatus;
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 */
	private void initComponents() {
		JPanel treePanel = createTreePanel();
		JPanel buttonPanel = createButtonPanel();

		this.setLayout(new BorderLayout());
		this.add(new JScrollPane(treePanel), BorderLayout.CENTER);
		this.add(buttonPanel, BorderLayout.SOUTH);

		pack();
	}

	private JPanel createTreePanel() {
		JPanel treePanel = new JPanel();
		
		JTree tree = new JTree();
		//TODO: Populate tree
		treePanel.add(tree);

		return new JPanel();
	}

	private JPanel createButtonPanel() {
		JPanel buttonPanel = new JPanel();

		okButton = new JButton();
		cancelButton = new JButton();

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				closeDialog(evt);
			}
		});

		okButton.setText("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				okButtonActionPerformed(evt);
			}
		});

		cancelButton.setText("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				cancelButtonActionPerformed(evt);
			}
		});

		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);

		return buttonPanel;
	}

	private void okButtonActionPerformed(ActionEvent evt) {
		doClose(RET_OK);
	}

	private void cancelButtonActionPerformed(ActionEvent evt) {
		doClose(RET_CANCEL);
	}

	/** Closes the dialog */
	private void closeDialog(WindowEvent evt) {
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
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				FilterEditor dialog = new FilterEditor(null, new JFrame(), true);
				dialog.addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						System.exit(0);
					}
				});
				dialog.setVisible(true);
			}
		});
	}

	private JButton cancelButton;
	private JButton okButton;

	private int returnStatus = RET_CANCEL;
}