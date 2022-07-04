package com.diquest.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.diquest.service.BugsRestService;

@RestController
public class BugsRestController {
	
	private BugsRestService bugsRestService;
	
	public BugsRestController(BugsRestService bugsRestService) {
		this.bugsRestService = bugsRestService;
	}
	
	@GetMapping("/api/v1.0/appkeys/{appKey}/search/advanced.search")
	public String test(@PathVariable("appKey") String appKey, @RequestParam Map<String, String> params, @RequestHeader Map<String, Object> requestHeader) {
		
		params.put("requestHeader.sid", (requestHeader.get("sid")==null?"":requestHeader.get("sid").toString()));
		
		return bugsRestService.search(params);
		
//		return "appKey::" + appKey;
	}

}
