package com.diquest.rest.nhn.service.orderby;

import com.diquest.ir.common.msg.protocol.Protocol;
import com.diquest.ir.common.msg.protocol.query.OrderBySet;
import com.diquest.rest.nhn.service.group.GroupBySetService;

public class OrderBySetService {

	private static OrderBySetService instance = null;

	public static OrderBySetService getInstance() {
		if (instance == null) {
			instance = new OrderBySetService();
		}
		return instance;
	}

	public OrderBySet getOrderBySet(String value, String collection) {
	
		String[] ArrayValue = value.split(":");
		
		if(collection.equalsIgnoreCase("TRACK")) {
			if (ArrayValue[0].equalsIgnoreCase("track_title")) {
				if(ArrayValue[1].equalsIgnoreCase("asc")) {
					return new OrderBySet(true, "TRACK_TITLE", Protocol.TriggerSet.OrderBy.OP_POSTWEIGHT);
				} else {
					return new OrderBySet(false, "TRACK_TITLE", Protocol.TriggerSet.OrderBy.OP_POSTWEIGHT);
				}
			} else if (ArrayValue[0].equalsIgnoreCase("release_ymd")) {
				if(ArrayValue[1].equalsIgnoreCase("asc")) {
					return new OrderBySet(false, "RELEASE_YMD", Protocol.TriggerSet.OrderBy.OP_POSTWEIGHT);
				} else {
					return new OrderBySet(true, "RELEASE_YMD", Protocol.TriggerSet.OrderBy.OP_POSTWEIGHT);
				}
			} else if (ArrayValue[0].equalsIgnoreCase("popular")) {
				if(ArrayValue[1].equalsIgnoreCase("asc")) {
					return new OrderBySet(false, "SCORE", Protocol.TriggerSet.OrderBy.OP_POSTWEIGHT);
				} else {
					return new OrderBySet(true, "SCORE", Protocol.TriggerSet.OrderBy.OP_POSTWEIGHT);
				}
			} else {
				return new OrderBySet(true, "SCORE", Protocol.TriggerSet.OrderBy.OP_PREWEIGHT);
			}
		} else if(collection.equalsIgnoreCase("ALBUM")) {
			if (ArrayValue[0].equalsIgnoreCase("title")) {			// 앨범명순
				if(ArrayValue[1].equalsIgnoreCase("asc")) {
					return new OrderBySet(true, "TITLE", Protocol.TriggerSet.OrderBy.OP_POSTWEIGHT);
				} else {
					return new OrderBySet(false, "TITLE", Protocol.TriggerSet.OrderBy.OP_POSTWEIGHT);
				}
			} else if (ArrayValue[0].equalsIgnoreCase("release_ymd")) {			// 발매일순
				if(ArrayValue[1].equalsIgnoreCase("asc")) {
					return new OrderBySet(false, "RELEASE_YMD", Protocol.TriggerSet.OrderBy.OP_POSTWEIGHT);
				} else {
					return new OrderBySet(true, "RELEASE_YMD", Protocol.TriggerSet.OrderBy.OP_POSTWEIGHT);
				}
			} else if (ArrayValue[0].equalsIgnoreCase("popular")) {			// 인기순
				if(ArrayValue[1].equalsIgnoreCase("asc")) {
					return new OrderBySet(false, "SCORE", Protocol.TriggerSet.OrderBy.OP_POSTWEIGHT);
				} else {
					return new OrderBySet(true, "SCORE", Protocol.TriggerSet.OrderBy.OP_POSTWEIGHT);
				}
			} else {													// 정확도순
				return new OrderBySet(true, "SCORE", Protocol.TriggerSet.OrderBy.OP_PREWEIGHT);
			}
		} else if(collection.equalsIgnoreCase("ARTIST")) {
			if (ArrayValue[0].equalsIgnoreCase("disp_nm")) {			// 이름순
				if(ArrayValue[1].equalsIgnoreCase("asc")) {
					return new OrderBySet(true, "DISP_NM", Protocol.TriggerSet.OrderBy.OP_POSTWEIGHT);
				} else {
					return new OrderBySet(false, "DISP_NM", Protocol.TriggerSet.OrderBy.OP_POSTWEIGHT);
				}
			} else {													// 정확도순
				return new OrderBySet(true, "DISP_NM", Protocol.TriggerSet.OrderBy.OP_PREWEIGHT);
			}
		} else if(collection.equalsIgnoreCase("MV")) {
			if (ArrayValue[0].equalsIgnoreCase("mv_title")) {			// 영상명순
				if(ArrayValue[1].equalsIgnoreCase("asc")) {
					return new OrderBySet(true, "MV_TITLE", Protocol.TriggerSet.OrderBy.OP_POSTWEIGHT);
				} else {
					return new OrderBySet(false, "MV_TITLE", Protocol.TriggerSet.OrderBy.OP_POSTWEIGHT);
				}
			} else if (ArrayValue[0].equalsIgnoreCase("release_ymd")) {			// 발매일순
				if(ArrayValue[1].equalsIgnoreCase("asc")) {
					return new OrderBySet(false, "RELEASE_YMD", Protocol.TriggerSet.OrderBy.OP_POSTWEIGHT);
				} else {
					return new OrderBySet(true, "RELEASE_YMD", Protocol.TriggerSet.OrderBy.OP_POSTWEIGHT);
				}
			} else {													// 정확도순
				return new OrderBySet(true, "MV_TITLE", Protocol.TriggerSet.OrderBy.OP_PREWEIGHT);
			}
		} else if(collection.equalsIgnoreCase("MUSICCAST")) {
			if (ArrayValue[0].equalsIgnoreCase("release_ymd")) {			// 최신순
				return new OrderBySet(false, "UPD_DT", Protocol.TriggerSet.OrderBy.OP_POSTWEIGHT);
			} else {													// 정확도순
				return new OrderBySet(false, "UPD_DT", Protocol.TriggerSet.OrderBy.OP_PREWEIGHT);
			}
		} else if(collection.equalsIgnoreCase("MUSICPD")) {
			if (ArrayValue[0].equalsIgnoreCase("title")) {			// 뮤직pd앨범명순
				if(ArrayValue[1].equalsIgnoreCase("asc")) {
					return new OrderBySet(true, "TITLE", Protocol.TriggerSet.OrderBy.OP_POSTWEIGHT);
				} else {
					return new OrderBySet(false, "TITLE", Protocol.TriggerSet.OrderBy.OP_POSTWEIGHT);
				}
			} else if (ArrayValue[0].equalsIgnoreCase("popular")) {			// 인기순
				if(ArrayValue[1].equalsIgnoreCase("asc")) {
					return new OrderBySet(false, "SCORE", Protocol.TriggerSet.OrderBy.OP_POSTWEIGHT);
				} else {
					return new OrderBySet(true, "SCORE", Protocol.TriggerSet.OrderBy.OP_POSTWEIGHT);
				}
			} else {													// 정확도순
				return new OrderBySet(true, "SCORE", Protocol.TriggerSet.OrderBy.OP_PREWEIGHT);
			}
		} else if(collection.equalsIgnoreCase("MUSICPOST")) {
			if (ArrayValue[0].equalsIgnoreCase("release_ymd")) {			// 발매일순
				if(ArrayValue[1].equalsIgnoreCase("asc")) {
					return new OrderBySet(false, "RELEASE_YMD", Protocol.TriggerSet.OrderBy.OP_POSTWEIGHT);
				} else {
					return new OrderBySet(true, "RELEASE_YMD", Protocol.TriggerSet.OrderBy.OP_POSTWEIGHT);
				}
			} else {													// 정확도순
				return new OrderBySet(true, "RELEASE_YMD", Protocol.TriggerSet.OrderBy.OP_PREWEIGHT);
			}
		} else if(collection.equalsIgnoreCase("CLASSIC")) {
			if (ArrayValue[0].equalsIgnoreCase("popular")) {			// 인기순
				if(ArrayValue[1].equalsIgnoreCase("asc")) {
					return new OrderBySet(false, "SCORE", Protocol.TriggerSet.OrderBy.OP_POSTWEIGHT);
				} else {
					return new OrderBySet(true, "SCORE", Protocol.TriggerSet.OrderBy.OP_POSTWEIGHT);
				}
			} else {													// 정확도순
				return new OrderBySet(true, "SCORE", Protocol.TriggerSet.OrderBy.OP_PREWEIGHT);
			}
		} else if(collection.equalsIgnoreCase("ENTITY")) {
		}
		
		return new OrderBySet(false, "WEIGHT");
	}
}
