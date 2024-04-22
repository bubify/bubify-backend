package com.uu.au.repository;

import com.uu.au.models.LadokEntry;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class LadokEntryRepositoryTests {

    @Autowired
    private LadokEntryRepository ladokEntryRepository;

    @Test
    public void testFindAll(){
        // Create an LadokEntry object, persist it and test the findAll method
        LadokEntry ladokEntry = LadokEntry.builder().build();

        // Test before saving the LadokEntry
        List<LadokEntry> ladokEntryList = ladokEntryRepository.findAll();
        assertEquals(0, ladokEntryList.size());

        // Save the LadokEntry and test again
        ladokEntryRepository.save(ladokEntry);
        ladokEntryList = ladokEntryRepository.findAll();
        assertEquals(1, ladokEntryList.size());
    }
}