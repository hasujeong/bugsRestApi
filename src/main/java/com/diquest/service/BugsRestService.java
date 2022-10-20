package com.diquest.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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
import com.diquest.ir.common.msg.protocol.result.ResultSet;
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
import com.diquest.mapper.domain.artistSelector;
import com.diquest.rest.nhn.common.Collections;
import com.diquest.rest.nhn.common.Connection;
import com.diquest.rest.nhn.filter.parse.FilterValueParser;
import com.diquest.rest.nhn.filter.result.FilterFieldParseResult;
import com.diquest.rest.nhn.result.AutoResult;
import com.diquest.rest.nhn.result.AutoTotalResult;
import com.diquest.rest.nhn.result.HotKwdResult;
import com.diquest.rest.nhn.result.NhnError;
import com.diquest.rest.nhn.result.NhnResult;
import com.diquest.rest.nhn.result.PurchaseResult;
import com.diquest.rest.nhn.result.SimilarResult;
import com.diquest.rest.nhn.result.TotalResult;
import com.diquest.rest.nhn.result.EntityResult;
import com.diquest.rest.nhn.service.error.ErrorMessageService;
import com.diquest.rest.nhn.service.error.logMessageService;
import com.diquest.rest.nhn.service.filter.EntityFilterSetService;
import com.diquest.rest.nhn.service.filter.FilterSetService;
import com.diquest.rest.nhn.service.filter.TotalFilterSetService;
import com.diquest.rest.nhn.service.option.searchQoption;
import com.diquest.rest.nhn.service.orderby.OrderBySetService;
import com.diquest.rest.nhn.service.select.AutoTagSelectSet;
import com.diquest.rest.nhn.service.select.AutoTotalSelectSet;
import com.diquest.rest.nhn.service.select.EntitySelectSet;
import com.diquest.rest.nhn.service.select.HotKwdSelectSet;
import com.diquest.rest.nhn.service.select.PurchaseSelectSet;
import com.diquest.rest.nhn.service.select.SelectSetService;
import com.diquest.rest.nhn.service.trigger.TriggerFieldService;
import com.diquest.rest.nhn.service.where.EntityWhereSet;
import com.diquest.rest.nhn.service.where.PurchaseWhereSet;
import com.diquest.rest.nhn.service.where.WhereSetService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

@Service
public class BugsRestService {
	
	@Resource
	private BugsRestService self;
	
	@Autowired
	AdminMapper adminMapper;
	
	protected HashMap<String, Integer> idxScoreMap = new HashMap<String, Integer>();
	
	protected static String currTimezone = new SimpleDateFormat("XXX").format(new Date()).replace(":", "");

	protected static int track_score = 100;
	protected static int album_score = 100;
	protected static int artist_score = 100;
	
	protected static String artist_keyword = "";
//	protected static String a_keyword = "";
	
	public BugsRestService() {
//		idxScoreMap.put("TRACK_TITLE", 100);
//		idxScoreMap.put("CATEGORY_2_NAME", 50);
//		idxScoreMap.put("CATEGORY_3_NAME", 50);
//		idxScoreMap.put("SELLER_TAGS_NAME", 20);
//		idxScoreMap.put("SHOPPING_IDX", 10);
	}
	
	@Cacheable("fieldSelectorMap")
	public List<Map<String, String>> fieldSelector() {
		
//		Map<String, String> fieldSelectorMap = new HashMap<String, String>();
//		
//		List<FieldSelector> fsList = adminMapper.fieldSelector();
//		
//		for (FieldSelector fs : fsList) {
//			fieldSelectorMap.put(fs.getQuery(), fs.getSelected());
//		}
//		
//		return fieldSelectorMap;
		
		List<Map<String, String>> fieldList = new ArrayList<Map<String, String>>();
		
		Map<String, String> fieldSelectorMap = new HashMap<String, String>();
		
		List<FieldSelector> fsList = adminMapper.fieldSelector();
		
		for (FieldSelector fs : fsList) {
			fieldSelectorMap.put(fs.getQuery(), fs.getSelected());
		}
		
		fieldList.add(fieldSelectorMap);
		
		artistSelector artistSelector	= new artistSelector();
		
		Map<String, String> artistFieldMap = new HashMap<String, String>();
		
		for (int i=0 ; i < artistSelector.getArtistNmList().length ; i++) {			
			artistFieldMap.put(artistSelector.getArtistNmList()[i], artistSelector.getArtistNmList()[i]);
			
		}
//		System.out.println("--------- " + artistFieldMap);
		fieldList.add(artistFieldMap);
		
		return fieldList;
	}
	
	public String search(Map<String, String> params, Map<String, Object> reqHeader, HttpServletRequest request) {
		
//		Map<String, String> fieldSelectorMap = self.fieldSelector();
//		System.out.println(":::::::::::::::::::" + fieldSelectorMap);
		
		List<Map<String, String>> fieldMap =  self.fieldSelector();
		
		Map<String, String> fieldSelectorMap = fieldMap.get(0);
		Map<String, String> artistSelectorMap = fieldMap.get(1);
		
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
			
			if(col.equalsIgnoreCase(Collections.TRACK)) {
				if(fieldSelectorMap.containsKey(qValue) == true) {
//					System.out.println(":::::qValue:::::::" + qValue);
					
					String selectedValue = fieldSelectorMap.get(qValue);
//					System.out.println("==================" + selectedValue);
					
					if(selectedValue.equalsIgnoreCase("track")) {
						track_score = 1000;
						album_score = 10;
						artist_score = 30;
					} else if(selectedValue.equalsIgnoreCase("album")) {
						track_score = 15;
						album_score = 1000;
						artist_score = 30;
					} else {
						track_score = 15;
						album_score = 10;
						artist_score = 1000;
					}
				} else {
						String[] qSlice = params.get("q").split("\\s+");

						if(qSlice.length > 0) {
							for(int s = 0; s < qSlice.length ; s++) {
								for(String key : artistSelectorMap.keySet()){
									if(key.equalsIgnoreCase(qSlice[s])) {
//										System.out.println("------------ " + artistSelectorMap.get(qSlice[s]));
										
										artist_keyword = qSlice[s];
//										a_keyword = params.get("q").replace(artist_keyword, "");
									}
								}
							}
						} 

						track_score = 100;
						album_score = 100;
						artist_score = 100;
				}
			}
		} else {			
			return makeEmptyNhnData(params);
		}
		
		logMessageService.requestReceived(reqHeader, request);
		
		String ret = "";
		
		Gson gson = new Gson();
		
		QueryParser parser = new QueryParser();
		
		QuerySet querySet = new QuerySet(1);
		Query query = new Query();
		
		try {
			
			FilterFieldParseResult filterFieldParseResult = parseFilterParams(params);
			query.setSelect(parseSelect(params));
			query.setFilter(parseFilter(params, filterFieldParseResult, getCollection(params)));
			query.setWhere(parseWhere(params, filterFieldParseResult, getCollection(params)));
//			query.setGroupBy(parseGroupBy(params));
			query.setOrderby(parseOrderBy(params, getCollection(params)));
			query.setFrom(getCollection(params));
			query.setResult(parseStart(params) - 1, parseStart(params) + parseSize(params) - 2);
			query.setSearchKeyword(parseQ(params));
			query.setFaultless(true);
			query.setThesaurusOption((byte) (Protocol.ThesaurusOption.EQUIV_SYNONYM | Protocol.ThesaurusOption.QUASI_SYNONYM));
			query.setSearchOption((byte) (Protocol.SearchOption.BANNED | Protocol.SearchOption.STOPWORD | Protocol.SearchOption.CACHE));
			query.setRankingOption((byte) (Protocol.RankingOption.CATEGORY_RANKING | Protocol.RankingOption.DOCUMENT_RANKING));
			query.setCategoryRankingOption((byte) (Protocol.CategoryRankingOption.EQUIV_SYNONYM | Protocol.CategoryRankingOption.QUASI_SYNONYM));	
			query.setUserName(getUserName(params));										// 로그인 사용자 ID 기록
			query.setExtData(RestUtils.getParam(params, "pr"));							// pr (app,web,pc)
			query.setLoggable(getLoggable(RestUtils.getParam(params, "search_tp")));
			query.setLogKeyword(parseQ(params).toCharArray());
			query.setPrintQuery(true);						// 실제 사용시 false
			parseTrigger(params, query, getCollection(params));
//			query.setQueryModifier("diver");
			query.setResultModifier("typo");
//				query.setDebug(true);
//				query.setFaultless(true);	
			
			querySet.addQuery(query);
		
			String queryStr = parser.queryToString(query);
//			System.out.println(" :::::::::: query ::::::: " + queryStr);
									
			CommandSearchRequest commandSearchRequest = new CommandSearchRequest(Connection.IP, Connection.PORT);
					
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

	
	// 통합검색 API
	public String Totalsearch(Map<String, String> params, Map<String, Object> reqHeader, HttpServletRequest request) {
		
//		Map<String, String> fieldSelectorMap = self.fieldSelector();
//		System.out.println(":::::::::::::::::::" + fieldSelectorMap);
		
		List<Map<String, String>> fieldMap =  self.fieldSelector();
		
		Map<String, String> fieldSelectorMap = fieldMap.get(0);
		Map<String, String> artistSelectorMap = fieldMap.get(1);
		
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
			String col = params.get("collection");
			
			if(col.equalsIgnoreCase(Collections.TRACK)) {
				if(fieldSelectorMap.containsKey(qValue) == true) {
					
					String selectedValue = fieldSelectorMap.get(qValue);
					
					if(selectedValue.equalsIgnoreCase("track")) {
						track_score = 1000;
						album_score = 10;
						artist_score = 30;
					} else if(selectedValue.equalsIgnoreCase("album")) {
						track_score = 15;
						album_score = 1000;
						artist_score = 30;
					} else {
						track_score = 15;
						album_score = 10;
						artist_score = 1000;
					}
				} else {
					String[] qSlice = params.get("q").split("\\s+");

					if(qSlice.length > 0) {
						for(int s = 0; s < qSlice.length ; s++) {
							for(String key : artistSelectorMap.keySet()){
								if(key.equalsIgnoreCase(qSlice[s])) {
//									System.out.println("------------ " + artistSelectorMap.get(qSlice[s]));
									
									artist_keyword = qSlice[s];
//									a_keyword = params.get("q").replace(artist_keyword, "");
								}
							}
						}
					} 

					track_score = 100;
					album_score = 100;
					artist_score = 100;
				}
			}
		} else {			
			return makeEmptyNhnData(params);
		}
		
		logMessageService.requestReceived(reqHeader, request);
		
		String ret = "";
		
		Gson gson = new Gson();
		
		QueryParser parser = new QueryParser();
		
		String colStr = params.get("collection");
		String[] colArray = getTotalCollection(params);
		int queryInt = 1;
			
		if(colStr.equalsIgnoreCase(Collections.TOTAL)) {
			queryInt = colArray.length; 
		} else {
			queryInt = 1;
		}		
		
		QuerySet querySet = new QuerySet(queryInt);
		Query query = new Query();
		
		try {
			
			for(int i = 0 ; i < colArray.length ; i++) {
				query = new Query();
				FilterFieldParseResult filterFieldParseResult = parseFilterParams(params);
				query.setSelect(parseSelect(params));
				query.setFilter(parseTotalFilter(params, filterFieldParseResult, colArray[i]));
				query.setWhere(parseWhere(params, filterFieldParseResult, colArray[i]));
	//			query.setGroupBy(parseGroupBy(params));
				query.setOrderby(parseTotalOrderBy(params, colArray[i]));
				query.setFrom(colArray[i]);
				query.setResult(parseStart(params) - 1, parseStart(params) + parseTotalSize(params,colArray[i]) - 2);
				query.setSearchKeyword(parseQ(params));
				query.setFaultless(true);
				query.setThesaurusOption((byte) (Protocol.ThesaurusOption.EQUIV_SYNONYM | Protocol.ThesaurusOption.QUASI_SYNONYM));
				query.setSearchOption((byte) (Protocol.SearchOption.BANNED | Protocol.SearchOption.STOPWORD | Protocol.SearchOption.CACHE));
				query.setRankingOption((byte) (Protocol.RankingOption.CATEGORY_RANKING | Protocol.RankingOption.DOCUMENT_RANKING));
				query.setCategoryRankingOption((byte) (Protocol.CategoryRankingOption.EQUIV_SYNONYM | Protocol.CategoryRankingOption.QUASI_SYNONYM));	
				query.setUserName(getUserName(params));										// 로그인 사용자 ID 기록
				query.setExtData(RestUtils.getParam(params, "pr"));							// pr (app,web,pc)
				query.setLoggable(getLoggable(RestUtils.getParam(params, "search_tp")));
				query.setLogKeyword(parseQ(params).toCharArray());
				query.setPrintQuery(true);						// 실제 사용시 false
				parseTrigger(params, query, colArray[i]);
				query.setResultModifier("typo");
				
				querySet.addQuery(query);
			
				String queryStr = parser.queryToString(query);
//				System.out.println(" :::::::::: query ::::::: [" + i + "] " + queryStr);
			}
									
			CommandSearchRequest commandSearchRequest = new CommandSearchRequest(Connection.IP, Connection.PORT);
					
			int returnCode = commandSearchRequest.request(querySet);
			
			if (returnCode <= -100) {
				ErrorMessageService.getInstance().minusReturnCodeLog(returnCode, commandSearchRequest.getException(), req);
				logMessageService.receiveEnd(reqHeader, request);
				return commandSearchRequestErrorResponse(commandSearchRequest.getException().getErrorMessage());
			} else {
				logMessageService.messageReceived(reqHeader, request);
			}
						
			String resultJson = "";
			
			if(colStr.equalsIgnoreCase(Collections.TOTAL)) {
				resultJson += gson.toJson(makeTotalResult(commandSearchRequest.getResultSet(), querySet, params));
				
			} else {
				resultJson = gson.toJson(makeResult(commandSearchRequest.getResultSet().getResult(0), query, params));
			}
			
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
	
	// 자동완성 API 
	public String Autosearch(Map<String, String> params, Map<String, Object> reqHeader, HttpServletRequest request) {

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
		
		QueryParser parser = new QueryParser();
		
		String colStr = params.get("collection");
		int queryInt = 1;
			
		if(colStr.equalsIgnoreCase(Collections.AUTO_TOTAL)) {
			queryInt = 2; 
		} else {
			queryInt = 1;
		}		
		
		QuerySet querySet = new QuerySet(queryInt);
		Query query = new Query();
		
		try {
			for(int i = 0 ; i < queryInt ; i++) {
				query = new Query();
				
				FilterFieldParseResult filterFieldParseResult = parseFilterParams(params);
				query.setSelect(parseAutoSelect(params, getCollection(params), i));
//				query.setFilter(parseFilter(params, filterFieldParseResult, getCollection(params)));
				query.setWhere(parseAutoWhere(params, filterFieldParseResult, getCollection(params), i));
	//			query.setGroupBy(parseGroupBy(params));
				query.setOrderby(parseOrderBy(params, getCollection(params)));
				query.setFrom(getCollection(params));
				query.setResult(parseStart(params) - 1, parseStart(params) + parseAutoSize(params, i) - 2);
				query.setSearchKeyword(parseQ(params));
				query.setFaultless(true);
				query.setThesaurusOption((byte) (Protocol.ThesaurusOption.EQUIV_SYNONYM | Protocol.ThesaurusOption.QUASI_SYNONYM));
				query.setSearchOption((byte) (Protocol.SearchOption.BANNED | Protocol.SearchOption.STOPWORD | Protocol.SearchOption.CACHE));
				query.setRankingOption((byte) (Protocol.RankingOption.CATEGORY_RANKING | Protocol.RankingOption.DOCUMENT_RANKING));
				query.setCategoryRankingOption((byte) (Protocol.CategoryRankingOption.EQUIV_SYNONYM | Protocol.CategoryRankingOption.QUASI_SYNONYM));	
				query.setLoggable(false);
				query.setPrintQuery(true);						// 실제 사용시 false
				parseTrigger(params, query, getCollection(params));
				query.setResultModifier("typo");
				
				querySet.addQuery(query);
			
				String queryStr = parser.queryToString(query);
//				System.out.println(" :::::::::: query ::::::: " + queryStr);
			}
									
			CommandSearchRequest commandSearchRequest = new CommandSearchRequest(Connection.IP, Connection.PORT);
					
			int returnCode = commandSearchRequest.request(querySet);
			
			if (returnCode <= -100) {
				ErrorMessageService.getInstance().minusReturnCodeLog(returnCode, commandSearchRequest.getException(), req);
				logMessageService.receiveEnd(reqHeader, request);
				return commandSearchRequestErrorResponse(commandSearchRequest.getException().getErrorMessage());
			} else {
				logMessageService.messageReceived(reqHeader, request);
			}
					
			String resultJson = "";

			if(colStr.equalsIgnoreCase(Collections.AUTO_TOTAL)) {
//				resultJson = gson.toJson(makeAutoResult(commandSearchRequest.getResultSet(), querySet, params));
				JsonObject jsonElement = gson.toJsonTree(makeAutoResult(commandSearchRequest.getResultSet(), querySet, params)).getAsJsonObject();
				resultJson = gson.toJson(jsonElement.getAsJsonObject("meta").get("result"));
			} else {
//				resultJson = gson.toJson(makeAutoTagResult(commandSearchRequest.getResultSet().getResult(0), query, params));
				JsonObject jsonElement = gson.toJsonTree(makeAutoTagResult(commandSearchRequest.getResultSet().getResult(0), query, params)).getAsJsonObject();
				resultJson = gson.toJson(jsonElement.get("result"));
			}
			
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
	
	// 인기검색어 API 
	public String hotKeyword(Map<String, String> params, Map<String, Object> reqHeader, HttpServletRequest request) {
		
		String req = "";
		req += "Host: " + (String) reqHeader.get("host") + "\n";
		req += "Connection: " + (String) reqHeader.get("connection") + "\n";
		req += "Upgrade-Insecure-Requests: " + (String) reqHeader.get("upgrade-insecure-requests") + "\n";
		req += "User-Agent: " + (String) reqHeader.get("user-agent") + "\n";
		req += "Accept: " + (String) reqHeader.get("accept") + "\n";
		req += "Accept-Encoding: " + (String) reqHeader.get("accept-encoding") + "\n";
		req += "Accept-Language: " + (String) reqHeader.get("accept-language");
		
		logMessageService.requestReceived(reqHeader, request);
		
		String ret = "";
		
		Gson gson = new Gson();
		
		QueryParser parser = new QueryParser();
		
		QuerySet querySet = new QuerySet(1);
		Query query = new Query();
		
		try {
			query.setSelect(parseHotSelect(params));
			query.setOrderby(parseOrderBy(params, getCollection(params)));
			query.setFrom(getCollection(params));
			query.setResult(parseStart(params) - 1, parseStart(params) + parseSize(params) - 2);
			query.setFaultless(true);
			query.setSearchOption(Protocol.SearchOption.CACHE);
			query.setLoggable(false);
			query.setPrintQuery(true);						// 실제 사용시 false
			parseTrigger(params, query, getCollection(params));
//			query.setQueryModifier("diver");
			query.setResultModifier("typo");
//				query.setDebug(true);
//				query.setFaultless(true);	
			
			querySet.addQuery(query);
		
			String queryStr = parser.queryToString(query);
//			System.out.println(" :::::::::: query ::::::: " + queryStr);
									
			CommandSearchRequest commandSearchRequest = new CommandSearchRequest(Connection.IP, Connection.PORT);
					
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
			
			String resultJson = gson.toJson(makeHotResult(commandSearchRequest.getResultSet().getResult(0), query, params));

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
	
	// 구매한 곡 검색 API 
	public String purchasedSearch(Map<String, Object> params, Map<String, Object> document, Map<String, Object> reqHeader, HttpServletRequest request) {
		
		List<Map<String, String>> index = (List<Map<String, String>>) params.get("index");
//		Map<String, Object> document = (Map<String, Object>) params.get("document");
		
		String collection = "";
		String q = "";
		String puchaseId = "";
		int start = (int) document.get("start");
		int size = (int) document.get("size");
		
		Map<String, String> idx1 = index.get(0);
		
		for(int i=0 ; i < index.size() ; i++) {
			if(index.get(i).get("name").equalsIgnoreCase("mv_track_artist_album_idx")) {
				collection = Collections.MV;
				q = index.get(i).get("query");
			} else if(index.get(i).get("name").equalsIgnoreCase("key_idx")) {
				puchaseId = index.get(i).get("query");
			} else {
				collection = Collections.TRACK;
				q = index.get(i).get("query");
			}
		}
		
		List<String> returns = (List<String>) document.get("returns");

		logMessageService.requestReceived(reqHeader, request);
		
		String ret = "";
		
		Gson gson = new Gson();
		
		QueryParser parser = new QueryParser();
		
		QuerySet querySet = new QuerySet(1);
		Query query = new Query();
		
		try {
			query = new Query();
			query.setSelect(parsePurchaseSelect(returns));
			query.setWhere(purchaseWhere(idx1, puchaseId, collection));
			query.setOrderby(purchaseOrderBy(collection));
			query.setFrom(collection);
			query.setResult(start-1, (start+size) - 2);
			query.setSearchKeyword(q);
			query.setFaultless(true);
			query.setThesaurusOption((byte) (Protocol.ThesaurusOption.EQUIV_SYNONYM | Protocol.ThesaurusOption.QUASI_SYNONYM));
			query.setSearchOption((byte) (Protocol.SearchOption.BANNED | Protocol.SearchOption.STOPWORD | Protocol.SearchOption.CACHE));
			query.setRankingOption((byte) (Protocol.RankingOption.CATEGORY_RANKING | Protocol.RankingOption.DOCUMENT_RANKING));
			query.setCategoryRankingOption((byte) (Protocol.CategoryRankingOption.EQUIV_SYNONYM | Protocol.CategoryRankingOption.QUASI_SYNONYM));	
			query.setLoggable(false);
			query.setPrintQuery(true);						// 실제 사용시 false
			query.setResultModifier("typo");
			
			querySet.addQuery(query);
		
			String queryStr = parser.queryToString(query);
//				System.out.println(" :::::::::: query ::::::: " + queryStr);
			
									
			CommandSearchRequest commandSearchRequest = new CommandSearchRequest(Connection.IP, Connection.PORT);
					
			int returnCode = commandSearchRequest.request(querySet);
			
			if (returnCode <= -100) {
				logMessageService.receiveEnd(reqHeader, request);
				return commandSearchRequestErrorResponse(commandSearchRequest.getException().getErrorMessage());
			} else {
				logMessageService.messageReceived(reqHeader, request);
			}
						
			String resultJson = "";
			
			resultJson = gson.toJson(makePurchaseResult(commandSearchRequest.getResultSet().getResult(0), query));
			
			ret = resultJson;
			
			logMessageService.receiveEnd(reqHeader, request);
			
		} catch (InvalidParameterException e) {
			logMessageService.receiveEnd(reqHeader, request);
			return invalidParameterResponse(e);
		} catch (Exception e) {
			logMessageService.receiveEnd(reqHeader, request);
			return internalServerResponse(e);
		}
		
		return ret;
		
	}
	
	// 유사곡 검색 API 
	public String similarSearch(List<Map<String, String>> index, Map<String, Object> document, Map<String, Object> reqHeader, HttpServletRequest request) {
		
		Map<String, String> params = new HashMap<String, String>();
		
		String collection = Collections.TRACK;
		String q = "";
		
		int start = (int) document.get("start");
		int size = (int) document.get("size");
		
		int num = 1;
		int searchSize = index.size() / 3;
		
		if(searchSize < 0) {
			searchSize = 0;
		}
		
		List<String> returns = (List<String>) document.get("returns");

		logMessageService.requestReceived(reqHeader, request);
		
		String ret = "";
		
		Gson gson = new Gson();
		
		QueryParser parser = new QueryParser();
		
		QuerySet querySet = new QuerySet(searchSize);
		Query query = new Query();
		FilterFieldParseResult filterFieldParseResult = parseFilterParams(params);
		
		try {
			for(int i = 0 ; i < searchSize ; i++) {
				
				for(int j = 0 ; j < index.size() ; j++) {
					if(num == Integer.parseInt(index.get(j).get("num"))) {
						if(index.get(j).get("name").equalsIgnoreCase("track_artist_album_idx")) {
							q = index.get(j).get("query");
						}
					}
				}
				
				query = new Query();
				query.setSelect(parsePurchaseSelect(returns));
				query.setWhere(similarWhere(index, collection, num));
				query.setFilter(parseTotalFilter(params, filterFieldParseResult, collection));
				query.setOrderby(purchaseOrderBy(collection));
				query.setFrom(collection);
				query.setResult(start-1, (start+size) - 2);
				query.setSearchKeyword(q);
				query.setFaultless(true);
				query.setThesaurusOption((byte) (Protocol.ThesaurusOption.EQUIV_SYNONYM | Protocol.ThesaurusOption.QUASI_SYNONYM));
				query.setSearchOption((byte) (Protocol.SearchOption.BANNED | Protocol.SearchOption.STOPWORD | Protocol.SearchOption.CACHE));
				query.setRankingOption((byte) (Protocol.RankingOption.CATEGORY_RANKING | Protocol.RankingOption.DOCUMENT_RANKING));
				query.setCategoryRankingOption((byte) (Protocol.CategoryRankingOption.EQUIV_SYNONYM | Protocol.CategoryRankingOption.QUASI_SYNONYM));	
				query.setLoggable(false);
				query.setPrintQuery(true);						// 실제 사용시 false
				query.setResultModifier("typo");
				
				querySet.addQuery(query);
			
				String queryStr = parser.queryToString(query);
//					System.out.println(" :::::::::: query ::::::: " + queryStr);
				num++;
			}
			
			CommandSearchRequest commandSearchRequest = new CommandSearchRequest(Connection.IP, Connection.PORT);
					
			int returnCode = commandSearchRequest.request(querySet);
			
			if (returnCode <= -100) {
				logMessageService.receiveEnd(reqHeader, request);
				return commandSearchRequestErrorResponse(commandSearchRequest.getException().getErrorMessage());
			} else {
				logMessageService.messageReceived(reqHeader, request);
			}
						
			String resultJson = "";
			
			resultJson = gson.toJson(makeSimilarResult(commandSearchRequest.getResultSet(), querySet));
			
			ret = resultJson;
			
			logMessageService.receiveEnd(reqHeader, request);
			
//				System.out.println(ret);
		} catch (InvalidParameterException e) {
			logMessageService.receiveEnd(reqHeader, request);
			return invalidParameterResponse(e);
		} catch (Exception e) {
			logMessageService.receiveEnd(reqHeader, request);
			return internalServerResponse(e);
		}
		
		return ret;
		
	}
	
	// 엔티티 검색 API (POST)
	public String EntitySearch(Map<String, Object> req, Map<String, Object> reqHeader, HttpServletRequest request) {
		
		Map<String, Object> filter = (Map<String, Object>) req.get("filter");
		Map<String, Object> q_index = (Map<String, Object>) req.get("query");
		Map<String, Object> document = (Map<String, Object>) req.get("document");
		
		List<Map<String, String>> index = (List<Map<String, String>>) q_index.get("index");
		
		String rawStr = (String) filter.get("raw");
		String[] raw = {};
		
		if(!rawStr.equalsIgnoreCase("")) {
			raw = rawStr.split("&&");
		} 
		
		String q = "";
		
		for(int j=0 ; j < index.size() ; j++) {
			if(index.get(j).get("type").equalsIgnoreCase("intersection")) {
				q = index.get(j).get("query");
			}
		}
		
		int start = (int) document.get("start");
		int size = (int) document.get("size");
		Map<String, String> sortMap = (Map<String, String>) document.get("sort"); 
		
		String sortVal = sortMap.get("name").toLowerCase() + ":" + sortMap.get("order").toLowerCase(); 
		
		boolean return_all = (boolean) document.get("return_all");

		logMessageService.requestReceived(reqHeader, request);
		
		String ret = "";
		
		Gson gson = new Gson();
		
		QueryParser parser = new QueryParser();
		
		QuerySet querySet = new QuerySet(1);
		Query query = new Query();
		
		try {
			query = new Query();
			query.setSelect(parseEntitySelect(return_all));
			query.setWhere(EntityWhere(index));
			if(raw.length > 0) {
				query.setFilter(EntityFilter(raw));
			}
			query.setOrderby(EntityOrderBy(sortVal, Collections.ENTITY));
			query.setFrom(Collections.ENTITY);
			query.setResult(start-1, (start+size) - 2);
			query.setSearchKeyword(q);
			query.setFaultless(true);
			query.setThesaurusOption((byte) (Protocol.ThesaurusOption.EQUIV_SYNONYM | Protocol.ThesaurusOption.QUASI_SYNONYM));
			query.setSearchOption((byte) (Protocol.SearchOption.BANNED | Protocol.SearchOption.STOPWORD | Protocol.SearchOption.CACHE));
			query.setRankingOption((byte) (Protocol.RankingOption.CATEGORY_RANKING | Protocol.RankingOption.DOCUMENT_RANKING));
			query.setCategoryRankingOption((byte) (Protocol.CategoryRankingOption.EQUIV_SYNONYM | Protocol.CategoryRankingOption.QUASI_SYNONYM));	
			query.setLoggable(false);
			query.setLoggable(true);
			query.setLogKeyword(q.toCharArray());
			query.setPrintQuery(true);						// 실제 사용시 false
			query.setResultModifier("typo");
			
			querySet.addQuery(query);
		
			String queryStr = parser.queryToString(query);
//					System.out.println(" :::::::::: query ::::::: " + queryStr);
			
			CommandSearchRequest commandSearchRequest = new CommandSearchRequest(Connection.IP, Connection.PORT);
					
			int returnCode = commandSearchRequest.request(querySet);
			
			if (returnCode <= -100) {
				logMessageService.receiveEnd(reqHeader, request);
				return commandSearchRequestErrorResponse(commandSearchRequest.getException().getErrorMessage());
			} else {
				logMessageService.messageReceived(reqHeader, request);
			}
						
			String resultJson = "";
			
			resultJson = gson.toJson(makeEntityResult(commandSearchRequest.getResultSet().getResult(0), query));
			
			ret = resultJson;
			
			logMessageService.receiveEnd(reqHeader, request);
			
//				System.out.println(ret);
		} catch (InvalidParameterException e) {
			logMessageService.receiveEnd(reqHeader, request);
			return invalidParameterResponse(e);
		} catch (Exception e) {
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
	
	protected String[] getTotalCollection(Map<String, String> params) {		
		String collec = params.get("collection");
		String[] colArray = {};
		
		if(collec.equalsIgnoreCase(Collections.TOTAL)) {
			colArray = new String[] {Collections.TRACK, Collections.ALBUM, Collections.ARTIST, Collections.MV, Collections.MUSICCAST, Collections.MUSICPD, Collections.MUSICPOST, Collections.CLASSIC, Collections.LYRICS};
		} else {
			colArray = new String[] {collec};
		}
		
		return colArray;
	}
	
	private FilterFieldParseResult parseFilterParams(Map<String, String> params) {
		return new FilterValueParser(params).parseAll();
	}
	
	private SelectSet[] parseSelect(Map<String, String> params) {
		return SelectSetService.getInstance().makeSelectSet(params);
	}
	
	private SelectSet[] parseHotSelect(Map<String, String> params) {
		return HotKwdSelectSet.getInstance().makeSelectSet(params);
	}
	
	private SelectSet[] parseAutoSelect(Map<String, String> params, String collection, int num) {
		if(collection.equalsIgnoreCase(Collections.AUTO_TOTAL)) {
			return AutoTotalSelectSet.getInstance().makeSelectSet(params, num);
		} else {
			return AutoTagSelectSet.getInstance().makeSelectSet(params);
		}		
	}
	
	private SelectSet[] parsePurchaseSelect(List<String> returns) {
		return PurchaseSelectSet.getInstance().makeSelectSet(returns);
	}
	
	private SelectSet[] parseEntitySelect(boolean return_all) {
		return EntitySelectSet.getInstance().makeSelectSet(return_all);
	}
	
	protected FilterSet[] parseFilter(Map<String, String> params, FilterFieldParseResult filterFieldParseResult, String collection) throws InvalidParameterException {
		return FilterSetService.getInstance().parseFilter(params, filterFieldParseResult, collection);
	}
	
	protected FilterSet[] parseTotalFilter(Map<String, String> params, FilterFieldParseResult filterFieldParseResult, String collection) throws InvalidParameterException {
		return TotalFilterSetService.getInstance().parseTotalFilter(params, filterFieldParseResult, collection);
	}
	
	protected FilterSet[] EntityFilter(String[] raw) throws InvalidParameterException {
		return EntityFilterSetService.getInstance().parseFilter(raw);
	}
	
	protected WhereSet[] parseWhere(Map<String, String> params, FilterFieldParseResult filterFieldParseResult, String collection) throws InvalidParameterException {
		return WhereSetService.getInstance().makeWhereSet(params, filterFieldParseResult, makeBaseWhereSet(params, collection));
	}
	
	protected List<WhereSet> makeBaseWhereSet(Map<String, String> params, String collection) throws InvalidParameterException {
		List<WhereSet> result = new ArrayList<WhereSet>();
		String keyword = parseQ(params);
//		String collection = getCollection(params);
		searchQoption qOption = new searchQoption(RestUtils.getParam(params, "q_option"), collection);
		
		String idxField = qOption.getIndexField();
		
		idxScoreMap = new HashMap<String, Integer>();

		if(collection.equalsIgnoreCase(Collections.TRACK)) {
			if(idxField.equalsIgnoreCase("track_idx")) {
				idxScoreMap.put("TRACK_IDX", 100);
				idxScoreMap.put("TRACK_IDX_WS", 100);
				idxScoreMap.put("SYN_TRACK_IDX_KO", 30);
				idxScoreMap.put("SYN_TRACK_IDX_WS", 30);
				idxScoreMap.put("SYN_TRACK_IDX", 30);
			} else if(idxField.equalsIgnoreCase("artist_idx")) {
				idxScoreMap.put("ARTIST_IDX", 100);
				idxScoreMap.put("ARTIST_IDX_WS", 100);
				idxScoreMap.put("SYN_ARTIST_IDX_WS", 30);
				idxScoreMap.put("SYN_ARTIST_IDX", 30);
			} else if(idxField.equalsIgnoreCase("album_idx")) {
				idxScoreMap.put("ALBUM_IDX", 100);
				idxScoreMap.put("ALBUM_IDX_WS", 100);
				idxScoreMap.put("SYN_ALBUM_IDX", 30);
				idxScoreMap.put("SYN_ALBUM_IDX_KO", 30);
				idxScoreMap.put("SYN_ALBUM_IDX_WS", 30);
			} else {
				if(!artist_keyword.equalsIgnoreCase("")) {
					if(qOption.isNofM()) {
						result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
						result.add(new WhereSet("TRACK_IDX", qOption.getOption(), keyword, track_score, qOption.getNofmPercent()));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("TRACK_IDX_WS", qOption.getOption(), keyword, track_score, qOption.getNofmPercent()));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("ARTIST_IDX", qOption.getOption(), keyword, artist_score, qOption.getNofmPercent()));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("ARTIST_IDX_WS", qOption.getOption(), keyword, artist_score, qOption.getNofmPercent()));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("ALBUM_IDX", qOption.getOption(), keyword, album_score, qOption.getNofmPercent()));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("ALBUM_IDX_WS", qOption.getOption(), keyword, album_score, qOption.getNofmPercent()));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("TRACK_ARTIST_ALBUM_IDX", qOption.getOption(), keyword, 30, qOption.getNofmPercent()));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("TRACK_ARTIST_ALBUM_IDX_WS", qOption.getOption(), keyword, 30, qOption.getNofmPercent()));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("SYN_TRACK_ARTIST_ALBUM_IDX", qOption.getOption(), keyword, 30, qOption.getNofmPercent()));
						result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
						
						result.add(new WhereSet(Protocol.WhereSet.OP_WEIGHTAND));
						
						result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
						result.add(new WhereSet("ARTIST_IDX", Protocol.WhereSet.OP_WEIGHTAND, artist_keyword, 500, qOption.getNofmPercent()));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("ARTIST_IDX_WS", qOption.getOption(), artist_keyword, 500, qOption.getNofmPercent()));
						result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
					} else {
						result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
						result.add(new WhereSet("TRACK_IDX", qOption.getOption(), keyword, track_score));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("TRACK_IDX_WS", qOption.getOption(), keyword, track_score));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("ARTIST_IDX", qOption.getOption(), keyword, artist_score));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("ARTIST_IDX_WS", qOption.getOption(), keyword, artist_score));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("ALBUM_IDX", qOption.getOption(), keyword, album_score));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("ALBUM_IDX_WS", qOption.getOption(), keyword, album_score));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("TRACK_ARTIST_ALBUM_IDX", qOption.getOption(), keyword, 30));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("TRACK_ARTIST_ALBUM_IDX_WS", qOption.getOption(), keyword, 30));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("SYN_TRACK_ARTIST_ALBUM_IDX", qOption.getOption(), keyword, 30));
						result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
						
						result.add(new WhereSet(Protocol.WhereSet.OP_WEIGHTAND));
						
						result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
						result.add(new WhereSet("ARTIST_IDX", qOption.getOption(), artist_keyword, 500));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("ARTIST_IDX_WS", qOption.getOption(), artist_keyword, 500));
						result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
					}
					artist_keyword = "";
					
				} else {
					if(track_score != 0) {
						idxScoreMap.put("TRACK_IDX", track_score);
						idxScoreMap.put("TRACK_IDX_WS", track_score);
						idxScoreMap.put("ARTIST_IDX", artist_score);
						idxScoreMap.put("ARTIST_IDX_WS", artist_score);
						idxScoreMap.put("ALBUM_IDX", album_score);
						idxScoreMap.put("ALBUM_IDX_WS", album_score);
					}
					idxScoreMap.put("TRACK_ARTIST_ALBUM_IDX", 30);
					idxScoreMap.put("TRACK_ARTIST_ALBUM_IDX_WS", 30);
					idxScoreMap.put("SYN_TRACK_ARTIST_ALBUM_IDX", 30);
				}
				
			}
		} else if(collection.equalsIgnoreCase(Collections.LYRICS)) {
				idxScoreMap.put("TRACK_IDX", 150);
				idxScoreMap.put("TRACK_IDX_WS", 150);
				idxScoreMap.put("ALBUM_IDX", 100);
				idxScoreMap.put("ALBUM_IDX_WS", 100);
				idxScoreMap.put("ARTIST_IDX", 300);
				idxScoreMap.put("ARTIST_IDX_WS", 300);
				idxScoreMap.put("LYRICS_IDX", 30);
				idxScoreMap.put("LYRICS_IDX_WS", 30);
				idxScoreMap.put("SYN_LYRICS_IDX", 30);
		} else if(collection.equalsIgnoreCase(Collections.ALBUM)) {
			if(idxField.equalsIgnoreCase("album_idx")) {
				idxScoreMap.put("ALBUM_IDX", 100);
				idxScoreMap.put("ALBUM_IDX_WS", 100);
				idxScoreMap.put("SYN_ALBUM_IDX", 30);
				idxScoreMap.put("SYN_ALBUM_IDX_KO", 30);
				idxScoreMap.put("SYN_ALBUM_IDX_WS", 30);
			} else if(idxField.equalsIgnoreCase("artist_idx")) {
				idxScoreMap.put("ARTIST_IDX", 100);
				idxScoreMap.put("ARTIST_IDX_WS", 100);
				idxScoreMap.put("SYN_ARTIST_IDX", 30);
				idxScoreMap.put("SYN_ARTIST_IDX_WS", 30);
			} else {
				idxScoreMap.put("ALBUM_IDX", 100);
				idxScoreMap.put("ALBUM_IDX_WS", 100);
				idxScoreMap.put("ARTIST_IDX", 300);
				idxScoreMap.put("ARTIST_IDX_WS", 300);
				idxScoreMap.put("ARTIST_ALBUM_IDX", 30);
				idxScoreMap.put("ARTIST_ALBUM_IDX_WS", 30);
				idxScoreMap.put("SYN_ARTIST_ALBUM_IDX", 30);
			}
		} else if(collection.equalsIgnoreCase(Collections.ARTIST)) {
				idxScoreMap.put("ARTIST_IDX", 200);
				idxScoreMap.put("ARTIST_IDX_WS", 200);
				idxScoreMap.put("GRP_NM_IDX", 100);
				idxScoreMap.put("GRP_NM_IDX_WS", 100);
				idxScoreMap.put("SYN_ARTIST_IDX_KO", 10);
				idxScoreMap.put("SYN_ARTIST_IDX", 10);
		} else if(collection.equalsIgnoreCase(Collections.MV)) {
			idxScoreMap.put("MV_TRACK_IDX", 100);
			idxScoreMap.put("MV_TRACK_IDX_WS", 100);
			idxScoreMap.put("ARTIST_IDX", 300);
			idxScoreMap.put("MV_TRACK_ARTIST_ALBUM_IDX", 30);
			idxScoreMap.put("SYN_MV_TRACK_ARTIST_ALBUM_IDX", 30);
			idxScoreMap.put("MV_TRACK_ARTIST_ALBUM_IDX_WS", 30);
		} else if(collection.equalsIgnoreCase(Collections.MUSICCAST)) {
			idxScoreMap.put("MUSICCAST_IDX", 100);
			idxScoreMap.put("MUSICCAST_IDX_WS", 100);
		} else if(collection.equalsIgnoreCase(Collections.MUSICPD)) {
			if(idxField.equalsIgnoreCase("musicpd_album_idx")) {
				idxScoreMap.put("MUSICPD_ALBUM_IDX", 100);
				idxScoreMap.put("MUSICPD_ALBUM_IDX_WS", 100);
			} else {
				idxScoreMap.put("MUSICPD_ALBUM_IDX", 100);
				idxScoreMap.put("MUSICPD_ALBUM_IDX_WS", 100);
			}
		} else if(collection.equalsIgnoreCase(Collections.MUSICPOST)) {
			idxScoreMap.put("MUSICPOST_IDX", 100);
			idxScoreMap.put("MUSICPOST_IDX_WS", 100);
		} else if(collection.equalsIgnoreCase(Collections.CLASSIC)) {
			idxScoreMap.put("ARTIST_IDX", 100);
			idxScoreMap.put("TITLE_IDX", 50);
			idxScoreMap.put("CLASSIC_IDX", 30);
		} else if(collection.equalsIgnoreCase(Collections.ENTITY)) {
			if(idxField.equalsIgnoreCase("track_idx")) {
				idxScoreMap.put("TRACK_IDX", 100);
			} else if(idxField.equalsIgnoreCase("album_idx")) {
				idxScoreMap.put("ALBUM_IDX", 100);
			} else if(idxField.equalsIgnoreCase("artist_idx")) {
				idxScoreMap.put("ARTIST_IDX", 100);
			} else if(idxField.equalsIgnoreCase("arranger_idx")) {
				idxScoreMap.put("ARRANGER_IDX", 100);
			} else if(idxField.equalsIgnoreCase("composer_idx")) {
				idxScoreMap.put("COMPOSER_IDX", 100);
			} else if(idxField.equalsIgnoreCase("featuring_idx")) {
				idxScoreMap.put("FEATURING_IDX", 100);
			} else if(idxField.equalsIgnoreCase("lyricist_idx")) {
				idxScoreMap.put("LYRICIST_IDX", 100);
			} else if(idxField.equalsIgnoreCase("genre_idx")) {
				idxScoreMap.put("GENRE_IDX", 100);
			} else if(idxField.equalsIgnoreCase("artist_role_idx")) {
				idxScoreMap.put("ARTIST_ROLE_IDX", 100);
			} else {
				idxScoreMap.put("TRACK_IDX", 100);
			} 
		} 

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
	
	protected WhereSet[] similarWhere(List<Map<String, String>> index, String collection, int num) throws InvalidParameterException {
		return PurchaseWhereSet.getInstance().makeWhereSet("", makeSimilarWhereSet(index, collection, num));
	}
	
	protected List<WhereSet> makeSimilarWhereSet(List<Map<String, String>> index, String collection, int num) throws InvalidParameterException {
		List<WhereSet> result = new ArrayList<WhereSet>();
		String q = "";
		String tarckQ = "";
		String artistQ = "";
		int nofmNum = 0;
		double DnofmNum = 0;
		
		String operand = "";
		
		byte option;

		for(int i=0 ; i < index.size() ; i++) {
			idxScoreMap = new HashMap<String, Integer>();
						
			if(num == Integer.parseInt(index.get(i).get("num"))) {				
				if(index.get(i).get("name").equalsIgnoreCase("track_artist_album_idx")) {
					q = index.get(i).get("query");
					operand = index.get(i).get("operand");
					
					if (operand.startsWith("nofm")) {
					 	option = Protocol.WhereSet.OP_N_OF_M;
					 	DnofmNum = Double.parseDouble(String.valueOf(index.get(i).get("nofm")));
					 	
					 	if(DnofmNum < 0){
					 		DnofmNum = 0;
			            }else if(DnofmNum > 1){
			            	DnofmNum = 1;
			            } 
					 	
					 	nofmNum = (int) (DnofmNum * 100);
					 	
					 	if(result.size() > 0) {
					 		result.add(new WhereSet(Protocol.WhereSet.OP_AND));
					 	}
					 	
					 	result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
						result.add(new WhereSet("TRACK_ARTIST_ALBUM_IDX", option, q, 30, nofmNum));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("TRACK_ARTIST_ALBUM_IDX_WS", option, q, 30, nofmNum));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("SYN_TRACK_ARTIST_ALBUM_IDX", option, q, 30, nofmNum));
						result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
			        } else {
			        	if (operand.equalsIgnoreCase("or")) {
			        		option = Protocol.WhereSet.OP_HASANY;
			        	} else {
			        		option = Protocol.WhereSet.OP_HASALL;
			        	}
			        	
			        	if(result.size() > 0) {
					 		result.add(new WhereSet(Protocol.WhereSet.OP_AND));
					 	}
			        	result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
						result.add(new WhereSet("TRACK_ARTIST_ALBUM_IDX", option, q, 30));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("TRACK_ARTIST_ALBUM_IDX_WS", option, q, 30));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("SYN_TRACK_ARTIST_ALBUM_IDX", option, q, 30));
						result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
			        }
					
										
				} else {
					if(index.get(i).get("name").equalsIgnoreCase("track_idx")) {
						tarckQ = index.get(i).get("query");
						operand = index.get(i).get("operand");
						
						if (operand.startsWith("nofm")) {
						 	option = Protocol.WhereSet.OP_N_OF_M;
						 	DnofmNum = Double.parseDouble(String.valueOf(index.get(i).get("nofm")));
						 							 	
						 	if(DnofmNum < 0){
						 		DnofmNum = 0;
				            }else if(DnofmNum > 1){
				            	DnofmNum = 1;
				            } 
						 	
						 	nofmNum = (int) (DnofmNum * 100);
						 	
						 	if(result.size() > 0) {
						 		result.add(new WhereSet(Protocol.WhereSet.OP_AND));
						 	}
						 	
						 	result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
							result.add(new WhereSet("TRACK_IDX", option, tarckQ, 100, nofmNum));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.add(new WhereSet("TRACK_IDX_WS", option, tarckQ, 100, nofmNum));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.add(new WhereSet("SYN_TRACK_IDX", option, tarckQ, 30, nofmNum));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.add(new WhereSet("SYN_TRACK_IDX_KO", option, tarckQ, 30, nofmNum));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.add(new WhereSet("SYN_TRACK_IDX_WS", option, tarckQ, 30, nofmNum));
							result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
				        } else {
				        	if (operand.equalsIgnoreCase("or")) {
				        		option = Protocol.WhereSet.OP_HASANY;
				        	} else {
				        		option = Protocol.WhereSet.OP_HASALL;
				        	}
				        	
				        	if(result.size() > 0) {
						 		result.add(new WhereSet(Protocol.WhereSet.OP_AND));
						 	}
				        	
				        	result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
							result.add(new WhereSet("TRACK_IDX", option, tarckQ, 100));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.add(new WhereSet("TRACK_IDX_WS", option, tarckQ, 100));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.add(new WhereSet("SYN_TRACK_IDX", option, tarckQ, 30));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.add(new WhereSet("SYN_TRACK_IDX_KO", option, tarckQ, 30));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.add(new WhereSet("SYN_TRACK_IDX_WS", option, tarckQ, 30));
							result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
				        }
					} else {
						artistQ = index.get(i).get("query");
						operand = index.get(i).get("operand");
						
						if (operand.startsWith("nofm")) {
						 	option = Protocol.WhereSet.OP_N_OF_M;
						 	DnofmNum = Double.parseDouble(String.valueOf(index.get(i).get("nofm")));
						 	
						 	if(DnofmNum < 0){
						 		DnofmNum = 0;
				            }else if(DnofmNum > 1){
				            	DnofmNum = 1;
				            } 
						 	
						 	nofmNum = (int) (DnofmNum * 100);
						 	if(result.size() > 0) {
						 		result.add(new WhereSet(Protocol.WhereSet.OP_AND));
						 	}
						 	
							result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
							result.add(new WhereSet("ARTIST_IDX", option, artistQ, 100, nofmNum));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.add(new WhereSet("ARTIST_IDX_WS", option, artistQ, 100, nofmNum));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.add(new WhereSet("SYN_ARTIST_IDX", option, artistQ, 30, nofmNum));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.add(new WhereSet("SYN_ARTIST_IDX_WS", option, artistQ, 30, nofmNum));
							result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
				        } else {
				        	if (operand.equalsIgnoreCase("or")) {
				        		option = Protocol.WhereSet.OP_HASANY;
				        	} else {
				        		option = Protocol.WhereSet.OP_HASALL;
				        	}
				        	
				        	if(result.size() > 0) {
						 		result.add(new WhereSet(Protocol.WhereSet.OP_AND));
						 	}
				        	
							result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
							result.add(new WhereSet("ARTIST_IDX", option, artistQ, 100));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.add(new WhereSet("ARTIST_IDX_WS", option, artistQ, 100));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.add(new WhereSet("SYN_ARTIST_IDX", option, artistQ, 30));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.add(new WhereSet("SYN_ARTIST_IDX_WS", option, artistQ, 30));
							result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
				        }
					}
				}
			}			
		}
		
		return result;
	}
	
	protected WhereSet[] purchaseWhere(Map<String, String> idx1, String puchaseId, String collection) throws InvalidParameterException {
		return PurchaseWhereSet.getInstance().makeWhereSet(puchaseId, makePurchaseWhereSet(idx1, puchaseId, collection));
	}
	
	protected List<WhereSet> makePurchaseWhereSet(Map<String, String> idx1, String puchaseId, String collection) throws InvalidParameterException {
		List<WhereSet> result = new ArrayList<WhereSet>();
		String keyword = idx1.get("query");
		String operand = idx1.get("operand");
		String name = idx1.get("name");
		byte option;
		
		searchQoption qOption = new searchQoption(operand, collection);
		
			if (operand.startsWith("nofm")) {
			 	option = Protocol.WhereSet.OP_N_OF_M;
	        } else if (operand.equalsIgnoreCase("or")) {
	        	option = Protocol.WhereSet.OP_HASANY;
	        } else {
	        	option = Protocol.WhereSet.OP_HASALL;
	        }
		
		idxScoreMap = new HashMap<String, Integer>();
		
		if(collection.equalsIgnoreCase(Collections.TRACK)) {
			if(name.equalsIgnoreCase("artist_idx")) {
				idxScoreMap.put("ARTIST_IDX", 100);
				idxScoreMap.put("ARTIST_IDX_WS", 100);
				idxScoreMap.put("SYN_ARTIST_IDX", 30);
				idxScoreMap.put("SYN_ARTIST_IDX_WS", 30);
			} else if(name.equalsIgnoreCase("album_idx")) {
				idxScoreMap.put("ALBUM_IDX", 100);
				idxScoreMap.put("ALBUM_IDX_WS", 100);
				idxScoreMap.put("SYN_ALBUM_IDX", 30);
				idxScoreMap.put("SYN_ALBUM_IDX_KO", 30);
				idxScoreMap.put("SYN_ALBUM_IDX_WS", 30);
			}
		} else if(collection.equalsIgnoreCase(Collections.MV)) {
			idxScoreMap.put("MV_TRACK_IDX", 100);
			idxScoreMap.put("MV_TRACK_IDX_WS", 100);
			idxScoreMap.put("ARTIST_IDX", 300);
			idxScoreMap.put("MV_TRACK_ARTIST_ALBUM_IDX", 30);
			idxScoreMap.put("SYN_MV_TRACK_ARTIST_ALBUM_IDX", 30);
			idxScoreMap.put("MV_TRACK_ARTIST_ALBUM_IDX_WS", 30);
		} 
				
		result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
		
		int opValue = 0;
		
		for (Entry<String, Integer> e : idxScoreMap.entrySet()) {
			if (opValue > 0) {
				result.add(new WhereSet(Protocol.WhereSet.OP_OR));
			}
			
			if (operand.startsWith("nofm")) {
				result.add(new WhereSet(e.getKey(), option, keyword, e.getValue(), qOption.getNofmPercent()));
			} else {
				result.add(new WhereSet(e.getKey(), option, keyword, e.getValue()));
			}
			
			opValue = 1;
		}
		result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
		
		return result;
	}
	
	protected WhereSet[] EntityWhere(List<Map<String, String>> index) throws InvalidParameterException {
		return EntityWhereSet.getInstance().makeWhereSet(makeEntityWhereSet(index));
	}
	
	protected List<WhereSet> makeEntityWhereSet(List<Map<String, String>> index) throws InvalidParameterException {
		List<WhereSet> result = new ArrayList<WhereSet>();
		String keyword = "";
		String operand = "";
		String name = "";
		String type = "";
		String sub_type = "";
				
		byte option;
	
		idxScoreMap = new HashMap<String, Integer>();
		
		for(int i=0 ; i < index.size() ; i++) {
			operand = index.get(i).get("operand");
			keyword = index.get(i).get("query");
			type = index.get(i).get("type");
			name = index.get(i).get("name");
			
			if (operand.startsWith("nofm")) {
			 	option = Protocol.WhereSet.OP_N_OF_M;
	        } else if (operand.equalsIgnoreCase("or")) {
	        	option = Protocol.WhereSet.OP_HASANY;
	        } else {
	        	option = Protocol.WhereSet.OP_HASALL;
	        }
			
			if (result.size() > 0) {
				if(type.equalsIgnoreCase("intersection")) {
					if (operand.startsWith("and")) {
						result.add(new WhereSet(Protocol.WhereSet.OP_AND));
			        } else {
			        	result.add(new WhereSet(Protocol.WhereSet.OP_OR));
			        }
				} else if(type.equalsIgnoreCase("exclusion")) {
					result.add(new WhereSet(Protocol.WhereSet.OP_NOT));
				}
			}
			
			if(type.equalsIgnoreCase("intersection")) {
				result.add(new WhereSet(name.toUpperCase(), option, keyword, 100));
			} else if(type.equalsIgnoreCase("exclusion")) {
				result.add(new WhereSet(name.toUpperCase(), option, keyword));
			}
		}
//		
//				
//		result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
//		
//		int opValue = 0;
//		
//		for (Entry<String, Integer> e : idxScoreMap.entrySet()) {
//			if (opValue > 0) {
//				result.add(new WhereSet(Protocol.WhereSet.OP_OR));
//			}
//					
//			result.add(new WhereSet(e.getKey(), option, keyword, e.getValue()));
//			opValue = 1;
//		}
//		result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
		
		return result;
	}
	
	protected WhereSet[] parseAutoWhere(Map<String, String> params, FilterFieldParseResult filterFieldParseResult, String collection, int num) throws InvalidParameterException {
		return WhereSetService.getInstance().makeWhereSet(params, filterFieldParseResult, makeAutoWhereSet(params, collection, num));
	}
	
	protected List<WhereSet> makeAutoWhereSet(Map<String, String> params, String collection, int num) throws InvalidParameterException {
		List<WhereSet> result = new ArrayList<WhereSet>();
		String keyword = parseQ(params);
		searchQoption qOption = new searchQoption(RestUtils.getParam(params, "q_option"), collection);
				
		idxScoreMap = new HashMap<String, Integer>();

		if(collection.equalsIgnoreCase(Collections.AUTO_TAG)) {
			idxScoreMap.put("FKEY_NOSP", 100);
			idxScoreMap.put("FKEY", 50);
			idxScoreMap.put("BKEY", 30);
		} else {
			if(num == 0) {
				result.add(new WhereSet("ARTIST_IDX", Protocol.WhereSet.OP_HASALL, keyword, 100));
//				idxScoreMap.put("ARTIST_IDX", 100);
			} else {
				idxScoreMap.put("ARTR_IDX", 1000); 
				idxScoreMap.put("FKEY_NOSP", 100);
				idxScoreMap.put("FKEY", 50);
				idxScoreMap.put("BKEY", 30);
			}
		}
		
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
	
	protected OrderBySet[] parseOrderBy(Map<String, String> params, String collection) {
		return new OrderBySet[] { OrderBySetService.getInstance().getOrderBySet(RestUtils.getParam(params, "sort"), collection) };
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
	
	// 자동완성 사이즈
	protected int parseAutoSize(Map<String, String> params, int num) {
		String size = RestUtils.getParam(params, "size");
		String collection = getCollection(params);
		
		if(collection.equalsIgnoreCase(Collections.AUTO_TAG)) {
			if (size.equals("")) {
				return 10;
			} else {
				return Integer.parseInt(size);
			}
		} else {
			if(num == 0) {
				return 3;
			} else {
				if (size.equals("")) {
					return 10;
				} else {
					return Integer.parseInt(size);
				}
			}
		}
	}
	
	// 통합검색 사이즈 체크
	protected int parseTotalSize(Map<String, String> params, String collection) {
		
		String col_size = collection + "@size";
		String size = RestUtils.getParam(params, col_size.toLowerCase());
		
		if (size.equals("")) {
			return 10;
		}
		return Integer.parseInt(size);
	}
	
	// 통합검색 정렬 체크
	protected OrderBySet[] parseTotalOrderBy(Map<String, String> params, String collection) {
		
		String col_sort = collection + "@sort";
		
		return new OrderBySet[] { OrderBySetService.getInstance().getOrderBySet(RestUtils.getParam(params, col_sort.toLowerCase()), collection) };
	}
	
	// 구매곡 정렬
	protected OrderBySet[] purchaseOrderBy(String collection) {
		return new OrderBySet[] { OrderBySetService.getInstance().getOrderBySet("", collection) };
	}
	
	// 엔티티 정렬
	protected OrderBySet[] EntityOrderBy(String value, String collection) {
		return new OrderBySet[] { OrderBySetService.getInstance().getOrderBySet(value, collection) };
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
	
	protected HotKwdResult makeHotResult(Result result, Query query, Map<String, String> params) throws IRException {
		return HotKwdResult.makeResult(query, result, params);
	}
	
	protected TotalResult makeTotalResult(ResultSet result, QuerySet query, Map<String, String> params) throws IRException {
		return TotalResult.makeTotalResult(query, result, params);
	}
	
	protected AutoTotalResult makeAutoResult(ResultSet result, QuerySet query, Map<String, String> params) throws IRException {
		return AutoTotalResult.makeAutoTotalResult(query, result, params);
	}
	
	protected AutoResult makeAutoTagResult(Result result, Query query, Map<String, String> params) throws IRException {
		String tag = RestUtils.getParam(params, "filter.use_esalbum_yn");

		return AutoResult.makeAutoTagResult(query, result, params, tag);
	}
	
	protected PurchaseResult makePurchaseResult(Result result, Query query) throws IRException {
		return PurchaseResult.makePurchaseResult(query, result);
	}
	
	protected SimilarResult makeSimilarResult(ResultSet result, QuerySet query) throws IRException {
		return SimilarResult.makeSimilarResult(query, result);
	}
	
	protected EntityResult makeEntityResult(Result result, Query query) throws IRException {
		return EntityResult.makeEntityResult(query, result);
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
