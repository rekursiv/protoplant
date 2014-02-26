package com.protoplant.tgif;

import java.util.logging.Level;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import util.logging.LogSetup;
import util.logging.LogView;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Injector;
import com.protoplant.tgif.event.DataInputEvent;
import com.protoplant.tgif.event.DataOutputEvent;

public class DebugPanel extends Composite {

	private LogView logView;
	private StyledText txtInput;
	private StyledText txtOutput;
	private Group grpLog;
	private Group grpInput;
	private Group grpOutput;

	public DebugPanel(Composite parent, int style, Injector injector) {
		super(parent, style);
		setLayout(new FillLayout(SWT.VERTICAL));
		
		grpInput = new Group(this, SWT.NONE);
		grpInput.setText("Input From TinyG");
		grpInput.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		txtInput = new StyledText(grpInput, SWT.BORDER | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL);
		txtInput.setAlwaysShowScrollBars(false);
		
		grpOutput = new Group(this, SWT.NONE);
		grpOutput.setText("Output To TinyG");
		grpOutput.setLayout(new FillLayout(SWT.HORIZONTAL));
		txtOutput = new StyledText(grpOutput, SWT.BORDER | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL);
		txtOutput.setAlwaysShowScrollBars(false);
		
		grpLog = new Group(this, SWT.NONE);
		grpLog.setText("Log");
		grpLog.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		logView = new LogView(grpLog, SWT.NONE);
		LogSetup.initView(logView, Level.ALL);

		if (injector!=null) injector.injectMembers(this);
		
	}
	
	@Subscribe
	public void onDataOutput(DataOutputEvent evt) {
		txtOutput.append(evt.getData());
		txtOutput.setTopIndex(txtOutput.getLineCount()-1);
	}
	
	@Subscribe
	public void onDataInput(DataInputEvent evt) {
		txtInput.append(evt.getData());
		txtInput.setTopIndex(txtInput.getLineCount()-1);
	}
	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
