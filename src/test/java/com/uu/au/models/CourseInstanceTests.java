package com.uu.au.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CourseInstanceTests {
    @Test
    public void testCourseInstance() {
        // Create a new CourseInstance and test ALL getters and setters
        CourseInstance courseInstance = new CourseInstance();
        courseInstance.setId(1L);
        courseInstance.setStartDate(LocalDate.of(2024, 1, 1));
        courseInstance.setEndDate(LocalDate.of(2024, 12, 31));
        courseInstance.setAchievementsUnlocked(null);
        courseInstance.setAchievementsPushedBack(null);

        assertEquals(1L, courseInstance.getId());
        assertEquals(LocalDate.of(2024, 1, 1), courseInstance.getStartDate());
        assertEquals(LocalDate.of(2024, 12, 31), courseInstance.getEndDate());
        assertNull(courseInstance.getAchievementsUnlocked());
        assertNull(courseInstance.getAchievementsPushedBack());
    }

    // TODO: Add more tests
}