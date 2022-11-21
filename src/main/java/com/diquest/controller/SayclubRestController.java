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
import com.diquest.rest.nhn.common.Collections;
import com.diquest.rest.nhn.domain.Param;
import com.diquest.ir.rest.common.constant.HttpStatus;
import com.diquest.service.SayclubRestService;

@RestController
public class SayclubRestController {
	
	private SayclubRestService sayclubRestService;
	
	public static String APP_KEY_SAYCAST = "saycast";

	public SayclubRestController(SayclubRestService sayclubRestService) {
		this.sayclubRestService = sayclubRestService;
	}
	
	@GetMapping("/{appKey}/search")
	public String colSearch(@PathVariable("appKey") String appKey, @RequestParam Map<String, String> params, @RequestHeader Map<String, Object> requestHeader, HttpServletRequest request) {
		
		params.put("requestHeader.sid", (requestHeader.get("sid")==null?"":requestHeader.get("sid").toString()));
		
		long time = System.currentTimeMillis();
		
		if (appKey.equals(APP_KEY_SAYCAST)) {
			params.put("collection",Collections.ALBUM);
			return sayclubRestService.saySearch(params, requestHeader, request);
		} else {
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
