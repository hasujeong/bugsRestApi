package com.diquest.rest.nhn.filter.parse;

import java.security.InvalidParameterException;

import com.diquest.ir.util.common.StringUtil;
import com.diquest.rest.nhn.filter.result.FilterNormalValueResult;
import com.diquest.rest.nhn.filter.value.AndFieldValue;
import com.diquest.rest.nhn.filter.value.AndNotFieldValue;
import com.diquest.rest.nhn.filter.value.OrFieldValue;
import com.diquest.rest.nhn.filter.value.OrNotFieldValue;

public class FilterNormalValueParser {
	private static String AND = "&";
	private static String OR = "|";
	private static String NOT = "!";
	private static String SINGLE_QUOTATION = "'";

	FilterNormalValueResult filterNormalValueResult;
	private String parseTargetParameter;

	public FilterNormalValueParser(String fieldName, String targetVal) {
		this.filterNormalValueResult = new FilterNormalValueResult(fieldName);
		this.parseTargetParameter = AND + targetVal;
	}

	public FilterNormalValueResult parseAll() throws InvalidParameterException {
		while (!StringUtil.isEmpty(parseTargetParameter)) {
			int prevLen = parseTargetParameter.length();
			parse();
			if(notParsed(prevLen)){ // 파싱해도 길이가 줄어들지 않았다면 에러 발생시킨다.
				throw new InvalidParameterException("filter value parse error : " + parseTargetParameter);
			}
		}

		return filterNormalValueResult;
	}

	private boolean notParsed(int prevLen) {
		return prevLen == parseTargetParameter.length();
	}

	private void parse() throws InvalidParameterException {
		if (parseTargetParameter.startsWith(AND)) {
			parseTargetParameter = parseTargetParameter.substring(1);
			andFieldParse();
		} else if (parseTargetParameter.startsWith(OR)) {
			parseTargetParameter = parseTargetParameter.substring(1);
			orFieldParse();
		} else {
			throw new InvalidParameterException("filter value parse error : " + parseTargetParameter);
		}

	}

	private void andFieldParse() {
		if (parseTargetParameter.startsWith(NOT)) {
			parseTargetParameter = parseTargetParameter.substring(1);
			andNotField();
		} else {
			andField();
		}
	}

	private void orFieldParse() {
		if (parseTargetParameter.startsWith(NOT)) {
			parseTargetParameter = parseTargetParameter.substring(1);
			orNotField();
		} else {
			orField();
		}
	}

	private void orField() {
		String value = getValue();
		if (!StringUtil.isEmpty(value)) {
			filterNormalValueResult.getValues().add(new OrFieldValue(value));
		}
	}

	private void orNotField() {
		String value = getValue();
		if (!StringUtil.isEmpty(value)) {
			filterNormalValueResult.getValues().add(new OrNotFieldValue(value));
		}
	}

	private void andField() {
		String value = getValue();
		if (!StringUtil.isEmpty(value)) {
			filterNormalValueResult.getValues().add(new AndFieldValue(value));
		}
	}

	private void andNotField() {
		String value = getValue();
		if (!StringUtil.isEmpty(value)) {
			filterNormalValueResult.getValues().add(new AndNotFieldValue(value));
		}
	}

	private String getValue() {
		String value;
		if (parseTargetParameter.startsWith(SINGLE_QUOTATION)) {
			int nextSingleQuotationIdx = parseTargetParameter.indexOf(SINGLE_QUOTATION, 1);
			value = parseTargetParameter.substring(1, nextSingleQuotationIdx);
			parseTargetParameter = parseTargetParameter.substring(nextSingleQuotationIdx + 1);
			return value;
		}

		int nextAndIdx = getNextAndIdx();
		int nextOrIdx = getNextOrIdx();

		value = parseTargetParameter.substring(0, Math.min(nextAndIdx, nextOrIdx));
		parseTargetParameter = parseTargetParameter.substring(Math.min(nextAndIdx, nextOrIdx));
		return value;

	}

	private int getNextOrIdx() {
		return parseTargetParameter.indexOf(OR) < 0 ? parseTargetParameter.length() : parseTargetParameter.indexOf(OR);
	}

	private int getNextAndIdx() {
		return parseTargetParameter.indexOf(AND) < 0 ? parseTargetParameter.length() : parseTargetParameter.indexOf(AND);
	}

}