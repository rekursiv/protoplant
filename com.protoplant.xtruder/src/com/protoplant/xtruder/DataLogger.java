package com.protoplant.xtruder;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import au.com.bytecode.opencsv.CSVWriter;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DataLogger {
	
	private Logger log;
	private CSVWriter writer = null;

	@Inject
	public DataLogger(Logger log) {
		this.log = log;
	}
	
	public void init() {
		try {
			writer = new CSVWriter(new FileWriter("data/"+getTimeStamp()+".csv"), ',');
		} catch (IOException e) {
			log.log(Level.WARNING, "", e);
		}
		writer.writeNext(new String[]{"Timestamp", "Type", "Data1", "Data2", "Data3"});
	}
	
	public void destroy() {
		try {
			if (writer!=null) writer.close();
		} catch (IOException e) {
			log.log(Level.WARNING, "", e);
		}
	}

	public void write(String type, String data1, String data2, String data3) {
		write(new String[] {getTimeStamp(), type, data1, data2, data3});
	}
	
	public void write(String type, String data1, String data2) {
		write(new String[] {getTimeStamp(), type, data1, data2});
	}
	
	public void write(String type, String data) {
		write(new String[] {getTimeStamp(), type, data});
	}
	
	public void write(String[] line) {
		if (writer!=null) writer.writeNext(line);
		log.info(Arrays.toString(line));
	}
	
	@Subscribe
	public void onMotorSpeedChange(MotorStateChangeEvent event) {
		if (event.getState()==MotorState.SPEED) {
			write("Motor", event.getPanel().getLabel(), event.getPanel().getSpeed()+"");
		}
	}
	
	private String getTimeStamp() {
		DateTime dt = new DateTime();
		DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd_HH-mm-ss_SSS");
		return dtf.print(dt);
	}

	
	public void test() {                //////////
//		log.info(getTimeStamp());
		write("test", "123");
	}

}
