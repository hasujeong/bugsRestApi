package com.diquest.rest.nhn.service.option;

import com.diquest.ir.rest.common.exception.InvalidParameterException;

/**
 * 상품 검색의 q_option 파라미터를 다루는 클래스
 *
 * @author lyj
 */
public class ProductQoption extends Qoption {
    public static final String INDEX_FIELD = "shopping_idx";

    public ProductQoption(String parameter) throws InvalidParameterException {
        super(parameter);
    }

    @Override
    String getUseIndexField() {
        return INDEX_FIELD;
    }


}
