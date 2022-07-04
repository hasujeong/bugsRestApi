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
		if(collection.endsWith("SELLER")){ // 스토어 관련 정렬은 다 가중치순임.. 나중에 상속받을 수 있게 리팩토링 해야 함..
			return new OrderBySet(false, "WEIGHT");
		}

		if (value.equalsIgnoreCase("alias:relevance")) {
			return new OrderBySet(false, "WEIGHT");
		} else if (value.equalsIgnoreCase("alias:recent")) {
			return new OrderBySet(true, "RECENT");
		} else if (value.equalsIgnoreCase("alias:review")) {
			return new OrderBySet(true, "REVIEW");
		} else if (value.equalsIgnoreCase("alias:price")) {
			return new OrderBySet(true, "SELLER_IS_PENALTY,_IS_RECENTLY_REG_20DAYS:desc,_SALE_PRICE:asc,CREATED_TIME", Protocol.TriggerSet.OrderBy.OP_POSTWEIGHT);
		} else if (value.equalsIgnoreCase("alias:order")) {
			return new OrderBySet(true, "ORDER");
		} else if (value.equalsIgnoreCase("alias:price_within24")) {
			if(collection.equalsIgnoreCase("BRANDI_PRODUCT")) {
				return new OrderBySet(true, "SELLER_IS_PENALTY,_IS_RECENTLY_REG_20DAYS:desc,_SALE_PRICE_WITHIN24:asc,CREATED_TIME", Protocol.TriggerSet.OrderBy.OP_POSTWEIGHT);
			} 
		}
		return new OrderBySet(false, "WEIGHT");
	}
}
