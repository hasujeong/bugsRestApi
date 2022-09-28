package com.diquest.rest.nhn.result;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.diquest.ir.common.exception.IRException;
import com.diquest.ir.common.msg.protocol.query.Query;
import com.diquest.ir.common.msg.protocol.query.QuerySet;
import com.diquest.ir.common.msg.protocol.result.Result;
import com.diquest.ir.common.msg.protocol.result.ResultSet;
import com.diquest.rest.nhn.service.select.AutoTotalSelectSet;

public class AutoTotalResult {
	private static String currTimezone = new SimpleDateFormat("XXX").format(new Date()).replace(":", "");
	private static RestCommandExtractor restCommandExtractor = new RestCommandExtractor("alp-search.bugs.co.kr", 5555);

	Meta meta;
	
	public AutoTotalResult(QuerySet q, ResultSet result, Map<String, String> map) throws IRException {
		List<Map<String, Object>> items = makeTotalItems(q, result, map); 
		Map<String, Object> artitems = makeArtistItems(q, result, map); 
		items.add(0, artitems);
		
		this.meta = new Meta(items);
	}
	
	public static AutoTotalResult makeAutoTotalResult(QuerySet query, ResultSet result, Map<String, String> map) throws IRException {
		
		return new AutoTotalResult(query,result, map);
	}
		
	private String getValue(Result result, int resultIdx) {
		return AutoTotalSelectSet.getInstance().getValue(result, resultIdx);
	}
	
	private List<Map<String, Object>> makeTotalItems(QuerySet q, ResultSet result, Map<String, String> map) throws IRException {
		List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
		
		for (int i = 0; i < result.getResult(1).getRealSize() ; i++) {
			Map<String, Object> temp = new HashMap<>();
			temp.put("meta", new Item(q.getQuery(1), result.getResult(1), map, i));
			temp.put("value", new Item(q.getQuery(1), result.getResult(1), map, i).value);
			
			items.add(temp);
		}
		
		return items;
	}
	
	private Map<String, Object> makeArtistItems(QuerySet q, ResultSet result, Map<String, String> map) throws IRException {
		List<ArtItem> items = new ArrayList<ArtItem>();
		
		Map<String, Object> temp = new HashMap<>();
		String firstValue = "";
		
		for (int i = 0; i < result.getResult(0).getRealSize(); i++) {
			items.add(new ArtItem(q.getQuery(0), result.getResult(0), map, i));
			
			if (i == 0) {
				firstValue = new ArtItem(q.getQuery(0), result.getResult(0), map, i).value;
			}
		}
		
		temp.put("meta", items);
		temp.put("value", firstValue);
		
		return temp;
	}

	private static class Item {
		String type;
		String ranking;
		String artist_id;
		String nation_cd;
		String genre_cd;
		String grp_cd;
		String sex_cd;
		String track_id;
		String album_id;
		String value;
		
		public Item(Query q, Result result, Map<String, String> map, int resultIdx) {
			String tp = getTypetotal(result, resultIdx);

			if(tp.equalsIgnoreCase("TR")) {
				this.type = getTypetotal(result, resultIdx);
				this.ranking = getRankingtotal(result, resultIdx);
				this.track_id = getTrack(result, resultIdx);
				this.album_id = getAlbum(result, resultIdx);
				this.artist_id = getArtisttotal(result, resultIdx);
				this.value = getValuetotal(result, resultIdx);
			} else if(tp.equalsIgnoreCase("AL")) {
				this.type = getTypetotal(result, resultIdx);
				this.ranking = getRankingtotal(result, resultIdx);
				this.album_id = getAlbum(result, resultIdx);
				this.artist_id = getArtisttotal(result, resultIdx);
				this.value = getValuetotal(result, resultIdx);
			} else if(tp.equalsIgnoreCase("TR_AR")) {
				this.type = getTypetotal(result, resultIdx);
				this.ranking = getRankingtotal(result, resultIdx);
				this.track_id = getTrack(result, resultIdx);
				this.album_id = getAlbum(result, resultIdx);
				this.artist_id = getArtisttotal(result, resultIdx);
				this.value = getValuetotal(result, resultIdx);
			} else if(tp.equalsIgnoreCase("AR")) {
				this.type = getTypetotal(result, resultIdx);
				this.ranking = getRankingtotal(result, resultIdx);
				this.artist_id = getArtisttotal(result, resultIdx);
				this.nation_cd = getNationtotal(result, resultIdx);
				this.genre_cd = getGenretotal(result, resultIdx);
				this.grp_cd = getGrpcdtotal(result, resultIdx);
				this.sex_cd = getSexcdtotal(result, resultIdx);
				this.value = getValuetotal(result, resultIdx);
			} else {
				this.type = getTypetotal(result, resultIdx);
				this.ranking = getRankingtotal(result, resultIdx);
				this.album_id = getAlbum(result, resultIdx);
				this.artist_id = getArtisttotal(result, resultIdx);
				this.value = getValuetotal(result, resultIdx);
			} 
		}
		
		private String getTypetotal(Result result, int resultIdx) {
			return AutoTotalSelectSet.getInstance().getTypetotal(result, resultIdx);
		}
		private String getRankingtotal(Result result, int resultIdx) {
			return AutoTotalSelectSet.getInstance().getRankingtotal(result, resultIdx);
		}
		private String getArtisttotal(Result result, int resultIdx) {
			return AutoTotalSelectSet.getInstance().getArtisttotal(result, resultIdx);
		}
		private String getAlbum(Result result, int resultIdx) {
			return AutoTotalSelectSet.getInstance().getAlbum(result, resultIdx);
		}
		private String getTrack(Result result, int resultIdx) {
			return AutoTotalSelectSet.getInstance().getTrack(result, resultIdx);
		}
		private String getValuetotal(Result result, int resultIdx) {
			return AutoTotalSelectSet.getInstance().getValuetotal(result, resultIdx);
		}

		private String getNationtotal(Result result, int resultIdx) {
			return AutoTotalSelectSet.getInstance().getNationtotal(result, resultIdx);
		}
		private String getGenretotal(Result result, int resultIdx) {
			return AutoTotalSelectSet.getInstance().getGenretotal(result, resultIdx);
		}
		private String getGrpcdtotal(Result result, int resultIdx) {
			return AutoTotalSelectSet.getInstance().getGrpcdtotal(result, resultIdx);
		}
		private String getSexcdtotal(Result result, int resultIdx) {
			return AutoTotalSelectSet.getInstance().getSexcdtotal(result, resultIdx);
		}
	}
	
	private static class ArtItem {
		String type;
		String ranking;
		String artist_id;
		String nation_cd;
		String genre_cd;
		String grp_cd;
		String sex_cd;
		String track_id;
		String album_id;
		String value;
		
		public ArtItem(Query q, Result result, Map<String, String> map, int resultIdx) {
			this.type = "EA"; //getType(result, resultIdx);
			this.ranking = getRanking(result, resultIdx);
			this.artist_id = getArtist(result, resultIdx);
			this.nation_cd = getNation(result, resultIdx);
			this.genre_cd = getGenre(result, resultIdx);
			this.grp_cd = getGrpcd(result, resultIdx);
			this.sex_cd = getSexcd(result, resultIdx);
			this.value = getValue(result, resultIdx);
		}
		
		private String getType(Result result, int resultIdx) {
			return AutoTotalSelectSet.getInstance().getType(result, resultIdx);
		}
		private String getRanking(Result result, int resultIdx) {
			return AutoTotalSelectSet.getInstance().getRanking(result, resultIdx);
		}
		private String getValue(Result result, int resultIdx) {
			return AutoTotalSelectSet.getInstance().getValue(result, resultIdx);
		}
		private String getArtist(Result result, int resultIdx) {
			return AutoTotalSelectSet.getInstance().getArtist(result, resultIdx);
		}
		private String getNation(Result result, int resultIdx) {
			return AutoTotalSelectSet.getInstance().getNation(result, resultIdx);
		}
		private String getGenre(Result result, int resultIdx) {
			return AutoTotalSelectSet.getInstance().getGenre(result, resultIdx);
		}
		private String getGrpcd(Result result, int resultIdx) {
			return AutoTotalSelectSet.getInstance().getGrpcd(result, resultIdx);
		}
		private String getSexcd(Result result, int resultIdx) {
			return AutoTotalSelectSet.getInstance().getSexcd(result, resultIdx);
		}
	}
	
	public static class Meta {
		List<Map<String, Object>> result;
		
		public Meta(List<Map<String, Object>> item) {
			this.result = item;
		}
	}

}