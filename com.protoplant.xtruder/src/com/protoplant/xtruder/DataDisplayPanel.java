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
	private String label;
	private int index;
	private boolean signed;
	private XtruderConfig config;
	private Label lblPrevMax;
	private Label lblPrevMin;
	private Button btnReset;
	private float curValue;
	private float minValue;
	private float maxValue;
	private DataLogger dl;
	private Label lblMax;
	private Label lblMin;
	
	
	public DataDisplayPanel(Composite parent, Injector injector, String label, int index, boolean signed) {
		super(parent, SWT.NONE);
		
		this.index = index;
		this.signed = signed;
		
		this.label = label;
		setText("("+index+") "+label);
		setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
		
		lblData = new Label(this, SWT.NONE);
		lblData.setText("0.00");
		lblData.setFont(SWTResourceManager.getFont("Segoe UI", 16, SWT.NORMAL));
		lblData.setBounds(50, 41, 117, 34);
		
		lblPrevMax = new Label(this, SWT.NONE);
		lblPrevMax.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
		lblPrevMax.setBounds(10, 21, 97, 21);
		
		lblPrevMin = new Label(this, SWT.NONE);
		lblPrevMin.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
		lblPrevMin.setBounds(10, 76, 97, 20);
		
		minValue = 99999;
		lblPrevMin.setText("PREV MIN");
		maxValue = -99999;
		lblPrevMax.setText("PREV MAX");
		
		btnReset = new Button(this, SWT.NONE);
		btnReset.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				reset();
			}
		});
		btnReset.setFont(SWTResourceManager.getFont("Segoe UI", 15, SWT.NORMAL));
		btnReset.setBounds(257, 33, 75, 45);
		btnReset.setText("Reset");
		
		lblMax = new Label(this, SWT.NONE);
		lblMax.setText("MAX");
		lblMax.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
		lblMax.setBounds(136, 21, 97, 21);
		
		lblMin = new Label(this, SWT.NONE);
		lblMin.setText("MIN");
		lblMin.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
		lblMin.setBounds(136, 76, 97, 20);
		
		if (injector!=null) injector.injectMembers(this);
	}
	
	
	@Inject
	public void inject(Logger log, XtruderConfig config, DataLogger dl) {
		this.log = log;
		this.config = config;
		this.dl = dl;
		lblData.setText("0.00 "+config.displays[index].unit);
	}
	
	@Subscribe
	public void onDataRx(SerialDataRxEvent evt) {
		int offset = index*2;  // data is two bytes long
		
		float scale = config.displays[index].scale;
		int data = (evt.getByte(offset)<<8)|evt.getByte(offset+1);
		if (signed) data = (short)data;  // make signed
		
		calcRunningAverage((float)data*scale);
		updateDisplay();
	}
	
	private void calcRunningAverage(float sample) {
		if (config.displays[index].smoothing >= 1.0) {
			float offset = sample-curValue;
			curValue+=offset/config.displays[index].smoothing;
		} else {
			curValue = sample;
		}
	}

	@Subscribe
	public void onCoilReset(CoilResetEvent event) {
		reset();
	}

	@Subscribe
	public void onMpgStepEvent(MpgStepEvent event) {			////////////////////////    TEST
		calcRunningAverage((float)event.getStep()*0.01f);
		updateDisplay();
	}
	
	public void updateDisplay() {
		if (curValue>maxValue) {
			maxValue = curValue;
			lblMax.setText(String.format("%.2f", maxValue));
		}
		if (curValue<minValue) {
			minValue = curValue;
			lblMin.setText(String.format("%.2f", minValue));
		}
		
		String data = String.format("%.2f", curValue)+" "+config.displays[index].unit;
		if (data.compareTo(lblData.getText())!=0) {
			lblData.setText(data);
			dl.write(label, String.format("%.2f", curValue));
		}
	}
	
	public void reset() {
		dl.write(label, "RESET", String.format("%.2f", minValue), String.format("%.2f", maxValue));
		lblPrevMin.setText(lblMin.getText());
		lblMin.setText("MIN");
		minValue = 99999;
		
		lblPrevMax.setText(lblMax.getText());
		lblMax.setText("MAX");
		maxValue = -99999;
	}
	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
