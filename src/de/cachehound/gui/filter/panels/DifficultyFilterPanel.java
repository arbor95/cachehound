package de.cachehound.gui.filter.panels;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JCheckBox;

import de.cachehound.filter.DifficultyFilter;
import de.cachehound.filter.IFilter;
import de.cachehound.types.Difficulty;

public class DifficultyFilterPanel extends
		AbstractFilterPanel<DifficultyFilter> {
	
	public DifficultyFilterPanel() {
		initComponents();
		setState(new DifficultyFilter(EnumSet.noneOf(Difficulty.class)));
	}

	@Override
	public DifficultyFilter getFilter() {
		Set<Difficulty> mask = EnumSet.noneOf(Difficulty.class);

		for (Difficulty b : Difficulty.values()) {
			if (boxes.get(b).isSelected()) {
				mask.add(b);
			}
		}

		return new DifficultyFilter(mask);
	}

	@Override
	public void setState(DifficultyFilter old) {
		for (Difficulty b : old.getMask()) {
			boxes.get(b).setSelected(true);
		}
	}

	@Override
	public boolean canHandle(IFilter f) {
		return (f instanceof DifficultyFilter);
	}

	private void initComponents() {
		Dimension size = this.getPreferredSize();
		size.width = size.width * 10 / 6;
		this.setPreferredSize(size);
		
		boxes = new EnumMap<Difficulty, JCheckBox>(Difficulty.class);

		for (Difficulty b : Difficulty.values()) {
			JCheckBox box = new JCheckBox(b.toString());
			add(box);
			boxes.put(b, box);
			box.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					notifyFilterChangedListeners();
				}
			});
		}
	}

	private Map<Difficulty, JCheckBox> boxes;
}
