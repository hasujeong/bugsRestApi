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
import com.diquest.rest.nhn.common.Connection;
import com.diquest.rest.nhn.filter.parse.FilterValueParser;
import com.diquest.rest.nhn.filter.result.FilterFieldParseResult;
import com.diquest.rest.nhn.result.NhnError;
import com.diquest.rest.nhn.result.NhnResult;
import com.diquest.rest.nhn.result.SayclubResult;
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
		
	public String saySearch(Map<String, String> params, Map<String, Object> reqHeader, HttpServletRequest request) {
		
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
	
	private String makeEmptyNhnData(Map<String, String> params) {
//		JsonUnknownUriResult result = new JsonUnknownUriResult(HttpStatus.OK, NhnResult.makeEmptyResult());
		String emptyData = GsonLoader.getInstance().toJson(NhnResult.makeEmptyResult());
		return emptyData;
	}
	
	protected String getCollection(Map<String, String> params) {
//		System.out.println("----------------" + params.get("collection"));		
		return params.get("collection");
	}
	
	protected String getTotalCollection(Map<String, String> params) {		
		String collection= params.get("collection");
		
		return collection;
	}
	
	private FilterFieldParseResult parseFilterParams(Map<String, String> params) {
		return new FilterValueParser(params).parseAll();
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
		
		if(collection.equalsIgnoreCase(SayclubCollections.SAYCAST)) {
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
	
	private SelectSet[] parseSelect(Map<String, String> params) {
		return SelectSetService.getInstance().makeSelectSet(params);
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
	
	protected FilterSet[] parseFilter(Map<String, String> params, FilterFieldParseResult filterFieldParseResult, String collection) throws InvalidParameterException {
		return FilterSetService.getInstance().parseFilter(params, filterFieldParseResult, collection);
	}
	
	private void parseTrigger(Map<String, String> req, Query query, String collection) throws InvalidParameterException {
		TriggerFieldService.getInstance().parseTrigger(req, query, collection);
	}
	
	protected SayclubResult makeResult(Result result, Query query, Map<String, String> params) throws IRException {
		return SayclubResult.makeProductResult(query, result, params);
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
