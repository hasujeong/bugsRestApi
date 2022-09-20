package com.diquest.rest.nhn.service.select;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.diquest.ir.common.msg.protocol.query.SelectSet;
import com.diquest.ir.common.msg.protocol.result.Result;
import com.diquest.ir.rest.util.RestUtils;
import com.diquest.ir.util.common.StringUtil;
import com.diquest.rest.nhn.service.trigger.TriggerFieldService;

public class HotKwdSelectSet {
	public static String RANKING = "RANKING";
	public static String PREV_RANK = "PREV_RANK";
	public static String COUNT = "COUNT";
	public static String KEYWORD = "KEYWORD";

	public static LinkedHashMap<String, Integer> fixedFieldMap = new LinkedHashMap<String, Integer>();
	static {
		fixedFieldMap.put(RANKING, 0);
		fixedFieldMap.put(PREV_RANK, 1);
		fixedFieldMap.put(COUNT, 2);
		fixedFieldMap.put(KEYWORD, 3);
	}

	private static HotKwdSelectSet instance = null;

	public static HotKwdSelectSet getInstance() {
		if (instance == null) {
			instance = new HotKwdSelectSet();
		}
		return instance;
	}

	public SelectSet[] makeSelectSet(Map<String, String> params) {
		ArrayList<SelectSet> list = new ArrayList<SelectSet>();
		list.addAll(getFixedSelectList());
		list.addAll(getReturnParamSelectList(params));
//		list.addAll(getTriggerSelectList(params));
		return list.toArray(new SelectSet[list.size()]);
	}

	private List<SelectSet> getTriggerSelectList(Map<String, String> params) {
		return TriggerFieldService.getInstance().makeSelectSet(params);
	}

	private List<SelectSet> getReturnParamSelectList(Map<String, String> params) {
		ArrayList<SelectSet> list = new ArrayList<SelectSet>();
		String ret = RestUtils.getParam(params, "return");
		for (String r : ret.split(",")) {
			if (!StringUtil.isEmpty(r)) {
				list.add(new SelectSet(r.toUpperCase()));
			}
		}
		return list;
	}

	private List<SelectSet> getFixedSelectList() {
		List<SelectSet> sets = new ArrayList<SelectSet>();
		for (Entry<String, Integer> entry : fixedFieldMap.entrySet()) {
			sets.add(new SelectSet(entry.getKey()));
		}
		return sets;
	}

	public int getRank(Result result, int resultIdx) {
		return Integer.parseInt(String.valueOf(result.getResult(resultIdx, fixedFieldMap.get(RANKING))));
	}
	
	public int getCount(Result result, int resultIdx) {
		return Integer.parseInt(String.valueOf(result.getResult(resultIdx, fixedFieldMap.get(COUNT))));
	}
	
	public int getPrevRank(Result result, int resultIdx) {
		return Integer.parseInt(String.valueOf(result.getResult(resultIdx, fixedFieldMap.get(PREV_RANK))));
	}
	
	public String getKeyword(Result result, int resultIdx) {
		return new String(result.getResult(resultIdx, fixedFieldMap.get(KEYWORD)));
	}
	
	public Entry<String, Object> getSourceData(String key, String value) {
		return new KeyValEntry(key.toLowerCase(), value);
	}

	class KeyValEntry implements Entry<String, Object> {
		String key;
		Object value;

		public KeyValEntry(String key, Object value) {
			this.key = key;
			this.value = value;
		}

		public String getKey() {
			return this.key;
		}

		public Object getValue() {
			return this.value;
		}

		public Object setValue(Object value) {
			this.value = value;
			return value;
		}

	}

	public int getFixFieldSize() {
		return fixedFieldMap.size();
	}

}
