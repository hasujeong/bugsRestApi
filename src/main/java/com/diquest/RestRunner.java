package com.diquest;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.diquest.service.BugsRestService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RestRunner {

	public BugsRestService bugsRestService;
	
	public RestRunner(BugsRestService bugsRestService) {
		
		this.bugsRestService = bugsRestService;
		
	}
	
	@EventListener(ApplicationReadyEvent.class)
	public void init() {
		
		bugsRestService.fieldSelector();
		
//		log.info("EventListener :: fieldSelector loading.");
		
	}

}
