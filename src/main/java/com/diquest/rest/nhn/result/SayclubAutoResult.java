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
import com.diquest.rest.nhn.service.select.SayclubNewSelectSet;

public class SayclubAutoResult {
	private static String currTimezone = new SimpleDateFormat("XXX").format(new Date()).replace(":", "");
	private static RestCommandExtractor restCommandExtractor = new RestCommandExtractor(Connection.IP, Connection.PORT);

	List<Item> result;

	public SayclubAutoResult(List<Item> result) {
		this.result = result;
	}
	
	public static SayclubAutoResult makeAutoTagResult(Query query, Result result, Map<String, String> params) {
		
		List<Item> items = new ArrayList<Item>();
		for (int i = 0; i < result.getRealSize(); i++) {
			items.add(new Item(result, query.getSelectFields(), params, i));
		}
		return new SayclubAutoResult(items);
	}

	private static class Item {
		String RANK;
		String DOCID;
		String RELEVANCE;
		String ID;
		String CASTNAME;
		String DOMAINID;
		
		public Item(Result result, SelectSet[] selectSet, Map<String, String> params, int resultIdx) {
			this.RANK = getSayAuto(result, resultIdx, "_RANK");
			this.DOCID = getSayAuto(result, resultIdx, "_DOCID");
			this.RELEVANCE = getSayAuto(result, resultIdx, "WEIGHT");
			this.ID = getSayAuto(result, resultIdx, "ID");
			this.CASTNAME = getSayAuto(result, resultIdx, "CASTNAME");
			this.DOMAINID = getSayAuto(result, resultIdx, "DOMAINID");
		}
		
		private String getSayAuto(Result result, int resultIdx, String field) {
			return SayclubNewSelectSet.getInstance().getSayAuto(result, resultIdx, field);
		}

	}
	
	public static class itemList {
		List<Item> item;
		
		public itemList(List<Item> item) {
			this.item = item;
		}
	}

}
