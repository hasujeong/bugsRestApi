package com.diquest.rest.nhn.filter.value;
public class AndNotFieldValue implements FieldValue {
		String value;

		public AndNotFieldValue(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

	}