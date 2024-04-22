package com.uu.au.repository;

import com.uu.au.models.HelpListCleared;
import com.uu.au.models.User;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class HelpListClearedRepositoryTests {

    @Autowired
    private HelpListClearedRepository helpListClearedRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testFindAll(){
        // Create an HelpListCleared object, persist it and test the findAll method
        HelpListCleared helpListCleared = HelpListCleared.builder().build();

        // helpListCleared must have a user field (@JoinColumn(nullable=false))
        User user = User.builder().build();
        userRepository.save(user);
        helpListCleared.setUser(user);

        // Test before saving the HelpListCleared
        List<HelpListCleared> helpListClearedList = helpListClearedRepository.findAll();
        assertEquals(0, helpListClearedList.size());

        // Save the HelpListCleared and test again
        helpListClearedRepository.save(helpListCleared);
        helpListClearedList = helpListClearedRepository.findAll();
        assertEquals(1, helpListClearedList.size());
    }
}