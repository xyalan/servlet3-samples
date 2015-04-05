package com.hialan.listener;

import com.hialan.event.ConsumerEvent;

import java.util.EventListener;

public interface ConsumerEventListener extends EventListener {
	void handleEvent(ConsumerEvent event) throws Exception;
}
