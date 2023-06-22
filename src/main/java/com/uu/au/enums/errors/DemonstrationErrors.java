package com.uu.au.enums.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class DemonstrationErrors {
    public static ResponseStatusException demonstrationAlreadyGraded() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                           "DEMONSTRATION_ALREADY_GRADED");
    }

    public static ResponseStatusException demonstrationNotFound() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                           "DEMONSTRATION_NOT_FOUND");
    }

    public static ResponseStatusException userInMultipleDemoRequest() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "USER_IN_MULTIPLE_DEMO_REQUEST");
    }

    public static ResponseStatusException demonstrationWithZeroAchievements() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "A_DEMONSTRATION_REQUEST_MUST_CONTAIN_AT_LEAST_ONE_ACHIEVEMENT");
    }

    public static ResponseStatusException demonstrationContainedNonDemonstrableAchievement() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "DEMONSTRATION_CONTAINED_NON_DEMONSTRABLE_ACHIEVEMENT");
    }

    public static ResponseStatusException studentsMayNotClaimDemonstration() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                                           "STUDENTS_MAY_NOT_CLAIM_OR_UNCLAIM_DEMONSTRATION");
    }

    public static ResponseStatusException attemptToUnlockPushedBackAchievement() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "CANNOT_UNLOCK_ACHIEVEMENT_CURRENTLY_ON_PUSH_BACK");
    }

    public static ResponseStatusException gradingRequiresVerifiedProfilePictures() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "GRADING_REQUIRES_VERIFIED_PROFILE_PIC");
    }
}
