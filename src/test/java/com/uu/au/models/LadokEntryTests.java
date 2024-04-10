package com.uu.au.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.uu.au.enums.Role;
import com.uu.au.enums.LadokEntryType;

import java.time.LocalDateTime;
import java.util.*;

public class LadokEntryTests {
    @Test
    public void testLadokEntry () {
        // Create a new LadoEntry and check ALL getters and setters
        LadokEntry ladokEntry = new LadokEntry();
        ladokEntry.setId(1L);
        ladokEntry.setType(LadokEntryType.ASSIGNMENTS1);
        LocalDateTime time = LocalDateTime.now();
        ladokEntry.setReportTime(time);

        User user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("j.d@uu.se");
        user.setRole(Role.STUDENT);
        
        ladokEntry.setUser(user);

        Set<Achievement> achievements = new HashSet<>();
        Achievement achievement = new Achievement();
        achievement.setId(1L);
        achievements.add(achievement);

        ladokEntry.setConsumedByEntry(achievements);
        
        assertEquals(1L, ladokEntry.getId());
        assertEquals(LadokEntryType.ASSIGNMENTS1, ladokEntry.getType());
        assertEquals(time, ladokEntry.getReportTime());
        assertEquals(user, ladokEntry.getUser());
        assertEquals(achievements, ladokEntry.getConsumedByEntry());
    }

    @Test
    public void testLadokEntryByBuilder () {
        // Create a new LadoEntry using builder and check 1 getter
        LocalDateTime time = LocalDateTime.now();
        User user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("j.d@uu.se");
        user.setRole(Role.STUDENT);

        Set<Achievement> achievements = new HashSet<>();
        Achievement achievement = new Achievement();
        achievement.setId(1L);
        achievements.add(achievement);

        LadokEntry ladokEntry = LadokEntry.builder()
            .id(1L)
            .type(LadokEntryType.ASSIGNMENTS1)
            .reportTime(time)
            .user(user)
            .consumedByEntry(achievements)
            .build();
        
        assertEquals(1L, ladokEntry.getId());
    }

    @Test
    public void testIsReported () {
        // Create a new LadoEntry and check if it is reported
        LadokEntry ladokEntry = new LadokEntry();
        assertFalse(ladokEntry.isReported());

        ladokEntry.setReportTime(LocalDateTime.now());
        assertTrue(ladokEntry.isReported());
    }
}