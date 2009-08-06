package de.cachehound.gui.filter;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import layout.TableLayout;
import layout.TableLayoutConstants;
import de.cachehound.filter.BearingFilter;
import de.cachehound.types.Bearing;

public class BearingFilterEditor extends
		AbstractSimpleFilterEditor<BearingFilter> {
	/** Creates new form BearingFilterEditor */
	public BearingFilterEditor(BearingFilter old, JFrame parent, boolean modal) {
		super(old, parent, modal);
	}

	public BearingFilter getFilter() {
		Set<Bearing> mask = EnumSet.noneOf(Bearing.class);

		for (Bearing b : Bearing.values()) {
			if (boxes.get(b).isSelected()) {
				mask.add(b);
			}
		}

		return new BearingFilter(mask);
	}

	protected void setState(BearingFilter old) {
		if (old != null) {
			for (Bearing b : old.getMask()) {
				boxes.get(b).setSelected(true);
			}
		}
	}

	protected JPanel createCheckBoxesPanel() {
		JPanel panel = new JPanel();
		boxes = new EnumMap<Bearing, JCheckBox>(Bearing.class);

		double size[][] = {
				{ TableLayoutConstants.FILL, TableLayoutConstants.FILL,
						TableLayoutConstants.FILL, TableLayoutConstants.FILL,
						TableLayoutConstants.FILL },
				{ TableLayoutConstants.FILL, TableLayoutConstants.FILL,
						TableLayoutConstants.FILL, TableLayoutConstants.FILL,
						TableLayoutConstants.FILL } };
		panel.setLayout(new TableLayout(size));

		int x = 2;
		int y = 0;
		int dx = 1;
		int dy = 0;

		for (Bearing b : Bearing.values()) {
			JCheckBox box = new JCheckBox(b.toString());
			panel.add(box, x + "," + y);
			boxes.put(b, box);

			if (x == 4 && y == 0) {
				dx = 0;
				dy = 1;
			} else if (x == 4 && y == 4) {
				dx = -1;
				dy = 0;
			} else if (x == 0 && y == 4) {
				dx = 0;
				dy = -1;
			} else if (x == 0 && y == 0) {
				dx = 1;
				dy = 0;
			}

			x += dx;
			y += dy;
		}

		return panel;
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				BearingFilterEditor dialog = new BearingFilterEditor(null,
						new JFrame(), true);
				dialog.addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						System.exit(0);
					}
				});
				dialog.setVisible(true);
			}
		});
	}

	private Map<Bearing, JCheckBox> boxes;
}
