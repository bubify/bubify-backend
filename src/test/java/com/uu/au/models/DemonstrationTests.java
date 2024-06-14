package com.uu.au.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.uu.au.enums.DemonstrationStatus;

import java.time.LocalDateTime;
import java.util.*;

public class DemonstrationTests {

    private Demonstration createBasicDemonstration() {
        // Create a basic Demonstration used as a basis in the tests
        Demonstration demonstration = new Demonstration();
        demonstration.setId(1L);
        demonstration.setRequestTime(LocalDateTime.of(2024, 1, 1, 12, 0));
        demonstration.setPickupTime(null);
        demonstration.setReportTime(null);

        // Create a set with 1 user
        Set<User> submitters = new HashSet<User>();
        User user = new User();
        user.setId(1L);
        submitters.add(user);
        demonstration.setSubmitters(submitters);

        demonstration.setStatus(DemonstrationStatus.SUBMITTED);

        // Create a list with 1 achievement
        List<Achievement> achievements = new ArrayList<Achievement>();
        Achievement achievement1 = new Achievement();
        achievement1.setId(1L);
        achievements.add(achievement1);
        demonstration.setAchievements(achievements);
        
        User examiner = new User();
        examiner.setId(1L);
        demonstration.setExaminer(examiner);
        demonstration.setZoomRoom("1234567890");
        demonstration.setZoomPassword("password");
        demonstration.setPhysicalRoom("Room 1");

        return demonstration;
    }

    @Test
    public void testDemonstration() {
        // Create a new Demonstration and test ALL getters and setters
        Demonstration demonstration = createBasicDemonstration();
        demonstration.setPickupTime(LocalDateTime.of(2024, 1, 1, 13, 0));
        demonstration.setReportTime(LocalDateTime.of(2024, 1, 1, 14, 0));
        
        assertEquals(1L, demonstration.getId());
        assertEquals(LocalDateTime.of(2024, 1, 1, 12, 0), demonstration.getRequestTime());
        assertEquals(LocalDateTime.of(2024, 1, 1, 13, 0), demonstration.getPickupTime());
        assertEquals(LocalDateTime.of(2024, 1, 1, 14, 0), demonstration.getReportTime());
        assertEquals(1, demonstration.getSubmitters().size());
        assertEquals(1L, demonstration.getSubmitters().iterator().next().getId());
        assertEquals(DemonstrationStatus.SUBMITTED, demonstration.getStatus());
        assertEquals(1, demonstration.getAchievements().size());
        assertEquals(1L, demonstration.getAchievements().get(0).getId());
        assertEquals(1L, demonstration.getExaminer().getId());
        assertEquals("1234567890", demonstration.getZoomRoom());
        assertEquals("password", demonstration.getZoomPassword());
        assertEquals("Room 1", demonstration.getPhysicalRoom());

        // Add another user to the submitters set and another achievement to the achievements list
        User user2 = new User();
        user2.setId(2L);
        demonstration.getSubmitters().add(user2);
        Achievement achievement2 = new Achievement();
        achievement2.setId(2L);
        demonstration.getAchievements().add(achievement2);

        assertEquals(2, demonstration.getSubmitters().size());
        assertEquals(2, demonstration.getAchievements().size());
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
    public void testDemonstrationEquals() {
        // Create Demonstration objects and test equals
        Demonstration demonstration1 = createBasicDemonstration();
        Demonstration demonstration2 = createBasicDemonstration();
        demonstration2.setId(2L);

        assertEquals(demonstration1, demonstration1);
        assertNotEquals(demonstration1, demonstration2);
    }

    @Test
    public void testDemonstrationHashCode() {
        // Create Demonstration objects and test the hashCode method
        Demonstration demonstration1 = createBasicDemonstration();
        Demonstration demonstration2 = createBasicDemonstration();
        demonstration2.setId(2L);

        assertEquals(demonstration1.hashCode(), demonstration1.hashCode());
        assertNotEquals(demonstration1.hashCode(), demonstration2.hashCode());
    }

    @Test
    public void testDemonstrationToString() {
        // Create a Demonstration and test the toString method
        Demonstration demonstration = createBasicDemonstration();

        assertTrue(demonstration.toString().startsWith("Demonstration(id=1"));
    }

    @Test
    public void testIsActiveAndSubmittedOrClaimed() {
        // Create a new Demonstration and test isActiveAndSubmittedOrClaimed
        Demonstration demonstration = createBasicDemonstration();

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
        Demonstration demonstration = createBasicDemonstration();

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
        Demonstration demonstration = createBasicDemonstration();

        demonstration.setPickupTime(null);
        assertFalse(demonstration.isPickedUp());

        demonstration.setPickupTime(LocalDateTime.now());
        assertTrue(demonstration.isPickedUp());
    }

    @Test
    public void testIsReported() {
        // Create a new Demonstration and test isReported
        Demonstration demonstration = createBasicDemonstration();

        demonstration.setReportTime(null);
        assertFalse(demonstration.isReported());

        demonstration.setReportTime(LocalDateTime.now());
        assertTrue(demonstration.isReported());
    }

    @Test
    public void testIsActiveAndClaimed() {
        // Create a new Demonstration and test isActiveAndClaimed
        Demonstration demonstration = createBasicDemonstration();

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
        Demonstration demonstration = createBasicDemonstration();
        demonstration.setRequestTime(LocalDateTime.now().minusMinutes(30));
        demonstration.setPickupTime(LocalDateTime.now());
        assertEquals(30, demonstration.pickupTimeInMinutes());
    }

    @Test
    public void testRoundTripTimeInMinutes() {
        // Create a new Demonstration and test roundTripTimeInMinutes
        Demonstration demonstration = createBasicDemonstration();
        demonstration.setRequestTime(LocalDateTime.now().minusMinutes(60));
        demonstration.setReportTime(LocalDateTime.now());
        assertEquals(60, demonstration.roundTripTimeInMinutes());
    }
}