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
		SWTResourceManager.dispose();
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
}