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
import com.diquest.ir.common.msg.protocol.query.QuerySet;
import com.diquest.ir.common.msg.protocol.query.SelectSet;
import com.diquest.ir.common.msg.protocol.result.Result;
import com.diquest.ir.common.msg.protocol.result.ResultSet;
import com.diquest.rest.nhn.common.Connection;
import com.diquest.rest.nhn.service.select.SelectSetService;
import com.diquest.rest.nhn.service.trigger.TriggerFieldService;

public class TotalResult {
	private static String currTimezone = new SimpleDateFormat("XXX").format(new Date()).replace(":", "");
	private static RestCommandExtractor restCommandExtractor = new RestCommandExtractor(Connection.IP, Connection.PORT);
	Header header;
	resultData result;

	public TotalResult(Header header, resultData result) {
		this.header = header;
		this.result = result;
	}
	
	public static TotalResult makeEmptyResult() {		
		return new TotalResult(new Header(true, currTimezone, "43"), new TotalData());
	}

	public static TotalResult makeTotalResult(QuerySet query, ResultSet result, Map<String, String> map) throws IRException {
		return new TotalResult(new Header(true, currTimezone, "43"), new TotalData(query,result, map));
	}

	private static class resultData {

	}

	private static class TotalData extends resultData {
		int itemCount;
		int start;
		int total;
		String query;
		int domainCount;
		Status status;
		itemList2 itemList;
		
		public TotalData(){
			List<TotalItem> items = new ArrayList<>();
			
			this.itemCount = 0;
			this.start = 1;
			this.total = 0;
			this.query = "";
			this.domainCount = 0;
			this.status = new Status();
			this.itemList = new itemList2(items);
		}
				
		public TotalData(QuerySet q, ResultSet result, Map<String, String> map) throws IRException {
			List<TotalItem> items = makeTotalItems(q, result, map); 
			
			int totalCnt = 0;
			
			for(int i=0; i < result.resultSize() ; i++) {
				totalCnt += result.getResult(i).getTotalSize();
			}
			
			this.itemCount = 10; //default 출력 count
			this.start = 1;
			this.total = totalCnt;
			this.query = String.valueOf(q.getQuery(0).getSearchKeyword());
			this.domainCount = q.querySize();
			this.status = new Status();
			this.itemList = new itemList2(items);
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
		
		private List<TotalItem> makeTotalItems(QuerySet q, ResultSet result, Map<String, String> map) throws IRException {
			List<TotalItem> items = new ArrayList<TotalItem>();
			
			for (int i = 0; i < result.resultSize(); i++) {
				items.add(new TotalItem(q.getQuery(i), result.getResult(i), map));
			}
			return items;
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
	
	private static class TotalItem {
		Status status;
		int start;
		String query;
		List<String> terms;
		int total;
		int itemCount;
		itemList itemList;
		String domain;

		public TotalItem(Query q, Result result, Map<String, String> map) throws IRException {
			List<Item> items = makeItems(result, q.getSelectFields(), map); 
			
			this.status = new Status(result);
			this.itemCount = result.getRealSize();
			this.itemList = new itemList(items);
			this.query = String.valueOf(q.getSearchKeyword());
			this.start = q.getResultStart() + 1;
			this.total = result.getTotalSize();
			this.terms = makeTerms(String.valueOf(q.getSearchKeyword()));
			this.domain = String.valueOf(q.getFromField());			// 컬렉션명
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

		private List<Item> makeItems(Result result, SelectSet[] selectSet, Map<String, String> params) {
			List<Item> items = new ArrayList<Item>();
			for (int i = 0; i < result.getRealSize(); i++) {
				items.add(new Item(result, selectSet, params, i));
			}
			return items;
		}
	}

	private static class Item {
		int rank;
		String docId;
		int relevance;
		String _ID;
		TreeMap<String, Object> source;
		LinkedHashMap<String, String> property;

		public Item(Result result, SelectSet[] selectSet, Map<String, String> params, int resultIdx) {
			this.rank = getRank(result, resultIdx);
			this.docId = getDocId(result, resultIdx);
			this.relevance = getRelevance(result, resultIdx);
			this._ID = getId(result, resultIdx);
			this.source = makeSource(result, selectSet, params, resultIdx);
			this.property = makeProperty(result, selectSet, params, resultIdx);
		}

		private String getId(Result result, int resultIdx) {
			return SelectSetService.getInstance().getId(result, resultIdx);
		}

		private int getRelevance(Result result, int resultIdx) {
			return SelectSetService.getInstance().getRelevance(result, resultIdx);
		}

		private String getDocId(Result result, int resultIdx) {
			return SelectSetService.getInstance().getDocId(result, resultIdx);
		}

		private int getRank(Result result, int resultIdx) {
			return SelectSetService.getInstance().getRank(result, resultIdx);
		}

		private TreeMap<String, Object> makeSource(Result result, SelectSet[] selectSet, Map<String, String> params, int resultIdx) {
			TreeMap<String, Object> source = new TreeMap<String, Object>();
			source.put("_" + SelectSetService.ID, SelectSetService.getInstance().getId(result, resultIdx));
			int start = SelectSetService.getInstance().getFixFieldSize();
			for (int j = start; j < selectSet.length; j++) {
				String key = new String(selectSet[j].getField());
				String value = new String(result.getResult(resultIdx, j));
				if (TriggerFieldService.getInstance().isTriggerField(params, key) || key.equals("_" + SelectSetService.ID)) {
					continue;
				}
				Entry<String, Object> keyVal = SelectSetService.getInstance().getSourceData(key, value);
				source.put(keyVal.getKey(), keyVal.getValue());
			}
			return source;
		}

		private LinkedHashMap<String, String> makeProperty(Result result, SelectSet[] selectSet, Map<String, String> params, int resultIdx) {
			LinkedHashMap<String, String> property = new LinkedHashMap<String, String>();
			int start = SelectSetService.getInstance().getFixFieldSize();
			for (int j = start; j < selectSet.length; j++) {
				if (TriggerFieldService.getInstance().isTriggerField(params, new String(selectSet[j].getField()))) {
					String key = new String(selectSet[j].getField());
					String value = new String(result.getResult(resultIdx, j));
					if (value.endsWith(".0")) {
						value = value.replaceAll(".0$", "");
					}
					property.put(key.toLowerCase(), value);
				}
			}
			return property.isEmpty() ? null : property;
		}
	}
	
	public static class Status {
		int code;
		String message;

		public Status() {
			this.code = 0;
			this.message = "OK";
		}
		public Status(Result result) {
			this.code = result.getErrorCode();
			if(code == 0) {
				this.message = "OK";
			} else {
				this.message = "ERROR";
			}
		}
	}
	
	public static class itemList {
		List<Item> item;
		
		public itemList(List<Item> item) {
			this.item = item;
		}
	}
	
	public static class itemList2 {
		List<TotalItem> item;
		
		public itemList2(List<TotalItem> item) {
			this.item = item;
		}
	}

	public static class Group {
		String id;
		String value;

		public Group(String id, String value) {
			this.id = id;
			this.value = value;
		}
	}
}
