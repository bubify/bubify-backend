package com.uu.au.repository;

import com.uu.au.models.TargetLevel;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class TargetLevelRepositoryTests {

    @Autowired
    private TargetLevelRepository targetLevelRepository;

    @Test
    public void testFindAll(){
        // Create an TargetLevel object, persist it and test the findAll method
        TargetLevel targetLevel = TargetLevel.builder().build();

        // Test before saving the TargetLevel
        List<TargetLevel> targetLevelList = targetLevelRepository.findAll();
        assertEquals(0, targetLevelList.size());

        // Save the TargetLevel and test again
        targetLevelRepository.save(targetLevel);
        targetLevelList = targetLevelRepository.findAll();
        assertEquals(1, targetLevelList.size());
    }
}