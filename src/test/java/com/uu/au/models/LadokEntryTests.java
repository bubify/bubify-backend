package com.uu.au.models;

import org.apache.tomcat.jni.Local;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.uu.au.enums.Role;
import com.uu.au.enums.LadokEntryType;

import java.time.LocalDateTime;
import java.util.*;

public class LadokEntryTests {

    private LadokEntry createBasicLadokEntry() {
        // Create a basic LadokEntry used as a basis in the tests
        LadokEntry ladokEntry = new LadokEntry();
        ladokEntry.setId(1L);
        ladokEntry.setType(LadokEntryType.ASSIGNMENTS1);
        ladokEntry.setReportTime(LocalDateTime.of(2024, 1, 1, 12, 0));

        User user = new User();
        user.setId(1L);
        
        Set<Achievement> achievements = new HashSet<>();
        Achievement achievement = new Achievement();
        achievement.setId(1L);
        achievements.add(achievement);
        
        ladokEntry.setUser(user);
        ladokEntry.setConsumedByEntry(achievements);

        return ladokEntry;
    }

    @Test
    public void testLadokEntry () {
        // Create a new LadokEntry and check ALL getters and setters
        LadokEntry ladokEntry = createBasicLadokEntry();
        
        assertEquals(1L, ladokEntry.getId());
        assertEquals(LadokEntryType.ASSIGNMENTS1, ladokEntry.getType());
        assertEquals(LocalDateTime.of(2024, 1, 1, 12, 0), ladokEntry.getReportTime());
        assertEquals(1L, ladokEntry.getUser().getId());
        assertEquals(1L, ladokEntry.getConsumedByEntry().iterator().next().getId());
    }

    @Test
    public void testLadokEntryByBuilder () {
        // Create a new LadokEntry using builder and check 1 getter
        LocalDateTime time = LocalDateTime.now();
        User user = new User();
        user.setId(1L);

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
    public void testLadokEntryEquals () {
        // Create LadokEntry objects and test equals
        LadokEntry ladokEntry1 = createBasicLadokEntry();
        LadokEntry ladokEntry2 = createBasicLadokEntry();
        LadokEntry ladokEntry3 = createBasicLadokEntry();
        ladokEntry3.setId(2L);

        // This class has the @EqualsAndHashCode(of={"id"}) annotation
        assertEquals(ladokEntry1, ladokEntry2);
        assertNotEquals(ladokEntry1, ladokEntry3);
    }

    @Test
    public void testLadokEntryHashCode () {
        // Create LadokEntry objects and test the hashCode method
        LadokEntry ladokEntry1 = createBasicLadokEntry();
        LadokEntry ladokEntry2 = createBasicLadokEntry();
        LadokEntry ladokEntry3 = createBasicLadokEntry();
        ladokEntry3.setId(2L);

        // This class has the @EqualsAndHashCode(of={"id"}) annotation
        assertEquals(ladokEntry1.hashCode(), ladokEntry2.hashCode());
        assertNotEquals(ladokEntry1.hashCode(), ladokEntry3.hashCode());
    }

    @Test
    public void testLadokEntryToString () {
        // Create a new LadokEntry and test the toString method
        LadokEntry ladokEntry = createBasicLadokEntry();
        
        assertTrue(ladokEntry.toString().startsWith("LadokEntry(id=1"));
    }

    @Test
    public void testIsReported () {
        // Create a new LadokEntry and check if it is reported
        LadokEntry ladokEntry = createBasicLadokEntry();
        ladokEntry.setReportTime(null);

        assertFalse(ladokEntry.isReported());

        ladokEntry.setReportTime(LocalDateTime.now());
        assertTrue(ladokEntry.isReported());
    }
}