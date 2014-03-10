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
	
	
	public MotorConfig[] motors = {new MotorConfig(), new MotorConfig(), new MotorConfig(), new MotorConfig(), new MotorConfig()};
	
	public DataDisplayConfig[] displays = {new DataDisplayConfig(),  new DataDisplayConfig(), new DataDisplayConfig()};
	
	
	
}
