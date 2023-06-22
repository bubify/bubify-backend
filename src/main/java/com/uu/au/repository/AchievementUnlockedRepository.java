package com.uu.au.repository;

import com.uu.au.models.AchievementUnlocked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface AchievementUnlockedRepository
        extends JpaRepository<AchievementUnlocked, Long> {
    Set<AchievementUnlocked> findAllByEnrolmentId(Long enrolmentId);
    Set<AchievementUnlocked> findAllByAchievementId(Long achievementId);
}

