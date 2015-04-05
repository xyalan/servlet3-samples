package com.hialan.mqclient;

import com.hialan.config.AbstractRabbitConfiguration;
import com.hialan.listener.SimpleMessageListener;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitClientConfiguration extends AbstractRabbitConfiguration {

	private static final String ETL_TAOBAO_RECEIPT_EXCHANGE = "etl.taobao_receipt_exchange";
	private static final String ETL_TAOBAO_SHIPPING_EXCHANGE = "etl.taobao_shipping_exchange";

	@Bean(name = { "etlConnectionFactory" })
	public ConnectionFactory etlConnectionFactory() {
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
		connectionFactory.setAddresses("localhost:5672");
		connectionFactory.setUsername("guest");
		connectionFactory.setPassword("guest");
		connectionFactory.setVirtualHost("/");
		connectionFactory.setRequestedHeartBeat(180);
		return connectionFactory;
	}

	@Bean(name = { "etlAdmin" })
	public AmqpAdmin etlAdmin() {
		return new RabbitAdmin(etlConnectionFactory());
	}

	@Bean(name = { "etlQueue" })
	public Queue etlQueue() {
		Map<String, Object> map = new HashMap<>();
		map.put("x-ha-policy", "all");
		Queue queue = new Queue("queue_mail",true, false, false,map);
		queue.setAdminsThatShouldDeclare(etlAdmin());
		return queue;
	}

	@Bean(name = { "receiptExchange" })
	public Exchange receiptExchange() {
		Map<String, Object> map = new HashMap<>();
		map.put("x-ha-policy", "all");
		DirectExchange directExchange = new DirectExchange(ETL_TAOBAO_RECEIPT_EXCHANGE,true, false,map);
		directExchange.setAdminsThatShouldDeclare(etlAdmin());
		return directExchange;
	}

	@Bean(name = { "receiptBinding" })
	public Binding receiptBinding() {
		Map<String, Object> map = new HashMap<>();
		map.put("x-ha-policy", "all");
		Binding binding = BindingBuilder.bind(etlQueue()).to(receiptExchange())
				.with("mail").and(map);
		binding.setAdminsThatShouldDeclare(etlAdmin());
		return binding;
	}

	@Bean(name = { "shippingExchange" })
	public Exchange shippingExchange() {
		Map<String, Object> map = new HashMap<>();
		map.put("x-ha-policy", "all");
		DirectExchange directExchange = new DirectExchange(ETL_TAOBAO_SHIPPING_EXCHANGE,true, false,map);
		directExchange.setAdminsThatShouldDeclare(etlAdmin());
		return directExchange;
	}

	@Bean(name = { "shippingBinding" })
	public Binding shippingBinding() {
		Map<String, Object> map = new HashMap<>();
		map.put("x-ha-policy", "all");
		Binding binding = BindingBuilder.bind(etlQueue()).to(shippingExchange())
				.with("mail").and(map);
		binding.setAdminsThatShouldDeclare(etlAdmin());
		return binding;
	}

	// -------- order listener config ---------------
	@Bean(name = { "orderMessageListenerContainer" })
	public SimpleMessageListenerContainer orderMessageListenerContainer() {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(etlConnectionFactory());
		container.setAutoStartup(false);
		container.setRecoveryInterval(10000);
		container.setQueues(etlQueue());
		container.setMessageListener(messageListener());
		container.setAcknowledgeMode(AcknowledgeMode.AUTO);
		return container;
	}

	@Bean
	public SimpleMessageListener messageListener() {
		return new SimpleMessageListener();
	}

}