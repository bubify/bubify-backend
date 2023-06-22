package com.uu.au.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public enum Level {
    @JsonProperty("GRADE_3")
    GRADE_3(3),
    @JsonProperty("GRADE_4")
    GRADE_4(4),
    @JsonProperty("GRADE_5")
    GRADE_5(5);

    private final int level;

    @JsonCreator
    public static Level forValues(String grade) {
        switch (grade) {
            case "GRADE_3": return Level.GRADE_3;
            case "GRADE_4": return Level.GRADE_4;
            case "GRADE_5": return Level.GRADE_5;
            default: return null;
        }
    }

    Level(final int level) {
        this.level = level;
    }

    public int getLevel() {
        return this.level;
    }

    public boolean lessThanOrEqual(Level level) {
        return this.level <= level.level;
    }
}
