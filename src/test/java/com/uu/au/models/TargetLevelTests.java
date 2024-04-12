package com.uu.au.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.uu.au.enums.Level;

import java.time.LocalDateTime;

public class TargetLevelTests {

    private TargetLevel createBasicTargetLevel() {
        // Create a basic TargetLevel used as a basis in the tests
        TargetLevel targetLevel = new TargetLevel();
        targetLevel.setId(1L);
        targetLevel.setEnrolmentId(2L);
        targetLevel.setLevel(Level.GRADE_3);
        targetLevel.setChangeTime(LocalDateTime.of(2024, 1, 1, 12, 0));

        return targetLevel;
    }

    @Test
    public void testTargetLevel () {
        // Creat a new TargetLevel and test ALL getters and setters
        TargetLevel targetLevel = createBasicTargetLevel();

        assertEquals(1L, targetLevel.getId());
        assertEquals(2L, targetLevel.getEnrolmentId());
        assertEquals(Level.GRADE_3, targetLevel.getLevel());
        assertEquals(LocalDateTime.of(2024, 1, 1, 12, 0), targetLevel.getChangeTime());
    }

    @Test
    public void testTargetLevelByBuilder () {
        // Creat a new TargetLevel using builder and test 1 getter
        TargetLevel targetLevel = TargetLevel.builder()
            .id(1L)
            .enrolmentId(2L)
            .level(Level.GRADE_3)
            .changeTime(LocalDateTime.now())
            .build();

        assertEquals(1L, targetLevel.getId());
    }

    @Test
    public void testTargetLevelEquals() {
        // Create TargetLevel objects and test equals
        TargetLevel targetLevel1 = createBasicTargetLevel();
        TargetLevel targetLevel2 = createBasicTargetLevel();
        targetLevel2.setId(2L);

        assertTrue(targetLevel1.equals(targetLevel1));
        assertFalse(targetLevel1.equals(targetLevel2));
    }

    @Test
    public void testTargetLevelHashCode() {
        // Create TargetLevel objects and test hashCode
        TargetLevel targetLevel1 = createBasicTargetLevel();
        TargetLevel targetLevel2 = createBasicTargetLevel();
        targetLevel2.setId(2L);

        assertEquals(targetLevel1.hashCode(), targetLevel1.hashCode());
        assertNotEquals(targetLevel1.hashCode(), targetLevel2.hashCode());
    }

    @Test
    public void testTargetLevelToString() {
        // Create a TargetLevel and test the toString method
        TargetLevel targetLevel = createBasicTargetLevel();

        assertTrue(targetLevel.toString().startsWith("TargetLevel(id=1"));
    }
}