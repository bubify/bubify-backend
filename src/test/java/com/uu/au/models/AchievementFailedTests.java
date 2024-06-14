package com.uu.au.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

public class AchievementFailedTests {

    private AchievementFailed createBasicAchievementFailed() {
        // Create a basic AchievementFailed used as a basis in the tests
        AchievementFailed achievementFailed = new AchievementFailed();
        achievementFailed.setId(1L);

        Enrolment enrolment = new Enrolment();
        enrolment.setId(1L);
        achievementFailed.setEnrolment(enrolment);

        Achievement achievement = new Achievement();
        achievement.setId(1L);
        achievementFailed.setAchievement(achievement);

        achievementFailed.setFailedTime(LocalDateTime.of(2024, 01, 01, 12, 00));
        achievementFailed.setUpdatedDateTime(LocalDateTime.of(2024, 01, 01, 12, 00));

        return achievementFailed;
    }

    @Test
    public void testAchievementFailed() {
        // Create a new AchievementFailed and test ALL getters and setters
        AchievementFailed achievementFailed = createBasicAchievementFailed();

        assertEquals(1L, achievementFailed.getId());
        assertEquals(1L, achievementFailed.getEnrolment().getId());
        assertEquals(1L, achievementFailed.getAchievement().getId());
        assertEquals(LocalDateTime.of(2024, 01, 01, 12, 00), achievementFailed.getFailedTime());
        assertEquals(LocalDateTime.of(2024, 01, 01, 12, 00), achievementFailed.getUpdatedDateTime());
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

    @Test
    public void testAchievementFailedEquals() {
        // Create AchievementFailed objects and test equals
        AchievementFailed achievementFailed1 = createBasicAchievementFailed();
        AchievementFailed achievementFailed2 = createBasicAchievementFailed();
        achievementFailed2.setId(2L);
        
        assertEquals(achievementFailed1, achievementFailed1);
        assertNotEquals(achievementFailed1, achievementFailed2);
    }
    
    @Test
    public void testAchievementFailedHashCode() {
        // Create AchievementFailed objects and test hashCode
        AchievementFailed achievementFailed1 = createBasicAchievementFailed();
        AchievementFailed achievementFailed2 = createBasicAchievementFailed();
        achievementFailed2.setId(2L);
        
        assertEquals(achievementFailed1.hashCode(), achievementFailed1.hashCode());
        assertNotEquals(achievementFailed1.hashCode(), achievementFailed2.hashCode());
    }

    @Test
    public void testAchievementFailedToString() {
        // Create a new AchievementFailed and test toString
        AchievementFailed achievementFailed = createBasicAchievementFailed();
        
        assertTrue(achievementFailed.toString().startsWith("AchievementFailed(id=1"));
    }
}