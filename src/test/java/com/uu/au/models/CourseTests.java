package com.uu.au.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CourseTests {

    private Course createBasicCourse() {
        // Create a basic Course used as a basis in the tests
        Course course = new Course();
        course.setId(1L);
        course.setStartDate(LocalDate.now());
        course.setName("Course 1");
        course.setGitHubOrgURL("http://github.com/org1");
        course.setCourseWebURL("http://example.com/course1");
        course.setHelpModule(false);
        course.setDemoModule(false);
        course.setOnlyIntroductionTasks(false);
        course.setBurndownModule(false);
        course.setStatisticsModule(false);
        course.setExamMode(true);
        course.setProfilePictures(false);
        course.setCodeExamDemonstrationBlocker(LocalDate.of(2024, 12, 31));
        course.setClearQueuesUsingCron(false);
        course.setRoomSetting("PHYSICAL");
        course.setCreatedDateTime(LocalDateTime.of(2024, 1, 1, 12, 0));
        course.setUpdatedDateTime(LocalDateTime.of(2024, 1, 1, 12, 0));

        return course;
    }

    @Test
    public void testCourse() {
        // Create a new Course and test ALL getters and setters
        Course course = createBasicCourse();

        assertEquals(1L, course.getId());
        assertEquals(LocalDate.now(), course.getStartDate());
        assertEquals("Course 1", course.getName());
        assertEquals("http://github.com/org1", course.getGitHubOrgURL());
        assertEquals("http://example.com/course1", course.getCourseWebURL());
        assertFalse(course.isHelpModule());
        assertFalse(course.isDemoModule());
        assertFalse(course.isOnlyIntroductionTasks());
        assertFalse(course.isBurndownModule());
        assertFalse(course.isStatisticsModule());
        assertTrue(course.isExamMode());
        assertFalse(course.isProfilePictures());
        assertEquals(LocalDate.of(2024, 12, 31), course.getCodeExamDemonstrationBlocker());
        assertFalse(course.isClearQueuesUsingCron());
        assertEquals("PHYSICAL", course.getRoomSetting());
        assertEquals(LocalDateTime.of(2024, 1, 1, 12, 0), course.getCreatedDateTime());
        assertEquals(LocalDateTime.of(2024, 1, 1, 12, 0), course.getUpdatedDateTime());
    }

    @Test
    public void testCourseByBuilder(){
        // Create a new Course with builder and default setters and test 1 getter
        Course course = Course.builder()
                    .id(1L)
                    .startDate(LocalDate.now())
                    .name("Course 1")
                    .gitHubOrgURL("http://github.com/org1")
                    .courseWebURL("http://example.com/course1")
                    .codeExamDemonstrationBlocker(LocalDate.of(2024, 12, 31))
                    .roomSetting("PHYSICAL")
                    .createdDateTime(LocalDateTime.now())
                    .updatedDateTime(LocalDateTime.now())
                    .build();

        assertEquals(1L, course.getId());
    }

    @Test
    public void testCourseEquals() {
        // Create Course objects and test equals
        Course course1 = createBasicCourse();
        Course course2 = createBasicCourse();
        course2.setId(2L);

        assertEquals(course1, course1);
        assertNotEquals(course1, course2);
    }

    @Test
    public void testCourseHashCode() {
        // Create Course objects and test the hashCode method
        Course course1 = createBasicCourse();
        Course course2 = createBasicCourse();
        course2.setId(2L);

        assertEquals(course1.hashCode(), course1.hashCode());
        assertNotEquals(course1.hashCode(), course2.hashCode());
    }

    @Test
    public void testCourseToString() {
        // Create a Course and test the toString method
        Course course = createBasicCourse();

        assertTrue(course.toString().startsWith("Course(id=1"));
    }

    @Test
    public void testCodeExamDemonstrationBlocker() {
        // Create a new Course and test codeExamDemonstrationBlocker method
        Course course = createBasicCourse();
        
        course.setCodeExamDemonstrationBlocker(null);
        course.setStartDate(LocalDate.of(2024, 01, 01));
        assertEquals(LocalDate.of(2024, 01, 01), course.codeExamDemonstrationBlocker());

        course.setCodeExamDemonstrationBlocker(LocalDate.of(2024, 12, 31));
        assertEquals(LocalDate.of(2024, 12, 31), course.codeExamDemonstrationBlocker());
    }

    @Test
    public void testGetYear(){
        // Create a new Course and test getYear method
        Course course = createBasicCourse();
        
        course.setStartDate(LocalDate.of(2024, 01, 01));
        assertEquals(2024, course.getYear());
    }

    @Test
    public void testCurrentCourseWeek(){
        // Create a new Course and test currentCourseWeek method
        Course course = createBasicCourse();
        course.setStartDate(LocalDate.now().minusWeeks(4));
        assertEquals(4, course.currentCourseWeek());
        
        course.setStartDate(LocalDate.now().plusWeeks(2));
        assertEquals(0, course.currentCourseWeek());
    }
}