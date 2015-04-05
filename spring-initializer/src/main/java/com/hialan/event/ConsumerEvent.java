package com.hialan.event;


import com.hialan.enumes.ConsumerEventType;

import java.util.EventObject;
import java.util.List;

public class ConsumerEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8390387692158009823L;

	private ConsumerEventType type;
	private List<String> message;

	public ConsumerEvent(final ConsumerEventType type, final List<String> message) {
		super(message);
		this.type = type;
		this.message = message;
	}

	public List<String> getMessage() {
		return message;
	}

	public ConsumerEventType getType() {
		return type;
	}

}