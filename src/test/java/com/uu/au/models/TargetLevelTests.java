package com.uu.au.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.uu.au.enums.Level;

import java.time.LocalDateTime;

public class TargetLevelTests {
    @Test
    public void testTargetLevel () {
        // Creat a new TargetLevel and test ALL getters and setters
        TargetLevel targetLevel = new TargetLevel();
        targetLevel.setId(1L);
        targetLevel.setEnrolmentId(2L);
        targetLevel.setLevel(Level.GRADE_3);
        targetLevel.setChangeTime(LocalDateTime.now());

        assertEquals(1L, targetLevel.getId());
        assertEquals(2L, targetLevel.getEnrolmentId());
        assertEquals(Level.GRADE_3, targetLevel.getLevel());
        assertNotNull(targetLevel.getChangeTime());
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
}