package com.diquest.rest.nhn.filter.parse;

import java.util.Map;
import java.util.Map.Entry;

import com.diquest.rest.nhn.filter.result.FilterFieldParseResult;
import com.diquest.rest.nhn.filter.result.FilterNormalValueResult;
import com.diquest.rest.nhn.filter.result.FilterRangeValueResult;

public class SayclubFilterValue {
	private FilterFieldParseResult filterFieldParseResult;
	private Map<String, String> params;

	public SayclubFilterValue(Map<String, String> params) {
		this.params = params;
		this.filterFieldParseResult = new FilterFieldParseResult();
	}

	public FilterFieldParseResult parseAll() {
		for (Entry<String, String> param : params.entrySet()) {
			if (isFilterField(param.getKey())) {
				parseFilterField(param);
			}
		}

		return filterFieldParseResult;
	}

	private void parseFilterField(Entry<String, String> param) {
		String field = removeFilterPrefixAndUpperCase(param.getKey());
		String value = param.getValue();
		
		System.out.println("++++++++++++++++ " + field + " //// " + value);
		if (isRangeValue(value)) {
			addRangeValueResults(new FilterRangeValueParser(field, value).parseAll());
		} else {
			addNormalValueResults(new FilterNormalValueParser(field, value).parseAll());
		}
	}

	private void addNormalValueResults(FilterNormalValueResult parsed) {
		if (parsed != null) {
			filterFieldParseResult.getFilterNormalValueResults().add(parsed);
		}
	}

	private void addRangeValueResults(FilterRangeValueResult parsed) {
		if (parsed != null) {
			filterFieldParseResult.getFilterRangeValueResults().add(parsed);
		}
	}

	private boolean isRangeValue(String value) {
		return value.startsWith("{") || value.startsWith("[");
	}

	private boolean isFilterField(String key) {
		return key.startsWith("filter=");
	}

	protected String removeFilterPrefixAndUpperCase(String field) {
		return field.substring(field.indexOf("filter.") + 7).toUpperCase();
	}
}
