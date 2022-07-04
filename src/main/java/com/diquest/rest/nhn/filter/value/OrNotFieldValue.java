package com.diquest.rest.nhn.filter.value;
public class OrNotFieldValue implements FieldValue {
		String value;

		public OrNotFieldValue(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}