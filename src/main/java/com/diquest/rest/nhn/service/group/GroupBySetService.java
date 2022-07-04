package com.diquest.rest.nhn.service.group;

import java.util.ArrayList;
import java.util.List;

import com.diquest.ir.common.msg.protocol.query.GroupBySet;
import com.diquest.ir.common.msg.protocol.query.Query;
import com.diquest.ir.common.msg.protocol.result.GroupResult;
import com.diquest.ir.common.msg.protocol.result.Result;
import com.diquest.rest.nhn.result.NhnResult;
import com.diquest.rest.nhn.result.NhnResult.Group;

public class GroupBySetService {

	private static GroupBySetService instance = null;

	public static GroupBySetService getInstance() {
		if (instance == null) {
			instance = new GroupBySetService();
		}
		return instance;
	}

	public List<Group> makeCategory1(Query q, Result result) {
		GroupBySet[] groupFields = q.getGroupSelectFields();
		if (groupFields.length == 0) {
			return null;
		}
		for (int i = 0; i < groupFields.length; i++) {
			if (String.valueOf(groupFields[i].getField()).equalsIgnoreCase("category_1")) {
				List<Group> groups = new ArrayList<NhnResult.Group>();
				GroupResult groupResult = result.getGroupResult(i);
				for (int j = 0; j < groupResult.groupResultSize(); j++) {
					groups.add(new Group(String.valueOf(groupResult.getId(j)), String.valueOf(groupResult.getIntValue(j))));
				}
				return groups;
			}
		}
		return null;
	}

	public List<Group> makeCategory12(Query q, Result result) {
		GroupBySet[] groupFields = q.getGroupSelectFields();
		if (groupFields.length == 0) {
			return null;
		}
		for (int i = 0; i < groupFields.length; i++) {
			if (String.valueOf(groupFields[i].getField()).equalsIgnoreCase("category_12")) {
				List<Group> groups = new ArrayList<NhnResult.Group>();
				GroupResult groupResult = result.getGroupResult(i);
				for (int j = 0; j < groupResult.groupResultSize(); j++) {
					groups.add(new Group(String.valueOf(groupResult.getId(j)), String.valueOf(groupResult.getIntValue(j))));
				}
				return groups;
			}
		}
		return null;
	}

	public List<Group> makeCategory123(Query q, Result result) {
		GroupBySet[] groupFields = q.getGroupSelectFields();
		if (groupFields.length == 0) {
			return null;
		}
		for (int i = 0; i < groupFields.length; i++) {
			if (String.valueOf(groupFields[i].getField()).equalsIgnoreCase("category_123")) {
				List<Group> groups = new ArrayList<NhnResult.Group>();
				GroupResult groupResult = result.getGroupResult(i);
				for (int j = 0; j < groupResult.groupResultSize(); j++) {
					groups.add(new Group(String.valueOf(groupResult.getId(j)), String.valueOf(groupResult.getIntValue(j))));
				}
				return groups;
			}
		}
		return null;
	}

}
