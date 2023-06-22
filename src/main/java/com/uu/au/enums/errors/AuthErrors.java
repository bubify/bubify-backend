package com.uu.au.enums.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AuthErrors {
    public static ResponseStatusException insufficientPrivileges() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "INSUFFICIENT_PRIVILEGES");
    }

    public static ResponseStatusException actionNotAllowedByStudent() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "STUDENT_NOT_ALLOWED_TO_PERFORM_THAT_ACTION");
    }

    public static ResponseStatusException JWTTokenExpired() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "JWT_TOKEN_EXPIRED");
    }
}
