package com.diquest.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

import com.diquest.ir.client.command.CommandSearchRequest;
import com.diquest.ir.common.exception.IRException;
import com.diquest.ir.common.msg.protocol.Protocol;
import com.diquest.ir.common.msg.protocol.query.FilterSet;
import com.diquest.ir.common.msg.protocol.query.GroupBySet;
import com.diquest.ir.common.msg.protocol.query.OrderBySet;
import com.diquest.ir.common.msg.protocol.query.Query;
import com.diquest.ir.common.msg.protocol.query.QueryParser;
import com.diquest.ir.common.msg.protocol.query.QuerySet;
import com.diquest.ir.common.msg.protocol.query.SelectSet;
import com.diquest.ir.common.msg.protocol.query.WhereSet;
import com.diquest.ir.common.msg.protocol.result.Result;
import com.diquest.ir.rest.common.constant.HttpStatus;
import com.diquest.ir.rest.common.exception.InvalidParameterException;
import com.diquest.ir.rest.common.object.RestHttpRequest;
import com.diquest.ir.rest.json.gson.GsonLoader;
import com.diquest.ir.rest.json.object.JsonUnknownUriResult;
import com.diquest.ir.rest.json.reponse.ResponseMaker;
import com.diquest.ir.rest.server.log.ServerLogManager;
import com.diquest.ir.rest.util.RestUtils;
import com.diquest.rest.nhn.filter.parse.FilterValueParser;
import com.diquest.rest.nhn.filter.result.FilterFieldParseResult;
import com.diquest.rest.nhn.result.NhnError;
import com.diquest.rest.nhn.result.NhnResult;
import com.diquest.rest.nhn.service.error.ErrorMessageService;
import com.diquest.rest.nhn.service.error.logMessageService;
import com.diquest.rest.nhn.service.filter.FilterSetService;
import com.diquest.rest.nhn.service.option.trackQoption;
import com.diquest.rest.nhn.service.orderby.OrderBySetService;
import com.diquest.rest.nhn.service.select.SelectSetService;
import com.diquest.rest.nhn.service.trigger.TriggerFieldService;
import com.diquest.rest.nhn.service.where.WhereSetService;
import com.google.gson.Gson;

@Service
public class BugsRestService {
	
	protected HashMap<String, Integer> idxScoreMap = new HashMap<String, Integer>();
	
	protected static String currTimezone = new SimpleDateFormat("XXX").format(new Date()).replace(":", "");
	
	public BugsRestService() {
//		idxScoreMap.put("TRACK_TITLE", 100);
//		idxScoreMap.put("CATEGORY_2_NAME", 50);
//		idxScoreMap.put("CATEGORY_3_NAME", 50);
//		idxScoreMap.put("SELLER_TAGS_NAME", 20);
//		idxScoreMap.put("SHOPPING_IDX", 10);
	}
	
	public String search(Map<String, String> params, Map<String, Object> reqHeader, HttpServletRequest request) {
		
		String req = "";
		req += "Host: " + (String) reqHeader.get("host") + "\n";
		req += "Connection: " + (String) reqHeader.get("connection") + "\n";
		req += "Upgrade-Insecure-Requests: " + (String) reqHeader.get("upgrade-insecure-requests") + "\n";
		req += "User-Agent: " + (String) reqHeader.get("user-agent") + "\n";
		req += "Accept: " + (String) reqHeader.get("accept") + "\n";
		req += "Accept-Encoding: " + (String) reqHeader.get("accept-encoding") + "\n";
		req += "Accept-Language: " + (String) reqHeader.get("accept-language");
		
		if(params.get("q") != null) {
			if(params.get("q").isEmpty()){
				return makeEmptyNhnData(params);
			}
		} else {
			return makeEmptyNhnData(params);
		}
		
		logMessageService.requestReceived(reqHeader, request);
		
		String ret = "";
		
		Gson gson = new Gson();
		
		QuerySet querySet = new QuerySet(1);
		Query query = new Query();
		
		QueryParser parser = new QueryParser();
		
		try {
			
			FilterFieldParseResult filterFieldParseResult = parseFilterParams(params);
			query.setSelect(parseSelect(params));
			query.setFilter(parseFilter(params, filterFieldParseResult));
			query.setWhere(parseWhere(params, filterFieldParseResult));
//			query.setGroupBy(parseGroupBy(params));
			query.setOrderby(parseOrderBy(params));
			query.setFrom(getCollection(params));
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
			parseTrigger(params, query, getCollection(params));
//			query.setQueryModifier("diver");
			query.setResultModifier("typo");
			querySet.addQuery(query);
			
//			String queryStr = parser.qodueryToString(query);
						
			CommandSearchRequest commandSearchRequest = new CommandSearchRequest("alp-search.bugs.co.kr", 5555);
					
			int returnCode = commandSearchRequest.request(querySet);
	
			if (returnCode <= -100) {
				ErrorMessageService.getInstance().minusReturnCodeLog(returnCode, commandSearchRequest.getException(), req);
				logMessageService.receiveEnd(reqHeader, request);
				return commandSearchRequestErrorResponse(commandSearchRequest.getException().getErrorMessage());
			} else {
				logMessageService.messageReceived(reqHeader, request);
			}
			
//			System.out.println(returnCode);
//			System.out.println(commandSearchRequest.getResultSet().getResult(0).getTotalSize());
			
			String resultJson = gson.toJson(makeResult(commandSearchRequest.getResultSet().getResult(0), query, params));
			ret = resultJson;
			
			logMessageService.receiveEnd(reqHeader, request);
			
//			System.out.println(ret);
			
			
		} catch (InvalidParameterException e) {
			ErrorMessageService.getInstance().invalidParameterLog(req, e);
			logMessageService.receiveEnd(reqHeader, request);
			return invalidParameterResponse(e);
		} catch (Exception e) {
			ErrorMessageService.getInstance().InternalServerErrorLog(req, e);
			logMessageService.receiveEnd(reqHeader, request);
			return internalServerResponse(e);
		}
		
		return ret;
		
	}
	
	private String makeEmptyNhnData(Map<String, String> params) {
//		JsonUnknownUriResult result = new JsonUnknownUriResult(HttpStatus.OK, NhnResult.makeEmptyResult());
		String emptyData = GsonLoader.getInstance().toJson(NhnResult.makeEmptyResult());
		return emptyData;
	}
	
	protected String getCollection(Map<String, String> params) {
//		System.out.println("----------------" + params.get("collection"));
		return params.get("collection");
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
		String collection = getCollection(params);
		
		idxScoreMap = new HashMap<String, Integer>();

		if(collection.equalsIgnoreCase("TRACK")) {
			idxScoreMap.put("TRACK_TITLE", 100);
		} else if(collection.equalsIgnoreCase("ALBUM")) {
			idxScoreMap.put("ALBUM_IDX", 100);
			idxScoreMap.put("ARTIST_IDX", 50);
		} else if(collection.equalsIgnoreCase("ARTIST")) {
			idxScoreMap.put("ARTIST_NM", 100);
			idxScoreMap.put("KOR_NM", 50);
			idxScoreMap.put("DISP_NM", 50);
			idxScoreMap.put("ENG_NM", 50);
			idxScoreMap.put("SEARCH_NM", 20);
			idxScoreMap.put("SYNONYM_NM", 20);
		} else if(collection.equalsIgnoreCase("MV")) {
			idxScoreMap.put("MV_TITLE", 100);
			idxScoreMap.put("ARTIST_NM", 50);
			idxScoreMap.put("TRACK_TITLE", 50);
			idxScoreMap.put("DISP_NM", 50);
			idxScoreMap.put("TITLE", 50);
			idxScoreMap.put("SEARCH_TITLE", 20);
			idxScoreMap.put("SEARCH_NM", 20);
		} else if(collection.equalsIgnoreCase("MUSICCAST")) {
			idxScoreMap.put("TITLE", 100);
		} else if(collection.equalsIgnoreCase("MUSICPD")) {
			idxScoreMap.put("TITLE", 100);
			idxScoreMap.put("NICKNAME", 30);
			idxScoreMap.put("ALBUM_TITLE", 50);
			idxScoreMap.put("ARTIST_NM", 50);
			idxScoreMap.put("TRACK_TITLE", 50);
			idxScoreMap.put("TAG", 30);
		} else if(collection.equalsIgnoreCase("MUSICPOST")) {
			idxScoreMap.put("TITLE", 100);
			idxScoreMap.put("ARTIST_NM", 50);
		} else if(collection.equalsIgnoreCase("CLASSIC")) {
			idxScoreMap.put("TITLE", 100);
			idxScoreMap.put("TITLE_KOR", 80);
			idxScoreMap.put("OPUS", 50);
			idxScoreMap.put("SEARCH_TITLE", 20);
			idxScoreMap.put("SYNONYM_TITLE", 20);
			idxScoreMap.put("TITLE_KOR_SEC", 40);
			idxScoreMap.put("TITLE_ENG", 40);
			idxScoreMap.put("SEARCH_TITLE_SEC", 20);
			idxScoreMap.put("ARTIST_NM", 30);
			idxScoreMap.put("KOR_NM", 30);
			idxScoreMap.put("DISP_NM", 30);
		} else if(collection.equalsIgnoreCase("ENTITY")) {
		}
		 
//		System.out.println("----------------- " + idxScoreMap);
		return WhereSetService.getInstance().makeWhereSet(params, filterFieldParseResult, makeBaseWhereSet(params));
	}
	
	protected List<WhereSet> makeBaseWhereSet(Map<String, String> params) throws InvalidParameterException {
		List<WhereSet> result = new ArrayList<WhereSet>();
		String keyword = parseQ(params);
		trackQoption qOption = new trackQoption(RestUtils.getParam(params, "q_option"));
			
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
		return new OrderBySet[] { OrderBySetService.getInstance().getOrderBySet(RestUtils.getParam(params, "sort"), getCollection(params)) };
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
	
	protected String commandSearchRequestErrorResponse(String message) {
		NhnError searchError = NhnError.makeError(false, -500, "Internal Server Error", message, currTimezone);
		String ErrorStr = GsonLoader.getInstance().toJson(searchError);
		
		return ErrorStr;
	}
	
	protected String invalidParameterResponse(InvalidParameterException e) {
		NhnError invalidError = NhnError.makeError(false, -400, "Invalid Parameter", e.getMessage(), currTimezone);
		String ErrorStr = GsonLoader.getInstance().toJson(invalidError);
		
		return ErrorStr;
	}
	
	protected String internalServerResponse(Exception e) {
		NhnError internalError = NhnError.makeError(false, -500, "Internal Server Error", e.getMessage(), currTimezone);
		String ErrorStr = GsonLoader.getInstance().toJson(internalError);
		
		return ErrorStr;
	}

}
