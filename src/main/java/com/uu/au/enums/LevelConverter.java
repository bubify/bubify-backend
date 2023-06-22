package com.uu.au.enums;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class LevelConverter implements AttributeConverter<Level, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Level level) {
        if (level == null) {
            return null;
        }
        return level.getLevel();
    }

    @Override
    public Level convertToEntityAttribute(Integer level) {
        switch (level) {
            case 3: return Level.GRADE_3;
            case 4: return Level.GRADE_4;
            case 5: return Level.GRADE_5;
            default: return null;
        }
    }
}