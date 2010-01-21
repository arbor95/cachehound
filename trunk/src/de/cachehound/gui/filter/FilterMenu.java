package de.cachehound.gui.filter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import de.cachehound.filter.AndFilter;
import de.cachehound.filter.ArchivedFilter;
import de.cachehound.filter.BearingFilter;
import de.cachehound.filter.DifficultyFilter;
import de.cachehound.filter.DisabledFilter;
import de.cachehound.filter.DistanceFilter;
import de.cachehound.filter.FoundFilter;
import de.cachehound.filter.HasCoordinatesFilter;
import de.cachehound.filter.IFilter;
import de.cachehound.filter.NotFilter;
import de.cachehound.filter.OrFilter;
import de.cachehound.filter.OwnedFilter;
import de.cachehound.filter.SizeFilter;
import de.cachehound.filter.TerrainFilter;

public class FilterMenu extends JMenu {
	private class MyActionListener implements ActionListener {
		private IFilter filter;

		public MyActionListener(IFilter filter) {
			this.filter = filter;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			FilterMenu.this.action.execute(filter);
		}
	}

	private IMenuAction action;

	public FilterMenu(String text, IMenuAction action) {
		this(text, action, false);
	}

	private FilterMenu(String text, IMenuAction action, boolean negated) {
		super(text);

		this.action = action;

		add(createMenuItem("AND", new AndFilter(), negated));
		add(createMenuItem("OR", new OrFilter(), negated));
		if (!negated) {
			add(new FilterMenu("NOT", action, true));
		}
		add(new JSeparator());
		add(createMenuItem("Difficulty", new DifficultyFilter(), negated));
		add(createMenuItem("Terrain", new TerrainFilter(), negated));
		add(createMenuItem("Bearing", new BearingFilter(), negated));
		add(createMenuItem("Distance", new DistanceFilter(), negated));
		add(createMenuItem("Size", new SizeFilter(), negated));
		add(new JSeparator());
		add(createMenuItem("Archived", new ArchivedFilter(), negated));
		add(createMenuItem("Disabled", new DisabledFilter(), negated));
		add(createMenuItem("Found", new FoundFilter(), negated));
		add(createMenuItem("Owned", new OwnedFilter(), negated));
		add(new JSeparator());
		add(createMenuItem("Has coordinates", new HasCoordinatesFilter(),
				negated));
	}

	private JMenuItem createMenuItem(String text, IFilter f, boolean negated) {
		JMenuItem item = new JMenuItem(text);
		IFilter filter;
		if (negated) {
			filter = new NotFilter(f);
		} else {
			filter = f;
		}
		item.addActionListener(new MyActionListener(filter));
		return item;
	}
}
