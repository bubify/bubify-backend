package com.uu.au.repository;

import com.uu.au.models.HelpRequest;
import com.uu.au.models.User;
import com.uu.au.enums.DemonstrationStatus;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.time.LocalDateTime;
import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class HelpRequestRepositoryTests {

    @Autowired
    private HelpRequestRepository helpRequestRepository;

    @Autowired
    private EntityManager entityManager; // Used to persist objects belonging to other repositories

    @Test
    public void testFindAll(){
        // Create an HelpRequest object, persist it and test the findAll method
        HelpRequest helpRequest = HelpRequest.builder().build();

        // Test before saving the HelpRequest
        List<HelpRequest> helpRequestList = helpRequestRepository.findAll();
        assertEquals(0, helpRequestList.size());

        // Save the HelpRequest and test again
        helpRequestRepository.save(helpRequest);
        helpRequestList = helpRequestRepository.findAll();
        assertEquals(1, helpRequestList.size());
    }

    @Test
    public void testFindAllByHelper(){
        // Create a User and HelpRequest object, persist them and test the findAllByHelper method
        User helper = User.builder().build();
        entityManager.persist(helper);
        HelpRequest helpRequest = HelpRequest.builder().helper(helper).build();

        // Test before saving the HelpRequest
        List<HelpRequest> helpRequestList = helpRequestRepository.findAllByHelper(helper);
        assertEquals(0, helpRequestList.size());

        // Save the HelpRequest and test again
        helpRequestRepository.save(helpRequest);
        helpRequestList = helpRequestRepository.findAllByHelper(helper);
        assertEquals(1, helpRequestList.size());

        // Add helper to another HelpRequest and test again
        HelpRequest helpRequest2 = HelpRequest.builder().helper(helper).build();
        helpRequestRepository.save(helpRequest2);

        helpRequestList = helpRequestRepository.findAllByHelper(helper);
        assertEquals(2, helpRequestList.size());
    }

    @Test
    public void testFindAllBySubmitters(){
        // Create a User and HelpRequest object, persist them and test the findAllBySubmitters method
        User submitter = User.builder().build();
        entityManager.persist(submitter);
        HelpRequest helpRequest = HelpRequest.builder().submitters(Set.of(submitter)).build();

        // Test before saving the HelpRequest
        List<HelpRequest> helpRequestList = helpRequestRepository.findAllBySubmitters(submitter);
        assertEquals(0, helpRequestList.size());

        // Save the HelpRequest and test again
        helpRequestRepository.save(helpRequest);
        helpRequestList = helpRequestRepository.findAllBySubmitters(submitter);
        assertEquals(1, helpRequestList.size());

        // Add submitter to another HelpRequest with 2 submitters and test again
        User submitter2 = User.builder().build();
        entityManager.persist(submitter2);
        HelpRequest helpRequest2 = HelpRequest.builder().submitters(Set.of(submitter, submitter2)).build();
        helpRequestRepository.save(helpRequest2);

        helpRequestList = helpRequestRepository.findAllBySubmitters(submitter);
        assertEquals(2, helpRequestList.size());
    }

    @Test
    public void testUsersWithActiveHelpRequestsUpToRequestId(){
        // Create a User and HelpRequest object, persist them and test the usersWithActiveHelpRequestsUpToRequestId method
        User submitter = User.builder().build();
        entityManager.persist(submitter);        
        HelpRequest helpRequest = HelpRequest.builder().submitters(Set.of(submitter)).status(DemonstrationStatus.SUBMITTED).build();

        // Test before saving the HelpRequest
        Set<User> userSet = helpRequestRepository.usersWithActiveHelpRequestsUpToRequestId(100L);
        assertEquals(0, userSet.size());

        // Save the HelpRequest and test again
        helpRequestRepository.save(helpRequest);
        userSet = helpRequestRepository.usersWithActiveHelpRequestsUpToRequestId(helpRequest.getId()+1);
        assertEquals(1, userSet.size());

        // Add submitter to another HelpRequest with 2 submitters and test again
        User submitter2 = User.builder().build();
        entityManager.persist(submitter2);
        HelpRequest helpRequest2 = HelpRequest.builder().submitters(Set.of(submitter, submitter2)).status(DemonstrationStatus.SUBMITTED).build();
        helpRequestRepository.save(helpRequest2);

        // First test with same id as before (second HelpRequest not included)
        userSet = helpRequestRepository.usersWithActiveHelpRequestsUpToRequestId(helpRequest.getId()+1);
        assertEquals(1, userSet.size());
        assertEquals(submitter, userSet.iterator().next());
        
        // Increase id by 1 and test again
        userSet = helpRequestRepository.usersWithActiveHelpRequestsUpToRequestId(helpRequest2.getId()+1);
        assertEquals(2, userSet.size());
        
        // Second helpRequest is not active anymore
        helpRequest2.setReportTime(LocalDateTime.now());
        
        userSet = helpRequestRepository.usersWithActiveHelpRequestsUpToRequestId(helpRequest2.getId()+1);
        assertEquals(1, userSet.size());
        assertEquals(submitter, userSet.iterator().next());
    }

    @Test
    public void testUsersWithActiveHelpRequests (){
        // Create a User and HelpRequest object, persist them and test the usersWithActiveHelpRequests method
        User submitter = User.builder().build();
        entityManager.persist(submitter);        
        HelpRequest helpRequest = HelpRequest.builder().submitters(Set.of(submitter)).status(DemonstrationStatus.SUBMITTED).build();

        // Test before saving the HelpRequest
        Set<User> userSet = helpRequestRepository.usersWithActiveHelpRequests();
        assertEquals(0, userSet.size());

        // Save the HelpRequest and test again
        helpRequestRepository.save(helpRequest);
        userSet = helpRequestRepository.usersWithActiveHelpRequests();
        assertEquals(1, userSet.size());

        // Add submitter to another HelpRequest with 2 submitters and test again
        User submitter2 = User.builder().build();
        entityManager.persist(submitter2);
        HelpRequest helpRequest2 = HelpRequest.builder().submitters(Set.of(submitter, submitter2)).status(DemonstrationStatus.SUBMITTED).build();
        helpRequestRepository.save(helpRequest2);

        // First test with same id as before (second HelpRequest not included)
        userSet = helpRequestRepository.usersWithActiveHelpRequests();
        assertEquals(2, userSet.size());
        
        // Second helpRequest is not active anymore
        helpRequest2.setReportTime(LocalDateTime.now());
        
        userSet = helpRequestRepository.usersWithActiveHelpRequests();
        assertEquals(1, userSet.size());
        assertEquals(submitter, userSet.iterator().next());
    }
}