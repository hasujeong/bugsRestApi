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

public class AutoTotalSelectSet {
	public static String TYPE = "TYPE";
	public static String RANKING = "_RANK";
	public static String ARTISTID = "ARTIST_ID";
	public static String NATIONCD = "NATION_CD";
	public static String GENRECD = "GENRE_CD";
	public static String GRP_CD = "GRP_CD";
	public static String SEX_CD = "SEX_CD";
	public static String VALUE = "KWD";
	public static String TRACKID = "TRACK_ID";
	public static String ALBUMID = "ALBUM_ID";
	
	public static LinkedHashMap<String, Integer> fixedFieldMap = new LinkedHashMap<String, Integer>();
	static {
		fixedFieldMap.put(TYPE, 0);
		fixedFieldMap.put(RANKING, 1);
		fixedFieldMap.put(ARTISTID, 2);
		fixedFieldMap.put(NATIONCD, 3);
		fixedFieldMap.put(GENRECD, 4);
		fixedFieldMap.put(GRP_CD, 5);
		fixedFieldMap.put(SEX_CD, 6);
		fixedFieldMap.put(VALUE, 7);
	}
	
	public static LinkedHashMap<String, Integer> fixedTotalFieldMap = new LinkedHashMap<String, Integer>();
	static {
		fixedTotalFieldMap.put(TYPE, 0);
		fixedTotalFieldMap.put(RANKING, 1);
		fixedTotalFieldMap.put(TRACKID, 2);
		fixedTotalFieldMap.put(ALBUMID, 3);
		fixedTotalFieldMap.put(ARTISTID, 4);
		fixedTotalFieldMap.put(VALUE, 5);
		
		fixedTotalFieldMap.put(NATIONCD, 6);
		fixedTotalFieldMap.put(GENRECD, 7);
		fixedTotalFieldMap.put(GRP_CD, 8);
		fixedTotalFieldMap.put(SEX_CD, 9);
	}

	private static AutoTotalSelectSet instance = null;

	public static AutoTotalSelectSet getInstance() {
		if (instance == null) {
			instance = new AutoTotalSelectSet();
		}
		return instance;
	}

	public SelectSet[] makeSelectSet(Map<String, String> params, int num) {
		ArrayList<SelectSet> list = new ArrayList<SelectSet>();
		list.addAll(getFixedSelectList(params, num));
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

	private List<SelectSet> getFixedSelectList(Map<String, String> params, int num) {
		List<SelectSet> sets = new ArrayList<SelectSet>();
		
		if(num==0) {
			for (Entry<String, Integer> entry : fixedFieldMap.entrySet()) {
				sets.add(new SelectSet(entry.getKey()));
			}
		} else {
			for (Entry<String, Integer> entry : fixedTotalFieldMap.entrySet()) {
				sets.add(new SelectSet(entry.getKey()));
			}
		}
		
		return sets;
	}

	/* ******** fixedFieldMap ************ */
	public String getType(Result result, int resultIdx) {
		return new String(result.getResult(resultIdx, fixedFieldMap.get(TYPE)));
	}

	public String getRanking(Result result, int resultIdx) {
		return String.valueOf(result.getResult(resultIdx, fixedFieldMap.get(RANKING)));
	}
	
	public String getArtist(Result result, int resultIdx) {
		return new String(result.getResult(resultIdx, fixedFieldMap.get(ARTISTID)));
	}
	
	public String getNation(Result result, int resultIdx) {
		return new String(result.getResult(resultIdx, fixedFieldMap.get(NATIONCD)));
	}
	
	public String getGenre(Result result, int resultIdx) {
		return new String(result.getResult(resultIdx, fixedFieldMap.get(GENRECD)));
	}
	
	public String getGrpcd(Result result, int resultIdx) {
		return new String(result.getResult(resultIdx, fixedFieldMap.get(GRP_CD)));
	}
	
	public String getSexcd(Result result, int resultIdx) {
		return new String(result.getResult(resultIdx, fixedFieldMap.get(SEX_CD)));
	}
	
	public String getValue(Result result, int resultIdx) {
		return new String(result.getResult(resultIdx, fixedFieldMap.get(VALUE)));
	}
	
	/* ******** fixedTotalFieldMap ************ */
	public String getTypetotal(Result result, int resultIdx) {
		return new String(result.getResult(resultIdx, fixedTotalFieldMap.get(TYPE)));
	}

	public String getRankingtotal(Result result, int resultIdx) {
		return String.valueOf(result.getResult(resultIdx, fixedTotalFieldMap.get(RANKING)));
	}
	
	public String getArtisttotal(Result result, int resultIdx) {
		return new String(result.getResult(resultIdx, fixedTotalFieldMap.get(ARTISTID)));
	}
	
	public String getTrack(Result result, int resultIdx) {
		return new String(result.getResult(resultIdx, fixedTotalFieldMap.get(TRACKID)));
	}
	
	public String getAlbum(Result result, int resultIdx) {
		return new String(result.getResult(resultIdx, fixedTotalFieldMap.get(ALBUMID)));
	}
	
	public String getValuetotal(Result result, int resultIdx) {
		return new String(result.getResult(resultIdx, fixedTotalFieldMap.get(VALUE)));
	}
	
	
	public String getNationtotal(Result result, int resultIdx) {
		return new String(result.getResult(resultIdx, fixedTotalFieldMap.get(NATIONCD)));
	}
	
	public String getGenretotal(Result result, int resultIdx) {
		return new String(result.getResult(resultIdx, fixedTotalFieldMap.get(GENRECD)));
	}
	
	public String getGrpcdtotal(Result result, int resultIdx) {
		return new String(result.getResult(resultIdx, fixedTotalFieldMap.get(GRP_CD)));
	}
	
	public String getSexcdtotal(Result result, int resultIdx) {
		return new String(result.getResult(resultIdx, fixedTotalFieldMap.get(SEX_CD)));
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
