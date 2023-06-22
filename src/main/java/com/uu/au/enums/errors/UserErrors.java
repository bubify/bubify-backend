package com.uu.au.enums.errors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class UserErrors {
    private static final Logger logger = LoggerFactory.getLogger(UserErrors.class);

    public static ResponseStatusException userNotFound() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                           "USER_NOT_FOUND");
    }

    public static ResponseStatusException userMissingUsername() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "USER_MISSING_USERNAME");
    }

    public static ResponseStatusException userMissingThumbnail() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "USER_MISSING_THUMBNAIL");
    }

    public static ResponseStatusException userEditOtherUser() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "USER_EDIT_OTHER_USER");
    }

    public static ResponseStatusException malformedUserName(String userName) {
        UserErrors.logger.error("Malformed username: " + userName);
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "JWT_MISSING_FROM_REQUEST_HEADERS");
    }

    public static ResponseStatusException notCurrentlyEnrolled() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                           "USER_NOT_CURRENTLY_ENROLLED");
    }

    public static ResponseStatusException invalidCurrentUser() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                                           "INVALID_CURRENT_USER");
    }

    public static ResponseStatusException userFirstNameShort() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                           "USER_FIRST_NAME_SHORT");
    }

    public static ResponseStatusException userLastNameShort() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "USER_LAST_NAME_SHORT");
    }

    public static ResponseStatusException userMissingRole() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "USER_MISSING_ROLE");
    }

    public static ResponseStatusException userMalformedRole() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "USER_MALFORMED_ROLE");
    }

    public static ResponseStatusException userAlreadyExists() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "USER_ALREADY_EXISTS");
    }

    public static ResponseStatusException enrolmentNotFound() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "ENROLMENT_NOT_FOUND");
    }
}
