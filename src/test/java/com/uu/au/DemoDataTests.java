package com.uu.au;

import com.uu.au.models.Demonstration;
import com.uu.au.models.User;
import com.uu.au.repository.DemonstrationRepository;
import com.uu.au.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

// @DataJpaTest // Doesn't work, complains that it can't find AuthController
@SpringBootTest
@Transactional // This ensures that the database is rolled back after each test
public class DemoDataTests {

    @Autowired
    private DemonstrationRepository demonstrationRepository;

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    public void testFindAllByExaminer() {
        // Create a user and a demonstration object
        User examiner = User.builder().build();
        entityManager.persist(examiner);
        // userRepository.save(examiner); // Alternative way to save the user

        // Test before saving the demonstration
        List<Demonstration> demonstrations = demonstrationRepository.findAllByExaminer(examiner);
        assertEquals(0, demonstrations.size());
        
        // Save the demonstration and test again
        Demonstration demonstration = Demonstration.builder().examiner(examiner).build();
        demonstrationRepository.save(demonstration);

        demonstrations = demonstrationRepository.findAllByExaminer(examiner);
        assertEquals(1, demonstrations.size());

        // Create another user without any demonstration and test again
        User anotherExaminer = User.builder().build();
        entityManager.persist(anotherExaminer);
        demonstrations = demonstrationRepository.findAllByExaminer(anotherExaminer);
        assertEquals(0, demonstrations.size());
    }

    @Test
    public void testFindAllBySubmitters() {
        // Create a user and a demonstration object
        User submitter = User.builder().build();
        entityManager.persist(submitter);

        // Test before saving the demonstration
        List<Demonstration> demonstrations = demonstrationRepository.findAllBySubmitters(submitter);
        assertEquals(0, demonstrations.size());
        
        // Save the demonstration and test again
        Demonstration demonstration = Demonstration.builder().submitters(Set.of(submitter)).build();
        demonstrationRepository.save(demonstration);

        demonstrations = demonstrationRepository.findAllBySubmitters(submitter);
        assertEquals(1, demonstrations.size());

        // Create another user without any demonstration and test again
        User anotherSubmitter = User.builder().build();
        entityManager.persist(anotherSubmitter);
        demonstrations = demonstrationRepository.findAllBySubmitters(anotherSubmitter);
        assertEquals(0, demonstrations.size());
    }

    @Test
    public void testUsersWithActiveDemoRequests() {
        // Test before saving any demonstration
        Set<User> users = demonstrationRepository.usersWithActiveDemoRequests();
        assertEquals(0, users.size());
        
        // Create a user and a demonstration object
        User submitter = User.builder().build();
        entityManager.persist(submitter);
        Demonstration demonstration = Demonstration.builder().submitters(Set.of(submitter)).build();
        demonstrationRepository.save(demonstration);

        // Test again
        users = demonstrationRepository.usersWithActiveDemoRequests();
        assertEquals(1, users.size());
    }
}