package com.diquest.rest.nhn.filter.value;
public class OrFieldValue implements FieldValue {
		String value;

		public OrFieldValue(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

	}