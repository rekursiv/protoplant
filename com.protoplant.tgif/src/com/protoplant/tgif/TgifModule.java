package com.protoplant.tgif;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

public class TgifModule  extends AbstractModule {
	private final EventBus eventBus = new EventBus();

	@Override
	protected void configure() {
		configureEventBus();
	}
	
	protected void configureEventBus() {
		bind(EventBus.class).toInstance(eventBus);
		bindListener(Matchers.any(), new TypeListener() {
			public <I> void hear(TypeLiteral<I> typeLiteral, TypeEncounter<I> typeEncounter) {
				typeEncounter.register(new InjectionListener<I>() {
					public void afterInjection(I i) {
						eventBus.register(i);
						System.out.println("EventBus registered "+i.getClass().getName());
					}
				});
			}
		});
	}
}