package com.diquest.rest.nhn.result;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.diquest.ir.common.exception.IRException;
import com.diquest.ir.common.msg.protocol.query.Query;
import com.diquest.ir.common.msg.protocol.query.SelectSet;
import com.diquest.ir.common.msg.protocol.result.GroupResult;
import com.diquest.ir.common.msg.protocol.result.Result;
import com.diquest.rest.nhn.common.Connection;
import com.diquest.rest.nhn.common.SayclubCollections;
import com.diquest.rest.nhn.service.select.SayclubSelectSet;
import com.diquest.rest.nhn.service.trigger.TriggerFieldService;

public class SayMallResult {
	private static String currTimezone = new SimpleDateFormat("XXX").format(new Date()).replace(":", "");
	private static RestCommandExtractor restCommandExtractor = new RestCommandExtractor(Connection.IP, Connection.PORT);
	Header header;
	NhnData result;

	public SayMallResult(Header header, NhnData result) {
		this.header = header;
		this.result = result;
	}
	
	public SayMallResult(NhnData result) {
		this.result = result;
	}
	
	public static SayMallResult makeEmptyResult() {
		return new SayMallResult(new Header(true, currTimezone, "43"), new BugsData());
	}

	public static SayMallResult makeSayMallResult(Query query, Result result, Map<String, String> map, String collection) throws IRException {
		return new SayMallResult(new Header(true, currTimezone, "43"), new BugsData(query, result, map, collection));
	}
	
	private static class NhnData {

	}

	private static class BugsData extends NhnData {
		Status status;
		String start;
		String query;
		List<String> terms;
		String total;
		String itemCount;
		itemList itemList;
		summary summary;
		
		public BugsData(){
			List<Item> items = new ArrayList<>();
			LinkedHashMap<String, Integer> CAT2_ID = new LinkedHashMap<String, Integer>();
			
			this.status = new Status();
			this.start = "1";
			this.query = "";
			this.terms = new ArrayList<>();
			this.total = "0";
			this.itemCount = "0";
			this.itemList = new itemList(items);
			this.summary = new summary(CAT2_ID);
		}

		public BugsData(Query q, Result result, Map<String, String> params, String collection) throws IRException {
			List<Item> items = makeItems(result, q.getSelectFields(), params, collection); 
			LinkedHashMap<String, Integer> CAT2_ID = makeGroups(result, q.getSelectFields(), params, collection);
			
			this.status = new Status();
			this.itemCount = Integer.toString(result.getRealSize());
			this.itemList = new itemList(items);
			this.query = String.valueOf(q.getSearchKeyword());
			this.start = Integer.toString(q.getResultStart() + 1);
			this.total = Integer.toString(result.getTotalSize());
			this.terms = makeTerms(String.valueOf(q.getSearchKeyword()));
			this.summary = new summary(CAT2_ID);
		}

		private List<String> makeTerms(String searchKeyword) throws IRException {
			List<String> terms = new ArrayList<String>();
			String[][] result = restCommandExtractor.request("KOREAN", "", searchKeyword);
			for (String[] innerRst : result) {
				if (innerRst != null) {
					terms.addAll(Arrays.asList(innerRst));
				}
			}

			return terms;
		}

		private List<Item> makeItems(Result result, SelectSet[] selectSet, Map<String, String> params, String collection) {
			List<Item> items = new ArrayList<Item>();
			for (int i = 0; i < result.getRealSize(); i++) {
				items.add(new Item(result, selectSet, params, i, collection));
			}
			return items;
		}
		
		private LinkedHashMap<String, Integer> makeGroups(Result result, SelectSet[] selectSet, Map<String, String> params, String collection) {
			LinkedHashMap<String, Integer> cat2 = new LinkedHashMap<String, Integer>();
			
			GroupResult[] groupResults = null;
	
			groupResults = result.getGroupResults();
            
            for (int j = 0; j < groupResults.length; j++) {
				int rSize = groupResults[j].groupResultSize();
		
				for (int k = 0; k < rSize; k++) {
					String cate = new String(groupResults[j].getId(k));
					int value = groupResults[j].getIntValue(k);	
					
					cat2.put(cate, value);
				}
            }
			
			return cat2;
		}

	}

	public static class Header {
		boolean isSuccessful;
		String timezone;
		String version;

		public Header(boolean isSuccessful, String timezone, String version) {
			this.isSuccessful = isSuccessful;
			this.timezone = timezone;
			this.version = version;
		}

	}

	private static class Item {
		String RANK;
		String DOCID;
		String RELEVANCE;
		String ID;
		String CAT1_ID;
		String CAT2_ID;
		String CAT3_ID;
		String CAT_ID;
		String CODE_STR;
		String FILE_NAME;
		String FROM_DATE;
		String IS_HEAD;
		String ITEM_ID;
		String ITEM_NAME;
		String ITEM_NAME_FOR_SORT;
		String LAYER;
		String PRICE;
		String PRODUCT_ID;
		String SELL_AMOUNT;
		String SEX;
		String USE_TYPE;
		
		public Item(Result result, SelectSet[] selectSet, Map<String, String> params, int resultIdx, String collection) {	
			if(collection.equalsIgnoreCase(SayclubCollections.SAYMALL_OLD)) {
				this.RANK = getSaymall(result, resultIdx, "_RANK");
				this.DOCID = getSaymall(result, resultIdx, "_DOCID");
				this.RELEVANCE = getSaymall(result, resultIdx, "WEIGHT");
				this.ID = getSaymall(result, resultIdx, "ID");
				this.CAT1_ID = getSaymall(result, resultIdx, "CAT1_ID");
				this.CAT2_ID = getSaymall(result, resultIdx, "CAT2_ID");
				this.CAT3_ID = getSaymall(result, resultIdx, "CAT3_ID");
				this.CAT_ID = getSaymall(result, resultIdx, "CAT_ID");
				this.CODE_STR = getSaymall(result, resultIdx, "CODE_STR");
				this.FILE_NAME = getSaymall(result, resultIdx, "FILE_NAME");
				this.FROM_DATE = getSaymall(result, resultIdx, "FROM_DATE");
				this.IS_HEAD = getSaymall(result, resultIdx, "IS_HEAD");
				this.ITEM_ID = getSaymall(result, resultIdx, "ITEM_ID");
				this.ITEM_NAME = getSaymall(result, resultIdx, "ITEM_NAME");
				this.ITEM_NAME_FOR_SORT = getSaymall(result, resultIdx, "ITEM_NAME_FOR_SORT");
				this.LAYER = getSaymall(result, resultIdx, "LAYER");
				this.PRICE = getSaymall(result, resultIdx, "PRICE");
				this.PRODUCT_ID = getSaymall(result, resultIdx, "PRODUCT_ID");
				this.SELL_AMOUNT = getSaymall(result, resultIdx, "SELL_AMOUNT");
				this.SEX = getSaymall(result, resultIdx, "SEX");
				this.USE_TYPE = getSaymall(result, resultIdx, "USE_TYPE");
			} else {
				this.RANK = getSaycastArt(result, resultIdx, "_RANK");
				this.DOCID = getSaycastArt(result, resultIdx, "_DOCID");
				this.RELEVANCE = getSaycastArt(result, resultIdx, "WEIGHT");
				this.ID = getSaycastArt(result, resultIdx, "ID");
			}
			
		}
		private String getSaycastArt(Result result, int resultIdx, String field) {
			return SayclubSelectSet.getInstance().getSaycastArt(result, resultIdx, field);
		}
		
		private String getSaymall(Result result, int resultIdx, String field) {
			return SayclubSelectSet.getInstance().getSaymall(result, resultIdx, field);
		}
		
	}
	
	public static class Status {
		String code;
		String message;

		public Status() {
			this.code = "0";
			this.message = "OK";
		}
	}
	
	public static class itemList {
		List<Item> item;
		
		public itemList(List<Item> item) {
			this.item = item;
		}
	}

	public static class summary {
		LinkedHashMap<String, Integer> CAT2_ID;

		public summary(LinkedHashMap<String, Integer> CAT2_ID) {
			this.CAT2_ID = CAT2_ID;
		}
	}
}
