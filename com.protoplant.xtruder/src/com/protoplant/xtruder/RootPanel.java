package com.protoplant.xtruder;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import util.logging.LogSetup;
import util.logging.LogView;

import com.google.inject.Injector;

public class RootPanel extends Composite {
//	private Logger log;

	private ControlPanel ctlPanel = null;

	private LogView logView;

	private StatusPanel statusPanel;


	public RootPanel(Composite parent, int style, Injector injector) {
		super(parent, style);
		
		ctlPanel = new ControlPanel(this, injector);

		setLayout(new FormLayout());

		int statusPanelTop = 12;
		int statusPanelHeight = 25;
		
//		if (true) {  // for WindowBuilder
		if (injector!=null) {
			XtruderConfig cfg = injector.getInstance(XtruderConfig.class);
			if (cfg.showLogView) {
				logView = new LogView(this, SWT.NONE);
				FormData fd_logView = new FormData();
				fd_logView.right = new FormAttachment(100, -12);
				fd_logView.bottom = new FormAttachment(0, 130);
				fd_logView.top = new FormAttachment(0, 12);
				fd_logView.left = new FormAttachment(0, 12);
				logView.setLayoutData(fd_logView);
				LogSetup.initView(logView, Level.ALL);
				statusPanelTop = 136;
			}
		}
		
		statusPanel = new StatusPanel(this, injector);
		FormData fd_statusPanel = new FormData();
		fd_statusPanel.right = new FormAttachment(100, -12);
		fd_statusPanel.top = new FormAttachment(0, statusPanelTop);
		fd_statusPanel.bottom = new FormAttachment(0, statusPanelTop+statusPanelHeight);
		fd_statusPanel.left = new FormAttachment(0, 12);
		statusPanel.setLayoutData(fd_statusPanel);
		
		FormData fd_ctlPanel = new FormData();
		fd_ctlPanel.left = new FormAttachment(0, 0);
		fd_ctlPanel.right = new FormAttachment(100, 0);
		fd_ctlPanel.bottom = new FormAttachment(100, 0);
		fd_ctlPanel.top = new FormAttachment(statusPanel, 6);
		ctlPanel.setLayoutData(fd_ctlPanel);

	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
