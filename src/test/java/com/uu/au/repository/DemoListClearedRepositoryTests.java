package com.uu.au.repository;

import com.uu.au.models.DemoListCleared;
import com.uu.au.models.User;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class DemoListClearedRepositoryTests {

    @Autowired
    private DemoListClearedRepository demoListClearedRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testFindAll(){
        // Create an DemoListCleared object, persist it and test the findAll method
        DemoListCleared demoListCleared = DemoListCleared.builder().build();

        // demoListCleared must have a user field (@JoinColumn(nullable=false))
        User user = User.builder().build();
        userRepository.save(user);
        demoListCleared.setUser(user);

        // Test before saving the DemoListCleared
        List<DemoListCleared> demoListClearedList = demoListClearedRepository.findAll();
        assertEquals(0, demoListClearedList.size());
        
        // Save the DemoListCleared and test again
        demoListClearedRepository.save(demoListCleared);
        demoListClearedList = demoListClearedRepository.findAll();
        assertEquals(1, demoListClearedList.size());
    }
}