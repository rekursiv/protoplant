package com.protoplant.xtruder;

import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Injector;


public class MotorPanel extends Group {
	
	protected Logger log;
	protected EventBus eb;
	protected StSmc st;
	
	protected Button btnStop;
	protected Button btnRun;
	protected Label lblSpeed;
	protected boolean isFocused = false;
	protected String label;


	protected RGB focusColor = new RGB(180, 200, 220);
	protected RGB stopColor = new RGB(220, 180, 180);
	protected RGB runColor = new RGB(180, 220, 180);
	
	protected boolean isRunning = false;
	protected int speedMotorUnits = 0;
	protected int maxSpeedMotorUnits = 150000;
	protected float speed = 0;
	protected float speedScaleFactor = 1000;

	protected int boardIndex;
	protected XtruderConfig config;
	
	
	public MotorPanel(Composite parent, Injector injector, int boardIndex, String label) {
		super(parent, SWT.NONE);

		this.boardIndex = boardIndex;
		this.setLabel(label);
		setText("("+boardIndex+") "+label);
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent arg0) {
				setFocus();
			}
		});
		setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
		setLayout(null);
		
		btnStop = new Button(this, SWT.NONE);
		btnStop.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent arg0) {
				setFocus();
			}
		});

		btnStop.setBackgroundImage(null);
		btnStop.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				stop();
			}
		});
		btnStop.setBounds(10, 33, 75, 51);
		btnStop.setText("Stop");
		btnStop.setFont(SWTResourceManager.getFont("Segoe UI", 15, SWT.NORMAL));
		
		btnRun = new Button(this, SWT.NONE);
		btnRun.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent arg0) {
				setFocus();
			}
		});
		btnRun.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				start();
			}
		});
		btnRun.setBounds(111, 33, 75, 51);
		btnRun.setText("Run");
		btnRun.setFont(SWTResourceManager.getFont("Segoe UI", 15, SWT.NORMAL));
		
		lblSpeed = new Label(this, SWT.BORDER);
		lblSpeed.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent event) {
				setFocus();
				
			  ////////////////    TEST
//				if (event.button==1)setSpeed(speed+1);
//				else setSpeed(speed-1);  
			  ////////////////    ////////////
			}
		});
		lblSpeed.setFont(SWTResourceManager.getFont("Segoe UI", 15, SWT.NORMAL));
		lblSpeed.setBounds(206, 43, 130, 30);
		lblSpeed.setText("0");

		
//		btnStop.setBackground(SWTResourceManager.getColor(stopColor));
//		btnRun.setBackground(SWTResourceManager.getColor(runColor));
		lblSpeed.setBackground(SWTResourceManager.getColor(stopColor));
		
		if (injector!=null) injector.injectMembers(this);

	}
	
	
	@Inject
	public void inject(Logger log, EventBus eb, StSmc st, XtruderConfig config) {
		this.log = log;
		this.eb = eb;
		this.st = st;
		this.config = config;
		speedScaleFactor = config.motors[boardIndex].speedScale;
		setSpeed(config.motors[boardIndex].speedInit, false);
	}
	
	@Subscribe
	public void onMpgStepEvent(MpgStepEvent event) {
		if (isFocused) {
			int step = event.getStep();
			int speedMtr = speedMotorUnits+step;
			if (speedMtr<0) speedMtr=0;
			else if (speedMtr>maxSpeedMotorUnits) speedMtr=maxSpeedMotorUnits;
			
			float speedDisp = ((float)speedMtr)/speedScaleFactor;
			setSpeed(speedDisp);
		}
	}
	
	@Subscribe
	public void onPanelFocus(PanelFocusEvent event) {
		if (event.getWidget()==this) {
			isFocused = true;
			setBackground(SWTResourceManager.getColor(focusColor));
		} else {
			isFocused = false;
			setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		}
	}
	
	@Override
	public boolean setFocus() {
		eb.post(new PanelFocusEvent(this));
		return true;
	}
	
	public void nudgeSpeed(float delta) {
		setSpeed(speed+delta);
	}

	public void setSpeed(float newSpeed) {
		setSpeed(newSpeed, true);
	}
	
	public void setSpeed(float newSpeed, boolean sendChangeEvent) {
		if (newSpeed!=speed) {
			speed=newSpeed;
			speedMotorUnits=(int)(newSpeed*speedScaleFactor);
			if (speedMotorUnits<1) {
				speedMotorUnits=1;
				speed=1/speedScaleFactor;
			}
			else if (speedMotorUnits>maxSpeedMotorUnits) {
				speedMotorUnits=maxSpeedMotorUnits;
				speed=speedMotorUnits/speedScaleFactor;
			}
			lblSpeed.setText(String.format("%.4f", speed));
			setMotorSpeed();
			if (sendChangeEvent) eb.post(new MotorStateChangeEvent(this, MotorState.SPEED));
		}
	}
	
	public void setMotorSpeed() {
		if (isRunning) {
			st.setCurBoardIndex(boardIndex);
			st.run(speedMotorUnits);
		}
	}
	
	public float getSpeed() {
		return speed;
	}
	
	public void start() {
		start(true);
	}
	
	public void start(boolean sendChangeEvent) {
		isRunning = true;
		lblSpeed.setBackground(SWTResourceManager.getColor(runColor));
		startMotor();
		if (sendChangeEvent) eb.post(new MotorStateChangeEvent(this, MotorState.RUN));
	}
	
	public void startMotor() {
		st.setCurBoardIndex(boardIndex);
		st.run(speedMotorUnits);
	}
	
	public void stop() {
		stop(true);
	}
	
	public void stop(boolean sendChangeEvent) {
		isRunning = false;
		lblSpeed.setBackground(SWTResourceManager.getColor(stopColor));
		stopMotor();
		if (sendChangeEvent) eb.post(new MotorStateChangeEvent(this, MotorState.STOP));
	}
	
	public void stopMotor() {
		st.setCurBoardIndex(boardIndex);
		st.hiZ();
//		st.hold();
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}


	public String getLabel() {
		return label;
	}


	public void setLabel(String label) {
		this.label = label;
	}



}
