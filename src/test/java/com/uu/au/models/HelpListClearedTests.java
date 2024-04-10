package com.uu.au.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.uu.au.enums.Role;

import java.time.LocalDateTime;

public class HelpListClearedTests {
    @Test
    public void testHelpListCleared (){
        // Create a new HelpListCleared and check ALL getters and setters
        HelpListCleared helpListCleared = new HelpListCleared();
        helpListCleared.setId(1L);
        LocalDateTime time = LocalDateTime.now();
        helpListCleared.setTime(time);

        User user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUserName("jdoe");
        user.setEmail("j.d@uu.se");
        user.setRole(Role.STUDENT);

        helpListCleared.setUser(user);

        assertEquals(1L, helpListCleared.getId());
        assertEquals(user, helpListCleared.getUser());
        assertEquals(time, helpListCleared.getTime());
   }

   @Test
   public void testHelpListClearedByBuilder (){
        // Create a new HelpListCleared using the builder and check 1 getter
             
        User user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUserName("jdoe");
        user.setEmail("j.d@uu.se");
        user.setRole(Role.STUDENT);

        HelpListCleared helpListCleared = HelpListCleared.builder()
            .id(1L)
            .time(LocalDateTime.now())
            .user(user)
            .build();

        assertEquals(1L, helpListCleared.getId());
   }
}