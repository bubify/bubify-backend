package com.uu.au.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

public class AchievementUnlockedTests {

    private AchievementUnlocked createBasicAchievementUnlocked() {
        // Create a basic AchievementUnlocked used as a basis in the tests
        AchievementUnlocked achievementUnlocked = new AchievementUnlocked();
        achievementUnlocked.setId(1L);

        Enrolment enrolment = new Enrolment();
        enrolment.setId(1L);
        achievementUnlocked.setEnrolment(enrolment);

        Achievement achievement = new Achievement();
        achievement.setId(1L);
        achievementUnlocked.setAchievement(achievement);

        achievementUnlocked.setUnlockTime(LocalDateTime.of(2024, 01, 01, 12, 00));
        achievementUnlocked.setUpdatedDateTime(LocalDateTime.of(2024, 01, 01, 12, 00));

        return achievementUnlocked;
    }

    @Test
    public void testAchievementUnlocked() {
        // Create a new AchievementUnlocked and test ALL getters and setters
        AchievementUnlocked achievementUnlocked = createBasicAchievementUnlocked();

        assertEquals(1L, achievementUnlocked.getId());
        assertEquals(1L, achievementUnlocked.getEnrolment().getId());
        assertEquals(1L, achievementUnlocked.getAchievement().getId());
        assertEquals(LocalDateTime.of(2024, 01, 01, 12, 00), achievementUnlocked.getUnlockTime());
        assertEquals(LocalDateTime.of(2024, 01, 01, 12, 00), achievementUnlocked.getUpdatedDateTime());
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

    @Test
    public void testAchievementUnlockedEquals() {
        // Create AchievementUnlocked objects and test equals
        AchievementUnlocked achievementUnlocked1 = createBasicAchievementUnlocked();
        AchievementUnlocked achievementUnlocked2 = createBasicAchievementUnlocked();
        achievementUnlocked2.setId(2L);

        assertEquals(achievementUnlocked1, achievementUnlocked1);
        assertNotEquals(achievementUnlocked1, achievementUnlocked2);
    }

    @Test
    public void testAchievementUnlockedHashCode() {
        // Create AchievementUnlocked objects and test hashCode
        AchievementUnlocked achievementUnlocked1 = createBasicAchievementUnlocked();
        AchievementUnlocked achievementUnlocked2 = createBasicAchievementUnlocked();
        achievementUnlocked2.setId(2L);

        assertEquals(achievementUnlocked1.hashCode(), achievementUnlocked1.hashCode());
        assertNotEquals(achievementUnlocked1.hashCode(), achievementUnlocked2.hashCode());
    }

    @Test
    public void testAchievementUnlockedToString() {
        // Create a new AchievementUnlocked and test toString
        AchievementUnlocked achievementUnlocked = createBasicAchievementUnlocked();

        assertTrue(achievementUnlocked.toString().startsWith("AchievementUnlocked(id=1"));
    }
}