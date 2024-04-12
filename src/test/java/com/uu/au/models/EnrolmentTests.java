package com.uu.au.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.uu.au.enums.Level;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class EnrolmentTests {

    private Enrolment createBasicEnrolment() {
        // Create a basic Enrolment used as a basis in the tests
        Enrolment enrolment = new Enrolment();
        enrolment.setId(1L);

        Course course = new Course();
        course.setId(1L);

        Set<AchievementUnlocked> setAU = new HashSet<>();
        AchievementUnlocked achievementUnlocked = new AchievementUnlocked();
        achievementUnlocked.setId(1L);
        setAU.add(achievementUnlocked);

        Set<AchievementPushedBack> setAPB = new HashSet<>();
        AchievementPushedBack achievementPushedBack = new AchievementPushedBack();
        achievementPushedBack.setId(1L);
        setAPB.add(achievementPushedBack);
        
        enrolment.setId(1L);
        enrolment.setCourseInstance(course);
        enrolment.setAchievementsUnlocked(setAU);
        enrolment.setAchievementsPushedBack(setAPB);

        return enrolment;
    }

    @Test
    public void testEnrolment (){
        // Create an Enrolment and check ALL setters and getters
        Enrolment enrolment = createBasicEnrolment();

        assertEquals(1L, enrolment.getId());
        assertEquals(1L, enrolment.getCourseInstance().getId());
        assertEquals(1L, enrolment.getAchievementsUnlocked().iterator().next().getId());
        assertEquals(1L, enrolment.getAchievementsPushedBack().iterator().next().getId());
    }

    @Test
    public void testEnrolmentByBuilder (){
        // Create an Enrolment by builder and check 1 getter
        Course course = new Course();
        course.setId(1L);

        Set<AchievementUnlocked> setAU = new HashSet<>();
        AchievementUnlocked achievementUnlocked = new AchievementUnlocked();
        achievementUnlocked.setId(1L);
        setAU.add(achievementUnlocked);

        Set<AchievementPushedBack> setAPB = new HashSet<>();
        AchievementPushedBack achievementPushedBack = new AchievementPushedBack();
        achievementPushedBack.setId(1L);
        setAPB.add(achievementPushedBack);

        Enrolment enrolment = Enrolment.builder()
            .id(1L)
            .courseInstance(course)
            .achievementsUnlocked(setAU)
            .achievementsPushedBack(setAPB)
            .build();

        assertEquals(1L, enrolment.getId());
    }

    @Test
    public void testEnrolmentEquals() {
        // Create Enrolment objects and test the equals method
        Enrolment enrolment1 = createBasicEnrolment();
        Enrolment enrolment2 = createBasicEnrolment();
        Enrolment enrolment3 = createBasicEnrolment();
        enrolment3.setId(2L);

        // This class has the @EqualsAndHashCode(of={"id"}) annotation
        assertEquals(enrolment1, enrolment2);
        assertNotEquals(enrolment1, enrolment3);
    }

    @Test
    public void testEnrolmentHashCode() {
        // Create Enrolment objects and test the hashCode method
        Enrolment enrolment1 = createBasicEnrolment();
        Enrolment enrolment2 = createBasicEnrolment();
        Enrolment enrolment3 = createBasicEnrolment();
        enrolment3.setId(2L);

        // This class has the @EqualsAndHashCode(of={"id"}) annotation
        assertEquals(enrolment1.hashCode(), enrolment2.hashCode());
        assertNotEquals(enrolment1.hashCode(), enrolment3.hashCode());
    }

    @Test
    public void testEnrolmentToString() {
        // Create an Enrolment and test the toString method
        Enrolment enrolment = createBasicEnrolment();

        assertTrue(enrolment.toString().startsWith("Enrolment(id=1"));
    }

    @Test
    public void testGetYear() {
        // Create a new enrolment and test the getYear method
        Enrolment enrolment = createBasicEnrolment();
        
        enrolment.getCourseInstance().setStartDate(LocalDate.of(2024, 1, 1));
        
        assertEquals(2024, enrolment.getYear());
    }

    @Test
    public void testIsUnlocked() {
        // Create an Enrolment and test the isUnlocked method
        Enrolment enrolment = createBasicEnrolment();

        Achievement achievement = new Achievement();
        achievement.setId(1L);

        enrolment.getAchievementsUnlocked().iterator().next().setAchievement(achievement);

        assertTrue(enrolment.isUnlocked(achievement));
    }

    @Test
    public void testThisYear() {
        // Create an Enrolment and test the thisYear method
        Enrolment enrolment = createBasicEnrolment();

        Course course = enrolment.getCourseInstance();
        course.setStartDate(LocalDate.now());
        assertTrue(enrolment.thisYear());
        
        // Previous year is also considered this year
        course.setStartDate(LocalDate.now().minusYears(1));
        assertTrue(enrolment.thisYear());
        
        course.setStartDate(LocalDate.of(2000, 1, 1));
        assertFalse(enrolment.thisYear());
    }

    @Test
    public void testBurnUp() {
        // Create an Enrolment and test the burnUp method
        Enrolment enrolment = createBasicEnrolment();

        Course course = enrolment.getCourseInstance();
        course.setStartDate(LocalDate.now().minusWeeks(2));
        
        // Create some achievements with different levels and set unlock times
        Achievement achiveOneLevel3 = new Achievement();
        achiveOneLevel3.setLevel(Level.GRADE_3);
        AchievementUnlocked auOneLevel3 = new AchievementUnlocked();
        auOneLevel3.setUnlockTime(LocalDateTime.now().minusWeeks(2));
        auOneLevel3.setAchievement(achiveOneLevel3);

        Achievement achiveTwoLevel3 = new Achievement();
        achiveTwoLevel3.setLevel(Level.GRADE_3);
        AchievementUnlocked auTwoLevel3 = new AchievementUnlocked();
        auTwoLevel3.setUnlockTime(LocalDateTime.now().minusWeeks(1));
        auTwoLevel3.setAchievement(achiveTwoLevel3);

        Achievement achiveOneLevel4 = new Achievement();
        achiveOneLevel4.setLevel(Level.GRADE_4);
        AchievementUnlocked auOneLevel4 = new AchievementUnlocked();
        auOneLevel4.setUnlockTime(LocalDateTime.now().minusWeeks(1));
        auOneLevel4.setAchievement(achiveOneLevel4);

        Achievement achiveTwoLevel4 = new Achievement();
        achiveTwoLevel4.setLevel(Level.GRADE_4);
        AchievementUnlocked auTwoLevel4 = new AchievementUnlocked();
        auTwoLevel4.setUnlockTime(LocalDateTime.now().minusWeeks(0));
        auTwoLevel4.setAchievement(achiveTwoLevel4);

        Achievement achiveOneLevel5 = new Achievement();
        achiveOneLevel5.setLevel(Level.GRADE_5);
        AchievementUnlocked auOneLevel5 = new AchievementUnlocked();
        auOneLevel5.setUnlockTime(LocalDateTime.now().minusWeeks(1));
        auOneLevel5.setAchievement(achiveOneLevel5);
        
        // Add the achievements to the enrolment
        Set<AchievementUnlocked> setAU = new HashSet<>();
        setAU.add(auOneLevel3);
        setAU.add(auTwoLevel3);
        setAU.add(auOneLevel4);
        setAU.add(auTwoLevel4);
        setAU.add(auOneLevel5);
        enrolment.setAchievementsUnlocked(setAU);

        // Check the burnUp per week and per level, first week is week 0
        // Note: unlocked achievements from previous weeks are also counted
        Map<Level, List<Integer>> burnUp = enrolment.burnUp();
        assertEquals(1, burnUp.get(Level.GRADE_3).get(0));
        assertEquals(2, burnUp.get(Level.GRADE_3).get(1));
        assertEquals(2, burnUp.get(Level.GRADE_3).get(2));

        // Note: GRADE_3 achievements are also included in GRADE_4
        assertEquals(1, burnUp.get(Level.GRADE_4).get(0));
        assertEquals(3, burnUp.get(Level.GRADE_4).get(1));
        assertEquals(4, burnUp.get(Level.GRADE_4).get(2));
        
        // Note: GRADE_3 and GRADE_4 achievements are also included in GRADE_5
        assertEquals(1, burnUp.get(Level.GRADE_5).get(0));
        assertEquals(4, burnUp.get(Level.GRADE_5).get(1));
        assertEquals(5, burnUp.get(Level.GRADE_5).get(2));
    }

    @Test
    public void testBurnDown() {
        // Create an Enrolment and test the burnDown method
        Enrolment enrolment = createBasicEnrolment();

        Course course = enrolment.getCourseInstance();
        course.setStartDate(LocalDate.now().minusWeeks(2));
        
        // Create some achievements with different levels and set unlock times
        Achievement achiveOneLevel3 = new Achievement();
        achiveOneLevel3.setLevel(Level.GRADE_3);
        AchievementUnlocked auOneLevel3 = new AchievementUnlocked();
        auOneLevel3.setUnlockTime(LocalDateTime.now().minusWeeks(2));
        auOneLevel3.setAchievement(achiveOneLevel3);

        Achievement achiveTwoLevel3 = new Achievement();
        achiveTwoLevel3.setLevel(Level.GRADE_3);
        AchievementUnlocked auTwoLevel3 = new AchievementUnlocked();
        auTwoLevel3.setUnlockTime(LocalDateTime.now().minusWeeks(1));
        auTwoLevel3.setAchievement(achiveTwoLevel3);

        Achievement achiveOneLevel4 = new Achievement();
        achiveOneLevel4.setLevel(Level.GRADE_4);
        AchievementUnlocked auOneLevel4 = new AchievementUnlocked();
        auOneLevel4.setUnlockTime(LocalDateTime.now().minusWeeks(1));
        auOneLevel4.setAchievement(achiveOneLevel4);

        Achievement achiveTwoLevel4 = new Achievement();
        achiveTwoLevel4.setLevel(Level.GRADE_4);
        AchievementUnlocked auTwoLevel4 = new AchievementUnlocked();
        auTwoLevel4.setUnlockTime(LocalDateTime.now().minusWeeks(0));
        auTwoLevel4.setAchievement(achiveTwoLevel4);

        Achievement achiveOneLevel5 = new Achievement();
        achiveOneLevel5.setLevel(Level.GRADE_5);
        AchievementUnlocked auOneLevel5 = new AchievementUnlocked();
        auOneLevel5.setUnlockTime(LocalDateTime.now().minusWeeks(1));
        auOneLevel5.setAchievement(achiveOneLevel5);
        
        // Add the achievements to the enrolment
        Set<AchievementUnlocked> setAU = new HashSet<>();
        setAU.add(auOneLevel3);
        setAU.add(auTwoLevel3);
        setAU.add(auOneLevel4);
        setAU.add(auTwoLevel4);
        setAU.add(auOneLevel5);
        enrolment.setAchievementsUnlocked(setAU);

        final var levelToTarget = new HashMap<Level, Integer>();
        levelToTarget.put(Level.GRADE_3, 2);
        levelToTarget.put(Level.GRADE_4, 4); // Note: 2 GRADE_3 achievements are also included
        levelToTarget.put(Level.GRADE_5, 5); // Note: 4 GRADE_3 and 2 GRADE_4 achievements are also included

        // Check the burnDown per week and per level, first week is duplicated so week 0 and 1 are the same
        // Note: unlocked achievements from previous weeks are also counted
        Map<Level, List<Integer>> burnDown = enrolment.burnDown(levelToTarget);
        assertEquals((2-1), burnDown.get(Level.GRADE_3).get(0)); // Duplicate of week 1
        assertEquals((2-1), burnDown.get(Level.GRADE_3).get(1));
        assertEquals((2-2), burnDown.get(Level.GRADE_3).get(2));
        assertEquals((2-2), burnDown.get(Level.GRADE_3).get(3));
        
        // Note: GRADE_3 achievements are also included in GRADE_4
        assertEquals((4-1), burnDown.get(Level.GRADE_4).get(0)); // Duplicate of week 1
        assertEquals((4-1), burnDown.get(Level.GRADE_4).get(1));
        assertEquals((4-3), burnDown.get(Level.GRADE_4).get(2));
        assertEquals((4-4), burnDown.get(Level.GRADE_4).get(3));
        
        // Note: GRADE_3 and GRADE_4 achievements are also included in GRADE_5
        assertEquals((5-1), burnDown.get(Level.GRADE_5).get(0)); // Duplicate of week 1
        assertEquals((5-1), burnDown.get(Level.GRADE_5).get(1));
        assertEquals((5-4), burnDown.get(Level.GRADE_5).get(2));
        assertEquals((5-5), burnDown.get(Level.GRADE_5).get(3));
    }
}