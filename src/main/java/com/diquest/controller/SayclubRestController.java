package com.diquest.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.diquest.ir.rest.json.gson.GsonLoader;
import com.diquest.ir.rest.json.object.JsonUnknownUriResult;
import com.diquest.rest.nhn.common.SayclubCollections;
import com.diquest.rest.nhn.domain.Param;
import com.diquest.ir.rest.common.constant.HttpStatus;
import com.diquest.service.SayclubRestService;

@RestController
public class SayclubRestController {
	
	private SayclubRestService sayclubRestService;
	
	public static String APP_KEY_SAYCAST = "saycast_alpha";
	public static String APP_KEY_SAYCAST_ART = "saycast_article_alpha";
	public static String APP_KEY_SAYMALL = "saymall_alpha";
	public static String APP_KEY_ALLUSER = "alluser_alpha";
	public static String APP_KEY_CHATUSER = "chatuser_alpha";

	public SayclubRestController(SayclubRestService sayclubRestService) {
		this.sayclubRestService = sayclubRestService;
	}
	
	@GetMapping("/search/v1.0/appkeys/VDHj9vamzjTEtPgT/serviceids/{appKey}/search")
	public String colSearch(@PathVariable("appKey") String appKey, @RequestParam Map<String, String> params, @RequestHeader Map<String, Object> requestHeader, HttpServletRequest request) {
		
		params.put("requestHeader.sid", (requestHeader.get("sid")==null?"":requestHeader.get("sid").toString()));
		
		long time = System.currentTimeMillis();
		
		if (appKey.equals(APP_KEY_SAYCAST)) {
			params.put("collection",SayclubCollections.SAYCAST);
			return sayclubRestService.saySearch(params, requestHeader, request);
		} else if (appKey.equals(APP_KEY_SAYCAST_ART)) {
			params.put("collection",SayclubCollections.SAYCAST_ART);
			return sayclubRestService.saySearch(params, requestHeader, request);
		} else if (appKey.equals(APP_KEY_SAYMALL)) {
			params.put("collection",SayclubCollections.SAYMALL);
			return sayclubRestService.saySearch(params, requestHeader, request);
		} else if (appKey.equals(APP_KEY_ALLUSER)) {
			params.put("collection",SayclubCollections.ALLUSER);
			return sayclubRestService.saySearch(params, requestHeader, request);
		} else if (appKey.equals(APP_KEY_CHATUSER)) {
			params.put("collection",SayclubCollections.CHATUSER);
			return sayclubRestService.saySearch(params, requestHeader, request);
		}else {
			return unknownRequest(params, time);
		}
		
//		return "appKey::" + appKey;
	}
	
	/*
	 * 자동완성 API (검색어) 
	 *
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
	*/
	
	private String unknownRequest(Map<String, String> params, long time) {
		JsonUnknownUriResult result = new JsonUnknownUriResult(System.currentTimeMillis() - time + " ms", HttpStatus.BAD_REQUEST, "unknown request uri");
		String unknownUri = GsonLoader.getInstance().toJson(result);
		
		return unknownUri;
	}
	
}
