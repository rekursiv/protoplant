package com.protoplant.xtruder;

import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Injector;

import org.eclipse.wb.swt.SWTResourceManager;

import util.config.ConfigManager;
import org.eclipse.swt.widgets.Group;

public class ControlPanel extends Composite {
	private MotorPanel motor1;
	private MotorPanel motor2;
	private LinkPanel linkMotor2_3;
	private MotorPanel motor3;
	private MotorPanel motor4;
	private LinkPanel linkMotor2_4;
	private WinderMinderPanel winderMinder;
	private CoilMassPanel coilMass;
	
	private Button btnStopAll;
	private Button btnRunAll;
	private Button btnReinitMotors;
	
	private StSmc st;
	private DataDisplayPanel data1;
	private DataDisplayPanel data2;
	private FaultPanel fault;

	private Logger log;
	private EventBus eb;
	private PiGpio io;
	private Button btnExit;
	private Button btnTest;
	private Button btnReloadConfig;
	private Group grpGlobalControls;



	public ControlPanel(Composite parent, Injector injector) {
		super(parent, SWT.NONE);
//		setBackground(SWTResourceManager.getColor(SWT.COLOR_DARK_RED));
		
		motor1 = new MotorPanel(this, injector, 0, "Winder");
		motor1.setBounds(739, 343, 350, 105);
		motor2 = new MotorPanel(this, injector, 1, "Roller");
		motor2.setBounds(373, 343, 350, 105);
		motor3 = new MotorPanel(this, injector, 2, "Wheel #1");
		motor3.setBounds(10, 121, 350, 105);
		motor4 = new MotorPanel(this, injector, 3, "Wheel #2");
		motor4.setBounds(10, 343, 350, 105);
		
		winderMinder = new WinderMinderPanel(this, injector, 4, "Winder Minder");
		winderMinder.setBounds(739, 454, 350, 105);
		
		linkMotor2_3 = new LinkPanel(this, injector, motor3, motor4);
		linkMotor2_3.setBounds(10, 232, 350, 105);
		linkMotor2_3.link();
		
		linkMotor2_4 = new LinkPanel(this, injector, motor4, motor2);
		linkMotor2_4.setBounds(373, 232, 350, 105);
		linkMotor2_4.link();
		
		coilMass = new CoilMassPanel(this, injector, motor2);
		coilMass.setBounds(739, 10, 350, 327);

		data1 = new DataDisplayPanel(this, injector, "Indicator", 0, true);
		data1.setBounds(373, 121, 350, 105);
		data2 = new DataDisplayPanel(this, injector, "Pressure", 1, false);
		data2.setBounds(10, 10, 350, 105);
		fault = new FaultPanel(this, injector);
		fault.setBounds(373, 10, 350, 105);
		
		/*
		
		btnReloadConfig = new Button(this, SWT.NONE);
		btnReloadConfig.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {   //  FIXME only reloads config for coil mass panel
				coilMass.reloadConfig();
			}
		});
		btnReloadConfig.setText("Reload Config");
		btnReloadConfig.setFont(SWTResourceManager.getFont("Segoe UI", 15, SWT.NORMAL));
		btnReloadConfig.setBounds(122, 601, 150, 58);
		*/
		
		
		grpGlobalControls = new Group(this, SWT.NONE);
		grpGlobalControls.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
		grpGlobalControls.setText("Global Controls");
		grpGlobalControls.setBounds(10, 458, 713, 101);
		
				btnStopAll = new Button(grpGlobalControls, SWT.NONE);
				btnStopAll.setBounds(10, 33, 120, 58);
				btnStopAll.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent arg0) {
						stopAll();
					}
				});
				btnStopAll.setFont(SWTResourceManager.getFont("Segoe UI", 15, SWT.NORMAL));
				btnStopAll.setText("Stop All");
				
				btnRunAll = new Button(grpGlobalControls, SWT.NONE);
				btnRunAll.setBounds(136, 33, 120, 58);
				btnRunAll.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent arg0) {
						startAll();
					}
				});
				btnRunAll.setFont(SWTResourceManager.getFont("Segoe UI", 15, SWT.NORMAL));
				btnRunAll.setText("Run All");
				
				btnReinitMotors = new Button(grpGlobalControls, SWT.NONE);
				btnReinitMotors.setBounds(262, 33, 150, 58);
				btnReinitMotors.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent arg0) {
						st.initAllBoards();
					}
				});
				btnReinitMotors.setText("Re-Init Motors");
				btnReinitMotors.setFont(SWTResourceManager.getFont("Segoe UI", 15, SWT.NORMAL));
				
				btnTest = new Button(grpGlobalControls, SWT.NONE);
				btnTest.setBounds(541, 33, 78, 58);
				btnTest.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent arg0) {
						coilMass.test();
					}
				});
				btnTest.setFont(SWTResourceManager.getFont("Segoe UI", 15, SWT.NORMAL));
				btnTest.setText("Test");
				
				btnExit = new Button(grpGlobalControls, SWT.NONE);
				btnExit.setBounds(625, 33, 78, 58);
				btnExit.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent arg0) {
						stopAll();
						winderMinder.destroy();
						getShell().dispose();
					}
				});
				btnExit.setText("Exit");
				btnExit.setFont(SWTResourceManager.getFont("Segoe UI", 15, SWT.NORMAL));
		
		//  for testing without Stepperature
		this.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseScrolled(MouseEvent evt) {
//				log.info(evt.toString());
				io.simulateStep(evt.count);
			}
		});

		if (injector!=null) injector.injectMembers(this);
	}
	
	@Inject
	public void inject(Logger log, EventBus eb, StSmc st, PiGpio io) {
		this.log = log;
		this.eb = eb;
		this.st = st;
		this.io = io;
	}
	
	public void startAll() {
		motor1.start(false);
		motor2.start(false);
		motor3.start(false);
		motor4.start(false);
		winderMinder.start(false);
		coilMass.start();
	}
	
	public void stopAll() {
		motor1.stop(false);
		motor2.stop(false);
		motor3.stop(false);
		motor4.stop(false);
		winderMinder.stop(false);
		coilMass.stop();
	}

	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
