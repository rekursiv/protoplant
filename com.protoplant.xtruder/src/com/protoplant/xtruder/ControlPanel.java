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

public class ControlPanel extends Composite {
	private MotorPanel motor1;
	private MotorPanel motor2;
	private LinkPanel linkMotor2_3;
	private MotorPanel motor3;
	private MotorPanel motor4;
	private LinkPanel linkMotor2_4;
	private MotorPanel motor5;
	private SpoolWeightPanel spoolWeight;
	
	private Button btnStopAll;
	private Button btnRunAll;
	private Button btnReinitMotors;
	
	private StSmc st;
	private DataDisplayPanel data1;
	private DataDisplayPanel data2;
	private DataDisplayPanel data3;

	private Logger log;
	private EventBus eb;
	private StepperatureInput mpg;




	public ControlPanel(Composite parent, Injector injector) {
		super(parent, SWT.NONE);
//		setBackground(SWTResourceManager.getColor(SWT.COLOR_DARK_RED));
		
		motor1 = new MotorPanel(this, injector, 0, "Winder");
		motor1.setBounds(10, 121, 350, 105);
		motor2 = new MotorPanel(this, injector, 1, "Roller");
		motor2.setBounds(10, 10, 350, 105);
		motor3 = new MotorPanel(this, injector, 2, "Wheel #1");
		motor3.setBounds(759, 10, 350, 105);
		motor4 = new MotorPanel(this, injector, 3, "Wheel #2");
		motor4.setBounds(759, 121, 350, 105);
		
		motor5 = new MotorPanel(this, injector, 4, "Aux");
		motor5.setBounds(10, 232, 350, 105);
		
		linkMotor2_3 = new LinkPanel(this, injector, motor2, motor3);
		linkMotor2_3.setBounds(386, 10, 350, 105);
		linkMotor2_3.link();
		
		linkMotor2_4 = new LinkPanel(this, injector, motor2, motor4);
		linkMotor2_4.setBounds(386, 121, 350, 105);
		linkMotor2_4.link();
		
		spoolWeight = new SpoolWeightPanel(this, injector, motor2);
		spoolWeight.setBounds(386, 232, 723, 105);

		data1 = new DataDisplayPanel(this, injector, "Indicator", 0, true);
		data1.setBounds(10, 343, 350, 105);
		data2 = new DataDisplayPanel(this, injector, "Laser", 1, false);
		data2.setBounds(386, 343, 350, 105);
		data3 = new DataDisplayPanel(this, injector, "Pressure", 2, false);
		data3.setBounds(759, 343, 350, 105);

		btnStopAll = new Button(this, SWT.NONE);
		btnStopAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				motor1.stop(false);
				motor2.stop(false);
				motor3.stop(false);
				motor4.stop(false);
				motor5.stop(false);
				spoolWeight.stop();
			}
		});
		btnStopAll.setFont(SWTResourceManager.getFont("Segoe UI", 15, SWT.NORMAL));
		btnStopAll.setBounds(10, 463, 188, 58);
		btnStopAll.setText("Stop All");
		
		btnRunAll = new Button(this, SWT.NONE);
		btnRunAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				motor1.run(false);
				motor2.run(false);
				motor3.run(false);
				motor4.run(false);
				motor5.run(false);
				spoolWeight.start();
			}
		});
		btnRunAll.setFont(SWTResourceManager.getFont("Segoe UI", 15, SWT.NORMAL));
		btnRunAll.setBounds(250, 463, 154, 58);
		btnRunAll.setText("Run All");
		
		btnReinitMotors = new Button(this, SWT.NONE);
		btnReinitMotors.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				st.initAllBoards();
			}
		});
		btnReinitMotors.setText("Re-Init Motors");
		btnReinitMotors.setFont(SWTResourceManager.getFont("Segoe UI", 15, SWT.NORMAL));
		btnReinitMotors.setBounds(465, 463, 188, 58);
		
		//  for testing without Stepperature
		this.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseScrolled(MouseEvent evt) {
//				log.info(evt.toString());
				mpg.simulateStep(evt.count);
			}
		});
		
		if (injector!=null) injector.injectMembers(this);
	}
	
	@Inject
	public void inject(Logger log, EventBus eb, StSmc st, StepperatureInput mpg) {
		this.log = log;
		this.eb = eb;
		this.st = st;
		this.mpg = mpg;
	}
	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
