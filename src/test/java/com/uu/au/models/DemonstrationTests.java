package com.uu.au.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.uu.au.enums.DemonstrationStatus;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class DemonstrationTests {
    @Test
    public void testDemonstration() {
        // Create a new Demonstration and test ALL getters and setters
        Demonstration demonstration = new Demonstration();
        demonstration.setId(1L);
        demonstration.setRequestTime(LocalDateTime.now());
        demonstration.setPickupTime(LocalDateTime.now());
        demonstration.setReportTime(LocalDateTime.now());

        // Create a set with 2 users
        Set<User> submitters = new HashSet<User>();
        User user1 = new User();
        User user2 = new User();
        submitters.add(user1);
        submitters.add(user2);
        demonstration.setSubmitters(submitters);

        demonstration.setStatus(DemonstrationStatus.SUBMITTED);

        // Create a list with 2 achievements
        List<Achievement> achievements = new ArrayList<Achievement>();
        Achievement achievement1 = new Achievement();
        Achievement achievement2 = new Achievement();
        achievements.add(achievement1);
        achievements.add(achievement2);
        demonstration.setAchievements(achievements);
        
        User examiner = new User();
        demonstration.setExaminer(examiner);
        demonstration.setZoomRoom("1234567890");
        demonstration.setZoomPassword("password");
        demonstration.setPhysicalRoom("Room 1");
        
        assertEquals(1L, demonstration.getId());
        assertNotNull(demonstration.getRequestTime());
        assertNotNull(demonstration.getPickupTime());
        assertNotNull(demonstration.getReportTime());
        assertEquals(submitters, demonstration.getSubmitters());
        assertEquals(DemonstrationStatus.SUBMITTED, demonstration.getStatus());
        assertEquals(achievements, demonstration.getAchievements());
        assertEquals(examiner, demonstration.getExaminer());
        assertEquals("1234567890", demonstration.getZoomRoom());
        assertEquals("password", demonstration.getZoomPassword());
        assertEquals("Room 1", demonstration.getPhysicalRoom());
    }

    // TODO: Add more tests
}