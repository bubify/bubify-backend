package com.uu.au.enums.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class HelpErrors {
    public static ResponseStatusException helpRequestCannotMarkByStudentNotInHelpRequest() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                           "HELPREQUEST_CANNOT_MARK_BY_STUDENT_NOT_IN_HELPREQUEST");
    }

    public static ResponseStatusException helpRequestExpired() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                           "HELPREQUEST_EXPIRED");
    }

    public static ResponseStatusException helpRequestNotFound() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                           "HELPREQUEST_NOT_FOUND");
    }

    public static ResponseStatusException helpRequestNotPickedUp() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                           "HELPREQUEST_NOT_PICKED_UP");
    }

    public static ResponseStatusException userInMultipleHelpRequest() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                           "USER_IN_MULTIPLE_HELP_REQUEST");
    }
}
