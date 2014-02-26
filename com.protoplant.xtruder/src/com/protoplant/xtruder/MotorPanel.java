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
	
	private Logger log;
	private EventBus eb;
	private StSmc st;
	
	private Button btnStop;
	private Button btnRun;
	private Label lblSpeed;
	private boolean isFocused = false;
	private String label;


	private RGB focusColor = new RGB(180, 200, 220);
	private RGB stopColor = new RGB(220, 180, 180);
	private RGB runColor = new RGB(180, 220, 180);
	
	private boolean isRunning = false;
	private int speedMotorUnits = 0;
	private int maxSpeedMotorUnits = 150000;
	private float speed = 0;
	private float speedScaleFactor = 1000;
//	private float speedScaleDelta = 0.000

	private int boardIndex;
	private XtruderConfig config;

	
	
	
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
				run();
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

	public void setSpeed(float newSpeed) {
		setSpeed(newSpeed, true);
	}
	
	public void setSpeed(float newSpeed, boolean sendChangeEvent) {
		if (newSpeed!=speed) {
			speed=newSpeed;
			speedMotorUnits=(int)(newSpeed*speedScaleFactor);
			if (speedMotorUnits<0) {
				speedMotorUnits=0;
				speed=0;
			}
			else if (speedMotorUnits>maxSpeedMotorUnits) {
				speedMotorUnits=maxSpeedMotorUnits;
				speed=speedMotorUnits/speedScaleFactor;
			}
			lblSpeed.setText(String.format("%.4f", speed));
			if (isRunning) {
				st.setCurBoardIndex(boardIndex);
				st.run(speedMotorUnits);
			}
			if (sendChangeEvent) eb.post(new MotorStateChangeEvent(this, MotorState.SPEED));
		}
	}
	
	public float getSpeed() {
		return speed;
	}
	
	public void run() {
		run(true);
	}
	
	public void run(boolean sendChangeEvent) {
		isRunning = true;
		lblSpeed.setBackground(SWTResourceManager.getColor(runColor));
		st.setCurBoardIndex(boardIndex);
		st.run(speedMotorUnits);
		if (sendChangeEvent) eb.post(new MotorStateChangeEvent(this, MotorState.RUN));
	}
	
	public void stop() {
		stop(true);
	}
	
	public void stop(boolean sendChangeEvent) {
		isRunning = false;
		lblSpeed.setBackground(SWTResourceManager.getColor(stopColor));
		st.setCurBoardIndex(boardIndex);
		st.hiZ();
//		st.hold();
		if (sendChangeEvent) eb.post(new MotorStateChangeEvent(this, MotorState.STOP));
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
