package com.protoplant.xtruder;

public class FastLoadModule extends XtruderModule {

	@Override
	protected void setupConfig() {
		config = new XtruderConfig();  // to save some load time, use default config instead of loading from disk
		bind(XtruderConfig.class).toInstance(config);
	}
	
}
