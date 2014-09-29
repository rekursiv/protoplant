package com.protoplant.xtruder;

import org.eclipse.swt.widgets.Composite;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Injector;

public class DiameterDisplayPanel extends DataDisplayPanel {

	private CoilMassPanel coilPanel;

	public DiameterDisplayPanel(Composite parent, Injector injector, CoilMassPanel coilPanel) {
		super(parent, injector, "Diameter", 0, true);
		this.coilPanel = coilPanel;
	}

	@Override
	protected void updateValues() {
		super.updateValues();
		if (coilPanel!=null) coilPanel.setDiameter(curValue);
	}
	
	
//	@Subscribe
//	public void onMpgStepEvent(MpgStepEvent event) {			////////////////////////    TEST
//		calcRunningAverage((float)event.getStep()*0.01f);
//		updateValues();
//	}
	
	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
