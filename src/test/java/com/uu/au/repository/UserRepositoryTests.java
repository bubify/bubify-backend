package com.uu.au.repository;

import com.uu.au.models.User;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class UserRepositoryTests {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testFindAll(){
        // Create an User object, persist it and test the findAll method
        User user = User.builder().build();

        // Test before saving the User
        List<User> userList = userRepository.findAll();
        assertEquals(0, userList.size());

        // Save the User and test again
        userRepository.save(user);
        userList = userRepository.findAll();
        assertEquals(1, userList.size());
    }
}