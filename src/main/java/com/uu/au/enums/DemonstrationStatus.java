package com.uu.au.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum DemonstrationStatus {
    @JsonProperty("SUBMITTED")
    SUBMITTED(0),
    @JsonProperty("CLAIMED")
    CLAIMED(1),
    @JsonProperty("CANCELLED_BY_STUDENT")
    CANCELLED_BY_STUDENT(2),
    @JsonProperty("CANCELLED_BY_TEACHER")
    CANCELLED_BY_TEACHER(3),
    @JsonProperty("COMPLETED")
    COMPLETED(4),
    @JsonProperty("IN_FLIGHT")
    IN_FLIGHT(5);

    private final int status;

    DemonstrationStatus(final int status) {
        this.status = status;
    }

    public int getStatus() {
        return this.status;
    }
}
