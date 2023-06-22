package com.uu.au.enums.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class GenericRequestListErrors {
    public static ResponseStatusException currentUserNotInSubmitters() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                           "CURRENT_USER_NOT_IN_SUBMITTERS");
    }

    public static ResponseStatusException doneDoesNotMatchRequest() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                           "DEMONSTRATION_DONE_DOES_NOT_MATCH_REQUEST");
    }
}
