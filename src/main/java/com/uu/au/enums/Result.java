package com.uu.au.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Result {
    @JsonProperty("Pass")
    PASS("Pass"),
    @JsonProperty("Fail")
    FAIL("Fail"),
    @JsonProperty("Pushback")
    PUSHBACK("Pushback");

    private final String result;

    Result(final String result) {
        this.result = result;
    }

    public String getResult() {
        return this.result;
    }
}
