package com.protoplant.xtruder;

import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.protoplant.xtruder.audio.AudioManager;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class FaultPanel extends Group {

	private Logger log;
	private Button btnReset;
	private Label lblOverMarker;
	private Label lblUnderMarker;
	private Label lblOver;
	private Label lblUnder;
	
	private int overCount = 0;
	private int underCount = 0;
	private AudioManager am;
	private DataLogger dl;
	private Button btnHomer;

	public FaultPanel(Composite parent, Injector injector) {
		super(parent, SWT.NONE);
		setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
		setText("Laser Faults");
		
		btnReset = new Button(this, SWT.NONE);
		btnReset.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				reset();
			}
		});
		btnReset.setFont(SWTResourceManager.getFont("Segoe UI", 15, SWT.NORMAL));
		btnReset.setBounds(219, 10, 92, 38);
		btnReset.setText("Reset");
		
		lblOverMarker = new Label(this, SWT.NONE);
		lblOverMarker.setFont(SWTResourceManager.getFont("Segoe UI", 15, SWT.NORMAL));
		lblOverMarker.setBounds(10, 29, 86, 28);
		lblOverMarker.setText("Over:");
		
		lblUnderMarker = new Label(this, SWT.NONE);
		lblUnderMarker.setFont(SWTResourceManager.getFont("Segoe UI", 15, SWT.NORMAL));
		lblUnderMarker.setBounds(10, 58, 86, 33);
		lblUnderMarker.setText("Under:");
		
		lblOver = new Label(this, SWT.NONE);
		lblOver.setFont(SWTResourceManager.getFont("Segoe UI", 15, SWT.NORMAL));
		lblOver.setBounds(102, 29, 75, 28);
		lblOver.setText("0");
		
		lblUnder = new Label(this, SWT.NONE);
		lblUnder.setFont(SWTResourceManager.getFont("Segoe UI", 15, SWT.NORMAL));
		lblUnder.setBounds(102, 58, 75, 33);
		lblUnder.setText("0");
		
		btnHomer = new Button(this, SWT.CHECK);
		btnHomer.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
		btnHomer.setBounds(209, 60, 102, 28);
		btnHomer.setText("Homer");
		
		if (injector!=null) injector.injectMembers(this);
	}
	
	@Inject
	public void inject(Logger log, DataLogger dl, AudioManager am) {
		this.log = log;
		this.dl = dl;
		this.am = am;
	}
	
	@Subscribe
	public void onFault(LaserEvent evt) {
		if (evt.isOver) {
			++overCount;
		}
		else {
			++underCount;
		}
		updateDisplay();
		dl.write("LaserFault", ""+underCount, ""+overCount);
		if (btnHomer.getSelection()) am.playClip("doh");
	}
	
	@Subscribe
	public void onCoilReset(CoilResetEvent event) {
		reset();
	}
	
	private void updateDisplay() {
		lblOver.setText(""+overCount);
		lblUnder.setText(""+underCount);
	}
	
	private void reset() {
		dl.write("LaserFault", "RESET", ""+underCount, ""+overCount);
		overCount = 0;
		underCount = 0;
		updateDisplay();
	}
	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
