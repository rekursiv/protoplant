package com.protoplant.xtruder;

import java.util.logging.Logger;

import org.eclipse.swt.widgets.Display;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.wiringpi.Gpio;

//  TODO:  change name to "GPIO"
@Singleton
public class StepperatureInput implements GpioPinListenerDigital, Runnable {
	
	protected GpioController gpio=null;
	protected GpioPinDigitalInput stepA=null;
	protected GpioPinDigitalInput stepB=null;
	protected GpioPinDigitalInput laserOver=null;
	
	private volatile Thread thread = null;
	protected volatile long prevStepTime = 0;
	protected volatile int curStep = 0;
//	protected int prevStep = 0;
	private volatile boolean isActive = true;
	
	private Logger log;
	private EventBus eb;
	private GpioPinDigitalInput laserUnder;
	
	
	@Inject
	public StepperatureInput(Logger log, EventBus eb)  {
		
		this.log = log;
		this.eb = eb;
	
    	if (!System.getProperty("os.arch").equals("arm")) {
    		log.warning("Not running on Raspberry Pi Hardware, GPIO will not be initialized.");
    	} else {
        	gpio = GpioFactory.getInstance();
        	stepA = gpio.provisionDigitalInputPin(RaspiPin.GPIO_00, PinPullResistance.PULL_DOWN);
        	stepA.addListener(this);
        	stepB = gpio.provisionDigitalInputPin(RaspiPin.GPIO_07, PinPullResistance.PULL_DOWN);
        	stepB.addListener(this);
        	laserUnder = gpio.provisionDigitalInputPin(RaspiPin.GPIO_02, PinPullResistance.PULL_UP);
        	laserUnder.addListener(this);
        	laserOver = gpio.provisionDigitalInputPin(RaspiPin.GPIO_03, PinPullResistance.PULL_UP);
        	laserOver.addListener(this);
    	}
    	
		thread = new Thread(this);
		thread.start();
		
	}

	@Override
	public void run() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			return;
		}
		
		if (curStep!=0) {
			asyncPostStep(new MpgStepEvent(curStep));
			curStep=0;
		}
		
		if (isActive) {
			thread = new Thread(this);
			thread.start();
		}
	}
	
	
	public void destroy() {
		isActive  = false;
		if (thread!=null) thread.interrupt();
		try {Thread.sleep(200);} catch (InterruptedException e) {}  // give things a chance to shut down
		if (gpio!=null) gpio.shutdown();
		log.info("");
	}

	
	
	@Override
	public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
//		log.info(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = " + event.getState());
		if (event.getPin()==stepA && event.getState()==PinState.HIGH) {
			calcStep(1);
		} else if (event.getPin()==stepB && event.getState()==PinState.HIGH) {
			calcStep(-1);
		} else if (event.getPin()==laserUnder && event.getState()==PinState.HIGH) {
//			log.info("* LASER Under *");
			asyncPostLaser(new LaserEvent(false));
		} else if (event.getPin()==laserOver && event.getState()==PinState.HIGH) {
//			log.info("* LASER Over *");
			asyncPostLaser(new LaserEvent(true));
		}
	}
	
	private void calcStep(int dir) {
		long delay = Gpio.millis()-prevStepTime;
		prevStepTime=Gpio.millis();

		if (delay<3) curStep+=dir*1000;
		else if (delay<8) curStep+=dir*100;
		else if (delay<16) curStep+=dir*10;
		else if (delay<48) curStep+=dir*5;
		else if (delay<128) curStep+=dir*2;
		else curStep+=dir;

	}
	
	
	private void asyncPostLaser(final LaserEvent event) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				eb.post(event);
			}
		});
	}
	
	private void asyncPostStep(final MpgStepEvent event) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				eb.post(event);
			}
		});
	}

	
	// call this from a mouse wheel listener to simulate MPG when the real hardware is not available
	public void simulateStep(int dir) {
		if (dir>0) dir = 1;
		else dir = -1;
		long delay = System.currentTimeMillis()-prevStepTime;
		prevStepTime=System.currentTimeMillis();
		if (delay<30) curStep+=dir*1000;
		else if (delay<80) curStep+=dir*100;
		else if (delay<160) curStep+=dir*10;
		else if (delay<480) curStep+=dir*5;
		else if (delay<1280) curStep+=dir*2;
		else curStep+=dir;
	}
	
//	public void simulateStep(int dir) {
//		if (dir>0) asyncPostLaser(new LaserEvent(false));
//		else asyncPostLaser(new LaserEvent(true));
//	}


}
