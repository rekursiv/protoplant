package com.protoplant.xtruder;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;

import util.config.ConfigManager;
import util.logging.LogSetup;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeListener;
import com.google.inject.spi.TypeEncounter;

public class XtruderModule extends AbstractModule {

	protected final ConfigManager<XtruderConfig> cfgMgr = new ConfigManager<XtruderConfig>(XtruderConfig.class, "xtruder-config.js");
	protected XtruderConfig config;
	protected final EventBus eventBus = new EventBus();

	
	@Override
	protected void configure() {
		setupConfig();
		setupLogging();
		setupEventBus();
	}
	
	protected void setupConfig() {
		config = cfgMgr.load();
		bind(XtruderConfig.class).toInstance(config);
	}
	
	protected void setupEventBus() {
		bind(EventBus.class).toInstance(eventBus);
		bindListener(Matchers.any(), new TypeListener() {
			public <I> void hear(TypeLiteral<I> typeLiteral, TypeEncounter<I> typeEncounter) {
				typeEncounter.register(new InjectionListener<I>() {
					public void afterInjection(I i) {
						eventBus.register(i);
//						System.out.println("EventBus registered "+i.getClass().getName());
					}
				});
			}
		});
	}
	
	protected void setupLogging() {
		
//		LogManager.getLogManager().getLogger("").setLevel(Level.ALL);

		// setup logging to console
		if (config.logToConsole) {
			LogSetup.initConsole(Level.ALL);
		}

		// setup logging to file
		if (config.logToFile) {
			try {
				LogManager.getLogManager().getLogger("").addHandler(new FileHandler("log/rti-ui-%u-%g.log", 0, 10));
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// log config errors
		cfgMgr.logErrorsIfAny();
	}


}
