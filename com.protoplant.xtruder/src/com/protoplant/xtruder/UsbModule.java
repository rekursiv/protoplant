package com.protoplant.xtruder;

import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.bind.DatatypeConverter;

import com.codeminders.hidapi.HIDDevice;
import com.codeminders.hidapi.HIDDeviceNotFoundException;
import com.codeminders.hidapi.HIDManager;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;


//VID:  0x2047  (8263)
//PID:  somewhere in the range of 0x03DF-0x03FD.  
//test PID:  0x0301  (769)
//"report id" = 0x3F



public class UsbModule {
	protected static final int vid = 0x2047;
	protected static final int reportId = 0x3F;
	
	protected int pid = 0x0301;
	protected Logger log;
	protected HIDDevice dev = null;
	protected EventBus eb;
	


	@Inject
	public UsbModule(Logger log, EventBus eb) {
		this.log = log;
		this.eb = eb;
	}
	
	public void connect() {
		disconnect();
        try {
			dev = HIDManager.getInstance().openById(vid, pid, null);
			dev.disableBlocking();
			log.info("Connected");
		} catch (HIDDeviceNotFoundException e) {
			dev = null;
		} catch (IOException e) {
			dev = null;
		}
	}
	
	public void disconnect() {
		if (dev==null) return;
		try {
			dev.close();
			log.info("Lost connection");
		} catch (IOException e) {
		} finally {
			dev = null;
		}
	}
	
	public void refreshWrite() {
		if (dev==null) {
			connect();
		}
		if (dev!=null) {
			try {
				writeUsb();
			} catch (IOException e) {
				disconnect();
			}
		}
	}
	
	public void refreshRead() {
		if (dev!=null) {
			try {
				readUsb();
			} catch (IOException e) {
				disconnect();
			}
		}
	}
	
	protected int extractInt16(byte[] buf, int offset) {
		return (short)(((buf[offset]&0xFF)<<8)|(buf[offset+1]&0xFF));
	}
	
	protected int extractUnsignedInt16(byte[] buf, int offset) {
		return ((buf[offset]&0xFF)<<8)|(buf[offset+1]&0xFF);
	}
	
	protected String toBinary(int byteData) {
		String bits = "0000000"+Integer.toBinaryString(byteData)+" ";
		return bits.substring(bits.length() - 9, bits.length());
	}
	
	protected void writeUsb() throws IOException {
		byte[] out = new byte[3];
		out[0]=reportId;
		out[1]=1;
		out[2]=0;
		dev.write(out);
	}
	
	protected void readUsb() throws IOException {
        byte[] buf = new byte[64];
        int bufLen = dev.readTimeout(buf, 10);
        log.info(bufLen+":"+DatatypeConverter.printHexBinary(buf));
	}

}
