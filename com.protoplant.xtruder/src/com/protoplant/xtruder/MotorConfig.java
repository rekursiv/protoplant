package com.protoplant.xtruder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class MotorConfig {

	public float speedInit = 5;
	public float speedScale = 2890;
	
	
}
