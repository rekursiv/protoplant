package com.protoplant.xtruder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.codeminders.hidapi.ClassPathLibraryLoader;
import com.codeminders.hidapi.HIDDeviceInfo;
import com.codeminders.hidapi.HIDManager;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

@Singleton
public class UsbManager extends Thread {

	private static final int TIMER_PERIOD = 100;
	private Logger log;
	private Injector injector;
	private List<UsbModule> modules;

	@Inject
	public UsbManager(Logger log, Injector injector) {
		this.log = log;
		this.injector = injector;
	}
	
	public void init() {
		if (!ClassPathLibraryLoader.loadNativeHIDLibrary()) {
			log.warning("Failed to load native HID library.");
		}
		
		listDevs();
		initModules();
        start();
		
	}
	
	private void initModules() {
		modules = new ArrayList<UsbModule>();
		modules.add(injector.getInstance(IndicatorModule.class));
		modules.add(injector.getInstance(AnalogModule.class));
	}
	
	private void listDevs() {
		try {
			HIDDeviceInfo[] devs = HIDManager.getInstance().listDevices();
			for (HIDDeviceInfo info : devs) {
				if (info.getVendor_id()==0x2047) log.info(info.toString());
//				log.info(info.toString());
			}
		} catch (IOException e) {
			log.log(Level.WARNING, "", e);
		}
	}

	@Override
	public void run() {
		while (isAlive()) {
			for (UsbModule mod : modules) {
				mod.refreshWrite();
			}
			try {
				Thread.sleep(TIMER_PERIOD);
			} catch (InterruptedException e) {
				log.info("thread interrupt");
				return;
			}
			for (UsbModule mod : modules) {
				mod.refreshRead();
			}
		}
	}
	

	public void release() {
		interrupt();
		for (UsbModule mod : modules) {
			mod.disconnect();
		}
		try {
			HIDManager.getInstance().release();
		} catch (IOException e) {
			log.log(Level.WARNING, "", e);
		}
	}
}
