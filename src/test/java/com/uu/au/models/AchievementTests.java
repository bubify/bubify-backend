package com.uu.au.models;

import com.uu.au.enums.Level;
import com.uu.au.enums.AchievementType;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

public class AchievementTests {
    @Test
    public void testAchievement() {
        // Create a new Achievement and test ALL getters and setters
        Achievement achievement = new Achievement();
        achievement.setId(1L);
        achievement.setCode("Code 1");
        achievement.setName("Achievement 1");
        achievement.setUrlToDescription("http://example.com/achievement1");
        achievement.setAchievementType(AchievementType.ACHIEVEMENT);
        achievement.setLevel(Level.forValues("GRADE_3"));
        achievement.setCreatedDateTime(LocalDateTime.now());
        achievement.setUpdatedDateTime(LocalDateTime.now());

        assertEquals(1L, achievement.getId());
        assertEquals("Code 1", achievement.getCode());
        assertEquals("Achievement 1", achievement.getName());
        assertEquals("http://example.com/achievement1", achievement.getUrlToDescription());
        assertEquals(AchievementType.ACHIEVEMENT, achievement.getAchievementType());
        assertEquals(Level.GRADE_3, achievement.getLevel());
        assertNotNull(achievement.getCreatedDateTime());
        assertNotNull(achievement.getUpdatedDateTime());
    }

    @Test
    public void testAchievementAllArgConstruct(){
        // Create a new Achievement with all arguments constructor and test 1 getter
        Achievement achievement = new Achievement(1L, "Code 1", "Achievement 1", "http://example.com/achievement1", AchievementType.ACHIEVEMENT, Level.GRADE_3, LocalDateTime.now(), LocalDateTime.now());

        assertEquals(1L, achievement.getId());
    }

    @Test
    public void testAchievementByBuilder(){
        // Create a new Achievement with builder and test 1 getter
        Achievement achievement = Achievement.builder()
                                    .id(1L)
                                    .code("Code 1")
                                    .name("Achievement 1")
                                    .urlToDescription("http://example.com/achievement1")
                                    .achievementType(AchievementType.ACHIEVEMENT).level(Level.GRADE_3)
                                    .createdDateTime(LocalDateTime.now())
                                    .updatedDateTime(LocalDateTime.now())
                                    .build();

        assertEquals(1L, achievement.getId());
    }

    @Test
    public void testAchievementRequiredForLevel() {
        // Create a new Achievement and test requiredForLevel method
        Achievement achievement = new Achievement();

        achievement.setLevel(Level.GRADE_3);
        assertTrue(achievement.requiredForLevel(Level.GRADE_3));
        assertTrue(achievement.requiredForLevel(Level.GRADE_4));
        assertTrue(achievement.requiredForLevel(Level.GRADE_5));

        achievement.setLevel(Level.GRADE_4);
        assertFalse(achievement.requiredForLevel(Level.GRADE_3));
        assertTrue(achievement.requiredForLevel(Level.GRADE_4));
        assertTrue(achievement.requiredForLevel(Level.GRADE_5));

        achievement.setLevel(Level.GRADE_5);
        assertFalse(achievement.requiredForLevel(Level.GRADE_3));
        assertFalse(achievement.requiredForLevel(Level.GRADE_4));
        assertTrue(achievement.requiredForLevel(Level.GRADE_5));
    }

    @Test
    public void testAchievementIsIntroTask() {
        // Create a new Achievement and test isIntroTask method
        Achievement achievement = new Achievement();

        achievement.setAchievementType(AchievementType.ACHIEVEMENT);
        assertFalse(achievement.isIntroTask());

        achievement.setAchievementType(AchievementType.INTRO_LAB);
        assertTrue(achievement.isIntroTask());
    }

    @Test
    public void testIsCodeExam(){
        // Create a new Achievement and test isIntroTask method
        Achievement achievement = new Achievement();

        achievement.setAchievementType(AchievementType.ACHIEVEMENT);
        assertFalse(achievement.isCodeExam());

        achievement.setAchievementType(AchievementType.CODE_EXAM);
        assertTrue(achievement.isCodeExam());
    }

    @Test
    public void testIsAssignment(){
        // Create a new Achievement and test isAssignment method
        Achievement achievement = new Achievement();

        achievement.setCode("Z123");
        assertTrue(achievement.isAssignment());

        achievement.setCode("LAB123");
        assertFalse(achievement.isAssignment());
    }

    @Test
    public void testIsLab(){
        // Create a new Achievement and test isLab method
        Achievement achievement = new Achievement();

        achievement.setCode("LAB123");
        assertTrue(achievement.isLab());

        achievement.setCode("Z123");
        assertFalse(achievement.isLab());
    }
}
