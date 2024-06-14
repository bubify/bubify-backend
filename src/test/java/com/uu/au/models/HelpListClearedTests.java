package com.uu.au.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.uu.au.enums.Role;

import java.time.LocalDateTime;

public class HelpListClearedTests {

    private HelpListCleared createBasicHelpListCleared() {
        // Create a basic HelpListCleared used as a basis in the tests
        HelpListCleared helpListCleared = new HelpListCleared();
        helpListCleared.setId(1L);
        LocalDateTime time = LocalDateTime.of(2024, 1, 1, 12, 0);
        helpListCleared.setTime(time);

        User user = new User();
        user.setId(1L);

        helpListCleared.setUser(user);

        return helpListCleared;
    }

    @Test
    public void testHelpListCleared (){
        // Create a new HelpListCleared and check ALL getters and setters
        HelpListCleared helpListCleared = createBasicHelpListCleared();
        
        assertEquals(1L, helpListCleared.getId());
        assertEquals(LocalDateTime.of(2024, 1, 1, 12, 0), helpListCleared.getTime());
        assertEquals(1L, helpListCleared.getUser().getId());
   }

   @Test
   public void testHelpListClearedByBuilder (){
        // Create a new HelpListCleared using the builder and check 1 getter
        User user = new User();
        user.setId(1L);

        HelpListCleared helpListCleared = HelpListCleared.builder()
            .id(1L)
            .time(LocalDateTime.now())
            .user(user)
            .build();

        assertEquals(1L, helpListCleared.getId());
   }

   @Test
   public void testHelpListClearedEquals (){
        // Create HelpListCleared objects and test equals
        HelpListCleared helpListCleared1 = createBasicHelpListCleared();
        HelpListCleared helpListCleared2 = createBasicHelpListCleared();
        HelpListCleared helpListCleared3 = createBasicHelpListCleared();

        // This class has the @EqualsAndHashCode(of={"id"}) annotation
        assertEquals(helpListCleared1, helpListCleared2);
        assertEquals(helpListCleared1, helpListCleared3);
        assertEquals(helpListCleared2, helpListCleared3);
   }

   @Test
   public void testHelpListClearedHashCode (){
        // Create HelpListCleared objects and test hashCode
        HelpListCleared helpListCleared1 = createBasicHelpListCleared();
        HelpListCleared helpListCleared2 = createBasicHelpListCleared();
        HelpListCleared helpListCleared3 = createBasicHelpListCleared();

        // This class has the @EqualsAndHashCode(of={"id"}) annotation
        assertEquals(helpListCleared1.hashCode(), helpListCleared2.hashCode());
        assertEquals(helpListCleared1.hashCode(), helpListCleared3.hashCode());
        assertEquals(helpListCleared2.hashCode(), helpListCleared3.hashCode());
   }

   @Test
   public void testHelpListClearedToString (){
        // Create a HelpListCleared and test the toString method
        HelpListCleared helpListCleared = createBasicHelpListCleared();
        
        assertTrue(helpListCleared.toString().startsWith("HelpListCleared(id=1"));
    }
}