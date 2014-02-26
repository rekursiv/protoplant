package com.protoplant.tgif;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.widgets.Display;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.protoplant.tgif.event.DataInputEvent;
import com.protoplant.tgif.event.DataOutputEvent;
import com.protoplant.tgif.event.DebugEvent;


@Singleton
public class LinkManager implements Runnable, SerialPortEventListener {
	
	private Logger log;
	private EventBus eb;
	private SerialPort serialPort;
	StringBuilder buffer = new StringBuilder();
	
	private volatile boolean isConnected;
	private volatile boolean isDestroyed;
	
	@Inject
	public LinkManager(Logger log, EventBus eb) {
		this.log = log;
		this.eb = eb;
	}
	
	public void init() {
		serialPort = new SerialPort("COM10");
		new Thread(this).start();
	}
	public void destroy() {
		isDestroyed = true;
		disconnect();
		//  TODO:  wait for connector thread to finish??
	}

	public void listPorts() {
        String[] portNames = SerialPortList.getPortNames();
        for(int i = 0; i < portNames.length; i++){
            log.info(portNames[i]);
        }
	}
	
	@Override
	public void run() {
//		log.info("run TOP");
		
		disconnect();
        try {
    		connect();
    		testConnection();
        } catch (SerialPortException e) {
            log.log(Level.INFO, "", e);
            disconnect();
        } finally {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				return;
			}
			if (!isConnected && !isDestroyed) {
				log.info("Previous connect attempt failed, trying again...");
				new Thread(this).start();
			}
        }

//		log.info("run BTM");
	}
	
	private void connect() throws SerialPortException {
        try {
        	serialPort.openPort();
        	serialPort.setParams(115200, 8, 1, 0);
        	serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_XONXOFF_IN | SerialPort.FLOWCONTROL_XONXOFF_OUT );
        	serialPort.addEventListener(this, SerialPort.MASK_RXCHAR);

        } catch (SerialPortException e) {
            log.log(Level.INFO, "", e);
            disconnect();
        }
	}
	
	private void disconnect() {
		isConnected = false;
        try {
        	if (serialPort!=null&&serialPort.isOpened()) {
        		serialPort.closePort();
          	}
        } catch (SerialPortException e) {
            log.log(Level.SEVERE, "!!!", e);
        }
	}
	
	private void testConnection() throws SerialPortException {
		serialPort.writeString("\n");
	}


	@Override
	public void serialEvent(SerialPortEvent evt) {
		if (evt.getEventValue()>0) {
			try {
				String msg = serialPort.readString();
				buffer.append(msg);
				int pos = 0;
				while ((pos = buffer.indexOf("\n")) >= 0) {
					postData(buffer.substring(0, pos+1));
					buffer.delete(0, pos+1);
				}
				isConnected = true;  //  TODO:  check data
			} catch (SerialPortException e) {
	            log.log(Level.SEVERE, "!!!", e);
	            //  TODO:  reconnect??
			}
		}
	}
	
	private void postData(final String data) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				eb.post(new DataInputEvent(data));
			}
		});
	}
	
	@Subscribe
	public void onDataOutput(DataOutputEvent evt) {
		try {
			serialPort.writeString(evt.getData());
		} catch (SerialPortException e) {
			e.printStackTrace();
		}
	}
	
	

	///////////////////////////////////////////////////////////////////////

	
	@Subscribe
	public void onDebug(DebugEvent evt) {
		log.info("debug");
		try {
//			serialPort.writeString("{\"sys\"}:\"\"}\n");
			serialPort.writeString("$test=1\n");
		} catch (SerialPortException e) {
			e.printStackTrace();
		}
	}
	
	
	
	public void test () {
		log.info("test TOP");
		
//		test = new JsscTest(log);
//		test.init();
		
		log.info("test BTM");
	}



}
