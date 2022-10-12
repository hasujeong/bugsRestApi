package com.diquest.rest.nhn.service.filter;

import com.diquest.ir.common.msg.protocol.Protocol;
import com.diquest.ir.common.msg.protocol.query.FilterSet;
import com.diquest.ir.rest.common.exception.InvalidParameterException;
import com.diquest.rest.nhn.common.Collections;
import com.diquest.rest.nhn.filter.result.FilterFieldParseResult;
import com.diquest.rest.nhn.filter.result.FilterNormalValueResult;
import com.diquest.rest.nhn.filter.result.FilterRangeValueResult;
import com.diquest.rest.nhn.filter.value.*;
import com.diquest.rest.nhn.service.trigger.TriggerFieldService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TotalFilterSetService {

    private static TotalFilterSetService instance = null;

    public static TotalFilterSetService getInstance() {
        if (instance == null) {
            instance = new TotalFilterSetService();
        }
        return instance;
    }

    public FilterSet[] parseTotalFilter(Map<String, String> params, FilterFieldParseResult filterFieldParseResult, String collection) throws InvalidParameterException {
        List<FilterSet> filters = new ArrayList<>();
//        filters.addAll(makeTriggerFilterFields(TriggerFieldService.getInstance().getTriggerFieldNames(params), filterFieldParseResult));
        filters.addAll(makeFilterFields(filterFieldParseResult, collection));
        return filters.toArray(new FilterSet[filters.size()]);
    }

    private List<FilterSet> makeFilterFields(FilterFieldParseResult filterFieldParseResult, String collection) throws InvalidParameterException {
        List<FilterSet> filters = new ArrayList<>();
        
        if(collection.equalsIgnoreCase(Collections.TRACK)) {
        	filters.add(new FilterSet((byte) (Protocol.FilterSet.OP_MATCH|Protocol.FilterSet.OP_WEIGHT_ADJUST), "COVER_YN", "N", 1000));
        	filters.add(new FilterSet((byte) (Protocol.FilterSet.OP_MATCH|Protocol.FilterSet.OP_WEIGHT_ADJUST), "MR_YN", "N", 1000));
        	filters.add(new FilterSet(Protocol.FilterSet.OP_MATCH, "SEARCH_EXCLUDE_YN", "N"));
        } 
        
        if(collection.equalsIgnoreCase(Collections.ALBUM) || collection.equalsIgnoreCase(Collections.ARTIST) || collection.equalsIgnoreCase(Collections.MV) || collection.equalsIgnoreCase(Collections.MUSICPD)) {
        	filters.add(new FilterSet(Protocol.FilterSet.OP_MATCH, "SEARCH_EXCLUDE_YN", "N"));
        }
        
        List<FilterNormalValueResult> normalFields = filterFieldParseResult.getFilterNormalValueResults();
       
        for (FilterNormalValueResult tnf : normalFields) {
        	 for (FieldValue v : tnf.getValues()) {
        		 filters.add(new FilterSet(Protocol.FilterSet.OP_MATCH, tnf.getFieldName(), v.getValue()));
        	 }
        }
        
        filterFieldParseResult.removeNormalFields(normalFields);
        return filters;
    }
}
