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

import util.config.ConfigManager;


public class CoilMassPanel extends Group {

	private Logger log;
	private Label lblData;
	private MotorPanel refMotor;
	
	private volatile float delay=0;
	private volatile float grams=0;
	private volatile float prevGrams=0;
	private volatile boolean isMotorRunning = false;
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
	private AudioManager am;
	private ConfigManager<XtruderConfig> cfgMgr;
		
	
	public CoilMassPanel(Composite parent, Injector injector, MotorPanel refMotor) {   //  350 x 327
		super(parent, SWT.NONE);
		
		this.refMotor = refMotor;
		
		setText("Coil Mass");
		setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
		setLayout(new FormLayout());
		
		lblData = new Label(this, SWT.NONE);
		FormData fd_lblData = new FormData();
		fd_lblData.bottom = new FormAttachment(0, 62);
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
		fd_btnReset.bottom = new FormAttachment(0, 95);
		fd_btnReset.top = new FormAttachment(0, 28);
		fd_btnReset.right = new FormAttachment(0, 295);
		fd_btnReset.left = new FormAttachment(0, 188);
		btnReset.setLayoutData(fd_btnReset);
		btnReset.setFont(SWTResourceManager.getFont("Segoe UI", 14, SWT.NORMAL));
		btnReset.setText("Reset");
		
		grpMaterial = new Group(this, SWT.NONE);
		FormData fd_grpMaterial = new FormData();
		fd_grpMaterial.right = new FormAttachment(0, 257);
		fd_grpMaterial.left = new FormAttachment(0, 12);
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
		fd_grpMaterial.bottom = new FormAttachment(grpReset, 81, SWT.BOTTOM);
		fd_grpMaterial.top = new FormAttachment(grpReset, 6);
		fd_lblData.right = new FormAttachment(0, 140);
		FormData fd_grpReset = new FormData();
		fd_grpReset.bottom = new FormAttachment(lblData, 81, SWT.BOTTOM);
		fd_grpReset.top = new FormAttachment(lblData, 6);
		fd_grpReset.right = new FormAttachment(0, 160);
		fd_grpReset.left = new FormAttachment(0, 8);
		grpReset.setLayoutData(fd_grpReset);
		FillLayout fl_grpReset = new FillLayout(SWT.HORIZONTAL);
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
		
		
		if (injector!=null) injector.injectMembers(this);

	}
	
	
	@Inject
	public void inject(Logger log, XtruderConfig config, ConfigManager<XtruderConfig> cfgMgr, AudioManager am) {
		this.log = log;
		this.config = config;
		this.cfgMgr = cfgMgr;
		this.am = am;
		this.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent arg0) {
				stop();
			}
		});
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
	
	public void reloadConfig() {
		config = cfgMgr.load();
//		log.info("###  "+config.cfplaDensity);		
	}
	
	public void start() {
		log.info("");
		isMotorRunning = true;
	}
	
	
	public void stop() {
		log.info("");
		isMotorRunning = false;
	}
	
	@Subscribe
	public void onDataRx(final SerialDataRxEvent evt) {
		if (!isMotorRunning) return;
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				float scale = config.displays[0].scale;
				int curValue = (evt.getByte(0)<<8)|evt.getByte(1);
				curValue = (short)curValue;  // make signed
				calcMass((float)curValue*scale);
			}
		});
	}
	
	private void calcMass(float diameter) {
		delay = (System.currentTimeMillis()-prevStepTime)/1000.0f;  // convert to seconds
		prevStepTime=System.currentTimeMillis();
		if (delay>1) return;

		float density = 0;
		if (rbPcabs.getSelection()) density = config.pcabsDensity;
		else if (rbHtpla.getSelection()) density = config.htplaDensity;
		else if (rbCfpla.getSelection()) density = config.cfplaDensity;

		float length = (refMotor.getSpeed()*delay)*2.54f;     // convert inch/second to cm
		float radius = diameter/20;                           // convert to cm
		float volume = length*(radius*radius*3.14159f);
		grams+=volume*density;
		
		if (rb250g.getSelection()) {
			if (grams>250) grams=0;
			updateAudio250g();
		}
		else if (rb1kg.getSelection()) {
			if (grams>1000) grams=0;
			updateAudio1kg();
		}
		
		lblData.setText(String.format("%.2f g", grams));
		prevGrams = grams;

	}
	
	private void updateAudio250g() {
		if (checkpoint(200)) am.playClip("200g");
		else if (checkpoint(220)) am.playClip("220g");
		else if (checkpoint(240)) am.playClip("240g");
		else if (checkpoint(245)) am.playClip("5");
		else if (checkpoint(246)) am.playClip("4");
		else if (checkpoint(247)) am.playClip("3");
		else if (checkpoint(248)) am.playClip("2");
		else if (checkpoint(249)) am.playClip("1");
		else if (checkpoint(0)) am.playClip("mark");
	}

	private void updateAudio1kg() {
		if (checkpoint(950)) am.playClip("950g");   
		else if (checkpoint(970)) am.playClip("970g");
		else if (checkpoint(990)) am.playClip("990g");
		else if (checkpoint(995)) am.playClip("5");
		else if (checkpoint(996)) am.playClip("4");
		else if (checkpoint(997)) am.playClip("3");
		else if (checkpoint(998)) am.playClip("2");
		else if (checkpoint(999)) am.playClip("1");
		else if (checkpoint(0)) am.playClip("mark");
	}
	
	private boolean checkpoint(float ref) {
		if (ref==0 && (grams+10)<prevGrams) return true;
		else if (ref!=0 && prevGrams<=ref && grams>=ref) return true;
		else return false;
	}
	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public void test() {
//		config.cfplaDensity = 30;
//		grams = 242;
//		prevGrams = grams;
//		lblData.setText(String.format("%.2f g", grams));
//		am.playClip("mark");
	}
	
	public void _test() {
		if (rb250g.getSelection()) {
			if (rbPcabs.getSelection()) am.playClip("200g");
			else if (rbHtpla.getSelection()) am.playClip("240g");
			else if (rbCfpla.getSelection()) am.playClip("5");
		}
		else if (rb1kg.getSelection()) {
			if (rbPcabs.getSelection()) am.playClip("900g");
			else if (rbHtpla.getSelection()) am.playClip("990g");
			else if (rbCfpla.getSelection()) am.playClip("mark");
		}
	}
	
}
