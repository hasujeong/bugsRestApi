package com.diquest.rest.nhn.service.filter;

import com.diquest.ir.common.msg.protocol.Protocol;
import com.diquest.ir.common.msg.protocol.query.FilterSet;
import com.diquest.ir.rest.common.exception.InvalidParameterException;
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

    public FilterSet[] parseFilter(Map<String, String> params, FilterFieldParseResult filterFieldParseResult) throws InvalidParameterException {
        List<FilterSet> filters = new ArrayList<>();
        filters.addAll(makeTriggerFilterFields(TriggerFieldService.getInstance().getTriggerFieldNames(params), filterFieldParseResult));
        filters.addAll(makeFilterFields(filterFieldParseResult));
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

    private List<FilterSet> makeFilterFields(FilterFieldParseResult filterFieldParseResult) throws InvalidParameterException {
        List<FilterSet> filters = new ArrayList<>();
        List<FilterRangeValueResult> rangeFields = filterFieldParseResult.getFilterRangeValueResults();
        for (FilterRangeValueResult tnf : rangeFields) {
            assertNormalRange(tnf);
            filters.add(new FilterSet(Protocol.FilterSet.OP_RANGE, tnf.getFieldName(), new String[]{tnf.getLeft().getValue(), tnf.getRight().getValue()}));
        }
        filterFieldParseResult.removeRangeFields(rangeFields);
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
        List<FieldValue> orNots = getOrNot(tnf.getValues()); // or not value ??? ????????? ?????? ??????
        if (orNots.size() > 0) {  // ???????????? ???????????????, ?????? ?????? ????????? ???????????? OP_NOT??? ???????????? ??????.
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
            } else { // or not ??? ?????? ?????????????????? ????????? ?????????. ????????????
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
        List<String> keywords = orNots.stream()//????????? ????????????
                .map(orNot -> orNot.getValue())
                .distinct()
                .collect(Collectors.toList());
        if (keywords.size() > 1) { // or not ??? 1??? ????????????? ????????? ?????? ???????????? ????????????.
            return;
        }
        if (keywords.size() == 1) { //1 ?????? ????????? ?????? ????????????.
            filters.add(new FilterSet(opValueNot, fieldName, keywords.get(0)));
        }

    }

    private List<FieldValue> getOrNot(List<FieldValue> values) {
        return values.stream()
                .filter(f -> f instanceof OrNotFieldValue)
                .collect(Collectors.toList());
    }

    private void addTriggerFieldNameNormalFilter(List<FilterSet> filters, FilterNormalValueResult tnf) throws InvalidParameterException {
        List<FieldValue> orNots = getOrNot(tnf.getValues()); // or not value ??? ????????? ?????? ??????
        if (orNots.size() > 0) {  // ???????????? ???????????????, ?????? ?????? ????????? ???????????? OP_NOT??? ???????????? ??????.
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
            } else { // or not ??? ?????? ?????????????????? ????????? ?????????. ????????????
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
