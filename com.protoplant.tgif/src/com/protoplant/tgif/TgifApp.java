package com.protoplant.tgif;


import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class TgifApp {

	/**
	 * Entry point
	 * @param args
	 */
	public static void main(String[] args) {
		TgifShell.setProperties();
		TgifApp instance = new TgifApp();
		instance.init();
	}
	
	protected void init() {
		TgifShell cs = new TgifShell();
		cs.setSize(1000, 600);
		cs.setText("TinyG InterFace");
		cs.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		Injector injector = Guice.createInjector(new TgifModule());
		
		new RootPanel(cs, SWT.NONE, injector);
		
		cs.init(injector);
				
	}
	
}
