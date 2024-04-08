package com.uu.au.models;

import com.uu.au.enums.Level;
import com.uu.au.enums.Result;
import com.uu.au.enums.Role;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.data.util.Pair;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

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

    @Test
    public void testNeedsZoomLink() {
        // Test the return from getNeedsZoomLink-method
        User user = new User();
        assertTrue(user.getNeedsZoomLink());

        user.setZoomRoom("ZoomRoom123");
        assertFalse(user.getNeedsZoomLink());
    }

    @Test
    public void testNeedsProfilePic() {
        // Test the return from getNeedsProfilePic-method
        User user = new User();
        assertTrue(user.getNeedsProfilePic());

        user.setProfilePic("profile.jpg");
        assertFalse(user.getNeedsProfilePic());
    }

    @Test
    public void testThumbnail() {
        // Test the return from getThumbnail-method
        User user = new User();
        user.setProfilePic("profile.jpg");
        assertEquals("profile.jpg", user.getThumbnail());

        user.setProfilePicThumbnail("thumbnail.jpg");
        assertEquals("thumbnail.jpg", user.getThumbnail());
    }

    @Test
    public void testCurrentEnrolment() {
        // Test the return from currentEnrolment-method
        User user = new User();
        assertNull(user.currentEnrolment().orElse(null));

        // TODO: Non-null test
    }
    
    @Test
    public void testLastEnrolment() {
        // Test the return from lastEnrolment-method
        User user = new User();
        assertNull(user.lastEnrolment().orElse(null));
        
        // TODO: Non-null test
    }
    
    @Test
    public void testCurrentCourseInstance() {
        // Test the return from currentCourseInstance-method
        User user = new User();
        assertNull(user.currentCourseInstance().orElse(null));
        
        // TODO: Non-null test
    }
    
    @Test
    public void testAchievementsUnlocked() {
        // Test the return from achievementsUnlocked-method
        User user = new User();
        assertEquals(0, user.achievementsUnlocked().size());
        
        // TODO: Non-0 test
    }
    
    @Test
    public void testAchievementsPushedBack() {
        // Test the return from achievementsPushedBack-method
        User user = new User();
        assertEquals(0, user.achievementsPushedBack().size());
        
        // TODO: Non-0 test
    }

    @Test
    public void testCurrentResult () {
        // Test the return from currentResult-method
        User user = new User();

        // Create an achievement
        Achievement achievement = new Achievement();
        achievement.setId(1L);
        achievement.setCode("Test Code");
        achievement.setName("Test Achievement");
        achievement.setUrlToDescription("http://test.com");
        achievement.setAchievementType(null);
        achievement.setLevel(Level.forValues("GRADE_3"));
        
        assertEquals(Result.FAIL, user.currentResult(achievement));
        
        // TODO: Non-0 test
        
        // AchievementUnlocked au = new AchievementUnlocked();
        // au.setAchievement(achievement);
        
        // user.achievementsPushedBack().add(achievement);
        // assertEquals(Result.PUSHBACK, user.currentResult(achievement));

        // user.achievementsUnlocked().add(achievement);
        // assertEquals(Result.PASS, user.currentResult(achievement));
    }

    @Test
    public void testProgress() {
        // Test the return from progress-method
        User user = new User();

        // Create an achievement
        Achievement achievement = new Achievement();
        achievement.setId(1L);
        achievement.setCode("Test Code");
        achievement.setName("Test Achievement");
        achievement.setUrlToDescription("http://test.com");
        achievement.setAchievementType(null);
        achievement.setLevel(Level.forValues("GRADE_3"));

        List<Achievement> achievements = new ArrayList<>();
        
        assertEquals(0, user.progress(achievements).size());
        
        achievements.add(achievement);
        
        assertEquals(1, user.progress(achievements).size());
    }

    @Test
    public void testGetGradeAndDate() {
        // Test the return from getGrade-method
        User user = new User();
        
        // Create an achievement
        Achievement achievement = new Achievement();
        achievement.setId(1L);
        achievement.setCode("Test Code");
        achievement.setName("Test Achievement");
        achievement.setUrlToDescription("http://test.com");
        achievement.setAchievementType(null);
        achievement.setLevel(Level.forValues("GRADE_3"));
        
        AchievementUnlocked au = new AchievementUnlocked();
        au.setAchievement(achievement);
        au.setUnlockTime(LocalDateTime.now());

        List<Achievement> achievements = new ArrayList<Achievement>();
        
        // TODO: Fix 'java.util.NoSuchElementException: No value present'

        achievements.add(achievement);
        assertNull(user.getGradeAndDate(achievements).orElse(null));

        // TODO: Non-0 test

        // achievements.add(achievement);

        // Optional<Pair<Level, LocalDate>> expected = Optional.of(Pair.of(Level.forValues("GRADE_3"), au.getUnlockTime().toLocalDate()));
        
        // assertEquals(expected, user.getGradeAndDate(achievements));
    }

    @Test
    public void testGetHP() {
        // Test the return from getHP-method
        User user = new User();
        
        // Create an achievement
        Achievement achievement = new Achievement();
        achievement.setId(1L);
        achievement.setCode("Test Code");
        achievement.setName("Test Achievement");
        achievement.setUrlToDescription("http://test.com");
        achievement.setAchievementType(null);
        achievement.setLevel(Level.forValues("GRADE_3"));
        
        List<Achievement> achievements = new ArrayList<Achievement>();
        assertNull(user.getHP(achievements).orElse(null));
        
        // TODO: Non-0 test
        // achievements.add(achievement);
        
        // assertEquals(1, user.getHP(achievements).size());
    }

    @Test
    public void testPassedAchievements() {
        // Test the return from passedAchievements-method
        User user = new User();
        
        // Create an achievement
        Achievement achievement = new Achievement();
        achievement.setId(1L);
        achievement.setCode("Test Code");
        achievement.setName("Test Achievement");
        achievement.setUrlToDescription("http://test.com");
        achievement.setAchievementType(null);
        achievement.setLevel(Level.forValues("GRADE_3"));
        
        List<Achievement> achievements = new ArrayList<Achievement>();
        assertEquals(0, user.passedAchievements(achievements).size());
        
        // TODO: Non-0 test
        // achievements.add(achievement);
        
        // assertEquals(1, user.passedAchievements(achievements).size());
    }

    @Test
    public void filterPassedAchievements() {
        // Test the return from filterPassedAchievements-method
        User user = new User();
        
        // Create an achievement
        Achievement achievement = new Achievement();
        achievement.setId(1L);
        achievement.setCode("Test Code");
        achievement.setName("Test Achievement");
        achievement.setUrlToDescription("http://test.com");
        achievement.setAchievementType(null);
        achievement.setLevel(Level.forValues("GRADE_3"));
        
        List<Achievement> achievements = new ArrayList<Achievement>();
        assertEquals(0, user.filterPassedAchievements(achievements).size());
        
        // TODO: Non-0 test
        // achievements.add(achievement);
        
        // assertEquals(1, user.filterPassedAchievements(achievements).size());
    }
}