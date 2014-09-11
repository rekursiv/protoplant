package com.protoplant.xtruder;

import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.bind.DatatypeConverter;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

public class AnalogModule extends UsbModule {
	
	@Inject
	public AnalogModule(Logger log, EventBus eb) {
		super(log, eb);
		pid = 0x03E1;
	}

	@Override
	public void writeUsb() throws IOException {
		byte cmd=0;
		byte[] out = new byte[3];
		out[0]=0x3F;
		out[1]=1;
		out[2]=cmd; 
		dev.write(out);
	}

	@Override
	public void readUsb() throws IOException {
        byte[] buf = new byte[64];
        int bufLen = dev.readTimeout(buf, 10);
        AnalogDataEvent ade = new AnalogDataEvent();
        ade.data1 = extractInt16(buf, 4);
        ade.data2 = extractInt16(buf, 6);
        ade.data3 = extractInt16(buf, 8);
        ade.data4 = extractInt16(buf, 10);
        eb.post(ade);
        
//        log.info(bufLen+":"+DatatypeConverter.printHexBinary(buf));
//        log.info(bufLen+":"+buf[1]+":"+buf[2]+":"+buf[3]+"     "+extractInt16(buf, 4)+":"+extractInt16(buf, 6)+":"+extractInt16(buf, 8)+":"+extractInt16(buf, 10));
        
	}
	

}


