package com.diquest.rest.nhn.service.option;

import com.diquest.ir.common.msg.protocol.Protocol;
import com.diquest.ir.rest.common.exception.InvalidParameterException;
import com.diquest.ir.util.common.StringUtil;
import com.diquest.rest.nhn.common.Collections;

public abstract class Qoption {
    private static final String OR = "or";
    private static final String AND = "and";
    private static final String NOFM = "nofm";
    private static final String OPTION_DELIM = ",";

    private final String indexField;
    private final byte option;
    private float nofmPercent;

    public Qoption(String parameter, String collection) throws InvalidParameterException {
        // q_option 파라미터가 없을 경우, NHN 과 동일하게 기본으로 맞춘다.
        if (StringUtil.isEmpty(parameter)) {
            this.indexField = getUseIndexField(collection);
            this.option = Protocol.WhereSet.OP_HASALL;
            return;
        }

        // 구분자로 들어올 경우
        if (parameter.contains(OPTION_DELIM)) {
            String operator = parameter.split(OPTION_DELIM)[0];
            String indexField = parameter.split(OPTION_DELIM)[1];

            validIndexFieldName(indexField, collection);
            validOperator(operator);

            this.indexField = indexField;
            this.option = getMarinerOption(operator);
            this.nofmPercent = getNofmPercent(operator);
            return;
        }
        
        // 구분자 없이 들어올 경우, operator 로 간주한다
        String operator = parameter;
        validOperator(operator);
        this.indexField = getUseIndexField(collection);
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

    // 다른 idx 이름이 들어왔을 경우 에러
    private void validIndexFieldName(String indexField, String collection) throws InvalidParameterException {
    	if(collection.equalsIgnoreCase(Collections.TRACK)) {
	        if (!indexField.equalsIgnoreCase("track_idx") && !indexField.equalsIgnoreCase("album_idx") && !indexField.equalsIgnoreCase("artist_idx") && !indexField.equalsIgnoreCase(getUseIndexField(collection))) {
	            throw new InvalidParameterException("not exist index : (" + indexField + ")");
	        }
    	} else if(collection.equalsIgnoreCase(Collections.ALBUM)) {
	        if (!indexField.equalsIgnoreCase("album_idx") && !indexField.equalsIgnoreCase("artist_idx") && !indexField.equalsIgnoreCase(getUseIndexField(collection))) {
	            throw new InvalidParameterException("not exist index : (" + indexField + ")");
	        }
    	} else if(collection.equalsIgnoreCase(Collections.ARTIST)) {
	        if (!indexField.equalsIgnoreCase("exact_artist_idx") && !indexField.equalsIgnoreCase("partner_artist_idx") && !indexField.equalsIgnoreCase(getUseIndexField(collection))) {
	            throw new InvalidParameterException("not exist index : (" + indexField + ")");
	        }
      	} else if(collection.equalsIgnoreCase(Collections.ENTITY)) {
	        if (!indexField.equalsIgnoreCase("track_idx") && !indexField.equalsIgnoreCase("album_idx") && 
	        	!indexField.equalsIgnoreCase("artist_idx") && !indexField.equalsIgnoreCase("arranger_idx") && 
	        	!indexField.equalsIgnoreCase("composer_idx") && !indexField.equalsIgnoreCase("featuring_idx") && 
	        	!indexField.equalsIgnoreCase("lyricist_idx") && !indexField.equalsIgnoreCase("genre_idx") && 
	        	!indexField.equalsIgnoreCase("artist_role_idx")&& !indexField.equalsIgnoreCase(getUseIndexField(collection))) {
	        	
	            throw new InvalidParameterException("not exist index : (" + indexField + ")");
	        }
      	} else {
	        if (!indexField.equalsIgnoreCase(getUseIndexField(collection))) {
	            throw new InvalidParameterException("not exist index : (" + indexField + ")");
	        }
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

    public String getUseIndexField(String collection) {
    	String field = "";
    	
    	if(collection.equalsIgnoreCase(Collections.TRACK)) {
    		field = "track_artist_album_idx";
    	} else if(collection.equalsIgnoreCase(Collections.LYRICS)) {
    		field = "lyrics_idx";
    	} else if(collection.equalsIgnoreCase(Collections.ALBUM)) {
    		field = "artist_album_idx";
    	} else if(collection.equalsIgnoreCase(Collections.ARTIST)) {
    		field = "artist_idx";
    	} else if(collection.equalsIgnoreCase(Collections.MV)) {
    		field = "mv_track_artist_album_idx";
    	} else if(collection.equalsIgnoreCase(Collections.MUSICCAST)) {
    		field = "musiccast_idx";
    	} else if(collection.equalsIgnoreCase(Collections.MUSICPD)) {
    		field = "musicpd_album_idx";
    	} else if(collection.equalsIgnoreCase(Collections.MUSICPOST)) {
    		field = "musicpost_idx";
    	} else if(collection.equalsIgnoreCase(Collections.CLASSIC)) {
    		field = "classic_idx";
    	}  else if(collection.equalsIgnoreCase(Collections.ENTITY)) {
    		field = "track_idx";
    	} else {
    		field = "track_artist_album_idx";
    	}
    	
    	return field;
    }
}
