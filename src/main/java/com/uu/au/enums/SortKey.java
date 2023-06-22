package com.uu.au.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum SortKey {
    @JsonProperty("VELOCITY_DEC")
    VELOCITY_DEC,
    @JsonProperty("VELOCITY_ASC")
    VELOCITY_ASC,
    @JsonProperty("FIRSTNAME")
    FIRSTNAME,
    @JsonProperty("LASTNAME")
    LASTNAME,
    @JsonProperty("PROGRESS_DEC")
    PROGRESS_DEC,
    @JsonProperty("PROCESS_ASC")
    PROCESS_ASC
}
