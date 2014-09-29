package com.protoplant.xtruder;

import java.io.File;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.protoplant.xtruder.audio.AudioManager;
import com.protoplant.xtruder.audio.HopperAlarm;

public class XtruderApp {
	
	
	public static void main(String[] args) {
		//runRemote();
		System.getProperties().setProperty("java.util.logging.config.class", "util.logging.LogSetup");
		XtruderApp instance = new XtruderApp();
		instance.init();
//		instance.test();
		System.out.println("Bye.");
		System.exit(0);
	}
	
	private static void runRemote() {
		if (System.getProperty("os.name").contains("Windows")) {
			System.out.println("Detected Windows, will attempt to run on remote system...");
			ProcessBuilder pb = new ProcessBuilder("java", "-jar", "runremote.jar");
			pb.directory(new File("C:/projects/eclipse_workspace/_deploy/runremote/"));
			pb.inheritIO();
			try {
				pb.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.exit(0);
		}
	}
	
	protected void init() {
		XtruderShell shell = new XtruderShell();
		
		// splash screen
		if (System.getProperty("os.arch").equals("arm")) {
			shell.setSize(600, 0);
			shell.setText("Loading Protoplant Xtruder, please wait...");
			shell.open();
		}
		

		// ChalkElec touchscreen is 1280 X 800
		shell.setSize(1280, 800);

		// Dell touchscreen is 1600 X 900
//		shell.setSize(1600, 900);
		
		shell.setText("Protoplant Xtruder V1.8");

		shell.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		Injector injector = Guice.createInjector(new XtruderModule());
		
		new RootPanel(shell, SWT.NONE, injector);
		
		
		PiGpio mpg = injector.getInstance(PiGpio.class);
		
		StSmc smc = injector.getInstance(StSmc.class);
		smc.initSpi();
		smc.initAllBoards();
		
		AudioManager am = injector.getInstance(AudioManager.class);
		SerialPortManager spm = injector.getInstance(SerialPortManager.class);
		spm.init();
		DataLogger dl = injector.getInstance(DataLogger.class);
		dl.init();

		
		shell.init();  // main loop
		
		mpg.destroy();
		spm.destroy();
		am.destroy();
		dl.destroy();

	}

	
	
	
	protected void test() {
//		CmdLineTest clt = new CmdLineTest();
//		clt.init();
//		clt.loop();

		Injector injector = Guice.createInjector(new FastLoadModule());
		injector.getInstance(PiGpio.class);
		
		while (true) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
				break;
			}
		}
		
	}

}
