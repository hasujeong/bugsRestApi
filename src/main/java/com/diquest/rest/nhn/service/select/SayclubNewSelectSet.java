package com.diquest.rest.nhn.service.select;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.diquest.ir.common.msg.protocol.Protocol;
import com.diquest.ir.common.msg.protocol.query.SelectSet;
import com.diquest.ir.common.msg.protocol.result.Result;
import com.diquest.ir.rest.util.RestUtils;
import com.diquest.ir.util.common.StringUtil;
import com.diquest.rest.nhn.common.Collections;
import com.diquest.rest.nhn.common.SayclubCollections;
import com.diquest.rest.nhn.service.select.SelectSetService.KeyValEntry;
import com.diquest.rest.nhn.service.trigger.TriggerFieldService;

public class SayclubNewSelectSet {
	public static String _RANK = "_RANK";
	public static String _DOCID = "_DOCID";
	public static String WEIGHT = "WEIGHT";
	public static String ID = "ID";
	
	public static LinkedHashMap<String, Integer> sayCast_ArticleMap = new LinkedHashMap<String, Integer>();
	public static LinkedHashMap<String, Integer> sayCast_Map = new LinkedHashMap<String, Integer>();
	public static LinkedHashMap<String, Integer> sayCastAuto_Map = new LinkedHashMap<String, Integer>();
	public static LinkedHashMap<String, Integer> sayCast_CjMap = new LinkedHashMap<String, Integer>();

	static {
		sayCast_ArticleMap.put(_RANK, 0);
		sayCast_ArticleMap.put(_DOCID, 1);
		sayCast_ArticleMap.put(WEIGHT, 2);
		sayCast_ArticleMap.put(ID, 3);
		sayCast_ArticleMap.put("ASRL", 4);
		sayCast_ArticleMap.put("BSRL", 5);
		sayCast_ArticleMap.put("CONTENT", 6);
		sayCast_ArticleMap.put("DOMAINID", 7);
		sayCast_ArticleMap.put("LASTUPDATE", 8);
		sayCast_ArticleMap.put("MSRL", 9);
		sayCast_ArticleMap.put("NICK", 10);
		sayCast_ArticleMap.put("REGDATE", 11);
		sayCast_ArticleMap.put("SUBJECT", 12);
		sayCast_ArticleMap.put("ROSECNT", 13);
		sayCast_ArticleMap.put("VIEWCNT", 14);
		sayCast_ArticleMap.put("TAILCNT", 15);
		sayCast_ArticleMap.put("HAVE_ROSE", 16);
		sayCast_ArticleMap.put("LUSRID", 17);
	}
	static {
		sayCastAuto_Map.put(_RANK, 0);
		sayCastAuto_Map.put(_DOCID, 1);
		sayCastAuto_Map.put(WEIGHT, 2);
		sayCastAuto_Map.put(ID, 3);
		sayCastAuto_Map.put("CASTNAME", 4);
		sayCastAuto_Map.put("DOMAINID", 5);
	}
	static {
		sayCast_Map.put(_RANK, 0);
		sayCast_Map.put(_DOCID, 1);
		sayCast_Map.put(WEIGHT, 2);
		sayCast_Map.put(ID, 3);
		sayCast_Map.put("CASTMENT", 4);
		sayCast_Map.put("CASTNAME", 5);
		sayCast_Map.put("CASTTYPE", 6);
		sayCast_Map.put("CATEGORY_AGE", 7);
		sayCast_Map.put("CATEGORY_GENRE", 8);
		sayCast_Map.put("CJGENDER", 9);
		sayCast_Map.put("CJID", 10);
		sayCast_Map.put("CJMSRL", 11);
		sayCast_Map.put("CJNAME", 12);
		sayCast_Map.put("DOMAINID", 13);
		sayCast_Map.put("LASTUPDATE", 14);
		sayCast_Map.put("ONAIR", 15);
		sayCast_Map.put("REGDATE", 16);
	}
	static {
		sayCast_CjMap.put(_RANK, 0);
		sayCast_CjMap.put(_DOCID, 1);
		sayCast_CjMap.put(WEIGHT, 2);
		sayCast_CjMap.put(ID, 3);
		sayCast_CjMap.put("CJMSRL", 4);
		sayCast_CjMap.put("PF", 5);
		sayCast_CjMap.put("CJNAME", 6);
		sayCast_CjMap.put("CJID", 7);
	}
	
	private static SayclubNewSelectSet instance = null;

	public static SayclubNewSelectSet getInstance() {
		if (instance == null) {
			instance = new SayclubNewSelectSet();
		}
		return instance;
	}

	public SelectSet[] makeSelectSet(Map<String, String> params) {
		ArrayList<SelectSet> list = new ArrayList<SelectSet>();
		String collection = params.get("collection");
		if(collection.equalsIgnoreCase(SayclubCollections.SAYCAST_ART)) {
			list.addAll(getSayArtSelectList(params));
		} else if(collection.equalsIgnoreCase(SayclubCollections.SAYCAST)) {
			list.addAll(getSayCastSelectList(params));
		} else if(collection.equalsIgnoreCase(SayclubCollections.SAYCAST_CJ)) {
			list.addAll(getSayCjSelectList(params));
		} else if(collection.equalsIgnoreCase(SayclubCollections.SAYCAST_AUTO)) {
			list.addAll(getSayCastAutoSelectList());
		} else {
			list.addAll(getReturnParamSelectList(params));
			list.addAll(getTriggerSelectList(params));
		}
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
	
	private List<SelectSet> getSayArtSelectList(Map<String, String> params) {
		String pass_field = "";
		int passage = 200;
		
		List<SelectSet> sets = new ArrayList<SelectSet>();
		for (Entry<String, Integer> entry : sayCast_ArticleMap.entrySet()) {
			if(entry.getKey().equalsIgnoreCase("SUBJECT") || entry.getKey().equalsIgnoreCase("CONTENT") || entry.getKey().equalsIgnoreCase("NICK") || entry.getKey().equalsIgnoreCase("LUSRID")) {
				String fieldNm = ("passage." + entry.getKey()).toLowerCase();
				
				pass_field = RestUtils.getParam(params, fieldNm);
				
				if(!pass_field.equalsIgnoreCase("")) {
					passage = Integer.parseInt(pass_field);
					
					sets.add(new SelectSet(entry.getKey(), (byte) (Protocol.SelectSet.SUMMARIZE | Protocol.SelectSet.HIGHLIGHT), passage));
				} else {
					sets.add(new SelectSet(entry.getKey(), Protocol.SelectSet.HIGHLIGHT));
				}
			} else {
				sets.add(new SelectSet(entry.getKey()));
			}
			
		}
		return sets;
	}

	private List<SelectSet> getSayCastSelectList(Map<String, String> params) {
		String pass_field = "";
		int passage = 200;
		
		List<SelectSet> sets = new ArrayList<SelectSet>();
		for (Entry<String, Integer> entry : sayCast_Map.entrySet()) {
			if(entry.getKey().equalsIgnoreCase("CASTMENT") || entry.getKey().equalsIgnoreCase("CASTNAME")) {
				String fieldNm = ("passage." + entry.getKey()).toLowerCase();
				
				pass_field = RestUtils.getParam(params, fieldNm);
				
				if(!pass_field.equalsIgnoreCase("")) {
					passage = Integer.parseInt(pass_field);
					
					sets.add(new SelectSet(entry.getKey(), (byte) (Protocol.SelectSet.SUMMARIZE | Protocol.SelectSet.HIGHLIGHT), passage));
				} else {
					sets.add(new SelectSet(entry.getKey(), Protocol.SelectSet.HIGHLIGHT));
				}
			} else {
				sets.add(new SelectSet(entry.getKey()));
			}
		}
		return sets;
	}
	
	private List<SelectSet> getSayCjSelectList(Map<String, String> params) {
		String pass_field = "";
		int passage = 200;
		
		List<SelectSet> sets = new ArrayList<SelectSet>();
		for (Entry<String, Integer> entry : sayCast_CjMap.entrySet()) {
			if(entry.getKey().equalsIgnoreCase("CJNAME") || entry.getKey().equalsIgnoreCase("CJID")) {
				String fieldNm = ("passage." + entry.getKey()).toLowerCase();
				
				pass_field = RestUtils.getParam(params, fieldNm);
				
				if(!pass_field.equalsIgnoreCase("")) {
					passage = Integer.parseInt(pass_field);
					
					sets.add(new SelectSet(entry.getKey(), (byte) (Protocol.SelectSet.SUMMARIZE | Protocol.SelectSet.HIGHLIGHT), passage));
				} else {
					sets.add(new SelectSet(entry.getKey(), Protocol.SelectSet.HIGHLIGHT));
				}
			} else {
				sets.add(new SelectSet(entry.getKey()));
			}
		}
		return sets;
	}
	
	private List<SelectSet> getSayCastAutoSelectList() {
		List<SelectSet> sets = new ArrayList<SelectSet>();
		for (Entry<String, Integer> entry : sayCastAuto_Map.entrySet()) {
			sets.add(new SelectSet(entry.getKey()));
		}
		return sets;
	}
	
	public String getSaycastArt(Result result, int resultIdx, String field) {
		return String.valueOf(result.getResult(resultIdx, sayCast_ArticleMap.get(field)));
	}
	
	public String getSaycast(Result result, int resultIdx, String field) {
		return String.valueOf(result.getResult(resultIdx, sayCast_Map.get(field)));
	}
	
	public String getSaycastCj(Result result, int resultIdx, String field) {
		return String.valueOf(result.getResult(resultIdx, sayCast_CjMap.get(field)));
	}
	
	public String getSayAuto(Result result, int resultIdx, String field) {
		return String.valueOf(result.getResult(resultIdx, sayCastAuto_Map.get(field)));
	}

	public int getSayArtFieldSize() {
		return sayCast_ArticleMap.size();
	}
	
	public String getId(Result result, int resultIdx) {
		return new String(result.getResult(resultIdx, sayCast_Map.get(ID)));
	}

	public int getRelevance(Result result, int resultIdx) {
		return Integer.parseInt(String.valueOf(result.getResult(resultIdx, sayCast_Map.get(WEIGHT))));
	}

	public String getDocId(Result result, int resultIdx) {
		return String.valueOf(result.getResult(resultIdx, sayCast_Map.get(_DOCID)));
	}

	public int getRank(Result result, int resultIdx) {
		return Integer.parseInt(String.valueOf(result.getResult(resultIdx, sayCast_Map.get(_RANK))));
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


}
