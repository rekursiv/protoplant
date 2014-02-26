package com.protoplant.tgif.event;

public class DataOutputEvent {
	
	private String data;
	
	public DataOutputEvent(String data) {
		super();
		this.data = data;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

}
