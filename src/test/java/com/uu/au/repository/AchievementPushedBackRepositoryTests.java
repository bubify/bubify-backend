package com.uu.au.repository;

import com.uu.au.models.AchievementPushedBack;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class AchievementPushedBackRepositoryTests {

    @Autowired
    private AchievementPushedBackRepository achievementPushedBackRepository;

    @Test
    public void testFindAll(){
        // Create an AchievementPushedBack object, persist it and test the findAll method
        AchievementPushedBack achievementPushedBack = AchievementPushedBack.builder().build();

        // Test before saving the AchievementPushedBack
        List<AchievementPushedBack> achievementPushedBackList = achievementPushedBackRepository.findAll();
        assertEquals(0, achievementPushedBackList.size());

        // Save the AchievementPushedBack and test again
        achievementPushedBackRepository.save(achievementPushedBack);
        achievementPushedBackList = achievementPushedBackRepository.findAll();
        assertEquals(1, achievementPushedBackList.size());
    }
}