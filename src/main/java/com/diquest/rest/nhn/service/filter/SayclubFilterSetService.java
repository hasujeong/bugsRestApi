package com.diquest.rest.nhn.service.filter;

import com.diquest.ir.common.msg.protocol.Protocol;
import com.diquest.ir.common.msg.protocol.query.FilterSet;
import com.diquest.ir.rest.common.exception.InvalidParameterException;
import com.diquest.rest.nhn.common.Collections;
import com.diquest.rest.nhn.common.SayclubCollections;
import com.diquest.rest.nhn.filter.result.FilterFieldParseResult;
import com.diquest.rest.nhn.filter.result.FilterNormalValueResult;
import com.diquest.rest.nhn.filter.result.FilterRangeValueResult;
import com.diquest.rest.nhn.filter.value.*;
import com.diquest.rest.nhn.service.trigger.TriggerFieldService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SayclubFilterSetService {

    private static SayclubFilterSetService instance = null;

    public static SayclubFilterSetService getInstance() {
        if (instance == null) {
            instance = new SayclubFilterSetService();
        }
        return instance;
    }

    public FilterSet[] parseFilter(String collection, String RangeFilter, String RangeKey) throws InvalidParameterException {
        List<FilterSet> filters = new ArrayList<>();
        filters.addAll(makeParamFilterFields(RangeFilter, RangeKey, collection)); 
        return filters.toArray(new FilterSet[filters.size()]);
    }

    private List<FilterSet> makeParamFilterFields(String RangeFilter, String RangeKey, String collection) throws InvalidParameterException {
        List<FilterSet> filters = new ArrayList<>();
        String[] rangeValues;
        
        String rangeStr = RangeFilter.replace("[", "").replace("]", "");
        rangeValues = rangeStr.split(",");
                
        if(collection.equalsIgnoreCase(SayclubCollections.CHATUSER_OLD)) {
        	filters.add(new FilterSet(Protocol.FilterSet.OP_RANGE, RangeKey, rangeValues));
        } 
               
        return filters;
    }  
}
