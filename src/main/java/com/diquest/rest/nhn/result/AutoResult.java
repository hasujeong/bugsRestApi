package com.diquest.rest.nhn.result;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.diquest.ir.common.exception.IRException;
import com.diquest.ir.common.msg.protocol.query.Query;
import com.diquest.ir.common.msg.protocol.query.QuerySet;
import com.diquest.ir.common.msg.protocol.query.SelectSet;
import com.diquest.ir.common.msg.protocol.result.Result;
import com.diquest.ir.common.msg.protocol.result.ResultSet;
import com.diquest.rest.nhn.common.Connection;
import com.diquest.rest.nhn.service.select.AutoTagSelectSet;

public class AutoResult {
	private static String currTimezone = new SimpleDateFormat("XXX").format(new Date()).replace(":", "");
	private static RestCommandExtractor restCommandExtractor = new RestCommandExtractor(Connection.IP, Connection.PORT);

	List<Item> result;

	public AutoResult(List<Item> result) {
		this.result = result;
	}
	
	public static AutoResult makeAutoTagResult(Query query, Result result, Map<String, String> params, String tag) {
		
		List<Item> items = new ArrayList<Item>();
		for (int i = 0; i < result.getRealSize(); i++) {
			items.add(new Item(result, query.getSelectFields(), params, i, tag));
		}
		return new AutoResult(items);
	}

	
//	private static class AutoData {
//		String type;
//		Integer ranking;
//		String artist_id;
//		String nation_cd;
//		String genre_cd;
//		String grp_cd;
//		String sex_cd;
//
//		public AutoData() {
//			this.type = "OK";
//		}
//		public AutoData(Result result, SelectSet[] selectSet, Map<String, String> params, int resultIdx) {
//			
//		}
//		
//		private List<String> makeTerms(String searchKeyword) throws IRException {
//			List<String> terms = new ArrayList<String>();
//			String[][] result = restCommandExtractor.request("KOREAN", "", searchKeyword);
//			for (String[] innerRst : result) {
//				if (innerRst != null) {
//					terms.addAll(Arrays.asList(innerRst));
//				}
//			}
//
//			return terms;
//		}
//		
//		private List<Item> makeArtist(Result result, SelectSet[] selectSet, Map<String, String> params) {
//			List<Item> items = new ArrayList<Item>();
//			for (int i = 0; i < result.getRealSize(); i++) {
//				items.add(new Item(result, selectSet, params, i));
//			}
//			return items;
//		}
//
//		private List<Item> makeItems(Result result, SelectSet[] selectSet, Map<String, String> params) {
//			List<Item> items = new ArrayList<Item>();
//			for (int i = 0; i < result.getRealSize(); i++) {
//				items.add(new Item(result, selectSet, params, i));
//			}
//			return items;
//		}
//
//	}

	private static class Item {
		String value;
		String type;
		String ranking;
		String tag_id;
		String tag_type;
		String weight;
		String esalbum_cnt;
		String albumreview_cnt;
		String special_cnt;
		String musiccast_cnt;
		
		public Item(Result result, SelectSet[] selectSet, Map<String, String> params, int resultIdx, String tag) {
			if(!tag.equalsIgnoreCase("")) {
				this.value = getValuetag(result, resultIdx);
				this.type = "TAG";
				this.weight = getRelevance(result, resultIdx);
				this.tag_id = getTagIdtag(result, resultIdx);
				this.esalbum_cnt = getEsalbum(result, resultIdx);
				this.albumreview_cnt = getAlbumreview(result, resultIdx);
				this.special_cnt = getSpecial(result, resultIdx);
				this.musiccast_cnt = getMusiccast(result, resultIdx);
			} else {
				this.value = getValue(result, resultIdx);
				this.type = "TAG";
				this.ranking = getRanking(result, resultIdx);
				this.tag_id = getTagId(result, resultIdx);
				this.tag_type = getType(result, resultIdx);
			}
		}
		
		private String getType(Result result, int resultIdx) {
			return AutoTagSelectSet.getInstance().getType(result, resultIdx);
		}
		
		private String getRelevance(Result result, int resultIdx) {
			return AutoTagSelectSet.getInstance().getRelevance(result, resultIdx);
		}

		private String getEsalbum(Result result, int resultIdx) {
			return AutoTagSelectSet.getInstance().getEsalbum(result, resultIdx);
		}
		
		private String getAlbumreview(Result result, int resultIdx) {
			return AutoTagSelectSet.getInstance().getAlbumreview(result, resultIdx);
		}
		
		private String getSpecial(Result result, int resultIdx) {
			return AutoTagSelectSet.getInstance().getSpecial(result, resultIdx);
		}
		
		private String getMusiccast(Result result, int resultIdx) {
			return AutoTagSelectSet.getInstance().getMusiccast(result, resultIdx);
		}
		
		private String getTagId(Result result, int resultIdx) {
			return AutoTagSelectSet.getInstance().getTagId(result, resultIdx);
		}
		
		private String getValue(Result result, int resultIdx) {
			return AutoTagSelectSet.getInstance().getValue(result, resultIdx);
		}
		
		private String getRanking(Result result, int resultIdx) {
			return AutoTagSelectSet.getInstance().getRanking(result, resultIdx);
		}
				
		private String getTagIdtag(Result result, int resultIdx) {
			return AutoTagSelectSet.getInstance().getTagIdtag(result, resultIdx);
		}
		
		private String getValuetag(Result result, int resultIdx) {
			return AutoTagSelectSet.getInstance().getValuetag(result, resultIdx);
		}
	}
	
	public static class itemList {
		List<Item> item;
		
		public itemList(List<Item> item) {
			this.item = item;
		}
	}

}
