package com.uu.au.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CourseTests {
    @Test
    public void testCourse() {
        // Create a new Course and test ALL getters and setters
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
        course.setCreatedDateTime(LocalDateTime.now());
        course.setUpdatedDateTime(LocalDateTime.now());

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
        assertNotNull(course.getCreatedDateTime());
        assertNotNull(course.getUpdatedDateTime());
    }

    @Test
    public void testCourseByBuilder(){
        // Create a new Course with builder and default setters and test all getters
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
        assertEquals(LocalDate.now(), course.getStartDate());
        assertEquals("Course 1", course.getName());
        assertEquals("http://github.com/org1", course.getGitHubOrgURL());
        assertEquals("http://example.com/course1", course.getCourseWebURL());
        assertTrue(course.isHelpModule());
        assertTrue(course.isDemoModule());
        assertTrue(course.isOnlyIntroductionTasks());
        assertTrue(course.isBurndownModule());
        assertTrue(course.isStatisticsModule());
        assertFalse(course.isExamMode());
        assertTrue(course.isProfilePictures());
        assertEquals(LocalDate.of(2024, 12, 31), course.getCodeExamDemonstrationBlocker());
        assertTrue(course.isClearQueuesUsingCron());
        assertEquals("PHYSICAL", course.getRoomSetting());
        assertNotNull(course.getCreatedDateTime());
        assertNotNull(course.getUpdatedDateTime());
    }

    @Test
    public void testCodeExamDemonstrationBlocker() {
        // Create a new Course and test codeExamDemonstrationBlocker method
        Course course = new Course();
        
        course.setCodeExamDemonstrationBlocker(null);
        course.setStartDate(LocalDate.of(2024, 01, 01));
        assertEquals(LocalDate.of(2024, 01, 01), course.codeExamDemonstrationBlocker());

        course.setCodeExamDemonstrationBlocker(LocalDate.of(2024, 12, 31));
        assertEquals(LocalDate.of(2024, 12, 31), course.codeExamDemonstrationBlocker());
    }

    @Test
    public void testGetYear(){
        // Create a new Course and test getYear method
        Course course = new Course();
        
        course.setStartDate(LocalDate.of(2024, 01, 01));
        assertEquals(2024, course.getYear());
    }

    @Test
    public void testCurrentCourseWeek(){
        // Create a new Course and test currentCourseWeek method
        Course course = new Course();
        course.setStartDate(LocalDate.now().minusWeeks(4));
        assertEquals(4, course.currentCourseWeek());
        
        course.setStartDate(LocalDate.now().plusWeeks(2));
        assertEquals(0, course.currentCourseWeek());
    }

    @Test
    public void testEquals(){
        // Create a new Course and test equals method
        Course course1 = new Course();
        course1.setId(1L);
        
        Course course2 = new Course();
        course2.setId(1L);

        Course course3 = new Course();
        course3.setId(2L);

        assertTrue(course1.equals((Object) course2));

        assertFalse(course1.equals((Object) course3));

        assertFalse(course1.equals(null));
    }

    @Test
    public void testHashCode(){
        // Create a new Course and test hashCode method
        Course course = new Course();
        course.setId(1L);
        assertNotNull(course.hashCode());
    }

    @Test
    public void testToString(){
        // Create a new Course and test toString method
        Course course = new Course();
        course.setId(1L);
        course.setStartDate(LocalDate.of(2024, 01, 01));
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
        course.setCreatedDateTime(null);
        course.setUpdatedDateTime(null);

        assertEquals("Course(id=1, startDate=2024-01-01, name=Course 1, gitHubOrgURL=http://github.com/org1, courseWebURL=http://example.com/course1, helpModule=false, demoModule=false, onlyIntroductionTasks=false, burndownModule=false, statisticsModule=false, examMode=true, profilePictures=false, codeExamDemonstrationBlocker=2024-12-31, clearQueuesUsingCron=false, roomSetting=PHYSICAL, createdDateTime=null, updatedDateTime=null)", course.toString());
    }
}