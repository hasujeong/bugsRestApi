package com.diquest.rest.nhn.service.option;

import com.diquest.ir.common.msg.protocol.Protocol;
import com.diquest.ir.rest.common.exception.InvalidParameterException;
import com.diquest.ir.util.common.StringUtil;

public abstract class Qoption {
    private static final String OR = "or";
    private static final String AND = "and";
    private static final String NOFM = "nofm";
    private static final String OPTION_DELIM = ",";

    private final String indexField;
    private final byte option;
    private float nofmPercent;

    public Qoption(String parameter) throws InvalidParameterException {
        // q_option 파라미터가 없을 경우, NHN 과 동일하게 기본으로 맞춘다.
        if (StringUtil.isEmpty(parameter)) {
            this.indexField = getUseIndexField();
            this.option = Protocol.WhereSet.OP_HASALL;
            return;
        }

        // 구분자로 들어올 경우
        if (parameter.contains(OPTION_DELIM)) {
            String operator = parameter.split(OPTION_DELIM)[0];
            String indexField = parameter.split(OPTION_DELIM)[1];

            validIndexFieldName(indexField);
            validOperator(operator);

            this.indexField = indexField;
            this.option = getMarinerOption(operator);
            this.nofmPercent = getNofmPercent(operator);
            return;
        }

        // 구분자 없이 들어올 경우, operator 로 간주한다
        String operator = parameter;
        validOperator(operator);
        this.indexField = getUseIndexField();
        this.option = getMarinerOption(operator);
        this.nofmPercent = getNofmPercent(operator);
    }

    private float getNofmPercent(String operator){
        if (operator.startsWith(NOFM)) {
            float value = Float.parseFloat(operator.replace(NOFM, ""));
            if(value < 0){
                return 0;
            }else if(value > 1){
                return 1;
            }else{
                return value;
            }
        }
        return 0;
    }

    private byte getMarinerOption(String operator) throws InvalidParameterException {
        if (operator.equalsIgnoreCase(AND)) {
            return Protocol.WhereSet.OP_HASALL;
        } else if (operator.equalsIgnoreCase(OR)) {
            return Protocol.WhereSet.OP_HASANY;
        } else if (operator.startsWith(NOFM)) {
            return Protocol.WhereSet.OP_N_OF_M;
        }
        throw new InvalidParameterException("query operand parsing failed in (" + operator + ")");
    }

    /**
     * and or nofm 값이 아닐 경우 에러
     *
     * @param operator
     * @throws InvalidParameterException
     */
    private void validOperator(String operator) throws InvalidParameterException {
        if (!operator.equalsIgnoreCase(AND) && !operator.equalsIgnoreCase(OR) && !operator.startsWith(NOFM)) {
            throw new InvalidParameterException("query operand parsing failed in (" + operator + ")");
        }

        if (operator.startsWith(NOFM)) {
            String percentStr = operator.replace(NOFM, "");
            try {
                Float.parseFloat(percentStr);
            } catch (NumberFormatException e) {
                throw new InvalidParameterException("nofm parsing failed (" + percentStr + ")");
            }
        }
    }

    // 상품 검색의 필드 이름은 SHOPPING_IDX 만 지원한다, 다른 이름이 들어왔을 경우 에러
    private void validIndexFieldName(String indexField) throws InvalidParameterException {
        if (!indexField.equalsIgnoreCase(getUseIndexField())) {
            throw new InvalidParameterException("not exist index : (" + indexField + ")");
        }
    }

    public byte getOption() {
        return option;
    }

    public int getNofmPercent() {
        return (int) (nofmPercent * 100);
    }

    public String getIndexField() {
        return indexField;
    }

    public boolean isNofM() {
        return option == Protocol.WhereSet.OP_N_OF_M;
    }

    abstract String getUseIndexField();
}
