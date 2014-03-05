package com.protoplant.xtruder;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class XtruderShell extends Shell {

	public void init() {
		Display display = Display.getDefault();
		open();
		layout();		
		while (!isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		try {Thread.sleep(200);} catch (InterruptedException e) {}  // give things a chance to shut down
		SWTResourceManager.dispose();
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
}