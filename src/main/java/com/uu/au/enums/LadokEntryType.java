package com.uu.au.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum LadokEntryType {
    @JsonProperty("Assignments 1")
    ASSIGNMENTS1("Assignments 1"),
    @JsonProperty("Assignments 2")
    ASSIGNMENTS2("Assignments 2"),
    @JsonProperty("Project")
    PROJECT("Project"),
    @JsonProperty("Imperative Exam")
    IMPERATIVE_EXAM("Imperative Exam"),
    @JsonProperty("OO Exam")
    OBJECT_ORIENTED_EXAM("OO Exam");

    public static final int NUMBER_OF_ENTRIES_FOR_PASS = 5;

    private final String ladokEntryType;

    LadokEntryType(final String ladokEntryType) {
        this.ladokEntryType = ladokEntryType;
    }

    public String getLadokEntryType() {
        return this.ladokEntryType;
    }

    }
