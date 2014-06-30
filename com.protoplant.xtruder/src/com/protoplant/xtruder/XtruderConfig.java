package com.protoplant.xtruder;

import util.config.ConfigBase;

public class XtruderConfig extends ConfigBase {

	// logging
	public boolean logToConsole=true;
	public boolean logToFile=false;
	public boolean showLogView=true;
	
	// coil mass
	public float pcabsDensity=1;
	public float htplaDensity=1;
	public float cfplaDensity=1;
	
	// feedback
	public float fbTarget175 = 1.73f;
	public float fbTarget285 = 2.85f;
	public float fbDeadband = 0.015f;
	public float fbSpread = 0.1f;
	public float fbNudgeFactor = 0.05f;	
	
	public MotorConfig[] motors = {new MotorConfig(), new MotorConfig(), new MotorConfig(), new MotorConfig(), new MotorConfig()};
	
	public DataDisplayConfig[] displays = {new DataDisplayConfig(),  new DataDisplayConfig()};
	
	
	
}
