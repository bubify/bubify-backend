package com.uu.au;

import com.uu.au.models.User;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;

import com.uu.au.enums.Role;

public class UserTests {
    @Test
    @DisplayName("Test User, getters and setters")
    public void testUser() {
        User user = new User();
        // user.setId(1);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUserName("jdoe");
        user.setEmail("j.d@uu.se");
        user.setRole(Role.STUDENT);
    
        // assertEquals(1, user.getId());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("jdoe", user.getUserName());
        assertEquals("j.d@uu.se", user.getEmail());
        assertEquals(Role.STUDENT, user.getRole());
    
    }
}