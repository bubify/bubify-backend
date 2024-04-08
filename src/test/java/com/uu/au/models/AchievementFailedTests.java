package com.uu.au.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

public class AchievementFailedTests {
    @Test
    public void testAchievementFailed() {
        // Create a new AchievementFailed and test ALL getters and setters
        AchievementFailed achievementFailed = new AchievementFailed();
        achievementFailed.setId(1L);
        Enrolment enrolment = new Enrolment();
        achievementFailed.setEnrolment(enrolment);
        Achievement achievement = new Achievement();
        achievementFailed.setAchievement(achievement);
        achievementFailed.setFailedTime(LocalDateTime.now());
        achievementFailed.setUpdatedDateTime(LocalDateTime.now());

        assertEquals(1L, achievementFailed.getId());
        assertEquals(enrolment, achievementFailed.getEnrolment());
        assertEquals(achievement, achievementFailed.getAchievement());
        assertNotNull(achievementFailed.getFailedTime());
        assertNotNull(achievementFailed.getUpdatedDateTime());
    }

    @Test
    public void testAchievementFailedAllArgConstruct(){
        // Create a new AchievementFailed with all arguments constructor and test 1 getter
        Enrolment enrolment = new Enrolment();
        Achievement achievement = new Achievement();
        AchievementFailed achievementFailed = new AchievementFailed(1L, enrolment, achievement, LocalDateTime.now(), LocalDateTime.now());

        assertEquals(1L, achievementFailed.getId());
    }

    @Test
    public void testAchievementFailedByBuilder(){
        // Create a new AchievementFailed with builder and test 1 getter
        Enrolment enrolment = new Enrolment();
        Achievement achievement = new Achievement();
        AchievementFailed achievementFailed = AchievementFailed.builder()
                                                .id(1L)
                                                .enrolment(enrolment)
                                                .achievement(achievement)
                                                .failedTime(LocalDateTime.now())
                                                .updatedDateTime(LocalDateTime.now())
                                                .build();

        assertEquals(1L, achievementFailed.getId());
    }
}