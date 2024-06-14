package com.uu.au.repository;

import com.uu.au.models.User;
import com.uu.au.models.Enrolment;
import com.uu.au.models.Course;
import com.uu.au.enums.Role;
import com.uu.au.controllers.InternalController;
import com.uu.au.controllers.DevelopmentController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;
import java.util.Set;

import org.mockito.Mockito;
import javax.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class UserRepositoryTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager; // Used to persist objects belonging to other repositories

    @Autowired
    private InternalController internalController;

    @Autowired
    private DevelopmentController developmentController;

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

    @Test
    public void testFindByUserName(){
        // Create an User object, persist it and test the findByUserName method
        User user = User.builder().userName("jdoe").build();

        // Test before saving the User
        assertFalse(userRepository.findByUserName("jdoe").isPresent());
        
        // Save the User and test again
        userRepository.save(user);
        assertTrue(userRepository.findByUserName("jdoe").isPresent());
        
        // Test concatenation of UserName
        assertFalse(userRepository.findByUserName("jd").isPresent());

        User user2 = User.builder().userName("jd").build();
        userRepository.save(user2);
        assertTrue(userRepository.findByUserName("jd").isPresent());
    }

    @Test
    public void testFindByEmail(){
        // Create an User object, persist it and test the findByEmail method
        User user = User.builder().email("j.d@uu.se").build();

        // Test before saving the User
        assertFalse(userRepository.findByEmail("j.d@uu.se").isPresent());

        // Save the User and test again
        userRepository.save(user);
        assertTrue(userRepository.findByEmail("j.d@uu.se").isPresent());

        // Test concatenation of Email
        assertFalse(userRepository.findByEmail("j.d@uu.s").isPresent());

        User user2 = User.builder().email("j.d@uu.s").build();
        userRepository.save(user2);
        assertTrue(userRepository.findByEmail("j.d@uu.s").isPresent());
        
        // Test return if not unique Email
        user2.setEmail("j.d@uu.se");

        Optional<User> someUser = userRepository.findByEmail("j.d@uu.se");
        assertTrue(someUser.isPresent());

        // Getting User by email (unique is not enforced) is ambiguous, both users can't be returned:
        assertEquals(user, someUser.get());
        assertEquals(user2, someUser.get());
    }

    @Test
    public void testCurrentUser(){
        // Test the currentUser method after authenticating with a User object
        User user = User.builder().userName("jdoe").build();
        userRepository.save(user);
        Long userId = user.getId();
        
        // Mock the SecurityContext and Authentication objects    
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.getName()).thenReturn(userId.toString());
        
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        
        // Create a another User object and assert the currentUser method
        User user2 = User.builder().userName("jane").build();
        userRepository.save(user2);
        
        assertEquals(user, userRepository.currentUser());
        assertNotEquals(user2, userRepository.currentUser());
        assertEquals("jdoe", userRepository.currentUser().getUserName());
        
        // Assert it throws an exception if the user id is malformed
        Mockito.when(authentication.getName()).thenReturn("abc");
        assertThrows(ResponseStatusException.class, () -> userRepository.currentUser());
    }
    
    @Test
    public void testCurrentUser2(){
        // Test the currentUser2 method after authenticating with a User object
        User user = User.builder().userName("jdoe").build();
        userRepository.save(user);
        Long userId = user.getId();

        // Mock the SecurityContext and Authentication objects    
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.getName()).thenReturn(userId.toString());
        
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        
        // Create a another User object and assert the currentUser2 method
        User user2 = User.builder().userName("jane").build();
        userRepository.save(user2);
        
        assertTrue(userRepository.currentUser2().isPresent());
        assertEquals(user, userRepository.currentUser2().get());
        assertNotEquals(user2, userRepository.currentUser2().get());
        assertEquals("jdoe", userRepository.currentUser2().get().getUserName());

        // Assert it returns optional empty if the user is not found (currentUser returns NULL)
        Mockito.when(authentication.getName()).thenReturn("100");
        assertFalse(userRepository.currentUser2().isPresent());
    }

    @Test
    public void testFindAllStudents(){
        // Create an User object, persist it and test the findAllStudents method
        User user = User.builder().role(Role.STUDENT).build();

        // Test before saving the User
        Set<User> userSet = userRepository.findAllStudents();
        assertEquals(0, userSet.size());

        // Save the User and test again
        userRepository.save(user);
        userSet = userRepository.findAllStudents();
        assertEquals(1, userSet.size());

        // Add a Teacher and test again
        User teacher = User.builder().role(Role.TEACHER).build();
        userRepository.save(teacher);
        userSet = userRepository.findAllStudents();
        assertEquals(1, userSet.size());

        // Add another Student and test again
        User student = User.builder().role(Role.STUDENT).build();
        userRepository.save(student);
        userSet = userRepository.findAllStudents();
        assertEquals(2, userSet.size());
    }

    @Test
    public void testFindAllCurrentlyEnrolledStudents(){
        // Create an User object, persist it and test the findAllCurrentlyEnrolledStudents method
        User user = User.builder().role(Role.STUDENT).build();
        userRepository.save(user);

        // Test before adding an Enrolment
        Set<User> userSet = userRepository.findAllCurrentlyEnrolledStudents();
        assertEquals(0, userSet.size());

        // Add an Enrolment and test again
        Course course = Course.builder().startDate(LocalDate.now()).build();
        Enrolment enrolment = Enrolment.builder().courseInstance(course).build();
        entityManager.persist(course);
        entityManager.persist(enrolment);
        user.setEnrolments(Set.of(enrolment));

        userSet = userRepository.findAllCurrentlyEnrolledStudents();
        assertEquals(1, userSet.size());

        // Add a teacher to the Enrolment and test again
        Enrolment enrolment2 = Enrolment.builder().courseInstance(course).build();
        entityManager.persist(enrolment2);
        User user2 = User.builder().enrolments(Set.of(enrolment2)).role(Role.TEACHER).build();
        userRepository.save(user2);

        userSet = userRepository.findAllCurrentlyEnrolledStudents();
        assertEquals(1, userSet.size());

        // Add another Student to the Enrolment and test again
        Enrolment enrolment3 = Enrolment.builder().courseInstance(course).build();
        entityManager.persist(enrolment3);
        User user3 = User.builder().enrolments(Set.of(enrolment3)).role(Role.STUDENT).build();
        userRepository.save(user3);

        userSet = userRepository.findAllCurrentlyEnrolledStudents();
        assertEquals(2, userSet.size());
    }

    @Test
    public void testFindOrThrow(){
        // Create an User object, persist it and test the findOrThrow method
        User user = User.builder().build();

        // Test before saving the User
        assertThrows(Exception.class, () -> userRepository.findOrThrow(100L));

        // Save the User and test again
        userRepository.save(user);
        assertEquals(user, userRepository.findOrThrow(user.getId()));

        // Add another User and test again
        User user2 = User.builder().build();
        userRepository.save(user2);
        assertEquals(user2, userRepository.findOrThrow(user2.getId()));
    }

    @Test
    public void testFindByUserNameOrThrow(){
        // Create an User object, persist it and test the findByUserNameOrThrow method
        User user = User.builder().userName("jdoe").build();

        // Test before saving the User
        assertThrows(Exception.class, () -> userRepository.findByUserNameOrThrow("jdoe"));

        // Save the User and test again
        userRepository.save(user);
        assertEquals(user, userRepository.findByUserNameOrThrow("jdoe"));

        // Add another User and test again
        User user2 = User.builder().userName("jane").build();
        userRepository.save(user2);
        assertEquals(user2, userRepository.findByUserNameOrThrow("jane"));
    }

    @Test
    public void testFindByGitHubHandle(){
        // Create an User object, persist it and test the findByGitHubHandle method
        User user = User.builder().build();
        userRepository.save(user);

        // Test before adding a GitHubHandle
        assertFalse(userRepository.findByGitHubHandle("johndoe").isPresent());

        // Add a GitHubHandle and test again
        user.setGitHubHandle("johndoe");
        assertTrue(userRepository.findByGitHubHandle("johndoe").isPresent());

        // Add another user with GitHubHandle and test again
        User user2 = User.builder().gitHubHandle("janedoe").build();
        userRepository.save(user2);
        assertTrue(userRepository.findByGitHubHandle("janedoe").isPresent());

        // Test return if not unique GitHubHandle
        user2.setGitHubHandle("johndoe");

        Optional<User> someUser = userRepository.findByGitHubHandle("johndoe");
        assertTrue(someUser.isPresent());

        // Getting User by GitHubHandle (unique is not enforced) is ambiguous, both users can't be returned:
        assertEquals(user, someUser.get());
        assertEquals(user2, someUser.get());
    }
}