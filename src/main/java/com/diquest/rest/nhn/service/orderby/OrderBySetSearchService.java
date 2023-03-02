package com.diquest.rest.nhn.service.orderby;

import java.util.Map;

import com.diquest.ir.common.msg.protocol.Protocol;
import com.diquest.ir.common.msg.protocol.query.OrderBySet;
import com.diquest.ir.rest.common.exception.InvalidParameterException;
import com.diquest.ir.rest.util.RestUtils;
import com.diquest.rest.nhn.common.Collections;
import com.diquest.rest.nhn.service.option.searchQoption;

public class OrderBySetSearchService {

	private static OrderBySetSearchService instance = null;

	public static OrderBySetSearchService getInstance() {
		if (instance == null) {
			instance = new OrderBySetSearchService();
		}
		return instance;
	}

	public OrderBySet getOrderBySetSearch(Map<String, String> params, String collection) throws InvalidParameterException {
	
		String value = RestUtils.getParam(params, "sort");
		String[] ArrayValue = value.split(":");
		
		if(collection.equalsIgnoreCase(Collections.TRACK)) {
			
			searchQoption qOption = new searchQoption(RestUtils.getParam(params, "q_option"), collection);

			String idxField = qOption.getIndexField();
			
			if (ArrayValue[0].equalsIgnoreCase("track_title")) {
				if(ArrayValue[1].equalsIgnoreCase("asc")) {
					return new OrderBySet(true, "TRACK_TITLE", Protocol.OrderBySet.OP_POSTWEIGHT);
				} else {
					return new OrderBySet(false, "TRACK_TITLE", Protocol.OrderBySet.OP_POSTWEIGHT);
				}
			} else if (ArrayValue[0].equalsIgnoreCase("release_ymd")) {
				if(ArrayValue[1].equalsIgnoreCase("asc")) {
					return new OrderBySet(false, "RELEASE_YMD", Protocol.OrderBySet.OP_POSTWEIGHT);
				} else {
					return new OrderBySet(true, "RELEASE_YMD", Protocol.OrderBySet.OP_POSTWEIGHT);
				}
			} else if (ArrayValue[0].equalsIgnoreCase("popular")) {
				if(ArrayValue[1].equalsIgnoreCase("asc")) {
					return new OrderBySet(false, "SCORE", Protocol.OrderBySet.OP_POSTWEIGHT);
				} else {
					return new OrderBySet(true, "SCORE", Protocol.OrderBySet.OP_POSTWEIGHT);
				}
			} else {
				if(idxField.equalsIgnoreCase("track_artist_album_idx")) {
					return new OrderBySet(true, "BUGS_WEIGHT", Protocol.OrderBySet.OP_PRE_ADDWEIGHT);
				} else {
					return new OrderBySet(true, "SCORE", Protocol.OrderBySet.OP_PREWEIGHT);
				}
			}
		} else if(collection.equalsIgnoreCase(Collections.LYRICS)) {
			if (ArrayValue[0].equalsIgnoreCase("track_title")) {
				if(ArrayValue[1].equalsIgnoreCase("asc")) {
					return new OrderBySet(true, "TRACK_TITLE", Protocol.OrderBySet.OP_POSTWEIGHT);
				} else {
					return new OrderBySet(false, "TRACK_TITLE", Protocol.OrderBySet.OP_POSTWEIGHT);
				}
			} else if (ArrayValue[0].equalsIgnoreCase("popular")) {
				if(ArrayValue[1].equalsIgnoreCase("asc")) {
					return new OrderBySet(false, "SCORE", Protocol.OrderBySet.OP_POSTWEIGHT);
				} else {
					return new OrderBySet(true, "SCORE", Protocol.OrderBySet.OP_POSTWEIGHT);
				}
			}else {
				return new OrderBySet(true, "BUGS_WEIGHT", Protocol.OrderBySet.OP_PRE_ADDWEIGHT);
			}
		} else if(collection.equalsIgnoreCase(Collections.ALBUM)) {
			searchQoption qOption = new searchQoption(RestUtils.getParam(params, "q_option"), collection);

			String idxField = qOption.getIndexField();
			
			if (ArrayValue[0].equalsIgnoreCase("title")) {			// 앨범명순
				if(ArrayValue[1].equalsIgnoreCase("asc")) {
					return new OrderBySet(true, "TITLE", Protocol.OrderBySet.OP_POSTWEIGHT);
				} else {
					return new OrderBySet(false, "TITLE", Protocol.OrderBySet.OP_POSTWEIGHT);
				}
			} else if (ArrayValue[0].equalsIgnoreCase("release_ymd")) {			// 발매일순
				if(ArrayValue[1].equalsIgnoreCase("asc")) {
					return new OrderBySet(false, "RELEASE_YMD", Protocol.OrderBySet.OP_POSTWEIGHT);
				} else {
					return new OrderBySet(true, "RELEASE_YMD", Protocol.OrderBySet.OP_POSTWEIGHT);
				}
			} else if (ArrayValue[0].equalsIgnoreCase("popular")) {			// 인기순
				if(ArrayValue[1].equalsIgnoreCase("asc")) {
					return new OrderBySet(false, "SCORE", Protocol.OrderBySet.OP_POSTWEIGHT);
				} else {
					return new OrderBySet(true, "SCORE", Protocol.OrderBySet.OP_POSTWEIGHT);
				}
			} else {
				// 정확도순
				if(idxField.equalsIgnoreCase("artist_album_idx")) {
					return new OrderBySet(true, "BUGS_WEIGHT", Protocol.OrderBySet.OP_PRE_ADDWEIGHT);
				} else {
					return new OrderBySet(true, "SCORE", Protocol.OrderBySet.OP_PREWEIGHT);
				}
			}
		} else if(collection.equalsIgnoreCase(Collections.ARTIST)) {
			if (ArrayValue[0].equalsIgnoreCase("disp_nm")) {			// 이름순
				if(ArrayValue[1].equalsIgnoreCase("asc")) {
					return new OrderBySet(true, "DISP_NM", Protocol.OrderBySet.OP_POSTWEIGHT);
				} else {
					return new OrderBySet(false, "DISP_NM", Protocol.OrderBySet.OP_POSTWEIGHT);
				}
			} else {													// 정확도순
				return new OrderBySet(true, "BUGS_WEIGHT", Protocol.OrderBySet.OP_PRE_ADDWEIGHT);
			}
		} else if(collection.equalsIgnoreCase(Collections.MV)) {
			if (ArrayValue[0].equalsIgnoreCase("mv_title")) {			// 영상명순
				if(ArrayValue[1].equalsIgnoreCase("asc")) {
					return new OrderBySet(true, "MV_TITLE", Protocol.OrderBySet.OP_POSTWEIGHT);
				} else {
					return new OrderBySet(false, "MV_TITLE", Protocol.OrderBySet.OP_POSTWEIGHT);
				}
			} else if (ArrayValue[0].equalsIgnoreCase("release_ymd")) {			// 발매일순
				if(ArrayValue[1].equalsIgnoreCase("asc")) {
					return new OrderBySet(false, "RELEASE_YMD", Protocol.OrderBySet.OP_POSTWEIGHT);
				} else {
					return new OrderBySet(true, "RELEASE_YMD", Protocol.OrderBySet.OP_POSTWEIGHT);
				}
			} else {													// 정확도순
				return new OrderBySet(true, "BUGS_WEIGHT", Protocol.OrderBySet.OP_PRE_ADDWEIGHT);
			}
		} else if(collection.equalsIgnoreCase(Collections.MUSICCAST)) {
			if (ArrayValue[0].equalsIgnoreCase("release_ymd")) {			// 최신순
				if(ArrayValue[1].equalsIgnoreCase("asc")) {
					return new OrderBySet(false, "RELEASE_YMD", Protocol.OrderBySet.OP_POSTWEIGHT);
				} else {
					return new OrderBySet(true, "RELEASE_YMD", Protocol.OrderBySet.OP_POSTWEIGHT);
				}
			} else {													// 정확도순
				return new OrderBySet(false, "POPULAR", Protocol.OrderBySet.OP_PREWEIGHT);
			}
		} else if(collection.equalsIgnoreCase(Collections.MUSICPD)) {
			if (ArrayValue[0].equalsIgnoreCase("title")) {			// 뮤직pd앨범명순
				if(ArrayValue[1].equalsIgnoreCase("asc")) {
					return new OrderBySet(true, "TITLE", Protocol.OrderBySet.OP_POSTWEIGHT);
				} else {
					return new OrderBySet(false, "TITLE", Protocol.OrderBySet.OP_POSTWEIGHT);
				}
			} 
			else if (ArrayValue[0].equalsIgnoreCase("popular")) {			// 인기순
				if(ArrayValue[1].equalsIgnoreCase("asc")) {
					return new OrderBySet(false, "POPULAR", Protocol.OrderBySet.OP_POSTWEIGHT);
				} else {
					return new OrderBySet(true, "POPULAR", Protocol.OrderBySet.OP_POSTWEIGHT);
				}
			} 
			else {													// 정확도순
				return new OrderBySet(true, "BUGS_WEIGHT", Protocol.OrderBySet.OP_PRE_ADDWEIGHT);
			}
		} else if(collection.equalsIgnoreCase(Collections.MUSICPOST)) {
			if (ArrayValue[0].equalsIgnoreCase("release_ymd")) {			// 발매일순
				if(ArrayValue[1].equalsIgnoreCase("asc")) {
					return new OrderBySet(false, "RELEASE_YMD", Protocol.OrderBySet.OP_POSTWEIGHT);
				} else {
					return new OrderBySet(true, "RELEASE_YMD", Protocol.OrderBySet.OP_POSTWEIGHT);
				}
			} else {													// 정확도순
				return new OrderBySet(true, "BUGS_WEIGHT", Protocol.OrderBySet.OP_PRE_ADDWEIGHT);
			}
		} else if(collection.equalsIgnoreCase(Collections.CLASSIC)) {
			if (ArrayValue[0].equalsIgnoreCase("popular")) {			// 인기순
				if(ArrayValue[1].equalsIgnoreCase("asc")) {
					return new OrderBySet(false, "SCORE", Protocol.OrderBySet.OP_POSTWEIGHT);
				} else {
					return new OrderBySet(true, "SCORE", Protocol.OrderBySet.OP_POSTWEIGHT);
				}
			} else {													// 정확도순
				return new OrderBySet(true, "SCORE", Protocol.OrderBySet.OP_PREWEIGHT);
			}
		} else if(collection.equalsIgnoreCase(Collections.ENTITY)) {
			if (ArrayValue[0].equalsIgnoreCase("popular")) {			// 인기순
				if(ArrayValue[1].equalsIgnoreCase("asc")) {
					return new OrderBySet(false, "POPULAR", Protocol.OrderBySet.OP_POSTWEIGHT);
				} else {
					return new OrderBySet(true, "POPULAR", Protocol.OrderBySet.OP_POSTWEIGHT);
				}
			} else if (ArrayValue[0].equalsIgnoreCase("release_ymd")) {			// 발매일순
				if(ArrayValue[1].equalsIgnoreCase("asc")) {
					return new OrderBySet(false, "RELEASE_YMD", Protocol.OrderBySet.OP_POSTWEIGHT);
				} else {
					return new OrderBySet(true, "RELEASE_YMD", Protocol.OrderBySet.OP_POSTWEIGHT);
				}
			} else {													// 정확도순
				return new OrderBySet(true, "POPULAR", Protocol.OrderBySet.OP_PREWEIGHT);
			}
		} else if(collection.equalsIgnoreCase(Collections.AUTO_TAG)) {
			return new OrderBySet(true, "ESALBUM_CNT", Protocol.OrderBySet.OP_PREWEIGHT);
		} else if(collection.equalsIgnoreCase(Collections.AUTO_TOTAL)) {
			return new OrderBySet(true, "SCORE", Protocol.OrderBySet.OP_PREWEIGHT);
		} else if(collection.equalsIgnoreCase(Collections.HOTKEYWORD)) {
			return new OrderBySet(true, "RANKING", Protocol.OrderBySet.OP_POSTWEIGHT);
		}
		
		return new OrderBySet(false, "WEIGHT");
	}
}
