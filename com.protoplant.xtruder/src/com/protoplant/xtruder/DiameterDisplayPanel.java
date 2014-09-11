package com.protoplant.xtruder;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Injector;

public class DiameterDisplayPanel extends DataDisplayPanel {

	private CoilMassPanel coilPanel;

	public DiameterDisplayPanel(Composite parent, Injector injector, CoilMassPanel coilPanel) {
		super(parent, injector, "Diameter", 0, false);
		this.coilPanel = coilPanel;
	}


	
	@Subscribe
	public void onData(final IndicatorDataEvent evt) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				curValue = evt.getCur();
				if (coilPanel!=null) coilPanel.setDiameter(curValue);
				if (evt.getMax()>maxValue) {
					maxValue = evt.getMax();
					lblMax.setText(String.format("%.2f", maxValue));
				}
				if (evt.getMin()<minValue) {
					minValue = evt.getMin();
					lblMin.setText(String.format("%.2f", minValue));
				}
				lblData.setText(String.format("%.2f", curValue)+" "+config.displays[index].unit);
				logData();
			}
		});
	}
	
	
	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
