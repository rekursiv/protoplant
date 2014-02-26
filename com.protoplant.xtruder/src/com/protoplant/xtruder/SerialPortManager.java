package com.protoplant.xtruder;

import java.util.ArrayList;
import java.util.List;
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

@Singleton
public class SerialPortManager implements Runnable, SerialPortEventListener {
	
	private static final int RESET_DELAY = 18;
	private static final int dataLen = 6;  //  FIXME
	
	private Logger log;
	private EventBus eb;
	private SerialPort serialPort;
	StringBuilder buffer = new StringBuilder();
	private List<Byte> data = new ArrayList<Byte>();
	private int timerCount = 0;
	
	private volatile boolean isConnected;
	private volatile boolean isDestroyed;
	
	@Inject
	public SerialPortManager(Logger log, EventBus eb) {
		this.log = log;
		this.eb = eb;
	}
	
	public void init() {
//		listPorts();       ///    TEST
		isDestroyed = false;
		serialPort = new SerialPort("/dev/ttyAMA0");  //  COM1
		connect(); 
		new Thread(this).start();
	}
	
	public void destroy() {
		isDestroyed = true;
		disconnect();
		log.info("link manager destroyed");
	}

	public void listPorts() {
        String[] portNames = SerialPortList.getPortNames();
        for(int i = 0; i < portNames.length; i++){
            log.info(portNames[i]);
        }
	}
	

	
	@Override
	public void run() {
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			return;
		}
		++timerCount;
//		System.out.println("*");
		if (!isDestroyed) {
			new Thread(this).start();
		}		
	}

	public void run_OLD() {

//		log.info("run TOP");
		
//		disconnect();
        try {
//    		connect();
    		testConnection();
    		log.info("connection OK");
        } catch (SerialPortException e) {
            log.log(Level.INFO, "", e);
            isConnected = false;
            disconnect();
            connect();
        } finally {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				return;
			}
			if (!isDestroyed) {
//				log.info("Previous connect attempt failed, trying again...");
				new Thread(this).start();
			}
        }

//		log.info("run BTM");
	}
	
	private void connect() {
		log.info("");
		timerCount=0;
        try {
        	serialPort.openPort();
        	serialPort.setParams(9600, 8, 1, 0);
        	serialPort.addEventListener(this, SerialPort.MASK_RXCHAR);
        	isConnected = true;
        } catch (SerialPortException e) {
            log.log(Level.INFO, "", e);
            disconnect();
        }
	}
	
	private void disconnect() {
		log.info("");
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
		serialPort.writeByte((byte)0);
	}


	@Override
	public void serialEvent(SerialPortEvent evt) {
		if (evt.getEventValue()>0) {
			try {
				if (timerCount>RESET_DELAY||data.size()>=dataLen) data.clear();

				byte[] dataChunk = serialPort.readBytes();
				for (byte dataByte : dataChunk) {
					data.add(dataByte);
				}
				if (data.size()>=dataLen) {
					postData();
//					data.clear();
				}
				timerCount=0;
				
			} catch (SerialPortException e) {
	            log.log(Level.SEVERE, "!!!", e);
			}
		}
	}
	
	
	private void testData() {
		SerialDataRxEvent evt = new SerialDataRxEvent(data);
		int offset = 0;
		int curValue = (evt.getByte(offset)<<8)|evt.getByte(offset+1);
		curValue = (short)curValue;  // make signed
		System.out.println(data.size()+":"+curValue);
	}
	
	private void postData() {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
//				System.out.println("POST");
				eb.post(new SerialDataRxEvent(data));
			}
		});
	}
	
	


}
