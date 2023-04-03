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
import com.diquest.rest.nhn.common.Collections;
import com.diquest.rest.nhn.common.Connection;
import com.diquest.rest.nhn.service.select.SayclubMusicSelectSet;

public class SayMusicResult {
	private static String currTimezone = new SimpleDateFormat("XXX").format(new Date()).replace(":", "");
	private static RestCommandExtractor restCommandExtractor = new RestCommandExtractor(Connection.IP, Connection.PORT);
	Header header;
	NhnData result;

	public SayMusicResult(Header header, NhnData result) {
		this.header = header;
		this.result = result;
	}
	
	public SayMusicResult(NhnData result) {
		this.result = result;
	}
	
	public static SayMusicResult makeEmptyResult() {
		return new SayMusicResult(new Header(true, currTimezone, "43"), new BugsData());
	}

	public static SayMusicResult makeSayMusicResult(Query query, Result result, Map<String, String> map, String collection) throws IRException {
		return new SayMusicResult(new Header(true, currTimezone, "43"), new BugsData(query, result, map, collection));
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
		String _ID;
		String TRACK_TITLE;
		String TRACK_ID;
		String ARTIST_ID;
		String ARTIST_NM;
		String LYRICS;
		
		public Item(Result result, SelectSet[] selectSet, Map<String, String> params, int resultIdx, String collection) {	
			if(collection.equalsIgnoreCase(Collections.TRACK)) {
				this.RANK = getSayTrack(result, resultIdx, "_RANK");
				this.DOCID = getSayTrack(result, resultIdx, "_DOCID");
				this.RELEVANCE = getSayTrack(result, resultIdx, "WEIGHT");
				this._ID = getSayTrack(result, resultIdx, "ID");
				this.TRACK_ID = getSayTrack(result, resultIdx, "TRACK_ID");
				this.TRACK_TITLE = getSayTrack(result, resultIdx, "TRACK_TITLE");
			} else if(collection.equalsIgnoreCase(Collections.ARTIST)) {
				this.RANK = getSayArtist(result, resultIdx, "_RANK");
				this.DOCID = getSayArtist(result, resultIdx, "_DOCID");
				this.RELEVANCE = getSayArtist(result, resultIdx, "WEIGHT");
				this._ID = getSayArtist(result, resultIdx, "ID");
				this.ARTIST_ID = getSayArtist(result, resultIdx, "ARTIST_ID");
				this.ARTIST_NM = getSayArtist(result, resultIdx, "ARTIST_NM");
			} else if(collection.equalsIgnoreCase(Collections.LYRICS)) {
				this.RANK = getSayLyrics(result, resultIdx, "_RANK");
				this.DOCID = getSayLyrics(result, resultIdx, "_DOCID");
				this.RELEVANCE = getSayLyrics(result, resultIdx, "WEIGHT");
				this._ID = getSayLyrics(result, resultIdx, "ID");
				this.TRACK_ID = getSayLyrics(result, resultIdx, "TRACK_ID");
				this.TRACK_TITLE = getSayLyrics(result, resultIdx, "TRACK_TITLE");
				this.ARTIST_NM = getSayLyrics(result, resultIdx, "ARTIST_NM");
				this.LYRICS = getSayLyrics(result, resultIdx, "LYRICS");
			}else {
				this.RANK = getSayTrack(result, resultIdx, "_RANK");
				this.DOCID = getSayTrack(result, resultIdx, "_DOCID");
				this.RELEVANCE = getSayTrack(result, resultIdx, "WEIGHT");
				this._ID = getSayTrack(result, resultIdx, "ID");
			}
			
		}
		private String getSayTrack(Result result, int resultIdx, String field) {
			return SayclubMusicSelectSet.getInstance().getSayTrack(result, resultIdx, field);
		}
		
		private String getSayArtist(Result result, int resultIdx, String field) {
			return SayclubMusicSelectSet.getInstance().getSayArtist(result, resultIdx, field);
		}
		
		private String getSayLyrics(Result result, int resultIdx, String field) {
			return SayclubMusicSelectSet.getInstance().getSayLyrics(result, resultIdx, field);
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
