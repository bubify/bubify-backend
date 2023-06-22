package com.uu.au.enums.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class CourseErrors {
  public static ResponseStatusException alreadyExists() {
    return new ResponseStatusException(HttpStatus.BAD_REQUEST, "COURSE_ALREADY_EXISTS");
  }

  public static ResponseStatusException emptyOrCorrupt() {
    return new ResponseStatusException(HttpStatus.BAD_REQUEST, "COURSE_DOES_NOT_EXISTS_OR_CORRUPT");
  }

  public static ResponseStatusException genericError() {
    return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "GENERIC_ERROR");
  }
}