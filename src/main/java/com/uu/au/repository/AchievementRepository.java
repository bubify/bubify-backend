package com.uu.au.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.uu.au.enums.errors.AchievementErrors;
import com.uu.au.models.Achievement;
import com.uu.au.enums.Level;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AchievementRepository
        extends JpaRepository<Achievement, Long> {

    Optional<Achievement> findByCode(String code);
    Optional<Achievement> findByLevel(Level level);

    default List<Achievement> findAllNeededForLevel(Level level) {
        return findAll()
                .stream()
                .filter(a -> a.requiredForLevel(level))
                .collect(Collectors.toList());
    }

    default Achievement findOrThrow(Long id) {
        return findById(id)
                .orElseThrow(AchievementErrors::achievementNotFound);
    }

    default Set<Achievement> findAllLabAchievements() {
        return findAll()
                .stream()
                .filter(a -> a.getCode().startsWith("LAB"))
                .collect(Collectors.toSet());
    }

    default Set<Achievement> findAllProjectAchievements() {
        return findAll()
                .stream()
                .filter(a -> a.getCode().startsWith("Y"))
                .collect(Collectors.toSet());
    }
}

