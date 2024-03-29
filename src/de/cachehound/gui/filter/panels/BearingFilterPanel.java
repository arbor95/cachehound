package de.cachehound.gui.filter.panels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JCheckBox;

import layout.TableLayout;
import layout.TableLayoutConstants;
import de.cachehound.filter.BearingFilter;
import de.cachehound.filter.IFilter;
import de.cachehound.types.Bearing;

public class BearingFilterPanel extends AbstractFilterPanel<BearingFilter> {

	public BearingFilterPanel() {
		initComponents();
		setState(new BearingFilter(EnumSet.noneOf(Bearing.class)));
	}

	@Override
	public BearingFilter getFilter() {
		Set<Bearing> mask = EnumSet.noneOf(Bearing.class);

		for (Bearing b : Bearing.values()) {
			if (boxes.get(b).isSelected()) {
				mask.add(b);
			}
		}

		return new BearingFilter(mask);
	}

	@Override
	public void setState(BearingFilter old) {
		for (Bearing b : old.getMask()) {
			boxes.get(b).setSelected(true);
		}
	}

	@Override
	public boolean canHandle(IFilter f) {
		return (f instanceof BearingFilter);
	}

	private void initComponents() {
		boxes = new EnumMap<Bearing, JCheckBox>(Bearing.class);

		double size[][] = {
				{ TableLayoutConstants.FILL, TableLayoutConstants.FILL,
						TableLayoutConstants.FILL, TableLayoutConstants.FILL,
						TableLayoutConstants.FILL },
				{ TableLayoutConstants.FILL, TableLayoutConstants.FILL,
						TableLayoutConstants.FILL, TableLayoutConstants.FILL,
						TableLayoutConstants.FILL } };
		setLayout(new TableLayout(size));

		int x = 2;
		int y = 0;
		int dx = 1;
		int dy = 0;

		for (Bearing b : Bearing.values()) {
			JCheckBox box = new JCheckBox(b.toString());
			add(box, x + "," + y);
			boxes.put(b, box);
			box.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					notifyFilterChangedListeners();
				}
			});

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
	}

	private Map<Bearing, JCheckBox> boxes;
}
