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

public class SayclubSelectSet {
	public static String _RANK = "_RANK";
	public static String _DOCID = "_DOCID";
	public static String WEIGHT = "WEIGHT";
	public static String ID = "ID";
	
	public static LinkedHashMap<String, Integer> sayCast_ArticleMap = new LinkedHashMap<String, Integer>();
	public static LinkedHashMap<String, Integer> sayCast_Map = new LinkedHashMap<String, Integer>();
	public static LinkedHashMap<String, Integer> sayMall_Map = new LinkedHashMap<String, Integer>();
	public static LinkedHashMap<String, Integer> allUser_Map = new LinkedHashMap<String, Integer>();
	public static LinkedHashMap<String, Integer> chatUser_Map = new LinkedHashMap<String, Integer>();

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
		sayMall_Map.put(_RANK, 0);
		sayMall_Map.put(_DOCID, 1);
		sayMall_Map.put(WEIGHT, 2);
		sayMall_Map.put(ID, 3);
		sayMall_Map.put("CAT1_ID", 4);
		sayMall_Map.put("CAT2_ID", 5);
		sayMall_Map.put("CAT3_ID", 6);
		sayMall_Map.put("CAT_ID", 7);
		sayMall_Map.put("CODE_STR", 8);
		sayMall_Map.put("FILE_NAME", 9);
		sayMall_Map.put("FROM_DATE", 10);
		sayMall_Map.put("IS_HEAD", 11);
		sayMall_Map.put("ITEM_ID", 12);
		sayMall_Map.put("ITEM_NAME", 13);
		sayMall_Map.put("ITEM_NAME_FOR_SORT", 14);
		sayMall_Map.put("LAYER", 15);
		sayMall_Map.put("PRICE", 16);
		sayMall_Map.put("PRODUCT_ID", 17);
		sayMall_Map.put("SELL_AMOUNT", 18);
		sayMall_Map.put("SEX", 19);
		sayMall_Map.put("USE_TYPE", 20);
	}
	static {
		allUser_Map.put(_RANK, 0);
		allUser_Map.put(_DOCID, 1);
		allUser_Map.put(WEIGHT, 2);
		allUser_Map.put(ID, 3);
		allUser_Map.put("ACCOUNT", 4);
		allUser_Map.put("BYEAR", 5);
		allUser_Map.put("CATEGORY_KEYWORD", 6);
		allUser_Map.put("CREGION", 7);
		allUser_Map.put("HIDDEN_SEX_BYEAR_CREGION", 8);
		allUser_Map.put("HIDDEN_USER_NAME", 9);
		allUser_Map.put("MSRL", 10);
		allUser_Map.put("SC_INFO", 11);
		allUser_Map.put("SEX", 12);
		allUser_Map.put("SEX_BYEAR_CREGION", 13);
		allUser_Map.put("USER_NAME", 14);
		allUser_Map.put("USER_NICKNAME", 15);
	}
	static {
		chatUser_Map.put(_RANK, 0);
		chatUser_Map.put(_DOCID, 1);
		chatUser_Map.put(WEIGHT, 2);
		chatUser_Map.put(ID, 3);
		chatUser_Map.put("ACCOUNT", 4);
		chatUser_Map.put("BYEAR", 5);
		chatUser_Map.put("CREGION", 6);
		chatUser_Map.put("CREGION_HIGH", 7);
		chatUser_Map.put("LOCATION", 8);
		chatUser_Map.put("LOCATION_WAIT", 9);
		chatUser_Map.put("MSG_ACCEPT", 10);
		chatUser_Map.put("MSRL", 11);
		chatUser_Map.put("SEX", 12);
		chatUser_Map.put("USER_NAME", 13);
		chatUser_Map.put("USER_NAME_OPENED", 14);
		chatUser_Map.put("USER_NICKNAME", 15);
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
		String collection = params.get("collection");
		if(collection.equalsIgnoreCase(SayclubCollections.SAYCAST_ART)) {
			list.addAll(getSayArtSelectList());
		} else if(collection.equalsIgnoreCase(SayclubCollections.SAYCAST)) {
			list.addAll(getSayCastSelectList());
		} else if(collection.equalsIgnoreCase(SayclubCollections.SAYMALL)) {
			list.addAll(getSayMallSelectList());
		} else if(collection.equalsIgnoreCase(SayclubCollections.ALLUSER)) {
			list.addAll(getAlluserSelectList());
		} else if(collection.equalsIgnoreCase(SayclubCollections.CHATUSER)) {
			list.addAll(getChatuserSelectList());
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
	
	private List<SelectSet> getSayMallSelectList() {
		List<SelectSet> sets = new ArrayList<SelectSet>();
		for (Entry<String, Integer> entry : sayMall_Map.entrySet()) {
			sets.add(new SelectSet(entry.getKey()));
		}
		return sets;
	}
	
	private List<SelectSet> getAlluserSelectList() {
		List<SelectSet> sets = new ArrayList<SelectSet>();
		for (Entry<String, Integer> entry : allUser_Map.entrySet()) {
			sets.add(new SelectSet(entry.getKey()));
		}
		return sets;
	}
	
	private List<SelectSet> getChatuserSelectList() {
		List<SelectSet> sets = new ArrayList<SelectSet>();
		for (Entry<String, Integer> entry : chatUser_Map.entrySet()) {
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
	
	public String getSaymall(Result result, int resultIdx, String field) {
		return String.valueOf(result.getResult(resultIdx, sayMall_Map.get(field)));
	}
	
	public String getAlluser(Result result, int resultIdx, String field) {
		return String.valueOf(result.getResult(resultIdx, allUser_Map.get(field)));
	}
	
	public String getChatuser(Result result, int resultIdx, String field) {
		return String.valueOf(result.getResult(resultIdx, chatUser_Map.get(field)));
	}
	
	public int getSayArtFieldSize() {
		return sayCast_ArticleMap.size();
	}

}
