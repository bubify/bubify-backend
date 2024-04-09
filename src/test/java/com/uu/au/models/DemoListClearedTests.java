package com.uu.au.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

public class DemoListClearedTests {
    @Test
    public void testDemoListCleared(){
        // Create a DemoListCleared and check ALL setters and getters
        LocalDateTime time = LocalDateTime.now();
        User user = new User();
        DemoListCleared demoListCleared = new DemoListCleared();
        demoListCleared.setId(1L);
        demoListCleared.setTime(time);
        demoListCleared.setUser(user);

        assertEquals(1L, demoListCleared.getId());
        assertEquals(time, demoListCleared.getTime());
        assertEquals(user, demoListCleared.getUser());
    }

    @Test
    public void testDemoListClearedByBuilder (){
        // Create a DemoListCleared using builder and check 1 field
        LocalDateTime time = LocalDateTime.now();
        User user = new User();
        DemoListCleared demoListCleared = DemoListCleared.builder()
                .id(1L)
                .time(time)
                .user(user)
                .build();

        assertEquals(1L, demoListCleared.getId());
    }
}