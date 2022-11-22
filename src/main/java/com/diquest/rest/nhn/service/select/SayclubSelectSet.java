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
import com.diquest.rest.nhn.common.Collections;
import com.diquest.rest.nhn.service.trigger.TriggerFieldService;

public class SayclubSelectSet {
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

	private static SayclubSelectSet instance = null;

	public static SayclubSelectSet getInstance() {
		if (instance == null) {
			instance = new SayclubSelectSet();
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
		
		if(ret.equalsIgnoreCase("_ALL")) {
			String collection = params.get("collection");
			
			if(collection.equalsIgnoreCase(Collections.TRACK)) {
				ret = "TRACK_ID,TRACK_TITLE,ALBUM_ID,ALBUM_TITLE,ARTIST_NM,DISP_NM,ENG_NM,INST_YN,KOR_NM,MASTER_STR_RIGHTS_YN,RELEASE_YMD,SCORE,SEARCH_EXCLUDE_YN,SEARCH_NM,SEARCH_TITLE,STATUS,SYNONYM_NM";
			} else if(collection.equalsIgnoreCase(Collections.ARTIST)) {
				ret = "ARTIST_ID,ARTIST_NM,ARTIST_TYPE,DISP_NM,ENG_NM,GRP_NM,GRP_SEARCH_NM, KOR_NM,SCORE,SEARCH_EXCLUDE_YN,SEARCH_NM,STATUS,SYNONYM_NM";
			} else if(collection.equalsIgnoreCase(Collections.MV)) {
				ret = "ARTIST_NM,DISP_NM,MASTER_MV_STR_RIGHTS_YN,MV_ID,MV_TITLE,RELEASE_YMD,SCORE,SEARCH_EXCLUDE_YN,SEARCH_NM,SEARCH_TITLE,STATUS,SVC_FULLHD_YN,TITLE,TRACK_TITLE";
			} else if(collection.equalsIgnoreCase(Collections.MUSICCAST)) {
				ret = "RELEASE_YMD,STATUS,TITLE,UNICONTENT_ID,UNICONTENT_R_ID,UPD_DT";
			}
			
			for (String r : ret.split(",")) {
				if (!StringUtil.isEmpty(r)) {
					list.add(new SelectSet(r.toUpperCase()));
				}
			}
		} else {
			for (String r : ret.split(",")) {
				if (!StringUtil.isEmpty(r)) {
					list.add(new SelectSet(r.toUpperCase()));
				}
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
