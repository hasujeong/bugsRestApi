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

public class SelectSetService {
	public static String _RANK = "_RANK";
	public static String _DOCID = "_DOCID";
	public static String WEIGHT = "WEIGHT";
	public static String ID = "ID";
	public static LinkedHashMap<String, Integer> fixedFieldMap = new LinkedHashMap<String, Integer>();
	static {
		fixedFieldMap.put(_RANK, 0);
		fixedFieldMap.put(_DOCID, 1);
		fixedFieldMap.put(WEIGHT, 2);
		fixedFieldMap.put(ID, 3);
	}

	private static SelectSetService instance = null;

	public static SelectSetService getInstance() {
		if (instance == null) {
			instance = new SelectSetService();
		}
		return instance;
	}

	public SelectSet[] makeSelectSet(Map<String, String> params) {
		ArrayList<SelectSet> list = new ArrayList<SelectSet>();
		list.addAll(getFixedSelectList());
		list.addAll(getReturnParamSelectList(params));
		list.addAll(getTriggerSelectList(params));
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

	public String getId(Result result, int resultIdx) {
		return new String(result.getResult(resultIdx, fixedFieldMap.get(ID)));
	}

	public int getRelevance(Result result, int resultIdx) {
		return Integer.parseInt(String.valueOf(result.getResult(resultIdx, fixedFieldMap.get(WEIGHT))));
	}

	public String getDocId(Result result, int resultIdx) {
		return String.valueOf(result.getResult(resultIdx, fixedFieldMap.get(_DOCID)));
	}

	public int getRank(Result result, int resultIdx) {
		return Integer.parseInt(String.valueOf(result.getResult(resultIdx, fixedFieldMap.get(_RANK))));
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
