package com.uu.au.controllers;

import java.util.List;

import com.uu.au.enums.Level;
import com.uu.au.enums.errors.UserErrors;
import com.uu.au.models.Json;
import com.uu.au.models.TargetLevel;
import com.uu.au.repository.TargetLevelRepository;

import com.uu.au.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(maxAge = 3600)
@RestController
class TargetLevelController {

    private final TargetLevelRepository targets;
    private final UserRepository users;

    public TargetLevelController(TargetLevelRepository targets, UserRepository users) {
        this.targets = targets;
        this.users = users;
    }

    @GetMapping("/targets")
    public List<TargetLevel> all() {
        return targets.findAll();
    }

    @PostMapping("/targets")
    public TargetLevel newTarget(@RequestBody Level newTargetLevel) {
        return targets.save(TargetLevel.builder()
                .enrolmentId(users.currentUser()
                        .currentEnrolment()
                        .orElseThrow(UserErrors::enrolmentNotFound)
                        .getId())
                .level(newTargetLevel)
                .build());
    }

    @GetMapping("/lastKnownTarget")
    public Json.Target one() {
        var targetLevel = targets.findLatestByEnrolmentId(users.currentUser().currentEnrolment().orElseThrow(UserErrors::enrolmentNotFound).getId());
        var t = targetLevel.isPresent() ? targetLevel.get().getLevel() : null;

        return Json.Target.builder()
                .target(t)
                .build();
    }
}
