package com.uu.au.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

public class AchievementUnlockedTests {
    @Test
    public void testAchievementUnlocked() {
        // Create a new AchievementUnlocked and test ALL getters and setters
        AchievementUnlocked achievementUnlocked = new AchievementUnlocked();
        achievementUnlocked.setId(1L);
        Enrolment enrolment = new Enrolment();
        achievementUnlocked.setEnrolment(enrolment);
        Achievement achievement = new Achievement();
        achievementUnlocked.setAchievement(achievement);
        achievementUnlocked.setUnlockTime(LocalDateTime.now());
        achievementUnlocked.setUpdatedDateTime(LocalDateTime.now());

        assertEquals(1L, achievementUnlocked.getId());
        assertEquals(enrolment, achievementUnlocked.getEnrolment());
        assertEquals(achievement, achievementUnlocked.getAchievement());
        assertNotNull(achievementUnlocked.getUnlockTime());
        assertNotNull(achievementUnlocked.getUpdatedDateTime());
    }

    @Test
    public void testAchievementUnlockedAllArgConstruct(){
        // Create a new AchievementUnlocked with all arguments constructor and test 1 getter
        Enrolment enrolment = new Enrolment();
        Achievement achievement = new Achievement();
        AchievementUnlocked achievementUnlocked = new AchievementUnlocked(1L, enrolment, achievement, LocalDateTime.now(), LocalDateTime.now());

        assertEquals(1L, achievementUnlocked.getId());
    }

    @Test
    public void testAchievementUnlockedByBuilder(){
        // Create a new AchievementUnlocked with builder and test 1 getter
        Enrolment enrolment = new Enrolment();
        Achievement achievement = new Achievement();
        AchievementUnlocked achievementUnlocked = AchievementUnlocked.builder()
                                                    .id(1L)
                                                    .enrolment(enrolment)
                                                    .achievement(achievement)
                                                    .unlockTime(LocalDateTime.now())
                                                    .updatedDateTime(LocalDateTime.now())
                                                    .build();

        assertEquals(1L, achievementUnlocked.getId());
    }
}