package com.diquest.rest.nhn.service.trigger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.diquest.ir.common.msg.protocol.query.Query;
import com.diquest.ir.common.msg.protocol.query.SelectSet;
import com.diquest.ir.rest.common.exception.InvalidParameterException;
import com.diquest.ir.rest.common.object.RestHttpRequest;
import com.diquest.ir.rest.util.RestUtils;

public class TriggerFieldService {
	private static TriggerFieldService instance = null;

	public static TriggerFieldService getInstance() {
		if (instance == null) {
			instance = new TriggerFieldService();
		}
		return instance;
	}

	public void parseTrigger(Map<String, String> params, Query query, String collection) throws InvalidParameterException {
		StringBuffer sb = new StringBuffer();
		String sortValue = RestUtils.getParam(params, "sort");
		
		for (Entry<String, String> e : params.entrySet()) {
			if (e.getKey().startsWith("trigger.")) {
				String field = getFieldUpperCase(e.getKey());
				String param = e.getValue();
				assertParam(param);
				sb.append(field).append("#").append(param).append("\t");
			}
		}
		
		String sbStr = sb.toString();
		
		if(sortValue.equalsIgnoreCase("alias:price")) {
			if (sbStr.indexOf("_SALE_PRICE#") == -1) {
				sb.append("_SALE_PRICE#value,sale_price\t");
			} 
			if (sbStr.indexOf("_IS_RECENTLY_REG_20DAYS#") == -1) {
				sb.append("_IS_RECENTLY_REG_20DAYS#value,is_recently_reg_20days\t");
			} 
		} 
		
		if(collection.equalsIgnoreCase("BRANDI_PRODUCT")) {
			if(sortValue.equalsIgnoreCase("alias:price_within24")) {
				if (sbStr.indexOf("_SALE_PRICE_WITHIN24#") == -1) {
					sb.append("_SALE_PRICE_WITHIN24#value,sale_price_within24\t");
				} 
				if (sbStr.indexOf("_IS_RECENTLY_REG_20DAYS#") == -1) {
					sb.append("_IS_RECENTLY_REG_20DAYS#value,is_recently_reg_20days\t");
				} 
			} 
		}
		
		if (sb.length() > 0) {
			query.setValue("TRIGGER_FIELDS", sb.toString());
			query.setValue("TRIGGER_EXTENSION", "NHNExtension");
		}
	}

	private void assertParam(String param) throws InvalidParameterException {
		if(!param.contains(",")){
			throw new InvalidParameterException("trigger option invalid");
		}
	}

	public boolean isTriggerField(Map<String, String> params, String field) {
		return params.containsKey("trigger." + field.toLowerCase());
	}

	private String getFieldUpperCase(String field) {
		return field.substring(field.indexOf("trigger.") + 8).toUpperCase().trim();
	}

	private String getField(String field) {
		return field.substring(field.indexOf("trigger.") + 8).trim();
	}

	public List<SelectSet> makeSelectSet(Map<String, String> params) {
		List<SelectSet> result = new ArrayList<SelectSet>();
		for (Entry<String, String> e : params.entrySet()) {
			if (e.getKey().startsWith("trigger.")) {
				result.add(new SelectSet(getFieldUpperCase(e.getKey())));
			}
		}
		return result;
	}

	public List<String> getTriggerFieldNames(Map<String, String> params) {
		List<String> result = new ArrayList<String>();
		for (Entry<String, String> e : params.entrySet()) {
			if (e.getKey().startsWith("trigger.")) {
				result.add(getFieldUpperCase(e.getKey()));
			}
		}
		return result;
	}
}
