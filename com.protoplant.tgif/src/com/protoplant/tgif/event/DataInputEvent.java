package com.protoplant.tgif.event;

public class DataInputEvent {
	
	private String data;
	
	public DataInputEvent(String data) {
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
