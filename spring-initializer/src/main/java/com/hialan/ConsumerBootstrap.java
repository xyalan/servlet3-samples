package com.hialan;

import org.slf4j.Logger;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class ConsumerBootstrap implements ApplicationListener<ApplicationEvent> {
	private Logger logger = org.slf4j.LoggerFactory.getLogger(ConsumerBootstrap.class);

	@Autowired
	@Qualifier(value = "orderMessageListenerContainer")
	private SimpleMessageListenerContainer listenerContainer;

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof ContextRefreshedEvent) {
			boolean isRoot = ((ContextRefreshedEvent) event).getApplicationContext().getParent() == null;
			if (isRoot) {
				logger.info("startup consumer listener");
				listenerContainer.start();
			}
		}

	}

}
