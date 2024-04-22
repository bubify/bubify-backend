package com.uu.au.repository;

import com.uu.au.models.HelpRequest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class HelpRequestRepositoryTests {

    @Autowired
    private HelpRequestRepository helpRequestRepository;

    @Test
    public void testFindAll(){
        // Create an HelpRequest object, persist it and test the findAll method
        HelpRequest helpRequest = HelpRequest.builder().build();

        // Test before saving the HelpRequest
        List<HelpRequest> helpRequestList = helpRequestRepository.findAll();
        assertEquals(0, helpRequestList.size());

        // Save the HelpRequest and test again
        helpRequestRepository.save(helpRequest);
        helpRequestList = helpRequestRepository.findAll();
        assertEquals(1, helpRequestList.size());
    }
}