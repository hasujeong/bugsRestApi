package com.diquest.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
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
import com.diquest.mapper.AdminMapper;
import com.diquest.mapper.domain.FieldSelector;
import com.diquest.rest.nhn.filter.parse.FilterValueParser;
import com.diquest.rest.nhn.filter.result.FilterFieldParseResult;
import com.diquest.rest.nhn.result.NhnError;
import com.diquest.rest.nhn.result.NhnResult;
import com.diquest.rest.nhn.service.error.ErrorMessageService;
import com.diquest.rest.nhn.service.error.logMessageService;
import com.diquest.rest.nhn.service.filter.FilterSetService;
import com.diquest.rest.nhn.service.option.searchQoption;
import com.diquest.rest.nhn.service.orderby.OrderBySetService;
import com.diquest.rest.nhn.service.select.SelectSetService;
import com.diquest.rest.nhn.service.trigger.TriggerFieldService;
import com.diquest.rest.nhn.service.where.WhereSetService;
import com.google.gson.Gson;

@Service
public class BugsRestService {
	
	@Resource
	private BugsRestService self;
	
	@Autowired
	AdminMapper adminMapper;
	
	protected HashMap<String, Integer> idxScoreMap = new HashMap<String, Integer>();
	
	protected static String currTimezone = new SimpleDateFormat("XXX").format(new Date()).replace(":", "");

	protected static int track_score = 100;
	protected static int album_score = 10;
	protected static int artist_score = 10;
	
	public BugsRestService() {
//		idxScoreMap.put("TRACK_TITLE", 100);
//		idxScoreMap.put("CATEGORY_2_NAME", 50);
//		idxScoreMap.put("CATEGORY_3_NAME", 50);
//		idxScoreMap.put("SELLER_TAGS_NAME", 20);
//		idxScoreMap.put("SHOPPING_IDX", 10);
	}
	
	@Cacheable("fieldSelectorMap")
	public Map<String, String> fieldSelector() {
		
		Map<String, String> fieldSelectorMap = new HashMap<String, String>();
		
		List<FieldSelector> fsList = adminMapper.fieldSelector();
		
//		System.out.println("::::::::::::::::::::::::");
		
		for (FieldSelector fs : fsList) {
			fieldSelectorMap.put(fs.getQuery(), fs.getSelected());
		}
		
		return fieldSelectorMap;
		
	}
	
	public String search(Map<String, String> params, Map<String, Object> reqHeader, HttpServletRequest request) {
		
		Map<String, String> fieldSelectorMap = self.fieldSelector();
//		System.out.println(":::::::::::::::::::" + fieldSelectorMap);
		
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
			String paramQ = parseQ(params);
			String qValue = paramQ.replaceAll("\\s", "");
			String col = getCollection(params);
			
			if(col.equalsIgnoreCase("TRACK")) {
				if(fieldSelectorMap.containsKey(qValue) == true) {
//					System.out.println(":::::qValue:::::::" + qValue);
					
					String selectedValue = fieldSelectorMap.get(qValue);
//					System.out.println("==================" + selectedValue);
					
					if(selectedValue.equalsIgnoreCase("track")) {
						track_score = 1000;
						album_score = 10;
						artist_score = 10;
					} else if(selectedValue.equalsIgnoreCase("album")) {
						track_score = 10;
						album_score = 1000;
						artist_score = 10;
					} else {
						track_score = 10;
						album_score = 10;
						artist_score = 1000;
					}
				} else {
					track_score = 100;
					album_score = 10;
					artist_score = 10;
				}
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
			query.setUserName(getUserName(params));										// 로그인 사용자 기록
			query.setExtData(RestUtils.getParam(params, "pr"));							// pr (app,web,pc)
			query.setLoggable(getLoggable(RestUtils.getParam(params, "search_tp")));
			query.setPrintQuery(true);		
			parseTrigger(params, query, getCollection(params));
//			query.setQueryModifier("diver");
			query.setResultModifier("typo");
			querySet.addQuery(query);
			
			String queryStr = parser.queryToString(query);
			System.out.println(" :::::::::: query ::::::: " + queryStr);
						
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
		
		return WhereSetService.getInstance().makeWhereSet(params, filterFieldParseResult, makeBaseWhereSet(params));
	}
	
	protected List<WhereSet> makeBaseWhereSet(Map<String, String> params) throws InvalidParameterException {
		List<WhereSet> result = new ArrayList<WhereSet>();
		String keyword = parseQ(params);
		String collection = getCollection(params);
		searchQoption qOption = new searchQoption(RestUtils.getParam(params, "q_option"), collection);
		
		String idxField = qOption.getIndexField();
		
		idxScoreMap = new HashMap<String, Integer>();

		if(collection.equalsIgnoreCase("TRACK")) {
			if(idxField.equalsIgnoreCase("track_idx")) {
				idxScoreMap.put("TRACK_IDX", 100);
				idxScoreMap.put("SYN_TRACK_IDX", 1);
			} else if(idxField.equalsIgnoreCase("artist_idx")) {
				idxScoreMap.put("ARTIST_IDX", 100);
				idxScoreMap.put("SYN_ARTIST_IDX", 1);
			} else if(idxField.equalsIgnoreCase("album_idx")) {
				idxScoreMap.put("ALBUM_IDX", 100);
				idxScoreMap.put("SYN_ALBUM_IDX", 1);
			} else {
				idxScoreMap.put("TRACK_IDX", track_score);
				idxScoreMap.put("ARTIST_IDX", artist_score);
				idxScoreMap.put("ALBUM_IDX", album_score);
				idxScoreMap.put("TRACK_ARTIST_ALBUM_IDX", 100);
				idxScoreMap.put("SYN_TRACK_ARTIST_ALBUM_IDX", 1);
			}
		} else if(collection.equalsIgnoreCase("ALBUM")) {
			if(idxField.equalsIgnoreCase("album_idx")) {
				idxScoreMap.put("ALBUM_IDX", 100);
				idxScoreMap.put("SYN_ALBUM_IDX", 1);
			} else if(idxField.equalsIgnoreCase("artist_idx")) {
				idxScoreMap.put("ARTIST_IDX", 100);
				idxScoreMap.put("SYN_ARTIST_IDX", 1);
			} else {
				idxScoreMap.put("ARTIST_ALBUM_IDX", 100);
				idxScoreMap.put("SYN_ARTIST_ALBUM_IDX", 1);
			}
		} else if(collection.equalsIgnoreCase("ARTIST")) {
			if(idxField.equalsIgnoreCase("artist_idx")) {
				idxScoreMap.put("ARTIST_IDX", 100);
				idxScoreMap.put("SYN_ARTIST_IDX", 1);
				idxScoreMap.put("GRP_NM_IDX", 50);
				idxScoreMap.put("SYN_GRP_NM_IDX", 50);
				idxScoreMap.put("GRP_SEARCH_NM_IDX", 1);
				idxScoreMap.put("SYN_GRP_SEARCH_NM", 1);
			} else if(idxField.equalsIgnoreCase("exact_artist_idx")) {
				idxScoreMap.put("ARTIST_IDX", 100);
				idxScoreMap.put("SYN_ARTIST_IDX", 1);
			} else {
				idxScoreMap.put("ARTIST_IDX", 100);
				idxScoreMap.put("SYN_ARTIST_IDX", 1);
				idxScoreMap.put("GRP_NM_IDX", 50);
				idxScoreMap.put("SYN_GRP_NM_IDX", 50);
				idxScoreMap.put("GRP_SEARCH_NM_IDX", 1);
				idxScoreMap.put("SYN_GRP_SEARCH_NM", 1);
			}
		} else if(collection.equalsIgnoreCase("MV")) {
			idxScoreMap.put("MV_TRACK_ARTIST_ALBUM_IDX", 100);
			idxScoreMap.put("SYN_MV_TRACK_ARTIST_ALBUM_IDX", 1);
		} else if(collection.equalsIgnoreCase("MUSICCAST")) {
			idxScoreMap.put("MUSICCAST_IDX", 100);
		} else if(collection.equalsIgnoreCase("MUSICPD")) {
			if(idxField.equalsIgnoreCase("musicpd_album_idx")) {
				idxScoreMap.put("MUSICPD_ALBUM_IDX", 100);
			} else {
				idxScoreMap.put("MUSICPD_ALBUM_IDX", 100);
			}
		} else if(collection.equalsIgnoreCase("MUSICPOST")) {
			idxScoreMap.put("MUSICPOST_IDX", 100);
		} else if(collection.equalsIgnoreCase("CLASSIC")) {
			idxScoreMap.put("CLASSIC_IDX", 100);
			idxScoreMap.put("SYN_CLASSIC_IDX", 100);
		} else if(collection.equalsIgnoreCase("ENTITY")) {
		}
		 
//		System.out.println("----------------- " + idxScoreMap);
		
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
