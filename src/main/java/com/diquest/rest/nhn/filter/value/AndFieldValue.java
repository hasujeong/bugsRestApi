package com.diquest.rest.nhn.filter.value;
public class AndFieldValue implements FieldValue {
	String value;

	public AndFieldValue(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}