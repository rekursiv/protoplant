package com.protoplant.xtruder;

import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;


public class DataDisplayPanel extends Group {

	private Logger log;
	private Label lblData;
	private int index;
	private boolean signed;
	private XtruderConfig config;
	private Label lblMax;
	private Label lblMin;
	private Button btnReset;
	private float curValue;
	private float minValue;
	private float maxValue;
	
	
	public DataDisplayPanel(Composite parent, Injector injector, String label, int index, boolean signed) {
		super(parent, SWT.NONE);
		
		this.index = index;
		this.signed = signed;
		
		setText("("+index+") "+label);
		setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
		
		lblData = new Label(this, SWT.NONE);
		lblData.setText("0.00");
		lblData.setFont(SWTResourceManager.getFont("Segoe UI", 16, SWT.NORMAL));
		lblData.setBounds(10, 37, 214, 34);
		
		lblMax = new Label(this, SWT.NONE);
		lblMax.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
		lblMax.setBounds(130, 16, 121, 21);
		lblMax.setText("MAX");
		
		lblMin = new Label(this, SWT.NONE);
		lblMin.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
		lblMin.setBounds(130, 71, 121, 20);
		lblMin.setText("MIN");
		
		btnReset = new Button(this, SWT.NONE);
		btnReset.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				reset();
			}
		});
		btnReset.setFont(SWTResourceManager.getFont("Segoe UI", 15, SWT.NORMAL));
		btnReset.setBounds(257, 24, 75, 65);
		btnReset.setText("Reset");
		
		if (injector!=null) injector.injectMembers(this);
	}
	
	
	@Inject
	public void inject(Logger log, XtruderConfig config) {
		this.log = log;
		this.config = config;
		lblData.setText("0.0000 "+config.displays[index].unit);
	}
	
	@Subscribe
	public void onDataRx(SerialDataRxEvent evt) {
		int offset = index*2;  // data is two bytes long
		
		if (offset==0) offset=2;   										////////////////////////    TEST
		
		float scale = config.displays[index].scale;
		int data = (evt.getByte(offset)<<8)|evt.getByte(offset+1);
		if (signed) data = (short)data;  // make signed
		
		calcRunningAverage((float)data*scale);
		updateDisplay();
		
//		System.out.println("> "+curValue);
	}
	
	private void calcRunningAverage(float sample) {
		if (config.displays[index].smoothing >= 1.0) {
			float offset = sample-curValue;
			curValue+=offset/config.displays[index].smoothing;
		} else {
			curValue = sample;
		}
	}


//	@Subscribe
//	public void onMpgStepEvent(MpgStepEvent event) {			////////////////////////    TEST
//		testValue+=(float)event.getStep()*0.01;
//		updateDisplay(testValue);
//	}
	
	public void updateDisplay() {
		if (curValue>maxValue) maxValue = curValue;
		if (curValue<minValue) minValue = curValue;
		lblData.setText(String.format("%.4f", curValue)+" "+config.displays[index].unit);
		lblMin.setText(String.format("%.4f", minValue));
		lblMax.setText(String.format("%.4f", maxValue));
	}
	
	public void reset() {
		minValue = 99999;
		lblMin.setText("MIN");
		maxValue = -99999;
		lblMax.setText("MAX");
	}
	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
