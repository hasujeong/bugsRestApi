package com.diquest.rest.nhn.filter.result;

import java.util.ArrayList;
import java.util.List;

public class FilterFieldParseResult {
	List<FilterNormalValueResult> filterNormalValueResults;
	List<FilterRangeValueResult> filterRangeValueResults;

	public FilterFieldParseResult() {
		this.filterNormalValueResults = new ArrayList<FilterNormalValueResult>();
		this.filterRangeValueResults = new ArrayList<FilterRangeValueResult>();
	}

	public List<FilterNormalValueResult> getFilterNormalValueResults() {
		return filterNormalValueResults;
	}

	public List<FilterRangeValueResult> getFilterRangeValueResults() {
		return filterRangeValueResults;
	}

	public List<FilterNormalValueResult> getTriggerNormalFields(List<String> triggerFields) {
		List<FilterNormalValueResult> triggerFilters = new ArrayList<>();
		for (FilterNormalValueResult f : filterNormalValueResults) {
			if (f.containTriggerFieldNameOrValue(triggerFields)) {
				triggerFilters.add(f);
			}
		}
		return triggerFilters;
	}
	
	public List<FilterRangeValueResult> getTriggerRangeFields(List<String> triggerFields) {
		List<FilterRangeValueResult> triggerFilters = new ArrayList<>();
		for (FilterRangeValueResult f : filterRangeValueResults) {
			if (f.containTriggerFieldNameOrValue(triggerFields)) {
				triggerFilters.add(f);
			}
		}
		return triggerFilters;
	}

	public void removeNormalFields(List<FilterNormalValueResult> triggerNormalFields) {
		filterNormalValueResults.removeAll(triggerNormalFields);
	}

	public void removeRangeFields(List<FilterRangeValueResult> triggerRangeFields) {
		filterRangeValueResults.removeAll(triggerRangeFields);
	}

}
