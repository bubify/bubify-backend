package com.uu.au;

import com.uu.au.models.User;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.uu.au.enums.Role;

public class UserTests {
    @Test
    public void testUser() {
        // Create a new user and test BASIC getters and setters
        User user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUserName("jdoe");
        user.setEmail("j.d@uu.se");
        user.setRole(Role.STUDENT);
        
        assertEquals(1L, user.getId());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("jdoe", user.getUserName());
        assertEquals("j.d@uu.se", user.getEmail());
        assertEquals(Role.STUDENT, user.getRole());
    }

    @Test
    public void testUserExtended() {
        // Create a new user and test ALL getters and setters
        User user = new User();

        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUserName("jdoe");
        user.setEmail("j.d@uu.se");
        user.setRole(Role.STUDENT);
        user.setGitHubHandle("johndoe");
        user.setGitHubFlowSuccessful(true);
        user.setZoomRoom("ZoomRoom123");
        user.setProfilePic("profile.jpg");
        user.setProfilePicThumbnail("thumbnail.jpg");
        user.setUserApprovedThumbnail(true);
        user.setVerifiedProfilePic(true);
        user.setCanClaimHelpRequests(true);
        user.setPreviouslyEnrolled(true);
        user.setUpdatedDateTime(LocalDateTime.now());
        user.setLastLogin(LocalDateTime.now());
        user.setDeadline(LocalDate.of(2024, 12, 31));

        assertEquals(1L, user.getId());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("jdoe", user.getUserName());
        assertEquals("j.d@uu.se", user.getEmail());
        assertEquals(Role.STUDENT, user.getRole());
        assertEquals("johndoe", user.getGitHubHandle());
        assertTrue(user.isGitHubFlowSuccessful());
        assertEquals("ZoomRoom123", user.getZoomRoom());
        assertEquals("profile.jpg", user.getProfilePic());
        assertEquals("thumbnail.jpg", user.getProfilePicThumbnail());
        assertTrue(user.isUserApprovedThumbnail());
        assertTrue(user.isVerifiedProfilePic());
        assertTrue(user.isCanClaimHelpRequests());
        assertTrue(user.isPreviouslyEnrolled());
        assertNotNull(user.getUpdatedDateTime());
        assertNotNull(user.getLastLogin());
        assertEquals(LocalDate.of(2024, 12, 31), user.getDeadline());
    }

    @Test
    public void testUserBuilder() {
        // Create a new user using the builder pattern and check default values set
        User user = User.builder()
            .id(1L)
            .firstName("John")
            .lastName("Doe")
            .userName("jdoe")
            .email("j.d@uu.se")
            .role(Role.STUDENT)
            .build();

        assertEquals(1L, user.getId());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("jdoe", user.getUserName());
        assertEquals("j.d@uu.se", user.getEmail());
        assertEquals(Role.STUDENT, user.getRole());
        assertFalse(user.isGitHubFlowSuccessful());
        assertFalse(user.isUserApprovedThumbnail());
        assertFalse(user.isVerifiedProfilePic());
        assertFalse(user.isCanClaimHelpRequests());
        assertFalse(user.isPreviouslyEnrolled());
    }

    @Test
    public void testUserRoles() {
        // Test all roles with the "is"-methods
        User user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUserName("jdoe");
        user.setEmail("j.d@uu.se");
        
        // STUDENT
        user.setRole(Role.STUDENT);
        assertFalse(user.isPriviliged());
        assertFalse(user.isJuniorTA());
        assertTrue(user.isStudent());
        assertFalse(user.isTeacher());
        assertFalse(user.isSeniorTAOrTeacher());
        
        // JUNIOR_TA
        user.setRole(Role.JUNIOR_TA);
        assertFalse(user.isPriviliged());
        assertTrue(user.isJuniorTA());
        assertFalse(user.isStudent());
        assertTrue(user.isTeacher());
        assertFalse(user.isSeniorTAOrTeacher());
        
        // SENIOR_TA
        user.setRole(Role.SENIOR_TA);
        assertTrue(user.isPriviliged());
        assertFalse(user.isJuniorTA());
        assertFalse(user.isStudent());
        assertTrue(user.isTeacher());
        assertTrue(user.isSeniorTAOrTeacher());
        
        // TEACHER
        user.setRole(Role.TEACHER);
        assertTrue(user.isPriviliged());
        assertFalse(user.isJuniorTA());
        assertFalse(user.isStudent());
        assertTrue(user.isTeacher());
        assertTrue(user.isSeniorTAOrTeacher());
    }

    @Test
    public void testEmailPrefix() {
        // Test the return from emailPrefix-method
        User user = new User();
        user.setEmail("j.d@uu.se");

        String prefix = user.emailPrefix();
        assertEquals("j.d", prefix);
    }
}