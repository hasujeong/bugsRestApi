package com.diquest.rest.nhn.filter.result;

import java.util.ArrayList;
import java.util.List;

import com.diquest.rest.nhn.filter.value.FieldValue;

public class FilterNormalValueResult {
	private String fieldName;
	private List<FieldValue> values;

	public FilterNormalValueResult(String fieldName) {
		this.fieldName = fieldName.toUpperCase();
		values = new ArrayList<FieldValue>();
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public List<FieldValue> getValues() {
		return values;
	}

	public void setValues(List<FieldValue> values) {
		this.values = values;
	}

	public boolean containTriggerFieldNameOrValue(List<String> triggerFields) {
		if (containTriggerFieldName(triggerFields)) {
			return true;
		}
		return containTriggerFieldValue(triggerFields);
	}

	private boolean containTriggerFieldValue(List<String> triggerFields) {
		for (FieldValue v : values) {
			if (triggerFields.contains(v.getValue())) {
				return true;
			}
		}
		return false;
	}

	private boolean containTriggerFieldName(List<String> triggerFields) {
		return triggerFields.contains(fieldName);
	}

}