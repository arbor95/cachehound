package de.cachehound.gui.filter.panels;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JCheckBox;

import de.cachehound.filter.IFilter;
import de.cachehound.filter.TerrainFilter;
import de.cachehound.types.Terrain;

public class TerrainFilterPanel extends AbstractFilterPanel<TerrainFilter> {
	public TerrainFilterPanel() {
		initComponents();
		setState(new TerrainFilter(EnumSet.noneOf(Terrain.class)));
	}

	@Override
	public TerrainFilter getFilter() {
		Set<Terrain> mask = EnumSet.noneOf(Terrain.class);

		for (Terrain b : Terrain.values()) {
			if (boxes.get(b).isSelected()) {
				mask.add(b);
			}
		}

		return new TerrainFilter(mask);
	}

	@Override
	public void setState(TerrainFilter old) {
		for (Terrain b : old.getMask()) {
			boxes.get(b).setSelected(true);
		}
	}

	@Override
	public boolean canHandle(IFilter f) {
		return (f instanceof TerrainFilter);
	}

	private void initComponents() {
		Dimension size = this.getPreferredSize();
		size.width = size.width * 10 / 6;
		this.setPreferredSize(size);

		boxes = new EnumMap<Terrain, JCheckBox>(Terrain.class);

		for (Terrain b : Terrain.values()) {
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

	private Map<Terrain, JCheckBox> boxes;

}
