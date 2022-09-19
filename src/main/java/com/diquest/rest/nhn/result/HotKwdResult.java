package com.diquest.rest.nhn.result;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.diquest.ir.common.exception.IRException;
import com.diquest.ir.common.msg.protocol.query.Query;
import com.diquest.ir.common.msg.protocol.query.SelectSet;
import com.diquest.ir.common.msg.protocol.result.Result;
import com.diquest.rest.nhn.common.Connection;
import com.diquest.rest.nhn.service.select.HotKwdSelectSet;

public class HotKwdResult {
	private static String currTimezone = new SimpleDateFormat("XXX").format(new Date()).replace(":", "");
	private static RestCommandExtractor restCommandExtractor = new RestCommandExtractor(Connection.IP, Connection.PORT);
	Header header;
	NhnData result;

	public HotKwdResult(Header header, NhnData result) {
		this.header = header;
		this.result = result;
	}
	
	public HotKwdResult(NhnData result) {
		this.result = result;
	}
	
	public static HotKwdResult makeEmptyResult() {
		return new HotKwdResult(new Header(true, currTimezone, "43"), new HotkwdData());
	}

	public static HotKwdResult makeResult(Query query, Result result, Map<String, String> map) throws IRException {
		return new HotKwdResult(new Header(true, currTimezone, "43"), new HotkwdData(query, result, map));
	}
	
	private static class NhnData {

	}

	private static class HotkwdData extends NhnData {
		Status status;
		int start;
		String query;
		List<String> terms;
		int total;
		int itemCount;
		itemList itemList;
//		List<Item> items;
		
		public HotkwdData(){
			List<Item> items = new ArrayList<>();
			
			this.status = new Status();
			this.start = 1;
			this.query = "";
			this.terms = new ArrayList<>();
			this.total = 0;
			this.itemCount = 0;
			this.itemList = new itemList(items);
		}

		public HotkwdData(Query q, Result result, Map<String, String> map) throws IRException {
			List<Item> items = makeItems(result, q.getSelectFields(), map); 
			
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
		String timezone;
		String version;

		public Header(boolean isSuccessful, String timezone, String version) {
			this.isSuccessful = isSuccessful;
			this.timezone = timezone;
			this.version = version;
		}

	}

	private static class Item {
		int ranking;
		int prevrack;
		int count;
		String keyword;

		public Item(Result result, SelectSet[] selectSet, Map<String, String> params, int resultIdx) {
			this.ranking = getRank(result, resultIdx);
			this.prevrack = getPrevRank(result, resultIdx);
			this.count = getCount(result, resultIdx);
			this.keyword = getKeyword(result, resultIdx);
		}

		private String getKeyword(Result result, int resultIdx) {
			return HotKwdSelectSet.getInstance().getKeyword(result, resultIdx);
		}

		private int getRank(Result result, int resultIdx) {
			return HotKwdSelectSet.getInstance().getRank(result, resultIdx);
		}
		
		private int getPrevRank(Result result, int resultIdx) {
			return HotKwdSelectSet.getInstance().getPrevRank(result, resultIdx);
		}
		
		private int getCount(Result result, int resultIdx) {
			return HotKwdSelectSet.getInstance().getCount(result, resultIdx);
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
