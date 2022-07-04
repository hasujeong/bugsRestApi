package com.diquest.rest.nhn.filter.result;

import java.util.List;

import com.diquest.rest.nhn.filter.value.FieldValue;

public class FilterRangeValueResult {
	private String fieldName;
	private FieldValue left;
	private FieldValue right;

	public FilterRangeValueResult(String fieldName) {
		this.fieldName = fieldName.toUpperCase();
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public FieldValue getLeft() {
		return left;
	}

	public void setLeft(FieldValue left) {
		this.left = left;
	}

	public FieldValue getRight() {
		return right;
	}

	public void setRight(FieldValue right) {
		this.right = right;
	}

	public boolean containTriggerFieldNameOrValue(List<String> triggerFields) {
		if (containTriggerFieldName(triggerFields)) {
			return true;
		}
		return containTriggerFieldValue(triggerFields);
	}

	private boolean containTriggerFieldValue(List<String> triggerFields) {
		String leftRemoveBrace = left.getValue().replace("[", "").replace("{", "");
		if (triggerFields.contains(leftRemoveBrace.toUpperCase())) {
			return true;
		}

		String rightRemoveBrace = right.getValue().replace("]", "").replace("}", "");
		return triggerFields.contains(rightRemoveBrace.toUpperCase());
	}

	private boolean containTriggerFieldName(List<String> triggerFields) {
		return triggerFields.contains(fieldName);
	}

}