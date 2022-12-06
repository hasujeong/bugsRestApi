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
import com.diquest.rest.nhn.common.Connection;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class TotalsearchResult {
	private static String currTimezone = new SimpleDateFormat("XXX").format(new Date()).replace(":", "");
//	private static RestCommandExtractor restCommandExtractor = new RestCommandExtractor(Connection.IP, Connection.PORT);
	Header header;
	resultData result;

	public TotalsearchResult(Header header, resultData result) {
		this.header = header;
		this.result = result;
	}
	
	public static TotalsearchResult makeEmptyResult() {		
		return new TotalsearchResult(new Header(true, currTimezone, "43"), new TotalData());
	}

	public static TotalsearchResult makeTotalsearchResult(List<Map<Integer, Object>> totalList) throws IRException {
		return new TotalsearchResult(new Header(true, currTimezone, "43"), new TotalData(totalList));
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
				
		public TotalData(List<Map<Integer, Object>> totalList) throws IRException {
			List<TotalItem> items = makeTotalItems(totalList); 
			
			Gson gson = new Gson();
			
			int totalCnt = 0;
			
			JsonParser parser = new JsonParser();
            
            for(int j = 0 ; j < totalList.size() ; j++) {
            	Object obj = parser.parse((String) totalList.get(j).get(j)); 
            	
            	// obj를 우선 JSONObject에 담음
                JsonObject jsonMain = (JsonObject) obj;
                JsonObject resultMain = (JsonObject) jsonMain.get("result");

                query = gson.toJson(resultMain.get("query")).replaceAll("\"", "");
                totalCnt += Integer.parseInt(gson.toJson(resultMain.get("total")).replaceAll("\"", ""));
                
            }
			
			this.itemCount = 10; //default 출력 count
			this.start = 1;
			this.total = totalCnt;
			this.query = query;
			this.domainCount = totalList.size();
			this.status = new Status();
			this.itemList = new itemList2(items);
		}
		
		private List<TotalItem> makeTotalItems(List<Map<Integer, Object>> totalList) throws IRException {
			List<TotalItem> items = new ArrayList<TotalItem>();
			
			JsonParser parser = new JsonParser();
            
            for(int j = 0 ; j < totalList.size() ; j++) {
            	Object obj = parser.parse((String) totalList.get(j).get(j));
            	Object collection = totalList.get(j).get(j+100);
            	            	
            	items.add(new TotalItem(obj, collection.toString()));
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

		public TotalItem(Object obj, String collection) throws IRException {
			List<Item> items = makeItems(obj); 
			
			Gson gson = new Gson();
			
        	// obj를 우선 JSONObject에 담음
            JsonObject jsonMain = (JsonObject) obj;
            
            JsonObject resultMain = (JsonObject) jsonMain.get("result");
//            JsonObject itemMain = (JsonObject) resultMain.get("itemList");
            JsonObject statusList = (JsonObject) resultMain.get("status");

            query = gson.toJson(resultMain.get("query")).replaceAll("\"", "");
            			
			this.status = new Status(statusList);
			this.itemCount = Integer.parseInt(gson.toJson(resultMain.get("itemCount")).replaceAll("\"", ""));
			this.itemList = new itemList(items);
			this.query = query;
			this.start = 1;
			this.total = Integer.parseInt(gson.toJson(resultMain.get("total")).replaceAll("\"", ""));
			this.terms = makeTerms(query);
			this.domain = collection;			// 컬렉션명
		}

		private List<String> makeTerms(String searchKeyword) throws IRException {
			List<String> terms = new ArrayList<String>();
//			String[][] result = restCommandExtractor.request("KOREAN", "", searchKeyword);
//			for (String[] innerRst : result) {
//				if (innerRst != null) {
//					terms.addAll(Arrays.asList(innerRst));
//				}
//			}
			terms.add(searchKeyword);
			
			return terms;
		}

		private List<Item> makeItems(Object obj) {
			List<Item> items = new ArrayList<Item>();
			
			// obj를 우선 JSONObject에 담음
            JsonObject jsonMain = (JsonObject) obj;
            
            JsonObject resultMain = (JsonObject) jsonMain.get("result");
            JsonObject itemMain = (JsonObject) resultMain.get("itemList");

         // jsonObject에서 jsonArray를 get함
            JsonArray jsonArr = (JsonArray)itemMain.get("item");

         // jsonArr에서 하나씩 JSONObject로 cast해서 사용
             if (jsonArr.size() > 0){
                 for(int i=0; i<jsonArr.size(); i++){
                	 JsonObject jsonObj = (JsonObject)jsonArr.get(i);
                	 
                	 items.add(new Item(jsonObj));
                 }
         	}
			return items;
		}
	}

	private static class Item {
		int rank;
		String docId;
		int relevance;
		String _ID;

		public Item(JsonObject jsonObj) {
			Gson gson = new Gson();
			
			this.rank = Integer.parseInt(gson.toJson(jsonObj.get("rank")).replaceAll("\"", ""));
			this.docId = gson.toJson(jsonObj.get("docId")).replaceAll("\"", "");
			this.relevance = Integer.parseInt(gson.toJson(jsonObj.get("relevance")).replaceAll("\"", ""));
			this._ID = gson.toJson(jsonObj.get("_ID")).replaceAll("\"", "");
		}
	}
	
	public static class Status {
		int code;
		String message;
		
		public Status() {
			this.code = 0;
			this.message = "OK";
		}
		public Status(JsonObject statusList) {
			Gson gson = new Gson();
			
			this.code = Integer.parseInt(gson.toJson(statusList.get("code")).replaceAll("\"", ""));
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
}
