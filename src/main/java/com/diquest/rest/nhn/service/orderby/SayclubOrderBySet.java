package com.diquest.rest.nhn.service.orderby;

import com.diquest.ir.common.msg.protocol.Protocol;
import com.diquest.ir.common.msg.protocol.query.OrderBySet;
import com.diquest.rest.nhn.common.Collections;
import com.diquest.rest.nhn.common.SayclubCollections;
import com.diquest.rest.nhn.service.group.GroupBySetService;

public class SayclubOrderBySet {

	private static SayclubOrderBySet instance = null;

	public static SayclubOrderBySet getInstance() {
		if (instance == null) {
			instance = new SayclubOrderBySet();
		}
		return instance;
	}

	public OrderBySet getOrderBySet(String value, String collection) {
		
		String[] ArrayValue = value.toUpperCase().split(" ");
		
		if(collection.equalsIgnoreCase(SayclubCollections.SAYCAST_OLD)) {
//			return new OrderBySet(false, "REGDATE", Protocol.OrderBySet.OP_PREWEIGHT);
			return new OrderBySet(true, "ONAIR", Protocol.OrderBySet.OP_POSTWEIGHT);
			
		} else if(collection.equalsIgnoreCase(SayclubCollections.SAYCAST_ART_OLD)) {
			if (ArrayValue[0].equalsIgnoreCase("REGDATE")) {
				if(ArrayValue[1].equalsIgnoreCase("ASC")) {
					return new OrderBySet(true, "REGDATE", Protocol.OrderBySet.OP_POSTWEIGHT);
				} else {
					return new OrderBySet(false, "REGDATE", Protocol.OrderBySet.OP_POSTWEIGHT);
				}
			} else if (ArrayValue[0].equalsIgnoreCase("LASTUPDATE")) {
				if(ArrayValue[1].equalsIgnoreCase("ASC")) {
					return new OrderBySet(true, "LASTUPDATE", Protocol.OrderBySet.OP_POSTWEIGHT);
				} else {
					return new OrderBySet(false, "LASTUPDATE", Protocol.OrderBySet.OP_POSTWEIGHT);
				}
			} else {
				return new OrderBySet(false, "REGDATE", Protocol.OrderBySet.OP_PREWEIGHT);
			}
			
		} else if(collection.equalsIgnoreCase(SayclubCollections.SAYMALL_OLD)) {
			if (ArrayValue[0].equalsIgnoreCase("SELL_AMOUNT")) {
				if(ArrayValue[1].equalsIgnoreCase("ASC")) {
					return new OrderBySet(true, "SELL_AMOUNT", Protocol.OrderBySet.OP_POSTWEIGHT);
				} else {
					return new OrderBySet(false, "SELL_AMOUNT", Protocol.OrderBySet.OP_POSTWEIGHT);
				}
			} else if (ArrayValue[0].equalsIgnoreCase("PRICE")) {
				if(ArrayValue[1].equalsIgnoreCase("ASC")) {
					return new OrderBySet(true, "PRICE", Protocol.OrderBySet.OP_POSTWEIGHT);
				} else {
					return new OrderBySet(false, "PRICE", Protocol.OrderBySet.OP_POSTWEIGHT);
				}
			} else if (ArrayValue[0].equalsIgnoreCase("FROM_DATE")) {
				if(ArrayValue[1].equalsIgnoreCase("ASC")) {
					return new OrderBySet(true, "FROM_DATE", Protocol.OrderBySet.OP_POSTWEIGHT);
				} else {
					return new OrderBySet(false, "FROM_DATE", Protocol.OrderBySet.OP_POSTWEIGHT);
				}
			} else if (ArrayValue[0].equalsIgnoreCase("ITEM_NAME_FOR_SORT")) {
				if(ArrayValue[1].equalsIgnoreCase("ASC")) {
					return new OrderBySet(true, "ITEM_NAME_FOR_SORT", Protocol.OrderBySet.OP_POSTWEIGHT);
				} else {
					return new OrderBySet(false, "ITEM_NAME_FOR_SORT", Protocol.OrderBySet.OP_POSTWEIGHT);
				}
			} else {
				return new OrderBySet(false, "ITEM_NAME_FOR_SORT", Protocol.OrderBySet.OP_PREWEIGHT);
			}
		} else if(collection.equalsIgnoreCase(SayclubCollections.ALLUSER_OLD)) {
			return new OrderBySet(false, "WEIGHT");
		} else if(collection.equalsIgnoreCase(SayclubCollections.CHATUSER_OLD)) {
			return new OrderBySet(false, "WEIGHT");
		} else if(collection.equalsIgnoreCase(SayclubCollections.SAYCAST)) {
//			return new OrderBySet(false, "REGDATE", Protocol.OrderBySet.OP_PREWEIGHT);
			return new OrderBySet(true, "ONAIR", Protocol.OrderBySet.OP_POSTWEIGHT);
			
		} else if(collection.equalsIgnoreCase(SayclubCollections.SAYCAST_ART)) {
			if (ArrayValue[0].equalsIgnoreCase("REGDATE")) {
				if(ArrayValue[1].equalsIgnoreCase("ASC")) {
					return new OrderBySet(true, "REGDATE", Protocol.OrderBySet.OP_POSTWEIGHT);
				} else {
					return new OrderBySet(false, "REGDATE", Protocol.OrderBySet.OP_POSTWEIGHT);
				}
			} else if (ArrayValue[0].equalsIgnoreCase("LASTUPDATE")) {
				if(ArrayValue[1].equalsIgnoreCase("ASC")) {
					return new OrderBySet(true, "LASTUPDATE", Protocol.OrderBySet.OP_POSTWEIGHT);
				} else {
					return new OrderBySet(false, "LASTUPDATE", Protocol.OrderBySet.OP_POSTWEIGHT);
				}
			} else {
				return new OrderBySet(false, "REGDATE", Protocol.OrderBySet.OP_PREWEIGHT);
			}
			
		} else if(collection.equalsIgnoreCase(SayclubCollections.SAYCAST_CJ)) {
			return new OrderBySet(true, "CJNAME", Protocol.OrderBySet.OP_PREWEIGHT);
			
		}
		
		return new OrderBySet(false, "WEIGHT");
	}
}
