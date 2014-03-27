package com.protoplant.xtruder;

import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.protoplant.xtruder.audio.AudioManager;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.wb.swt.SWTResourceManager;


public class SpoolWeightPanel extends Group implements Runnable {

	private Logger log;
	private Label lblData;
	private MotorPanel refMotor;
	
	private volatile float grams=0;
	private volatile float prevGrams=0;
	private volatile boolean isMotorRunning = false;
	private volatile Thread thread = null;
	private volatile long prevStepTime = 0;
	
	private Button rb250g;
	private Button rb1kg;
	private Button rbPcabs;
	private Button rbHtpla;
	private Button rbCfpla;
	private Button btnReset;
	private Group grpMaterial;
	private Group grpReset;
	private XtruderConfig config;
	private Group grpDia;
	private Button rb175;
	private Button rb3;
	private AudioManager am;
		
	
	public SpoolWeightPanel(Composite parent, Injector injector, MotorPanel refMotor) {
		super(parent, SWT.NONE);
		
		this.refMotor = refMotor;
		
		setText("Spool Weight");
		setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
		setLayout(new FormLayout());
		
		lblData = new Label(this, SWT.NONE);
		FormData fd_lblData = new FormData();
		fd_lblData.bottom = new FormAttachment(0, 62);
		fd_lblData.right = new FormAttachment(0, 162);
		fd_lblData.top = new FormAttachment(0, 8);
		fd_lblData.left = new FormAttachment(0, 7);
		lblData.setLayoutData(fd_lblData);
		lblData.setText("0.00 g");
		lblData.setFont(SWTResourceManager.getFont("Segoe UI", 24, SWT.NORMAL));
		
		btnReset = new Button(this, SWT.NONE);
		btnReset.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				grams=0;
				lblData.setText("0.00 g");
			}
		});
		FormData fd_btnReset = new FormData();
		fd_btnReset.right = new FormAttachment(0, 712);
		fd_btnReset.left = new FormAttachment(0, 635);
		btnReset.setLayoutData(fd_btnReset);
		btnReset.setFont(SWTResourceManager.getFont("Segoe UI", 14, SWT.NORMAL));
		btnReset.setText("Reset");
		
		grpMaterial = new Group(this, SWT.NONE);
		fd_btnReset.top = new FormAttachment(grpMaterial, -67);
		fd_btnReset.bottom = new FormAttachment(grpMaterial, 0, SWT.BOTTOM);
		FormData fd_grpMaterial = new FormData();
		fd_grpMaterial.left = new FormAttachment(btnReset, -260, SWT.LEFT);
		fd_grpMaterial.right = new FormAttachment(btnReset, -6);
		grpMaterial.setLayoutData(fd_grpMaterial);
		FillLayout fl_grpMaterial = new FillLayout(SWT.HORIZONTAL);
		fl_grpMaterial.marginWidth = 5;
		fl_grpMaterial.spacing = 4;
		grpMaterial.setLayout(fl_grpMaterial);
		
		rbPcabs = new Button(grpMaterial, SWT.RADIO);
		rbPcabs.setSelection(true);
		rbPcabs.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		rbPcabs.setText("PCABS");
		
		rbHtpla = new Button(grpMaterial, SWT.RADIO);
		rbHtpla.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		rbHtpla.setText("HTPLA");
		
		rbCfpla = new Button(grpMaterial, SWT.RADIO);
		rbCfpla.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		rbCfpla.setText("CFPLA");
		
		grpReset = new Group(this, SWT.NONE);
		FormData fd_grpReset = new FormData();
		fd_grpReset.bottom = new FormAttachment(0, 75);
		fd_grpReset.right = new FormAttachment(0, 246);
		fd_grpReset.top = new FormAttachment(0, 0);
		fd_grpReset.left = new FormAttachment(0, 168);
		grpReset.setLayoutData(fd_grpReset);
		FillLayout fl_grpReset = new FillLayout(SWT.VERTICAL);
		fl_grpReset.marginWidth = 5;
		fl_grpReset.spacing = 4;
		grpReset.setLayout(fl_grpReset);
		
		rb250g = new Button(grpReset, SWT.RADIO);
		rb250g.setSelection(true);
		rb250g.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		rb250g.setText("250g");
		
		rb1kg = new Button(grpReset, SWT.RADIO);
		rb1kg.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		rb1kg.setText("1kg");
		
		grpDia = new Group(this, SWT.NONE);
		fd_grpMaterial.bottom = new FormAttachment(grpDia, 75);
		fd_grpMaterial.top = new FormAttachment(grpDia, 0, SWT.TOP);
		FillLayout fl_grpDia = new FillLayout(SWT.VERTICAL);
		fl_grpDia.spacing = 4;
		fl_grpDia.marginWidth = 5;
		grpDia.setLayout(fl_grpDia);
		FormData fd_grpDia = new FormData();
		fd_grpDia.right = new FormAttachment(grpMaterial, -6);
		fd_grpDia.bottom = new FormAttachment(0, 75);
		fd_grpDia.top = new FormAttachment(grpReset, 0, SWT.TOP);
		fd_grpDia.left = new FormAttachment(grpReset, 6);
		grpDia.setLayoutData(fd_grpDia);
		
		rb175 = new Button(grpDia, SWT.RADIO);
		rb175.setText("1.75mm");
		rb175.setSelection(true);
		rb175.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		
		rb3 = new Button(grpDia, SWT.RADIO);
		rb3.setText("3mm");
		rb3.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		
		
		if (injector!=null) injector.injectMembers(this);

	}
	
	
	@Inject
	public void inject(Logger log, XtruderConfig config, AudioManager am) {
		this.log = log;
		this.config = config;
		this.am = am;
		this.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent arg0) {
				stop();
			}
		});
	}
	
	public void destroy() {
		isMotorRunning = false;
		if (thread!=null) thread.interrupt();
	}
	
	@Subscribe
	public void onMotorSpeedChange(MotorStateChangeEvent event) {
		if (event.getPanel()==refMotor) {
			if (event.getState()==MotorState.RUN && isMotorRunning==false) {
				start();
			}
			if (event.getState()==MotorState.STOP) {
				stop();
			}
		}

	}
	
	@Override
	public void run() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			return;
		}

		updateDisplay();
		
		if (isMotorRunning) {
			thread = new Thread(this);
			thread.start();
		}
	}
	
	public void start() {
		isMotorRunning = true;
		thread = new Thread(this);
		thread.start();
	}
	
	
	public void stop() {
		isMotorRunning = false;
	}
	
	private void updateDisplay() {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				float delay = (System.currentTimeMillis()-prevStepTime)/1000.0f;
				prevStepTime=System.currentTimeMillis();
				if (delay>1) return;
				float scale = 0;
				if (rb175.getSelection()) {
//					if (rbPcabs.getSelection()) scale = config.pcabs175GramsPerInch;
//					else if (rbHtpla.getSelection()) scale = config.htpla175density;
//					else if (rbCfpla.getSelection()) scale = config.cfpla175GramsPerInch;
				} else if (rb3.getSelection()) {
//					if (rbPcabs.getSelection()) scale = config.pcabs3GramsPerInch;
//					else if (rbHtpla.getSelection()) scale = config.htpla3GramsPerInch;
//					else if (rbCfpla.getSelection()) scale = config.cfpla3GramsPerInch;
				}
				grams+=(refMotor.getSpeed()*delay)*scale;
				if (rb250g.getSelection()) {
					updateAudio250g();
					if (grams>250) grams=0;
				}
				else if (rb1kg.getSelection()) {
					updateAudio1kg();
					if (grams>1000) grams=0;
				}
				lblData.setText(String.format("%.2f g", grams));
				prevGrams = grams;
			}
		});
	}
	
	private void updateAudio250g() {
		if (checkpoint(200)) am.playClip("200g");  //  
		else if (checkpoint(240)) am.playClip("240g");
		else if (checkpoint(245)) am.playClip("5");
		else if (checkpoint(246)) am.playClip("4");
		else if (checkpoint(247)) am.playClip("3");
		else if (checkpoint(248)) am.playClip("2");
		else if (checkpoint(249)) am.playClip("1");
		else if (checkpoint(0)) am.playClip("mark");
	}
	
	private void updateAudio1kg() {
		if (checkpoint(900)) am.playClip("900g");  //  970
		else if (checkpoint(990)) am.playClip("990g");
		else if (checkpoint(995)) am.playClip("5");
		else if (checkpoint(996)) am.playClip("4");
		else if (checkpoint(997)) am.playClip("3");
		else if (checkpoint(998)) am.playClip("2");
		else if (checkpoint(999)) am.playClip("1");
		else if (checkpoint(0)) am.playClip("mark");
	}
	
	private boolean checkpoint(float ref) {
		if (prevGrams<=ref && grams>=ref) return true;
		else if (ref-0.0f<0.1 && grams<prevGrams) return true;
		else return false;
	}
	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}


	public void test() {
		log.info("");
//		grams = 242;
//		prevGrams = grams;
//		lblData.setText(String.format("%.2f g", grams));
		am.playClip("mark");
	}
}
