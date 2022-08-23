package com.diquest.rest.nhn.service.option;

import com.diquest.ir.rest.common.exception.InvalidParameterException;

/**
 * 상품 검색의 q_option 파라미터를 다루는 클래스
 */
public class searchQoption extends Qoption {
//    public static String INDEX_FIELD = "track_artist_album_idx";

    public searchQoption(String parameter, String collection) throws InvalidParameterException {
        super(parameter, collection);
    }

//    @Override
//    String getUseIndexField() {
//    	System.out.println("###############"+INDEX_FIELD);
//        return INDEX_FIELD;
//    }


}
