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
import com.diquest.ir.common.msg.protocol.result.ResultSet;
import com.diquest.ir.rest.common.exception.InvalidParameterException;
import com.diquest.ir.rest.json.gson.GsonLoader;
import com.diquest.ir.rest.util.RestUtils;
import com.diquest.mapper.AdminMapper;
import com.diquest.rest.nhn.common.SayclubCollections;
import com.diquest.rest.nhn.common.Collections;
import com.diquest.rest.nhn.common.Connection;
import com.diquest.rest.nhn.filter.parse.SayclubFilterValue;
import com.diquest.rest.nhn.filter.result.FilterFieldParseResult;
import com.diquest.rest.nhn.result.NhnError;
import com.diquest.rest.nhn.result.NhnResult;
import com.diquest.rest.nhn.result.SayMallResult;
import com.diquest.rest.nhn.result.SayclubAutoResult;
import com.diquest.rest.nhn.result.SayclubNewResult;
import com.diquest.rest.nhn.result.SayclubResult;
import com.diquest.rest.nhn.result.SayclubTotalResult;
import com.diquest.rest.nhn.service.error.ErrorMessageService;
import com.diquest.rest.nhn.service.error.logMessageService;
import com.diquest.rest.nhn.service.filter.SayclubFilterSetService;
import com.diquest.rest.nhn.service.orderby.SayclubOrderBySet;
import com.diquest.rest.nhn.service.select.SayclubNewSelectSet;
import com.diquest.rest.nhn.service.select.SayclubSelectSet;
import com.diquest.rest.nhn.service.trigger.TriggerFieldService;
import com.diquest.rest.nhn.service.where.SayclubWhereSet;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

@Service
public class SayclubRestService {
	
	@Resource
	private SayclubRestService self;
	
	@Autowired
	AdminMapper adminMapper;
	
	protected HashMap<String, Integer> idxScoreMap = new HashMap<String, Integer>();
	
	protected static String currTimezone = new SimpleDateFormat("XXX").format(new Date()).replace(":", "");

	protected static int track_score = 100;
	protected static int album_score = 100;
	protected static int artist_score = 100;
	
	protected static String artist_keyword = "";
	
	public SayclubRestService() {

	}
	
	// 리뉴얼 검색
	public String sayNewSearch(Map<String, String> params, Map<String, Object> reqHeader, HttpServletRequest request) {
		
		String req = "";
		req += "Host: " + (String) reqHeader.get("host") + "\n";
		req += "Connection: " + (String) reqHeader.get("connection") + "\n";
		req += "Upgrade-Insecure-Requests: " + (String) reqHeader.get("upgrade-insecure-requests") + "\n";
		req += "User-Agent: " + (String) reqHeader.get("user-agent") + "\n";
		req += "Accept: " + (String) reqHeader.get("accept") + "\n";
		req += "Accept-Encoding: " + (String) reqHeader.get("accept-encoding") + "\n";
		req += "Accept-Language: " + (String) reqHeader.get("accept-language");
		
		String OriginKwd = "";
		
		if(params.get("q") != null) {
			OriginKwd = parseQ(params);		
		} else {			
			OriginKwd = "";
//			return makeEmptyNhnData(params);
		}
		
		logMessageService.requestReceived(reqHeader, request);
		
		String collection = getCollection(params);
		String ret = "";
		
		Gson gson = new Gson();
		
		char[] startTag = "<b>".toCharArray(); // Highlight tag 설정 startTag
		char[] endTag = "</b>".toCharArray(); // Highlight tag 설정 endTag
		
		QueryParser parser = new QueryParser();
		
		QuerySet querySet = new QuerySet(1);
		Query query = new Query(startTag, endTag);
		
		try {
			
			FilterFieldParseResult filterFieldParseResult = parseFilterParams(params);
			query.setSelect(parseNewSelect(params));
//			if(!RangeFilter.equalsIgnoreCase("")) {
//				query.setFilter(parseFilter(collection, RangeFilter, RangeKey));
//			}
			query.setWhere(parseNewWhere(params, filterFieldParseResult, collection, OriginKwd));
//			query.setGroupBy(parseNewGroupBy(params));
			query.setOrderby(parseNewOrderBy(params, collection));
			query.setFrom(collection);
			query.setResult(parseStart(params) - 1, parseStart(params) + parseSize(params) - 2);
			query.setSearchKeyword(OriginKwd);
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
			parseTrigger(params, query, collection);
			query.setResultModifier("typo");
			query.setValue("typo-parameters", OriginKwd);
	    	query.setValue("typo-options", "ALPHABETS_TO_HANGUL|HANGUL_TO_ALPHABETS");
	    	query.setValue("typo-correct-result-num", "1");
			
			querySet.addQuery(query);
		
			String queryStr = parser.queryToString(query);
//			System.out.println(" :::::::::: query ::::::: " + queryStr);
			
			CommandSearchRequest.setProps(Connection.IP, Connection.PORT, 5000, 50, 50);
			CommandSearchRequest commandSearchRequest = new CommandSearchRequest(Connection.IP, Connection.PORT);
					
			int returnCode = commandSearchRequest.request(querySet);
			
			if (returnCode <= -100) {
				ErrorMessageService.getInstance().minusReturnCodeLog(returnCode, commandSearchRequest.getException(), req);
				logMessageService.receiveEnd(reqHeader, request);
				return commandSearchRequestErrorResponse(commandSearchRequest.getException().getErrorMessage());
			} else {
				logMessageService.messageReceived(reqHeader, request);
				
				ResultSet resultSet = commandSearchRequest.getResultSet();
    			Result[] resultlist = resultSet.getResultList();
    			Result result1 = resultlist[0];
    			    			
    			int totalSize = 0;
    			String typoKwd = "";
    			
    			totalSize = result1.getTotalSize();
    			    			
    			if (totalSize == 0) {
    				if (result1.getValue("typo-result") != null) {
    					typoKwd = result1.getValue("typo-result");
					}
    				
    				if (!typoKwd.equals("")) {
    					params.put("q", typoKwd);
    					querySet = new QuerySet(1);
    					
						query = new Query();
						    						
						filterFieldParseResult = parseFilterParams(params);
						query.setSelect(parseNewSelect(params));
						query.setWhere(parseNewWhere(params, filterFieldParseResult, collection, parseQ(params)));
//						query.setGroupBy(parseNewGroupBy(params));
						query.setOrderby(parseNewOrderBy(params, getCollection(params)));
						query.setFrom(collection);
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
						parseTrigger(params, query, collection);
						
						querySet.addQuery(query);
					
						queryStr = parser.queryToString(query);
//						System.out.println(" :::::::::: query ::::::: " + queryStr);
    					
    					commandSearchRequest = new CommandSearchRequest(Connection.IP, Connection.PORT);
    							
    					returnCode = commandSearchRequest.request(querySet);
    					
    					if (returnCode <= -100) {
    						ErrorMessageService.getInstance().minusReturnCodeLog(returnCode, commandSearchRequest.getException(), req);
    						logMessageService.receiveEnd(reqHeader, request);
    						return commandSearchRequestErrorResponse(commandSearchRequest.getException().getErrorMessage());
    					} else {
    						logMessageService.messageReceived(reqHeader, request);
    					}
    				}
    			}
    			params.put("q", OriginKwd);
			}
			
//			System.out.println(returnCode);
//			System.out.println(commandSearchRequest.getResultSet().getResult(0).getTotalSize());
			
			String resultJson = "";
			
			resultJson = gson.toJson(makeNewResult(commandSearchRequest.getResultSet().getResult(0), query, params, collection));
			
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
	
	// 방송국 자동완성 API 
	public String sayAutosearch(Map<String, String> params, Map<String, Object> reqHeader, HttpServletRequest request) {

		String req = "";
		req += "Host: " + (String) reqHeader.get("host") + "\n";
		req += "Connection: " + (String) reqHeader.get("connection") + "\n";
		req += "Upgrade-Insecure-Requests: " + (String) reqHeader.get("upgrade-insecure-requests") + "\n";
		req += "User-Agent: " + (String) reqHeader.get("user-agent") + "\n";
		req += "Accept: " + (String) reqHeader.get("accept") + "\n";
		req += "Accept-Encoding: " + (String) reqHeader.get("accept-encoding") + "\n";
		req += "Accept-Language: " + (String) reqHeader.get("accept-language");
		
		String OriginKwd = "";
		
		if(params.get("q") != null) {
			OriginKwd = parseQ(params);		
		} else {			
			OriginKwd = "";
//			return makeEmptyNhnData(params);
		}
		
		logMessageService.requestReceived(reqHeader, request);
		
		String collection = getCollection(params);
		String ret = "";
		
		Gson gson = new Gson();
		
		QueryParser parser = new QueryParser();
		
		QuerySet querySet = new QuerySet(1);
		Query query = new Query();
			
		try {
			
			FilterFieldParseResult filterFieldParseResult = parseFilterParams(params);
			query.setSelect(parseNewSelect(params));
			query.setWhere(parseNewWhere(params, filterFieldParseResult, collection, OriginKwd));
			query.setOrderby(parseNewOrderBy(params, collection));
			query.setFrom(collection);
			query.setResult(parseStart(params) - 1, parseStart(params) + parseSize(params) - 2);
			query.setSearchKeyword(OriginKwd);
			query.setFaultless(true);
			query.setThesaurusOption((byte) (Protocol.ThesaurusOption.EQUIV_SYNONYM | Protocol.ThesaurusOption.QUASI_SYNONYM));
			query.setSearchOption((byte) (Protocol.SearchOption.BANNED | Protocol.SearchOption.STOPWORD | Protocol.SearchOption.CACHE));
			query.setRankingOption((byte) (Protocol.RankingOption.CATEGORY_RANKING | Protocol.RankingOption.DOCUMENT_RANKING));
			query.setCategoryRankingOption((byte) (Protocol.CategoryRankingOption.EQUIV_SYNONYM | Protocol.CategoryRankingOption.QUASI_SYNONYM));	
//			query.setUserName(getUserName(params));										// 로그인 사용자 ID 기록
			query.setLoggable(getLoggable(RestUtils.getParam(params, "search_tp")));
			query.setLogKeyword(parseQ(params).toCharArray());
			query.setPrintQuery(true);						// 실제 사용시 false
			parseTrigger(params, query, collection);
			query.setResultModifier("typo");
			query.setValue("typo-parameters", OriginKwd);
	    	query.setValue("typo-options", "ALPHABETS_TO_HANGUL|HANGUL_TO_ALPHABETS");
	    	query.setValue("typo-correct-result-num", "1");
			
			querySet.addQuery(query);
		
			String queryStr = parser.queryToString(query);
//			System.out.println(" :::::::::: query ::::::: " + queryStr);
			
			CommandSearchRequest.setProps(Connection.IP, Connection.PORT, 5000, 50, 50);
			CommandSearchRequest commandSearchRequest = new CommandSearchRequest(Connection.IP, Connection.PORT);
					
			int returnCode = commandSearchRequest.request(querySet);
			
			if (returnCode <= -100) {
				ErrorMessageService.getInstance().minusReturnCodeLog(returnCode, commandSearchRequest.getException(), req);
				logMessageService.receiveEnd(reqHeader, request);
				return commandSearchRequestErrorResponse(commandSearchRequest.getException().getErrorMessage());
			} else {
				logMessageService.messageReceived(reqHeader, request);
				
				ResultSet resultSet = commandSearchRequest.getResultSet();
    			Result[] resultlist = resultSet.getResultList();
    			Result result1 = resultlist[0];
    			    			
    			int totalSize = 0;
    			String typoKwd = "";
    			
    			totalSize = result1.getTotalSize();
    			    			
    			if (totalSize == 0) {
    				if (result1.getValue("typo-result") != null) {
    					typoKwd = result1.getValue("typo-result");
					}
    				
    				if (!typoKwd.equals("")) {
    					params.put("q", typoKwd);
    					querySet = new QuerySet(1);
    					
						query = new Query();
						    						
						filterFieldParseResult = parseFilterParams(params);
						query.setSelect(parseNewSelect(params));
						query.setWhere(parseNewWhere(params, filterFieldParseResult, collection, parseQ(params)));
						query.setOrderby(parseNewOrderBy(params, getCollection(params)));
						query.setFrom(collection);
						query.setResult(parseStart(params) - 1, parseStart(params) + parseSize(params) - 2);
						query.setSearchKeyword(parseQ(params));
						query.setFaultless(true);
						query.setThesaurusOption((byte) (Protocol.ThesaurusOption.EQUIV_SYNONYM | Protocol.ThesaurusOption.QUASI_SYNONYM));
						query.setSearchOption((byte) (Protocol.SearchOption.BANNED | Protocol.SearchOption.STOPWORD | Protocol.SearchOption.CACHE));
						query.setRankingOption((byte) (Protocol.RankingOption.CATEGORY_RANKING | Protocol.RankingOption.DOCUMENT_RANKING));
						query.setCategoryRankingOption((byte) (Protocol.CategoryRankingOption.EQUIV_SYNONYM | Protocol.CategoryRankingOption.QUASI_SYNONYM));	
//						query.setUserName(getUserName(params));										// 로그인 사용자 ID 기록
						query.setLoggable(getLoggable(RestUtils.getParam(params, "search_tp")));
						query.setLogKeyword(parseQ(params).toCharArray());
						query.setPrintQuery(true);						// 실제 사용시 false
						parseTrigger(params, query, collection);
						
						querySet.addQuery(query);
					
						queryStr = parser.queryToString(query);
//						System.out.println(" :::::::::: query ::::::: " + queryStr);
    					
    					commandSearchRequest = new CommandSearchRequest(Connection.IP, Connection.PORT);
    							
    					returnCode = commandSearchRequest.request(querySet);
    					
    					if (returnCode <= -100) {
    						ErrorMessageService.getInstance().minusReturnCodeLog(returnCode, commandSearchRequest.getException(), req);
    						logMessageService.receiveEnd(reqHeader, request);
    						return commandSearchRequestErrorResponse(commandSearchRequest.getException().getErrorMessage());
    					} else {
    						logMessageService.messageReceived(reqHeader, request);
    					}
    				}
    			}
    			params.put("q", OriginKwd);
			}
			
//			System.out.println(returnCode);
//			System.out.println(commandSearchRequest.getResultSet().getResult(0).getTotalSize());
			
			String resultJson = "";
			
			JsonObject jsonElement = gson.toJsonTree(makeAutoResult(commandSearchRequest.getResultSet().getResult(0), query, params)).getAsJsonObject();
			resultJson = gson.toJson(jsonElement.get("result"));
			
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
	
	// 리뉴얼 통합검색
	public String sayNewTotalSearch(Map<String, String> params, Map<String, Object> reqHeader, HttpServletRequest request) {
		
		String req = "";
		req += "Host: " + (String) reqHeader.get("host") + "\n";
		req += "Connection: " + (String) reqHeader.get("connection") + "\n";
		req += "Upgrade-Insecure-Requests: " + (String) reqHeader.get("upgrade-insecure-requests") + "\n";
		req += "User-Agent: " + (String) reqHeader.get("user-agent") + "\n";
		req += "Accept: " + (String) reqHeader.get("accept") + "\n";
		req += "Accept-Encoding: " + (String) reqHeader.get("accept-encoding") + "\n";
		req += "Accept-Language: " + (String) reqHeader.get("accept-language");
		
		String OriginKwd = "";
		
		if(params.get("q") != null) {
			OriginKwd = parseQ(params);		
		} else {			
			OriginKwd = "";
//				return makeEmptyNhnData(params);
		}
		
		logMessageService.requestReceived(reqHeader, request);
		
		String colStr = params.get("collection");
		String[] colArray = getTotalCollection(params);
		int queryInt = 1;
			
		if(colStr.equalsIgnoreCase(Collections.TOTAL)) {
			queryInt = colArray.length; 
		} else {
			queryInt = 1;
		}
		
		String ret = "";
		
		Gson gson = new Gson();
		
		char[] startTag = "<b>".toCharArray(); // Highlight tag 설정 startTag
		char[] endTag = "</b>".toCharArray(); // Highlight tag 설정 endTag
		
		QueryParser parser = new QueryParser();
		
		QuerySet querySet = new QuerySet(queryInt);
		Query query = new Query(startTag, endTag);
		
		try {
			for(int i = 0 ; i < colArray.length ; i++) {
				
				FilterFieldParseResult filterFieldParseResult = parseFilterParams(params);
				query.setSelect(parseNewSelect(params));
	//				if(!RangeFilter.equalsIgnoreCase("")) {
	//					query.setFilter(parseFilter(collection, RangeFilter, RangeKey));
	//				}
				query.setWhere(parseNewWhere(params, filterFieldParseResult, colArray[i], OriginKwd));
				query.setGroupBy(parseNewGroupBy(params));
//				query.setOrderby(parseNewOrderBy(params, colArray[i]));
				query.setFrom(colArray[i]);
				query.setResult(parseStart(params) - 1, parseStart(params) + parseSize(params) - 2);
				query.setSearchKeyword(OriginKwd);
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
				query.setValue("typo-parameters", OriginKwd);
		    	query.setValue("typo-options", "ALPHABETS_TO_HANGUL|HANGUL_TO_ALPHABETS");
		    	query.setValue("typo-correct-result-num", "1");
				
				querySet.addQuery(query);
			
				String queryStr = parser.queryToString(query);
	//				System.out.println(" :::::::::: query ::::::: " + queryStr);
			}
				
			CommandSearchRequest.setProps(Connection.IP, Connection.PORT, 5000, 50, 50);
			CommandSearchRequest commandSearchRequest = new CommandSearchRequest(Connection.IP, Connection.PORT);
					
			int returnCode = commandSearchRequest.request(querySet);
			
			if (returnCode <= -100) {
				ErrorMessageService.getInstance().minusReturnCodeLog(returnCode, commandSearchRequest.getException(), req);
				logMessageService.receiveEnd(reqHeader, request);
				return commandSearchRequestErrorResponse(commandSearchRequest.getException().getErrorMessage());
			} else {
				logMessageService.messageReceived(reqHeader, request);
			}
			
//				System.out.println(returnCode);
//				System.out.println(commandSearchRequest.getResultSet().getResult(0).getTotalSize());
			
			String resultJson = "";
			
			resultJson += gson.toJson(makeTotalResult(commandSearchRequest.getResultSet(), querySet, params));
			
			ret = resultJson;
			
			logMessageService.receiveEnd(reqHeader, request);
			
//				System.out.println(ret);
			
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
	
	// 기존 검색
	public String sayOldSearch(Map<String, String> params, Map<String, Object> reqHeader, HttpServletRequest request) {
		
		String req = "";
		req += "Host: " + (String) reqHeader.get("host") + "\n";
		req += "Connection: " + (String) reqHeader.get("connection") + "\n";
		req += "Upgrade-Insecure-Requests: " + (String) reqHeader.get("upgrade-insecure-requests") + "\n";
		req += "User-Agent: " + (String) reqHeader.get("user-agent") + "\n";
		req += "Accept: " + (String) reqHeader.get("accept") + "\n";
		req += "Accept-Encoding: " + (String) reqHeader.get("accept-encoding") + "\n";
		req += "Accept-Language: " + (String) reqHeader.get("accept-language");
		
		String OriginKwd = "";
		
		if(params.get("q") != null) {
			OriginKwd = parseQ(params);		
		} else {			
			OriginKwd = "";
//			return makeEmptyNhnData(params);
		}
		
		logMessageService.requestReceived(reqHeader, request);
		
		String collection = getCollection(params);
		String ret = "";
		
		String FilterParams = parseFilterParam(params);
		String RangeFilter = "";
		String RangeKey = "";
		
		if(collection.equalsIgnoreCase(SayclubCollections.CHATUSER_OLD)) {				
			if(FilterParams.indexOf("&") > -1) {
				
        		String[] f = FilterParams.split("&");
        		
        		for(int i=0 ; i < f.length ; i++) {
        			String key = f[i].split("=")[0].toUpperCase();
        			String value = f[i].split("=")[1];
        			
        			if(key.equalsIgnoreCase("BYEAR")) {
        				if(value.indexOf("[") > -1) {
        					RangeFilter = value;
        					RangeKey = key;
        				}
        			}
        		}
        		
        	} else if(FilterParams.indexOf("|") > -1) {
				
        		String[] f = FilterParams.split("\\|");
        		
        		for(int i=0 ; i < f.length ; i++) {
        			String key = f[i].split("=")[0].toUpperCase();
        			String value = f[i].split("=")[1];
        			
        			if(key.equalsIgnoreCase("BYEAR")) {
        				if(value.indexOf("[") > -1) {
        					RangeFilter = value;
        					RangeKey = key;
        				}
        			}
        		}
        		
        	} else {
        		if(FilterParams.indexOf("[") > -1) {
        			String key = FilterParams.split("=")[0].toUpperCase();
        			String value = FilterParams.split("=")[1];
        			
        			RangeFilter = value;
					RangeKey = key;
        		}
        	}
		}
		
		Gson gson = new Gson();
		
		QueryParser parser = new QueryParser();
		
		QuerySet querySet = new QuerySet(1);
		Query query = new Query();
			
		try {
			
			FilterFieldParseResult filterFieldParseResult = parseFilterParams(params);
			query.setSelect(parseSelect(params));
			if(!RangeFilter.equalsIgnoreCase("")) {
				query.setFilter(parseFilter(collection, RangeFilter, RangeKey));
			}
			query.setWhere(parseWhere(params, filterFieldParseResult, collection, OriginKwd));
			query.setGroupBy(parseGroupBy(params));
			query.setOrderby(parseOrderBy(params, collection));
			query.setFrom(collection);
			query.setResult(parseStart(params) - 1, parseStart(params) + parseSize(params) - 2);
			query.setSearchKeyword(OriginKwd);
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
			parseTrigger(params, query, collection);
			query.setResultModifier("typo");
			query.setValue("typo-parameters", OriginKwd);
	    	query.setValue("typo-options", "ALPHABETS_TO_HANGUL|HANGUL_TO_ALPHABETS");
	    	query.setValue("typo-correct-result-num", "1");
			
			querySet.addQuery(query);
		
			String queryStr = parser.queryToString(query);
//			System.out.println(" :::::::::: query ::::::: " + queryStr);
			
			CommandSearchRequest.setProps(Connection.IP, Connection.PORT, 5000, 50, 50);
			CommandSearchRequest commandSearchRequest = new CommandSearchRequest(Connection.IP, Connection.PORT);
					
			int returnCode = commandSearchRequest.request(querySet);
			
			if (returnCode <= -100) {
				ErrorMessageService.getInstance().minusReturnCodeLog(returnCode, commandSearchRequest.getException(), req);
				logMessageService.receiveEnd(reqHeader, request);
				return commandSearchRequestErrorResponse(commandSearchRequest.getException().getErrorMessage());
			} else {
				logMessageService.messageReceived(reqHeader, request);
				
				ResultSet resultSet = commandSearchRequest.getResultSet();
    			Result[] resultlist = resultSet.getResultList();
    			Result result1 = resultlist[0];
    			    			
    			int totalSize = 0;
    			String typoKwd = "";
    			
    			totalSize = result1.getTotalSize();
    			    			
    			if (totalSize == 0) {
    				if (result1.getValue("typo-result") != null) {
    					typoKwd = result1.getValue("typo-result");
					}
    				
    				if (!typoKwd.equals("")) {
    					params.put("q", typoKwd);
    					querySet = new QuerySet(1);
    					
						query = new Query();
						    						
						filterFieldParseResult = parseFilterParams(params);
						query.setSelect(parseSelect(params));
						query.setWhere(parseWhere(params, filterFieldParseResult, collection, parseQ(params)));
						query.setGroupBy(parseGroupBy(params));
						query.setOrderby(parseOrderBy(params, getCollection(params)));
						query.setFrom(collection);
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
						parseTrigger(params, query, collection);
						
						querySet.addQuery(query);
					
						queryStr = parser.queryToString(query);
//						System.out.println(" :::::::::: query ::::::: " + queryStr);
    					
    					commandSearchRequest = new CommandSearchRequest(Connection.IP, Connection.PORT);
    							
    					returnCode = commandSearchRequest.request(querySet);
    					
    					if (returnCode <= -100) {
    						ErrorMessageService.getInstance().minusReturnCodeLog(returnCode, commandSearchRequest.getException(), req);
    						logMessageService.receiveEnd(reqHeader, request);
    						return commandSearchRequestErrorResponse(commandSearchRequest.getException().getErrorMessage());
    					} else {
    						logMessageService.messageReceived(reqHeader, request);
    					}
    				}
    			}
    			params.put("q", OriginKwd);
			}
			
//			System.out.println(returnCode);
//			System.out.println(commandSearchRequest.getResultSet().getResult(0).getTotalSize());
			
			String resultJson = "";
			
			if(collection.equalsIgnoreCase(SayclubCollections.SAYMALL_OLD)) {
				resultJson = gson.toJson(makeMallResult(commandSearchRequest.getResultSet().getResult(0), query, params, collection));
			} else {
				resultJson = gson.toJson(makeResult(commandSearchRequest.getResultSet().getResult(0), query, params, collection));
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
			colArray = colArray = new String[] {SayclubCollections.SAYCAST, SayclubCollections.SAYCAST_CJ};
		} else {
			colArray = new String[] {collec};
		}
		
		return colArray;
	}
	
	private FilterFieldParseResult parseFilterParams(Map<String, String> params) {
		return new SayclubFilterValue(params).parseAll();
	}
	
	// 리뉴얼 버전
	protected WhereSet[] parseNewWhere(Map<String, String> params, FilterFieldParseResult filterFieldParseResult, String collection, String OriginKwd) throws InvalidParameterException {
		return SayclubWhereSet.getInstance().makeWhereSet(params, filterFieldParseResult, makeBaseNewWhereSet(params, collection, OriginKwd));
	}
	
	protected List<WhereSet> makeBaseNewWhereSet(Map<String, String> params, String collection, String OriginKwd) throws InvalidParameterException {
		List<WhereSet> result = new ArrayList<WhereSet>();
		String keyword = OriginKwd;
//		String trimKeyword = keyword.replaceAll("\\s", "");
	
		byte searchOption;
		String qOption = parseQoption(params);
		String paramFilter = parseFilterParam(params);
				
		String[] OptValues = {};
 		String Opt = "";
		Map<String, Double> fieldOpt = new HashMap<String, Double>();
		
		if(!qOption.equalsIgnoreCase("")) {
			OptValues = qOption.split(",");
			for(int i=0 ; i < OptValues.length ; i++) {
				if(OptValues[i].equalsIgnoreCase("and") || OptValues[i].equalsIgnoreCase("or") || OptValues[i].equalsIgnoreCase("boolean")) {
					Opt = OptValues[i];
				} else { 					
					if(!OptValues[i].split("\\*").equals("")) {
						fieldOpt.put(OptValues[i].split("\\*")[0], Double.parseDouble(OptValues[i].split("\\*")[1]));
					} else {
						fieldOpt.put(OptValues[i], 0.0);
					}				
				}
			}
		}
		
		if (Opt.equalsIgnoreCase("and")) {
			 searchOption = Protocol.WhereSet.OP_HASALL;
        } else if (Opt.equalsIgnoreCase("or")) {
        	searchOption = Protocol.WhereSet.OP_HASANY;
        } else {
        	searchOption = Protocol.WhereSet.OP_HASALL;
        }
			
		HashMap<String, Integer> sayCastMap = new HashMap<String, Integer>();
		HashMap<String, Integer> sayArticleMap = new HashMap<String, Integer>();
		HashMap<String, Integer> sayMallMap = new HashMap<String, Integer>();
		
		if(collection.equalsIgnoreCase(SayclubCollections.SAYCAST)) {
			
			if(keyword.equalsIgnoreCase("")) {
				result.add(new WhereSet("ALL", searchOption, "A"));
			} else {
				for (Entry<String, Double> field : fieldOpt.entrySet()) {
					String fieldNm = field.getKey().toUpperCase();
					int weight = 0;
					weight = (int) (field.getValue() * 100);
					
					sayCastMap.put(fieldNm, weight);
					sayCastMap.put(fieldNm + "_WS", weight);
				}
				
				for (Entry<String, Integer> e : sayCastMap.entrySet()) {
					if (result.size() > 0) {
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					}
					result.add(new WhereSet(e.getKey(), searchOption, keyword, e.getValue()));
				}
			}
			
		} else if(collection.equalsIgnoreCase(SayclubCollections.SAYCAST_ART)) {
			
			if(keyword.equalsIgnoreCase("")) {
				result.add(new WhereSet("ALL", searchOption, "A"));
			} else {
				for (Entry<String, Double> field : fieldOpt.entrySet()) {
					String fieldNm = field.getKey().toUpperCase();
					int weight = 0;
					weight = (int) (field.getValue() * 100);
					
					sayArticleMap.put(fieldNm, weight);
					sayArticleMap.put(fieldNm + "_WS", weight);
				}
				
				for (Entry<String, Integer> e : sayArticleMap.entrySet()) {
					if (result.size() > 0) {
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					}
					result.add(new WhereSet(e.getKey(), searchOption, keyword, e.getValue()));
				}
			}
			
		} else if(collection.equalsIgnoreCase(SayclubCollections.SAYCAST_AUTO)) {
			
			result.add(new WhereSet("FKEY", Protocol.WhereSet.OP_HASALL, keyword.replaceAll("\\s", ""), 500));
			result.add(new WhereSet(Protocol.WhereSet.OP_OR));
			result.add(new WhereSet("BKEY", Protocol.WhereSet.OP_HASALL, keyword.replaceAll("\\s", ""), 0));
		
		} else if(collection.equalsIgnoreCase(SayclubCollections.SAYCAST_CJ)) {
			
			for (Entry<String, Double> field : fieldOpt.entrySet()) {
				String fieldNm = field.getKey().toUpperCase();
				int weight = 0;
				weight = (int) (field.getValue() * 100);
				
				sayCastMap.put(fieldNm, weight);
				sayCastMap.put(fieldNm + "_WS", weight);
			}
			
			for (Entry<String, Integer> e : sayCastMap.entrySet()) {
				if (result.size() > 0) {
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
				}
				result.add(new WhereSet(e.getKey(), searchOption, keyword, e.getValue()));
			}
			
			if (result.size() > 0) {
				result.add(new WhereSet(Protocol.WhereSet.OP_AND));
				result.add(new WhereSet("STATUS", Protocol.WhereSet.OP_HASALL, "Y"));
			} else {
				result.add(new WhereSet("STATUS", Protocol.WhereSet.OP_HASALL, "Y"));
			}
			
		}
		
		String[] fts;
        
        if(!paramFilter.equalsIgnoreCase("")) {
        	  	
        	if(paramFilter.indexOf("|") > -1) {
        		if (result.size() > 0) {
            		result.add(new WhereSet(Protocol.WhereSet.OP_AND));
    			}
        		
        		result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
        		
        		fts = paramFilter.split("\\|");
        		        		
        		for(int i=0 ; i < fts.length ; i++) {
        			String key = fts[i].split("=")[0].toUpperCase();
        			String value = fts[i].split("=")[1];
        			
        			if(i > 0) {
        				result.add(new WhereSet(Protocol.WhereSet.OP_OR));
        			}
        			result.add(new WhereSet(key, searchOption, value));
        		}
        		result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
        		
        	} else if(paramFilter.indexOf("&") > -1) {
        		if (result.size() > 0) {
            		result.add(new WhereSet(Protocol.WhereSet.OP_AND));
    			}
        		
        		result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
        		
        		fts = paramFilter.split("&");
        		int k = 0;
        		
        		for(int i=0 ; i < fts.length ; i++) {
        			String key = fts[i].split("=")[0].toUpperCase();
        			String value = fts[i].split("=")[1];
        			        			
        			if(value.indexOf("[") == -1) {
        				if(k > 0) {
            				result.add(new WhereSet(Protocol.WhereSet.OP_AND));
            			}
        				
        				result.add(new WhereSet(key, searchOption, value));
        				k++;
        			} 
        		}
        		result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
        		
        	} else {
        		fts = paramFilter.split("=");
        		
    			String key = fts[0].toUpperCase();
    			String value = fts[1];
    			
    			if(value.indexOf("[") == -1) {
    				if (result.size() > 0) {
    	        		result.add(new WhereSet(Protocol.WhereSet.OP_AND));
    				}
    				result.add(new WhereSet(key, searchOption, value));
    			}
    			
        	}
        } 
			
		return result;
	}
	
	// 기존 버전
	protected WhereSet[] parseWhere(Map<String, String> params, FilterFieldParseResult filterFieldParseResult, String collection, String OriginKwd) throws InvalidParameterException {
		return SayclubWhereSet.getInstance().makeWhereSet(params, filterFieldParseResult, makeBaseWhereSet(params, collection, OriginKwd));
	}
	
	protected List<WhereSet> makeBaseWhereSet(Map<String, String> params, String collection, String OriginKwd) throws InvalidParameterException {
		List<WhereSet> result = new ArrayList<WhereSet>();
		String keyword = OriginKwd;
//		String trimKeyword = keyword.replaceAll("\\s", "");
	
		byte searchOption;
		String qOption = parseQoption(params);
		String paramFilter = parseFilterParam(params);
				
		String[] OptValues = {};
 		String Opt = "";
		Map<String, Double> fieldOpt = new HashMap<String, Double>();
		
		if(!qOption.equalsIgnoreCase("")) {
			OptValues = qOption.split(",");
			for(int i=0 ; i < OptValues.length ; i++) {
				if(OptValues[i].equalsIgnoreCase("and") || OptValues[i].equalsIgnoreCase("or") || OptValues[i].equalsIgnoreCase("boolean")) {
					Opt = OptValues[i];
				} else { 					
					if(!OptValues[i].split("\\*").equals("")) {
						fieldOpt.put(OptValues[i].split("\\*")[0], Double.parseDouble(OptValues[i].split("\\*")[1]));
					} else {
						fieldOpt.put(OptValues[i], 0.0);
					}				
				}
			}
		}
		
		if (Opt.equalsIgnoreCase("and")) {
			 searchOption = Protocol.WhereSet.OP_HASALL;
        } else if (Opt.equalsIgnoreCase("or")) {
        	searchOption = Protocol.WhereSet.OP_HASANY;
        } else {
        	searchOption = Protocol.WhereSet.OP_HASALL;
        }
			
		HashMap<String, Integer> sayCastMap = new HashMap<String, Integer>();
		HashMap<String, Integer> sayArticleMap = new HashMap<String, Integer>();
		HashMap<String, Integer> sayMallMap = new HashMap<String, Integer>();
		
		if(collection.equalsIgnoreCase(SayclubCollections.SAYCAST_OLD)) {
			
			if(keyword.equalsIgnoreCase("")) {
				result.add(new WhereSet("ALL", searchOption, "A"));
			} else {
				for (Entry<String, Double> field : fieldOpt.entrySet()) {
					String fieldNm = field.getKey().toUpperCase();
					int weight = 0;
					weight = (int) (field.getValue() * 100);
					
					sayCastMap.put(fieldNm, weight);
					sayCastMap.put(fieldNm + "_WS", weight);
				}
				
				for (Entry<String, Integer> e : sayCastMap.entrySet()) {
					if (result.size() > 0) {
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					}
					result.add(new WhereSet(e.getKey(), searchOption, keyword, e.getValue()));
				}
			}
			
		} else if(collection.equalsIgnoreCase(SayclubCollections.SAYCAST_ART_OLD)) {
			
			if(keyword.equalsIgnoreCase("")) {
				result.add(new WhereSet("ALL", searchOption, "A"));
			} else {
				for (Entry<String, Double> field : fieldOpt.entrySet()) {
					String fieldNm = field.getKey().toUpperCase();
					int weight = 0;
					weight = (int) (field.getValue() * 100);
					
					sayArticleMap.put(fieldNm, weight);
					sayArticleMap.put(fieldNm + "_WS", weight);
				}
				
				for (Entry<String, Integer> e : sayArticleMap.entrySet()) {
					if (result.size() > 0) {
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					}
					result.add(new WhereSet(e.getKey(), searchOption, keyword, e.getValue()));
				}
			}
			
		} else if(collection.equalsIgnoreCase(SayclubCollections.SAYMALL_OLD)) {
			for (Entry<String, Double> field : fieldOpt.entrySet()) {
				String fieldNm = field.getKey().toUpperCase();
				int weight = 0;
				weight = (int) (field.getValue() * 100);
				
				sayMallMap.put(fieldNm, weight);
				sayMallMap.put(fieldNm + "_WS", weight);
			}
						
			for (Entry<String, Integer> e : sayMallMap.entrySet()) {
				if (result.size() > 0) {
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
				}
				result.add(new WhereSet(e.getKey(), searchOption, keyword, e.getValue()));
			}
		} else if(collection.equalsIgnoreCase(SayclubCollections.ALLUSER_OLD)) {
			result.add(new WhereSet("ALL", searchOption, "A"));
			
		} else if(collection.equalsIgnoreCase(SayclubCollections.CHATUSER_OLD)) {
			result.add(new WhereSet("ALL", searchOption, "A"));
		}
		
		String[] fts;
        
        if(!paramFilter.equalsIgnoreCase("")) {
        	  	
        	if(paramFilter.indexOf("|") > -1) {
        		if (result.size() > 0) {
            		result.add(new WhereSet(Protocol.WhereSet.OP_AND));
    			}
        		
        		result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
        		
        		fts = paramFilter.split("\\|");
        		        		
        		for(int i=0 ; i < fts.length ; i++) {
        			String key = fts[i].split("=")[0].toUpperCase();
        			String value = fts[i].split("=")[1];
        			
        			if(i > 0) {
        				result.add(new WhereSet(Protocol.WhereSet.OP_OR));
        			}
        			result.add(new WhereSet(key, searchOption, value));
        		}
        		result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
        		
        	} else if(paramFilter.indexOf("&") > -1) {
        		if (result.size() > 0) {
            		result.add(new WhereSet(Protocol.WhereSet.OP_AND));
    			}
        		
        		result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
        		
        		fts = paramFilter.split("&");
        		int k = 0;
        		
        		for(int i=0 ; i < fts.length ; i++) {
        			String key = fts[i].split("=")[0].toUpperCase();
        			String value = fts[i].split("=")[1];
        			        			
        			if(value.indexOf("[") == -1) {
        				if(k > 0) {
            				result.add(new WhereSet(Protocol.WhereSet.OP_AND));
            			}
        				
        				result.add(new WhereSet(key, searchOption, value));
        				k++;
        			} 
        		}
        		result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
        		
        	} else {
        		fts = paramFilter.split("=");
        		
    			String key = fts[0].toUpperCase();
    			String value = fts[1];
    			
    			if(value.indexOf("[") == -1) {
    				if (result.size() > 0) {
    	        		result.add(new WhereSet(Protocol.WhereSet.OP_AND));
    				}
    				result.add(new WhereSet(key, searchOption, value));
    			}
    			
        	}
        } 
			
		return result;
	}
	
	// 리뉴얼 select
	private SelectSet[] parseNewSelect(Map<String, String> params) {
		return SayclubNewSelectSet.getInstance().makeSelectSet(params);
	}
	
	// 기존 select
	private SelectSet[] parseSelect(Map<String, String> params) {
		return SayclubSelectSet.getInstance().makeSelectSet(params);
	}
	
	// 리뉴얼 groupby
	public GroupBySet[] parseNewGroupBy(Map<String, String> params) {
//		String catGroup = RestUtils.getParam(params, "group");
		String cat2Group = RestUtils.getParam(params, "summary.CAT2_ID");
		
		List<GroupBySet> result = new ArrayList<GroupBySet>();
		
		if (!cat2Group.equals("")) {
			result.add(new GroupBySet("CAT2_ID", (byte) (Protocol.GroupBySet.OP_COUNT | Protocol.GroupBySet.ORDER_COUNT), "DESC"));
		} else {
			return new GroupBySet[0];
		}
		
//		for (String g : catGroup.split(",")) {
//			result.add(new GroupBySet(g.toUpperCase(), (byte) (Protocol.GroupBySet.OP_COUNT | Protocol.GroupBySet.ORDER_COUNT), "DESC"));
//		}
		
		return result.toArray(new GroupBySet[result.size()]);
	}
	
	// 기존 groupby
	public GroupBySet[] parseGroupBy(Map<String, String> params) {
//		String catGroup = RestUtils.getParam(params, "group");
		String cat2Group = RestUtils.getParam(params, "summary.CAT2_ID");
		
		List<GroupBySet> result = new ArrayList<GroupBySet>();
		
		if (!cat2Group.equals("")) {
			result.add(new GroupBySet("CAT2_ID", (byte) (Protocol.GroupBySet.OP_COUNT | Protocol.GroupBySet.ORDER_COUNT), "DESC"));
		} else {
			return new GroupBySet[0];
		}
		
//		for (String g : catGroup.split(",")) {
//			result.add(new GroupBySet(g.toUpperCase(), (byte) (Protocol.GroupBySet.OP_COUNT | Protocol.GroupBySet.ORDER_COUNT), "DESC"));
//		}
		
		return result.toArray(new GroupBySet[result.size()]);
	}
	
	// 리뉴얼 orderby
	protected OrderBySet[] parseNewOrderBy(Map<String, String> params, String collection) {
		return new OrderBySet[] { SayclubOrderBySet.getInstance().getOrderBySet(RestUtils.getParam(params, "sort"), collection) };
	}
	
	// 기존 orderby
	protected OrderBySet[] parseOrderBy(Map<String, String> params, String collection) {
		return new OrderBySet[] { SayclubOrderBySet.getInstance().getOrderBySet(RestUtils.getParam(params, "sort"), collection) };
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
	
	protected FilterSet[] parseFilter(String collection, String RangeFilter, String RangeKey) throws InvalidParameterException {
		return SayclubFilterSetService.getInstance().parseFilter(collection,RangeFilter,RangeKey);
	}
	
	private void parseTrigger(Map<String, String> req, Query query, String collection) throws InvalidParameterException {
		TriggerFieldService.getInstance().parseTrigger(req, query, collection);
	}
	
	protected SayclubResult makeResult(Result result, Query query, Map<String, String> params, String collection) throws IRException {
		return SayclubResult.makeSayclubResult(query, result, params, collection);
	}
	
	protected SayMallResult makeMallResult(Result result, Query query, Map<String, String> params, String collection) throws IRException {
		return SayMallResult.makeSayMallResult(query, result, params, collection);
	}
	
	protected SayclubNewResult makeNewResult(Result result, Query query, Map<String, String> params, String collection) throws IRException {
		return SayclubNewResult.makeSayclubResult(query, result, params, collection);
	}
	
	protected SayclubTotalResult makeTotalResult(ResultSet result, QuerySet query, Map<String, String> params) throws IRException {
		return SayclubTotalResult.makeTotalResult(query, result, params);
	}
	
	protected SayclubAutoResult makeAutoResult(Result result, Query query, Map<String, String> params) throws IRException {
		return SayclubAutoResult.makeAutoTagResult(query, result, params);
	}
	
	protected String parseQ(Map<String, String> params) {
		return RestUtils.getParam(params, "q");
	}
	
	protected String parseQoption(Map<String, String> params) {
		return RestUtils.getParam(params, "q_option");
	}
	
	protected String parseFilterParam(Map<String, String> params) {
		return RestUtils.getParam(params, "filter");
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
