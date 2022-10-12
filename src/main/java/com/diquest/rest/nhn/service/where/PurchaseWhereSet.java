package com.diquest.rest.nhn.service.where;

import com.diquest.ir.common.msg.protocol.Protocol;
import com.diquest.ir.common.msg.protocol.query.WhereSet;
import com.diquest.ir.rest.common.exception.InvalidParameterException;
import com.diquest.ir.rest.util.RestUtils;
import com.diquest.rest.nhn.filter.result.FilterFieldParseResult;
import com.diquest.rest.nhn.filter.result.FilterNormalValueResult;
import com.diquest.rest.nhn.filter.value.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PurchaseWhereSet {

    private static PurchaseWhereSet instance = null;

    public static PurchaseWhereSet getInstance() {
        if (instance == null) {
            instance = new PurchaseWhereSet();
        }
        return instance;
    }

    public WhereSet[] makeWhereSet(String purchaseId, List<WhereSet> baseWhereSet) throws InvalidParameterException {
        List<WhereSet> result = new ArrayList<WhereSet>();

        result.addAll(baseWhereSet);
        result.addAll(makePurchaseWhereSet(purchaseId));
//        result.addAll(makeExtraWhereSetFromFilter(filterFieldParseResult));

        return result.toArray(new WhereSet[result.size()]);
    }

    private List<WhereSet> makeExtraWhereSetFromFilter(FilterFieldParseResult filterFieldParseResult) throws InvalidParameterException {
        List<WhereSet> whereSets = new ArrayList<>();
        List<FilterNormalValueResult> results = filterFieldParseResult.getFilterNormalValueResults();
        for (FilterNormalValueResult fnv : results) {
            whereSets.addAll(makeWhereSet(fnv));
        }
        return whereSets;
    }
    
    private List<WhereSet> makePurchaseWhereSet(String purchaseId) throws InvalidParameterException {
    	List<WhereSet> result = new ArrayList<WhereSet>();
//    	String[] idValue = purchaseId.split(" ");
    	
    	if(!purchaseId.equalsIgnoreCase("")) {
	    	result.add(new WhereSet(Protocol.WhereSet.OP_AND));
	    	result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
	    	result.add(new WhereSet("ID", Protocol.WhereSet.OP_HASANY, purchaseId));
	    	result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
    	}
    	
        return result;
    }

    private boolean isKeywordEmpty(Map<String, String> params) {
        return parseQ(params).equals("");
    }

    private Collection<? extends WhereSet> makeWhereSet(FilterNormalValueResult filterNormalValueResult) throws InvalidParameterException {
        List<WhereSet> result = new ArrayList<WhereSet>();
        if (filterNormalValueResult.getValues().size() == 0) {
            return result;
        }
        boolean isFirst = true;
        result.add(new WhereSet(Protocol.WhereSet.OP_AND));
        result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
        for (FieldValue fv : filterNormalValueResult.getValues()) {
            if (fv instanceof AndFieldValue) {
                if (isFirst) {
                    result.add(new WhereSet(filterNormalValueResult.getFieldName(), Protocol.WhereSet.OP_HASALL, fv.getValue()));
                    isFirst = false;
                } else {
                    result.add(new WhereSet(Protocol.WhereSet.OP_AND));
                    result.add(new WhereSet(filterNormalValueResult.getFieldName(), Protocol.WhereSet.OP_HASALL, fv.getValue()));
                }
            } else if (fv instanceof OrFieldValue) {
                result.add(new WhereSet(Protocol.WhereSet.OP_OR));
                result.add(new WhereSet(filterNormalValueResult.getFieldName(), Protocol.WhereSet.OP_HASALL, fv.getValue()));
            } else if (fv instanceof AndNotFieldValue) {
                if (isFirst) {
                    result.add(new WhereSet("ALL", Protocol.WhereSet.OP_HASALL, "A"));
                    result.add(new WhereSet(Protocol.WhereSet.OP_NOT));
                    result.add(new WhereSet(filterNormalValueResult.getFieldName(), Protocol.WhereSet.OP_HASALL, fv.getValue()));
                    isFirst = false;
                } else {
                    result.add(new WhereSet(Protocol.WhereSet.OP_NOT));
                    result.add(new WhereSet(filterNormalValueResult.getFieldName(), Protocol.WhereSet.OP_HASALL, fv.getValue()));
                }
            } else {
                result.add(new WhereSet(Protocol.WhereSet.OP_OR));
                result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
                result.add(new WhereSet("ALL", Protocol.WhereSet.OP_HASALL, "A"));
                result.add(new WhereSet(Protocol.WhereSet.OP_NOT));
                result.add(new WhereSet(filterNormalValueResult.getFieldName(), Protocol.WhereSet.OP_HASALL, fv.getValue()));
                result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
            }
        }
        result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
        return result;
    }

    protected String parseQ(Map<String, String> params) {
        return RestUtils.getParam(params, "q");
    }
}
