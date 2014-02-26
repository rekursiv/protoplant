package com.protoplant.xtruder;

import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Injector;


public class DataDisplayPanel extends Group {

	private Logger log;
	private Label lblData;
	private int index;
	private boolean signed;
	private XtruderConfig config;
	
	
	public DataDisplayPanel(Composite parent, Injector injector, String label, int index, boolean signed) {
		super(parent, SWT.NONE);
		
		this.index = index;
		this.signed = signed;
		
		setText("("+index+") "+label);
		setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
		
		lblData = new Label(this, SWT.NONE);
		lblData.setFont(SWTResourceManager.getFont("Segoe UI", 24, SWT.NORMAL));
		lblData.setBounds(10, 28, 270, 59);
		
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
		float scale = config.displays[index].scale;
		int curValue = (evt.getByte(offset)<<8)|evt.getByte(offset+1);
		if (signed) curValue = (short)curValue;  // make signed
		
		lblData.setText(String.format("%.4f", (float)curValue*scale)+" "+config.displays[index].unit);
//		System.out.println("> "+curValue);
	}
	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
