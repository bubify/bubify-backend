package com.uu.au.enums.errors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AchievementErrors {
    public static ResponseStatusException achievementNotFound() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                           "ACHIEVEMENT_NOT_FOUND");
    }
}
