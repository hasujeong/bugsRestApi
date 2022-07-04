package com.diquest.rest.nhn.result;

import com.diquest.ir.client.network.ExternalClientPool;
import com.diquest.ir.common.exception.IRException;
import com.diquest.ir.common.msg.ext.body.common.ErrorMessage;
import com.diquest.ir.util.msg.Transmitable;
import com.diquest.ir.util.msg.external.ExtRequest;
import com.diquest.ir.util.msg.external.ExtResponse;
import com.diquest.ir.util.msg.type.ArrayT;
import com.diquest.ir.util.msg.type.DataT;
import com.diquest.ir.util.msg.type.StringT;

public class RestCommandExtractor {
	private final String ip;
	private final int port;
	ErrorMessage failMsg;
	
	public RestCommandExtractor(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}


	public String[][] request(String extractor, String option, String keyword) throws IRException{
		DataT data = new DataT();
		DataT result = null;
		data.put("TYPE", new StringT(extractor));
		data.put("OPTION", new StringT(option));
		data.put("KEYWORD", new StringT(keyword));
		ExtRequest request = new ExtRequest("com.diquest.ir.server.msg.ExtMessageExtractor", data);
		ExtResponse response = ExternalClientPool.getInstance(ip, port).invoke(request);
		if (response == null) {
			failMsg = new ErrorMessage("Cannot connect to Server.");
			return null;
		}
		if(response.getErrorCode() >= 0){
			result = (DataT) response.getData();
		}else{
			failMsg = new ErrorMessage("Fail to extract terms.");
			return null;
		}
		Transmitable[] resultList =((ArrayT)result.get("result")).value;
		Transmitable[] remnantList =((ArrayT)result.get("remnant")).value;
		Transmitable[] additionalList =((ArrayT)result.get("additional")).value;

		String[][] resultKeywords = new String[3][];
		resultKeywords[0] = StringT.convert(resultList);
		resultKeywords[1] = StringT.convert(remnantList);
		resultKeywords[2] = StringT.convert(additionalList);

		return resultKeywords;
	}
	public ErrorMessage getException(){
		return failMsg;
	}
}
