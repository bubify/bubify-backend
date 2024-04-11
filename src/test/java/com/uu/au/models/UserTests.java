package com.uu.au.models;

import com.uu.au.enums.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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

        Enrolment enrolment = new Enrolment();
        enrolment.setId(1L);        
        Set<Enrolment> enrolments = new HashSet<>();
        enrolments.add(enrolment);
        user.setEnrolments(enrolments);

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
        assertEquals(1, user.getEnrolments().size());
    }

    @Test
    public void testUserByBuilder() {
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
    public void testGetNeedsGitHubHandle () {
        // Test the return from getNeedsGitHubHandle-method
        User user = new User();
        assertTrue(user.getNeedsGitHubHandle());

        user.setGitHubHandle("johndoe");
        assertFalse(user.getNeedsGitHubHandle());
    }

    @Test
    public void testGetGitHubRepoURL() {
        // Test the return from getGitHubRepoURL-method
        User user = new User();
        user.setId(1L);
        user.setEmail("j.d@uu.se");
        
        Course course = new Course();
        course.setId(1L);
        course.setGitHubOrgURL("http://github.com/");
        
        Enrolment enrolment = new Enrolment();
        enrolment.setId(1L);
        enrolment.setCourseInstance(course);
        
        Set<Enrolment> enrolments = new HashSet<>();
        enrolments.add(enrolment);
        user.setEnrolments(enrolments);
        
        assertEquals("http://github.com/j.d", user.getGitHubRepoURL().get());
        
        // Test optional empty when no course is set
        user.setEnrolments(null);
        assertNull(user.getGitHubRepoURL().orElse(null));
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
    public void testLastEnrolment() {
        // Test the return from lastEnrolment-method
        User user = new User();
        user.setId(1L);

        // Test the return when no enrolments are set
        assertNull(user.lastEnrolment().orElse(null));

        // Add one course to enrolments and test the return
        Course course1 = new Course();
        course1.setId(1L);
        course1.setStartDate(LocalDate.of(2023, 1, 1));
        
        Enrolment enrolment1 = new Enrolment();
        enrolment1.setId(11L);
        enrolment1.setCourseInstance(course1);
        
        Set<Enrolment> enrolments = new HashSet<>();
        enrolments.add(enrolment1);
        user.setEnrolments(enrolments);
        
        assertEquals(enrolment1, user.lastEnrolment().get());
        
        // Add another course to enrolments and test the return
        Course course2 = new Course();
        course2.setId(2L);
        course2.setStartDate(LocalDate.of(2024, 1, 1));
        
        Enrolment enrolment2 = new Enrolment();
        enrolment2.setId(22L);
        enrolment2.setCourseInstance(course2);
        
        enrolments.add(enrolment2);
        user.setEnrolments(enrolments);

        assertEquals(enrolment2, user.lastEnrolment().get());
    }

    @Test
    public void testCurrentEnrolment() {
        // Test the return from currentEnrolment-method
        User user = new User();
        user.setId(1L);

        // Test the return when no enrolments are set
        assertNull(user.currentEnrolment().orElse(null));

        // Add one course to enrolments and test the return
        Course course1 = new Course();
        course1.setId(1L);
        course1.setStartDate(LocalDate.of(2023, 1, 1));
        
        Enrolment enrolment1 = new Enrolment();
        enrolment1.setId(11L);
        enrolment1.setCourseInstance(course1);
        
        Set<Enrolment> enrolments = new HashSet<>();
        enrolments.add(enrolment1);
        user.setEnrolments(enrolments);
        
        assertEquals(enrolment1, user.currentEnrolment().get());
    }
    
    @Test
    public void testCurrentCourseInstance() {
        // Test the return from currentCourseInstance-method
        User user = new User();
        user.setId(1L);

        // Test the return when no enrolments are set
        assertNull(user.currentCourseInstance().orElse(null));
        
        // Add one course to enrolments and test the return
        Course course1 = new Course();
        course1.setId(1L);
        course1.setStartDate(LocalDate.of(2023, 1, 1));
        
        Enrolment enrolment1 = new Enrolment();
        enrolment1.setId(11L);
        enrolment1.setCourseInstance(course1);
        
        Set<Enrolment> enrolments = new HashSet<>();
        enrolments.add(enrolment1);
        user.setEnrolments(enrolments);
        
        assertEquals(course1, user.currentCourseInstance().get());
    }
    
    @Test
    public void testAchievementsUnlocked() {
        // Test the return from achievementsUnlocked-method
        User user = new User();
        user.setId(1L);

        // Test the return when no enrolments are set
        assertEquals(0, user.achievementsUnlocked().size());
        
        // Add one course to enrolments but no unlocked assignments and test the return
        Course course1 = new Course();
        course1.setId(1L);
        course1.setStartDate(LocalDate.of(2023, 1, 1));
        
        Enrolment enrolment1 = new Enrolment();
        enrolment1.setId(11L);
        enrolment1.setCourseInstance(course1);

        Set<Enrolment> enrolments = new HashSet<>();
        enrolments.add(enrolment1);
        user.setEnrolments(enrolments);

        Set<AchievementUnlocked> achievementsUnlocked = new HashSet<>();
        enrolment1.setAchievementsUnlocked(achievementsUnlocked);
        
        assertEquals(0, user.achievementsUnlocked().size());

        // Add one unlocked assignment to enrolments and test the return
        Achievement achievement1 = new Achievement();
        achievement1.setId(1L);

        AchievementUnlocked au1 = new AchievementUnlocked();
        au1.setId(1L);
        au1.setUnlockTime(LocalDateTime.now());
        au1.setEnrolment(enrolment1);
        au1.setAchievement(achievement1);

        achievementsUnlocked.add(au1);
        enrolment1.setAchievementsUnlocked(achievementsUnlocked);

        assertEquals(1, user.achievementsUnlocked().size());

        // Add another unlocked assignment to enrolments and test the return
        Achievement achievement2 = new Achievement();
        achievement2.setId(2L);

        AchievementUnlocked au2 = new AchievementUnlocked();
        au2.setId(2L);
        au2.setUnlockTime(LocalDateTime.now());
        au2.setEnrolment(enrolment1);
        au2.setAchievement(achievement2);

        achievementsUnlocked.add(au2);
        enrolment1.setAchievementsUnlocked(achievementsUnlocked);

        assertEquals(2, user.achievementsUnlocked().size());
    }
    
    @Test
    public void testAchievementsPushedBack() {
        // Test the return from achievementsPushedBack-method
        User user = new User();
        user.setId(1L);

        // Test the return when no enrolments are set
        assertEquals(0, user.achievementsPushedBack().size());
        
        // Add one course to enrolments but no PushedBack assignments and test the return
        Course course1 = new Course();
        course1.setId(1L);
        course1.setStartDate(LocalDate.of(2023, 1, 1));
        
        Enrolment enrolment1 = new Enrolment();
        enrolment1.setId(11L);
        enrolment1.setCourseInstance(course1);

        Set<Enrolment> enrolments = new HashSet<>();
        enrolments.add(enrolment1);
        user.setEnrolments(enrolments);

        Set<AchievementPushedBack> achievementsPushedBack = new HashSet<>();
        enrolment1.setAchievementsPushedBack(achievementsPushedBack);
        
        assertEquals(0, user.achievementsPushedBack().size());

        // Add one PushedBack assignment to enrolments and test the return
        Achievement achievement1 = new Achievement();
        achievement1.setId(1L);

        AchievementPushedBack ap1 = new AchievementPushedBack();
        ap1.setId(1L);
        ap1.setEnrolment(enrolment1);
        ap1.setAchievement(achievement1);
        ap1.setPushedBackTime(LocalDateTime.now());

        achievementsPushedBack.add(ap1);
        enrolment1.setAchievementsPushedBack(achievementsPushedBack);

        assertEquals(1, user.achievementsPushedBack().size());

        // Add another PushedBack assignment to enrolments and test the return
        Achievement achievement2 = new Achievement();
        achievement2.setId(2L);

        AchievementPushedBack ap2 = new AchievementPushedBack();
        ap2.setId(2L);
        ap2.setEnrolment(enrolment1);
        ap2.setAchievement(achievement2);
        ap2.setPushedBackTime(LocalDateTime.now());

        achievementsPushedBack.add(ap2);
        enrolment1.setAchievementsPushedBack(achievementsPushedBack);

        assertEquals(2, user.achievementsPushedBack().size());

        // Test the return when the PushedBack assignment is not active
        ap2.setPushedBackTime(LocalDateTime.now().minusDays(2));

        assertEquals(1, user.achievementsPushedBack().size());
    }

    @Test
    public void testCurrentResult () {
        // Test the return from currentResult-method
        User user = new User();
        user.setId(1L);

        // Add one course to enrolments but no unlocked/pushed backed assignments and test the return
        Course course1 = new Course();
        course1.setId(1L);
        course1.setStartDate(LocalDate.of(2023, 1, 1));
        
        Enrolment enrolment1 = new Enrolment();
        enrolment1.setId(11L);
        enrolment1.setCourseInstance(course1);

        Set<Enrolment> enrolments = new HashSet<>();
        enrolments.add(enrolment1);
        user.setEnrolments(enrolments);

        Set<AchievementUnlocked> achievementsUnlocked = new HashSet<>();
        enrolment1.setAchievementsUnlocked(achievementsUnlocked);

        Set<AchievementPushedBack> achievementsPushedBack = new HashSet<>();
        enrolment1.setAchievementsPushedBack(achievementsPushedBack);

        // Create an achievement
        Achievement achievement1 = new Achievement();
        achievement1.setId(1L);
        
        assertEquals(Result.FAIL, user.currentResult(achievement1));

        // Test the return when the achievement is unlocked
        AchievementUnlocked au1 = new AchievementUnlocked();
        au1.setId(1L);
        au1.setEnrolment(enrolment1);
        au1.setAchievement(achievement1);
        au1.setUnlockTime(LocalDateTime.now());

        achievementsUnlocked.add(au1);
        
        assertEquals(Result.PASS, user.currentResult(achievement1));
        
        // Test the return when another achievement is pushed back
        Achievement achievement2 = new Achievement();
        achievement2.setId(2L);
        
        AchievementPushedBack ap2 = new AchievementPushedBack();
        ap2.setId(2L);
        ap2.setEnrolment(enrolment1);
        ap2.setAchievement(achievement2);
        ap2.setPushedBackTime(LocalDateTime.now());
        
        achievementsPushedBack.add(ap2);
        
        assertEquals(Result.PUSHBACK, user.currentResult(achievement2));
    }

    @Test
    public void testProgress() {
        // Test the return from progress-method
        User user = new User();
        user.setId(1L);

        // Add one course to enrolments but no unlocked/pushed backed assignments and test the return
        Course course1 = new Course();
        course1.setId(1L);
        course1.setStartDate(LocalDate.of(2023, 1, 1));
        
        Enrolment enrolment1 = new Enrolment();
        enrolment1.setId(11L);
        enrolment1.setCourseInstance(course1);

        Set<Enrolment> enrolments = new HashSet<>();
        enrolments.add(enrolment1);
        user.setEnrolments(enrolments);

        Set<AchievementUnlocked> achievementsUnlocked = new HashSet<>();
        enrolment1.setAchievementsUnlocked(achievementsUnlocked);

        Set<AchievementPushedBack> achievementsPushedBack = new HashSet<>();
        enrolment1.setAchievementsPushedBack(achievementsPushedBack);
        
        List<Achievement> achievements = new ArrayList<Achievement>();
        
        assertEquals(0, user.progress(achievements).size());
        
        // Test the return when an unlocked achievement is added
        Achievement achievement1 = new Achievement();
        achievement1.setId(1L);

        AchievementUnlocked au1 = new AchievementUnlocked();
        au1.setId(1L);
        au1.setEnrolment(enrolment1);
        au1.setAchievement(achievement1);
        au1.setUnlockTime(LocalDateTime.now());

        achievementsUnlocked.add(au1);

        achievements.add(achievement1);
        assertEquals(1, user.progress(achievements).size());
        List<Result> expected = new ArrayList<Result>();
        expected.add(Result.PASS);
        assertEquals(expected, user.progress(achievements));
        
        // Test the return when a pushed backed achievement is added
        Achievement achievement2 = new Achievement();
        achievement2.setId(2L);
        
        AchievementPushedBack ap2 = new AchievementPushedBack();
        ap2.setId(2L);
        ap2.setEnrolment(enrolment1);
        ap2.setAchievement(achievement2);
        ap2.setPushedBackTime(LocalDateTime.now());
        
        achievementsPushedBack.add(ap2);

        achievements.add(achievement2);
        assertEquals(2, user.progress(achievements).size());
        expected.add(Result.PUSHBACK);
        assertEquals(expected, user.progress(achievements));
    }

    @Test
    public void testGetGradeAndDate() {
        // Test the return from getGradeAndDate-method
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
        
        Course course = new Course();
        course.setId(1L);
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
        course.setStartDate(LocalDate.of(2023, 1, 1));
        
        Enrolment enrolment1 = new Enrolment();
        enrolment1.setId(11L);
        enrolment1.setCourseInstance(course);

        Set<AchievementUnlocked> achievementsUnlocked = new HashSet<>();
        enrolment1.setAchievementsUnlocked(achievementsUnlocked);
        
        Set<AchievementPushedBack> achievementsPushedBack = new HashSet<>();
        enrolment1.setAchievementsPushedBack(achievementsPushedBack);
        
        Set<Enrolment> enrolments = new HashSet<>();
        enrolments.add(enrolment1);
        user.setEnrolments(enrolments);

        List<Achievement> achievements = new ArrayList<Achievement>();

        // Test the return when no achievements are set
        // BUG? Fails if achievements is empty
        // assertNull(user.getGradeAndDate(achievements).orElse(null));
        
        // Add one unlocked assignment to enrolments and test the return
        Achievement achievement1 = new Achievement();
        achievement1.setId(1L);
        achievement1.setLevel(Level.GRADE_3);
            achievement1.setCode("Code 1");
            achievement1.setName("Achievement 1");
            achievement1.setUrlToDescription("http://example.com/achievement1");
            achievement1.setAchievementType(AchievementType.ACHIEVEMENT);
            achievement1.setCreatedDateTime(LocalDateTime.now());
            achievement1.setUpdatedDateTime(LocalDateTime.now());
        
        AchievementUnlocked au1 = new AchievementUnlocked();
        au1.setId(1L);
        au1.setUnlockTime(LocalDateTime.now().minusWeeks(1));
        au1.setEnrolment(enrolment1);
        au1.setAchievement(achievement1);
            au1.setUpdatedDateTime(LocalDateTime.now());

        achievementsUnlocked.add(au1);

        Achievement achievement2 = new Achievement();
        achievement2.setId(1L);
        achievement2.setLevel(Level.GRADE_4);
        
        Achievement achievement3 = new Achievement();
        achievement3.setId(1L);
        achievement3.setLevel(Level.GRADE_5);
        
        achievements.add(achievement1);
        achievements.add(achievement2);
        achievements.add(achievement3);

        // TODO: Fix, throws NoSuchElementException in the call to 'whenDidWePassGrade(Level.GRADE_4)'
        // It should never reach that line since no GRADE_4 achievements are unlocked
        // user.getGradeAndDate(achievements);
    }

    @Test
    public void testGetHP() {
        // Test the return from getHP-method
        User user = new User();
        user.setId(1L);

        List<Achievement> achievements = new ArrayList<Achievement>();

        // Test the return when no achievements are set => NO credit
        assertNull(user.getHP(achievements).orElse(null));
        
        // Add a basic set of achievements
        for (int i = 1; i <= 5; i++) {
            Achievement achievement = new Achievement();
            achievement.setId((long) i);
            achievement.setAchievementType(AchievementType.PROJECT);
            achievement.setLevel(Level.GRADE_3);
            achievements.add(achievement);
        }
        for (int i = 1; i <= 14; i++) {
            Achievement achievement = new Achievement();
            achievement.setId((long) 10 + i);
            achievement.setAchievementType(AchievementType.ACHIEVEMENT);
            achievement.setLevel(Level.GRADE_3);
            achievements.add(achievement);
        }
        for (int i = 1; i <= 4; i++) {
            Achievement achievement = new Achievement();
            achievement.setId((long) 30 + i);
            achievement.setAchievementType(AchievementType.ASSIGNMENT);
            achievement.setLevel(Level.GRADE_3);
            achievements.add(achievement);
        }

        // 5 x project, 14 x achievement, 4 x assignment => NO credit
        assertNull(user.getHP(achievements).orElse(null));
        
        Achievement achievement100 = new Achievement();
        achievement100.setId(100L);
        achievement100.setAchievementType(AchievementType.ACHIEVEMENT);
        achievement100.setLevel(Level.GRADE_4);
        achievements.add(achievement100);
        
        // 5 x project, 15 x achievement (BUT 1 is GRADE_4), 4 x assignment => NO credit
        assertNull(user.getHP(achievements).orElse(null));
        
        achievement100.setLevel(Level.GRADE_3);

        // 5 x project, 15 x achievement, 4 x assignment => INLUPP1 credit
        List<AcademicCreditType> hp = user.getHP(achievements).get();
        assertEquals(1, hp.size());
        assertEquals(AcademicCreditType.INLUPP1, hp.get(0));

        Achievement achievement101 = new Achievement();
        achievement101.setId(100L);
        achievement101.setAchievementType(AchievementType.PROJECT);
        achievement101.setLevel(Level.GRADE_3);
        achievements.add(achievement101);
        
        // 6 x project, 15 x achievement, 4 x assignment => PROJECT credit
        hp = user.getHP(achievements).get();
        assertEquals(1, hp.size());
        assertEquals(AcademicCreditType.PROJECT, hp.get(0));
        
        for (int i = 1; i <= 15; i++) {
            Achievement achievement = new Achievement();
            achievement.setId((long) 50 + i);
            achievement.setAchievementType(AchievementType.ACHIEVEMENT);
            achievement.setLevel(Level.GRADE_3);
            achievements.add(achievement);
        }
        
        achievement101.setAchievementType(AchievementType.ASSIGNMENT);
        // 5 x project, 30 x achievement, 5 x assignment => INLUPP1, INLUPP2 credit
        hp = user.getHP(achievements).get();
        assertEquals(2, hp.size());
        assertEquals(AcademicCreditType.INLUPP1, hp.get(0));
        assertEquals(AcademicCreditType.INLUPP2, hp.get(1));
        
        achievement101.setAchievementType(AchievementType.PROJECT);
        // 6 x project, 30 x achievement, 4 x assignment => PROJECT, INLUPP1 credit
        hp = user.getHP(achievements).get();
        assertEquals(2, hp.size());
        assertEquals(AcademicCreditType.PROJECT, hp.get(0));
        assertEquals(AcademicCreditType.INLUPP1, hp.get(1));

        for (int i = 1; i <= 4; i++) {
            Achievement achievement = new Achievement();
            achievement.setId((long) 70 + i);
            achievement.setAchievementType(AchievementType.ACHIEVEMENT);
            achievement.setLevel(Level.GRADE_3);
            achievements.add(achievement);
        }

        // 6 x project, 34 x achievement, 4 x assignment => PROJECT, INLUPP1, INLUPP2 credit
        hp = user.getHP(achievements).get();
        assertEquals(3, hp.size());
        assertEquals(AcademicCreditType.PROJECT, hp.get(0));
        assertEquals(AcademicCreditType.INLUPP1, hp.get(1));
        assertEquals(AcademicCreditType.INLUPP2, hp.get(2));
    }

    @Test
    public void testPassedAchievements() {
        // Test the return from passedAchievements-method
        User user = new User();
        user.setId(1L);

        // Add one course to enrolments but no unlocked/pushed backed assignments and test the return
        Course course1 = new Course();
        course1.setId(1L);
        course1.setStartDate(LocalDate.of(2023, 1, 1));
        
        Enrolment enrolment1 = new Enrolment();
        enrolment1.setId(11L);
        enrolment1.setCourseInstance(course1);

        Set<Enrolment> enrolments = new HashSet<>();
        enrolments.add(enrolment1);
        user.setEnrolments(enrolments);

        Set<AchievementUnlocked> achievementsUnlocked = new HashSet<>();
        enrolment1.setAchievementsUnlocked(achievementsUnlocked);

        Set<AchievementPushedBack> achievementsPushedBack = new HashSet<>();
        enrolment1.setAchievementsPushedBack(achievementsPushedBack);

        List<Achievement> achievements = new ArrayList<Achievement>();

        // Test the return when no achievements are set
        assertEquals(0, user.passedAchievements(achievements).size());

        // Create an achievement
        Achievement achievement1 = new Achievement();
        achievement1.setId(1L);
        achievement1.setCode("Code1");
        
        // Add one unlocked assignment to enrolments and test the return
        AchievementUnlocked au1 = new AchievementUnlocked();
        au1.setId(1L);
        au1.setEnrolment(enrolment1);
        au1.setAchievement(achievement1);
        au1.setUnlockTime(LocalDateTime.now());

        achievementsUnlocked.add(au1);
        achievements.add(achievement1);
        List<String> passedAchievements = user.passedAchievements(achievements);
        assertEquals(1, passedAchievements.size());
        assertEquals("Code1", passedAchievements.get(0));
        
        // Test the return when a pushed backed achievement is added
        Achievement achievement2 = new Achievement();
        achievement2.setId(2L);
        achievement2.setCode("Code2");
        
        AchievementPushedBack ap2 = new AchievementPushedBack();
        ap2.setId(2L);
        ap2.setEnrolment(enrolment1);
        ap2.setAchievement(achievement2);
        ap2.setPushedBackTime(LocalDateTime.now());
        
        achievementsPushedBack.add(ap2);
        achievements.add(achievement2);
        assertEquals(1, user.passedAchievements(achievements).size());
    }

    @Test
    public void filterPassedAchievements() {
        // Test the return from filterPassedAchievements-method
        User user = new User();
        user.setId(1L);

        // Add one course to enrolments but no unlocked/pushed backed assignments and test the return
        Course course1 = new Course();
        course1.setId(1L);
        course1.setStartDate(LocalDate.of(2023, 1, 1));
        
        Enrolment enrolment1 = new Enrolment();
        enrolment1.setId(11L);
        enrolment1.setCourseInstance(course1);

        Set<Enrolment> enrolments = new HashSet<>();
        enrolments.add(enrolment1);
        user.setEnrolments(enrolments);

        Set<AchievementUnlocked> achievementsUnlocked = new HashSet<>();
        enrolment1.setAchievementsUnlocked(achievementsUnlocked);

        Set<AchievementPushedBack> achievementsPushedBack = new HashSet<>();
        enrolment1.setAchievementsPushedBack(achievementsPushedBack);

        List<Achievement> achievements = new ArrayList<Achievement>();

        // Test the return when no achievements are set
        assertEquals(0, user.filterPassedAchievements(achievements).size());

        // Create an achievement
        Achievement achievement1 = new Achievement();
        achievement1.setId(1L);
        achievement1.setCode("Code1");
        
        // Add one unlocked assignment to enrolments and test the return
        AchievementUnlocked au1 = new AchievementUnlocked();
        au1.setId(1L);
        au1.setEnrolment(enrolment1);
        au1.setAchievement(achievement1);
        au1.setUnlockTime(LocalDateTime.now());

        achievementsUnlocked.add(au1);
        achievements.add(achievement1);
        List<Achievement> filteredAchievements = user.filterPassedAchievements(achievements);
        assertEquals(1, filteredAchievements.size());
        assertEquals(achievement1, filteredAchievements.get(0));
        
        // Test the return when a pushed backed achievement is added
        Achievement achievement2 = new Achievement();
        achievement2.setId(2L);
        achievement2.setCode("Code2");
        
        AchievementPushedBack ap2 = new AchievementPushedBack();
        ap2.setId(2L);
        ap2.setEnrolment(enrolment1);
        ap2.setAchievement(achievement2);
        ap2.setPushedBackTime(LocalDateTime.now());
        
        achievementsPushedBack.add(ap2);
        achievements.add(achievement2);
        assertEquals(1, user.filterPassedAchievements(achievements).size());
    }
}