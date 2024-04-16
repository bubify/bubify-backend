package com.uu.au.models;

import com.uu.au.enums.Level;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class CourseInstanceTests {

    private CourseInstance createBasicCourseInstance() {
        // Create a basic CourseInstance used as a basis in the tests
        CourseInstance courseInstance = new CourseInstance();
        courseInstance.setId(1L);
        courseInstance.setStartDate(LocalDate.of(2024, 1, 1));
        courseInstance.setEndDate(LocalDate.of(2024, 12, 31));

        AchievementUnlocked achievementUnlocked = new AchievementUnlocked();
        achievementUnlocked.setId(1L);
        Set<AchievementUnlocked> achievementsUnlocked = new HashSet<>();
        courseInstance.setAchievementsUnlocked(achievementsUnlocked);
        
        AchievementPushedBack achievementPushedBack = new AchievementPushedBack();
        achievementPushedBack.setId(1L);
        Set<AchievementPushedBack> achievementsPushedBack = new HashSet<>();
        courseInstance.setAchievementsPushedBack(achievementsPushedBack);

        return courseInstance;
    }
    @Test
    public void testCourseInstance() {
        // Create a new CourseInstance and test ALL getters and setters
        CourseInstance courseInstance = createBasicCourseInstance();

        assertEquals(1L, courseInstance.getId());
        assertEquals(LocalDate.of(2024, 1, 1), courseInstance.getStartDate());
        assertEquals(LocalDate.of(2024, 12, 31), courseInstance.getEndDate());
        assertEquals(0, courseInstance.getAchievementsUnlocked().size());
        assertEquals(0, courseInstance.getAchievementsPushedBack().size());
    }

    @Test
    public void testCourseInstanceByBuilder(){
        // Create a new CourseInstance with builder and test 1 getter
        CourseInstance courseInstance = CourseInstance.builder()
                                    .id(1L)
                                    .startDate(LocalDate.now())
                                    .endDate(LocalDate.now())
                                    .achievementsUnlocked(Set.of(new AchievementUnlocked()))
                                    .achievementsPushedBack(Set.of(new AchievementPushedBack()))
                                    .build();

        assertEquals(1L, courseInstance.getId());
    }

    @Test
    public void testCourseInstanceEquals() {
        // Create CourseInstance objects and test equals
        CourseInstance courseInstance1 = createBasicCourseInstance();
        CourseInstance courseInstance2 = createBasicCourseInstance();
        courseInstance2.setId(2L);

        assertEquals(courseInstance1, courseInstance1);
        assertNotEquals(courseInstance1, courseInstance2);
    }

    @Test
    public void testCourseInstanceHashCode() {
        // Create CourseInstance objects and test the hashCode method
        CourseInstance courseInstance1 = createBasicCourseInstance();
        CourseInstance courseInstance2 = createBasicCourseInstance();
        courseInstance2.setId(2L);

        assertEquals(courseInstance1.hashCode(), courseInstance1.hashCode());
        assertNotEquals(courseInstance1.hashCode(), courseInstance2.hashCode());
    }

    @Test
    public void testCourseInstanceToString() {
        // Create a CourseInstance and test the toString method
        CourseInstance courseInstance = createBasicCourseInstance();

        assertTrue(courseInstance.toString().startsWith("CourseInstance(id=1"));
    }

    @Test
    public void testIsUnlocked() {
        // Create a CourseInstance and test the isUnlocked method
        CourseInstance courseInstance = createBasicCourseInstance();

        Achievement achievement1 = new Achievement();
        achievement1.setId(1L);

        // Test result when no achievements are unlocked
        assertFalse(courseInstance.isUnlocked(achievement1));

        AchievementUnlocked au1 = new AchievementUnlocked();
        au1.setId(1L);
        au1.setAchievement(achievement1);
        Set<AchievementUnlocked> achievementsUnlocked = new HashSet<>();
        achievementsUnlocked.add(au1);
        courseInstance.setAchievementsUnlocked(achievementsUnlocked);

        // Test result when achievement is unlocked
        assertTrue(courseInstance.isUnlocked(achievement1));

        Achievement achievement2 = new Achievement();
        achievement2.setId(2L);

        // Test result when achievement is not unlocked
        assertFalse(courseInstance.isUnlocked(achievement2));
    }

    @Test
    public void testThisYear() {
        // Create a CourseInstance and test the thisYear method
        CourseInstance courseInstance = createBasicCourseInstance();
        
        // Test result when course is in the current year
        courseInstance.setStartDate(LocalDate.now());
        assertTrue(courseInstance.thisYear());
        
        // Test result when course is in the previous year (also counts as this year)
        courseInstance.setStartDate(LocalDate.now().minusYears(1));
        assertTrue(courseInstance.thisYear());
        
        // Test result when course is not in the current or previous year
        courseInstance.setStartDate(LocalDate.now().minusYears(2));
        assertFalse(courseInstance.thisYear());
        
        courseInstance.setStartDate(LocalDate.now().plusYears(1));
        assertFalse(courseInstance.thisYear());
    }

    @Test
    public void testGetYear() {
        // Create a CourseInstance and test the getYear method
        CourseInstance courseInstance = createBasicCourseInstance();
        
        // Test result when course is in the current year
        courseInstance.setStartDate(LocalDate.now());
        assertEquals(LocalDate.now().getYear(), courseInstance.getYear());
    }
    
    @Test
    public void testBurnUpEmpty() {
        // Create a CourseInstance and test the burnUp method when no achievements are unlocked
        CourseInstance courseInstance = createBasicCourseInstance();
        courseInstance.setStartDate(LocalDate.now().minusWeeks(1));
        
        Set<AchievementUnlocked> setAU = new HashSet<>();
        courseInstance.setAchievementsUnlocked(setAU);

        // Check the burnUp for GRADE_3, first week is week 0
        Map<Level, List<Integer>> burnUp = courseInstance.burnUp();
        assertEquals(0, burnUp.get(Level.GRADE_3).get(0));
        assertEquals(0, burnUp.get(Level.GRADE_3).get(1));
    }

    @Test
    public void testBurnUpOne() {
        // Create a CourseInstance and test the burnUp method with only one achievement unlocked
        CourseInstance courseInstance = createBasicCourseInstance();
        courseInstance.setStartDate(LocalDate.now().minusWeeks(1));
        
        // Create one achievement and set unlock time
        Achievement achiveOneLevel3 = new Achievement();
        achiveOneLevel3.setLevel(Level.GRADE_3);
        AchievementUnlocked auOneLevel3 = new AchievementUnlocked();
        auOneLevel3.setUnlockTime(LocalDateTime.now());
        auOneLevel3.setAchievement(achiveOneLevel3);
        
        // Add the achievement to the courseInstance
        Set<AchievementUnlocked> setAU = new HashSet<>();
        setAU.add(auOneLevel3);
        courseInstance.setAchievementsUnlocked(setAU);

        // Check the burnUp per week and per level, first week is week 0
        // Note: unlocked achievements from previous weeks are also counted
        Map<Level, List<Integer>> burnUp = courseInstance.burnUp();
        assertEquals(0, burnUp.get(Level.GRADE_3).get(0));
        assertEquals(1, burnUp.get(Level.GRADE_3).get(1));

        // Note: GRADE_3 achievements are also included in GRADE_4
        assertEquals(0, burnUp.get(Level.GRADE_4).get(0));
        assertEquals(1, burnUp.get(Level.GRADE_4).get(1));
        
        // Note: GRADE_3 and GRADE_4 achievements are also included in GRADE_5
        assertEquals(0, burnUp.get(Level.GRADE_5).get(0));
        assertEquals(1, burnUp.get(Level.GRADE_5).get(1));
    }

    @Test
    public void testBurnUpMany() {
        // Create a CourseInstance and test the burnUp method with multiple achievements unlocked
        CourseInstance courseInstance = createBasicCourseInstance();
        courseInstance.setStartDate(LocalDate.now().minusWeeks(2));
        
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
        
        // Add the achievements to the courseInstance
        Set<AchievementUnlocked> setAU = new HashSet<>();
        setAU.add(auOneLevel3);
        setAU.add(auTwoLevel3);
        setAU.add(auOneLevel4);
        setAU.add(auTwoLevel4);
        setAU.add(auOneLevel5);
        courseInstance.setAchievementsUnlocked(setAU);

        // Check the burnUp per week and per level, first week is week 0
        // Note: unlocked achievements from previous weeks are also counted
        Map<Level, List<Integer>> burnUp = courseInstance.burnUp();
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
    public void testCurrentCourseWeek() {
        // Create a CourseInstance and test the currentCourseWeek method
        CourseInstance courseInstance = createBasicCourseInstance();
        
        // Test result when course starts today
        courseInstance.setStartDate(LocalDate.now());
        assertEquals(0, courseInstance.currentCourseWeek());
        
        // Test result when course started 1 week ago
        courseInstance.setStartDate(LocalDate.now().minusWeeks(1));
        assertEquals(1, courseInstance.currentCourseWeek());

        // Test result when course starts in 1 week
        courseInstance.setStartDate(LocalDate.now().plusWeeks(1));
        assertEquals(-1, courseInstance.currentCourseWeek());
    }

    @Test
    public void testBurnDownEmpty(){
        // Create a CourseInstance and test the burnDown method when no achievements are unlocked
        CourseInstance courseInstance = createBasicCourseInstance();
        courseInstance.setStartDate(LocalDate.now().minusWeeks(1));

        Set<AchievementUnlocked> setAU = new HashSet<>();
        courseInstance.setAchievementsUnlocked(setAU);

        final var levelToTarget = new HashMap<Level, Integer>();
        levelToTarget.put(Level.GRADE_3, 0);
        levelToTarget.put(Level.GRADE_4, 0); // Note: 2 GRADE_3 achievements are also included
        levelToTarget.put(Level.GRADE_5, 0); // Note: 4 GRADE_3 and 2 GRADE_4 achievements are also included

        // Check the burnDown for GRADE_3, first week is week 0
        Map<Level, List<Integer>> burnDown = courseInstance.burnDown(levelToTarget);
        assertEquals(0, burnDown.get(Level.GRADE_3).get(0));
        assertEquals(0, burnDown.get(Level.GRADE_3).get(1));
    }

    @Test
    public void testBurnDownOne(){
        // Create a CourseInstance and test the burnDown method with only one achievement unlocked
        CourseInstance courseInstance = createBasicCourseInstance();
        courseInstance.setStartDate(LocalDate.now().minusWeeks(1));

        // Create one achievement and set unlock time
        Achievement achiveOneLevel3 = new Achievement();
        achiveOneLevel3.setLevel(Level.GRADE_3);
        AchievementUnlocked auOneLevel3 = new AchievementUnlocked();
        auOneLevel3.setUnlockTime(LocalDateTime.now());
        auOneLevel3.setAchievement(achiveOneLevel3);

        // Add the achievement to the courseInstance
        Set<AchievementUnlocked> setAU = new HashSet<>();
        setAU.add(auOneLevel3);
        courseInstance.setAchievementsUnlocked(setAU);

        final var levelToTarget = new HashMap<Level, Integer>();
        levelToTarget.put(Level.GRADE_3, 1);
        levelToTarget.put(Level.GRADE_4, 1); // Note: 2 GRADE_3 achievements are also included
        levelToTarget.put(Level.GRADE_5, 1); // Note: 4 GRADE_3 and 2 GRADE_4 achievements are also included

        // Check the burnDown per week and per level, first week is week 0
        // Note: unlocked achievements from previous weeks are also counted
        Map<Level, List<Integer>> burnDown = courseInstance.burnDown(levelToTarget);
        assertEquals((1-0), burnDown.get(Level.GRADE_3).get(0));
        assertEquals((1-1), burnDown.get(Level.GRADE_3).get(1));
        
        // Note: GRADE_3 achievements are also included in GRADE_4
        assertEquals((1-0), burnDown.get(Level.GRADE_4).get(0));
        assertEquals((1-1), burnDown.get(Level.GRADE_4).get(1));
        
        // Note: GRADE_3 and GRADE_4 achievements are also included in GRADE_5
        assertEquals((1-0), burnDown.get(Level.GRADE_5).get(0));
        assertEquals((1-1), burnDown.get(Level.GRADE_5).get(1));
    }

    @Test
    public void testBurnDownMany(){
        // Create a CourseInstance and test the burnDown method
        CourseInstance courseInstance = createBasicCourseInstance();
        courseInstance.setStartDate(LocalDate.now().minusWeeks(2));

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
        
        // Add the achievements to the courseInstance
        Set<AchievementUnlocked> setAU = new HashSet<>();
        setAU.add(auOneLevel3);
        setAU.add(auTwoLevel3);
        setAU.add(auOneLevel4);
        setAU.add(auTwoLevel4);
        setAU.add(auOneLevel5);
        courseInstance.setAchievementsUnlocked(setAU);

        final var levelToTarget = new HashMap<Level, Integer>();
        levelToTarget.put(Level.GRADE_3, 2);
        levelToTarget.put(Level.GRADE_4, 4); // Note: 2 GRADE_3 achievements are also included
        levelToTarget.put(Level.GRADE_5, 5); // Note: 4 GRADE_3 and 2 GRADE_4 achievements are also included

        // Check the burnDown per week and per level, first week is week 0
        // Note: unlocked achievements from previous weeks are also counted
        Map<Level, List<Integer>> burnDown = courseInstance.burnDown(levelToTarget);
        assertEquals((2-1), burnDown.get(Level.GRADE_3).get(0));
        assertEquals((2-2), burnDown.get(Level.GRADE_3).get(1));
        assertEquals((2-2), burnDown.get(Level.GRADE_3).get(2));
        
        // Note: GRADE_3 achievements are also included in GRADE_4
        assertEquals((4-1), burnDown.get(Level.GRADE_4).get(0));
        assertEquals((4-3), burnDown.get(Level.GRADE_4).get(1));
        assertEquals((4-4), burnDown.get(Level.GRADE_4).get(2));
        
        // Note: GRADE_3 and GRADE_4 achievements are also included in GRADE_5
        assertEquals((5-1), burnDown.get(Level.GRADE_5).get(0));
        assertEquals((5-4), burnDown.get(Level.GRADE_5).get(1));
        assertEquals((5-5), burnDown.get(Level.GRADE_5).get(2));
    }
}