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

public class EntityWhereSet {

    private static EntityWhereSet instance = null;

    public static EntityWhereSet getInstance() {
        if (instance == null) {
            instance = new EntityWhereSet();
        }
        return instance;
    }

    public WhereSet[] makeWhereSet(List<WhereSet> baseWhereSet) throws InvalidParameterException {
        List<WhereSet> result = new ArrayList<WhereSet>();

        result.addAll(baseWhereSet);
//        result.addAll(makePurchaseWhereSet(purchaseId));
//        result.addAll(makeExtraWhereSetFromFilter(filterFieldParseResult));

        return result.toArray(new WhereSet[result.size()]);
    }

    private List<WhereSet> makePurchaseWhereSet(String purchaseId) throws InvalidParameterException {
    	List<WhereSet> result = new ArrayList<WhereSet>();
//    	String[] idValue = purchaseId.split(" ");
    	
    	result.add(new WhereSet(Protocol.WhereSet.OP_AND));
    	result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_OPEN));
    	result.add(new WhereSet("ID", Protocol.WhereSet.OP_HASANY, purchaseId));
    	result.add(new WhereSet(Protocol.WhereSet.OP_BRACE_CLOSE));
    	
        return result;
    }

}
