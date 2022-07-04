package com.diquest.rest.nhn.filter.value;

public class RangeValue implements FieldValue {
	private String value;

	public RangeValue(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
