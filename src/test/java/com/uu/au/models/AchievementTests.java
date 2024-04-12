package com.uu.au.models;

import com.uu.au.enums.Level;
import com.uu.au.enums.AchievementType;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

public class AchievementTests {

    private Achievement createBasicAchievement() {
        // Create a basic Achievement used as a basis in the tests
        Achievement achievement = new Achievement();
        achievement.setId(1L);
        achievement.setCode("Code 1");
        achievement.setName("Achievement 1");
        achievement.setUrlToDescription("http://example.com/achievement1");
        achievement.setAchievementType(AchievementType.ACHIEVEMENT);
        achievement.setLevel(Level.GRADE_3);
        achievement.setCreatedDateTime(LocalDateTime.of(2024, 01, 01, 12, 00));
        achievement.setUpdatedDateTime(LocalDateTime.of(2024, 01, 01, 12, 00));

        return achievement;
    }

    @Test
    public void testAchievement() {
        // Create a new Achievement and test ALL getters and setters
        Achievement achievement = createBasicAchievement();

        assertEquals(1L, achievement.getId());
        assertEquals("Code 1", achievement.getCode());
        assertEquals("Achievement 1", achievement.getName());
        assertEquals("http://example.com/achievement1", achievement.getUrlToDescription());
        assertEquals(AchievementType.ACHIEVEMENT, achievement.getAchievementType());
        assertEquals(Level.GRADE_3, achievement.getLevel());
        assertEquals(LocalDateTime.of(2024, 01, 01, 12, 00), achievement.getCreatedDateTime());
        assertEquals(LocalDateTime.of(2024, 01, 01, 12, 00), achievement.getUpdatedDateTime());
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
    public void testAchievementEquals() {
        // Create Achievement objects and test equals
        Achievement achievement1 = createBasicAchievement();
        Achievement achievement2 = createBasicAchievement();
        Achievement achievement3 = createBasicAchievement();
        achievement3.setId(2L);

        // This class has the @EqualsAndHashCode(of={"id"}) annotation
        assertEquals(achievement1, achievement2);
        assertNotEquals(achievement1, achievement3);
    }
    
    @Test
    public void testAchievementHashCode() {
        // Create Achievement objects and test hashCode
        Achievement achievement1 = createBasicAchievement();
        Achievement achievement2 = createBasicAchievement();
        Achievement achievement3 = createBasicAchievement();
        achievement3.setId(2L);
        
        // This class has the @EqualsAndHashCode(of={"id"}) annotation
        assertEquals(achievement1.hashCode(), achievement2.hashCode());
        assertNotEquals(achievement1.hashCode(), achievement3.hashCode());
    }

    @Test
    public void testAchievementToString() {
        // Create a new Achievement and test toString
        Achievement achievement = createBasicAchievement();

        assertTrue(achievement.toString().startsWith("Achievement(id=1"));
    }

    @Test
    public void testAchievementRequiredForLevel() {
        // Create a new Achievement and test requiredForLevel method
        Achievement achievement = createBasicAchievement();

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
        Achievement achievement = createBasicAchievement();

        achievement.setAchievementType(AchievementType.ACHIEVEMENT);
        assertFalse(achievement.isIntroTask());

        achievement.setAchievementType(AchievementType.INTRO_LAB);
        assertTrue(achievement.isIntroTask());
    }

    @Test
    public void testIsCodeExam(){
        // Create a new Achievement and test isIntroTask method
        Achievement achievement = createBasicAchievement();

        achievement.setAchievementType(AchievementType.ACHIEVEMENT);
        assertFalse(achievement.isCodeExam());

        achievement.setAchievementType(AchievementType.CODE_EXAM);
        assertTrue(achievement.isCodeExam());
    }

    @Test
    public void testIsAssignment(){
        // Create a new Achievement and test isAssignment method
        Achievement achievement = createBasicAchievement();

        achievement.setCode("Z123");
        assertTrue(achievement.isAssignment());

        achievement.setCode("LAB123");
        assertFalse(achievement.isAssignment());
    }

    @Test
    public void testIsLab(){
        // Create a new Achievement and test isLab method
        Achievement achievement = createBasicAchievement();

        achievement.setCode("LAB123");
        assertTrue(achievement.isLab());

        achievement.setCode("Z123");
        assertFalse(achievement.isLab());
    }
}
