package com.diquest.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.diquest.ir.rest.json.gson.GsonLoader;
import com.diquest.ir.rest.json.object.JsonUnknownUriResult;
import com.diquest.ir.rest.json.reponse.ResponseMaker;
import com.diquest.ir.rest.util.RestUtils;
import com.diquest.rest.nhn.common.Collections;
import com.diquest.rest.nhn.domain.Param;
import com.diquest.ir.common.msg.protocol.query.WhereSet;
import com.diquest.ir.rest.common.constant.HttpStatus;
import com.diquest.service.BugsRestService;

@RestController
public class BugsRestController {
	
	private BugsRestService bugsRestService;
	
	public static String APP_KEY_TRACK = "lZjTO0HlFg91HwD";
	public static String APP_KEY_LYRICS = "M2NjMWRjOWMwOGN";
	public static String APP_KEY_ALBUM = "vbmKja0BuYy35Ok";
	public static String APP_KEY_ARTIST = "7S4pV1yEaFoWJsj";
	public static String APP_KEY_MV = "58IHeEjp2lyoR4M";
	public static String APP_KEY_MCAST = "7ZEz9GaqpMRc5DH";
	public static String APP_KEY_MPD = "6Jfys7XEvdQ0KuU";
	public static String APP_KEY_MPOST = "SVmCkyjdYM9n3go";
	public static String APP_KEY_CLASSIC = "0WleItZOyGJbrVF";
	public static String APP_KEY_ENTITY = "H2wEkSvKZY9bdhl";
	public static String APP_KEY_TOTAL = "2KJnuc31oL0rgjt";
	public static String APP_KEY_AUTOTAG = "MjYxYTg2ZDEwMWN";
	public static String APP_KEY_AUTOTOTAL = "YTgxMDkyMzlkNjJ";
	public static String APP_KEY_HOT = "ZTRiMDhlNWM1NTh";

	public BugsRestController(BugsRestService bugsRestService) {
		this.bugsRestService = bugsRestService;
	}
	
	@GetMapping("/{appKey}/v1/search/advanced.search")
	public String colSearch(@PathVariable("appKey") String appKey, @RequestParam Map<String, String> params, @RequestHeader Map<String, Object> requestHeader, HttpServletRequest request) {
		
		params.put("requestHeader.sid", (requestHeader.get("sid")==null?"":requestHeader.get("sid").toString()));
		
		String prValue = RestUtils.getParam(params, "pr");
		
		long time = System.currentTimeMillis();
		
		if (appKey.equals(APP_KEY_TRACK)) {
			params.put("collection",Collections.TRACK);
			
			if(prValue.indexOf("sayclub_web") > -1) {
				return bugsRestService.SayMusicSearch(params, requestHeader, request);
			} else {
				return bugsRestService.search(params, requestHeader, request);
			}
			
		} else if (appKey.equals(APP_KEY_LYRICS)) {
			params.put("collection",Collections.LYRICS);
			return bugsRestService.search(params, requestHeader, request);
		} else if (appKey.equals(APP_KEY_ALBUM)) {
			params.put("collection",Collections.ALBUM);
			return bugsRestService.search(params, requestHeader, request);
		} else if (appKey.equals(APP_KEY_ARTIST)) {
			params.put("collection",Collections.ARTIST);
			
			if(prValue.indexOf("sayclub_web") > -1) {
				return bugsRestService.SayMusicSearch(params, requestHeader, request);
			} else {
				return bugsRestService.search(params, requestHeader, request);
			}
		} else if (appKey.equals(APP_KEY_MV)) {
			params.put("collection",Collections.MV);
			return bugsRestService.search(params, requestHeader, request);
		} else if (appKey.equals(APP_KEY_MCAST)) {
			params.put("collection",Collections.MUSICCAST);
			return bugsRestService.search(params, requestHeader, request);
		} else if (appKey.equals(APP_KEY_MPD)) {
			params.put("collection",Collections.MUSICPD);
			return bugsRestService.search(params, requestHeader, request);
		} else if (appKey.equals(APP_KEY_MPOST)) {
			params.put("collection",Collections.MUSICPOST);
			return bugsRestService.search(params, requestHeader, request);
		} else if (appKey.equals(APP_KEY_CLASSIC)) {
			params.put("collection",Collections.CLASSIC);
			return bugsRestService.search(params, requestHeader, request);
		} else if (appKey.equals(APP_KEY_ENTITY)) {
			params.put("collection",Collections.ENTITY);
			return bugsRestService.search(params, requestHeader, request);
		} else {
			return unknownRequest(params, time);
		}
		
//		return "appKey::" + appKey;
	}
	
	/*
	 * 구매한 곡 검색(앨범,아티스트,영상), 엔티티 API 
	 */
	@PostMapping("/{appKey}/v1/search/advanced.search")
	public String postSearch(@PathVariable("appKey") String appKey, @RequestBody Param param, @RequestHeader Map<String, Object> requestHeader, HttpServletRequest request) {
		
		Map<String, String> header = null;
		
		Map<String, Object> req = (Map<String, Object>) param.getRequest();
		
		Map<String, Object> params = (Map<String, Object>) param.getRequest().get("query");
		Map<String, Object> document = (Map<String, Object>) param.getRequest().get("document");
		
		List<Map<String, String>> index = (List<Map<String, String>>) params.get("index");
		
		long time = System.currentTimeMillis();
		
		if (appKey.equals(APP_KEY_TRACK)) {
			if(index.size() > 0) {
				if(index.get(0).get("num") != null) {
					return bugsRestService.similarSearch(index, document, requestHeader, request);
				} else {
					return bugsRestService.purchasedSearch(params, document, requestHeader, request);
				}
			} else {
				return unknownRequest(header, time);
			}
		} else if (appKey.equals(APP_KEY_MV)) {
			return bugsRestService.purchasedMvSearch(params, document, requestHeader, request);
		} else if (appKey.equals(APP_KEY_ENTITY)) {
			return bugsRestService.EntitySearch(req, requestHeader, request);
		} else {
			return unknownRequest(header, time);
		}
//		return bugsRestService.purchasedSearch(params, document, requestHeader, request);
	}

	
	/*
	 * 통합검색 API 
	 */
	@GetMapping("/{appKey}/v1/search/fusion.search")
	public String totalSearch(@PathVariable("appKey") String appKey, @RequestParam Map<String, String> params, @RequestHeader Map<String, Object> requestHeader, HttpServletRequest request) {
		
		params.put("requestHeader.sid", (requestHeader.get("sid")==null?"":requestHeader.get("sid").toString()));
		
		long time = System.currentTimeMillis();
		
		if (appKey.equals(APP_KEY_TOTAL)) {
			params.put("collection",Collections.TOTAL);

			return bugsRestService.Totalsearch(params, requestHeader, request);
		} else {
			return unknownRequest(params, time);
		}
	}
	
	/*
	 * 자동완성 API (검색어) 
	 */
	@GetMapping("/{appKey}/v1/autocomplete/prefix")
	public String autoCompleteSearch(@PathVariable("appKey") String appKey, @RequestParam Map<String, String> params, @RequestHeader Map<String, Object> requestHeader, HttpServletRequest request) {
		
		params.put("requestHeader.sid", (requestHeader.get("sid")==null?"":requestHeader.get("sid").toString()));
		
		long time = System.currentTimeMillis();
		
		if (appKey.equals(APP_KEY_AUTOTOTAL)) {
			params.put("collection",Collections.AUTO_TOTAL);
			
			return bugsRestService.Autosearch(params, requestHeader, request);
		} else {
			return unknownRequest(params, time);
		}
	}
	
	/*
	 * 자동완성 API (태그) 
	 */
	@GetMapping("/{appKey}/v1/autocomplete/prefix_tag")
	public String autoTagSearch(@PathVariable("appKey") String appKey, @RequestParam Map<String, String> params, @RequestHeader Map<String, Object> requestHeader, HttpServletRequest request) {
		
		params.put("requestHeader.sid", (requestHeader.get("sid")==null?"":requestHeader.get("sid").toString()));
		
		long time = System.currentTimeMillis();
		
		if (appKey.equals(APP_KEY_AUTOTAG)) {
			params.put("collection",Collections.AUTO_TAG);
			
			return bugsRestService.Autosearch(params, requestHeader, request);
		} else {
			return unknownRequest(params, time);
		}
	}
	
	/*
	 * 인기검색어 API 
	 */
	@GetMapping("/{appKey}/v1/hotkeyword")
	public String HotKwdSearch(@PathVariable("appKey") String appKey, @RequestParam Map<String, String> params, @RequestHeader Map<String, Object> requestHeader, HttpServletRequest request) {
		
		params.put("requestHeader.sid", (requestHeader.get("sid")==null?"":requestHeader.get("sid").toString()));
		
		long time = System.currentTimeMillis();
		
		if (appKey.equals(APP_KEY_HOT)) {
			params.put("collection",Collections.HOTKEYWORD);
			
			return bugsRestService.hotKeyword(params, requestHeader, request);
		} else {
			return unknownRequest(params, time);
		}
	}
	
	
	@GetMapping("/health_check")
	public String handleRequest() {

		long time = System.currentTimeMillis();
		
		JsonUnknownUriResult result = new JsonUnknownUriResult(System.currentTimeMillis() - time + " ms", HttpStatus.OK, "server is alive");
		String healthUrl = GsonLoader.getInstance().toJson(result);
		
		return healthUrl;
	}
	
	private String unknownRequest(Map<String, String> params, long time) {
		JsonUnknownUriResult result = new JsonUnknownUriResult(System.currentTimeMillis() - time + " ms", HttpStatus.BAD_REQUEST, "unknown request uri");
		String unknownUri = GsonLoader.getInstance().toJson(result);
		
		return unknownUri;
	}
	
}
