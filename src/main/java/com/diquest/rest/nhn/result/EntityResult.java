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
import com.diquest.rest.nhn.service.select.EntitySelectSet;

public class EntityResult {
	private static String currTimezone = new SimpleDateFormat("XXX").format(new Date()).replace(":", "");
	private static RestCommandExtractor restCommandExtractor = new RestCommandExtractor(Connection.IP, Connection.PORT);
	Header header;
	NhnData result;

	public EntityResult(Header header, NhnData result) {
		this.header = header;
		this.result = result;
	}
	
	public EntityResult(NhnData result) {
		this.result = result;
	}
	
	public static EntityResult makeEmptyResult() {
		return new EntityResult(new Header(true, currTimezone, "43"), new BugsData());
	}

	public static EntityResult makeEntityResult(Query query, Result result) throws IRException {
		return new EntityResult(new Header(true, currTimezone, "43"), new BugsData(query, result));
	}
	
	private static class NhnData {

	}

	private static class BugsData extends NhnData {
		Status status;
		int start;
		String query;
		List<String> terms;
		int total;
		int itemCount;
		itemList itemList;
//		List<Item> items;
		
		public BugsData(){
			List<Item> items = new ArrayList<>();
			
			this.status = new Status();
			this.start = 1;
			this.query = "";
			this.terms = new ArrayList<>();
			this.total = 0;
			this.itemCount = 0;
			this.itemList = new itemList(items);
		}

		public BugsData(Query q, Result result) throws IRException {
			List<Item> items = makeItems(result, q.getSelectFields()); 
			
			this.status = new Status();
			this.itemCount = result.getRealSize();
			this.itemList = new itemList(items);
			this.query = String.valueOf(q.getSearchKeyword());
			this.start = q.getResultStart() + 1;
			this.total = result.getTotalSize();
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

		private List<Item> makeItems(Result result, SelectSet[] selectSet) {
			List<Item> items = new ArrayList<Item>();
			for (int i = 0; i < result.getRealSize(); i++) {
				items.add(new Item(result, selectSet, i));
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

	private static class Item {
		int rank;
		String docId;
		int relevance;
		String _ID;
		TreeMap<String, Object> source;

		public Item(Result result, SelectSet[] selectSet, int resultIdx) {
			this.rank = getRank(result, resultIdx);
			this.docId = getDocId(result, resultIdx);
			this.relevance = getRelevance(result, resultIdx);
			this._ID = getId(result, resultIdx);
			this.source = makeSource(result, selectSet, resultIdx);
		}

		private String getId(Result result, int resultIdx) {
			return EntitySelectSet.getInstance().getId(result, resultIdx);
		}

		private int getRelevance(Result result, int resultIdx) {
			return EntitySelectSet.getInstance().getRelevance(result, resultIdx);
		}

		private String getDocId(Result result, int resultIdx) {
			return EntitySelectSet.getInstance().getDocId(result, resultIdx);
		}

		private int getRank(Result result, int resultIdx) {
			return EntitySelectSet.getInstance().getRank(result, resultIdx);
		}
				
		private TreeMap<String, Object> makeSource(Result result, SelectSet[] selectSet, int resultIdx) {
			TreeMap<String, Object> source = new TreeMap<String, Object>();
			source.put("_" + EntitySelectSet.ID, EntitySelectSet.getInstance().getId(result, resultIdx));
			int start = EntitySelectSet.getInstance().getFixFieldSize();
			for (int j = start; j < selectSet.length; j++) {
				String key = new String(selectSet[j].getField());
				String value = new String(result.getResult(resultIdx, j));
				
				Entry<String, Object> keyVal = EntitySelectSet.getInstance().getSourceData(key, value);
				source.put(keyVal.getKey(), keyVal.getValue());
			}
			return source;
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
