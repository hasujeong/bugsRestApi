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
import com.diquest.ir.common.msg.protocol.result.Result;
import com.diquest.rest.nhn.common.Connection;
import com.diquest.rest.nhn.service.select.SelectSetService;
import com.diquest.rest.nhn.service.trigger.TriggerFieldService;

public class SayclubResult {
	private static String currTimezone = new SimpleDateFormat("XXX").format(new Date()).replace(":", "");
	private static RestCommandExtractor restCommandExtractor = new RestCommandExtractor(Connection.IP, Connection.PORT);
	Header header;
	NhnData result;

	public SayclubResult(Header header, NhnData result) {
		this.header = header;
		this.result = result;
	}
	
	public SayclubResult(NhnData result) {
		this.result = result;
	}
	
	public static SayclubResult makeEmptyResult() {
		return new SayclubResult(new Header(true, currTimezone, "43"), new BugsData());
	}

	public static SayclubResult makeProductResult(Query query, Result result, Map<String, String> map) throws IRException {
		return new SayclubResult(new Header(true, currTimezone, "43"), new ProductData(query, result, map));
	}

	public static SayclubResult makeStoreResult(Query query, Result result, Map<String, String> map) throws IRException {
		return new SayclubResult(new Header(true, currTimezone, "43"), new BugsData(query, result, map));
	}
	
	private static class NhnData {

	}

	private static class ProductData extends NhnData {
		Status status;
		String start;
		String query;
		List<String> terms;
		String total;
		String itemCount;
		itemList itemList;
		
		public ProductData(){
			List<Item> items = new ArrayList<>();
			
			this.status = new Status();
			this.start = "1";
			this.query = "";
			this.terms = new ArrayList<>();
			this.total = "0";
			this.itemCount = "0";
			this.itemList = new itemList(items);
		}

		public ProductData(Query q, Result result, Map<String, String> params) throws IRException {
			List<Item> items = makeItems(result, q.getSelectFields(), params); 
			
			this.status = new Status();
			this.itemCount = Integer.toString(result.getRealSize());
			this.itemList = new itemList(items);
			this.query = String.valueOf(q.getSearchKeyword());
			this.start = Integer.toString(q.getResultStart() + 1);
			this.total = Integer.toString(result.getTotalSize());
			this.terms = makeTerms(String.valueOf(q.getSearchKeyword()));
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

	private static class BugsData extends NhnData {
		Status status;
		String start;
		String query;
		List<String> terms;
		String total;
		String itemCount;
		itemList itemList;
//		List<Item> items;
		
		public BugsData(){
			List<Item> items = new ArrayList<>();
			
			this.status = new Status();
			this.start = "1";
			this.query = "";
			this.terms = new ArrayList<>();
			this.total = "0";
			this.itemCount = "0";
			this.itemList = new itemList(items);
		}

		public BugsData(Query q, Result result, Map<String, String> map) throws IRException {
			List<Item> items = makeItems(result, q.getSelectFields(), map); 
						
			this.status = new Status();
			this.itemCount = Integer.toString(result.getRealSize());
			this.itemList = new itemList(items);
			this.query = String.valueOf(q.getSearchKeyword());
			this.start = Integer.toString(q.getResultStart() + 1);
			this.total = Integer.toString(result.getTotalSize());
			this.terms = makeTerms(String.valueOf(q.getSearchKeyword()));
		}

		private List<Status> makeStatus() {
			List<Status> status = new ArrayList<Status>();
			status.add(new Status());
			
			return status;
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

	public static class Header {
		boolean isSuccessful;
//		int resultCode;
//		String resultMessage;
		String timezone;
		String version;

//		public Header(boolean isSuccessful, int resultCode, String resultMessage, String timezone) {
		public Header(boolean isSuccessful, String timezone, String version) {
			this.isSuccessful = isSuccessful;
//			this.resultCode = resultCode;
//			this.resultMessage = resultMessage;
			this.timezone = timezone;
			this.version = version;
		}

	}

	private static class Item {
		String rank;
		String docId;
		String relevance;
		String _ID;
		TreeMap<String, Object> source;
		LinkedHashMap<String, String> property;

		public Item(Result result, SelectSet[] selectSet, Map<String, String> params, int resultIdx) {
			this.rank = Integer.toString(getRank(result, resultIdx));
			this.docId = getDocId(result, resultIdx);
			this.relevance = Integer.toString(getRelevance(result, resultIdx));
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

	public static class Group {
		String id;
		String value;

		public Group(String id, String value) {
			this.id = id;
			this.value = value;
		}
	}
}
