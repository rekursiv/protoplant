package com.protoplant.xtruder;

import java.util.ArrayList;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.google.inject.Injector;

public class WinderMinderPanel extends MotorPanel implements Runnable {

	private volatile boolean isMovingForward = true;
	private volatile boolean isPolling = false;
	private volatile boolean switchDetected = false;
	private volatile Thread thread = null;

	public WinderMinderPanel(Composite parent, Injector injector, int boardIndex, String label) {
		super(parent, injector, boardIndex, label);
		addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent arg0) {
				stopPolling();
			}
		});
	}
	
	public void destroy() {
		isPolling = false;
		if (thread!=null) thread.interrupt();
	}

	@Override
	public void run() {
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			return;
		}

		doAsncPoll();
		
		if (isPolling) {
			thread = new Thread(this);
			thread.start();
		}
	}

	
	private void doAsncPoll() {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				pollMotorController();
			}
		});
	}
	
	private void pollMotorController() {
		st.setCurBoardIndex(boardIndex);
		ArrayList<Integer> bytes = st.getStatus();
		
		if (!switchDetected && st.isSwitchOn(bytes) && !st.isBusy(bytes)) {
			log.info("SWITCH ON   "+st.toBinary(bytes.get(1).intValue()));
			switchDetected = true;
			isMovingForward = !isMovingForward;
			setMotorSpeed();
		} else if (switchDetected && !st.isSwitchOn(bytes) && st.isBusy(bytes)) {
			log.info("SWITCH OFF  "+st.toBinary(bytes.get(1).intValue()));
			switchDetected = false;
		}
	}
	
	public void startPolling() {
		if (!isPolling) {
			isPolling = true;
			thread = new Thread(this);
			thread.start();
		}
	}
	
	public void stopPolling() {
		isPolling = false;
	}

	@Override
	public void setMotorSpeed() {
		if (isRunning) {
			st.setCurBoardIndex(boardIndex);
			int speed = speedMotorUnits;
			if (!isMovingForward) speed = -speed;
			st.run(speed);
		}
	}
	
	@Override
	public void startMotor() {
		st.setCurBoardIndex(boardIndex);
		ArrayList<Integer> bytes = st.getStatus();
		if ((bytes.get(1).intValue() & 0x04) == 0) {  // don't start motor if switch engaged
			isMovingForward = true;
			isRunning = true;
			setMotorSpeed();
			startPolling();
		}
	}
	
	@Override
	public void stopMotor() {
		stopPolling();
		try {Thread.sleep(210);} catch (InterruptedException e) {}
		st.setCurBoardIndex(boardIndex);
		st.hiZ();
	}
	
	public void test() {
		st.setCurBoardIndex(boardIndex);
		st.dumpStatus();
	}

}
