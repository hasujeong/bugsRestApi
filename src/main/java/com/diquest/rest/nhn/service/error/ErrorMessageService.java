package com.diquest.rest.nhn.service.error;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.diquest.ir.common.msg.ext.body.common.ErrorMessage;
import com.diquest.ir.rest.common.exception.InvalidParameterException;

public class ErrorMessageService {
	private static ErrorMessageService instance = null;

	public static ErrorMessageService getInstance() {
		if (instance == null) {
			instance = new ErrorMessageService();
		}
		return instance;
	}
	
	private static final Logger logger = LoggerFactory.getLogger(logMessageService.class);

	public void minusReturnCodeLog(int returnCode, ErrorMessage exception, String req) {
		logger.error("-------------------Command Search Request Error Log Start-------------------");
		logger.error("-------------------Error Request -------------------");
		logger.error(req);
		logger.error("-------------------Error Request End-------------------");
		logger.error("-------------------Error Message Start-------------------");
		logger.error("returnCode : " + returnCode);
		logger.error(exception.getStackTrace());
		logger.error("-------------------Error Message End-------------------");
		logger.error("-------------------Command Search Request Error Log End-------------------");
	}

	public void invalidParameterLog(String req, InvalidParameterException e) {
		logger.error("-------------------Invalid Parameter Error Log Start-------------------");
		logger.error("-------------------Error Request -------------------");
		logger.error(req);
		logger.error("-------------------Error Request End-------------------");
		logger.error("-------------------Error Message Start-------------------");
		logger.error("", e);
		logger.error("-------------------Error Message End-------------------");
		logger.error("-------------------Invalid Parameter Error Log End-------------------");
	}

	public void InternalServerErrorLog(String req, Exception e) {
		logger.error("-------------------Internal Server Error Log Start-------------------");
		logger.error("-------------------Error Request -------------------");
		logger.error(req);
		logger.error("-------------------Error Request End-------------------");
		logger.error("-------------------Error Message Start-------------------");
		logger.error("", e);
		logger.error("-------------------Error Message End-------------------");
		logger.error("-------------------Internal Server Error Log End-------------------");
	}
}
