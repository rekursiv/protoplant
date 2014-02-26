package com.protoplant.xtruder;

import util.config.ConfigBase;

public class XtruderConfig extends ConfigBase {

	// logging
	public boolean logToConsole=true;
	public boolean logToFile=false;
	public boolean showLogView=true;
	
	public float pcabs175GramsPerInch=1;
	public float htpla175GramsPerInch=1;
	public float cfpla175GramsPerInch=1;
	
	public float pcabs3GramsPerInch=2;
	public float htpla3GramsPerInch=2;
	public float cfpla3GramsPerInch=2;
	
	public MotorConfig[] motors = {new MotorConfig(), new MotorConfig(), new MotorConfig(), new MotorConfig(), new MotorConfig()};
	
	public DataDisplayConfig[] displays = {new DataDisplayConfig(),  new DataDisplayConfig(), new DataDisplayConfig()};
	
	
	
}
