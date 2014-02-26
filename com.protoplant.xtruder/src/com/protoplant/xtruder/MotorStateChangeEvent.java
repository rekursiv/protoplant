package com.protoplant.xtruder;

public class MotorStateChangeEvent {

	
	private MotorPanel mp;
	private MotorState state;

	public MotorStateChangeEvent(MotorPanel mp, MotorState state) {
		this.mp = mp;
		this.state = state;
	}

	public MotorPanel getPanel() {
		return mp;
	}

	public MotorState getState() {
		return state;
	}

	
}
