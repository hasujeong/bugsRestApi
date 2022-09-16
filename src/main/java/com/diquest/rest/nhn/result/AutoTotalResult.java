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
import com.diquest.ir.common.msg.protocol.result.Result;
import com.diquest.ir.common.msg.protocol.result.ResultSet;
import com.diquest.rest.nhn.service.select.AutoTotalSelectSet;

public class AutoTotalResult {
	private static String currTimezone = new SimpleDateFormat("XXX").format(new Date()).replace(":", "");
	private static RestCommandExtractor restCommandExtractor = new RestCommandExtractor("alp-search.bugs.co.kr", 5555);

	Meta meta;

//	public AutoTotalResult(resultData result) {
//		this.meta = result;
//	}
	
	public AutoTotalResult(QuerySet q, ResultSet result, Map<String, String> map) throws IRException {
		List<Item> items = makeTotalItems(q, result, map); 
		List<ArtItem> artitems = makeArtistItems(q, result, map); 
		
//		System.out.println("@@@@@@@ items @@@@@ " + getValue(result.getResult(0), 0));
		
//		this.meta = new Meta(artitems, items, "");
		this.meta = new Meta(artitems, items, "");
	}
	
	public static AutoTotalResult makeAutoTotalResult(QuerySet query, ResultSet result, Map<String, String> map) throws IRException {
		
		return new AutoTotalResult(query,result, map);
	}
	
	private static class resultData {

	}
	
	private String getValue(Result result, int resultIdx) {
		return AutoTotalSelectSet.getInstance().getValue(result, resultIdx);
	}
	
	private List<Item> makeTotalItems(QuerySet q, ResultSet result, Map<String, String> map) throws IRException {
		List<Item> items = new ArrayList<Item>();
		
		System.out.println("--------- start1 --------- ");
		
//		for (int i = 0; i < result.resultSize(); i++) {
//			items.add(new Item(q.getQuery(i), result.getResult(i), map, i));
//		}
		
		for (int i = 0; i < result.getResult(1).getRealSize() ; i++) {
			items.add(new Item(q.getQuery(1), result.getResult(1), map, i));
		}
		
		System.out.println("-------- end ----------- ");
		return items;
	}
	
	private List<ArtItem> makeArtistItems(QuerySet q, ResultSet result, Map<String, String> map) throws IRException {
		List<ArtItem> items = new ArrayList<ArtItem>();
		
		System.out.println("--------- start2 --------- ");
		
		for (int i = 0; i < result.getResult(0).getRealSize(); i++) {
			items.add(new ArtItem(q.getQuery(0), result.getResult(0), map, i));
		}
		
		System.out.println("-------- end ----------- ");
		return items;
	}
	
//	private static class AutoData extends resultData {
//		Meta meta;
//		String value;
//		
////		public AutoData(){
////			List<Item> items = new ArrayList<>();
////			
////			this.itemCount = 0;
////			this.start = 1;
////			this.total = 0;
////			this.query = "";
////			this.domainCount = 0;
////			this.status = new Status();
////			this.itemList = new itemList2(items);
////		}
//				
//		public AutoData(QuerySet q, ResultSet result, Map<String, String> map) throws IRException {
//			List<Item> items = makeTotalItems(q, result, map); 
//			
//			System.out.println("@@@@@@@ items @@@@@ " + items);
//			
//			this.meta = new Meta(items, "");
//		}
//		
//		private String getValue(Result result, int resultIdx) {
//			return AutoTotalSelectSet.getInstance().getValue(result, resultIdx);
//		}
//		
//		private List<Item> makeTotalItems(QuerySet q, ResultSet result, Map<String, String> map) throws IRException {
//			List<Item> items = new ArrayList<Item>();
//			
//			System.out.println("@@@@@@@ start @@@@@ ");
//			
//			for (int i = 0; i < result.resultSize(); i++) {
//				items.add(new Item(q.getQuery(i), result.getResult(i), map, i));
//			}
//			
//			System.out.println("@@@@@@@ end @@@@@ ");
//			return items;
//		}
//
//	}

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
			System.out.println("############### " + resultIdx);
			
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
		List<ArtItem> artitems;
		List<Item> meta;
		String value;
		
		public Meta(List<ArtItem> artitems, List<Item> item, String value) {
			this.artitems = artitems;
			this.meta = item;
			this.value = value;
		}
	}

}
