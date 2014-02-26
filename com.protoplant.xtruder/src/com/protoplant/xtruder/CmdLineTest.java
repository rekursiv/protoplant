package com.protoplant.xtruder;

import java.util.Scanner;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class CmdLineTest {
	
	public StSmc st = null;
	
	public void init() {
		System.out.println("Setting up DI framework...");
		Injector injector = Guice.createInjector(new FastLoadModule());
		st = injector.getInstance(StSmc.class);
		st.initSpi();
		st.initAllBoards();
	}
	
	
	public void loop() {
		Scanner s = new Scanner(System.in);
		String in = "";
		int count = 1;
		char c = ' ';
		while (!in.equals("Q")) {
			System.out.print("> ");
			in = s.nextLine().toUpperCase();
			System.out.println(count+": "+in+"    -----------");
			if (in.isEmpty()) {
				st.test();
			} else {
				c=in.toCharArray()[0];
				if (in.isEmpty()) break;
				if (Character.isAlphabetic(c)&&c!='B') {
					cmdLineParseCmd(in.toCharArray()[0]);
				} else {
					cmdLineParseNumber(in);
				}
			}
			++count;
		}
		s.close();
	}


	public void cmdLineParseNumber(String in) {
		boolean setBoard = false;
		if (in.startsWith("B")) {
			setBoard = true;
			in = in.substring(1);
		}
		int num = 0;
		try { num = Integer.parseInt(in); } catch (NumberFormatException e) { return; }
		if (setBoard) {
			st.setCurBoardIndex(num);		
		} else {
			st.run(num);
//			st.goToPos(num);
		}
	}
	
	public void cmdLineParseCmd(char cmd) {
    	switch (cmd) {
    	case 'I':
    		st.initAllBoards();
    		break;
    	case 'R':
    		st.resetDevice();
    		break;
    	case 'S':
    		st.dumpStatus();
    		break;
    	case 'H':
    		st.hold();
    		break;
    	case 'Z':
    		st.hiZ();
    		break;
     	}
	}


}
