package com.uu.au.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.uu.au.enums.Role;
import com.uu.au.enums.DemonstrationStatus;

import java.time.LocalDateTime;
import java.util.*;

public class HelpRequestTests {

    private HelpRequest createBasicHelpRequest() {
        // Create a basic HelpRequest used as a basis in the tests
        HelpRequest helpRequest = new HelpRequest();
        helpRequest.setId(1L);
        helpRequest.setRequestTime(LocalDateTime.of(2024, 1, 1, 12, 0));
        helpRequest.setPickupTime(null);
        helpRequest.setReportTime(null);
        
        User user = new User();
        user.setId(1L);
        User helper = new User();
        helper.setId(2L);
        Set<User> submitters = new HashSet<>();
        submitters.add(user);

        helpRequest.setSubmitters(submitters);
        helpRequest.setHelper(helper);
        helpRequest.setMessage("Help me!");
        helpRequest.setZoomRoom("1234567890");
        helpRequest.setZoomPassword("password");
        helpRequest.setPhysicalRoom("Room 1");
        helpRequest.setStatus(DemonstrationStatus.SUBMITTED);
        
        return helpRequest;
    }
    
    @Test
    public void testHelpRequest (){
        // Create a new HelpRequest and check ALL getters and setters
        HelpRequest helpRequest = createBasicHelpRequest();
        helpRequest.setPickupTime(LocalDateTime.of(2024, 1, 1, 13, 0));
        helpRequest.setReportTime(LocalDateTime.of(2024, 1, 1, 14, 0));
        
        assertEquals(1L, helpRequest.getId());
        assertEquals(LocalDateTime.of(2024, 1, 1, 12, 0), helpRequest.getRequestTime());
        assertEquals(LocalDateTime.of(2024, 1, 1, 13, 0), helpRequest.getPickupTime());
        assertEquals(LocalDateTime.of(2024, 1, 1, 14, 0), helpRequest.getReportTime());
        assertEquals(1, helpRequest.getSubmitters().size());
        assertEquals(2L, helpRequest.getHelper().getId());
        assertEquals("Help me!", helpRequest.getMessage());
        assertEquals("1234567890", helpRequest.getZoomRoom());
        assertEquals("password", helpRequest.getZoomPassword());
        assertEquals("Room 1", helpRequest.getPhysicalRoom());
        assertEquals(DemonstrationStatus.SUBMITTED, helpRequest.getStatus());
    }

    @Test
    public void testHelpRequestByBuilder (){
        // Create a new HelpRequest with builder and check 1 getter
        LocalDateTime requestTime = LocalDateTime.now();
        LocalDateTime pickupTime = LocalDateTime.now();
        LocalDateTime reportTime = LocalDateTime.now();

        User user = new User();
        user.setId(1L);

        User helper = new User();
        helper.setId(2L);

        HelpRequest helpRequest = HelpRequest.builder()
            .id(1L)
            .requestTime(requestTime)
            .pickupTime(pickupTime)
            .reportTime(reportTime)
            .submitters(Set.of(user))
            .helper(helper)
            .message("Help me!")
            .zoomRoom("1234567890")
            .zoomPassword("password")
            .physicalRoom("Room 1")
            .status(DemonstrationStatus.SUBMITTED)
            .build();

        assertEquals(1L, helpRequest.getId());
    }

    @Test
    public void testHelpRequestEquals (){
        // Create HelpRequest objects and test equals
        HelpRequest helpRequest1 = createBasicHelpRequest();
        HelpRequest helpRequest2 = createBasicHelpRequest();
        helpRequest2.setId(2L);

        assertEquals(helpRequest1, helpRequest1);
        assertNotEquals(helpRequest1, helpRequest2);
    }

    @Test
    public void testHelpRequestHashCode (){
        // Create HelpRequest objects and test the hashCode method
        HelpRequest helpRequest1 = createBasicHelpRequest();
        HelpRequest helpRequest2 = createBasicHelpRequest();
        helpRequest2.setId(2L);

        assertEquals(helpRequest1.hashCode(), helpRequest1.hashCode());
        assertNotEquals(helpRequest1.hashCode(), helpRequest2.hashCode());
    }

    @Test
    public void testHelpRequestToString (){
        // Create a new HelpRequest and test the toString method
        HelpRequest helpRequest = createBasicHelpRequest();
        
        assertTrue(helpRequest.toString().startsWith("HelpRequest(id=1"));
    }

    @Test
    public void testHelpRequestIsActive (){
        // Create a new HelpRequest and check if it is active
        HelpRequest helpRequest = createBasicHelpRequest();
        
        // ReportTime is null, RequestTime within 24 hours and status is not IN_FLIGHT
        helpRequest.setRequestTime(LocalDateTime.now());
        helpRequest.setReportTime(null);
        helpRequest.setStatus(DemonstrationStatus.SUBMITTED);
        assertTrue(helpRequest.isActive());
        
        // ReportTime is null, RequestTime within 24 hours BUT status is IN_FLIGHT
        helpRequest.setRequestTime(LocalDateTime.now());
        helpRequest.setReportTime(null);
        helpRequest.setStatus(DemonstrationStatus.IN_FLIGHT);
        assertFalse(helpRequest.isActive());

        // RequestTime within 24 hours and status is not IN_FLIGHT BUT ReportTime is not null
        helpRequest.setRequestTime(LocalDateTime.now());
        helpRequest.setReportTime(LocalDateTime.now());
        helpRequest.setStatus(DemonstrationStatus.CLAIMED);
        assertFalse(helpRequest.isActive());

        // ReportTime is null and status is not IN_FLIGHT BUT RequestTime is more than 24 hours
        helpRequest.setRequestTime(LocalDateTime.now().minusHours(25));
        helpRequest.setReportTime(null);
        helpRequest.setStatus(DemonstrationStatus.CANCELLED_BY_STUDENT);
        assertFalse(helpRequest.isActive());
    }

    @Test
    public void testHelpRequestIsActiveAndSubmitted (){
        // Create a new HelpRequest and check if it is active and submitted
        HelpRequest helpRequest = createBasicHelpRequest();
        
        // ReportTime is null, RequestTime within 24 hours and status is SUBMITTED
        helpRequest.setRequestTime(LocalDateTime.now());
        helpRequest.setReportTime(null);
        helpRequest.setStatus(DemonstrationStatus.SUBMITTED);
        assertTrue(helpRequest.isActiveAndSubmitted());
        
        // ReportTime is null, RequestTime within 24 hours BUT status is not SUBMITTED
        helpRequest.setRequestTime(LocalDateTime.now());
        helpRequest.setReportTime(null);
        helpRequest.setStatus(DemonstrationStatus.CANCELLED_BY_TEACHER);
        assertFalse(helpRequest.isActiveAndSubmitted());

        // ReportTime is not null, RequestTime within 24 hours and status is SUBMITTED
        helpRequest.setRequestTime(LocalDateTime.now());
        helpRequest.setReportTime(LocalDateTime.now());
        helpRequest.setStatus(DemonstrationStatus.COMPLETED);
        assertFalse(helpRequest.isActiveAndSubmitted());
    }

    @Test
    public void testHelpRequestIsActiveAndClaimed (){
        // Create a new HelpRequest and check if it is active and claimed
        HelpRequest helpRequest = createBasicHelpRequest();
        
        // ReportTime is null, RequestTime within 24 hours and status is CLAIMED
        helpRequest.setRequestTime(LocalDateTime.now());
        helpRequest.setReportTime(null);
        helpRequest.setStatus(DemonstrationStatus.CLAIMED);
        assertTrue(helpRequest.isActiveAndClaimed());
        
        // ReportTime is null, RequestTime within 24 hours BUT status is not CLAIMED
        helpRequest.setRequestTime(LocalDateTime.now());
        helpRequest.setReportTime(null);
        helpRequest.setStatus(DemonstrationStatus.SUBMITTED);
        assertFalse(helpRequest.isActiveAndClaimed());

        // ReportTime is not null, RequestTime within 24 hours and status is CLAIMED
        helpRequest.setRequestTime(LocalDateTime.now());
        helpRequest.setReportTime(LocalDateTime.now());
        helpRequest.setStatus(DemonstrationStatus.CLAIMED);
        assertFalse(helpRequest.isActiveAndClaimed());
    }

    @Test
    public void testHelpRequestIsActiveAndSubmittedOrClaimedOrInFlight (){
        // Create a new HelpRequest and check if it is active and submitted or claimed or in flight
        HelpRequest helpRequest = createBasicHelpRequest();
        
        // ReportTime is null, RequestTime within 24 hours and status is SUBMITTED/CLAIMED/IN_FLIGHT
        helpRequest.setRequestTime(LocalDateTime.now());
        helpRequest.setReportTime(null);
        helpRequest.setStatus(DemonstrationStatus.SUBMITTED);
        assertTrue(helpRequest.isActiveAndSubmittedOrClaimedOrInFlight());
        
        helpRequest.setStatus(DemonstrationStatus.CLAIMED);
        assertTrue(helpRequest.isActiveAndSubmittedOrClaimedOrInFlight());
        
        // BUG? Could not be ACTIVE and IN_FLIGHT at the same time!
        // helpRequest.setStatus(DemonstrationStatus.IN_FLIGHT);
        // assertTrue(helpRequest.isActiveAndSubmittedOrClaimedOrInFlight());

        // ReportTime is null, RequestTime within 24 hours BUT status is not SUBMITTED, CLAIMED or IN_FLIGHT
        helpRequest.setRequestTime(LocalDateTime.now());
        helpRequest.setReportTime(null);
        helpRequest.setStatus(DemonstrationStatus.CANCELLED_BY_STUDENT);
        assertFalse(helpRequest.isActiveAndSubmittedOrClaimedOrInFlight());
        
        // ReportTime is not null, RequestTime within 24 hours and status is not SUBMITTED, CLAIMED or IN_FLIGHT
        helpRequest.setRequestTime(LocalDateTime.now());
        helpRequest.setReportTime(LocalDateTime.now());
        helpRequest.setStatus(DemonstrationStatus.COMPLETED);
        assertFalse(helpRequest.isActiveAndSubmittedOrClaimedOrInFlight());
    }

    @Test
    public void testHelpRequestIsActiveAndSubmittedOrClaimed (){
        // Create a new HelpRequest and check if it is active and submitted or claimed
        HelpRequest helpRequest = createBasicHelpRequest();
        
        // ReportTime is null, RequestTime within 24 hours and status is SUBMITTED/CLAIMED
        helpRequest.setRequestTime(LocalDateTime.now());
        helpRequest.setReportTime(null);
        helpRequest.setStatus(DemonstrationStatus.SUBMITTED);
        assertTrue(helpRequest.isActiveAndSubmittedOrClaimed());
        
        helpRequest.setStatus(DemonstrationStatus.CLAIMED);
        assertTrue(helpRequest.isActiveAndSubmittedOrClaimed());
        
        // ReportTime is null, RequestTime within 24 hours BUT status is not SUBMITTED or CLAIMED
        helpRequest.setRequestTime(LocalDateTime.now());
        helpRequest.setReportTime(null);
        helpRequest.setStatus(DemonstrationStatus.CANCELLED_BY_TEACHER);
        assertFalse(helpRequest.isActiveAndSubmittedOrClaimed());

        // ReportTime is not null, RequestTime within 24 hours and status is not SUBMITTED or CLAIMED
        helpRequest.setRequestTime(LocalDateTime.now());
        helpRequest.setReportTime(LocalDateTime.now());
        helpRequest.setStatus(DemonstrationStatus.COMPLETED);
        assertFalse(helpRequest.isActiveAndSubmittedOrClaimed());
    }

    @Test
    public void testHelpRequestIsPickedUp (){
        // Create a new HelpRequest and check if it is picked up
        HelpRequest helpRequest = createBasicHelpRequest();
        
        // Status is CLAIMED
        helpRequest.setStatus(DemonstrationStatus.CLAIMED);
        assertTrue(helpRequest.isPickedUp());
        
        // Status is not CLAIMED
        helpRequest.setStatus(DemonstrationStatus.SUBMITTED);
        assertFalse(helpRequest.isPickedUp());
    }

    @Test
    public void testHelpRequestIncludesSubmitter (){
        // Create a new HelpRequest and check if it includes a submitter
        HelpRequest helpRequest = createBasicHelpRequest();
        helpRequest.setSubmitters(null);
        
        User user = new User();
        user.setId(1L);
        Set<User> submitters = new HashSet<>();
        submitters.add(user);

        // Submitters is null
        assertFalse(helpRequest.includesSubmitter(user));

        // Submitters is not null and contains the user
        helpRequest.setSubmitters(submitters);
        assertTrue(helpRequest.includesSubmitter(user));
        
        // Submitters is not null, contains 2 submitters but not the user
        User user2 = new User();
        user2.setId(2L);
        submitters.add(user2);
        helpRequest.setSubmitters(submitters);
        
        assertTrue(helpRequest.includesSubmitter(user));
        assertTrue(helpRequest.includesSubmitter(user2));
        
        // Submitters is not null but does not contain the user
        User user3 = new User();
        user3.setId(3L);

        assertFalse(helpRequest.includesSubmitter(user3));
    }

    @Test
    public void testPickupTimeInMinutes (){
        // Create a new HelpRequest and check pickup times
        HelpRequest helpRequest = createBasicHelpRequest();
        
        helpRequest.setRequestTime(LocalDateTime.now().minusMinutes(5));
        helpRequest.setPickupTime(LocalDateTime.now());
        assertEquals(5, helpRequest.pickupTimeInMinutes());

        helpRequest.setRequestTime(LocalDateTime.now().minusMinutes(15));
        helpRequest.setPickupTime(LocalDateTime.now().minusMinutes(5));
        assertEquals(10, helpRequest.pickupTimeInMinutes());
        
        helpRequest.setRequestTime(LocalDateTime.now());
        helpRequest.setPickupTime(LocalDateTime.now());
        assertEquals(0, helpRequest.pickupTimeInMinutes());
        
        // RequestTime is after PickupTime, should return negative value?
        helpRequest.setRequestTime(LocalDateTime.now());
        helpRequest.setPickupTime(LocalDateTime.now().minusMinutes(5));
        assertTrue(helpRequest.pickupTimeInMinutes() < 0);
    }

    @Test
    public void testRoundTripTimeInMinutes (){
        // Create a new HelpRequest and check round trip times
        HelpRequest helpRequest = createBasicHelpRequest();
        
        helpRequest.setRequestTime(LocalDateTime.now().minusMinutes(5));
        helpRequest.setReportTime(LocalDateTime.now());
        assertEquals(5, helpRequest.roundTripTimeInMinutes());

        helpRequest.setRequestTime(LocalDateTime.now().minusMinutes(15));
        helpRequest.setReportTime(LocalDateTime.now().minusMinutes(5));
        assertEquals(10, helpRequest.roundTripTimeInMinutes());
        
        helpRequest.setRequestTime(LocalDateTime.now());
        helpRequest.setReportTime(LocalDateTime.now());
        assertEquals(0, helpRequest.roundTripTimeInMinutes());
        
        // RequestTime is after ReportTime, should return negative value?
        helpRequest.setRequestTime(LocalDateTime.now());
        helpRequest.setReportTime(LocalDateTime.now().minusMinutes(5));
        assertTrue(helpRequest.roundTripTimeInMinutes() < 0);
    }
}