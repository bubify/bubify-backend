package com.uu.au.enums;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class ResultConverter implements AttributeConverter<Result, String> {

    @Override
    public String convertToDatabaseColumn(Result result) {
        if (result == null) {
            return null;
        }
        return result.getResult();
    }

    @Override
    public Result convertToEntityAttribute(String result) {
        switch (result) {
            case "Pass": return Result.PASS;
            case "Fail": return Result.FAIL;
            case "Pushback": return Result.PUSHBACK;
            default: return null;
        }
    }
}
