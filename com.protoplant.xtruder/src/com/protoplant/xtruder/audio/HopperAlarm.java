package com.protoplant.xtruder.audio;

import java.util.logging.Logger;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.protoplant.xtruder.SerialDataRxEvent;

@Singleton
public class HopperAlarm {
	
	private static final float threshold = 100;
	private static final int repeatTime = 600;
	private static final int index = 2;
		
	private Logger log;
	private int emptyCount = 0;
	private int fullCount = 0;
	private AudioManager am;
	
	@Inject
	public HopperAlarm(Logger log, AudioManager am) {
		this.log = log;
		this.am = am;
	}
	
	@Subscribe
	public void onDataRx(SerialDataRxEvent evt) {
		int offset = index*2;  // data is two bytes long
		int curValue = (evt.getByte(offset)<<8)|evt.getByte(offset+1);
		if (curValue>threshold) ++fullCount;
		else ++emptyCount;
		if (emptyCount+fullCount>repeatTime) {
//			log.info("****  "+fullCount+" : "+emptyCount);
			if (emptyCount>fullCount) am.playClip("starvin");
			emptyCount=0;
			fullCount=0;
		}
	}
	
	

}
