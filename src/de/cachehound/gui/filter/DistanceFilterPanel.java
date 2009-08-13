package de.cachehound.gui.filter;

import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import de.cachehound.filter.DistanceFilter;
import de.cachehound.filter.IFilter;

public class DistanceFilterPanel extends
		AbstractFilterPanel<DistanceFilter> {

	public DistanceFilterPanel() {
		initComponents();
		setState(new DistanceFilter(0));
	}
	
	@Override
	public DistanceFilter getFilter() {
		return new DistanceFilter(((Number)model.getValue()).doubleValue());
	}

	@Override
	public void setState(DistanceFilter old) {
		model.setValue(old.getLimit());
	}

	@Override
	public boolean canHandle(IFilter f) {
		return (f instanceof DistanceFilter);
	}

	private void initComponents() {
		model = new SpinnerNumberModel(5d, 0, 1000, 1);
		
		add(new JLabel("Show only caches nearer than"));
		add(new JSpinner(model));
	}
	
	private SpinnerModel model;
}
