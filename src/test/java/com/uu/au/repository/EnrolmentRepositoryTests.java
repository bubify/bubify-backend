package com.uu.au.repository;

import com.uu.au.models.Enrolment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class EnrolmentRepositoryTests {

    @Autowired
    private EnrolmentRepository enrolmentRepository;

    @Test
    public void testFindAll(){
        // Create an Enrolment object, persist it and test the findAll method
        Enrolment enrolment = Enrolment.builder().build();

        // Test before saving the Enrolment
        List<Enrolment> enrolmentList = enrolmentRepository.findAll();
        assertEquals(0, enrolmentList.size());

        // Save the Enrolment and test again
        enrolmentRepository.save(enrolment);
        enrolmentList = enrolmentRepository.findAll();
        assertEquals(1, enrolmentList.size());
    }
}