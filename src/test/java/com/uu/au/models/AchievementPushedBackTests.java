package com.uu.au.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

public class AchievementPushedBackTests {

    private AchievementPushedBack createBasicAchievementPushedBack() {
        // Create a basic AchievementPushedBack used as a basis in the tests
        AchievementPushedBack achievementPushedBack = new AchievementPushedBack();
        achievementPushedBack.setId(1L);

        Enrolment enrolment = new Enrolment();
        enrolment.setId(1L);
        achievementPushedBack.setEnrolment(enrolment);

        Achievement achievement = new Achievement();
        achievement.setId(1L);
        achievementPushedBack.setAchievement(achievement);

        achievementPushedBack.setPushedBackTime(LocalDateTime.of(2024, 01, 01, 12, 00));
        achievementPushedBack.setUpdatedDateTime(LocalDateTime.of(2024, 01, 01, 12, 00));

        return achievementPushedBack;
    }


    @Test
    public void testAchievementPushedBack() {
        // Create a new AchievementPushedBack and test ALL getters and setters
        AchievementPushedBack achievementPushedBack = createBasicAchievementPushedBack();

        assertEquals(1L, achievementPushedBack.getId());
        assertEquals(1L, achievementPushedBack.getEnrolment().getId());
        assertEquals(1L, achievementPushedBack.getAchievement().getId());
        assertEquals(LocalDateTime.of(2024, 01, 01, 12, 00), achievementPushedBack.getPushedBackTime());
        assertEquals(LocalDateTime.of(2024, 01, 01, 12, 00), achievementPushedBack.getUpdatedDateTime());
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
    public void testAchievementPushedBackEquals() {
        // Create AchievementPushedBack objects and test equals
        AchievementPushedBack achievementPushedBack1 = createBasicAchievementPushedBack();
        AchievementPushedBack achievementPushedBack2 = createBasicAchievementPushedBack();
        achievementPushedBack2.setId(2L);

        assertEquals(achievementPushedBack1, achievementPushedBack1);
        assertNotEquals(achievementPushedBack1, achievementPushedBack2);
    }

    @Test
    public void testAchievementPushedBackHashCode() {
        // Create AchievementPushedBack objects and test hashCode
        AchievementPushedBack achievementPushedBack1 = createBasicAchievementPushedBack();
        AchievementPushedBack achievementPushedBack2 = createBasicAchievementPushedBack();
        achievementPushedBack2.setId(2L);

        assertEquals(achievementPushedBack1.hashCode(), achievementPushedBack1.hashCode());
        assertNotEquals(achievementPushedBack1.hashCode(), achievementPushedBack2.hashCode());
    }

    @Test
    public void testAchievementPushedBackToString() {
        // Create a new AchievementPushedBack and test toString method
        AchievementPushedBack achievementPushedBack = createBasicAchievementPushedBack();
        
        assertTrue(achievementPushedBack.toString().startsWith("AchievementPushedBack(id=1"));
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