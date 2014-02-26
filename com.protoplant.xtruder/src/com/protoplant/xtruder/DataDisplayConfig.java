package com.protoplant.xtruder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class DataDisplayConfig {

	public float scale = 1.0f;
	public String unit = "mm";
	
	
	
}
