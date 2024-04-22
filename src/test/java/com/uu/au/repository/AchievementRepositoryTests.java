package com.uu.au.repository;

import com.uu.au.models.Achievement;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

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
}