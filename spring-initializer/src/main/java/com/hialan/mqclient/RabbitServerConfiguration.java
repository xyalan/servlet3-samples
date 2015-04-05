package com.hialan.mqclient;

import com.hialan.config.AbstractRabbitConfiguration;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitServerConfiguration extends AbstractRabbitConfiguration {

	private final static String AMQP_EXCHANGE_NAME = "ccms_event_bus_exchange";
	private final static String AMQP_QUEUE_NAME = "ccms_event_bus_queue";
	private final static String AMQP_ROUTING_KEY_NAME = "ccms_event_bus";

	@Bean(name = { "connectionFactory" })
	public ConnectionFactory connectionFactory() {
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
		connectionFactory.setAddresses("localhost:5672");
		connectionFactory.setUsername("guest");
		connectionFactory.setPassword("guest");
		connectionFactory.setVirtualHost("/");
		connectionFactory.setRequestedHeartBeat(180);
		return connectionFactory;
	}

	@Bean(name = {"amqpAdmin"})
	public AmqpAdmin amqpAdmin() {
		return new RabbitAdmin(connectionFactory());
	}

	@Bean(name = { "rabbitTemplate" })
	public RabbitTemplate rabbitTemplate() {
		RabbitTemplate template = new RabbitTemplate(connectionFactory());
		template.setExchange(AMQP_EXCHANGE_NAME);
		template.setRoutingKey(AMQP_ROUTING_KEY_NAME);
		template.setQueue(AMQP_QUEUE_NAME);
		//template.setMessageConverter(jsonMessageConverter());
		return template;
	}

	@Bean(name = {"amqpQueue"})
	public Queue amqpQueue() {
		Map<String, Object> map = new HashMap<>();
		map.put("x-ha-policy", "all");
		Queue queue = new Queue(AMQP_QUEUE_NAME,true, false, false,map);
		queue.setAdminsThatShouldDeclare(amqpAdmin());
		return queue;
	}

	@Bean(name = {"amqpExchange"})
	public Exchange amqpExchange() {
		Map<String, Object> map = new HashMap<>();
		map.put("x-ha-policy", "all");
		DirectExchange directExchange = new DirectExchange(AMQP_EXCHANGE_NAME,true, false,map);
		directExchange.setAdminsThatShouldDeclare(amqpAdmin());
		return directExchange;
	}

	@Bean(name = {"amqpBinding"})
	public Binding amqpBinding() {
		Map<String, Object> map = new HashMap<>();
		map.put("x-ha-policy", "all");
		Binding binding = BindingBuilder.bind(amqpQueue()).to(amqpExchange()).with(AMQP_ROUTING_KEY_NAME).and(map);
		binding.setAdminsThatShouldDeclare(amqpAdmin());
		return binding;
	}

}