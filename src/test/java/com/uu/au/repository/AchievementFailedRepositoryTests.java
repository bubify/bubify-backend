package com.uu.au.repository;

import com.uu.au.models.AchievementFailed;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class AchievementFailedRepositoryTests {

    @Autowired
    private AchievementFailedRepository achievementFailedRepository;

    @Test
    public void testFindAll(){
        // Create an AchievementFailed object, persist it and test the findAll method
        AchievementFailed achievementFailed = AchievementFailed.builder().build();

        // Test before saving the AchievementFailed
        List<AchievementFailed> achievementFailedList = achievementFailedRepository.findAll();
        assertEquals(0, achievementFailedList.size());
        
        // Save the AchievementFailed and test again
        achievementFailedRepository.save(achievementFailed);
        achievementFailedList = achievementFailedRepository.findAll();
        assertEquals(1, achievementFailedList.size());
    }
}