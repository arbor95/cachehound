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

import de.cachehound.filter.SizeFilter;
import de.cachehound.types.CacheSize;

public class SizeFilterEditor extends AbstractSimpleFilterEditor<SizeFilter> {
	/** Creates new form CacheSizeFilterEditor */
	public SizeFilterEditor(SizeFilter old, JFrame parent, boolean modal) {
		super(old, parent, modal);
	}

	public SizeFilter getFilter() {
		Set<CacheSize> mask = EnumSet.noneOf(CacheSize.class);

		for (CacheSize b : CacheSize.values()) {
			if (boxes.get(b).isSelected()) {
				mask.add(b);
			}
		}

		return new SizeFilter(mask);
	}

	protected void setState(SizeFilter old) {
		if (old != null) {
			for (CacheSize b : old.getMask()) {
				boxes.get(b).setSelected(true);
			}
		}
	}

	protected JPanel createCheckBoxesPanel() {
		JPanel panel = new JPanel();
		boxes = new EnumMap<CacheSize, JCheckBox>(CacheSize.class);

		for (CacheSize b : CacheSize.values()) {
			JCheckBox box = new JCheckBox(b.toString());
			panel.add(box);
			boxes.put(b, box);
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
				SizeFilterEditor dialog = new SizeFilterEditor(null,
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

	private Map<CacheSize, JCheckBox> boxes;
}
