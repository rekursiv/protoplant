package com.protoplant.xtruder;

import java.util.logging.Logger;

import org.eclipse.swt.SWT;
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


public class LinkPanel extends Group {
	
	private Logger log;
	private EventBus eb;
	
	private Button btnUnlink;
	private Button btnLink;
	private Label lblRatio;
	private boolean isFocused = false;

	private RGB unlinkColor = new RGB(220, 180, 180);
	private RGB linkColor = new RGB(180, 220, 180);
	
	private boolean isLinked = false;
	

	private MotorPanel srcMotor;
	private MotorPanel dstMotor;
	
	float ratio;
	
	


	public LinkPanel(Composite parent, Injector injector, MotorPanel srcMotor, MotorPanel dstMotor) {
		super(parent, SWT.NONE);

		this.srcMotor = srcMotor;
		this.dstMotor = dstMotor;
		
		
		setText("Link from "+srcMotor.getLabel()+" to "+dstMotor.getLabel());
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent arg0) {
				setFocus();
			}
		});
		setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
		setLayout(null);
		
		btnUnlink = new Button(this, SWT.NONE);

		btnUnlink.setBackgroundImage(null);
		btnUnlink.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				unlink();
			}
		});
		btnUnlink.setBounds(10, 33, 85, 51);
		btnUnlink.setText("Unlink");
		btnUnlink.setFont(SWTResourceManager.getFont("Segoe UI", 15, SWT.NORMAL));
		
		btnLink = new Button(this, SWT.NONE);
		btnLink.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				link();
			}
		});
		btnLink.setBounds(112, 33, 75, 51);
		btnLink.setText("Link");
		btnLink.setFont(SWTResourceManager.getFont("Segoe UI", 15, SWT.NORMAL));
		
		lblRatio = new Label(this, SWT.BORDER);

		lblRatio.setFont(SWTResourceManager.getFont("Segoe UI", 15, SWT.NORMAL));
		lblRatio.setBounds(202, 43, 130, 30);
		lblRatio.setText("0");

		
//		btnStop.setBackground(SWTResourceManager.getColor(stopColor));
//		btnRun.setBackground(SWTResourceManager.getColor(runColor));
		lblRatio.setBackground(SWTResourceManager.getColor(unlinkColor));
		
		if (injector!=null) injector.injectMembers(this);

	}
	
	
	@Inject
	public void inject(Logger log, EventBus eb) {
		this.log = log;
		this.eb = eb;
		calcRatio();
	}
	

	
	@Subscribe
	public void onMotorSpeedChange(MotorStateChangeEvent event) {
		if (event.getPanel()==dstMotor) {
			if (event.getState()==MotorState.SPEED) calcRatio();
		} else if (event.getPanel()==srcMotor) {
			if (event.getState()==MotorState.SPEED) {
				if (isLinked && ratio>=0) {
					float speed = srcMotor.getSpeed()*ratio;
					dstMotor.setSpeed(speed, false);
				} else {
					calcRatio();
				}
			} else if (isLinked) {
				if (event.getState()==MotorState.RUN) dstMotor.start();
				else dstMotor.stop();
			}
		}
	}
	
	protected void calcRatio() {
		float srcSpeed = srcMotor.getSpeed();
		float dstSpeed = dstMotor.getSpeed();
		if (srcSpeed==0) {
			ratio = -1;
			lblRatio.setText("-");
		}
		else {
			ratio = dstSpeed/srcSpeed;
			lblRatio.setText(String.format("%.4f", ratio));
		}
		
	}
	
	
	public void link() {
		if (!isLinked) {
			isLinked = true;
			lblRatio.setBackground(SWTResourceManager.getColor(linkColor));
		}
	}
	
	public void unlink() {
		if (isLinked) {
			isLinked = false;
			lblRatio.setBackground(SWTResourceManager.getColor(unlinkColor));
		}

	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
