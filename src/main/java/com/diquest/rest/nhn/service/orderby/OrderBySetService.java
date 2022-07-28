package com.diquest.rest.nhn.service.orderby;

import com.diquest.ir.common.msg.protocol.Protocol;
import com.diquest.ir.common.msg.protocol.query.OrderBySet;
import com.diquest.rest.nhn.service.group.GroupBySetService;

public class OrderBySetService {

	private static OrderBySetService instance = null;

	public static OrderBySetService getInstance() {
		if (instance == null) {
			instance = new OrderBySetService();
		}
		return instance;
	}

	public OrderBySet getOrderBySet(String value, String collection) {
	
		String[] ArrayValue = value.split(":");
		
		if (ArrayValue[0].equalsIgnoreCase("track_title")) {
			if(ArrayValue[1].equalsIgnoreCase("asc")) {
				return new OrderBySet(true, "TRACK_TITLE", Protocol.TriggerSet.OrderBy.OP_POSTWEIGHT);
			} else {
				return new OrderBySet(false, "TRACK_TITLE", Protocol.TriggerSet.OrderBy.OP_POSTWEIGHT);
			}
		} else if (ArrayValue[0].equalsIgnoreCase("release_ymd")) {
			if(ArrayValue[1].equalsIgnoreCase("asc")) {
				return new OrderBySet(false, "RELEASE_YMD", Protocol.TriggerSet.OrderBy.OP_POSTWEIGHT);
			} else {
				return new OrderBySet(true, "RELEASE_YMD", Protocol.TriggerSet.OrderBy.OP_POSTWEIGHT);
			}
		} else if (ArrayValue[0].equalsIgnoreCase("popular")) {
			return new OrderBySet(false, "WEIGHT");
		} else if (ArrayValue[0].equalsIgnoreCase("relevance")) {
			return new OrderBySet(false, "WEIGHT");
		}
		return new OrderBySet(false, "WEIGHT");
	}
}
