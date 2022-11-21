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
import com.diquest.mapper.domain.FieldSelector;
import com.diquest.mapper.domain.artistSelector;
import com.diquest.rest.nhn.common.Collections;
import com.diquest.rest.nhn.common.Connection;
import com.diquest.rest.nhn.filter.parse.FilterValueParser;
import com.diquest.rest.nhn.filter.result.FilterFieldParseResult;
import com.diquest.rest.nhn.result.AutoResult;
import com.diquest.rest.nhn.result.AutoTotalResult;
import com.diquest.rest.nhn.result.NhnError;
import com.diquest.rest.nhn.result.NhnResult;
import com.diquest.rest.nhn.result.SayclubResult;
import com.diquest.rest.nhn.service.error.ErrorMessageService;
import com.diquest.rest.nhn.service.error.logMessageService;
import com.diquest.rest.nhn.service.filter.EntityFilterSetService;
import com.diquest.rest.nhn.service.filter.FilterSetService;
import com.diquest.rest.nhn.service.filter.TotalFilterSetService;
import com.diquest.rest.nhn.service.option.searchQoption;
import com.diquest.rest.nhn.service.orderby.OrderBySetService;
import com.diquest.rest.nhn.service.select.AutoTagSelectSet;
import com.diquest.rest.nhn.service.select.AutoTotalSelectSet;
import com.diquest.rest.nhn.service.select.SelectSetService;
import com.diquest.rest.nhn.service.trigger.TriggerFieldService;
import com.diquest.rest.nhn.service.where.WhereSetService;
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
//	protected static String a_keyword = "";
	
	public SayclubRestService() {
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
	
	public String saySearch(Map<String, String> params, Map<String, Object> reqHeader, HttpServletRequest request) {
		
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
		String OriginKwd = parseQ(params);
		
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
			query.setResultModifier("typo");
			query.setValue("typo-parameters", OriginKwd);
	    	query.setValue("typo-options", "ALPHABETS_TO_HANGUL|HANGUL_TO_HANGUL");
	    	query.setValue("typo-correct-result-num", "1");
			
			querySet.addQuery(query);
		
			String queryStr = parser.queryToString(query);
//			System.out.println(" :::::::::: query ::::::: " + queryStr);
			
			CommandSearchRequest.setProps(Connection.IP, Connection.PORT, 10000, 50, 50);
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
						query.setFilter(parseFilter(params, filterFieldParseResult, getCollection(params)));
						query.setWhere(parseWhere(params, filterFieldParseResult, getCollection(params)));
//						query.setGroupBy(parseGroupBy(params));
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
						
						querySet.addQuery(query);
					
						queryStr = parser.queryToString(query);
//						System.out.println(" :::::::::: query22 ::::::: " + queryStr);
    					
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
	
//	// 자동완성 API 
//	public String Autosearch(Map<String, String> params, Map<String, Object> reqHeader, HttpServletRequest request) {
//
//		String req = "";
//		req += "Host: " + (String) reqHeader.get("host") + "\n";
//		req += "Connection: " + (String) reqHeader.get("connection") + "\n";
//		req += "Upgrade-Insecure-Requests: " + (String) reqHeader.get("upgrade-insecure-requests") + "\n";
//		req += "User-Agent: " + (String) reqHeader.get("user-agent") + "\n";
//		req += "Accept: " + (String) reqHeader.get("accept") + "\n";
//		req += "Accept-Encoding: " + (String) reqHeader.get("accept-encoding") + "\n";
//		req += "Accept-Language: " + (String) reqHeader.get("accept-language");
//		
//		if(params.get("q") != null) {
//			if(params.get("q").isEmpty()){
//				return makeEmptyNhnData(params);
//			}
//		} else {			
//			return makeEmptyNhnData(params);
//		}
//		
//		logMessageService.requestReceived(reqHeader, request);
//		
//		String ret = "";
//		String OriginKwd = parseQ(params);
//		
//		Gson gson = new Gson();
//		
//		QueryParser parser = new QueryParser();
//		
//		String colStr = params.get("collection");
//		int queryInt = 1;
//			
//		if(colStr.equalsIgnoreCase(Collections.AUTO_TOTAL)) {
//			queryInt = 2; 
//		} else {
//			queryInt = 1;
//		}		
//		
//		QuerySet querySet = new QuerySet(queryInt);
//		Query query = new Query();
//		
//		try {
//			for(int i = 0 ; i < queryInt ; i++) {
//				query = new Query();
//				
//				FilterFieldParseResult filterFieldParseResult = parseFilterParams(params);
//				query.setSelect(parseAutoSelect(params, getCollection(params), i));
////				query.setFilter(parseFilter(params, filterFieldParseResult, getCollection(params)));
//				query.setWhere(parseAutoWhere(params, filterFieldParseResult, getCollection(params), i));
//	//			query.setGroupBy(parseGroupBy(params));
//				query.setOrderby(parseOrderBy(params, getCollection(params)));
//				query.setFrom(getCollection(params));
//				query.setResult(parseStart(params) - 1, parseStart(params) + parseAutoSize(params, i) - 2);
//				query.setSearchKeyword(parseQ(params));
//				query.setFaultless(true);
//				query.setThesaurusOption((byte) (Protocol.ThesaurusOption.EQUIV_SYNONYM | Protocol.ThesaurusOption.QUASI_SYNONYM));
//				query.setSearchOption((byte) (Protocol.SearchOption.BANNED | Protocol.SearchOption.STOPWORD | Protocol.SearchOption.CACHE));
//				query.setRankingOption((byte) (Protocol.RankingOption.CATEGORY_RANKING | Protocol.RankingOption.DOCUMENT_RANKING));
//				query.setCategoryRankingOption((byte) (Protocol.CategoryRankingOption.EQUIV_SYNONYM | Protocol.CategoryRankingOption.QUASI_SYNONYM));	
//				query.setLoggable(false);
//				query.setPrintQuery(true);						// 실제 사용시 false
//				parseTrigger(params, query, getCollection(params));
//				query.setResultModifier("typo");
//				query.setValue("typo-parameters", OriginKwd);
//		    	query.setValue("typo-options", "ALPHABETS_TO_HANGUL|HANGUL_TO_HANGUL");
//		    	query.setValue("typo-correct-result-num", "1");
//				
//				querySet.addQuery(query);
//			
//				String queryStr = parser.queryToString(query);
////				System.out.println(" :::::::::: query ::::::: " + queryStr);
//			}
//								
//			CommandSearchRequest.setProps(Connection.IP, Connection.PORT, 10000, 50, 50);
//			CommandSearchRequest commandSearchRequest = new CommandSearchRequest(Connection.IP, Connection.PORT);
//					
//			int returnCode = commandSearchRequest.request(querySet);
//			
//			if (returnCode <= -100) {
//				ErrorMessageService.getInstance().minusReturnCodeLog(returnCode, commandSearchRequest.getException(), req);
//				logMessageService.receiveEnd(reqHeader, request);
//				return commandSearchRequestErrorResponse(commandSearchRequest.getException().getErrorMessage());
//			} else {
//				logMessageService.messageReceived(reqHeader, request);
//				
//				if(colStr.equalsIgnoreCase(Collections.AUTO_TOTAL)) {
//					ResultSet resultSet = commandSearchRequest.getResultSet();
//	    			Result[] resultlist = resultSet.getResultList();
//	    			Result result1 = resultlist[0];
//	    			Result result2 = resultlist[1];
//	    			    			
//	    			int totalSize = 0;
//	    			String typoKwd = "";
//	    			
//	    			totalSize = result1.getTotalSize() + result2.getTotalSize();
//	    			    			
//	    			if (totalSize == 0) {
//	    				if (result1.getValue("typo-result") != null) {
//	    					typoKwd = result1.getValue("typo-result");
//						}
//	    				
//	    				if (!typoKwd.equals("")) {
//	    					params.put("q", typoKwd);
//	    					querySet = new QuerySet(queryInt);
//	    					
//	    					for(int j = 0 ; j < queryInt ; j++) {
//	    						query = new Query();
//	    						    						
//	    						FilterFieldParseResult filterFieldParseResult = parseFilterParams(params);
//	    						query.setSelect(parseAutoSelect(params, getCollection(params), j));
//	    						query.setWhere(parseAutoWhere(params, filterFieldParseResult, getCollection(params), j));
//	    						query.setOrderby(parseOrderBy(params, getCollection(params)));
//	    						query.setFrom(getCollection(params));
//	    						query.setResult(parseStart(params) - 1, parseStart(params) + parseAutoSize(params, j) - 2);
//	    						query.setSearchKeyword(parseQ(params));
//	    						query.setFaultless(true);
//	    						query.setThesaurusOption((byte) (Protocol.ThesaurusOption.EQUIV_SYNONYM | Protocol.ThesaurusOption.QUASI_SYNONYM));
//	    						query.setSearchOption((byte) (Protocol.SearchOption.BANNED | Protocol.SearchOption.STOPWORD | Protocol.SearchOption.CACHE));
//	    						query.setRankingOption((byte) (Protocol.RankingOption.CATEGORY_RANKING | Protocol.RankingOption.DOCUMENT_RANKING));
//	    						query.setCategoryRankingOption((byte) (Protocol.CategoryRankingOption.EQUIV_SYNONYM | Protocol.CategoryRankingOption.QUASI_SYNONYM));	
//	    						query.setLoggable(false);
//	    						query.setPrintQuery(true);						// 실제 사용시 false
//	    						parseTrigger(params, query, getCollection(params));
//	    						
//	    						querySet.addQuery(query);
//	    					}
//	    											
//	    					commandSearchRequest = new CommandSearchRequest(Connection.IP, Connection.PORT);
//	    							
//	    					returnCode = commandSearchRequest.request(querySet);
//	    					
//	    					if (returnCode <= -100) {
//	    						ErrorMessageService.getInstance().minusReturnCodeLog(returnCode, commandSearchRequest.getException(), req);
//	    						logMessageService.receiveEnd(reqHeader, request);
//	    						return commandSearchRequestErrorResponse(commandSearchRequest.getException().getErrorMessage());
//	    					} else {
//	    						logMessageService.messageReceived(reqHeader, request);
//	    					}
//	    				}
//	    			}
//	    			params.put("q", OriginKwd);
//				}
//			}
//					
//			String resultJson = "";
//
//			if(colStr.equalsIgnoreCase(Collections.AUTO_TOTAL)) {
////				resultJson = gson.toJson(makeAutoResult(commandSearchRequest.getResultSet(), querySet, params));
//				JsonObject jsonElement = gson.toJsonTree(makeAutoResult(commandSearchRequest.getResultSet(), querySet, params)).getAsJsonObject();
//				resultJson = gson.toJson(jsonElement.getAsJsonObject("meta").get("result"));
//			} else {
////				resultJson = gson.toJson(makeAutoTagResult(commandSearchRequest.getResultSet().getResult(0), query, params));
//				JsonObject jsonElement = gson.toJsonTree(makeAutoTagResult(commandSearchRequest.getResultSet().getResult(0), query, params)).getAsJsonObject();
//				resultJson = gson.toJson(jsonElement.get("result"));
//			}
//			
//			ret = resultJson;
//			
//			logMessageService.receiveEnd(reqHeader, request);
//			
////			System.out.println(ret);
//			
//		} catch (InvalidParameterException e) {
//			ErrorMessageService.getInstance().invalidParameterLog(req, e);
//			logMessageService.receiveEnd(reqHeader, request);
//			return invalidParameterResponse(e);
//		} catch (Exception e) {
//			ErrorMessageService.getInstance().InternalServerErrorLog(req, e);
//			logMessageService.receiveEnd(reqHeader, request);
//			return internalServerResponse(e);
//		}
//		
//		return ret;
//		
//	}
	
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
		
	private SelectSet[] parseAutoSelect(Map<String, String> params, String collection, int num) {
		if(collection.equalsIgnoreCase(Collections.AUTO_TOTAL)) {
			return AutoTotalSelectSet.getInstance().makeSelectSet(params, num);
		} else {
			return AutoTagSelectSet.getInstance().makeSelectSet(params);
		}		
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
		String trimKeyword = keyword.replaceAll("\\s", "");
		
//		String collection = getCollection(params);
		searchQoption qOption = new searchQoption(RestUtils.getParam(params, "q_option"), collection);
		
		String idxField = qOption.getIndexField();
				
		HashMap<String, Integer> trackMap = new HashMap<String, Integer>();
		HashMap<String, Integer> lyricsMap = new HashMap<String, Integer>();
		HashMap<String, Integer> albumMap = new HashMap<String, Integer>();
		HashMap<String, Integer> artistMap = new HashMap<String, Integer>();
		HashMap<String, Integer> mvMap = new HashMap<String, Integer>();
		HashMap<String, Integer> musicpdMap = new HashMap<String, Integer>();
		HashMap<String, Integer> musicpostMap = new HashMap<String, Integer>();
		HashMap<String, Integer> musiccastMap = new HashMap<String, Integer>();
		HashMap<String, Integer> classicMap = new HashMap<String, Integer>();
		HashMap<String, Integer> entityMap = new HashMap<String, Integer>();
		
		if(collection.equalsIgnoreCase(Collections.TRACK)) {
			if(idxField.equalsIgnoreCase("track_idx")) {
				if(qOption.isNofM()) {
					result.add(new WhereSet("TRACK_IDX_WS", qOption.getOption(), trimKeyword, 100, qOption.getNofmPercent()));
				} else {
					result.add(new WhereSet("TRACK_IDX_WS", qOption.getOption(), trimKeyword, 100));
				}
				trackMap.put("TRACK_IDX", 100);
				trackMap.put("TRACK_IDX_WS", 100);
				trackMap.put("SYN_TRACK_IDX_KO", 30);
				trackMap.put("SYN_TRACK_IDX_WS", 30);
				trackMap.put("SYN_TRACK_IDX", 30);
			} else if(idxField.equalsIgnoreCase("artist_idx")) {
				trackMap.put("ARTIST_IDX", 100);
				trackMap.put("ARTIST_IDX_WS", 100);
				trackMap.put("SYN_ARTIST_IDX_WS", 30);
				trackMap.put("SYN_ARTIST_IDX", 30);
			} else if(idxField.equalsIgnoreCase("album_idx")) {
				trackMap.put("ALBUM_IDX", 100);
				trackMap.put("ALBUM_IDX_WS", 100);
				trackMap.put("SYN_ALBUM_IDX", 30);
				trackMap.put("SYN_ALBUM_IDX_KO", 30);
				trackMap.put("SYN_ALBUM_IDX_WS", 30);
			} else {
				if(!artist_keyword.equalsIgnoreCase("")) {
					if(qOption.isNofM()) {
						result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
						result.add(new WhereSet("TRACK_IDX", qOption.getOption(), keyword, track_score, qOption.getNofmPercent()));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("TRACK_IDX_WS", qOption.getOption(), keyword, track_score, qOption.getNofmPercent()));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("TRACK_IDX_WS", qOption.getOption(), trimKeyword, track_score, qOption.getNofmPercent()));
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
						result.add(new WhereSet("TRACK_IDX_WS", qOption.getOption(), trimKeyword, track_score));
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
						if(qOption.isNofM()) {
							result.add(new WhereSet("TRACK_IDX_WS", qOption.getOption(), trimKeyword, 100, qOption.getNofmPercent()));
						} else {
							result.add(new WhereSet("TRACK_IDX_WS", qOption.getOption(), trimKeyword, 100));
						}
						trackMap.put("TRACK_IDX", track_score);
						trackMap.put("TRACK_IDX_WS", track_score);
						trackMap.put("ARTIST_IDX", artist_score);
						trackMap.put("ARTIST_IDX_WS", artist_score);
						trackMap.put("ALBUM_IDX", album_score);
						trackMap.put("ALBUM_IDX_WS", album_score);
					}
					trackMap.put("TRACK_ARTIST_ALBUM_IDX", 30);
					trackMap.put("TRACK_ARTIST_ALBUM_IDX_WS", 30);
					trackMap.put("SYN_TRACK_ARTIST_ALBUM_IDX", 30);
				}
			}
			
			for (Entry<String, Integer> e : trackMap.entrySet()) {
				if (result.size() > 0) {
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
				}
				if (qOption.isNofM()) {
					result.add(new WhereSet(e.getKey(), qOption.getOption(), keyword, e.getValue(), qOption.getNofmPercent()));
				} else {
					result.add(new WhereSet(e.getKey(), qOption.getOption(), keyword, e.getValue()));
				}
			}
		} else if(collection.equalsIgnoreCase(Collections.LYRICS)) {
				lyricsMap.put("TRACK_IDX", 100);
				lyricsMap.put("TRACK_IDX_WS", 100);
				lyricsMap.put("ARTIST_IDX", 300);
				lyricsMap.put("ARTIST_IDX_WS", 300);
				lyricsMap.put("LYRICS_IDX", 300);
				lyricsMap.put("LYRICS_IDX_WS", 300);
				lyricsMap.put("TOTAL_LYRICS_IDX", 30);
				lyricsMap.put("TOTAL_LYRICS_IDX_WS", 30);
				
				for (Entry<String, Integer> e : lyricsMap.entrySet()) {
					if (result.size() > 0) {
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					}
					if (qOption.isNofM()) {
						result.add(new WhereSet(e.getKey(), qOption.getOption(), keyword, e.getValue(), qOption.getNofmPercent()));
					} else {
						result.add(new WhereSet(e.getKey(), qOption.getOption(), keyword, e.getValue()));
					}
				}
		} else if(collection.equalsIgnoreCase(Collections.ALBUM)) {
			if(idxField.equalsIgnoreCase("album_idx")) {
				albumMap.put("ALBUM_IDX", 100);
				albumMap.put("ALBUM_IDX_WS", 100);
				albumMap.put("SYN_ALBUM_IDX", 30);
				albumMap.put("SYN_ALBUM_IDX_KO", 30);
				albumMap.put("SYN_ALBUM_IDX_WS", 30);
			} else if(idxField.equalsIgnoreCase("artist_idx")) {
				albumMap.put("ARTIST_IDX", 100);
				albumMap.put("ARTIST_IDX_WS", 100);
				albumMap.put("SYN_ARTIST_IDX", 30);
				albumMap.put("SYN_ARTIST_IDX_WS", 30);
			} else {
				albumMap.put("ALBUM_IDX", 100);
				albumMap.put("ALBUM_IDX_WS", 100);
				albumMap.put("ARTIST_IDX", 300);
				albumMap.put("ARTIST_IDX_WS", 300);
				albumMap.put("ARTIST_ALBUM_IDX", 30);
				albumMap.put("ARTIST_ALBUM_IDX_WS", 30);
				albumMap.put("SYN_ARTIST_ALBUM_IDX", 30);
			}
			
			for (Entry<String, Integer> e : albumMap.entrySet()) {
				if (result.size() > 0) {
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
				}
				if (qOption.isNofM()) {
					result.add(new WhereSet(e.getKey(), qOption.getOption(), keyword, e.getValue(), qOption.getNofmPercent()));
				} else {
					result.add(new WhereSet(e.getKey(), qOption.getOption(), keyword, e.getValue()));
				}
			}
		} else if(collection.equalsIgnoreCase(Collections.ARTIST)) {
				artistMap.put("ARTIST_IDX", 200);
				artistMap.put("ARTIST_IDX_WS", 200);
				artistMap.put("GRP_NM_IDX", 100);
				artistMap.put("GRP_NM_IDX_WS", 100);
				artistMap.put("SYN_ARTIST_IDX_KO", 10);
				artistMap.put("SYN_ARTIST_IDX", 10);
				
				for (Entry<String, Integer> e : artistMap.entrySet()) {
					if (result.size() > 0) {
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					}
					if (qOption.isNofM()) {
						result.add(new WhereSet(e.getKey(), qOption.getOption(), keyword, e.getValue(), qOption.getNofmPercent()));
					} else {
						result.add(new WhereSet(e.getKey(), qOption.getOption(), keyword, e.getValue()));
					}
				}
		} else if(collection.equalsIgnoreCase(Collections.MV)) {
			mvMap.put("MV_TRACK_IDX", 100);
			mvMap.put("MV_TRACK_IDX_WS", 100);
			mvMap.put("ARTIST_IDX", 300);
			mvMap.put("MV_TRACK_ARTIST_ALBUM_IDX", 30);
			mvMap.put("SYN_MV_TRACK_ARTIST_ALBUM_IDX", 30);
			mvMap.put("MV_TRACK_ARTIST_ALBUM_IDX_WS", 30);
			
			for (Entry<String, Integer> e : mvMap.entrySet()) {
				if (result.size() > 0) {
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
				}
				if (qOption.isNofM()) {
					result.add(new WhereSet(e.getKey(), qOption.getOption(), keyword, e.getValue(), qOption.getNofmPercent()));
				} else {
					result.add(new WhereSet(e.getKey(), qOption.getOption(), keyword, e.getValue()));
				}
			}
		} else if(collection.equalsIgnoreCase(Collections.MUSICCAST)) {
			musiccastMap.put("MUSICCAST_IDX", 100);
			musiccastMap.put("MUSICCAST_IDX_WS", 100);
			
			for (Entry<String, Integer> e : musiccastMap.entrySet()) {
				if (result.size() > 0) {
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
				}
				if (qOption.isNofM()) {
					result.add(new WhereSet(e.getKey(), qOption.getOption(), keyword, e.getValue(), qOption.getNofmPercent()));
				} else {
					result.add(new WhereSet(e.getKey(), qOption.getOption(), keyword, e.getValue()));
				}
			}
		} else if(collection.equalsIgnoreCase(Collections.MUSICPD)) {
			if(idxField.equalsIgnoreCase("musicpd_album_idx")) {
				musicpdMap.put("MUSICPD_ALBUM_IDX", 100);
				musicpdMap.put("MUSICPD_ALBUM_IDX_WS", 100);
			} else {
				musicpdMap.put("MUSICPD_ALBUM_IDX", 100);
				musicpdMap.put("MUSICPD_ALBUM_IDX_WS", 100);
			}
			
			for (Entry<String, Integer> e : musicpdMap.entrySet()) {
				if (result.size() > 0) {
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
				}
				if (qOption.isNofM()) {
					result.add(new WhereSet(e.getKey(), qOption.getOption(), keyword, e.getValue(), qOption.getNofmPercent()));
				} else {
					result.add(new WhereSet(e.getKey(), qOption.getOption(), keyword, e.getValue()));
				}
			}
		} else if(collection.equalsIgnoreCase(Collections.MUSICPOST)) {
			musicpostMap.put("MUSICPOST_IDX", 100);
			musicpostMap.put("MUSICPOST_IDX_WS", 100);
			
			for (Entry<String, Integer> e : musicpostMap.entrySet()) {
				if (result.size() > 0) {
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
				}
				if (qOption.isNofM()) {
					result.add(new WhereSet(e.getKey(), qOption.getOption(), keyword, e.getValue(), qOption.getNofmPercent()));
				} else {
					result.add(new WhereSet(e.getKey(), qOption.getOption(), keyword, e.getValue()));
				}
			}
		} else if(collection.equalsIgnoreCase(Collections.CLASSIC)) {
			String classic_kwd = keyword.replaceAll("\\s", "");
			
			if(qOption.isNofM()) {
				result.add(new WhereSet("ARTIST_IDX", qOption.getOption(), classic_kwd, 100, qOption.getNofmPercent()));
				result.add(new WhereSet(Protocol.WhereSet.OP_OR));
				result.add(new WhereSet("ARTIST_IDX", qOption.getOption(), keyword, 100, qOption.getNofmPercent()));
				result.add(new WhereSet(Protocol.WhereSet.OP_OR));
				result.add(new WhereSet("TITLE_IDX", qOption.getOption(), keyword, 50, qOption.getNofmPercent()));
				result.add(new WhereSet(Protocol.WhereSet.OP_OR));
				result.add(new WhereSet("TITLE_IDX", qOption.getOption(), classic_kwd, 50, qOption.getNofmPercent()));
			} else {
				result.add(new WhereSet("ARTIST_IDX", qOption.getOption(), classic_kwd, 100));
				result.add(new WhereSet(Protocol.WhereSet.OP_OR));
				result.add(new WhereSet("ARTIST_IDX", qOption.getOption(), keyword, 100));
				result.add(new WhereSet(Protocol.WhereSet.OP_OR));
				result.add(new WhereSet("TITLE_IDX", qOption.getOption(), keyword, 50));
				result.add(new WhereSet(Protocol.WhereSet.OP_OR));
				result.add(new WhereSet("TITLE_IDX", qOption.getOption(), classic_kwd, 50));
			}
			classicMap.put("CLASSIC_IDX", 30);
			classicMap.put("CLASSIC_IDX_KOR", 30);
			
			for (Entry<String, Integer> e : classicMap.entrySet()) {
				if (result.size() > 0) {
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
				}
				if (qOption.isNofM()) {
					result.add(new WhereSet(e.getKey(), qOption.getOption(), keyword, e.getValue(), qOption.getNofmPercent()));
				} else {
					result.add(new WhereSet(e.getKey(), qOption.getOption(), keyword, e.getValue()));
				}
			}
		} else if(collection.equalsIgnoreCase(Collections.ENTITY)) {
			if(idxField.equalsIgnoreCase("track_idx")) {
				entityMap.put("TRACK_IDX", 100);
			} else if(idxField.equalsIgnoreCase("album_idx")) {
				entityMap.put("ALBUM_IDX", 100);
			} else if(idxField.equalsIgnoreCase("artist_idx")) {
				entityMap.put("ARTIST_IDX", 100);
			} else if(idxField.equalsIgnoreCase("arranger_idx")) {
				entityMap.put("ARRANGER_IDX", 100);
			} else if(idxField.equalsIgnoreCase("composer_idx")) {
				entityMap.put("COMPOSER_IDX", 100);
			} else if(idxField.equalsIgnoreCase("featuring_idx")) {
				entityMap.put("FEATURING_IDX", 100);
			} else if(idxField.equalsIgnoreCase("lyricist_idx")) {
				entityMap.put("LYRICIST_IDX", 100);
			} else if(idxField.equalsIgnoreCase("genre_idx")) {
				entityMap.put("GENRE_IDX", 100);
			} else if(idxField.equalsIgnoreCase("artist_role_idx")) {
				entityMap.put("ARTIST_ROLE_IDX", 100);
			} else {
				entityMap.put("TRACK_IDX", 100);
			} 
			
			for (Entry<String, Integer> e : entityMap.entrySet()) {
				if (result.size() > 0) {
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
				}
				if (qOption.isNofM()) {
					result.add(new WhereSet(e.getKey(), qOption.getOption(), keyword, e.getValue(), qOption.getNofmPercent()));
				} else {
					result.add(new WhereSet(e.getKey(), qOption.getOption(), keyword, e.getValue()));
				}
			}
		} 

//		for (Entry<String, Integer> e : idxScoreMap.entrySet()) {
//			if (result.size() > 0) {
//				result.add(new WhereSet(Protocol.WhereSet.OP_OR));
//			}
//			if (qOption.isNofM()) {
//				result.add(new WhereSet(e.getKey(), qOption.getOption(), keyword, e.getValue(), qOption.getNofmPercent()));
//			} else {
//				result.add(new WhereSet(e.getKey(), qOption.getOption(), keyword, e.getValue()));
//			}
//		}
		
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
	
	// 통합검색 정렬 체크
	protected String TotalOrderBy(Map<String, String> params, String collection) {
		
		String col_sort = collection + "@sort";
		
		String sort = RestUtils.getParam(params, col_sort.toLowerCase());
		
		return sort;
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
	
	protected SayclubResult makeResult(Result result, Query query, Map<String, String> params) throws IRException {
		return SayclubResult.makeProductResult(query, result, params);
	}
	
	protected AutoTotalResult makeAutoResult(ResultSet result, QuerySet query, Map<String, String> params) throws IRException {
		return AutoTotalResult.makeAutoTotalResult(query, result, params);
	}
	
	protected AutoResult makeAutoTagResult(Result result, Query query, Map<String, String> params) throws IRException {
		String tag = RestUtils.getParam(params, "filter.use_esalbum_yn");

		return AutoResult.makeAutoTagResult(query, result, params, tag);
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
