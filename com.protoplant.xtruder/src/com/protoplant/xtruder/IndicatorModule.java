package com.protoplant.xtruder;

import java.io.IOException;
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

public class IndicatorModule extends UsbModule {
	
	private volatile boolean doZero=false;
	
	
	@Inject
	public IndicatorModule(Logger log, EventBus eb) {
		super(log, eb);
		pid = 0x03E0;
	}


	@Override
	public void writeUsb() throws IOException {
		byte cmd=0;
		if (doZero) {
			doZero=false;
			cmd=1;
		}
		byte[] out = new byte[5];
		out[0]=0x3F;
		out[1]=1;
		out[2]=cmd; 
		dev.write(out);
	}
	
	
	@Override
	public void readUsb() throws IOException {
        byte[] buf = new byte[64];
        dev.readTimeout(buf, 10);
        
        float cur = (float)extractInt16(buf, 4)*0.002f;
        float min = (float)extractInt16(buf, 6)*0.002f;        
        float max = (float)extractInt16(buf, 8)*0.002f;
        
        IndicatorDataEvent ide = new IndicatorDataEvent(cur, min, max);
        
        eb.post(ide);

//        log.info(bufLen+":"+buf[1]+":"+buf[2]+":"+buf[3]+"     "+extractInt16(buf, 4)+":"+extractInt16(buf, 6)+":"+extractInt16(buf, 8));
        
	}
	
	@Subscribe
	public void onZero(IndicatorZeroEvent evt) {
		doZero=true;
	}
	
}


//System.out.println(DatatypeConverter.printHexBinary(buf));
//System.out.println(toBinary(buf[4])+toBinary(buf[5]));

//System.out.println(String.format("%02X %02X", buf[2], buf[3]));
//System.out.println(""+buf[2]);
//System.out.println(new String(buf, "US-ASCII").substring(2, len+2));
//System.out.println("-----");
