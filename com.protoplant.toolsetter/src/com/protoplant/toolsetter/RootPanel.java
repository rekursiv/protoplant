package com.protoplant.toolsetter;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;


public class RootPanel extends Composite {
	private Text txtGcode;
	private VarPanel vpToolLength;
	private VarPanel vpWorkHeight;
	private VarPanel vpTsHeight;
	private VarPanel vpTsZ;
	private VarPanel vpTsY;
	private VarPanel vpTsX;
	private VarPanel vpTableZ;
	private VarPanel vpToolNum;
	private VarPanel vpZSafePlane;
	private VarPanel vpRebound;
	private VarPanel vpFastFeed;
	private VarPanel vpSlowFeed;
	private VarPanel vpFeedDistance;
	private VarPanel vpSafeDepth;
	private Button btnGenerateCode;
	private Button btnSaveDefaults;
	private Button btnEditCodeTemplate;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public RootPanel(Composite parent, int style) {
		super(parent, style);
		
		Image img = new Image(Display.getDefault(), App.class.getResourceAsStream("bg.png"));
		this.setBackgroundImage(img);
		setLayout(null);
		
		txtGcode = new Text(this, SWT.BORDER | SWT.MULTI);
		txtGcode.setBounds(10, 115, 379, 475);
		
		vpToolLength = new VarPanel(this, "Tool Length:", 3000);
		vpToolLength.setBounds(496, 177, 157, 28);
		
		vpWorkHeight = new VarPanel(this, "Work Height:", 5000);
		vpWorkHeight.setBounds(107, 636, 157, 28);
		
		vpTsHeight = new VarPanel(this, "TS Height:", 5000);
		vpTsHeight.setBounds(555, 640, 157, 28);
		
		vpTsZ = new VarPanel(this, "TS Z Position:", -10000);
		vpTsZ.setBounds(797, 360, 157, 28);
		
		vpTsY = new VarPanel(this, "TS Y Position:", -10000);
		vpTsY.setBounds(611, 430, 157, 28);
		
		vpTsX = new VarPanel(this, "TS X Position:", -10000);
		vpTsX.setBounds(611, 315, 157, 28);
		
		vpTableZ = new VarPanel(this, "Table Z Position:", -20000);
		vpTableZ.setBounds(325, 730, 185, 28);
		
		vpToolNum = new VarPanel(this, "Tool Number:", 1, 0, 99, 0, 1, 1);
		vpToolNum.setBounds(700, 261, 148, 28);
		
		vpZSafePlane = new VarPanel(this, "Z Safe Plane:", -10000);
		vpZSafePlane.setBounds(555, 10, 157, 28);
		
		vpRebound = new VarPanel(this, "Rebound:", 100);
		vpRebound.setBounds(691, 562, 157, 28);
		
		vpFastFeed = new VarPanel(this, "Fast Feed:", 30000);
		vpFastFeed.setBounds(10, 10, 179, 28);
		
		vpSlowFeed = new VarPanel(this, "Slow Feed:", 5000);
		vpSlowFeed.setBounds(195, 10, 157, 28);
		
		vpFeedDistance = new VarPanel(this, "Feed Distance:", 1000);
		vpFeedDistance.setBounds(10, 44, 179, 28);
		
		vpSafeDepth = new VarPanel(this, "Safe Depth:", 25);
		vpSafeDepth.setBounds(195, 44, 157, 28);
		
		btnGenerateCode = new Button(this, SWT.NONE);
		btnGenerateCode.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				genCode();
			}

		});
		btnGenerateCode.setBounds(9, 84, 99, 25);
		btnGenerateCode.setText("Generate Code");
		
		btnSaveDefaults = new Button(this, SWT.NONE);
		btnSaveDefaults.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				nyi();
			}
		});
		btnSaveDefaults.setBounds(136, 84, 99, 25);
		btnSaveDefaults.setText("Save Defaults");
		
		btnEditCodeTemplate = new Button(this, SWT.NONE);
		btnEditCodeTemplate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				nyi();
			}

		});
		btnEditCodeTemplate.setBounds(241, 84, 148, 25);
		btnEditCodeTemplate.setText("Edit Code Template");
	}

	private void nyi() {
		MessageBox mb = new MessageBox(this.getShell());
		mb.setMessage("Not Yet Implemented");
		mb.open();
	}
	
	private void genCode() {
		StringBuilder code = new StringBuilder();
		code.append("G90 G17 G40 G80;\n");
		code.append("M06 T"+vpToolNum.getIntValue()+" H"+vpToolNum.getIntValue()+";\n");
		code.append("G00 G53 Z"+vpZSafePlane.getDoubleValue()+";\n");
		code.append("G00 G53 X"+vpTsX.getDoubleValue()+" Y"+vpTsY.getDoubleValue()+";\n");
		code.append("G00 G53 Z"+(vpTsZ.getDoubleValue()+vpToolLength.getDoubleValue()+vpFeedDistance.getDoubleValue())+";\n");	
		code.append("G31 G49 G91 M79 Z"+(-(vpSafeDepth.getDoubleValue()+vpFeedDistance.getDoubleValue()))+" F"+vpFastFeed.getDoubleValue()+";\n");
		code.append("G00 G91 Z"+vpRebound.getDoubleValue()+";\n");
		code.append("G37 G43 G90 H"+vpToolNum.getIntValue()+" Z"+(vpTsHeight.getDoubleValue()-vpWorkHeight.getDoubleValue())+" F"+vpSlowFeed.getDoubleValue()+";\n");
		code.append("G00 G91 Z"+vpRebound.getDoubleValue()+";\n");
		code.append("G90;\n");
		code.append("M30;\n");
		txtGcode.setText(code.toString());
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
