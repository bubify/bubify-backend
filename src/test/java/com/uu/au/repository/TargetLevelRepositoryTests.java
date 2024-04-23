package com.uu.au.repository;

import com.uu.au.models.TargetLevel;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    @Test
    public void testFindAllByEnrolmentId(){
        // Create an TargetLevel object, persist it and test the findAllByEnrolmentId method
        TargetLevel targetLevel = TargetLevel.builder().enrolmentId(1L).build();

        // Test before saving the TargetLevel
        List<TargetLevel> targetLevelList = targetLevelRepository.findAllByEnrolmentId(1L);
        assertEquals(0, targetLevelList.size());

        // Save the TargetLevel and test again
        targetLevelRepository.save(targetLevel);
        targetLevelList = targetLevelRepository.findAllByEnrolmentId(1L);
        assertEquals(1, targetLevelList.size());

        // Save another TargetLevel with same enrolmentId and test again
        TargetLevel targetLevel2 = TargetLevel.builder().enrolmentId(1L).build();
        targetLevelRepository.save(targetLevel2);
        targetLevelList = targetLevelRepository.findAllByEnrolmentId(1L);
        assertEquals(2, targetLevelList.size());

        // Test with a different enrolmentId
        targetLevelList = targetLevelRepository.findAllByEnrolmentId(100L);
        assertEquals(0, targetLevelList.size());
    }

    @Test
    public void testFindLatestByEnrolmentId(){
        // Create an TargetLevel object, persist it and test the findLatestByEnrolmentId method
        TargetLevel targetLevel = TargetLevel.builder().enrolmentId(1L).changeTime(LocalDateTime.now().minusDays(1)).build();

        // Test before saving the TargetLevel
        assertTrue(targetLevelRepository.findLatestByEnrolmentId(1L).isEmpty());

        // Save the TargetLevel and test again
        targetLevelRepository.save(targetLevel);
        assertEquals(targetLevel, targetLevelRepository.findLatestByEnrolmentId(1L).get());

        // Save another TargetLevel with same enrolmentId and test again
        TargetLevel targetLevel2 = TargetLevel.builder().enrolmentId(1L).build();
        targetLevelRepository.save(targetLevel2);

        assertEquals(targetLevel2, targetLevelRepository.findLatestByEnrolmentId(1L).get());

        // Test with a different enrolmentId
        assertTrue(targetLevelRepository.findLatestByEnrolmentId(100L).isEmpty());
    }
}