package com.protoplant.toolsetter;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.layout.RowLayout;

public class VarPanel extends Composite {
	private Label label;
	private Spinner spinner;


	public VarPanel(Composite parent, String labelText, int selection, int minimum, int maximum, int digits, int increment, int pageIncrement) {
		super(parent, SWT.NONE);

		RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
		rowLayout.center = true;
		rowLayout.marginWidth = 3;
		rowLayout.spacing = 10;
		setLayout(rowLayout);
		
		label = new Label(this, SWT.NONE);
		label.setText(labelText);
		
		spinner = new Spinner(this, SWT.BORDER);
		spinner.setValues(selection, minimum, maximum, digits, increment, pageIncrement);
	}
	
	public VarPanel(Composite parent, String labelText, int selection) {
		this(parent, labelText, selection, -999999, 999999, 3, 1000, 1);
	}
	
	/**
	 * @wbp.parser.constructor
	 */
	public VarPanel(Composite parent, String labelText) {
		this(parent, labelText, 0, -999999, 999999, 3, 1000, 1);
//		this(parent, labelText, 77, 0, 99, 1, 1, 1);
	}
	
	public void setText(String text) {
		label.setText(text);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public double getDoubleValue() {
		int selection = spinner.getSelection();
		int digits = spinner.getDigits();
		return (selection / Math.pow(10, digits));
	}
	
	public int getIntValue() {
		return spinner.getSelection();
	}
}
