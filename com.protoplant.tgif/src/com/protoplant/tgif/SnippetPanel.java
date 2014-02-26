package com.protoplant.tgif;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import util.logging.LogSetup;
import util.logging.LogView;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.protoplant.tgif.event.DataOutputEvent;
import com.protoplant.tgif.event.DebugEvent;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.custom.Bullet;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

public class SnippetPanel extends Composite {
	private EventBus eb;
	private StyledText text;
	private Button btnSendLine1;
	private Button btnSendLine2;
	private Logger log;
	private Group grpSend;
	private Button btnLoad;
	private Button btnSave;
	private Label lblFilename;
	private Button btnCont3;
	private Button btnCont2;
	private Button btnSendLine3;
	private Button btnSendSelection;
	private Button btnCont1;
	private Button btnReset;


	public SnippetPanel(Composite parent, int style, Injector injector) {
		super(parent, style);
		
		setLayout(new FormLayout());
		

		text = new StyledText(this, SWT.BORDER);
		text.setAlwaysShowScrollBars(false);
		FormData fd_txtSendMe = new FormData();
		fd_txtSendMe.top = new FormAttachment(0);
		fd_txtSendMe.right = new FormAttachment(100, 0);
		fd_txtSendMe.left = new FormAttachment(0);
		text.setLayoutData(fd_txtSendMe);

		text.addLineStyleListener(new LineStyleListener()
		{
		    public void lineGetStyle(LineStyleEvent e)
		    {
		        //Set the line number
		        e.bulletIndex = text.getLineAtOffset(e.lineOffset);
		
		        //Set the style
		        StyleRange style = new StyleRange();
		        style.metrics = new GlyphMetrics(0, 0, 25);
		        style.background = SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND);
		
		        //Create and set the bullet
		        e.bullet = new Bullet(ST.BULLET_NUMBER,style);
		    }
		});
		
		grpSend = new Group(this, SWT.NONE);
		fd_txtSendMe.bottom = new FormAttachment(grpSend, -6);
		grpSend.setText("Send");
		grpSend.setLayout(new FormLayout());
		FormData fd_grpSend = new FormData();
		fd_grpSend.right = new FormAttachment(100, 0);
		fd_grpSend.left = new FormAttachment(text, 0, SWT.LEFT);
		fd_grpSend.bottom = new FormAttachment(100, -45);
		fd_grpSend.top = new FormAttachment(100, -120);
		grpSend.setLayoutData(fd_grpSend);
		
		
		
		
		btnSendLine1 = new Button(grpSend, SWT.NONE);
		FormData fd_btnSendLine1 = new FormData();
		fd_btnSendLine1.top = new FormAttachment(0, 5);
		btnSendLine1.setLayoutData(fd_btnSendLine1);
		btnSendLine1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				sendLine(0, btnCont1.getSelection());
			}
		});
		btnSendLine1.setText("Line 1");
		
		
		
		btnSendLine2 = new Button(grpSend, SWT.NONE);
		FormData fd_btnSendLine2 = new FormData();
		fd_btnSendLine2.right = new FormAttachment(0, 140);
		fd_btnSendLine2.left = new FormAttachment(0, 97);
		btnSendLine2.setLayoutData(fd_btnSendLine2);
		btnSendLine2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				sendLine(1, btnCont2.getSelection());
			}
		});
		btnSendLine2.setText("Line 2");
		

		
		btnCont3 = new Button(grpSend, SWT.CHECK);
		fd_btnSendLine1.left = new FormAttachment(0, 10);
		FormData fd_btnCont3 = new FormData();
		btnCont3.setLayoutData(fd_btnCont3);
		btnCont3.setText("Continue");
		
		btnCont2 = new Button(grpSend, SWT.CHECK);
		fd_btnCont3.top = new FormAttachment(btnCont2, -16);
		fd_btnCont3.bottom = new FormAttachment(btnCont2, 0, SWT.BOTTOM);
		fd_btnSendLine2.bottom = new FormAttachment(btnCont2);

		btnCont2.setText("Continue");
		FormData fd_btnCont2 = new FormData();
		fd_btnCont2.top = new FormAttachment(btnSendLine2, 6);
		fd_btnCont2.right = new FormAttachment(btnSendLine2, 68);
		fd_btnCont2.left = new FormAttachment(btnSendLine2, 0, SWT.LEFT);
		btnCont2.setLayoutData(fd_btnCont2);
		
		btnSendLine3 = new Button(grpSend, SWT.NONE);
		btnSendLine3.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				sendLine(2, btnCont3.getSelection());
			}
		});
		btnSendLine3.setText("Line 3");
		FormData fd_btnSendLine3 = new FormData();
		fd_btnSendLine3.bottom = new FormAttachment(btnSendLine2, 25);
		fd_btnSendLine3.top = new FormAttachment(btnSendLine2, 0, SWT.TOP);
		fd_btnSendLine3.right = new FormAttachment(0, 235);
		fd_btnSendLine3.left = new FormAttachment(0, 192);
		btnSendLine3.setLayoutData(fd_btnSendLine3);
		
		btnSendSelection = new Button(grpSend, SWT.NONE);
		fd_btnCont3.right = new FormAttachment(0, 265);
		btnSendSelection.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				send(text.getSelectionText());
			}
		});
		btnSendSelection.setText("Selection");
		FormData fd_btnSendSelection = new FormData();
		fd_btnSendSelection.top = new FormAttachment(btnCont3, -25, SWT.TOP);
		fd_btnSendSelection.bottom = new FormAttachment(btnCont3);
		fd_btnSendSelection.right = new FormAttachment(0, 345);
		fd_btnSendSelection.left = new FormAttachment(0, 285);
		btnSendSelection.setLayoutData(fd_btnSendSelection);
		
		btnCont1 = new Button(grpSend, SWT.CHECK);
		btnCont1.setText("Continue");
		FormData fd_btnCont1 = new FormData();
		fd_btnCont1.top = new FormAttachment(btnCont2, -16);
		fd_btnCont1.bottom = new FormAttachment(btnCont2, 0, SWT.BOTTOM);
		fd_btnCont1.right = new FormAttachment(0, 80);
		btnCont1.setLayoutData(fd_btnCont1);
		
		btnLoad = new Button(this, SWT.NONE);
		btnLoad.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				showFileDialog(SWT.OPEN);
			}
		});
		FormData fd_btnLoad = new FormData();
		btnLoad.setLayoutData(fd_btnLoad);
		btnLoad.setText("Load");
		
		btnSave = new Button(this, SWT.NONE);
		btnSave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				showFileDialog(SWT.SAVE);
			}
		});
		FormData fd_btnSave = new FormData();
		fd_btnSave.bottom = new FormAttachment(100, -10);
		fd_btnSave.top = new FormAttachment(100, -35);
		fd_btnSave.left = new FormAttachment(btnLoad, 6);
		btnSave.setLayoutData(fd_btnSave);
		btnSave.setText("Save");
		
		lblFilename = new Label(this, SWT.NONE);
		fd_btnLoad.top = new FormAttachment(lblFilename, -25);
		fd_btnLoad.bottom = new FormAttachment(lblFilename, 0, SWT.BOTTOM);
		FormData fd_lblFilename = new FormData();
		fd_lblFilename.top = new FormAttachment(btnSave, 0, SWT.TOP);
		fd_lblFilename.right = new FormAttachment(100, -12);
		fd_lblFilename.bottom = new FormAttachment(100, -10);
		fd_lblFilename.left = new FormAttachment(0, 180);
		lblFilename.setLayoutData(fd_lblFilename);
		lblFilename.setText("Filename");
		
		btnReset = new Button(this, SWT.NONE);
		btnReset.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				send(new String(new char[] {(char)0x18}));
			}
		});
		fd_btnLoad.left = new FormAttachment(btnReset, 42);
		btnReset.setBackground(SWTResourceManager.getColor(SWT.COLOR_RED));
		FormData fd_btnReset = new FormData();
		fd_btnReset.top = new FormAttachment(btnLoad, 0, SWT.TOP);
		fd_btnReset.left = new FormAttachment(0, 12);
		btnReset.setLayoutData(fd_btnReset);
		btnReset.setText("Reset");

		if (injector!=null) injector.injectMembers(this);
		
	}
	
	@Inject
	public void inject(EventBus eb, Logger log) {
		this.eb = eb;
		this.log = log;
		loadFile(Paths.get(getSnippetDir()+"default.txt"));
	}
	
	public void send(String cmd) {
		if (cmd==null) return;
		if (cmd.isEmpty()) return;
		cmd = cmd.replace("\r", "");
		if (!cmd.endsWith("\n")) cmd += "\n";
		eb.post(new DataOutputEvent(cmd));
	}
	
	public void sendLine(int index, boolean cont) {
		if (cont) {
			StringBuilder sb = new StringBuilder();
			for (int i=index; i<text.getLineCount(); ++i) {
				String line = text.getLine(i);
				sb.append(line);
				if (!line.isEmpty()) sb.append("\n");
			}
			send(sb.toString());
		} else {
			send(text.getLine(index));
		}
	}

	private void showFileDialog(int type) {  //  SWT.OPEN
		FileDialog fd = new FileDialog(getShell(), type);
		fd.setFilterPath(getSnippetDir());
		fd.setFilterExtensions(new String[] {"*.txt"});
		String result = fd.open();
		if (result!=null&&!result.isEmpty()) {
			Path path = Paths.get(result);
			lblFilename.setText(path.getFileName().toString());
			if (type==SWT.OPEN) {
				loadFile(path);
			} else if (type==SWT.SAVE) {
				saveFile(path);
			}
		}
	}
	
	private void loadFile(Path path) {
		try {
			StringBuilder sb = new StringBuilder();
			for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
				if (sb.length()>0) sb.append("\n");
				sb.append(line);
			}
			text.setText(sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void saveFile(Path path) {
		String txt = text.getText().replace("\r", "");
		try {
			Files.write(path, Arrays.asList(txt.split("\\n")), StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	private String getSnippetDir() {
		String home = "C:/";
		try {
			home = URLDecoder.decode(ClassLoader.getSystemClassLoader().getResource(".").getPath(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		String prj = home.substring(1, home.lastIndexOf("/bin/"));
		return prj+"/snippets/";
	}
	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
