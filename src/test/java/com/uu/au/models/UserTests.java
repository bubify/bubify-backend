package com.uu.au.models;

import com.uu.au.enums.*;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.data.util.Pair;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class UserTests {

    private User createBasicUser(Role role) {
        // Create a basic user used as a basis in the tests
        User user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUserName("jdoe");
        user.setEmail("j.d@uu.se");
        user.setRole(role);
        return user;
    }

    private Set<Enrolment> createBasicEnrolmentsSet() {
        // Create a basic set of enrolments used as a basis in the tests
        Course course = new Course();
        course.setId(1L);
        course.setStartDate(LocalDate.of(2024, 1, 1));
        
        Enrolment enrolment = new Enrolment();
        enrolment.setId(11L);
        enrolment.setCourseInstance(course);

        Set<AchievementUnlocked> achievementsUnlocked = new HashSet<>();
        enrolment.setAchievementsUnlocked(achievementsUnlocked);
        
        Set<AchievementPushedBack> achievementsPushedBack = new HashSet<>();
        enrolment.setAchievementsPushedBack(achievementsPushedBack);
        
        Set<Enrolment> enrolments = new HashSet<>();
        enrolments.add(enrolment);

        return enrolments;
    }

    private List<Achievement> createBasicAchievements() {
        // Create a basic set of achievements used as a basis in the tests
        Achievement achievement1 = new Achievement();
        achievement1.setId(1L);
        achievement1.setLevel(Level.GRADE_3);

        Achievement achievement2 = new Achievement();
        achievement2.setId(2L);
        achievement2.setLevel(Level.GRADE_4);

        Achievement achievement3 = new Achievement();
        achievement3.setId(3L);
        achievement3.setLevel(Level.GRADE_5);
        
        List<Achievement> achievements = new ArrayList<Achievement>();
        achievements.add(achievement1);
        achievements.add(achievement2);
        achievements.add(achievement3);

        return achievements;
    }
    
    private List<Achievement> createBasicAchievementsOnly3() {
        // Create a basic set of achievements used as a basis in the tests
        Achievement achievement1 = new Achievement();
        achievement1.setId(1L);
        achievement1.setLevel(Level.GRADE_3);

        Achievement achievement2 = new Achievement();
        achievement2.setId(2L);
        achievement2.setLevel(Level.GRADE_3);

        Achievement achievement3 = new Achievement();
        achievement3.setId(3L);
        achievement3.setLevel(Level.GRADE_3);
        
        List<Achievement> achievements = new ArrayList<Achievement>();
        achievements.add(achievement1);
        achievements.add(achievement2);
        achievements.add(achievement3);

        return achievements;
    }

    @Test
    public void testUser() {
        // Create a new user and test BASIC getters and setters
        User user = createBasicUser(Role.STUDENT);
        
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
        User user = createBasicUser(Role.STUDENT);

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
    public void testUserEquals() {
        // Test the equals-method
        User user1 = createBasicUser(Role.STUDENT);
        User user2 = createBasicUser(Role.STUDENT);
        User user3 = createBasicUser(Role.STUDENT);
        user3.setId(2L);

        // This class has the @EqualsAndHashCode(of={"id"}) annotation
        assertEquals(user1, user2);
        assertNotEquals(user1, user3);
    }
    
    @Test
    public void testUserHashCode() {
        // Test the hashCode-method
        User user1 = createBasicUser(Role.STUDENT);
        User user2 = createBasicUser(Role.STUDENT);
        User user3 = createBasicUser(Role.STUDENT);
        user3.setId(2L);

        // This class has the @EqualsAndHashCode(of={"id"}) annotation
        assertEquals(user1.hashCode(), user2.hashCode());
        assertNotEquals(user1.hashCode(), user3.hashCode());
    }

    @Test
    public void testUserToString() {
        // Test the toString-method
        User user = createBasicUser(Role.STUDENT);

        assertTrue(user.toString().startsWith("User(id=1"));
    }

    @Test
    public void testGetNeedsGitHubHandle () {
        // Test the return from getNeedsGitHubHandle-method
        User user = createBasicUser(Role.STUDENT);
        assertTrue(user.getNeedsGitHubHandle());

        user.setGitHubHandle("johndoe");
        assertFalse(user.getNeedsGitHubHandle());
    }

    @Test
    public void testGetGitHubRepoURL() {
        // Test the return from getGitHubRepoURL-method
        User user = createBasicUser(Role.STUDENT);
        
        // Test when having 1 course enrolled
        Course course1 = new Course();
        course1.setId(1L);
        course1.setStartDate(LocalDate.of(2023, 1, 1));
        course1.setGitHubOrgURL("http://github.com/");
        
        Enrolment enrolment1 = new Enrolment();
        enrolment1.setId(1L);
        enrolment1.setCourseInstance(course1);
        
        Set<Enrolment> enrolments = new HashSet<>();
        enrolments.add(enrolment1);
        user.setEnrolments(enrolments);
        
        assertEquals("http://github.com/j.d", user.getGitHubRepoURL().get());
        
        // Test when having 2 courses enrolled, the course with the most recent start year should be used
        Course course2 = new Course();
        course2.setId(2L);
        course2.setStartDate(LocalDate.of(2024, 1, 1));
        course2.setGitHubOrgURL("http://example.com/");
        
        Enrolment enrolment2 = new Enrolment();
        enrolment2.setId(2L);
        enrolment2.setCourseInstance(course2);

        enrolments.add(enrolment2);
        user.setEnrolments(enrolments);
        
        assertEquals("http://example.com/j.d", user.getGitHubRepoURL().get());
        
        // Test optional empty when no course is set
        user.setEnrolments(null);
        assertNull(user.getGitHubRepoURL().orElse(null));
    }

    @Test
    public void testUserRolesStudent() {
        // Test all roles with the "is"-methods
        User user = createBasicUser(Role.STUDENT);
        assertFalse(user.isPriviliged());
        assertFalse(user.isJuniorTA());
        assertTrue(user.isStudent());
        assertFalse(user.isTeacher());
        assertFalse(user.isSeniorTAOrTeacher());
    }
    
    @Test
    public void testUserRolesJuniorTA() {
        // Test all roles with the "is"-methods
        User user = createBasicUser(Role.JUNIOR_TA);
        assertFalse(user.isPriviliged());
        assertTrue(user.isJuniorTA());
        assertFalse(user.isStudent());
        assertTrue(user.isTeacher());
        assertFalse(user.isSeniorTAOrTeacher());
    }
        
    @Test
    public void testUserRolesSeniorTA() {
        // Test all roles with the "is"-methods
        User user = createBasicUser(Role.SENIOR_TA);
        assertTrue(user.isPriviliged());
        assertFalse(user.isJuniorTA());
        assertFalse(user.isStudent());
        assertTrue(user.isTeacher());
        assertTrue(user.isSeniorTAOrTeacher());
    }
    
    @Test
    public void testUserRolesTeacher() {
        // Test all roles with the "is"-methods
        User user = createBasicUser(Role.TEACHER);
        assertTrue(user.isPriviliged());
        assertFalse(user.isJuniorTA());
        assertFalse(user.isStudent());
        assertTrue(user.isTeacher());
        assertTrue(user.isSeniorTAOrTeacher());
    }

    @Test
    public void testEmailPrefix() {
        // Test the return from emailPrefix-method
        User user = createBasicUser(Role.STUDENT);
        
        // Normal case
        user.setEmail("j.d@uu.se");
        assertEquals("j.d", user.emailPrefix());
        
        // E-mail is annotated @NotBlank so it should not be null or empty

        // Empty email
        // user.setEmail(null);
        // assertEquals("", user.emailPrefix());

        // No @-sign in email
        // user.setEmail("j.d-uu.se");
        // assertEquals("j.d", user.emailPrefix());

        // Double @-sign in email
        user.setEmail("j.d@uu@se");
        assertEquals("j.d", user.emailPrefix());
    }

    @Test
    public void testNeedsZoomLink() {
        // Test the return from getNeedsZoomLink-method
        User user = createBasicUser(Role.STUDENT);
        
        user.setZoomRoom(null);
        assertTrue(user.getNeedsZoomLink());

        user.setZoomRoom("");
        assertFalse(user.getNeedsZoomLink());
        
        user.setZoomRoom("ZoomRoom123");
        assertFalse(user.getNeedsZoomLink());
    }

    @Test
    public void testNeedsProfilePic() {
        // Test the return from getNeedsProfilePic-method
        User user = createBasicUser(Role.STUDENT);

        user.setProfilePic(null);
        assertTrue(user.getNeedsProfilePic());

        user.setProfilePic("");
        assertFalse(user.getNeedsProfilePic());
        
        user.setProfilePic("profile.jpg");
        assertFalse(user.getNeedsProfilePic());
    }

    @Test
    public void testThumbnail() {
        // Test the return from getThumbnail-method
        User user = createBasicUser(Role.STUDENT);
        
        // Test the return when no profile pic is set
        user.setProfilePic(null);
        assertNull(user.getThumbnail());

        // Test the return when profile pic is set
        user.setProfilePic("profile.jpg");
        assertEquals("profile.jpg", user.getThumbnail());

        // Test the return when profile pic thumbnail is set
        user.setProfilePicThumbnail("thumbnail.jpg");
        assertEquals("thumbnail.jpg", user.getThumbnail());
    }

    @Test
    public void testLastEnrolment() {
        // Test the return from lastEnrolment-method
        User user = createBasicUser(Role.STUDENT);

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
        User user = createBasicUser(Role.STUDENT);

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

        // Add another course to enrolments and test the return
        Course course2 = new Course();
        course2.setId(2L);
        course2.setStartDate(LocalDate.of(2024, 1, 1));

        Enrolment enrolment2 = new Enrolment();
        enrolment2.setId(22L);
        enrolment2.setCourseInstance(course2);
        
        enrolments.add(enrolment2);
        user.setEnrolments(enrolments);

        assertEquals(enrolment2, user.currentEnrolment().get());
    }
    
    @Test
    public void testCurrentCourseInstance() {
        // Test the return from currentCourseInstance-method
        User user = createBasicUser(Role.STUDENT);

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

        // Add another course to enrolments and test the return
        Course course2 = new Course();
        course2.setId(2L);
        course2.setStartDate(LocalDate.of(2024, 1, 1));

        Enrolment enrolment2 = new Enrolment();
        enrolment2.setId(22L);
        enrolment2.setCourseInstance(course2);

        enrolments.add(enrolment2);
        user.setEnrolments(enrolments);

        assertEquals(course2, user.currentCourseInstance().get());
    }
    
    @Test
    public void testAchievementsUnlocked() {
        // Test the return from achievementsUnlocked-method
        User user = createBasicUser(Role.STUDENT);

        // Test the return when no enrolments are set
        assertEquals(0, user.achievementsUnlocked().size());
        
        // Add one course to enrolments but no unlocked assignments and test the return
        Set<Enrolment> enrolments = createBasicEnrolmentsSet();
        user.setEnrolments(enrolments);

        Enrolment enrolment = enrolments.iterator().next();
        Set<AchievementUnlocked> achievementsUnlocked = enrolments.iterator().next().getAchievementsUnlocked();
        
        assertEquals(0, user.achievementsUnlocked().size());

        // Add one unlocked assignment to enrolments and test the return
        Achievement achievement1 = new Achievement();
        achievement1.setId(1L);

        AchievementUnlocked au1 = new AchievementUnlocked();
        au1.setId(1L);
        au1.setUnlockTime(LocalDateTime.now());
        au1.setEnrolment(enrolment);
        au1.setAchievement(achievement1);

        achievementsUnlocked.add(au1);
        enrolment.setAchievementsUnlocked(achievementsUnlocked);

        assertEquals(1, user.achievementsUnlocked().size());
        assertEquals(achievement1, user.achievementsUnlocked().iterator().next());

        // Add another unlocked assignment to enrolments and test the return
        Achievement achievement2 = new Achievement();
        achievement2.setId(2L);

        AchievementUnlocked au2 = new AchievementUnlocked();
        au2.setId(2L);
        au2.setUnlockTime(LocalDateTime.now());
        au2.setEnrolment(enrolment);
        au2.setAchievement(achievement2);

        achievementsUnlocked.add(au2);
        enrolment.setAchievementsUnlocked(achievementsUnlocked);

        assertEquals(2, user.achievementsUnlocked().size());
    }
    
    @Test
    public void testAchievementsPushedBack() {
        // Test the return from achievementsPushedBack-method
        User user = createBasicUser(Role.STUDENT);

        // Test the return when no enrolments are set
        assertEquals(0, user.achievementsPushedBack().size());
        
        // Add one course to enrolments but no PushedBack assignments and test the return
        Set<Enrolment> enrolments = createBasicEnrolmentsSet();
        user.setEnrolments(enrolments);

        Enrolment enrolment = enrolments.iterator().next();
        Set<AchievementUnlocked> achievementsUnlocked = enrolments.iterator().next().getAchievementsUnlocked();
        Set<AchievementPushedBack> achievementsPushedBack = enrolments.iterator().next().getAchievementsPushedBack();
        
        assertEquals(0, user.achievementsPushedBack().size());

        // Add one PushedBack assignment to enrolments and test the return
        Achievement achievement1 = new Achievement();
        achievement1.setId(1L);

        AchievementPushedBack ap1 = new AchievementPushedBack();
        ap1.setId(1L);
        ap1.setEnrolment(enrolment);
        ap1.setAchievement(achievement1);
        ap1.setPushedBackTime(LocalDateTime.now());

        achievementsPushedBack.add(ap1);
        enrolment.setAchievementsPushedBack(achievementsPushedBack);

        assertEquals(1, user.achievementsPushedBack().size());
        assertEquals(achievement1, user.achievementsPushedBack().iterator().next());

        // Add another PushedBack assignment to enrolments and test the return
        Achievement achievement2 = new Achievement();
        achievement2.setId(2L);

        AchievementPushedBack ap2 = new AchievementPushedBack();
        ap2.setId(2L);
        ap2.setEnrolment(enrolment);
        ap2.setAchievement(achievement2);
        ap2.setPushedBackTime(LocalDateTime.now());

        achievementsPushedBack.add(ap2);
        enrolment.setAchievementsPushedBack(achievementsPushedBack);

        assertEquals(2, user.achievementsPushedBack().size());

        // Test the return when the PushedBack assignment is not active
        ap2.setPushedBackTime(LocalDateTime.now().minusDays(2));

        assertEquals(1, user.achievementsPushedBack().size());
    }

    @Test
    public void testCurrentResult () {
        // Test the return from currentResult-method
        User user = createBasicUser(Role.STUDENT);

        // Add one course to enrolments but no unlocked/pushed backed assignments and test the return
        Set<Enrolment> enrolments = createBasicEnrolmentsSet();
        user.setEnrolments(enrolments);

        Enrolment enrolment = enrolments.iterator().next();
        Set<AchievementUnlocked> achievementsUnlocked = enrolments.iterator().next().getAchievementsUnlocked();
        Set<AchievementPushedBack> achievementsPushedBack = enrolments.iterator().next().getAchievementsPushedBack();

        // Create an achievement
        Achievement achievement1 = new Achievement();
        achievement1.setId(1L);
        
        assertEquals(Result.FAIL, user.currentResult(achievement1));

        // Test the return when the achievement is unlocked
        AchievementUnlocked au1 = new AchievementUnlocked();
        au1.setId(1L);
        au1.setEnrolment(enrolment);
        au1.setAchievement(achievement1);
        au1.setUnlockTime(LocalDateTime.now());

        achievementsUnlocked.add(au1);
        
        assertEquals(Result.PASS, user.currentResult(achievement1));
        
        // Test the return when another achievement is pushed back
        Achievement achievement2 = new Achievement();
        achievement2.setId(2L);
        
        AchievementPushedBack ap2 = new AchievementPushedBack();
        ap2.setId(2L);
        ap2.setEnrolment(enrolment);
        ap2.setAchievement(achievement2);
        ap2.setPushedBackTime(LocalDateTime.now());
        
        achievementsPushedBack.add(ap2);
        
        assertEquals(Result.PUSHBACK, user.currentResult(achievement2));
    }

    @Test
    public void testProgress() {
        // Test the return from progress-method
        User user = createBasicUser(Role.STUDENT);

        // Add one course to enrolments but no unlocked/pushed backed assignments and test the return
        Set<Enrolment> enrolments = createBasicEnrolmentsSet();
        user.setEnrolments(enrolments);

        Enrolment enrolment = enrolments.iterator().next();
        Set<AchievementUnlocked> achievementsUnlocked = enrolments.iterator().next().getAchievementsUnlocked();
        Set<AchievementPushedBack> achievementsPushedBack = enrolments.iterator().next().getAchievementsPushedBack();
        
        List<Achievement> achievements = new ArrayList<Achievement>();
        
        assertEquals(0, user.progress(achievements).size());
        
        // Test the return when an unlocked achievement is added
        Achievement achievement1 = new Achievement();
        achievement1.setId(1L);

        AchievementUnlocked au1 = new AchievementUnlocked();
        au1.setId(1L);
        au1.setEnrolment(enrolment);
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
        ap2.setEnrolment(enrolment);
        ap2.setAchievement(achievement2);
        ap2.setPushedBackTime(LocalDateTime.now());
        
        achievementsPushedBack.add(ap2);

        achievements.add(achievement2);
        assertEquals(2, user.progress(achievements).size());
        expected.add(Result.PUSHBACK);
        assertEquals(expected, user.progress(achievements));
    }

    @Test
    public void testGetGradeAndDateEmptyList() {
        // Test the return from getGradeAndDate-method when the list of achievements is empty
        User user = createBasicUser(Role.STUDENT);
        
        Set<Enrolment> enrolments = createBasicEnrolmentsSet();
        user.setEnrolments(enrolments);

        List<Achievement> achievements = new ArrayList<Achievement>();

        Optional<Pair<Level, LocalDate>> gradeAndDate = user.getGradeAndDate(achievements);
        assertFalse(gradeAndDate.isPresent());
    }

    @Test
    public void testGetGradeAndDateOnlyGrade3InList() {
        // Test the return from getGradeAndDate-method when the list of achievements only contains GRADE_3
        User user = createBasicUser(Role.STUDENT);
        
        Set<Enrolment> enrolments = createBasicEnrolmentsSet();
        user.setEnrolments(enrolments);

        // A list of achievements (3 x GRADE_3)
        List<Achievement> achievements = createBasicAchievementsOnly3();

        // Test the return when no achievements are unlocked
        Optional<Pair<Level, LocalDate>> gradeAndDate = user.getGradeAndDate(achievements);
        assertFalse(gradeAndDate.isPresent());    

        // Add all GRADE_3 achievement to unlocked and test the return
        Enrolment enrolment = enrolments.iterator().next();
        Set<AchievementUnlocked> achievementsUnlocked = enrolments.iterator().next().getAchievementsUnlocked();

        AchievementUnlocked au1 = AchievementUnlocked.builder()
            .id(1L)
            .enrolment(enrolment)
            .achievement(achievements.get(0))
            .unlockTime(LocalDateTime.of(2023, 1, 10, 12, 0))
            .build();
        achievementsUnlocked.add(au1);
        AchievementUnlocked au2 = AchievementUnlocked.builder()
            .id(1L)
            .enrolment(enrolment)
            .achievement(achievements.get(1))
            .unlockTime(LocalDateTime.of(2023, 1, 20, 12, 0))
            .build();
        achievementsUnlocked.add(au2);
        AchievementUnlocked au3 = AchievementUnlocked.builder()
            .id(1L)
            .enrolment(enrolment)
            .achievement(achievements.get(2))
            .unlockTime(LocalDateTime.of(2023, 1, 15, 12, 0))
            .build();
        achievementsUnlocked.add(au3);

        gradeAndDate = user.getGradeAndDate(achievements);
        assertEquals(Level.GRADE_3, gradeAndDate.get().getFirst());
        assertEquals(LocalDate.of(2023, 1, 20), gradeAndDate.get().getSecond());
    }

    @Test
    public void testGetGradeAndDateNoUnlocked() {
        // Test the return from getGradeAndDate-method when no achievements are unlocked
        User user = createBasicUser(Role.STUDENT);
        
        Set<Enrolment> enrolments = createBasicEnrolmentsSet();
        user.setEnrolments(enrolments);

        // A list of achievements (1 x GRADE_3, 1 x GRADE_4, 1 x GRADE_5)
        List<Achievement> achievements = createBasicAchievements();

        // Test the return when no achievements are unlocked
        Optional<Pair<Level, LocalDate>> gradeAndDate = user.getGradeAndDate(achievements);
        assertFalse(gradeAndDate.isPresent());
    }
    
    @Test
    public void testGetGradeAndDateGrade3() {
        // Test the return from getGradeAndDate-method when the grade is GRADE_3
        User user = createBasicUser(Role.STUDENT);
        
        Set<Enrolment> enrolments = createBasicEnrolmentsSet();
        user.setEnrolments(enrolments);

        // A list of achievements (1 x GRADE_3, 1 x GRADE_4, 1 x GRADE_5)
        List<Achievement> achievements = createBasicAchievements();

        Enrolment enrolment = enrolments.iterator().next();
        Set<AchievementUnlocked> achievementsUnlocked = enrolments.iterator().next().getAchievementsUnlocked();

        // Add one GRADE_3 achievement to unlocked and test the return
        AchievementUnlocked au1 = AchievementUnlocked.builder()
            .id(1L)
            .enrolment(enrolment)
            .achievement(achievements.get(0))
            .unlockTime(LocalDateTime.of(2023, 1, 20, 12, 0))
            .build();
        achievementsUnlocked.add(au1);
        
        Optional<Pair<Level, LocalDate>> gradeAndDate = user.getGradeAndDate(achievements);
        assertEquals(Level.GRADE_3, gradeAndDate.get().getFirst());
        assertEquals(LocalDate.of(2023, 1, 20), gradeAndDate.get().getSecond());
    }

    @Test
    public void testGetGradeAndDateGrade4() {
        // Test the return from getGradeAndDate-method when the grade is GRADE_4
        User user = createBasicUser(Role.STUDENT);
    
        Set<Enrolment> enrolments = createBasicEnrolmentsSet();
        user.setEnrolments(enrolments);

        // A list of achievements (1 x GRADE_3, 1 x GRADE_4, 1 x GRADE_5)
        List<Achievement> achievements = createBasicAchievements();

        Enrolment enrolment = enrolments.iterator().next();
        Set<AchievementUnlocked> achievementsUnlocked = enrolments.iterator().next().getAchievementsUnlocked();

        // Add one GRADE_4 achievement to unlocked and test the return
        AchievementUnlocked au2 = AchievementUnlocked.builder()
            .id(2L)
            .enrolment(enrolment)
            .achievement(achievements.get(1))
            .unlockTime(LocalDateTime.of(2023, 1, 15, 12, 0))
            .build();
        achievementsUnlocked.add(au2);
        
        // All GRADE_4 achievements are unlocked but not all GRADE_3 achievements => No Grade
        Optional<Pair<Level, LocalDate>> gradeAndDate = user.getGradeAndDate(achievements);
        assertFalse(gradeAndDate.isPresent());
        
        // Add one GRADE_3 achievement to unlocked and test the return
        AchievementUnlocked au1 = AchievementUnlocked.builder()
            .id(1L)
            .enrolment(enrolment)
            .achievement(achievements.get(0))
            .unlockTime(LocalDateTime.of(2023, 1, 20, 12, 0))
            .build();
        achievementsUnlocked.add(au1);

        // Should return the date when the last GRADE_3 or GRADE_4 achievement was unlocked
        gradeAndDate = user.getGradeAndDate(achievements);
        assertEquals(Level.GRADE_4, gradeAndDate.get().getFirst());
        assertEquals(LocalDate.of(2023, 1, 20), gradeAndDate.get().getSecond());
    }
    
    @Test
    public void testGetGradeAndDateGrade5() {
        // Test the return from getGradeAndDate-method when the grade is GRADE_5
        User user = createBasicUser(Role.STUDENT);
    
        Set<Enrolment> enrolments = createBasicEnrolmentsSet();
        user.setEnrolments(enrolments);

        // A list of achievements (1 x GRADE_3, 1 x GRADE_4, 1 x GRADE_5)
        List<Achievement> achievements = createBasicAchievements();

        Enrolment enrolment = enrolments.iterator().next();
        Set<AchievementUnlocked> achievementsUnlocked = enrolments.iterator().next().getAchievementsUnlocked();

        // Add all GRADE_3, GRADE_4 and GRADE_5 achievements to unlocked and test the return
        AchievementUnlocked au1 = AchievementUnlocked.builder()
            .id(1L)
            .enrolment(enrolment)
            .achievement(achievements.get(0))
            .unlockTime(LocalDateTime.of(2023, 1, 20, 12, 0))
            .build();
        achievementsUnlocked.add(au1);

        AchievementUnlocked au2 = AchievementUnlocked.builder()
            .id(2L)
            .enrolment(enrolment)
            .achievement(achievements.get(1))
            .unlockTime(LocalDateTime.of(2023, 1, 15, 12, 0))
            .build();
        achievementsUnlocked.add(au2);

        AchievementUnlocked au3 = AchievementUnlocked.builder()
            .id(1L)
            .enrolment(enrolment)
            .achievement(achievements.get(2))
            .unlockTime(LocalDateTime.of(2023, 1, 25, 12, 0))
            .build();
        achievementsUnlocked.add(au3);
        
        Optional<Pair<Level, LocalDate>> gradeAndDate = user.getGradeAndDate(achievements);
        assertEquals(Level.GRADE_5, gradeAndDate.get().getFirst());
        assertEquals(LocalDate.of(2023, 1, 25), gradeAndDate.get().getSecond());
    }

    @Test
    public void testGetHP() {
        // Test the return from getHP-method
        User user = createBasicUser(Role.STUDENT);

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
        User user = createBasicUser(Role.STUDENT);

        // Add one course to enrolments but no unlocked/pushed backed assignments and test the return
        Set<Enrolment> enrolments = createBasicEnrolmentsSet();
        user.setEnrolments(enrolments);

        Enrolment enrolment = enrolments.iterator().next();
        Set<AchievementUnlocked> achievementsUnlocked = enrolments.iterator().next().getAchievementsUnlocked();
        Set<AchievementPushedBack> achievementsPushedBack = enrolments.iterator().next().getAchievementsPushedBack();

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
        au1.setEnrolment(enrolment);
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
        ap2.setEnrolment(enrolment);
        ap2.setAchievement(achievement2);
        ap2.setPushedBackTime(LocalDateTime.now());
        
        achievementsPushedBack.add(ap2);
        achievements.add(achievement2);
        assertEquals(1, user.passedAchievements(achievements).size());
    }

    @Test
    public void filterPassedAchievements() {
        // Test the return from filterPassedAchievements-method
        User user = createBasicUser(Role.STUDENT);

        // Add one course to enrolments but no unlocked/pushed backed assignments and test the return
        Set<Enrolment> enrolments = createBasicEnrolmentsSet();
        user.setEnrolments(enrolments);

        Enrolment enrolment = enrolments.iterator().next();
        Set<AchievementUnlocked> achievementsUnlocked = enrolments.iterator().next().getAchievementsUnlocked();
        Set<AchievementPushedBack> achievementsPushedBack = enrolments.iterator().next().getAchievementsPushedBack();

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
        au1.setEnrolment(enrolment);
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
        ap2.setEnrolment(enrolment);
        ap2.setAchievement(achievement2);
        ap2.setPushedBackTime(LocalDateTime.now());
        
        achievementsPushedBack.add(ap2);
        achievements.add(achievement2);
        assertEquals(1, user.filterPassedAchievements(achievements).size());
    }
}