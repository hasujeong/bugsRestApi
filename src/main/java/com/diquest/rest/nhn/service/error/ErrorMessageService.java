package com.diquest.rest.nhn.service.error;

import com.diquest.ir.common.msg.ext.body.common.ErrorMessage;
import com.diquest.ir.rest.common.exception.InvalidParameterException;
import com.diquest.ir.rest.common.object.RestHttpRequest;
import com.diquest.ir.rest.server.log.ServerLogManager;
import com.diquest.rest.nhn.service.filter.FilterSetService;

public class ErrorMessageService {
	private static ErrorMessageService instance = null;

	public static ErrorMessageService getInstance() {
		if (instance == null) {
			instance = new ErrorMessageService();
		}
		return instance;
	}

	public void minusReturnCodeLog(int returnCode, ErrorMessage exception, String req) {
		ServerLogManager.getInstance().error("-------------------Command Search Request Error Log Start-------------------");
		ServerLogManager.getInstance().error("-------------------Error Request -------------------");
		ServerLogManager.getInstance().error(req);
		ServerLogManager.getInstance().error("-------------------Error Request End-------------------");
		ServerLogManager.getInstance().error("-------------------Error Message Start-------------------");
		ServerLogManager.getInstance().error("returnCode : " + returnCode);
		ServerLogManager.getInstance().error(exception.getStackTrace());
		ServerLogManager.getInstance().error("-------------------Error Message End-------------------");
		ServerLogManager.getInstance().error("-------------------Command Search Request Error Log End-------------------");
	}

	public void invalidParameterLog(String req, InvalidParameterException e) {
		ServerLogManager.getInstance().error("-------------------Invalid Parameter Error Log Start-------------------");
		ServerLogManager.getInstance().error("-------------------Error Request -------------------");
		ServerLogManager.getInstance().error(req);
		ServerLogManager.getInstance().error("-------------------Error Request End-------------------");
		ServerLogManager.getInstance().error("-------------------Error Message Start-------------------");
		ServerLogManager.getInstance().error("", e);
		ServerLogManager.getInstance().error("-------------------Error Message End-------------------");
		ServerLogManager.getInstance().error("-------------------Invalid Parameter Error Log End-------------------");
	}

	public void InternalServerErrorLog(String req, Exception e) {
		ServerLogManager.getInstance().error("-------------------Internal Server Error Log Start-------------------");
		ServerLogManager.getInstance().error("-------------------Error Request -------------------");
		ServerLogManager.getInstance().error(req);
		ServerLogManager.getInstance().error("-------------------Error Request End-------------------");
		ServerLogManager.getInstance().error("-------------------Error Message Start-------------------");
		ServerLogManager.getInstance().error("", e);
		ServerLogManager.getInstance().error("-------------------Error Message End-------------------");
		ServerLogManager.getInstance().error("-------------------Internal Server Error Log End-------------------");
	}
}
