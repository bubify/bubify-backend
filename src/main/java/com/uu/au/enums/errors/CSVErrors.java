package com.uu.au.enums.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class CSVErrors {
    public static ResponseStatusException csvContainsMoreThanASingleLine() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "CSV_CONTAINS_MORE_THAN_A_SINGLE_LINE");
    }
    public static ResponseStatusException malformed() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "CSV_MALFORMED");
    }
}
