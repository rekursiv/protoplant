package com.protoplant.xtruder;

import java.util.ArrayList;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;

import com.google.inject.Injector;

public class WinderMinderPanel extends MotorPanel implements Runnable {

	private volatile boolean isMovingForward = true;
	private volatile boolean isPolling = false;
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

		pollMotor();
		
		if (isPolling) {
			thread = new Thread(this);
			thread.start();
		}
	}

	
	private void pollMotor() {
		st.setCurBoardIndex(boardIndex);
		ArrayList<Integer> bytes = st.getStatus();
		if ((bytes.get(1).intValue() & 0x02) == 0) {
//			log.info("BUSY "+st.toBinary(bytes.get(1).intValue()));
		} else {
//			log.info("READY "+st.toBinary(bytes.get(1).intValue()));
			if (isMovingForward) {
				st.goToPos(9000);
				isMovingForward = false;
			} else {
				st.goToPos(-9000);
				isMovingForward = true;
			}
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
		// do nothing
	}
	
	public void setMotorSpeedAbsPos() {
		st.setCurBoardIndex(boardIndex);
		st.setMaxSpeed(20);
	}
	
	@Override
	public void startMotor() {
		setMotorSpeedAbsPos();
		isMovingForward = true;
		startPolling();
//		st.run(speedMotorUnits);
	}
	
	@Override
	public void stopMotor() {
		stopPolling();
		try {Thread.sleep(210);} catch (InterruptedException e) {}
		st.setCurBoardIndex(boardIndex);
		st.hiZ();
//		st.hold();

	}
	
	public void test() {
		st.setCurBoardIndex(boardIndex);
		st.dumpStatus();
	}

}
