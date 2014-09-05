package com.protoplant.xtruder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.Spi;

// boards from eBay are based on ST L6472

@Singleton
public class StSmc {

	final static int MOVE_TORQUE = 0x16;   // default = 0x29 
	final static int MOVE_TORQUE_L6482 = 0x04;   // default = 0x29 
	
	final static int HOLD_TORQUE = 0x05;   // default = 0x29
	final static int OCD_THRESH = 0x0F;  // default = 0x08
	final static int STEP_MODE = 0x0A;    // 0x08=full step, 9=half, A=1/4, B=1/8, C=1/16

	final static int MAX_SPEED = 200;
	final static int ACCEL = 90;
	final static int DECEL = 90;
	
	int numBoards = 5;
	int curBoardIndex = 0;
	
	private Logger log;
	
	
	@Inject
	public StSmc(Logger log) {
		this.log = log;
	}
	
	public void initSpi() {
		
    	if (!System.getProperty("os.arch").equals("arm")) {
    		log.warning("Not running on Raspberry Pi Hardware, GPIO will not be initialized.");
    		return;
    	}
		
//		Gpio.piHiPri(99);
		
        // setup SPI for communication
        int fd = Spi.wiringPiSPISetup(0, 1000000);
        if (fd <= -1) {
            log.warning("SPI setup fail");
            return;
        }
        log.info("SPI setup success");
	}
	

	//////////////////////////////////////////////////////////////////////////////

	public void setCurBoardIndex(int index) {
		if (index>=0&&index<numBoards) {
			curBoardIndex = index;
//			log.info("Current board index set to "+index);
		}
	}
	
	public void initAllBoards() {
		for (curBoardIndex=0; curBoardIndex<numBoards; ++curBoardIndex) {
			resetDevice();
			setDefaults();
			resetErrorFlags();
		}
		curBoardIndex=0;
		fixBrokenL6482();
	}
	
	public void initCurrentBoard() {
		if (curBoardIndex!=0) {  // can't reset board 0
			resetDevice();
			setDefaults();
			resetErrorFlags();
		}
	}
	
	public void setDefaults() {
		setOcd(OCD_THRESH);
		setStepMode(STEP_MODE);
		if (curBoardIndex==0) setTorque(MOVE_TORQUE_L6482, MOVE_TORQUE);
		else setTorque(HOLD_TORQUE, MOVE_TORQUE);
		setMaxSpeed(MAX_SPEED);
		setAccel(ACCEL);
		setDecel(DECEL);
	}
	
	
	
	public void resetDevice() {
		sendByte(0xC0);
	}
	
	public void setOcd(int threshold) {
		sendByte(0x13);
		sendByte(threshold);
	}
	
	public void setStepMode(int stepMode) {
		sendByte(0x16);
		sendByte(stepMode);
	}
	
	public void setMaxSpeed(int speed) {
		sendByte(0x07);
		sendByte(speed>>8);
		sendByte(speed);
	}
	
	public void setAccel(int rate) {
		sendByte(0x05);
		sendByte(rate>>8);
		sendByte(rate);
	}
	
	public void setDecel(int rate) {
		sendByte(0x06);
		sendByte(rate>>8);
		sendByte(rate);
	}
	
	
	public void setTorque(int holdTorque, int moveTorque) {
		sendByte(0x09);
		sendByte(holdTorque);
		sendByte(0x0A);
		sendByte(moveTorque);
		sendByte(0x0B);
		sendByte(moveTorque);
		sendByte(0x0C);
		sendByte(moveTorque);
	}
	
	public void resetErrorFlags() {
		sendByte(0xD0);  // "get status" command resets all error flags
	}
	
	public void dumpStatus() {
		sendByte(0xD0);
		log.info("[REPLY1] " + toBinary(getReply()));
		log.info("[REPLY2] " + toBinary(getReply()));
	}
	
	public ArrayList<Integer> getStatus() {
		ArrayList<Integer> bytes = new ArrayList<Integer>();
		sendByte(0xD0);
		bytes.add(getReply());
		bytes.add(getReply());
		return bytes;
	}
	
	private void fixBrokenL6482() {
		// set OCD_SD=0 in CONFIG (force bridge to stay on after over current detect)
		sendByte(0x1A);
		sendByte(0x2C);
		sendByte(0x08);
	}

	
	public void run(int speed) {
		if (speed<0) {
			sendByte(0x51);  // run reverse
			speed = -speed;
		}
		else sendByte(0x50); // run forward
		sendByte(speed>>16);
		sendByte(speed>>8);
		sendByte(speed);
	}
	
	public void goToPos(int pos) {
		sendByte(0x60);
		sendByte(pos>>16);
		sendByte(pos>>8);
		sendByte(pos);
	}
	
	
	public void hold() {
		sendByte(0xB0);
	}
	
	public void hiZ() {
		sendByte(0xA0);
	}

	
	
	
	//////////////////////////////////////////////////////////////////////////////	
	///////////////////////////////
	
	public void sendByte(int byteData) {
		ArrayList<Integer> pkt = new ArrayList<Integer>();
		for (int i=0; i<curBoardIndex; ++i) pkt.add(0);
		pkt.add(byteData);
		for (int i=0; i<curBoardIndex; ++i) pkt.add(0);
		writeSpi(pkt);
	}
	
	public int getReply() {
		ArrayList<Integer> pkt = new ArrayList<Integer>();
		for (int i=0; i<(numBoards-curBoardIndex); ++i) pkt.add(0);
		writeSpi(pkt);
		int reply = pkt.get((numBoards-curBoardIndex)-1);
		return reply;
	}	
	
    private void writeSpi(ArrayList<Integer> pkt){
//    	log.info("[TX] " + toBinary(pkt));
    	if (!System.getProperty("os.arch").equals("arm")) return;   /// bypass send if running on Windows
        
    	byte[] bytes = new byte[pkt.size()];
        for (int i=0; i<pkt.size(); ++i) {
        	bytes[i] = pkt.get(i).byteValue();
        }
        Spi.wiringPiSPIDataRW(0, bytes, bytes.length);
        for (int i=0; i<bytes.length; ++i) {
        	pkt.set(i, (int)bytes[i]);
        }
//        log.info("[RX] " + toBinary(pkt));
    }
	
    
	public boolean isSwitchOn(ArrayList<Integer> bytes) {
		return (bytes.get(1).intValue() & 0x04) != 0;
	}
	
	public boolean isBusy(ArrayList<Integer> bytes) {
		return (bytes.get(1).intValue() & 0x02) == 0;
	}
    
    //////////////////////    DEBUG    ////////////////////////////////
	

    public void test() {
    	run(30000);
	}
    
    
	public String toBinary(ArrayList<Integer> bytes) {
		StringBuilder sb = new StringBuilder();
		for (int b : bytes) sb.append(toBinary(b));
		return sb.toString();
	}
	
	public String toBinary(int byteData) {
		String bits = "0000000"+Integer.toBinaryString(byteData)+" ";
		return bits.substring(bits.length() - 9, bits.length());
	}
    
	
    
}










