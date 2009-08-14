package de.cachehound.gui.filter;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JCheckBox;

import de.cachehound.filter.IFilter;
import de.cachehound.filter.SizeFilter;
import de.cachehound.types.CacheSize;

public class SizeFilterPanel extends AbstractFilterPanel<SizeFilter> {

	public SizeFilterPanel() {
		initComponents();
		setState(new SizeFilter(EnumSet.noneOf(CacheSize.class)));
	}

	@Override
	public SizeFilter getFilter() {
		Set<CacheSize> mask = EnumSet.noneOf(CacheSize.class);

		for (CacheSize b : CacheSize.values()) {
			if (boxes.get(b).isSelected()) {
				mask.add(b);
			}
		}

		return new SizeFilter(mask);
	}

	@Override
	public void setState(SizeFilter old) {
		for (CacheSize b : old.getMask()) {
			boxes.get(b).setSelected(true);
		}
	}

	@Override
	public boolean canHandle(IFilter f) {
		return (f instanceof SizeFilter);
	}

	private void initComponents() {
		Dimension size = this.getPreferredSize();
		size.width = size.width * 10 / 6;
		this.setPreferredSize(size);
		
		boxes = new EnumMap<CacheSize, JCheckBox>(CacheSize.class);

		for (CacheSize b : CacheSize.values()) {
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

	private Map<CacheSize, JCheckBox> boxes;
}
