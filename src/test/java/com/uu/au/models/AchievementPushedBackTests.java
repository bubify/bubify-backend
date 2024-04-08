package com.uu.au.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

public class AchievementPushedBackTests {
    @Test
    public void testAchievementPushedBack() {
        // Create a new AchievementPushedBack and test ALL getters and setters
        AchievementPushedBack achievementPushedBack = new AchievementPushedBack();
        achievementPushedBack.setId(1L);
        Enrolment enrolment = new Enrolment();
        achievementPushedBack.setEnrolment(enrolment);
        Achievement achievement = new Achievement();
        achievementPushedBack.setAchievement(achievement);
        achievementPushedBack.setPushedBackTime(LocalDateTime.now());
        achievementPushedBack.setUpdatedDateTime(LocalDateTime.now());

        assertEquals(1L, achievementPushedBack.getId());
        assertEquals(enrolment, achievementPushedBack.getEnrolment());
        assertEquals(achievement, achievementPushedBack.getAchievement());
        assertNotNull(achievementPushedBack.getPushedBackTime());
        assertNotNull(achievementPushedBack.getUpdatedDateTime());
    }

    @Test
    public void testAchievementPushedBackAllArgConstruct(){
        // Create a new AchievementPushedBack with all arguments constructor and test 1 getter
        Enrolment enrolment = new Enrolment();
        Achievement achievement = new Achievement();
        AchievementPushedBack achievementPushedBack = new AchievementPushedBack(1L, enrolment, achievement, LocalDateTime.now(), LocalDateTime.now());

        assertEquals(1L, achievementPushedBack.getId());
    }

    @Test
    public void testAchievementPushedBackByBuilder(){
        // Create a new AchievementPushedBack with builder and test 1 getter
        Enrolment enrolment = new Enrolment();
        Achievement achievement = new Achievement();
        AchievementPushedBack achievementPushedBack = AchievementPushedBack.builder()
                                                        .id(1L)
                                                        .enrolment(enrolment)
                                                        .achievement(achievement)
                                                        .pushedBackTime(LocalDateTime.now())
                                                        .updatedDateTime(LocalDateTime.now())
                                                        .build();

        assertEquals(1L, achievementPushedBack.getId());
    }

    @Test
    public void testAchievementPushedBackIsActive() {
        // Create a new AchievementPushedBack and test isActive method
        AchievementPushedBack achievementPushedBack = new AchievementPushedBack();
        
        achievementPushedBack.setPushedBackTime(LocalDateTime.now());
        assertTrue(achievementPushedBack.isActive());
        
        achievementPushedBack.setPushedBackTime(LocalDateTime.now().minusHours(23));
        assertTrue(achievementPushedBack.isActive());
        
        achievementPushedBack.setPushedBackTime(LocalDateTime.now().minusDays(1).minusMinutes(1));
        assertFalse(achievementPushedBack.isActive());
    }
}