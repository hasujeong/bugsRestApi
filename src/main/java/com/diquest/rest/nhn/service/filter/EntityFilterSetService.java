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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EntityFilterSetService {

    private static EntityFilterSetService instance = null;

    public static EntityFilterSetService getInstance() {
        if (instance == null) {
            instance = new EntityFilterSetService();
        }
        return instance;
    }

    public FilterSet[] parseFilter(String[] raw) throws InvalidParameterException {
        List<FilterSet> filters = new ArrayList<>();
        filters.addAll(makeFilterFields(raw));
        return filters.toArray(new FilterSet[filters.size()]);
    }

    private List<FilterSet> makeFilterFields(String[] raw) throws InvalidParameterException {
        List<FilterSet> filters = new ArrayList<>();
 
        String code = "";
        String value = "";

        String ymdCode = "";
        String ymd1 = "";
		String ymd2 = "";
        
		for(int i=0 ; i < raw.length ; i++) {
			String rawStr = raw[i].toUpperCase();
			
			if(!rawStr.contains("RELEASE_YMD")) {				// 코드값만 가져오기
				code = raw[i].split("==")[0].trim();
				value = raw[i].split("==")[1].trim();
				value = value.replaceAll("[^\\w+]", "");
				
				filters.add(new FilterSet(Protocol.FilterSet.OP_MATCH, code, value));
			} else {
				String[] release = raw[i].split(">=");
								
				if(release.length > 1) {
					ymdCode = raw[i].split(">=")[0].trim();
					value = raw[i].split(">=")[1].trim();
					value = value.replaceAll("[^\\w+]", "");
					ymd1 = value;
				} else {
					ymdCode = raw[i].split("<=")[0].trim();
					value = raw[i].split("<=")[1].trim();
					value = value.replaceAll("[^\\w+]", "");
					ymd2 = value;
				}
				
				if(!ymd1.equalsIgnoreCase("") && !ymd2.equalsIgnoreCase("")) {
					String[] range = {ymd1, ymd2};
					filters.add(new FilterSet(Protocol.FilterSet.OP_RANGE, ymdCode, range));
				}
			}
		}
		
        return filters;
    }

}
