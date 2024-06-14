package com.uu.au.repository;

import com.uu.au.models.AchievementUnlocked;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class AchievementUnlockedRepositoryTests {

    @Autowired
    private AchievementUnlockedRepository achievementUnlockedRepository;

    @Test
    public void testFindAll(){
        // Create an AchievementUnlocked object, persist it and test the findAll method
        AchievementUnlocked achievementUnlocked = AchievementUnlocked.builder().build();
        
        // Test before saving the AchievementUnlocked
        List<AchievementUnlocked> achievementUnlockedList = achievementUnlockedRepository.findAll();
        assertEquals(0, achievementUnlockedList.size());

        // Save the AchievementUnlocked and test again
        achievementUnlockedRepository.save(achievementUnlocked);
        achievementUnlockedList = achievementUnlockedRepository.findAll();
        assertEquals(1, achievementUnlockedList.size());
    }
}