package com.uu.au.repository;

import com.uu.au.enums.Level;
import com.uu.au.models.Achievement;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class AchievementRepositoryTests {

    @Autowired
    private AchievementRepository achievementRepository;

    @Test
    public void testFindAll(){
        // Create an Achievement object, persist it and test the findAll method
        Achievement achievement = Achievement.builder().build();

        // Test before saving the Achievement
        List<Achievement> achievementList = achievementRepository.findAll();
        assertEquals(0, achievementList.size());

        // Save the Achievement and test again
        achievementRepository.save(achievement);
        achievementList = achievementRepository.findAll();
        assertEquals(1, achievementList.size());
    }

    @Test
    public void testFindByCode(){
        // Create an Achievement object, persist it and test the findByCode method
        Achievement achievement = Achievement.builder().code("Code 1").build();

        // Test before saving the Achievement
        assertFalse(achievementRepository.findByCode("Code 1").isPresent());

        // Save the Achievement and test again
        achievementRepository.save(achievement);
        assertTrue(achievementRepository.findByCode("Code 1").isPresent());
    }

    @Test
    public void testFindByLevel(){
        // Create an Achievement object, persist it and test the findByLevel method
        Achievement achievement = Achievement.builder().level(Level.GRADE_3).build();

        // Test before saving the Achievement
        assertFalse(achievementRepository.findByLevel(Level.GRADE_3).isPresent());

        // Save the Achievement and test again
        achievementRepository.save(achievement);
        assertTrue(achievementRepository.findByLevel(Level.GRADE_3).isPresent());
    }

    @Test
    public void testFindAllNeededForLevel(){
        // Create an Achievement object, persist it and test the findAllNeededForLevel method
        Achievement achievement = Achievement.builder().level(Level.GRADE_3).build();

        // Test before saving the Achievement
        List<Achievement> achievementList = achievementRepository.findAllNeededForLevel(Level.GRADE_3);
        assertEquals(0, achievementList.size());

        // Save the Achievement and test again
        achievementRepository.save(achievement);
        achievementList = achievementRepository.findAllNeededForLevel(Level.GRADE_3);
        assertEquals(1, achievementList.size());

        // Add another Achievement object and test again
        Achievement achievement2 = Achievement.builder().level(Level.GRADE_4).build();
        achievementRepository.save(achievement2);
        achievementList = achievementRepository.findAllNeededForLevel(Level.GRADE_4);
        assertEquals(2, achievementList.size());
    }

    @Test
    public void testFindOrThrow(){
        // Create an Achievement object, persist it and test the findOrThrow method
        Achievement achievement = Achievement.builder().build();

        // Test before saving the Achievement
        assertThrows(Exception.class, () -> achievementRepository.findOrThrow(achievement.getId()));

        // Save the Achievement and test again
        achievementRepository.save(achievement);
        Achievement foundAchievement = achievementRepository.findOrThrow(achievement.getId());
        assertEquals(achievement, foundAchievement);

        // Test another non-existing id
        assertThrows(Exception.class, () -> achievementRepository.findOrThrow(100L));
    }

    @Test
    public void testFindAllLabAchievements(){
        // Create an Achievement object, persist it and test the findAllLabAchievements method
        Achievement achievement = Achievement.builder().code("LAB1").build();

        // Test before saving the Achievement
        Set<Achievement> achievementSet = achievementRepository.findAllLabAchievements();
        assertEquals(0, achievementSet.size());

        // Save the Achievement and test again
        achievementRepository.save(achievement);
        achievementSet = achievementRepository.findAllLabAchievements();
        assertEquals(1, achievementSet.size());

        // Add another 2 Achievement objects and test again
        Achievement achievement2 = Achievement.builder().code("LAB2").build();
        Achievement achievement3 = Achievement.builder().code("Y123").build();
        achievementRepository.save(achievement2);
        achievementRepository.save(achievement3);

        achievementSet = achievementRepository.findAllLabAchievements();
        assertEquals(2, achievementSet.size());
    }

    @Test
    public void testFindAllProjectAchievements(){
        // Create an Achievement object, persist it and test the findAllProjectAchievements method
        Achievement achievement = Achievement.builder().code("Y123").build();

        // Test before saving the Achievement
        Set<Achievement> achievementSet = achievementRepository.findAllProjectAchievements();
        assertEquals(0, achievementSet.size());

        // Save the Achievement and test again
        achievementRepository.save(achievement);
        achievementSet = achievementRepository.findAllProjectAchievements();
        assertEquals(1, achievementSet.size());

        // Add another 2 Achievement objects and test again
        Achievement achievement2 = Achievement.builder().code("Y234").build();
        Achievement achievement3 = Achievement.builder().code("LAB1").build();
        achievementRepository.save(achievement2);
        achievementRepository.save(achievement3);

        achievementSet = achievementRepository.findAllProjectAchievements();
        assertEquals(2, achievementSet.size());
    }
}