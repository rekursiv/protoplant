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
import org.eclipse.swt.widgets.Spinner;


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
	private Button btnResetMass;
	private Group grpMaterial;
	private Group grpReset;
	
	private XtruderConfig config;
	private AudioManager am;
	private ConfigManager<XtruderConfig> cfgMgr;
	private Spinner spnCount;
	private Label lblCount;
	private Button btnResetCount;
		
	
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
		
		btnResetMass = new Button(this, SWT.NONE);
		btnResetMass.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				grams=0;
				lblData.setText("0.00 g");
			}
		});
		FormData fd_btnResetMass = new FormData();
		fd_btnResetMass.left = new FormAttachment(lblData, 6);
		fd_btnResetMass.bottom = new FormAttachment(lblData, 49);
		fd_btnResetMass.top = new FormAttachment(lblData, 0, SWT.TOP);
		fd_btnResetMass.right = new FormAttachment(100, -12);
		btnResetMass.setLayoutData(fd_btnResetMass);
		btnResetMass.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
		btnResetMass.setText("Reset Mass");
		
		grpMaterial = new Group(this, SWT.NONE);
		FormData fd_grpMaterial = new FormData();
		fd_grpMaterial.right = new FormAttachment(0, 260);
		fd_grpMaterial.left = new FormAttachment(0, 15);
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
		fd_lblData.right = new FormAttachment(0, 220);
		FormData fd_grpReset = new FormData();
		fd_grpReset.right = new FormAttachment(0, 165);
		fd_grpReset.left = new FormAttachment(0, 13);
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
		
		spnCount = new Spinner(this, SWT.BORDER);
		fd_grpReset.bottom = new FormAttachment(spnCount, 81, SWT.BOTTOM);
		fd_grpReset.top = new FormAttachment(spnCount, 6);
		spnCount.setFont(SWTResourceManager.getFont("Segoe UI", 24, SWT.NORMAL));
		spnCount.setMaximum(1000);
		FormData fd_spnCount = new FormData();
		fd_spnCount.bottom = new FormAttachment(0, 145);
		spnCount.setLayoutData(fd_spnCount);
		
		lblCount = new Label(this, SWT.NONE);
		lblCount.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
		fd_spnCount.top = new FormAttachment(lblCount, 6);
		fd_spnCount.right = new FormAttachment(0, 205);
		fd_spnCount.left = new FormAttachment(lblCount, 0, SWT.LEFT);
		FormData fd_lblCount = new FormData();
		fd_lblCount.right = new FormAttachment(0, 160);
		fd_lblCount.top = new FormAttachment(lblData);
		fd_lblCount.left = new FormAttachment(lblData, 0, SWT.LEFT);
		lblCount.setLayoutData(fd_lblCount);
		lblCount.setText("Coil Count");
		
		btnResetCount = new Button(this, SWT.NONE);
		btnResetCount.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				spnCount.setSelection(0);
			}
		});
		btnResetCount.setText("Reset Count");
		btnResetCount.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
		FormData fd_btnResetCount = new FormData();
		fd_btnResetCount.left = new FormAttachment(lblData, 6);
		fd_btnResetCount.right = new FormAttachment(btnResetMass, 0, SWT.RIGHT);
		fd_btnResetCount.bottom = new FormAttachment(0, 135);
		fd_btnResetCount.top = new FormAttachment(spnCount, 0, SWT.TOP);
		btnResetCount.setLayoutData(fd_btnResetCount);
		
		
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
			if (grams>250) {
				grams=0;
				incrementCount();
				am.playClip("mark");
			}
			updateAudio250g();
		}
		else if (rb1kg.getSelection()) {
			if (grams>1000) {
				grams=0;
				incrementCount();
				am.playClip("mark");
			}
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
//		else if (checkpoint(0)) am.playClip("mark");
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
//		else if (checkpoint(0)) am.playClip("mark");
	}
	
	private boolean checkpoint(float ref) {
		if (ref==0 && (grams+10)<prevGrams) return true;
		else if (ref!=0 && prevGrams<=ref && grams>=ref) return true;
		else return false;
	}
	
	private void incrementCount() {
		spnCount.setSelection(spnCount.getSelection()+1);
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
