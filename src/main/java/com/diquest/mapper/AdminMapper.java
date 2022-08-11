package com.diquest.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.diquest.mapper.domain.FieldSelector;

@Mapper
public interface AdminMapper {
	
	 public List<FieldSelector> fieldSelector();

}
