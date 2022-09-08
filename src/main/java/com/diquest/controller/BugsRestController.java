package com.diquest.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.diquest.ir.rest.json.gson.GsonLoader;
import com.diquest.ir.rest.json.object.JsonUnknownUriResult;
import com.diquest.ir.rest.json.reponse.ResponseMaker;
import com.diquest.ir.rest.common.constant.HttpStatus;
import com.diquest.service.BugsRestService;

@RestController
public class BugsRestController {
	
	private BugsRestService bugsRestService;
	
	public static String APP_KEY_TRACK = "lZjTO0HlFg91HwD";
	public static String APP_KEY_LYRICS = "lyrics";
	public static String APP_KEY_ALBUM = "vbmKja0BuYy35Ok";
	public static String APP_KEY_ARTIST = "7S4pV1yEaFoWJsj";
	public static String APP_KEY_MV = "58IHeEjp2lyoR4M";
	public static String APP_KEY_MCAST = "7ZEz9GaqpMRc5DH";
	public static String APP_KEY_MPD = "6Jfys7XEvdQ0KuU";
	public static String APP_KEY_MPOST = "SVmCkyjdYM9n3go";
	public static String APP_KEY_CLASSIC = "0WleItZOyGJbrVF";
	public static String APP_KEY_ENTITY = "H2wEkSvKZY9bdhl";
	public static String APP_KEY_TOTAL = "2KJnuc31oL0rgjt";
	public static String APP_KEY_AUTO = "autocomplete";

	public BugsRestController(BugsRestService bugsRestService) {
		this.bugsRestService = bugsRestService;
	}
	
	@GetMapping("/api/v1.0/appkeys/{appKey}/search/advanced.search")
	public String colSearch(@PathVariable("appKey") String appKey, @RequestParam Map<String, String> params, @RequestHeader Map<String, Object> requestHeader, HttpServletRequest request) {
		
		params.put("requestHeader.sid", (requestHeader.get("sid")==null?"":requestHeader.get("sid").toString()));
		
		long time = System.currentTimeMillis();
		
		if (appKey.equals(APP_KEY_TRACK)) {
			params.put("collection","TRACK");
			return bugsRestService.search(params, requestHeader, request);
		} else if (appKey.equals(APP_KEY_LYRICS)) {
			params.put("collection","LYRICS");
			return bugsRestService.search(params, requestHeader, request);
		} else if (appKey.equals(APP_KEY_ALBUM)) {
			params.put("collection","ALBUM");
			return bugsRestService.search(params, requestHeader, request);
		} else if (appKey.equals(APP_KEY_ARTIST)) {
			params.put("collection","ARTIST");
			return bugsRestService.search(params, requestHeader, request);
		} else if (appKey.equals(APP_KEY_MV)) {
			params.put("collection","MV");
			return bugsRestService.search(params, requestHeader, request);
		} else if (appKey.equals(APP_KEY_MCAST)) {
			params.put("collection","MUSICCAST");
			return bugsRestService.search(params, requestHeader, request);
		} else if (appKey.equals(APP_KEY_MPD)) {
			params.put("collection","MUSICPD");
			return bugsRestService.search(params, requestHeader, request);
		} else if (appKey.equals(APP_KEY_MPOST)) {
			params.put("collection","MUSICPOST");
			return bugsRestService.search(params, requestHeader, request);
		} else if (appKey.equals(APP_KEY_CLASSIC)) {
			params.put("collection","CLASSIC");
			return bugsRestService.search(params, requestHeader, request);
		} else if (appKey.equals(APP_KEY_ENTITY)) {
			params.put("collection","ENTITY");
			return bugsRestService.search(params, requestHeader, request);
		} else {
			return unknownRequest(params, time);
		}
		
//		return "appKey::" + appKey;
	}
	
	/*
	 * 통합검색 API 
	 */
	@GetMapping("/api/v1.0/appkeys/{appKey}/search/fusion.search")
	public String totalSearch(@PathVariable("appKey") String appKey, @RequestParam Map<String, String> params, @RequestHeader Map<String, Object> requestHeader, HttpServletRequest request) {
		
		params.put("requestHeader.sid", (requestHeader.get("sid")==null?"":requestHeader.get("sid").toString()));
		
		long time = System.currentTimeMillis();
		
		if (appKey.equals(APP_KEY_TOTAL)) {
			params.put("collection","TOTAL");

			return bugsRestService.Totalsearch(params, requestHeader, request);
		} else {
			return unknownRequest(params, time);
		}
	}
	
	/*
	 * 자동완성 API (검색어) 
	 */
	@GetMapping("/api/v1.0/appkeys/{appKey}/autocomplete/prefix")
	public String autoCompleteSearch(@PathVariable("appKey") String appKey, @RequestParam Map<String, String> params, @RequestHeader Map<String, Object> requestHeader, HttpServletRequest request) {
		
		params.put("requestHeader.sid", (requestHeader.get("sid")==null?"":requestHeader.get("sid").toString()));
		
		long time = System.currentTimeMillis();
		
		if (appKey.equals(APP_KEY_AUTO)) {
			params.put("collection","AUTO_TOTAL");
			
			return bugsRestService.Autosearch(params, requestHeader, request);
		} else {
			return unknownRequest(params, time);
		}
	}
	
	/*
	 * 자동완성 API (태그) 
	 */
	@GetMapping("/api/v1.0/appkeys/{appKey}/autocomplete/prefix_tag")
	public String autoTagSearch(@PathVariable("appKey") String appKey, @RequestParam Map<String, String> params, @RequestHeader Map<String, Object> requestHeader, HttpServletRequest request) {
		
		params.put("requestHeader.sid", (requestHeader.get("sid")==null?"":requestHeader.get("sid").toString()));
		
		long time = System.currentTimeMillis();
		
		if (appKey.equals(APP_KEY_AUTO)) {
			params.put("collection","AUTO_TAG");
			
			return bugsRestService.Autosearch(params, requestHeader, request);
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
