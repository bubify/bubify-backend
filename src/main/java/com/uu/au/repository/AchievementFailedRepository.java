package com.uu.au.repository;

import com.uu.au.models.AchievementFailed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AchievementFailedRepository
        extends JpaRepository<AchievementFailed, Long> {
}


