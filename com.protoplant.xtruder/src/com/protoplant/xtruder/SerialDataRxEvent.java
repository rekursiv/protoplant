package com.protoplant.xtruder;

import java.util.List;

public class SerialDataRxEvent {
	private List<Byte> data;

	public SerialDataRxEvent(List<Byte> data) {
		this.data = data;
	}
	
	//  return UNSIGNED byte as an int
	//  to work around Java's lack of support for unsigned types
	public int getByte(int offset) {
		if (offset>=0&&offset<data.size()) return data.get(offset)&0xFF;
		else return 0;
	}


	

}
