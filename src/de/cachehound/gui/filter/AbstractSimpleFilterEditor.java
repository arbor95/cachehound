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

import de.cachehound.filter.IFilter;

public abstract class AbstractSimpleFilterEditor<T extends IFilter> extends
		JDialog {
	/** A return status code - returned if Cancel button has been pressed */
	public static final int RET_CANCEL = 0;
	/** A return status code - returned if OK button has been pressed */
	public static final int RET_OK = 1;

	/** Creates new form CacheSizeFilterEditor */
	public AbstractSimpleFilterEditor(T old, JFrame parent, boolean modal) {
		super(parent, modal);
		initComponents();
		setState(old);
	}

	/** @return the return status of this dialog - one of RET_OK or RET_CANCEL */
	public int getReturnStatus() {
		return returnStatus;
	}

	public abstract T getFilter();

	protected abstract void setState(T old);

	/**
	 * This method is called from within the constructor to initialize the form.
	 */
	private void initComponents() {
		JPanel checkBoxPanel = createCheckBoxesPanel();
		JPanel buttonPanel = createButtonPanel();

		this.setLayout(new BorderLayout());
		this.add(checkBoxPanel, BorderLayout.CENTER);
		this.add(buttonPanel, BorderLayout.SOUTH);

		pack();
	}

	protected abstract JPanel createCheckBoxesPanel();

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

	private JButton cancelButton;
	private JButton okButton;

	private int returnStatus = RET_CANCEL;
}
