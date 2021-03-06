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
	public static String APP_KEY_BRANDI_STORE = "FQbDmOzB8r6y0JA";
	
	public BugsRestController(BugsRestService bugsRestService) {
		this.bugsRestService = bugsRestService;
	}
	
	@GetMapping("/api/v1.0/appkeys/{appKey}/search/advanced.search")
	public String test(@PathVariable("appKey") String appKey, @RequestParam Map<String, String> params, @RequestHeader Map<String, Object> requestHeader, HttpServletRequest request) {
		
		params.put("requestHeader.sid", (requestHeader.get("sid")==null?"":requestHeader.get("sid").toString()));
		
		long time = System.currentTimeMillis();
		
		if (appKey.equals(APP_KEY_TRACK)) {
			params.put("collection","TRACK");
			
			return bugsRestService.search(params, requestHeader, request);
		} else {
			return unknownRequest(params, time);
		}
		
//		return "appKey::" + appKey;
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
