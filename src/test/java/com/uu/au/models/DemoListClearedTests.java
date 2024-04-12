package com.uu.au.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

public class DemoListClearedTests {

    private DemoListCleared createBasicDemoListCleared() {
        // Create a basic DemoListCleared used as a basis in the tests
        DemoListCleared demoListCleared = new DemoListCleared();
        demoListCleared.setId(1L);
        demoListCleared.setTime(LocalDateTime.of(2024, 1, 1, 12, 0));

        User user = new User();
        user.setId(1L);
        demoListCleared.setUser(user);

        return demoListCleared;
    }
    @Test
    public void testDemoListCleared(){
        // Create a DemoListCleared and check ALL setters and getters
        DemoListCleared demoListCleared = createBasicDemoListCleared();

        assertEquals(1L, demoListCleared.getId());
        assertEquals(LocalDateTime.of(2024, 1, 1, 12, 0), demoListCleared.getTime());
        assertEquals(1L, demoListCleared.getUser().getId());
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

    @Test
    public void testDemoListClearedEquals() {
        // Create DemoListCleared objects and test equals
        DemoListCleared demoListCleared1 = createBasicDemoListCleared();
        DemoListCleared demoListCleared2 = createBasicDemoListCleared();
        DemoListCleared demoListCleared3 = createBasicDemoListCleared();
        demoListCleared3.setId(2L);

        // This class has the @EqualsAndHashCode(of={"id"}) annotation
        assertEquals(demoListCleared1, demoListCleared2);
        assertNotEquals(demoListCleared1, demoListCleared3);
    }

    @Test
    public void testDemoListClearedHashCode() {
        // Create DemoListCleared objects and test hashCode
        DemoListCleared demoListCleared1 = createBasicDemoListCleared();
        DemoListCleared demoListCleared2 = createBasicDemoListCleared();
        DemoListCleared demoListCleared3 = createBasicDemoListCleared();
        demoListCleared3.setId(2L);

        // This class has the @EqualsAndHashCode(of={"id"}) annotation
        assertEquals(demoListCleared1.hashCode(), demoListCleared2.hashCode());
        assertNotEquals(demoListCleared1.hashCode(), demoListCleared3.hashCode());
    }

    @Test
    public void testDemoListClearedToString() {
        // Create a DemoListCleared and test the toString method
        DemoListCleared demoListCleared = createBasicDemoListCleared();

        assertTrue(demoListCleared.toString().startsWith("DemoListCleared(id=1"));
    }
}