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

public class FilterSetService {

    private static FilterSetService instance = null;

    public static FilterSetService getInstance() {
        if (instance == null) {
            instance = new FilterSetService();
        }
        return instance;
    }

    public FilterSet[] parseFilter(Map<String, String> params, FilterFieldParseResult filterFieldParseResult, String collection) throws InvalidParameterException {
        List<FilterSet> filters = new ArrayList<>();
//        filters.addAll(makeTriggerFilterFields(TriggerFieldService.getInstance().getTriggerFieldNames(params), filterFieldParseResult));
        filters.addAll(makeFilterFields(filterFieldParseResult, collection));
        return filters.toArray(new FilterSet[filters.size()]);
    }

    private List<FilterSet> makeTriggerFilterFields(List<String> triggerFieldNames, FilterFieldParseResult filterFieldParseResult) throws InvalidParameterException {
        List<FilterSet> filters = new ArrayList<>();
        List<FilterNormalValueResult> triggerNormalFields = filterFieldParseResult.getTriggerNormalFields(triggerFieldNames);
        for (FilterNormalValueResult tnf : triggerNormalFields) {
            if (isFieldNameTriggerField(triggerFieldNames, tnf.getFieldName())) {
                addTriggerFieldNameNormalFilter(filters, tnf);
            } else {
                addTriggerFieldValueNormalFilter(filters, tnf);
            }
        }
        filterFieldParseResult.removeNormalFields(triggerNormalFields);

        List<FilterRangeValueResult> triggerRangeFields = filterFieldParseResult.getTriggerRangeFields(triggerFieldNames);
        for (FilterRangeValueResult tnf : triggerRangeFields) {
            if (isFieldNameTriggerField(triggerFieldNames, tnf.getFieldName())) {
                addTriggerFieldNameRangeFilter(filters, tnf);
            } else {
                addTriggerFieldValueNormalFilter(filters, tnf);
            }
        }
        filterFieldParseResult.removeRangeFields(triggerRangeFields);
        return filters;

    }

    private List<FilterSet> makeFilterFields(FilterFieldParseResult filterFieldParseResult, String collection) throws InvalidParameterException {
        List<FilterSet> filters = new ArrayList<>();
        
        if(collection.equalsIgnoreCase(Collections.TRACK)) {
        	filters.add(new FilterSet((byte) (Protocol.FilterSet.OP_MATCH|Protocol.FilterSet.OP_WEIGHT_ADJUST), "COVER_YN", "N", 1000));
        	filters.add(new FilterSet((byte) (Protocol.FilterSet.OP_MATCH|Protocol.FilterSet.OP_WEIGHT_ADJUST), "MR_YN", "N", 1000));
        	
//        	filters.add(new FilterSet((byte) (Protocol.FilterSet.OP_MATCH|Protocol.FilterSet.OP_WEIGHT_ADJUST), "ALBUM_TP", new String[]{"SP", "RL", "SL"}, 500));
        } else if(collection.equalsIgnoreCase(Collections.LYRICS)) {
        	filters.add(new FilterSet(Protocol.FilterSet.OP_MATCH, "SEARCH_EXCLUDE_YN", "N"));
        	filters.add(new FilterSet(Protocol.FilterSet.OP_MATCH, "STATUS", "Y"));
        } else if(collection.equalsIgnoreCase(Collections.MV)) {
        	filters.add(new FilterSet((byte) (Protocol.FilterSet.OP_MATCH|Protocol.FilterSet.OP_WEIGHT_ADJUST), "ATTR_TP", "STA", 1000));
        } else if(collection.equalsIgnoreCase(Collections.ALBUM)) {
        	filters.add(new FilterSet((byte) (Protocol.FilterSet.OP_MATCH|Protocol.FilterSet.OP_WEIGHT_ADJUST), "ALBUM_TP", new String[]{"RL", "RM", "EP"}, 1000));
        	filters.add(new FilterSet((byte) (Protocol.FilterSet.OP_MATCH|Protocol.FilterSet.OP_WEIGHT_ADJUST), "ALBUM_TP", new String[]{"SL", "OS", "BS", "LV", "SP", "PO"}, 500));
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

    private void assertNormalRange(FilterRangeValueResult tnf) throws InvalidParameterException {
        assertNormalRange(tnf.getLeft().getValue().replace("{", "").replace("[", ""));
        assertNormalRange(tnf.getRight().getValue().replace("}", "").replace("]", ""));
    }

    private void assertNormalRange(String target) throws InvalidParameterException {
        if (!target.isEmpty()) {
            boolean success = false;
            try {
                Integer.parseInt(target);
                success = true;
            } catch (Exception e) {

            }
            try {
                Double.parseDouble(target);
                success = true;
            } catch (Exception e) {

            }
            if(!success){
                throw new InvalidParameterException("range value must be number convertable.");
            }
        }

    }

    private void assertIntConvertable(String value) throws InvalidParameterException {
        String target = value.substring(1);
        if (target.isEmpty()) {

        }
        Integer.parseInt(value.substring(1));
    }

    private void addTriggerFieldValueNormalFilter(List<FilterSet> filters, FilterRangeValueResult tnf) {
        filters.add(new FilterSet(Protocol.TriggerSet.Filter.OP_VALUE_RANGE, tnf.getFieldName(), new String[]{tnf.getLeft().getValue(), tnf.getRight().getValue()}));
    }

    private void addTriggerFieldNameRangeFilter(List<FilterSet> filters, FilterRangeValueResult tnf) {
        filters.add(new FilterSet(Protocol.TriggerSet.Filter.OP_RANGE, tnf.getFieldName(), new String[]{tnf.getLeft().getValue(), tnf.getRight().getValue()}));
    }

    private void addTriggerFieldValueNormalFilter(List<FilterSet> filters, FilterNormalValueResult tnf) throws InvalidParameterException {
        List<FieldValue> orNots = getOrNot(tnf.getValues()); // or not value 가 있는지 먼저 확인
        if (orNots.size() > 0) {  // 하나라도 걸려있으면, 모든 다른 필터를 무시하고 OP_NOT만 걸어주면 된다.
            processOrNots(filters, orNots, tnf.getFieldName(), Protocol.TriggerSet.Filter.OP_VALUE_NOT);
            return;
        }
        List<FieldValue> orValues = new ArrayList<>();
        for (FieldValue fv : tnf.getValues()) {
            if (fv instanceof AndFieldValue) {
                filters.add(new FilterSet(Protocol.TriggerSet.Filter.OP_VALUE_MATCH, tnf.getFieldName(), fv.getValue()));
            } else if (fv instanceof OrFieldValue) {
                orValues.add(fv);
            } else if (fv instanceof AndNotFieldValue) {
                filters.add(new FilterSet(Protocol.TriggerSet.Filter.OP_VALUE_NOT, tnf.getFieldName(), fv.getValue()));
            } else { // or not 은 먼저 처리했으므로 있으면 안된다. 에러처리
                throw new InvalidParameterException("filter field parsing error");
            }
        }

        if (orValues.size() > 0) {
            List<String> values = new ArrayList<>();
            for (FieldValue fv : orValues) {
                values.add(fv.getValue());
            }
            filters.add(new FilterSet(Protocol.TriggerSet.Filter.OP_VALUE_MATCH, tnf.getFieldName(), values.toArray(new String[values.size()])));
        }

    }

    private void processOrNots(List<FilterSet> filters, List<FieldValue> orNots, String fieldName, byte opValueNot) {
        List<String> keywords = orNots.stream()//키워드 중복제거
                .map(orNot -> orNot.getValue())
                .distinct()
                .collect(Collectors.toList());
        if (keywords.size() > 1) { // or not 이 1개 이상이면? 필터를 걸지 않은것과 동일하다.
            return;
        }
        if (keywords.size() == 1) { //1 개일 경우만 필터 걸어준다.
            filters.add(new FilterSet(opValueNot, fieldName, keywords.get(0)));
        }

    }

    private List<FieldValue> getOrNot(List<FieldValue> values) {
        return values.stream()
                .filter(f -> f instanceof OrNotFieldValue)
                .collect(Collectors.toList());
    }

    private void addTriggerFieldNameNormalFilter(List<FilterSet> filters, FilterNormalValueResult tnf) throws InvalidParameterException {
        List<FieldValue> orNots = getOrNot(tnf.getValues()); // or not value 가 있는지 먼저 확인
        if (orNots.size() > 0) {  // 하나라도 걸려있으면, 모든 다른 필터를 무시하고 OP_NOT만 걸어주면 된다.
            processOrNots(filters, orNots, tnf.getFieldName(), Protocol.TriggerSet.Filter.OP_NOT);
            return;
        }
        List<FieldValue> orValues = new ArrayList<>();
        for (FieldValue fv : tnf.getValues()) {
            if (fv instanceof AndFieldValue) {
                filters.add(new FilterSet(Protocol.TriggerSet.Filter.OP_MATCH, tnf.getFieldName(), fv.getValue()));
            } else if (fv instanceof OrFieldValue) {
                orValues.add(fv);
            } else if (fv instanceof AndNotFieldValue) {
                filters.add(new FilterSet(Protocol.TriggerSet.Filter.OP_NOT, tnf.getFieldName(), fv.getValue()));
            } else { // or not 은 먼저 처리했으므로 있으면 안된다. 에러처리
                throw new InvalidParameterException("filter field parsing error");
            }
        }

        if (orValues.size() > 0) {
            List<String> values = new ArrayList<>();
            for (FieldValue fv : orValues) {
                values.add(fv.getValue());
            }
            filters.add(new FilterSet(Protocol.TriggerSet.Filter.OP_MATCH, tnf.getFieldName(), values.toArray(new String[values.size()])));
        }
    }

    private boolean isFieldNameTriggerField(List<String> triggerFieldNames, String fieldName) {
        return triggerFieldNames.contains(fieldName);
    }

}
