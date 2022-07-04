package com.diquest.rest.nhn.filter.parse;

import java.security.InvalidParameterException;

import com.diquest.ir.util.common.StringUtil;
import com.diquest.rest.nhn.filter.result.FilterRangeValueResult;
import com.diquest.rest.nhn.filter.value.RangeValue;

public class FilterRangeValueParser {
	FilterRangeValueResult filterRangeValueResult;
	private String parseTargetParameter;

	public FilterRangeValueParser(String fieldName, String targetVal) {
		this.filterRangeValueResult = new FilterRangeValueResult(fieldName);
		this.parseTargetParameter = targetVal;
	}

	public FilterRangeValueResult parseAll() throws InvalidParameterException {
		if (StringUtil.isEmpty(parseTargetParameter)) {
			return null;
		}

		parse();
		return filterRangeValueResult;
	}

	private void parse() throws InvalidParameterException {
		assertRangeValue();

		filterRangeValueResult.setLeft(new RangeValue(parseTargetParameter.split(",")[0]));
		filterRangeValueResult.setRight(new RangeValue(parseTargetParameter.split(",")[1]));
	}

	private void assertRangeValue() {
		if(!parseTargetParameter.endsWith("}") && !parseTargetParameter.endsWith("]")){
			throw new InvalidParameterException("range filter must ends with } or ] : " + parseTargetParameter);
		}
		if (!parseTargetParameter.contains(",")) {
			throw new InvalidParameterException("range filter must contains , : " + parseTargetParameter);
		}
	}
}