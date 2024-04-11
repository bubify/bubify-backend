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

    @Test
    public void testDemonstrationByBuilder() {
        // Create a new Demonstration by builder and test 1 getter
        Demonstration demonstration = Demonstration.builder()
            .id(1L)
            .requestTime(LocalDateTime.now())
            .pickupTime(LocalDateTime.now())
            .reportTime(LocalDateTime.now())
            .submitters(new HashSet<User>())
            .status(DemonstrationStatus.SUBMITTED)
            .achievements(new ArrayList<Achievement>())
            .examiner(new User())
            .zoomRoom("1234567890")
            .zoomPassword("password")
            .physicalRoom("Room 1")
            .build();
        
        assertEquals(1L, demonstration.getId());
    }

    @Test
    public void testIsActiveAndSubmittedOrClaimed() {
        // Create a new Demonstration and test isActiveAndSubmittedOrClaimed
        Demonstration demonstration = new Demonstration();

        // Demo is submitted and requested within 24 hours
        demonstration.setStatus(DemonstrationStatus.SUBMITTED);
        demonstration.setRequestTime(LocalDateTime.now());
        assertTrue(demonstration.isActiveAndSubmittedOrClaimed());

        // Demo is claimed and requested within 24 hours
        demonstration.setStatus(DemonstrationStatus.CLAIMED);
        assertTrue(demonstration.isActiveAndSubmittedOrClaimed());

        // Demo is submitted but requested more than 24 hours ago
        demonstration.setRequestTime(LocalDateTime.now().minusHours(25));
        assertFalse(demonstration.isActiveAndSubmittedOrClaimed());

        // Demo is neither submitted nor claimed and reuested more than 24 hours ago
        demonstration.setStatus(DemonstrationStatus.CANCELLED_BY_STUDENT);
        assertFalse(demonstration.isActiveAndSubmittedOrClaimed());

        // Demo is within 24 hours but not submitted or claimed
        demonstration.setStatus(DemonstrationStatus.CANCELLED_BY_TEACHER);
        demonstration.setRequestTime(LocalDateTime.now().minusHours(23));
        assertFalse(demonstration.isActiveAndSubmittedOrClaimed());
    }

    @Test
    public void testIsActive() {
        // Create a new Demonstration and test isActive
        Demonstration demonstration = new Demonstration();

        // Demo is not reported, requested within 24 hours and not in flight
        demonstration.setReportTime(null);
        demonstration.setRequestTime(LocalDateTime.now());
        demonstration.setStatus(DemonstrationStatus.CLAIMED);
        assertTrue(demonstration.isActive());
        
        // Demo is not reported, requested within 24 hours BUT in flight
        demonstration.setStatus(DemonstrationStatus.IN_FLIGHT);
        assertFalse(demonstration.isActive());

        // Demo is not reported or in flight BUT requested more than 24 hours ago
        demonstration.setStatus(DemonstrationStatus.COMPLETED);
        demonstration.setRequestTime(LocalDateTime.now().minusHours(25));
        assertFalse(demonstration.isActive());
        
        // Demo not in flight, requested within 24 hours BUT reported
        demonstration.setStatus(DemonstrationStatus.SUBMITTED);
        demonstration.setRequestTime(LocalDateTime.now().minusHours(23));
        demonstration.setReportTime(LocalDateTime.now());
        assertFalse(demonstration.isActive());
    }

    @Test
    public void testIsPickedUp() {
        // Create a new Demonstration and test isPickedUp
        Demonstration demonstration = new Demonstration();

        demonstration.setPickupTime(null);
        assertFalse(demonstration.isPickedUp());

        demonstration.setPickupTime(LocalDateTime.now());
        assertTrue(demonstration.isPickedUp());
    }

    @Test
    public void testIsReported() {
        // Create a new Demonstration and test isReported
        Demonstration demonstration = new Demonstration();

        demonstration.setReportTime(null);
        assertFalse(demonstration.isReported());

        demonstration.setReportTime(LocalDateTime.now());
        assertTrue(demonstration.isReported());
    }

    @Test
    public void testIsActiveAndClaimed() {
        // Create a new Demonstration and test isActiveAndClaimed
        Demonstration demonstration = new Demonstration();

        demonstration.setReportTime(null);
        demonstration.setRequestTime(LocalDateTime.now());
        demonstration.setStatus(DemonstrationStatus.CLAIMED);
        assertTrue(demonstration.isActiveAndClaimed());
        
        demonstration.setStatus(DemonstrationStatus.SUBMITTED);
        assertFalse(demonstration.isActiveAndClaimed());

        demonstration.setStatus(DemonstrationStatus.IN_FLIGHT);
        assertFalse(demonstration.isActiveAndClaimed());
    }

    @Test
    public void testPickupTimeInMinutes() {
        // Create a new Demonstration and test pickupTimeInMinutes
        Demonstration demonstration = new Demonstration();
        demonstration.setRequestTime(LocalDateTime.now().minusMinutes(30));
        demonstration.setPickupTime(LocalDateTime.now());
        assertEquals(30, demonstration.pickupTimeInMinutes());
    }

    @Test
    public void testRoundTripTimeInMinutes() {
        // Create a new Demonstration and test roundTripTimeInMinutes
        Demonstration demonstration = new Demonstration();
        demonstration.setRequestTime(LocalDateTime.now().minusMinutes(60));
        demonstration.setReportTime(LocalDateTime.now());
        assertEquals(60, demonstration.roundTripTimeInMinutes());
    }
}