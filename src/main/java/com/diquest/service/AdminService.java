package com.diquest.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.diquest.mapper.AdminMapper;
import com.diquest.mapper.domain.FieldSelector;

@Service
public class AdminService {
	
	@Autowired
	AdminMapper adminMapper;
	
	@Cacheable("fieldSelectorMap")
	public Map<String, String> fieldSelector() {
		
		Map<String, String> fieldSelectorMap = new HashMap<String, String>();
		
		List<FieldSelector> fsList = adminMapper.fieldSelector();
		
		System.out.println(fsList.size());
		
		for (FieldSelector fs : fsList) {
			fieldSelectorMap.put(fs.getQuery(), fs.getSelected());
			
			System.out.println(fs.getSelected());
		}
		
		return fieldSelectorMap;
		
	}
	
}
