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

public class SayclubMusicSelectSet {
	public static String _RANK = "_RANK";
	public static String _DOCID = "_DOCID";
	public static String WEIGHT = "WEIGHT";
	public static String ID = "ID";
	
	public static LinkedHashMap<String, Integer> trackMap = new LinkedHashMap<String, Integer>();
	public static LinkedHashMap<String, Integer> artistMap = new LinkedHashMap<String, Integer>();

	static {
		trackMap.put(_RANK, 0);
		trackMap.put(_DOCID, 1);
		trackMap.put(WEIGHT, 2);
		trackMap.put(ID, 3);
		trackMap.put("TRACK_ID", 4);
		trackMap.put("TRACK_TITLE", 5);
	}
	static {
		artistMap.put(_RANK, 0);
		artistMap.put(_DOCID, 1);
		artistMap.put(WEIGHT, 2);
		artistMap.put(ID, 3);
		artistMap.put("ARTIST_NM", 4);
		artistMap.put("ARTIST_ID", 5);
	}
	
	private static SayclubMusicSelectSet instance = null;

	public static SayclubMusicSelectSet getInstance() {
		if (instance == null) {
			instance = new SayclubMusicSelectSet();
		}
		return instance;
	}

	public SelectSet[] makeSelectSet(Map<String, String> params) {
		ArrayList<SelectSet> list = new ArrayList<SelectSet>();
		String collection = params.get("collection");
		if(collection.equalsIgnoreCase(Collections.TRACK)) {
			list.addAll(getSayTrackSelectList());
		} else if(collection.equalsIgnoreCase(Collections.ARTIST)) {
			list.addAll(getSayArtistSelectList());
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
	
	private List<SelectSet> getSayTrackSelectList() {
		List<SelectSet> sets = new ArrayList<SelectSet>();
		for (Entry<String, Integer> entry : trackMap.entrySet()) {
			sets.add(new SelectSet(entry.getKey()));
		}
		return sets;
	}
	
	private List<SelectSet> getSayArtistSelectList() {
		List<SelectSet> sets = new ArrayList<SelectSet>();
		for (Entry<String, Integer> entry : artistMap.entrySet()) {
			sets.add(new SelectSet(entry.getKey()));
		}
		return sets;
	}
	
	public String getSayTrack(Result result, int resultIdx, String field) {
		return String.valueOf(result.getResult(resultIdx, trackMap.get(field)));
	}
	
	public String getSayArtist(Result result, int resultIdx, String field) {
		return String.valueOf(result.getResult(resultIdx, artistMap.get(field)));
	}
	
}
