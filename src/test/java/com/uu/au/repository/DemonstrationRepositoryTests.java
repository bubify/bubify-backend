package com.uu.au.repository;

import com.uu.au.models.Demonstration;
import com.uu.au.models.User;
import com.uu.au.enums.DemonstrationStatus;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.*;

// @DataJpaTest // Doesn't work, complains that it can't find AuthController
@SpringBootTest
@Transactional // This ensures that the database is rolled back after each test
public class DemonstrationRepositoryTests {

    @Autowired
    private DemonstrationRepository demonstrationRepository;

    @Autowired
    private EntityManager entityManager; // Used to persist objects belonging to other repositories

    @Test
    public void testFindAll(){
        // Create a Demonstration object, persist it and test the findAll method
        Demonstration demonstration = Demonstration.builder().build();

        // Test before saving the Demonstration
        List<Demonstration> demonstrationList = demonstrationRepository.findAll();
        assertEquals(0, demonstrationList.size());
        
        // Save the Demonstration and test again
        demonstrationRepository.save(demonstration);
        demonstrationList = demonstrationRepository.findAll();
        assertEquals(1, demonstrationList.size());
    }

    @Test
    public void testFindAllByExaminer() {
        // Create a user and a demonstration object
        User examiner = User.builder().build();
        entityManager.persist(examiner);
        Demonstration demonstration = Demonstration.builder().examiner(examiner).build();

        // Test before saving the demonstration
        List<Demonstration> demonstrationList = demonstrationRepository.findAllByExaminer(examiner);
        assertEquals(0, demonstrationList.size());
        
        // Save the demonstration and test again
        demonstrationRepository.save(demonstration);

        demonstrationList = demonstrationRepository.findAllByExaminer(examiner);
        assertEquals(1, demonstrationList.size());

        // Create another demonstration with examiner and test again
        Demonstration anotherDemonstration = Demonstration.builder().examiner(examiner).build();
        demonstrationRepository.save(anotherDemonstration);
        
        demonstrationList = demonstrationRepository.findAllByExaminer(examiner);
        assertEquals(2, demonstrationList.size());  
    }

    @Test
    public void testFindAllBySubmitters() {
        // Create a user and a demonstration object
        User submitter = User.builder().build();
        entityManager.persist(submitter);
        Demonstration demonstration = Demonstration.builder().submitters(Set.of(submitter)).build();

        // Test before saving the demonstration
        List<Demonstration> demonstrationList = demonstrationRepository.findAllBySubmitters(submitter);
        assertEquals(0, demonstrationList.size());
        
        // Save the demonstration and test again
        demonstrationRepository.save(demonstration);

        demonstrationList = demonstrationRepository.findAllBySubmitters(submitter);
        assertEquals(1, demonstrationList.size());

        // Create and add another demonstration with 2 submitters and test again
        User anotherSubmitter = User.builder().build();
        entityManager.persist(anotherSubmitter);
        Demonstration anotherDemonstration = Demonstration.builder().submitters(Set.of(submitter, anotherSubmitter)).build();
        demonstrationRepository.save(anotherDemonstration);
        
        demonstrationList = demonstrationRepository.findAllBySubmitters(submitter);
        assertEquals(2, demonstrationList.size());
        demonstrationList = demonstrationRepository.findAllBySubmitters(anotherSubmitter);
        assertEquals(1, demonstrationList.size());
    }

    @Test
    public void testUsersWithActiveDemoRequests() {
        // Create a user and a demonstration object (status SUBMITTED and requestTime within 24 hours)
        User submitter = User.builder().build();
        entityManager.persist(submitter);
        Demonstration demonstration = Demonstration.builder().submitters(Set.of(submitter))
                                        .requestTime(LocalDateTime.now()).status(DemonstrationStatus.SUBMITTED).build();
        
        // Test before saving any demonstration
        Set<User> users = demonstrationRepository.usersWithActiveDemoRequests();
        assertEquals(0, users.size());
        
        // Save the demonstration and test again
        demonstrationRepository.save(demonstration);

        users = demonstrationRepository.usersWithActiveDemoRequests();
        assertEquals(1, users.size());

        // Create another user and a demonstration object with 2 submitters (status COMPLETED and requestTime within 24 hours)
        User anotherSubmitter = User.builder().build();
        entityManager.persist(anotherSubmitter);
        Demonstration anotherDemonstration = Demonstration.builder().submitters(Set.of(submitter, anotherSubmitter))
        .requestTime(LocalDateTime.now()).status(DemonstrationStatus.COMPLETED).build();
        demonstrationRepository.save(anotherDemonstration);
        
        users = demonstrationRepository.usersWithActiveDemoRequests();
        assertEquals(1, users.size());
        
        // Create another user and a demonstration object with 2 submitters (status SUBMITTED and requestTime within 24 hours)
        Demonstration thirdDemonstration = Demonstration.builder().submitters(Set.of(submitter, anotherSubmitter))
                                                .requestTime(LocalDateTime.now()).status(DemonstrationStatus.SUBMITTED).build();
        demonstrationRepository.save(thirdDemonstration);

        users = demonstrationRepository.usersWithActiveDemoRequests();
        assertEquals(2, users.size());
    }
}