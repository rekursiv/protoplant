package com.protoplant.xtruder;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Injector;

public class PressureDisplayPanel extends DataDisplayPanel {


	public PressureDisplayPanel(Composite parent, Injector injector) {
		super(parent, injector, "Pressure", 1, false);
	}

	
	@Subscribe
	public void onData(final AnalogDataEvent evt) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				calcRunningAverage((float)evt.data1*config.displays[index].scale);
				updateValues();
			}
		});
	}
	

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
