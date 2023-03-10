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
import com.diquest.rest.nhn.common.SayclubCollections;
import com.diquest.rest.nhn.service.select.SayclubNewSelectSet;

public class SayclubNewResult {
	private static String currTimezone = new SimpleDateFormat("XXX").format(new Date()).replace(":", "");
	private static RestCommandExtractor restCommandExtractor = new RestCommandExtractor(Connection.IP, Connection.PORT);
	Header header;
	NhnData result;

	public SayclubNewResult(Header header, NhnData result) {
		this.header = header;
		this.result = result;
	}
	
	public SayclubNewResult(NhnData result) {
		this.result = result;
	}
	
	public static SayclubNewResult makeEmptyResult() {
		return new SayclubNewResult(new Header(true, currTimezone, "43"), new BugsData());
	}

	public static SayclubNewResult makeSayclubResult(Query query, Result result, Map<String, String> map, String collection) throws IRException {
		return new SayclubNewResult(new Header(true, currTimezone, "43"), new BugsData(query, result, map, collection));
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

		public BugsData(Query q, Result result, Map<String, String> params, String collection) throws IRException {
			List<Item> items = makeItems(result, q.getSelectFields(), params, collection); 
			
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

		private List<Item> makeItems(Result result, SelectSet[] selectSet, Map<String, String> params, String collection) {
			List<Item> items = new ArrayList<Item>();
			for (int i = 0; i < result.getRealSize(); i++) {
				items.add(new Item(result, selectSet, params, i, collection));
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
		String RANK;
		String DOCID;
		String RELEVANCE;
		String ID;
		String ASRL;
		String BSRL;
		String CONTENT;
		String DOMAINID;
		String LASTUPDATE;
		String MSRL;
		String NICK;
		String REGDATE;
		String SUBJECT;
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
		String CASTMENT;
		String CASTNAME;
		String CASTTYPE;
		String CATEGORY_AGE;
		String CATEGORY_GENRE;
		String CJGENDER;
		String CJID;
		String CJMSRL;
		String CJNAME;
		String ONAIR;
		String ACCOUNT;
		String BYEAR;
		String CATEGORY_KEYWORD;
		String CREGION;
		String HIDDEN_SEX_BYEAR_CREGION;
		String HIDDEN_USER_NAME;
		String SC_INFO;
		String SEX_BYEAR_CREGION;
		String USER_NAME;
		String USER_NICKNAME;
		String CREGION_HIGH;
		String LOCATION;
		String LOCATION_WAIT;
		String MSG_ACCEPT;
		String USER_NAME_OPENED;
		String ROSECNT;
		String VIEWCNT;
		String TAILCNT;
		String HAVE_ROSE;
		String LUSRID;
		String PF;

		public Item(Result result, SelectSet[] selectSet, Map<String, String> params, int resultIdx, String collection) {	
			if(collection.equalsIgnoreCase(SayclubCollections.SAYCAST_ART)) {
				this.RANK = getSaycastArt(result, resultIdx, "_RANK");
				this.DOCID = getSaycastArt(result, resultIdx, "_DOCID");
				this.RELEVANCE = getSaycastArt(result, resultIdx, "WEIGHT");
				this.ID = getSaycastArt(result, resultIdx, "ID");
				this.ASRL = getSaycastArt(result, resultIdx, "ASRL");
				this.BSRL = getSaycastArt(result, resultIdx, "BSRL");
				this.CONTENT = getSaycastArt(result, resultIdx, "CONTENT");
				this.DOMAINID = getSaycastArt(result, resultIdx, "DOMAINID");
				this.LASTUPDATE = getSaycastArt(result, resultIdx, "LASTUPDATE");
				this.MSRL = getSaycastArt(result, resultIdx, "MSRL");
				this.NICK = getSaycastArt(result, resultIdx, "NICK");
				this.REGDATE = getSaycastArt(result, resultIdx, "REGDATE");
				this.SUBJECT = getSaycastArt(result, resultIdx, "SUBJECT");
				this.ROSECNT = getSaycastArt(result, resultIdx, "ROSECNT");
				this.VIEWCNT = getSaycastArt(result, resultIdx, "VIEWCNT");
				this.TAILCNT = getSaycastArt(result, resultIdx, "TAILCNT");
				this.HAVE_ROSE = getSaycastArt(result, resultIdx, "HAVE_ROSE");
				this.LUSRID = getSaycastArt(result, resultIdx, "LUSRID");
			} else if(collection.equalsIgnoreCase(SayclubCollections.SAYCAST)) {
				this.RANK = getSaycast(result, resultIdx, "_RANK");
				this.DOCID = getSaycast(result, resultIdx, "_DOCID");
				this.RELEVANCE = getSaycast(result, resultIdx, "WEIGHT");
				this.ID = getSaycast(result, resultIdx, "ID");
				this.CASTMENT = getSaycast(result, resultIdx, "CASTMENT");
				this.CASTNAME = getSaycast(result, resultIdx, "CASTNAME");
				this.CASTTYPE = getSaycast(result, resultIdx, "CASTTYPE");
				this.CATEGORY_AGE = getSaycast(result, resultIdx, "CATEGORY_AGE");
				this.CATEGORY_GENRE = getSaycast(result, resultIdx, "CATEGORY_GENRE");
				this.CJGENDER = getSaycast(result, resultIdx, "CJGENDER");
				this.CJID = getSaycast(result, resultIdx, "CJID");
				this.CJMSRL = getSaycast(result, resultIdx, "CJMSRL");
				this.CJNAME = getSaycast(result, resultIdx, "CJNAME");
				this.DOMAINID = getSaycast(result, resultIdx, "DOMAINID");
				this.LASTUPDATE = getSaycast(result, resultIdx, "LASTUPDATE");
				this.ONAIR = getSaycast(result, resultIdx, "ONAIR");
				this.REGDATE = getSaycast(result, resultIdx, "REGDATE");
			} else if(collection.equalsIgnoreCase(SayclubCollections.SAYCAST_CJ)) {
				this.RANK = getSaycastCj(result, resultIdx, "_RANK");
				this.DOCID = getSaycastCj(result, resultIdx, "_DOCID");
				this.RELEVANCE = getSaycastCj(result, resultIdx, "WEIGHT");
				this.ID = getSaycastCj(result, resultIdx, "ID");
				this.CJID = getSaycastCj(result, resultIdx, "CJID");
				this.CJMSRL = getSaycastCj(result, resultIdx, "CJMSRL");
				this.CJNAME = getSaycastCj(result, resultIdx, "CJNAME");
				this.PF = getSaycastCj(result, resultIdx, "PF");
			} else {
				this.RANK = getSaycastArt(result, resultIdx, "_RANK");
				this.DOCID = getSaycastArt(result, resultIdx, "_DOCID");
				this.RELEVANCE = getSaycastArt(result, resultIdx, "WEIGHT");
				this.ID = getSaycastArt(result, resultIdx, "ID");
			}
			
		}
		private String getSaycastArt(Result result, int resultIdx, String field) {
			return SayclubNewSelectSet.getInstance().getSaycastArt(result, resultIdx, field);
		}
		
		private String getSaycast(Result result, int resultIdx, String field) {
			return SayclubNewSelectSet.getInstance().getSaycast(result, resultIdx, field);
		}
		
		private String getSaycastCj(Result result, int resultIdx, String field) {
			return SayclubNewSelectSet.getInstance().getSaycastCj(result, resultIdx, field);
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
