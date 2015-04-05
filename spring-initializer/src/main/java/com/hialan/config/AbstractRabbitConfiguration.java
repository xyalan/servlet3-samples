package com.hialan.config;

import org.springframework.amqp.support.converter.JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(PropertyPlaceholderConfiguration.class)
public class AbstractRabbitConfiguration {

	@Bean
	public MessageConverter jsonMessageConverter() {
		return new JsonMessageConverter();
	}

}

@Configuration
class PropertyPlaceholderConfiguration {
	@Bean
	public PropertyPlaceholderConfigurer propertyPlaceholderConfigurer() {
		return new PropertyPlaceholderConfigurer();
	}
}

