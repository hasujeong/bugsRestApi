package com.diquest.rest.nhn.service.error;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadPoolExecutor;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.diquest.ir.rest.server.pool.RestHttpRequestJob;

public class logMessageService {
	
	private static final Logger logger = LoggerFactory.getLogger(logMessageService.class);
	
	private static ThreadPoolExecutor threadPool;
	
	public static void requestReceived(Map<String, Object> reqHeader, HttpServletRequest request) {
		
		String remoteIp = Optional.ofNullable(request.getHeader("X-FORWARDED-FOR")).orElse(request.getRemoteAddr());
		int remotePort = request.getRemotePort();
		
		if (remoteIp.equals("0:0:0:0:0:0:0:1")) {
			remoteIp = "127.0.0.1";
		}
		String fromIp = remoteIp + ":" + remotePort;

		logger.info("Thread Status : [workers running] ");
		
//		logger.info("Thread Status : [" + threadPool.getActiveCount() + "/" + threadPool.getMaximumPoolSize() + " workers running] ");
		logger.info("http request received.. [from : " + fromIp + "]");
	}
	
	public static void receiveEnd(Map<String, Object> reqHeader, HttpServletRequest request) {

		long time = System.currentTimeMillis();
		String remoteIp = Optional.ofNullable(request.getHeader("X-FORWARDED-FOR")).orElse(request.getRemoteAddr());
		int remotePort = request.getRemotePort();
		
		if (remoteIp.equals("0:0:0:0:0:0:0:1")) {
			remoteIp = "127.0.0.1";
		}
		String fromIp = remoteIp + ":" + remotePort;
		
		logger.info("http request process end.. [from : " + fromIp + "] [total time :" + (System.currentTimeMillis() - time) + "]");
	}
	
	public static void messageReceived(Map<String, Object> reqHeader, HttpServletRequest request) {

		String req = "";
		
		String clientIp = Optional.ofNullable(request.getHeader("X-FORWARDED-FOR")).orElse(request.getRemoteAddr());
		String req_url = request.getRequestURI();
		String query_url = request.getQueryString();
				
		if (clientIp.equals("0:0:0:0:0:0:0:1")) {
			clientIp = "127.0.0.1";
		}
		
		 if(query_url == null) {
			 req += "" + req_url + "\n";
		} else {
			 req += "" + req_url + "?" + query_url + "\n";
		}
		
		req += "Host: " + (String) reqHeader.get("host") + "\n";
		req += "Accept: " + (String) reqHeader.get("accept") + "\n";
		req += "X-Forwarded-For: " +  clientIp;
		
		printRequest(req);

//		if (reqHeader != null) { 
//			
//			printRequest(req);
//			if (!req.getUri().equals("/favicon.ico")) { // 브라우저 요청시 총2번의 요청으로 처음에는 favicon.ico 가져오려고 함, 무시
//				RestHttpRequestJob job = new RestHttpRequestJob(req, ctx, time);
//				try {
//					Dispatcher.addBatchJob(job);
//				} catch (Exception e1) {
//					returnServerException(req, ctx, time, e1);
//				}
//			}
//		} else {
//			logger.error("current request is not an http request.. from :" + reqHeader.get("host"));
//			ch.close();
//		}

	}
	
	public static void printRequest(String req) {
		String delemeter = "";
		for (int i = 0; i < 70; i++)
			delemeter += "-";
		logger.info("HTTP Request Info \n" + delemeter + "\n" + req + "\n" + delemeter);
	}

}
