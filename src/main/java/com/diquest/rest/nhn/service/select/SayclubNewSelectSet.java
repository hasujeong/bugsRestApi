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
import com.diquest.rest.nhn.common.SayclubCollections;
import com.diquest.rest.nhn.service.trigger.TriggerFieldService;

public class SayclubNewSelectSet {
	public static String _RANK = "_RANK";
	public static String _DOCID = "_DOCID";
	public static String WEIGHT = "WEIGHT";
	public static String ID = "ID";
	
	public static LinkedHashMap<String, Integer> sayCast_ArticleMap = new LinkedHashMap<String, Integer>();
	public static LinkedHashMap<String, Integer> sayCast_Map = new LinkedHashMap<String, Integer>();
	public static LinkedHashMap<String, Integer> sayCastAuto_Map = new LinkedHashMap<String, Integer>();

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
			list.addAll(getSayArtSelectList());
		} else if(collection.equalsIgnoreCase(SayclubCollections.SAYCAST)) {
			list.addAll(getSayCastSelectList());
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
	
	private List<SelectSet> getSayArtSelectList() {
		List<SelectSet> sets = new ArrayList<SelectSet>();
		for (Entry<String, Integer> entry : sayCast_ArticleMap.entrySet()) {
			sets.add(new SelectSet(entry.getKey()));
		}
		return sets;
	}
	
	private List<SelectSet> getSayCastSelectList() {
		List<SelectSet> sets = new ArrayList<SelectSet>();
		for (Entry<String, Integer> entry : sayCast_Map.entrySet()) {
			sets.add(new SelectSet(entry.getKey()));
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

	public int getSayArtFieldSize() {
		return sayCast_ArticleMap.size();
	}

}
