package de.cachehound.gui.filter;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JCheckBox;

import de.cachehound.filter.SizeFilter;
import de.cachehound.types.CacheSize;

public class SizeFilterEditor extends AbstractSimpleFilterEditor<SizeFilter> {

	public SizeFilterEditor() {
		initComponents();
		setState(new SizeFilter(EnumSet.noneOf(CacheSize.class)));
	}

	public SizeFilterEditor(SizeFilter old) {
		initComponents();
		setState(old);
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

	public void setState(SizeFilter old) {
		for (CacheSize b : old.getMask()) {
			boxes.get(b).setSelected(true);
		}
	}

	private void initComponents() {
		boxes = new EnumMap<CacheSize, JCheckBox>(CacheSize.class);

		for (CacheSize b : CacheSize.values()) {
			JCheckBox box = new JCheckBox(b.toString());
			add(box);
			boxes.put(b, box);
		}
	}

	private Map<CacheSize, JCheckBox> boxes;
}
