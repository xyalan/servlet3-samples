package com.hialan.enumes;

public enum ConsumerEventType {
	ORDER("order"), SHIPPING("shipping");

	private String value;

	public static ConsumerEventType valueOfIgnoreCase(final String value) {
		for (final ConsumerEventType t : values()) {
			if (t.value.equalsIgnoreCase(value)) {
				return t;
			}
		}
		return null;
	}

	private ConsumerEventType(String _value) {
		this.value = _value;
	}

	public String getValue() {
		return value;
	}

}