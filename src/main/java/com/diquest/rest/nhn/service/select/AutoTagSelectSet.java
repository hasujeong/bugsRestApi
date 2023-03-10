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

public class AutoTagSelectSet {
	public static String RANKING = "_RANK";
	public static String TAG_ID = "TAG_ID";
	public static String TAG_TYPE = "TAG_TYPE";
	public static String VALUE = "KWD";
	public static String WEIGHT = "WEIGHT";
	public static String ESALBUM_CNT = "ESALBUM_CNT";
	public static String ALBUMREVIEW_CNT = "ALBUMREVIEW_CNT";
	public static String SPECIAL_CNT = "SPECIAL_CNT";
	public static String MUSICCAST_CNT = "MUSICCAST_CNT";
	
	public static LinkedHashMap<String, Integer> fixedFieldMap = new LinkedHashMap<String, Integer>();
	static {
		fixedFieldMap.put(RANKING, 0);
		fixedFieldMap.put(TAG_ID, 1);
		fixedFieldMap.put(TAG_TYPE, 2);
		fixedFieldMap.put(VALUE, 3);
	}
	
	public static LinkedHashMap<String, Integer> fixedTagFieldMap = new LinkedHashMap<String, Integer>();
	static {
		fixedTagFieldMap.put(VALUE, 0);
		fixedTagFieldMap.put(WEIGHT, 1);
		fixedTagFieldMap.put(TAG_ID, 2);
		fixedTagFieldMap.put(ESALBUM_CNT, 3);
		fixedTagFieldMap.put(ALBUMREVIEW_CNT, 4);
		fixedTagFieldMap.put(SPECIAL_CNT, 5);
		fixedTagFieldMap.put(MUSICCAST_CNT, 6);		
	}

	private static AutoTagSelectSet instance = null;

	public static AutoTagSelectSet getInstance() {
		if (instance == null) {
			instance = new AutoTagSelectSet();
		}
		return instance;
	}

	public SelectSet[] makeSelectSet(Map<String, String> params) {
		ArrayList<SelectSet> list = new ArrayList<SelectSet>();
		list.addAll(getFixedSelectList(params));
		list.addAll(getReturnParamSelectList(params));
		
		return list.toArray(new SelectSet[list.size()]);
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

	private List<SelectSet> getFixedSelectList(Map<String, String> params) {
		String filter = RestUtils.getParam(params, "filter.use_type");
		List<SelectSet> sets = new ArrayList<SelectSet>();
		
		if(filter.equalsIgnoreCase("") || filter.equalsIgnoreCase("TAG")) {
			for (Entry<String, Integer> entry : fixedFieldMap.entrySet()) {
				sets.add(new SelectSet(entry.getKey()));
			}
		} else {
			for (Entry<String, Integer> entry : fixedTagFieldMap.entrySet()) {
				sets.add(new SelectSet(entry.getKey()));
			}
		}
		
		return sets;
	}

/*************** fixedFieldMap *************************/
	public String getType(Result result, int resultIdx) {
		return new String(result.getResult(resultIdx, fixedFieldMap.get(TAG_TYPE)));
	}
	
	public String getValue(Result result, int resultIdx) {
		return new String(result.getResult(resultIdx, fixedFieldMap.get(VALUE)));
	}
	
	public String getTagId(Result result, int resultIdx) {
		return new String(result.getResult(resultIdx, fixedFieldMap.get(TAG_ID)));
	}

	public String getRanking(Result result, int resultIdx) {
		return String.valueOf(result.getResult(resultIdx, fixedFieldMap.get(RANKING)));
	}
	/****************************************/	
	
/*************** fixedTagFieldMap *************************/		
	public String getValuetag(Result result, int resultIdx) {
		return new String(result.getResult(resultIdx, fixedTagFieldMap.get(VALUE)));
	}
	
	public String getTagIdtag(Result result, int resultIdx) {
		return new String(result.getResult(resultIdx, fixedTagFieldMap.get(TAG_ID)));
	}
	
	public String getMusiccast(Result result, int resultIdx) {
		return String.valueOf(result.getResult(resultIdx, fixedTagFieldMap.get(MUSICCAST_CNT)));
	}
	
	public String getSpecial(Result result, int resultIdx) {
		return String.valueOf(result.getResult(resultIdx, fixedTagFieldMap.get(SPECIAL_CNT)));
	}
	
	public String getAlbumreview(Result result, int resultIdx) {
		return String.valueOf(result.getResult(resultIdx, fixedTagFieldMap.get(ALBUMREVIEW_CNT)));
	}
	
	public String getEsalbum(Result result, int resultIdx) {
		return String.valueOf(result.getResult(resultIdx, fixedTagFieldMap.get(ESALBUM_CNT)));
	}
	
	public String getRelevance(Result result, int resultIdx) {
		return String.valueOf(result.getResult(resultIdx, fixedTagFieldMap.get(WEIGHT)));
	}
	/****************************************/

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
