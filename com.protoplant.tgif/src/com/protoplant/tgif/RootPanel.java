package com.protoplant.tgif;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.google.inject.Injector;

public class RootPanel extends Composite {
	private SashForm sashForm;
	private DebugPanel debugPanel;
	private TabFolder tabFolder;
	private TabItem tbtmCmd;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public RootPanel(Composite parent, int style, Injector injector) {
		super(parent, style);
		setLayout(new FillLayout(SWT.HORIZONTAL));
		
		sashForm = new SashForm(this, SWT.NONE);
		
		tabFolder = new TabFolder(sashForm, SWT.NONE);

		SnippetPanel sp = new SnippetPanel(tabFolder, SWT.NONE, injector);
		tbtmCmd = new TabItem(tabFolder, SWT.NONE);
		tbtmCmd.setText("Snippet");
		tbtmCmd.setControl(sp);

		
		
//		composite_1 = new Composite(sashForm, SWT.NONE);
//		composite_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_DARK_CYAN));
//		sashForm.setWeights(new int[] {1, 1});
		
		debugPanel = new DebugPanel(sashForm, SWT.NONE, injector);

	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
