package com.uu.au.controllers;

import java.util.List;

import com.uu.au.models.Achievement;
import com.uu.au.repository.AchievementRepository;

import org.springframework.web.bind.annotation.*;

@CrossOrigin(maxAge = 3600)
@RestController
class AchievementController {

    private final AchievementRepository repository;

    public AchievementController(AchievementRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/achievements")
    List<Achievement> all() {
        return repository.findAll();
    }
}
