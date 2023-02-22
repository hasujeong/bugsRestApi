package com.diquest.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
import com.diquest.ir.rest.json.reponse.ResponseMaker;
import com.diquest.ir.rest.server.log.ServerLogManager;
import com.diquest.ir.rest.util.RestUtils;
import com.diquest.mapper.AdminMapper;
import com.diquest.mapper.domain.ClassicArtistSelector;
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
import com.diquest.rest.nhn.result.SayMusicResult;
import com.diquest.rest.nhn.result.SimilarResult;
import com.diquest.rest.nhn.result.TotalResult;
import com.diquest.rest.nhn.result.TotalsearchResult;
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
import com.diquest.rest.nhn.service.select.SayclubMusicSelectSet;
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

	protected static int track_score_100 = 100;
	protected static int track_score_150 = 150;
	protected static int track_score = 100;
	protected static int album_score = 100;
	protected static int artist_score = 100;

	protected static String artist_keyword = "";
	protected static String CSartist_keyword = "";

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

		artistSelector artistSelector = new artistSelector();

		Map<String, String> artistFieldMap = new HashMap<String, String>();

		for (int i = 0; i < artistSelector.getArtistNmList().length; i++) {
			artistFieldMap.put(artistSelector.getArtistNmList()[i], artistSelector.getArtistNmList()[i]);

		}
//		System.out.println("--------- " + artistFieldMap);
		fieldList.add(artistFieldMap);

		ClassicArtistSelector CSartistSelector = new ClassicArtistSelector();

		LinkedHashMap<String, String> CSartistFieldMap = new LinkedHashMap<String, String>();

		for (int i = 0; i < CSartistSelector.getCSArtistNmList().length; i++) {
			CSartistFieldMap.put(CSartistSelector.getCSArtistNmList()[i], CSartistSelector.getCSArtistNmList()[i]);

		}
//		System.out.println("--------- " + CSartistFieldMap);
		fieldList.add(CSartistFieldMap);

		return fieldList;
	}

	public String search(Map<String, String> params, Map<String, Object> reqHeader, HttpServletRequest request) {

//		Map<String, String> fieldSelectorMap = self.fieldSelector();
//		System.out.println(":::::::::::::::::::" + fieldSelectorMap);

		List<Map<String, String>> fieldMap = self.fieldSelector();

		Map<String, String> fieldSelectorMap = fieldMap.get(0);
		Map<String, String> artistSelectorMap = fieldMap.get(1);
		Map<String, String> CSartistSelectorMap = fieldMap.get(2);

		String req = "";
		req += "Host: " + (String) reqHeader.get("host") + "\n";
		req += "Connection: " + (String) reqHeader.get("connection") + "\n";
		req += "Upgrade-Insecure-Requests: " + (String) reqHeader.get("upgrade-insecure-requests") + "\n";
		req += "User-Agent: " + (String) reqHeader.get("user-agent") + "\n";
		req += "Accept: " + (String) reqHeader.get("accept") + "\n";
		req += "Accept-Encoding: " + (String) reqHeader.get("accept-encoding") + "\n";
		req += "Accept-Language: " + (String) reqHeader.get("accept-language");

		if (params.get("q") != null) {
			if(params.get("q").isEmpty()) {
				return makeEmptyNhnData(params);
			}
			if(parseSize(params) == 0) {
				return makeEmptyNhnData(params);
			}
//			String chkQ = parseQ(params);
//
//			if (!chkQ.matches(".*[0-9|a-z|A-Z|ㄱ-ㅎ|ㅏ-ㅣ|가-힝].*")) {
//				return makeEmptyNhnData(params);
//			}

			String paramQ = parseQ(params);
			String qValue = paramQ.replaceAll("\\s", "");
			String col = getCollection(params);

			if(col.equalsIgnoreCase(Collections.TRACK)) {
				if (fieldSelectorMap.containsKey(qValue) == true) {
//					System.out.println(":::::qValue:::::::" + qValue);

					String selectedValue = fieldSelectorMap.get(qValue);
//					System.out.println("==================" + selectedValue);

					if (selectedValue.equalsIgnoreCase("track")) {
						track_score_100 = 1000;
						track_score_150 = 1000;
						album_score = 10;
						artist_score = 30;
					} else if (selectedValue.equalsIgnoreCase("album")) {
						track_score_100 = 15;
						track_score_150 = 15;
						album_score = 1000;
						artist_score = 30;
					} else {
						track_score_100 = 15;
						track_score_150 = 15;
						album_score = 10;
						artist_score = 1000;
					}
				} else {
					String[] qSlice = params.get("q").split("\\s+");

					if (qSlice.length > 0) {
						for (int s = 0; s < qSlice.length; s++) {
							for (String key : artistSelectorMap.keySet()) {
								if (key.equalsIgnoreCase(qSlice[s])) {
//										System.out.println("------------ " + artistSelectorMap.get(qSlice[s]));

									artist_keyword = qSlice[s];
//										a_keyword = params.get("q").replace(artist_keyword, "");
								}
							}
						}
					}

					track_score_100 = 100;
					track_score_150 = 150;
					album_score = 100;
					artist_score = 100;
				}
			} else if (col.equalsIgnoreCase(Collections.CLASSIC)) {
				String CS_kwd = params.get("q").replaceAll("\\s", "");

				for (String key : CSartistSelectorMap.keySet()) {
					if (CS_kwd.indexOf(key) > -1) {
						CS_kwd = CS_kwd.replaceAll(key, "");
						CSartist_keyword = CS_kwd;
					}
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
			query.setSearchOption((byte) (Protocol.SearchOption.BANNED | Protocol.SearchOption.STOPWORD	| Protocol.SearchOption.CACHE));
			query.setRankingOption((byte) (Protocol.RankingOption.CATEGORY_RANKING | Protocol.RankingOption.DOCUMENT_RANKING));
			query.setCategoryRankingOption((byte) (Protocol.CategoryRankingOption.EQUIV_SYNONYM	| Protocol.CategoryRankingOption.QUASI_SYNONYM));
			query.setUserName(getUserName(params)); // 로그인 사용자 ID 기록
			query.setExtData(RestUtils.getParam(params, "pr")); // pr (app,web,pc)
			query.setLoggable(getLoggable(RestUtils.getParam(params, "search_tp")));
			query.setLogKeyword(parseQ(params).toCharArray());
			query.setPrintQuery(true); // 실제 사용시 false
			parseTrigger(params, query, getCollection(params));
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
						query.setFilter(parseFilter(params, filterFieldParseResult, getCollection(params)));
						query.setWhere(parseWhere2(params, filterFieldParseResult, getCollection(params)));
//						query.setGroupBy(parseGroupBy(params));
						query.setOrderby(parseOrderBy(params, getCollection(params)));
						query.setFrom(getCollection(params));
						query.setResult(parseStart(params) - 1, parseStart(params) + parseSize(params) - 2);
						query.setSearchKeyword(parseQ(params));
						query.setFaultless(true);
						query.setThesaurusOption((byte) (Protocol.ThesaurusOption.EQUIV_SYNONYM	| Protocol.ThesaurusOption.QUASI_SYNONYM));
						query.setSearchOption((byte) (Protocol.SearchOption.BANNED | Protocol.SearchOption.STOPWORD	| Protocol.SearchOption.CACHE));
						query.setRankingOption((byte) (Protocol.RankingOption.CATEGORY_RANKING | Protocol.RankingOption.DOCUMENT_RANKING));
						query.setCategoryRankingOption((byte) (Protocol.CategoryRankingOption.EQUIV_SYNONYM	| Protocol.CategoryRankingOption.QUASI_SYNONYM));
						query.setUserName(getUserName(params)); // 로그인 사용자 ID 기록
						query.setExtData(RestUtils.getParam(params, "pr")); // pr (app,web,pc)
						query.setLoggable(getLoggable(RestUtils.getParam(params, "search_tp")));
						query.setLogKeyword(parseQ(params).toCharArray());
						query.setPrintQuery(true); // 실제 사용시 false
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

	// 통합검색 API (멀티스레드)
	public String Totalsearch(Map<String, String> params, Map<String, Object> reqHeader, HttpServletRequest request) {

		String keyword = parseQ(params);

		try {
			keyword = URLEncoder.encode(keyword, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		int colSize = 10;
		String colSort = "";

		String urlStr = "";
		String colStr = "";

		String prValue = "";
		String searchTpValue = "";

		if (!RestUtils.getParam(params, "pr").equalsIgnoreCase("")) {
			prValue = "&pr=" + RestUtils.getParam(params, "pr");
		}

		if (!RestUtils.getParam(params, "search_tp").equalsIgnoreCase("")) {
			searchTpValue = "&search_tp=" + RestUtils.getParam(params, "search_tp");
		}

		String[] colArray = getTotalCollection(params);

		for (int i = 0; i < colArray.length; i++) {
			if (colArray[i].equalsIgnoreCase(Collections.EXACT_ARTIST)) {
				colSize = parseTotalSize(params, Collections.EXACT_ARTIST);
				colStr += Collections.EXACT_ARTIST + "##";
				colSort = TotalOrderBy(params, Collections.EXACT_ARTIST);

				if (!colSort.equalsIgnoreCase("")) {
					colSort = "&sort=" + colSort;
				}

				urlStr += "http://api-alar-kr-tcc.qpit.ai/7S4pV1yEaFoWJsj/v1/search/advanced.search?filter.search_exclude_yn=N&q="
						+ keyword + prValue + searchTpValue + "&q_option=and,exact_artist_idx&size=" + colSize + colSort
						+ "##";
				colSort = "";
			}
			if (colArray[i].equalsIgnoreCase(Collections.TRACK)) {
				colSize = parseTotalSize(params, Collections.TRACK);
				colStr += Collections.TRACK + "##";
				colSort = TotalOrderBy(params, Collections.TRACK);

				if (!colSort.equalsIgnoreCase("")) {
					colSort = "&sort=" + colSort;
				}

				urlStr += "http://api-track-kr-tcc.qpit.ai/lZjTO0HlFg91HwD/v1/search/advanced.search?filter.search_exclude_yn=N&q="
						+ keyword + prValue + searchTpValue + "&size=" + colSize + colSort + "##";
				colSort = "";
			}
			if (colArray[i].equalsIgnoreCase(Collections.ALBUM)) {
				colSize = parseTotalSize(params, Collections.ALBUM);
				colStr += Collections.ALBUM + "##";
				colSort = TotalOrderBy(params, Collections.ALBUM);

				if (!colSort.equalsIgnoreCase("")) {
					colSort = "&sort=" + colSort;
				}

				urlStr += "http://api-alar-kr-tcc.qpit.ai/vbmKja0BuYy35Ok/v1/search/advanced.search?filter.search_exclude_yn=N&q="
						+ keyword + prValue + searchTpValue + "&size=" + colSize + colSort + "##";
				colSort = "";
			}
			if (colArray[i].equalsIgnoreCase(Collections.ARTIST)) {
				colSize = parseTotalSize(params, Collections.ARTIST);
				colStr += Collections.ARTIST + "##";
				colSort = TotalOrderBy(params, Collections.ARTIST);

				if (!colSort.equalsIgnoreCase("")) {
					colSort = "&sort=" + colSort;
				}

				urlStr += "http://api-alar-kr-tcc.qpit.ai/7S4pV1yEaFoWJsj/v1/search/advanced.search?filter.search_exclude_yn=N&q="
						+ keyword + prValue + searchTpValue + "&size=" + colSize + colSort + "##";
				colSort = "";
			}
			if (colArray[i].equalsIgnoreCase(Collections.MV)) {
				colSize = parseTotalSize(params, Collections.MV);
				colStr += Collections.MV + "##";
				colSort = TotalOrderBy(params, Collections.MV);

				if (!colSort.equalsIgnoreCase("")) {
					colSort = "&sort=" + colSort;
				}

				urlStr += "http://api-etc-kr-tcc.qpit.ai/58IHeEjp2lyoR4M/v1/search/advanced.search?filter.search_exclude_yn=N&q="
						+ keyword + prValue + searchTpValue + "&size=" + colSize + colSort + "##";
				colSort = "";
			}
			if (colArray[i].equalsIgnoreCase(Collections.MUSICCAST)) {
				colSize = parseTotalSize(params, Collections.MUSICCAST);
				colStr += Collections.MUSICCAST + "##";
				colSort = TotalOrderBy(params, Collections.MUSICCAST);

				if (!colSort.equalsIgnoreCase("")) {
					colSort = "&sort=" + colSort;
				}

				urlStr += "http://api-etc-kr-tcc.qpit.ai/7ZEz9GaqpMRc5DH/v1/search/advanced.search?q=" + keyword
						+ prValue + searchTpValue + "&size=" + colSize + colSort + "##";
				colSort = "";
			}
			if (colArray[i].equalsIgnoreCase(Collections.MUSICPD)) {
				colSize = parseTotalSize(params, Collections.MUSICPD);
				colStr += Collections.MUSICPD + "##";
				colSort = TotalOrderBy(params, Collections.MUSICPD);

				if (!colSort.equalsIgnoreCase("")) {
					colSort = "&sort=" + colSort;
				}

				urlStr += "http://api-etc-kr-tcc.qpit.ai/6Jfys7XEvdQ0KuU/v1/search/advanced.search?filter.search_exclude_yn=N&q="
						+ keyword + prValue + searchTpValue + "&size=" + colSize + colSort + "##";
				colSort = "";
			}
			if (colArray[i].equalsIgnoreCase(Collections.MUSICPOST)) {
				colSize = parseTotalSize(params, Collections.MUSICPOST);
				colStr += Collections.MUSICPOST + "##";
				colSort = TotalOrderBy(params, Collections.MUSICPOST);

				if (!colSort.equalsIgnoreCase("")) {
					colSort = "&sort=" + colSort;
				}

				urlStr += "http://api-etc-kr-tcc.qpit.ai/SVmCkyjdYM9n3go/v1/search/advanced.search?q=" + keyword
						+ prValue + searchTpValue + "&size=" + colSize + colSort + "##";
				colSort = "";
			}
			if (colArray[i].equalsIgnoreCase(Collections.CLASSIC)) {
				colSize = parseTotalSize(params, Collections.CLASSIC);
				colStr += Collections.CLASSIC + "##";
				colSort = TotalOrderBy(params, Collections.CLASSIC);

				if (!colSort.equalsIgnoreCase("")) {
					colSort = "&sort=" + colSort;
				}

				urlStr += "http://api-etc-kr-tcc.qpit.ai/0WleItZOyGJbrVF/v1/search/advanced.search?q=" + keyword
						+ prValue + searchTpValue + "&size=" + colSize + colSort + "##";
				colSort = "";
			}
			if (colArray[i].equalsIgnoreCase(Collections.LYRICS)) {
				colSize = parseTotalSize(params, Collections.LYRICS);
				colStr += Collections.LYRICS + "##";
				colSort = TotalOrderBy(params, Collections.LYRICS);

				if (!colSort.equalsIgnoreCase("")) {
					colSort = "&sort=" + colSort;
				}

				urlStr += "http://api-alar-kr-tcc.qpit.ai/M2NjMWRjOWMwOGN/v1/search/advanced.search?q=" + keyword
						+ prValue + searchTpValue + "&size=" + colSize + colSort + "##";
				colSort = "";
			}
		}

		String[] totalUrls = urlStr.split("##");
		String[] cols = colStr.split("##");

//		System.out.println("url :: " + urlStr);
		String totalUrl = "";
		String result = "";

		List<Map<Integer, Object>> totalList = new ArrayList<Map<Integer, Object>>();

		List<Future<Map<Integer, Object>>> resultList = new ArrayList<>();

		Gson gson = new Gson();

		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(9);

		for (int j = 0; j < totalUrls.length; j++) {
			totalUrl = totalUrls[j];

			Callable<Map<Integer, Object>> task = new totalThread(totalUrl, cols[j], j);
			Future<Map<Integer, Object>> futures = executor.submit(task);
			resultList.add(futures);
		}
		executor.shutdown();

//		List<Callable<Map<Integer, Object>>> totalSearch = new ArrayList<>();
//		
//			for(int j=0 ; j < totalUrls.length ; j++) {
//				totalUrl = totalUrls[j];
//				
//				totalSearch.add(new totalThread(totalUrl, cols[j], j));
//			}
//		
//		ExecutorService executorService = Executors.newFixedThreadPool(9);
//		
//		try {
//			resultList = executorService.invokeAll(totalSearch);
//		} catch (InterruptedException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		executorService.shutdown();

		for (Future<Map<Integer, Object>> future : resultList) {
			try {
//                   System.out.println("Future result is - " + " - " + future.get() + "; And Task done is " + future.isDone());
				totalList.add(future.get());
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}

		try {
			result = gson.toJson(makeTotalsearchResult(totalList));
		} catch (IRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	// 통합검색 실행 thread
	public class totalThread implements Callable<Map<Integer, Object>> {

		String totalUrl = "";
		String cols = "";
		int num = 0;

		public totalThread(String totalUrl, String cols, int num) {
			this.totalUrl = totalUrl;
			this.cols = cols;
			this.num = num;
		}

		@Override
		public Map<Integer, Object> call() {

			int totalCode = 0;
			String resultJson = "";

			Map<Integer, Object> Col_items = new HashMap<Integer, Object>();

			try {
				HttpURLConnection totalConnection = (HttpURLConnection) new URL(totalUrl).openConnection();

				totalConnection.setRequestMethod("GET");
				totalConnection.setRequestProperty("Content-Type", "application/json;");
				totalConnection.setRequestProperty("Accept", "application/json");
				totalConnection.setConnectTimeout(5000);
				totalConnection.setReadTimeout(5000);
				totalConnection.setDoOutput(true);

				try {
					totalCode = totalConnection.getResponseCode();

					if (totalCode == 400 || totalCode == 401 || totalCode == 500) {
						System.out.println(totalCode + " Error!");
					} else {
						BufferedReader br = new BufferedReader(new InputStreamReader(totalConnection.getInputStream(), "UTF-8"));
						StringBuilder sb = new StringBuilder();
						String line = "";

						while ((line = br.readLine()) != null) {
							sb.append(line);
						}

						resultJson = sb.toString();

						Col_items.put(num, resultJson);
						Col_items.put(num + 100, cols);
					}
//    					System.out.println("순서"+num+"("+ cols + ") : " + totalUrl);

				} catch (Exception e) {
					e.printStackTrace();
				}

				totalConnection.disconnect();
			} catch (Exception e) {
				e.printStackTrace();
			}

			return Col_items;
		}
	}

	// 통합검색 API (기존)
	public String TestTotalsearch(Map<String, String> params, Map<String, Object> reqHeader,
			HttpServletRequest request) {

		List<Map<String, String>> fieldMap = self.fieldSelector();

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

		if (params.get("q") != null) {
			if (params.get("q").isEmpty()) {
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

					if (qSlice.length > 0) {
						for (int s = 0; s < qSlice.length; s++) {
							for (String key : artistSelectorMap.keySet()) {
								if (key.equalsIgnoreCase(qSlice[s])) {
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

		if (colStr.equalsIgnoreCase(Collections.TOTAL)) {
			queryInt = colArray.length;
		} else {
			queryInt = 1;
		}

		QuerySet querySet = new QuerySet(queryInt);
		Query query = new Query();

		try {

			for (int i = 0; i < colArray.length; i++) {
				query = new Query();
				FilterFieldParseResult filterFieldParseResult = parseFilterParams(params);
				query.setSelect(parseSelect(params));
				query.setFilter(parseTotalFilter(params, filterFieldParseResult, colArray[i]));
				query.setWhere(parseWhere(params, filterFieldParseResult, colArray[i]));
				// query.setGroupBy(parseGroupBy(params));
				query.setOrderby(parseTotalOrderBy(params, colArray[i]));
				query.setFrom(colArray[i]);
				query.setResult(parseStart(params) - 1, parseStart(params) + parseTotalSize(params, colArray[i]) - 2);
				query.setSearchKeyword(parseQ(params));
				query.setFaultless(true);
				query.setThesaurusOption((byte) (Protocol.ThesaurusOption.EQUIV_SYNONYM | Protocol.ThesaurusOption.QUASI_SYNONYM));
				query.setSearchOption((byte) (Protocol.SearchOption.BANNED | Protocol.SearchOption.STOPWORD	| Protocol.SearchOption.CACHE));
				query.setRankingOption((byte) (Protocol.RankingOption.CATEGORY_RANKING | Protocol.RankingOption.DOCUMENT_RANKING));
				query.setCategoryRankingOption((byte) (Protocol.CategoryRankingOption.EQUIV_SYNONYM	| Protocol.CategoryRankingOption.QUASI_SYNONYM));
				query.setUserName(getUserName(params)); // 로그인 사용자 ID 기록
				query.setExtData(RestUtils.getParam(params, "pr")); // pr (app,web,pc)
				query.setLoggable(getLoggable(RestUtils.getParam(params, "search_tp")));
				query.setLogKeyword(parseQ(params).toCharArray());
				query.setPrintQuery(true); // 실제 사용시 false
				parseTrigger(params, query, colArray[i]);
				query.setResultModifier("typo");

				querySet.addQuery(query);

				String queryStr = parser.queryToString(query);
//				System.out.println(" :::::::::: query ::::::: [" + i + "] " + queryStr);
			}

			CommandSearchRequest.setProps(Connection.IP, Connection.PORT, 5000, 50, 50);
			CommandSearchRequest commandSearchRequest = new CommandSearchRequest(Connection.IP, Connection.PORT);

			int returnCode = commandSearchRequest.request(querySet);

			if (returnCode <= -100) {
				ErrorMessageService.getInstance().minusReturnCodeLog(returnCode, commandSearchRequest.getException(),req);
				logMessageService.receiveEnd(reqHeader, request);
				return commandSearchRequestErrorResponse(commandSearchRequest.getException().getErrorMessage());
			} else {
				logMessageService.messageReceived(reqHeader, request);
			}

			String resultJson = "";

			if (colStr.equalsIgnoreCase(Collections.TOTAL)) {
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

		if (params.get("q") != null) {
			if (params.get("q").isEmpty()) {
				return makeEmptyNhnData(params);
			}
			if (parseSize(params) == 0) {
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

		String colStr = params.get("collection");
		int queryInt = 1;

		if (colStr.equalsIgnoreCase(Collections.AUTO_TOTAL)) {
			queryInt = 2;
		} else {
			queryInt = 1;
		}

		QuerySet querySet = new QuerySet(queryInt);
		Query query = new Query();

		try {
			for (int i = 0; i < queryInt; i++) {
				query = new Query();

				FilterFieldParseResult filterFieldParseResult = parseFilterParams(params);
				query.setSelect(parseAutoSelect(params, getCollection(params), i));
//				query.setFilter(parseFilter(params, filterFieldParseResult, getCollection(params)));
				query.setWhere(parseAutoWhere(params, filterFieldParseResult, getCollection(params), i));
				// query.setGroupBy(parseGroupBy(params));
				query.setOrderby(parseOrderBy(params, getCollection(params)));
				query.setFrom(getCollection(params));
				if (i == 1) {
					query.setResult(parseStart(params) - 1, 19);
				} else {
					query.setResult(parseStart(params) - 1, parseStart(params) + parseAutoSize(params, i) - 2);
				}
				query.setSearchKeyword(parseQ(params));
				query.setFaultless(true);
				query.setThesaurusOption((byte) (Protocol.ThesaurusOption.EQUIV_SYNONYM | Protocol.ThesaurusOption.QUASI_SYNONYM));
				query.setSearchOption((byte) (Protocol.SearchOption.BANNED | Protocol.SearchOption.STOPWORD	| Protocol.SearchOption.CACHE));
				query.setRankingOption((byte) (Protocol.RankingOption.CATEGORY_RANKING | Protocol.RankingOption.DOCUMENT_RANKING));
				query.setCategoryRankingOption((byte) (Protocol.CategoryRankingOption.EQUIV_SYNONYM	| Protocol.CategoryRankingOption.QUASI_SYNONYM));
//				query.setLoggable(false);
				query.setLoggable(getAutoLoggable(RestUtils.getParam(params, "search_tp")));
				query.setLogKeyword(parseQ(params).toCharArray());
				query.setPrintQuery(true); // 실제 사용시 false
				parseTrigger(params, query, getCollection(params));
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
				ErrorMessageService.getInstance().minusReturnCodeLog(returnCode, commandSearchRequest.getException(),
						req);
				logMessageService.receiveEnd(reqHeader, request);
				return commandSearchRequestErrorResponse(commandSearchRequest.getException().getErrorMessage());
			} else {
				logMessageService.messageReceived(reqHeader, request);

				if (colStr.equalsIgnoreCase(Collections.AUTO_TOTAL)) {
					ResultSet resultSet = commandSearchRequest.getResultSet();
					Result[] resultlist = resultSet.getResultList();
					Result result1 = resultlist[1];

					int totalSize = 0;
					String typoKwd = "";

					totalSize = result1.getTotalSize();

					if (totalSize == 0) {
						if (result1.getValue("typo-result") != null) {
							typoKwd = result1.getValue("typo-result");
						}

						if (!typoKwd.equals("")) {
							params.put("q", typoKwd);
							querySet = new QuerySet(queryInt);
							query = new Query();

							for (int i = 0; i < queryInt; i++) {
								query = new Query();

								FilterFieldParseResult filterFieldParseResult = parseFilterParams(params);
								query.setSelect(parseAutoSelect(params, getCollection(params), i));
								query.setWhere(parseAutoWhere2(params, filterFieldParseResult, getCollection(params), i,
										OriginKwd));
								query.setOrderby(parseOrderBy(params, getCollection(params)));
								query.setFrom(getCollection(params));
								if (i == 1) {
									query.setResult(parseStart(params) - 1, 19);
									query.setSearchKeyword(parseQ(params));
								} else {
									query.setResult(parseStart(params) - 1,
											parseStart(params) + parseAutoSize(params, i) - 2);
									query.setSearchKeyword(OriginKwd);
								}
								query.setFaultless(true);
								query.setThesaurusOption((byte) (Protocol.ThesaurusOption.EQUIV_SYNONYM	| Protocol.ThesaurusOption.QUASI_SYNONYM));
								query.setSearchOption((byte) (Protocol.SearchOption.BANNED | Protocol.SearchOption.STOPWORD | Protocol.SearchOption.CACHE));
								query.setRankingOption((byte) (Protocol.RankingOption.CATEGORY_RANKING | Protocol.RankingOption.DOCUMENT_RANKING));
								query.setCategoryRankingOption((byte) (Protocol.CategoryRankingOption.EQUIV_SYNONYM | Protocol.CategoryRankingOption.QUASI_SYNONYM));
//								query.setLoggable(false);
								query.setLoggable(getAutoLoggable(RestUtils.getParam(params, "search_tp")));
								query.setLogKeyword(parseQ(params).toCharArray());
								query.setPrintQuery(true); // 실제 사용시 false
								parseTrigger(params, query, getCollection(params));

								querySet.addQuery(query);

								String queryStr = parser.queryToString(query);
								// System.out.println(" :::::::::: query ::::::: " + queryStr);
							}

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
			}

			String resultJson = "";

			if (colStr.equalsIgnoreCase(Collections.AUTO_TOTAL)) {
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
			query.setPrintQuery(true); // 실제 사용시 false
			parseTrigger(params, query, getCollection(params));
//			query.setQueryModifier("diver");
			query.setResultModifier("typo");
//				query.setDebug(true);
//				query.setFaultless(true);	

			querySet.addQuery(query);

			String queryStr = parser.queryToString(query);
//			System.out.println(" :::::::::: query ::::::: " + queryStr);

			CommandSearchRequest.setProps(Connection.IP, Connection.PORT, 5000, 50, 50);
			CommandSearchRequest commandSearchRequest = new CommandSearchRequest(Connection.IP, Connection.PORT);

			int returnCode = commandSearchRequest.request(querySet);

			if (returnCode <= -100) {
				ErrorMessageService.getInstance().minusReturnCodeLog(returnCode, commandSearchRequest.getException(),req);
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
	public String purchasedSearch(Map<String, Object> params, Map<String, Object> document,
			Map<String, Object> reqHeader, HttpServletRequest request) {

		List<Map<String, String>> index = (List<Map<String, String>>) params.get("index");
//		Map<String, Object> document = (Map<String, Object>) params.get("document");

		String collection = Collections.TRACK;
		String q = "";
		String puchaseId = "";
		int start = (int) document.get("start");
		int size = (int) document.get("size");

		Map<String, String> idx1 = index.get(0);

		for (int i = 0; i < index.size(); i++) {
			if (index.get(i).get("name").equalsIgnoreCase("key_idx")) {
				puchaseId = index.get(i).get("query");
			} else {
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
			query.setResult(start - 1, (start + size) - 2);
			query.setSearchKeyword(q);
			query.setFaultless(true);
			query.setThesaurusOption((byte) (Protocol.ThesaurusOption.EQUIV_SYNONYM | Protocol.ThesaurusOption.QUASI_SYNONYM));
			query.setSearchOption((byte) (Protocol.SearchOption.BANNED | Protocol.SearchOption.STOPWORD	| Protocol.SearchOption.CACHE));
			query.setRankingOption((byte) (Protocol.RankingOption.CATEGORY_RANKING | Protocol.RankingOption.DOCUMENT_RANKING));
			query.setCategoryRankingOption((byte) (Protocol.CategoryRankingOption.EQUIV_SYNONYM	| Protocol.CategoryRankingOption.QUASI_SYNONYM));
			query.setLoggable(false);
			query.setPrintQuery(true); // 실제 사용시 false
			query.setResultModifier("typo");

			querySet.addQuery(query);

			String queryStr = parser.queryToString(query);
//				System.out.println(" :::::::::: query ::::::: " + queryStr);

			CommandSearchRequest.setProps(Connection.IP, Connection.PORT, 5000, 50, 50);
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

	// 구매한 영상 검색 API
	public String purchasedMvSearch(Map<String, Object> params, Map<String, Object> document,
			Map<String, Object> reqHeader, HttpServletRequest request) {

		List<Map<String, String>> index = (List<Map<String, String>>) params.get("index");
//		Map<String, Object> document = (Map<String, Object>) params.get("document");

		String collection = Collections.MV;
		String q = "";
		String puchaseId = "";
		int start = (int) document.get("start");
		int size = (int) document.get("size");

		Map<String, String> idx1 = index.get(0);

		for (int i = 0; i < index.size(); i++) {
			if (index.get(i).get("name").equalsIgnoreCase("key_idx")) {
				puchaseId = index.get(i).get("query");
			} else {
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
			query.setResult(start - 1, (start + size) - 2);
			query.setSearchKeyword(q);
			query.setFaultless(true);
			query.setThesaurusOption((byte) (Protocol.ThesaurusOption.EQUIV_SYNONYM | Protocol.ThesaurusOption.QUASI_SYNONYM));
			query.setSearchOption((byte) (Protocol.SearchOption.BANNED | Protocol.SearchOption.STOPWORD	| Protocol.SearchOption.CACHE));
			query.setRankingOption((byte) (Protocol.RankingOption.CATEGORY_RANKING | Protocol.RankingOption.DOCUMENT_RANKING));
			query.setCategoryRankingOption((byte) (Protocol.CategoryRankingOption.EQUIV_SYNONYM	| Protocol.CategoryRankingOption.QUASI_SYNONYM));
			query.setLoggable(false);
			query.setPrintQuery(true); // 실제 사용시 false
			query.setResultModifier("typo");

			querySet.addQuery(query);

			String queryStr = parser.queryToString(query);
//				System.out.println(" :::::::::: query ::::::: " + queryStr);

			CommandSearchRequest.setProps(Connection.IP, Connection.PORT, 5000, 50, 50);
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
	public String similarSearch(List<Map<String, String>> index, Map<String, Object> document,
			Map<String, Object> reqHeader, HttpServletRequest request) {

		Map<String, String> params = new HashMap<String, String>();

		String collection = Collections.TRACK;
		String q = "";

		int start = (int) document.get("start");
		int size = (int) document.get("size");

		int num = 1;
		int searchSize = index.size() / 3;

		if (searchSize < 0) {
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
			for (int i = 0; i < searchSize; i++) {

				for (int j = 0; j < index.size(); j++) {
					if (num == Integer.parseInt(index.get(j).get("num"))) {
						if (index.get(j).get("name").equalsIgnoreCase("track_artist_album_idx")) {
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
				query.setResult(start - 1, (start + size) - 2);
				query.setSearchKeyword(q);
				query.setFaultless(true);
				query.setThesaurusOption((byte) (Protocol.ThesaurusOption.EQUIV_SYNONYM | Protocol.ThesaurusOption.QUASI_SYNONYM));
				query.setSearchOption((byte) (Protocol.SearchOption.BANNED | Protocol.SearchOption.STOPWORD	| Protocol.SearchOption.CACHE));
				query.setRankingOption((byte) (Protocol.RankingOption.CATEGORY_RANKING | Protocol.RankingOption.DOCUMENT_RANKING));
				query.setCategoryRankingOption((byte) (Protocol.CategoryRankingOption.EQUIV_SYNONYM	| Protocol.CategoryRankingOption.QUASI_SYNONYM));
				query.setLoggable(false);
				query.setPrintQuery(true); // 실제 사용시 false
				query.setResultModifier("typo");

				querySet.addQuery(query);

				String queryStr = parser.queryToString(query);
//					System.out.println(" :::::::::: query ::::::: " + queryStr);
				num++;
			}

			CommandSearchRequest.setProps(Connection.IP, Connection.PORT, 5000, 50, 50);
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

		if (!rawStr.equalsIgnoreCase("")) {
			raw = rawStr.split("&&");
		}

		String q = "";

		for (int j = 0; j < index.size(); j++) {
			if (index.get(j).get("type").equalsIgnoreCase("intersection")) {
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
			if (raw.length > 0) {
				query.setFilter(EntityFilter(raw));
			}
			query.setOrderby(EntityOrderBy(sortVal, Collections.ENTITY));
			query.setFrom(Collections.ENTITY);
			query.setResult(start - 1, (start + size) - 2);
			query.setSearchKeyword(q);
			query.setFaultless(true);
			query.setThesaurusOption((byte) (Protocol.ThesaurusOption.EQUIV_SYNONYM | Protocol.ThesaurusOption.QUASI_SYNONYM));
			query.setSearchOption((byte) (Protocol.SearchOption.BANNED | Protocol.SearchOption.STOPWORD	| Protocol.SearchOption.CACHE));
			query.setRankingOption((byte) (Protocol.RankingOption.CATEGORY_RANKING | Protocol.RankingOption.DOCUMENT_RANKING));
			query.setCategoryRankingOption((byte) (Protocol.CategoryRankingOption.EQUIV_SYNONYM	| Protocol.CategoryRankingOption.QUASI_SYNONYM));
			query.setLoggable(false);
			query.setLogKeyword(q.toCharArray());
			query.setPrintQuery(true); // 실제 사용시 false
			query.setResultModifier("typo");

			querySet.addQuery(query);

			String queryStr = parser.queryToString(query);
//					System.out.println(" :::::::::: query ::::::: " + queryStr);

			CommandSearchRequest.setProps(Connection.IP, Connection.PORT, 5000, 50, 50);
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

	// 세이클럽 음악 검색
	public String SayMusicSearch(Map<String, String> params, Map<String, Object> reqHeader,
			HttpServletRequest request) {

		List<Map<String, String>> fieldMap = self.fieldSelector();

		Map<String, String> fieldSelectorMap = fieldMap.get(0);
		Map<String, String> artistSelectorMap = fieldMap.get(1);

		String collection = getCollection(params);

		String req = "";
		req += "Host: " + (String) reqHeader.get("host") + "\n";
		req += "Connection: " + (String) reqHeader.get("connection") + "\n";
		req += "Upgrade-Insecure-Requests: " + (String) reqHeader.get("upgrade-insecure-requests") + "\n";
		req += "User-Agent: " + (String) reqHeader.get("user-agent") + "\n";
		req += "Accept: " + (String) reqHeader.get("accept") + "\n";
		req += "Accept-Encoding: " + (String) reqHeader.get("accept-encoding") + "\n";
		req += "Accept-Language: " + (String) reqHeader.get("accept-language");

		if (params.get("q") != null) {
			if (params.get("q").isEmpty()) {
				return makeEmptyNhnData(params);
			}
			if (parseSize(params) == 0) {
				return makeEmptyNhnData(params);
			}
			String paramQ = parseQ(params);
			String qValue = paramQ.replaceAll("\\s", "");
			String col = getCollection(params);

			if (col.equalsIgnoreCase(Collections.TRACK)) {
				if (fieldSelectorMap.containsKey(qValue) == true) {

					String selectedValue = fieldSelectorMap.get(qValue);

					if (selectedValue.equalsIgnoreCase("track")) {
						track_score_100 = 1000;
						track_score_150 = 1000;
						album_score = 10;
						artist_score = 30;
					} else if (selectedValue.equalsIgnoreCase("album")) {
						track_score_100 = 15;
						track_score_150 = 15;
						album_score = 1000;
						artist_score = 30;
					} else {
						track_score_100 = 15;
						track_score_150 = 15;
						album_score = 10;
						artist_score = 1000;
					}
				} else {
					String[] qSlice = params.get("q").split("\\s+");

					if (qSlice.length > 0) {
						for (int s = 0; s < qSlice.length; s++) {
							for (String key : artistSelectorMap.keySet()) {
								if (key.equalsIgnoreCase(qSlice[s])) {
									artist_keyword = qSlice[s];
								}
							}
						}
					}

					track_score_100 = 100;
					track_score_150 = 150;
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
			query.setSelect(parseSayMusicSelect(params));
			query.setFilter(parseFilter(params, filterFieldParseResult, getCollection(params)));
			query.setWhere(parseWhere(params, filterFieldParseResult, getCollection(params)));
			query.setOrderby(parseOrderBy(params, getCollection(params)));
			query.setFrom(getCollection(params));
			query.setResult(parseStart(params) - 1, parseStart(params) + parseSize(params) - 2);
			query.setSearchKeyword(parseQ(params));
			query.setFaultless(true);
			query.setThesaurusOption((byte) (Protocol.ThesaurusOption.EQUIV_SYNONYM | Protocol.ThesaurusOption.QUASI_SYNONYM));
			query.setSearchOption((byte) (Protocol.SearchOption.BANNED | Protocol.SearchOption.STOPWORD | Protocol.SearchOption.CACHE));
			query.setRankingOption((byte) (Protocol.RankingOption.CATEGORY_RANKING | Protocol.RankingOption.DOCUMENT_RANKING));
			query.setCategoryRankingOption((byte) (Protocol.CategoryRankingOption.EQUIV_SYNONYM	| Protocol.CategoryRankingOption.QUASI_SYNONYM));
			query.setUserName(getUserName(params)); // 로그인 사용자 ID 기록
			query.setExtData(RestUtils.getParam(params, "pr")); // pr (app,web,pc)
			query.setLoggable(getLoggable(RestUtils.getParam(params, "search_tp")));
			query.setLogKeyword(parseQ(params).toCharArray());
			query.setPrintQuery(true); // 실제 사용시 false
			parseTrigger(params, query, getCollection(params));
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
						query.setFilter(parseFilter(params, filterFieldParseResult, getCollection(params)));
						query.setWhere(parseWhere(params, filterFieldParseResult, getCollection(params)));
						query.setOrderby(parseOrderBy(params, getCollection(params)));
						query.setFrom(getCollection(params));
						query.setResult(parseStart(params) - 1, parseStart(params) + parseSize(params) - 2);
						query.setSearchKeyword(parseQ(params));
						query.setFaultless(true);
						query.setThesaurusOption((byte) (Protocol.ThesaurusOption.EQUIV_SYNONYM	| Protocol.ThesaurusOption.QUASI_SYNONYM));
						query.setSearchOption((byte) (Protocol.SearchOption.BANNED | Protocol.SearchOption.STOPWORD	| Protocol.SearchOption.CACHE));
						query.setRankingOption((byte) (Protocol.RankingOption.CATEGORY_RANKING | Protocol.RankingOption.DOCUMENT_RANKING));
						query.setCategoryRankingOption((byte) (Protocol.CategoryRankingOption.EQUIV_SYNONYM	| Protocol.CategoryRankingOption.QUASI_SYNONYM));
						query.setUserName(getUserName(params)); // 로그인 사용자 ID 기록
						query.setExtData(RestUtils.getParam(params, "pr")); // pr (app,web,pc)
						query.setLoggable(getLoggable(RestUtils.getParam(params, "search_tp")));
						query.setLogKeyword(parseQ(params).toCharArray());
						query.setPrintQuery(true); // 실제 사용시 false
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

			String resultJson = gson.toJson(makeSayMusicResult(commandSearchRequest.getResultSet().getResult(0), query, params, collection));

			ret = resultJson;

			logMessageService.receiveEnd(reqHeader, request);

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

		if (collec.equalsIgnoreCase(Collections.TOTAL)) {
			colArray = new String[] { Collections.EXACT_ARTIST, Collections.TRACK, Collections.ALBUM,Collections.ARTIST, Collections.MV, Collections.MUSICCAST, Collections.MUSICPD,Collections.MUSICPOST, Collections.CLASSIC, Collections.LYRICS };
		} else {
			colArray = new String[] { collec };
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
		if (collection.equalsIgnoreCase(Collections.AUTO_TOTAL)) {
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

	private SelectSet[] parseSayMusicSelect(Map<String, String> params) {
		return SayclubMusicSelectSet.getInstance().makeSelectSet(params);
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

	protected List<WhereSet> makeBaseWhereSet(Map<String, String> params, String collection)
			throws InvalidParameterException {
		List<WhereSet> result = new ArrayList<WhereSet>();
		String orgKeyword = parseQ(params);
		String keyword = "";

		String[] matchSp = orgKeyword.split("\\s+");

		if (!orgKeyword.matches(".*[0-9|a-z|A-Z|ㄱ-ㅎ|ㅏ-ㅣ|가-힝].*")) {
			keyword = orgKeyword;
		} else {
			if (matchSp.length > 0) {
				for (int m = 0; m < matchSp.length; m++) {
					if (!matchSp[m].matches(".*[0-9|a-z|A-Z|ㄱ-ㅎ|ㅏ-ㅣ|가-힝].*")) {
						if (m - 1 >= 0) {
							matchSp[m] = matchSp[m];
						} else {
							matchSp[m + 1] = matchSp[m] + matchSp[m + 1];
							matchSp[m] = "";
						}
					} else {
						if (m == 0) {
							matchSp[m] = matchSp[m];
						} else {
							matchSp[m] = " " + matchSp[m];
						}
					}
					keyword += matchSp[m];
				}
			} else {
				keyword = orgKeyword;
			}
		}

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

		if (collection.equalsIgnoreCase(Collections.TRACK)) {
			if (idxField.equalsIgnoreCase("track_idx")) {
				if (qOption.isNofM()) {
					result.add(new WhereSet("TRACK_IDX_WS", qOption.getOption(), trimKeyword, 100,	qOption.getNofmPercent()));
				} else {
					result.add(new WhereSet("TRACK_IDX_WS", qOption.getOption(), trimKeyword, 100));
				}
				trackMap.put("TRACK_IDX", 100);
				trackMap.put("TRACK_IDX_WS", 100);
				trackMap.put("SYN_TRACK_IDX_KO", 30);
				trackMap.put("SYN_TRACK_IDX_WS", 30);
				trackMap.put("SYN_TRACK_IDX", 30);
			} else if (idxField.equalsIgnoreCase("artist_idx")) {
				trackMap.put("ARTIST_IDX", 100);
				trackMap.put("ARTIST_IDX_WS", 100);
				trackMap.put("SYN_ARTIST_IDX_WS", 30);
				trackMap.put("SYN_ARTIST_IDX", 30);
			} else if (idxField.equalsIgnoreCase("album_idx")) {
				trackMap.put("ALBUM_IDX", 100);
				trackMap.put("ALBUM_IDX_WS", 100);
				trackMap.put("SYN_ALBUM_IDX", 30);
				trackMap.put("SYN_ALBUM_IDX_KO", 30);
				trackMap.put("SYN_ALBUM_IDX_WS", 30);
			} else {
				if (!artist_keyword.equalsIgnoreCase("")) {
					if (qOption.isNofM()) {
						result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
						result.add(new WhereSet("TRACK_IDX", Protocol.WhereSet.OP_HASALLONE, keyword, track_score_150, qOption.getNofmPercent()));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("TRACK_IDX_WS", Protocol.WhereSet.OP_HASALLONE, keyword, track_score_100, qOption.getNofmPercent()));
//						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
//						result.add(new WhereSet("TRACK_IDX_WS", qOption.getOption(), trimKeyword, track_score, qOption.getNofmPercent()));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("ARTIST_IDX", qOption.getOption(), keyword, artist_score, qOption.getNofmPercent()));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("ARTIST_IDX_WS", qOption.getOption(), keyword, artist_score, qOption.getNofmPercent()));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("TRACK_ARTIST_ALBUM_IDX", qOption.getOption(), keyword, 30, qOption.getNofmPercent()));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("TRACK_ARTIST_ALBUM_IDX_WS", qOption.getOption(), keyword, 30, qOption.getNofmPercent()));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("TRACK_ARTIST_ALBUM_IDX_WS", qOption.getOption(), trimKeyword, qOption.getNofmPercent()));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("SYN_TRACK_ARTIST_ALBUM_IDX", qOption.getOption(), keyword, 30, qOption.getNofmPercent()));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("EXACT_IDX", qOption.getOption(), trimKeyword, 100, qOption.getNofmPercent()));

						if (album_score == 1000) {
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.add(new WhereSet("ALBUM_IDX", qOption.getOption(), keyword, album_score, qOption.getNofmPercent()));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.add(new WhereSet("ALBUM_IDX_WS", qOption.getOption(), keyword, album_score, qOption.getNofmPercent()));
						}
						result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));

						result.add(new WhereSet(Protocol.WhereSet.OP_WEIGHTAND));

						result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
//						result.add(new WhereSet("ARTIST_IDX", Protocol.WhereSet.OP_WEIGHTAND, artist_keyword, 500, qOption.getNofmPercent()));
//						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("EXACT_ARTIST_IDX", qOption.getOption(), artist_keyword, 200, qOption.getNofmPercent()));
						result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
					} else {
						result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
						result.add(new WhereSet("TRACK_IDX", Protocol.WhereSet.OP_HASALLONE, keyword, track_score_150));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("TRACK_IDX_WS", Protocol.WhereSet.OP_HASALLONE, keyword, track_score_100));
//						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
//						result.add(new WhereSet("TRACK_IDX_WS", qOption.getOption(), trimKeyword, track_score));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("ARTIST_IDX", qOption.getOption(), keyword, artist_score));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("ARTIST_IDX_WS", qOption.getOption(), keyword, artist_score));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("TRACK_ARTIST_ALBUM_IDX", qOption.getOption(), keyword, 30));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("TRACK_ARTIST_ALBUM_IDX_WS", qOption.getOption(), keyword, 30));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("TRACK_ARTIST_ALBUM_IDX_WS", qOption.getOption(), trimKeyword));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("SYN_TRACK_ARTIST_ALBUM_IDX", qOption.getOption(), keyword, 30));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("EXACT_IDX", qOption.getOption(), trimKeyword, 100));

						if (album_score == 1000) {
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.add(new WhereSet("ALBUM_IDX", qOption.getOption(), keyword, album_score));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.add(new WhereSet("ALBUM_IDX_WS", qOption.getOption(), keyword, album_score));
						}
						result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));

						result.add(new WhereSet(Protocol.WhereSet.OP_WEIGHTAND));

						result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
//						result.add(new WhereSet("ARTIST_IDX", qOption.getOption(), artist_keyword, 500));
//						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("EXACT_ARTIST_IDX", qOption.getOption(), artist_keyword, 200));
						result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
					}
					artist_keyword = "";

				} else {
					if (track_score_100 != 0) {
						if (qOption.isNofM()) {
							result.add(new WhereSet("TRACK_IDX", Protocol.WhereSet.OP_HASALLONE, keyword, track_score_150, qOption.getNofmPercent()));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.add(new WhereSet("TRACK_IDX_WS", Protocol.WhereSet.OP_HASALLONE, keyword, track_score_100, qOption.getNofmPercent()));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.add(new WhereSet("TRACK_ARTIST_ALBUM_IDX_WS", qOption.getOption(), keyword, 30, qOption.getNofmPercent()));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.add(new WhereSet("TRACK_ARTIST_ALBUM_IDX_WS", qOption.getOption(), trimKeyword, qOption.getNofmPercent()));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.add(new WhereSet("EXACT_IDX", qOption.getOption(), trimKeyword, 100, qOption.getNofmPercent()));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.add(new WhereSet("EXACT_ARTIST_IDX", qOption.getOption(), trimKeyword, 200, qOption.getNofmPercent()));
						} else {
							result.add(new WhereSet("TRACK_IDX", Protocol.WhereSet.OP_HASALLONE, keyword, track_score_150));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.add(new WhereSet("TRACK_IDX_WS", Protocol.WhereSet.OP_HASALLONE, keyword, track_score_100));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.add(new WhereSet("TRACK_ARTIST_ALBUM_IDX_WS", qOption.getOption(), keyword, 30));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.add(new WhereSet("TRACK_ARTIST_ALBUM_IDX_WS", qOption.getOption(), trimKeyword));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.add(new WhereSet("EXACT_IDX", qOption.getOption(), trimKeyword, 100));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.add(new WhereSet("EXACT_ARTIST_IDX", qOption.getOption(), trimKeyword, 200));
						}
//						trackMap.put("TRACK_IDX", track_score_150);
//						trackMap.put("TRACK_IDX_WS", track_score_100);
						trackMap.put("ARTIST_IDX", artist_score);
						trackMap.put("ARTIST_IDX_WS", artist_score);

						if (album_score == 1000) {
							trackMap.put("ALBUM_IDX", album_score);
							trackMap.put("ALBUM_IDX_WS", album_score);
						}
					}
					trackMap.put("TRACK_ARTIST_ALBUM_IDX", 30);
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
		} else if (collection.equalsIgnoreCase(Collections.LYRICS)) {
			lyricsMap.put("TRACK_IDX", 200);
			lyricsMap.put("TRACK_IDX_WS", 200);
			lyricsMap.put("ARTIST_IDX", 300);
			lyricsMap.put("ARTIST_IDX_WS", 300);
			lyricsMap.put("LYRICS_IDX", 100);
			lyricsMap.put("LYRICS_IDX_WS", 100);
			lyricsMap.put("TOTAL_LYRICS_IDX", 30);
			lyricsMap.put("TOTAL_LYRICS_IDX_WS", 30);

			if (qOption.isNofM()) {
				result.add(new WhereSet("LYRICS_IDX_PH2", qOption.getOption(), keyword, 200, qOption.getNofmPercent()));
				result.add(new WhereSet(Protocol.WhereSet.OP_OR));
				result.add(new WhereSet("LYRICS_IDX_PH2", qOption.getOption(), keyword, 200, qOption.getNofmPercent()));
			} else {
				result.add(new WhereSet("LYRICS_IDX_PH3", qOption.getOption(), keyword, 200));
				result.add(new WhereSet(Protocol.WhereSet.OP_OR));
				result.add(new WhereSet("LYRICS_IDX_PH3", qOption.getOption(), keyword, 200));
			}

			for (Entry<String, Integer> e : lyricsMap.entrySet()) {
				if (result.size() > 0) {
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
				}
				if (qOption.isNofM()) {
					result.add(new WhereSet(e.getKey(), qOption.getOption(), keyword, e.getValue(),
							qOption.getNofmPercent()));
				} else {
					result.add(new WhereSet(e.getKey(), qOption.getOption(), keyword, e.getValue()));
				}
			}
		} else if (collection.equalsIgnoreCase(Collections.ALBUM)) {
			if (idxField.equalsIgnoreCase("album_idx")) {
				albumMap.put("ALBUM_IDX", 100);
				albumMap.put("ALBUM_IDX_WS", 100);
				albumMap.put("SYN_ALBUM_IDX", 30);
				albumMap.put("SYN_ALBUM_IDX_KO", 30);
				albumMap.put("SYN_ALBUM_IDX_WS", 30);
			} else if (idxField.equalsIgnoreCase("artist_idx")) {
				albumMap.put("ARTIST_IDX", 100);
				albumMap.put("ARTIST_IDX_WS", 100);
				albumMap.put("SYN_ARTIST_IDX", 30);
				albumMap.put("SYN_ARTIST_IDX_WS", 30);
			} else {
				albumMap.put("ALBUM_IDX", 100);
				albumMap.put("ALBUM_IDX_WS", 100);
				albumMap.put("ARTIST_IDX", 100);
				albumMap.put("ARTIST_IDX_WS", 100);
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
		} else if (collection.equalsIgnoreCase(Collections.ARTIST)) {
			if (idxField.equalsIgnoreCase("exact_artist_idx")) {
				if (qOption.isNofM()) {
					result.add(new WhereSet("EXACT_ARTIST_IDX", qOption.getOption(), trimKeyword, 100, qOption.getNofmPercent()));
				} else {
					result.add(new WhereSet("EXACT_ARTIST_IDX", qOption.getOption(), trimKeyword, 100));
				}

			} else {
				if (qOption.isNofM()) {
					result.add(new WhereSet("EXACT_ARTIST_IDX", qOption.getOption(), trimKeyword, 100, qOption.getNofmPercent()));
				} else {
					result.add(new WhereSet("EXACT_ARTIST_IDX", qOption.getOption(), trimKeyword, 100));
				}
				artistMap.put("ARTIST_IDX", 100);
				artistMap.put("ARTIST_IDX_WS", 100);
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
			}

		} else if (collection.equalsIgnoreCase(Collections.MV)) {
			mvMap.put("MV_TRACK_IDX", 100);
			mvMap.put("MV_TRACK_IDX_WS", 100);
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
		} else if (collection.equalsIgnoreCase(Collections.MUSICCAST)) {
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
		} else if (collection.equalsIgnoreCase(Collections.MUSICPD)) {
			if (idxField.equalsIgnoreCase("musicpd_album_idx")) {
				musicpdMap.put("TITLE_IDX", 1000);
				musicpdMap.put("TITLE_IDX_WS", 1000);
				musicpdMap.put("MUSICPD_ALBUM_IDX", 30);
				musicpdMap.put("MUSICPD_ALBUM_IDX_WS", 30);
			} else {
				musicpdMap.put("TITLE_IDX", 1000);
				musicpdMap.put("TITLE_IDX_WS", 1000);
				musicpdMap.put("MUSICPD_ALBUM_IDX", 30);
				musicpdMap.put("MUSICPD_ALBUM_IDX_WS", 30);
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
		} else if (collection.equalsIgnoreCase(Collections.MUSICPOST)) {
			musicpostMap.put("MUSICPOST_IDX", 10);
			musicpostMap.put("MUSICPOST_IDX_WS", 10);
			musicpostMap.put("ARTIST_NM_IDX", 100);
			musicpostMap.put("ARTIST_NM_IDX_WS", 100);
			musicpostMap.put("TITLE_IDX", 300);
			musicpostMap.put("TITLE_IDX_WS", 300);

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
		} else if (collection.equalsIgnoreCase(Collections.CLASSIC)) {
			String classic_kwd = keyword.replaceAll("\\s", "");

			if (!CSartist_keyword.equalsIgnoreCase("")) {
				if (qOption.isNofM()) {
					result.add(new WhereSet("ARTIST_IDX", qOption.getOption(), classic_kwd, 300, qOption.getNofmPercent()));
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					result.add(new WhereSet("ARTIST_IDX", qOption.getOption(), keyword, 300, qOption.getNofmPercent()));
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					result.add(new WhereSet("TITLE_IDX", qOption.getOption(), keyword, 50, qOption.getNofmPercent()));
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					result.add(new WhereSet("TITLE_IDX", qOption.getOption(), classic_kwd, 50, qOption.getNofmPercent()));
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					result.add(new WhereSet("TITLE_IDX_KOR", qOption.getOption(), keyword, 50, qOption.getNofmPercent()));
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					result.add(new WhereSet("SECTION_TITLE_IDX", qOption.getOption(), CSartist_keyword, 100,
							qOption.getNofmPercent()));
				} else {
					result.add(new WhereSet("ARTIST_IDX", qOption.getOption(), classic_kwd, 300));
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					result.add(new WhereSet("ARTIST_IDX", qOption.getOption(), keyword, 300));
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					result.add(new WhereSet("TITLE_IDX", qOption.getOption(), keyword, 50));
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					result.add(new WhereSet("TITLE_IDX", qOption.getOption(), classic_kwd, 50));
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					result.add(new WhereSet("TITLE_IDX_KOR", qOption.getOption(), keyword, 50));
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					result.add(new WhereSet("SECTION_TITLE_IDX", qOption.getOption(), CSartist_keyword, 100));
				}
				classicMap.put("CLASSIC_IDX", 30);
				classicMap.put("CLASSIC_IDX_KOR", 30);

				CSartist_keyword = "";
			} else {
				if (qOption.isNofM()) {
					result.add(new WhereSet("ARTIST_IDX", qOption.getOption(), classic_kwd, 100,
							qOption.getNofmPercent()));
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					result.add(new WhereSet("ARTIST_IDX", qOption.getOption(), keyword, 100, qOption.getNofmPercent()));
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					result.add(new WhereSet("TITLE_IDX", qOption.getOption(), keyword, 50, qOption.getNofmPercent()));
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					result.add(new WhereSet("TITLE_IDX", qOption.getOption(), classic_kwd, 50, qOption.getNofmPercent()));
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					result.add(new WhereSet("TITLE_IDX_KOR", qOption.getOption(), keyword, 50, qOption.getNofmPercent()));
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					result.add(new WhereSet("SECTION_TITLE_IDX", qOption.getOption(), classic_kwd, 100, qOption.getNofmPercent()));
				} else {
					result.add(new WhereSet("ARTIST_IDX", qOption.getOption(), classic_kwd, 100));
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					result.add(new WhereSet("ARTIST_IDX", qOption.getOption(), keyword, 100));
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					result.add(new WhereSet("TITLE_IDX", qOption.getOption(), keyword, 50));
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					result.add(new WhereSet("TITLE_IDX", qOption.getOption(), classic_kwd, 50));
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					result.add(new WhereSet("TITLE_IDX_KOR", qOption.getOption(), keyword, 50));
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					result.add(new WhereSet("SECTION_TITLE_IDX", qOption.getOption(), classic_kwd, 100));
				}
				classicMap.put("CLASSIC_IDX", 30);
				classicMap.put("CLASSIC_IDX_KOR", 30);
			}

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
		} else if (collection.equalsIgnoreCase(Collections.ENTITY)) {
			if (idxField.equalsIgnoreCase("track_idx")) {
				entityMap.put("TRACK_IDX", 100);
			} else if (idxField.equalsIgnoreCase("album_idx")) {
				entityMap.put("ALBUM_IDX", 100);
			} else if (idxField.equalsIgnoreCase("artist_idx")) {
				entityMap.put("ARTIST_IDX", 100);
			} else if (idxField.equalsIgnoreCase("arranger_idx")) {
				entityMap.put("ARRANGER_IDX", 100);
			} else if (idxField.equalsIgnoreCase("composer_idx")) {
				entityMap.put("COMPOSER_IDX", 100);
			} else if (idxField.equalsIgnoreCase("featuring_idx")) {
				entityMap.put("FEATURING_IDX", 100);
			} else if (idxField.equalsIgnoreCase("lyricist_idx")) {
				entityMap.put("LYRICIST_IDX", 100);
			} else if (idxField.equalsIgnoreCase("genre_idx")) {
				entityMap.put("GENRE_IDX", 100);
			} else if (idxField.equalsIgnoreCase("artist_role_idx")) {
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

	protected WhereSet[] parseWhere2(Map<String, String> params, FilterFieldParseResult filterFieldParseResult, String collection) throws InvalidParameterException {
		return WhereSetService.getInstance().makeWhereSet(params, filterFieldParseResult, makeBaseWhereSet2(params, collection));
	}

	protected List<WhereSet> makeBaseWhereSet2(Map<String, String> params, String collection) throws InvalidParameterException {
		List<WhereSet> result = new ArrayList<WhereSet>();
		String orgKeyword = parseQ(params);
		String keyword = "";

		String[] matchSp = orgKeyword.split("\\s+");

		if (!orgKeyword.matches(".*[0-9|a-z|A-Z|ㄱ-ㅎ|ㅏ-ㅣ|가-힝].*")) {
			keyword = orgKeyword;
		} else {
			if (matchSp.length > 0) {
				for (int m = 0; m < matchSp.length; m++) {
					if (!matchSp[m].matches(".*[0-9|a-z|A-Z|ㄱ-ㅎ|ㅏ-ㅣ|가-힝].*")) {
						if (m - 1 >= 0) {
							matchSp[m] = matchSp[m];
						} else {
							matchSp[m + 1] = matchSp[m] + matchSp[m + 1];
							matchSp[m] = "";
						}
					} else {
						if (m == 0) {
							matchSp[m] = matchSp[m];
						} else {
							matchSp[m] = " " + matchSp[m];
						}
					}
					keyword += matchSp[m];
				}
			} else {
				keyword = orgKeyword;
			}
		}

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

		if (collection.equalsIgnoreCase(Collections.TRACK)) {
			if (idxField.equalsIgnoreCase("track_idx")) {
				if (qOption.isNofM()) {
					result.add(new WhereSet("TRACK_IDX_WS", qOption.getOption(), trimKeyword, 100, qOption.getNofmPercent()));
				} else {
					result.add(new WhereSet("TRACK_IDX_WS", qOption.getOption(), trimKeyword, 100));
				}
				trackMap.put("TRACK_IDX", 100);
				trackMap.put("TRACK_IDX_WS", 100);
				trackMap.put("SYN_TRACK_IDX_KO", 30);
				trackMap.put("SYN_TRACK_IDX_WS", 30);
				trackMap.put("SYN_TRACK_IDX", 30);
			} else if (idxField.equalsIgnoreCase("artist_idx")) {
				trackMap.put("ARTIST_IDX", 100);
				trackMap.put("ARTIST_IDX_WS", 100);
				trackMap.put("SYN_ARTIST_IDX_WS", 30);
				trackMap.put("SYN_ARTIST_IDX", 30);
			} else if (idxField.equalsIgnoreCase("album_idx")) {
				trackMap.put("ALBUM_IDX", 100);
				trackMap.put("ALBUM_IDX_WS", 100);
				trackMap.put("SYN_ALBUM_IDX", 30);
				trackMap.put("SYN_ALBUM_IDX_KO", 30);
				trackMap.put("SYN_ALBUM_IDX_WS", 30);
			} else {
				if (!artist_keyword.equalsIgnoreCase("")) {
					if (qOption.isNofM()) {
						result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
						result.add(new WhereSet("TRACK_IDX", Protocol.WhereSet.OP_HASALLONE, keyword, track_score_150, qOption.getNofmPercent()));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("TRACK_IDX_WS", Protocol.WhereSet.OP_HASALLONE, keyword, track_score_100, qOption.getNofmPercent()));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("ARTIST_IDX", qOption.getOption(), keyword, artist_score, qOption.getNofmPercent()));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("ARTIST_IDX_WS", qOption.getOption(), keyword, artist_score, qOption.getNofmPercent()));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("TRACK_ARTIST_ALBUM_IDX", qOption.getOption(), keyword, 30, qOption.getNofmPercent()));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("TRACK_ARTIST_ALBUM_IDX_WS", qOption.getOption(), keyword, 30, qOption.getNofmPercent()));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("TRACK_ARTIST_ALBUM_IDX_WS", qOption.getOption(), trimKeyword, qOption.getNofmPercent()));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("SYN_TRACK_ARTIST_ALBUM_IDX", qOption.getOption(), keyword, 30, qOption.getNofmPercent()));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("EXACT_IDX", qOption.getOption(), trimKeyword, 100, qOption.getNofmPercent()));

						if (album_score == 1000) {
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.add(new WhereSet("ALBUM_IDX", qOption.getOption(), keyword, album_score, qOption.getNofmPercent()));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.add(new WhereSet("ALBUM_IDX_WS", qOption.getOption(), keyword, album_score, qOption.getNofmPercent()));
						}
						result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));

						result.add(new WhereSet(Protocol.WhereSet.OP_WEIGHTAND));

						result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
						result.add(new WhereSet("EXACT_ARTIST_IDX", qOption.getOption(), artist_keyword, 200, qOption.getNofmPercent()));
						result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
					} else {
						result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
						result.add(new WhereSet("TRACK_IDX", Protocol.WhereSet.OP_HASALLONE, keyword, track_score_150));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("TRACK_IDX_WS", Protocol.WhereSet.OP_HASALLONE, keyword, track_score_100));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("ARTIST_IDX", qOption.getOption(), keyword, artist_score));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("ARTIST_IDX_WS", qOption.getOption(), keyword, artist_score));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("TRACK_ARTIST_ALBUM_IDX", qOption.getOption(), keyword, 30));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("TRACK_ARTIST_ALBUM_IDX_WS", qOption.getOption(), keyword, 30));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("TRACK_ARTIST_ALBUM_IDX_WS", qOption.getOption(), trimKeyword));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("SYN_TRACK_ARTIST_ALBUM_IDX", qOption.getOption(), keyword, 30));
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
						result.add(new WhereSet("EXACT_IDX", qOption.getOption(), trimKeyword, 100));

						if (album_score == 1000) {
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.add(new WhereSet("ALBUM_IDX", qOption.getOption(), keyword, album_score));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.add(new WhereSet("ALBUM_IDX_WS", qOption.getOption(), keyword, album_score));
						}
						result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));

						result.add(new WhereSet(Protocol.WhereSet.OP_WEIGHTAND));

						result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
						result.add(new WhereSet("EXACT_ARTIST_IDX", qOption.getOption(), artist_keyword, 200));
						result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
					}
					artist_keyword = "";

				} else {
					if (track_score_100 != 0) {
						if (qOption.isNofM()) {
							result.add(new WhereSet("TRACK_IDX", Protocol.WhereSet.OP_HASALLONE, keyword, track_score_150, qOption.getNofmPercent()));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.add(new WhereSet("TRACK_IDX_WS", Protocol.WhereSet.OP_HASALLONE, keyword, track_score_100, qOption.getNofmPercent()));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.add(new WhereSet("TRACK_ARTIST_ALBUM_IDX_WS", qOption.getOption(), keyword, 30, qOption.getNofmPercent()));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.add(new WhereSet("TRACK_ARTIST_ALBUM_IDX_WS", qOption.getOption(), trimKeyword, qOption.getNofmPercent()));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.add(new WhereSet("EXACT_IDX", qOption.getOption(), trimKeyword, 100, qOption.getNofmPercent()));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.add(new WhereSet("EXACT_ARTIST_IDX", qOption.getOption(), trimKeyword, 200, qOption.getNofmPercent()));
						} else {
							result.add(new WhereSet("TRACK_IDX", Protocol.WhereSet.OP_HASALLONE, keyword, track_score_150));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.add(new WhereSet("TRACK_IDX_WS", Protocol.WhereSet.OP_HASALLONE, keyword, track_score_100));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.add(new WhereSet("TRACK_ARTIST_ALBUM_IDX_WS", qOption.getOption(), keyword, 30));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.add(new WhereSet("TRACK_ARTIST_ALBUM_IDX_WS", qOption.getOption(), trimKeyword));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.add(new WhereSet("EXACT_IDX", qOption.getOption(), trimKeyword, 100));
							result.add(new WhereSet(Protocol.WhereSet.OP_OR));
							result.add(new WhereSet("EXACT_ARTIST_IDX", qOption.getOption(), trimKeyword, 200));
						}
						trackMap.put("ARTIST_IDX", artist_score);
						trackMap.put("ARTIST_IDX_WS", artist_score);

						if (album_score == 1000) {
							trackMap.put("ALBUM_IDX", album_score);
							trackMap.put("ALBUM_IDX_WS", album_score);
						}
					}
					trackMap.put("TRACK_ARTIST_ALBUM_IDX", 30);
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
		} else if (collection.equalsIgnoreCase(Collections.LYRICS)) {
			lyricsMap.put("TRACK_IDX", 200);
			lyricsMap.put("TRACK_IDX_WS", 200);
			lyricsMap.put("ARTIST_IDX", 300);
			lyricsMap.put("ARTIST_IDX_WS", 300);
			lyricsMap.put("LYRICS_IDX", 100);
			lyricsMap.put("LYRICS_IDX_WS", 100);
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
		} else if (collection.equalsIgnoreCase(Collections.ALBUM)) {
			if (idxField.equalsIgnoreCase("album_idx")) {
				albumMap.put("ALBUM_IDX", 100);
				albumMap.put("ALBUM_IDX_WS", 100);
				albumMap.put("SYN_ALBUM_IDX", 30);
				albumMap.put("SYN_ALBUM_IDX_KO", 30);
				albumMap.put("SYN_ALBUM_IDX_WS", 30);
			} else if (idxField.equalsIgnoreCase("artist_idx")) {
				albumMap.put("ARTIST_IDX", 100);
				albumMap.put("ARTIST_IDX_WS", 100);
				albumMap.put("SYN_ARTIST_IDX", 30);
				albumMap.put("SYN_ARTIST_IDX_WS", 30);
			} else {
				albumMap.put("ALBUM_IDX", 100);
				albumMap.put("ALBUM_IDX_WS", 100);
				albumMap.put("ARTIST_IDX", 100);
				albumMap.put("ARTIST_IDX_WS", 100);
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
		} else if (collection.equalsIgnoreCase(Collections.ARTIST)) {
			if (idxField.equalsIgnoreCase("exact_artist_idx")) {
				if (qOption.isNofM()) {
					result.add(new WhereSet("EXACT_ARTIST_IDX", qOption.getOption(), trimKeyword, 100, qOption.getNofmPercent()));
				} else {
					result.add(new WhereSet("EXACT_ARTIST_IDX", qOption.getOption(), trimKeyword, 100));
				}

			} else {
				artistMap.put("ARTIST_IDX", 100);
				artistMap.put("ARTIST_IDX_WS", 100);
				artistMap.put("GRP_NM_IDX", 100);
				artistMap.put("GRP_NM_IDX_WS", 100);
				artistMap.put("SYN_ARTIST_IDX_KO", 10);
				artistMap.put("SYN_ARTIST_IDX", 10);

				for (Entry<String, Integer> e : artistMap.entrySet()) {
					if (result.size() > 0) {
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					}
					if (qOption.isNofM()) {
						result.add(new WhereSet(e.getKey(), qOption.getOption(), keyword, e.getValue(),
								qOption.getNofmPercent()));
					} else {
						result.add(new WhereSet(e.getKey(), qOption.getOption(), keyword, e.getValue()));
					}
				}
			}

		} else if (collection.equalsIgnoreCase(Collections.MV)) {
			mvMap.put("MV_TRACK_IDX", 100);
			mvMap.put("MV_TRACK_IDX_WS", 100);
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
		} else if (collection.equalsIgnoreCase(Collections.MUSICCAST)) {
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
		} else if (collection.equalsIgnoreCase(Collections.MUSICPD)) {
			if (idxField.equalsIgnoreCase("musicpd_album_idx")) {
				musicpdMap.put("TITLE_IDX", 1000);
				musicpdMap.put("TITLE_IDX_WS", 1000);
				musicpdMap.put("MUSICPD_ALBUM_IDX", 30);
				musicpdMap.put("MUSICPD_ALBUM_IDX_WS", 30);
			} else {
				musicpdMap.put("TITLE_IDX", 1000);
				musicpdMap.put("TITLE_IDX_WS", 1000);
				musicpdMap.put("MUSICPD_ALBUM_IDX", 30);
				musicpdMap.put("MUSICPD_ALBUM_IDX_WS", 30);
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
		} else if (collection.equalsIgnoreCase(Collections.MUSICPOST)) {
//			musicpostMap.put("MUSICPOST_IDX", 30);
			musicpostMap.put("MUSICPOST_IDX_WS", 30);
//			musicpostMap.put("TITLE_IDX", 200);
			musicpostMap.put("TITLE_IDX_WS", 200);

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
		} else if (collection.equalsIgnoreCase(Collections.CLASSIC)) {
			String classic_kwd = keyword.replaceAll("\\s", "");

			if (!CSartist_keyword.equalsIgnoreCase("")) {
				if (qOption.isNofM()) {
					result.add(new WhereSet("ARTIST_IDX", qOption.getOption(), classic_kwd, 300, qOption.getNofmPercent()));
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					result.add(new WhereSet("ARTIST_IDX", qOption.getOption(), keyword, 300, qOption.getNofmPercent()));
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					result.add(new WhereSet("TITLE_IDX", qOption.getOption(), keyword, 50, qOption.getNofmPercent()));
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					result.add(new WhereSet("TITLE_IDX", qOption.getOption(), classic_kwd, 50, qOption.getNofmPercent()));
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					result.add(new WhereSet("TITLE_IDX_KOR", qOption.getOption(), keyword, 50, qOption.getNofmPercent()));
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					result.add(new WhereSet("SECTION_TITLE_IDX", qOption.getOption(), CSartist_keyword, 100, qOption.getNofmPercent()));
				} else {
					result.add(new WhereSet("ARTIST_IDX", qOption.getOption(), classic_kwd, 300));
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					result.add(new WhereSet("ARTIST_IDX", qOption.getOption(), keyword, 300));
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					result.add(new WhereSet("TITLE_IDX", qOption.getOption(), keyword, 50));
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					result.add(new WhereSet("TITLE_IDX", qOption.getOption(), classic_kwd, 50));
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					result.add(new WhereSet("TITLE_IDX_KOR", qOption.getOption(), keyword, 50));
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					result.add(new WhereSet("SECTION_TITLE_IDX", qOption.getOption(), CSartist_keyword, 100));
				}
				classicMap.put("CLASSIC_IDX", 30);
				classicMap.put("CLASSIC_IDX_KOR", 30);

				CSartist_keyword = "";
			} else {
				if (qOption.isNofM()) {
					result.add(new WhereSet("ARTIST_IDX", qOption.getOption(), classic_kwd, 100, qOption.getNofmPercent()));
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					result.add(new WhereSet("ARTIST_IDX", qOption.getOption(), keyword, 100, qOption.getNofmPercent()));
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					result.add(new WhereSet("TITLE_IDX", qOption.getOption(), keyword, 50, qOption.getNofmPercent()));
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					result.add(new WhereSet("TITLE_IDX", qOption.getOption(), classic_kwd, 50, qOption.getNofmPercent()));
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					result.add(new WhereSet("TITLE_IDX_KOR", qOption.getOption(), keyword, 50, qOption.getNofmPercent()));
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					result.add(new WhereSet("SECTION_TITLE_IDX", qOption.getOption(), classic_kwd, 100, qOption.getNofmPercent()));
				} else {
					result.add(new WhereSet("ARTIST_IDX", qOption.getOption(), classic_kwd, 100));
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					result.add(new WhereSet("ARTIST_IDX", qOption.getOption(), keyword, 100));
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					result.add(new WhereSet("TITLE_IDX", qOption.getOption(), keyword, 50));
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					result.add(new WhereSet("TITLE_IDX", qOption.getOption(), classic_kwd, 50));
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					result.add(new WhereSet("TITLE_IDX_KOR", qOption.getOption(), keyword, 50));
					result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					result.add(new WhereSet("SECTION_TITLE_IDX", qOption.getOption(), classic_kwd, 100));
				}
				classicMap.put("CLASSIC_IDX", 30);
				classicMap.put("CLASSIC_IDX_KOR", 30);
			}

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
		} else if (collection.equalsIgnoreCase(Collections.ENTITY)) {
			if (idxField.equalsIgnoreCase("track_idx")) {
				entityMap.put("TRACK_IDX", 100);
			} else if (idxField.equalsIgnoreCase("album_idx")) {
				entityMap.put("ALBUM_IDX", 100);
			} else if (idxField.equalsIgnoreCase("artist_idx")) {
				entityMap.put("ARTIST_IDX", 100);
			} else if (idxField.equalsIgnoreCase("arranger_idx")) {
				entityMap.put("ARRANGER_IDX", 100);
			} else if (idxField.equalsIgnoreCase("composer_idx")) {
				entityMap.put("COMPOSER_IDX", 100);
			} else if (idxField.equalsIgnoreCase("featuring_idx")) {
				entityMap.put("FEATURING_IDX", 100);
			} else if (idxField.equalsIgnoreCase("lyricist_idx")) {
				entityMap.put("LYRICIST_IDX", 100);
			} else if (idxField.equalsIgnoreCase("genre_idx")) {
				entityMap.put("GENRE_IDX", 100);
			} else if (idxField.equalsIgnoreCase("artist_role_idx")) {
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

		for (int i = 0; i < index.size(); i++) {
			if (num == Integer.parseInt(index.get(i).get("num"))) {
				if (index.get(i).get("name").equalsIgnoreCase("track_artist_album_idx")) {
					q = index.get(i).get("query");
					operand = index.get(i).get("operand");

					if (operand.startsWith("nofm")) {
						option = Protocol.WhereSet.OP_N_OF_M;
						DnofmNum = Double.parseDouble(String.valueOf(index.get(i).get("nofm")));

						if (DnofmNum < 0) {
							DnofmNum = 0;
						} else if (DnofmNum > 1) {
							DnofmNum = 1;
						}

						nofmNum = (int) (DnofmNum * 100);

						if (result.size() > 0) {
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

						if (result.size() > 0) {
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
					if (index.get(i).get("name").equalsIgnoreCase("track_idx")) {
						tarckQ = index.get(i).get("query");
						operand = index.get(i).get("operand");

						if (operand.startsWith("nofm")) {
							option = Protocol.WhereSet.OP_N_OF_M;
							DnofmNum = Double.parseDouble(String.valueOf(index.get(i).get("nofm")));

							if (DnofmNum < 0) {
								DnofmNum = 0;
							} else if (DnofmNum > 1) {
								DnofmNum = 1;
							}

							nofmNum = (int) (DnofmNum * 100);

							if (result.size() > 0) {
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

							if (result.size() > 0) {
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

							if (DnofmNum < 0) {
								DnofmNum = 0;
							} else if (DnofmNum > 1) {
								DnofmNum = 1;
							}

							nofmNum = (int) (DnofmNum * 100);
							if (result.size() > 0) {
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

							if (result.size() > 0) {
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

		if (collection.equalsIgnoreCase(Collections.TRACK)) {
			if (name.equalsIgnoreCase("artist_idx")) {
				idxScoreMap.put("ARTIST_IDX", 100);
				idxScoreMap.put("ARTIST_IDX_WS", 100);
				idxScoreMap.put("SYN_ARTIST_IDX", 30);
				idxScoreMap.put("SYN_ARTIST_IDX_WS", 30);
			} else if (name.equalsIgnoreCase("album_idx")) {
				idxScoreMap.put("ALBUM_IDX", 100);
				idxScoreMap.put("ALBUM_IDX_WS", 100);
				idxScoreMap.put("SYN_ALBUM_IDX", 30);
				idxScoreMap.put("SYN_ALBUM_IDX_KO", 30);
				idxScoreMap.put("SYN_ALBUM_IDX_WS", 30);
			} else if (name.equalsIgnoreCase("track_idx")) {
				idxScoreMap.put("TRACK_IDX", 100);
				idxScoreMap.put("TRACK_IDX_WS", 100);
				idxScoreMap.put("SYN_TRACK_IDX", 30);
				idxScoreMap.put("SYN_TRACK_IDX_KO", 30);
				idxScoreMap.put("SYN_TRACK_IDX_WS", 30);
			} else {
				idxScoreMap.put("TRACK_ARTIST_ALBUM_IDX", 100);
				idxScoreMap.put("TRACK_ARTIST_ALBUM_IDX_WS", 100);
				idxScoreMap.put("SYN_TRACK_ARTIST_ALBUM_IDX", 30);
			}
		} else if (collection.equalsIgnoreCase(Collections.MV)) {
			if (name.equalsIgnoreCase("mv_idx")) {
				idxScoreMap.put("MV_IDX", 100);
				idxScoreMap.put("MV_IDX_WS", 100);
			} else if (name.equalsIgnoreCase("artist_idx")) {
				idxScoreMap.put("ARTIST_IDX", 100);
				idxScoreMap.put("ARTIST_IDX_WS", 100);
			} else {
				idxScoreMap.put("MV_TRACK_ARTIST_ALBUM_IDX", 100);
				idxScoreMap.put("MV_TRACK_ARTIST_ALBUM_IDX_WS", 100);
				idxScoreMap.put("SYN_MV_TRACK_ARTIST_ALBUM_IDX", 30);
			}
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

		for (int i = 0; i < index.size(); i++) {
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
				if (type.equalsIgnoreCase("intersection")) {
					if (operand.startsWith("and")) {
						result.add(new WhereSet(Protocol.WhereSet.OP_AND));
					} else if (operand.startsWith("or")) {
						result.add(new WhereSet(Protocol.WhereSet.OP_OR));
					} else {
						result.add(new WhereSet(Protocol.WhereSet.OP_AND));
					}
				} else if (type.equalsIgnoreCase("exclusion")) {
					result.add(new WhereSet(Protocol.WhereSet.OP_NOT));
				}
			}

			if (type.equalsIgnoreCase("intersection")) {
				if (name.toUpperCase().equalsIgnoreCase("ALBUM_IDX")) {
					if (keyword.matches("[0-9]+집") == true) {
						result.add(new WhereSet("EDITION_NO", option, keyword, 100));
					} else {
						result.add(new WhereSet(name.toUpperCase(), option, keyword, 100));
					}
				} else {
					if (operand.startsWith("nofm")) {
						result.add(new WhereSet(name.toUpperCase(), option, keyword, 100, 100));
					} else {
						result.add(new WhereSet(name.toUpperCase(), option, keyword, 100));
					}

				}
			} else if (type.equalsIgnoreCase("exclusion")) {
				result.add(new WhereSet(name.toUpperCase(), option, keyword));
			}
		}
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

		if (collection.equalsIgnoreCase(Collections.AUTO_TAG)) {
			idxScoreMap.put("FKEY_NOSP", 100);
			idxScoreMap.put("FKEY", 50);
			idxScoreMap.put("BKEY", 30);
		} else {
			if (num == 0) {
				result.add(new WhereSet("ARTIST_IDX", Protocol.WhereSet.OP_HASALL, keyword.replaceAll("\\s", ""), 100));
//				idxScoreMap.put("ARTIST_IDX", 100);
			} else {
//				idxScoreMap.put("ARTR_IDX", 1000); 
//				idxScoreMap.put("FKEY_NOSP", 100);
//				idxScoreMap.put("FKEY", 50);
//				idxScoreMap.put("BKEY", 30);

				result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
				result.add(new WhereSet("FKEY", Protocol.WhereSet.OP_HASALL, keyword.replaceAll("\\s", ""), 1000));
				result.add(new WhereSet(Protocol.WhereSet.OP_OR));
				result.add(new WhereSet("BKEY", Protocol.WhereSet.OP_HASALL, keyword.replaceAll("\\s", ""), 0));
				result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
				result.add(new WhereSet(Protocol.WhereSet.OP_NOT));
				result.add(new WhereSet("ARTIST_IDX", Protocol.WhereSet.OP_HASALL, keyword.replaceAll("\\s", "")));
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

	protected WhereSet[] parseAutoWhere2(Map<String, String> params, FilterFieldParseResult filterFieldParseResult, String collection, int num, String kwd) throws InvalidParameterException {
		return WhereSetService.getInstance().makeWhereSet(params, filterFieldParseResult,
				makeAutoWhereSet2(params, collection, num, kwd));
	}

	protected List<WhereSet> makeAutoWhereSet2(Map<String, String> params, String collection, int num, String kwd) throws InvalidParameterException {
		List<WhereSet> result = new ArrayList<WhereSet>();
		String keyword = parseQ(params);

		searchQoption qOption = new searchQoption(RestUtils.getParam(params, "q_option"), collection);

		idxScoreMap = new HashMap<String, Integer>();

		if (collection.equalsIgnoreCase(Collections.AUTO_TAG)) {
			idxScoreMap.put("FKEY_NOSP", 100);
			idxScoreMap.put("FKEY", 50);
			idxScoreMap.put("BKEY", 30);
		} else {
			if (num == 0) {
				result.add(new WhereSet("ARTIST_IDX", Protocol.WhereSet.OP_HASALL, kwd.replaceAll("\\s", ""), 100));
			} else {
				result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
				result.add(new WhereSet("FKEY", Protocol.WhereSet.OP_HASALL, keyword.replaceAll("\\s", ""), 1000));
				result.add(new WhereSet(Protocol.WhereSet.OP_OR));
				result.add(new WhereSet("BKEY", Protocol.WhereSet.OP_HASALL, keyword.replaceAll("\\s", ""), 0));
				result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
				result.add(new WhereSet(Protocol.WhereSet.OP_NOT));
				result.add(new WhereSet("ARTIST_IDX", Protocol.WhereSet.OP_HASALL, keyword.replaceAll("\\s", "")));
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
			result.add(new GroupBySet(g.toUpperCase(),
					(byte) (Protocol.GroupBySet.OP_COUNT | Protocol.GroupBySet.ORDER_COUNT), "DESC"));
		}
		return result.toArray(new GroupBySet[result.size()]);
	}

	protected OrderBySet[] parseOrderBy(Map<String, String> params, String collection) {
		return new OrderBySet[] {OrderBySetService.getInstance().getOrderBySet(RestUtils.getParam(params, "sort"), collection) };
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

		if (collection.equalsIgnoreCase(Collections.AUTO_TAG)) {
			if (size.equals("")) {
				return 10;
			} else {
				return Integer.parseInt(size);
			}
		} else {
			if (num == 0) {
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

	protected boolean getAutoLoggable(String value) {
		if (value.equalsIgnoreCase("sb")) {
			return true;
		} else if (value.equalsIgnoreCase("ign")) {
			return false;
		}
		return false;
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

	protected TotalsearchResult makeTotalsearchResult(List<Map<Integer, Object>> totalList) throws IRException {
		return TotalsearchResult.makeTotalsearchResult(totalList);
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

	protected SayMusicResult makeSayMusicResult(Result result, Query query, Map<String, String> params, String collection) throws IRException {
		return SayMusicResult.makeSayMusicResult(query, result, params, collection);
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
