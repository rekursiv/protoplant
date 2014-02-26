package com.protoplant.tgif.scratch;

import java.util.logging.Level;
import java.util.logging.Logger;



import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;

public class JsscTest implements SerialPortEventListener {
	
	private Logger log;
	private SerialPort serialPort;

	public JsscTest(Logger log) {
		this.log = log;
	}
		

	public void portNames() {
        String[] portNames = SerialPortList.getPortNames();
        for(int i = 0; i < portNames.length; i++){
            System.out.println(portNames[i]);
        }
    }
	
	public void init() {
		if (serialPort!=null && serialPort.isOpened()) destroy();
		serialPort = new SerialPort("COM10");
        try {
        	serialPort.openPort();
        	serialPort.setParams(115200, 8, 1, 0);
        	serialPort.addEventListener(this, SerialPort.MASK_RXCHAR);
//        	sp.writeString("?\n");
//        	sp.sendBreak(1);
//        	sp.writeString("{\"sys\"}:\"\"}\n");
        	serialPort.writeString("{\"fb\"}:\"\"}\n");
        } catch (SerialPortException e) {
            log.log(Level.SEVERE, "!!!", e);
            destroy();
        }
	}
	
	public void destroy() {
        try {
        	if (serialPort!=null&&serialPort.isOpened()) {
        		serialPort.closePort();
        		serialPort=null;
        	}
        } catch (SerialPortException e) {
            log.log(Level.SEVERE, "!!!", e);
        }
	}
	
	@Override
	public void serialEvent(SerialPortEvent evt) {
//		log.info("got bytes:"+evt.getEventValue());
		
		if (evt.getEventValue()>0) {
			try {
				String msg = serialPort.readString();
				msg = msg.replace("\n", "\\n");
				msg = msg.replace("\r", "\\r");
				log.info("bytes="+evt.getEventValue()+"    msg='"+msg+"'");
	//			sp.purgePort(SerialPort.PURGE_RXCLEAR);
			} catch (SerialPortException e) {
	            log.log(Level.SEVERE, "!!!", e);
	            destroy();
			}
		}
		
	}
	

	
	
	
	
	
	
    
	public void readDataTest() {
        serialPort = new SerialPort("COM10");
        try {
            boolean opened = serialPort.openPort();//Open serial port
            if (opened) {
            	log.info("opened");
	            serialPort.setParams(115200, 8, 1, 0);//Set params.
	            byte[] buffer = serialPort.readBytes(10);//Read 10 bytes from serial port
	            log.info(buffer.toString());
            } else {
            	log.info("NOT opened");
            }
        } catch (SerialPortException ex) {
            log.log(Level.SEVERE, "bad", ex);
        } finally {
            try {
				boolean closed = serialPort.closePort();
				if (closed) log.info("closed");
				else log.info("NOT closed");
			} catch (SerialPortException e) {
				e.printStackTrace();
			}
        }
    }




}
