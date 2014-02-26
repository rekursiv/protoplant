package com.protoplant.xtruder;

import java.util.logging.Logger;

import org.eclipse.swt.widgets.Display;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

public class StepTest implements Runnable {

	
	private Logger log;
	private EventBus eb;
	private boolean isActive = true;

	@Inject
	public StepTest(Logger log, EventBus eventBus) {
		this.log = log;
		this.eb = eventBus;
		new Thread(this).start();
	}
	
	@Override
	public void run() {
	
//		System.out.print("*");
		
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				eb.post(new MpgStepEvent(1));
			}
		});
		
		
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			return;
		}
		if (isActive) new Thread(this).start();
	}
	
	public void destroy() {
		isActive = false;
	}

}
