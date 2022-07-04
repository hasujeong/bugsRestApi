package com.diquest.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.stereotype.Service;

import com.diquest.ir.client.command.CommandSearchRequest;
import com.diquest.ir.common.exception.IRException;
import com.diquest.ir.common.msg.protocol.Protocol;
import com.diquest.ir.common.msg.protocol.query.FilterSet;
import com.diquest.ir.common.msg.protocol.query.GroupBySet;
import com.diquest.ir.common.msg.protocol.query.OrderBySet;
import com.diquest.ir.common.msg.protocol.query.Query;
import com.diquest.ir.common.msg.protocol.query.QuerySet;
import com.diquest.ir.common.msg.protocol.query.SelectSet;
import com.diquest.ir.common.msg.protocol.query.WhereSet;
import com.diquest.ir.common.msg.protocol.result.Result;
import com.diquest.ir.rest.common.exception.InvalidParameterException;
import com.diquest.ir.rest.common.object.RestHttpRequest;
import com.diquest.ir.rest.util.RestUtils;
import com.diquest.rest.nhn.filter.parse.FilterValueParser;
import com.diquest.rest.nhn.filter.result.FilterFieldParseResult;
import com.diquest.rest.nhn.result.NhnResult;
import com.diquest.rest.nhn.service.error.ErrorMessageService;
import com.diquest.rest.nhn.service.filter.FilterSetService;
import com.diquest.rest.nhn.service.option.ProductQoption;
import com.diquest.rest.nhn.service.orderby.OrderBySetService;
import com.diquest.rest.nhn.service.select.SelectSetService;
import com.diquest.rest.nhn.service.trigger.TriggerFieldService;
import com.diquest.rest.nhn.service.where.WhereSetService;
import com.google.gson.Gson;

@Service
public class BugsRestService {
	
	protected HashMap<String, Integer> idxScoreMap = new HashMap<String, Integer>();
	
	public BugsRestService() {
		idxScoreMap.put("NAME", 100);
		idxScoreMap.put("CATEGORY_2_NAME", 50);
		idxScoreMap.put("CATEGORY_3_NAME", 50);
		idxScoreMap.put("SELLER_TAGS_NAME", 20);
		idxScoreMap.put("SHOPPING_IDX", 10);
	}
	
	public String search(Map<String, String> params) {
		
		String ret = "";
		
		Gson gson = new Gson();
		
		QuerySet querySet = new QuerySet(1);
		Query query = new Query();
		
		try {
			
			FilterFieldParseResult filterFieldParseResult = parseFilterParams(params);
			
			query.setSelect(parseSelect(params));
			query.setFilter(parseFilter(params, filterFieldParseResult));
			query.setWhere(parseWhere(params, filterFieldParseResult));
			query.setGroupBy(parseGroupBy(params));
			query.setOrderby(parseOrderBy(params));
			query.setFrom(getCollection());
			query.setResult(parseStart(params) - 1, parseStart(params) + parseSize(params) - 2);
			query.setSearchKeyword(parseQ(params));
			query.setFaultless(true);
			query.setThesaurusOption((byte) (Protocol.ThesaurusOption.EQUIV_SYNONYM | Protocol.ThesaurusOption.QUASI_SYNONYM));
			query.setSearchOption((byte) (Protocol.SearchOption.BANNED | Protocol.SearchOption.STOPWORD | Protocol.SearchOption.CACHE));
			query.setRankingOption((byte) (Protocol.RankingOption.CATEGORY_RANKING | Protocol.RankingOption.DOCUMENT_RANKING));
			query.setCategoryRankingOption((byte) (Protocol.CategoryRankingOption.EQUIV_SYNONYM | Protocol.CategoryRankingOption.QUASI_SYNONYM));
			query.setUserName(getUserName(params));
			query.setExtData(RestUtils.getParam(params, "pr"));
			query.setLoggable(getLoggable(RestUtils.getParam(params, "search_tp")));
			query.setPrintQuery(true);
			
			parseTrigger(params, query, getCollection());
			query.setQueryModifier("diver");
			query.setResultModifier("typo");
			querySet.addQuery(query);
			
			CommandSearchRequest commandSearchRequest = new CommandSearchRequest("133.186.171.19", 15555);
			int returnCode = commandSearchRequest.request(querySet);
			if (returnCode <= -100) {
//				ErrorMessageService.getInstance().minusReturnCodeLog(returnCode, commandSearchRequest.getException(), params.toString());
//				return commandSearchRequestErrorResponse(req, commandSearchRequest.getException().getErrorMessage());
			}
			
			System.out.println(returnCode);
			System.out.println(commandSearchRequest.getResultSet().getResult(0).getTotalSize());
			
			String resultJson = gson.toJson(makeResult(commandSearchRequest.getResultSet().getResult(0), query, params));
			ret = resultJson;
			
			System.out.println(ret);
			
		} catch (InvalidParameterException e) {
//			ErrorMessageService.getInstance().invalidParameterLog(req, e);
//			return invalidParameterResponse(req, e);
		} catch (Exception e) {
//			ErrorMessageService.getInstance().InternalServerErrorLog(req, e);
//			return internalServerResponse(req, e);
		}
		
		return ret;
		
	}
	
	protected String getCollection() {
		return "BRANDI_PRODUCT";
	}
	
	private FilterFieldParseResult parseFilterParams(Map<String, String> params) {
		return new FilterValueParser(params).parseAll();
	}
	
	private SelectSet[] parseSelect(Map<String, String> params) {
		return SelectSetService.getInstance().makeSelectSet(params);
	}
	
	protected FilterSet[] parseFilter(Map<String, String> params, FilterFieldParseResult filterFieldParseResult) throws InvalidParameterException {
		return FilterSetService.getInstance().parseFilter(params, filterFieldParseResult);
	}
	
	protected WhereSet[] parseWhere(Map<String, String> params, FilterFieldParseResult filterFieldParseResult) throws InvalidParameterException {
		return WhereSetService.getInstance().makeWhereSet(params, filterFieldParseResult, makeBaseWhereSet(params));
	}
	
	protected List<WhereSet> makeBaseWhereSet(Map<String, String> params) throws InvalidParameterException {
		List<WhereSet> result = new ArrayList<WhereSet>();
		String keyword = parseQ(params);
		ProductQoption qOption = new ProductQoption(RestUtils.getParam(params, "q_option"));
		for (Entry<String, Integer> e : idxScoreMap.entrySet()) {
			if (result.size() > 0) {
				result.add(new WhereSet(Protocol.WhereSet.OP_OR));
			}
			if (qOption.isNofM()) {
				result.add(new WhereSet(e.getKey(), qOption.getOption(), keyword, e.getValue(), qOption.getNofmPercent()));
			} else {
				result.add(new WhereSet(e.getKey(), qOption.getOption(), keyword, e.getValue()));
			}
		}
		return result;
	}
	
	public GroupBySet[] parseGroupBy(Map<String, String> params) {
		String catGroup = RestUtils.getParam(params, "group");
		if (catGroup.equals("")) {
			return new GroupBySet[0];
		}
		List<GroupBySet> result = new ArrayList<GroupBySet>();
		for (String g : catGroup.split(",")) {
			result.add(new GroupBySet(g.toUpperCase(), (byte) (Protocol.GroupBySet.OP_COUNT | Protocol.GroupBySet.ORDER_COUNT), "DESC"));
		}
		return result.toArray(new GroupBySet[result.size()]);
	}
	
	protected OrderBySet[] parseOrderBy(Map<String, String> params) {
		return new OrderBySet[] { OrderBySetService.getInstance().getOrderBySet(RestUtils.getParam(params, "sort"), getCollection()) };
	}
	
	protected int parseStart(Map<String, String> params) {
		String start = RestUtils.getParam(params, "start");
		if (start.equals("")) {
			return 1;
		}
		return Integer.parseInt(start);
	}
	
	protected int parseSize(Map<String, String> params) {
		String size = RestUtils.getParam(params, "size");
		if (size.equals("")) {
			return 10;
		}
		return Integer.parseInt(size);
	}
	
	protected String getUserName(Map<String, String> params) {
		String userName = "";
		String sid = params.get("requestHeader.sid");
		if (sid == null) {
			userName = "";
		} else {
			userName = sid;
		}
		return userName;
	}
	
	protected boolean getLoggable(String value) {
		if (value.equalsIgnoreCase("sb")) {
			return true;
		} else if (value.equalsIgnoreCase("ign")) {
			return false;
		}
		return true;
	}
	
	private void parseTrigger(Map<String, String> req, Query query, String collection) throws InvalidParameterException {
		TriggerFieldService.getInstance().parseTrigger(req, query, collection);
	}
	
	protected NhnResult makeResult(Result result, Query query, Map<String, String> params) throws IRException {
		return NhnResult.makeProductResult(query, result, params);
	}
	
	protected String parseQ(Map<String, String> params) {
		return RestUtils.getParam(params, "q");
	}

}
