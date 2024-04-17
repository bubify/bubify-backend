package com.uu.au.repository;

import com.uu.au.models.Demonstration;
import com.uu.au.models.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DemonstrationRepositoryTests {

    @Mock
    private DemonstrationRepository demonstrationRepository;

    @Test
    public void testFindAllByExaminer() {
        // Create a user and a demonstration object and mock the repository method before testing
        User examiner = User.builder().id(1L).build();
        Demonstration demonstration = Demonstration.builder().examiner(examiner).build();
        when(demonstrationRepository.findAllByExaminer(any(User.class))).thenReturn(Arrays.asList(demonstration));
        
        List<Demonstration> demonstrations = demonstrationRepository.findAllByExaminer(examiner);
        assertEquals(1, demonstrations.size());
        verify(demonstrationRepository, times(1)).findAllByExaminer(any(User.class));
    }

    @Test
    public void testFindAllBySubmitters() {
        // Create a user and a demonstration object and mock the repository method before testing
        User submitter = User.builder().id(1L).build();
        Demonstration demonstration = Demonstration.builder().submitters(Set.of(submitter)).build();
        when(demonstrationRepository.findAllBySubmitters(any(User.class))).thenReturn(Arrays.asList(demonstration));

        List<Demonstration> demonstrations = demonstrationRepository.findAllBySubmitters(submitter);
        assertEquals(1, demonstrations.size());
        verify(demonstrationRepository, times(1)).findAllBySubmitters(any(User.class));
    }

    @Test
    public void testUsersWithActiveDemoRequests() {
        // Create a user and a demonstration object and mock the repository method before testing
        User submitter = User.builder().id(1L).build();
        when(demonstrationRepository.usersWithActiveDemoRequests()).thenReturn(Set.of(submitter));

        Set<User> users = demonstrationRepository.usersWithActiveDemoRequests();
        assertEquals(1, users.size());
        verify(demonstrationRepository, times(1)).usersWithActiveDemoRequests();
    }
}