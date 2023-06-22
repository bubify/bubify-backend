package com.uu.au.repository;

import com.uu.au.models.HelpRequest;
import com.uu.au.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
 
@Repository
public interface HelpRequestRepository
        extends JpaRepository<HelpRequest, Long> {

    List<HelpRequest> findAllByHelper(User user);

    List<HelpRequest> findAllBySubmitters(User user);

    default Set<User> usersWithActiveHelpRequestsUpToRequestId(Long id) {
        Set<User> uu = new HashSet<>();
        findAll()
                .stream()
                .filter(HelpRequest::isActiveAndSubmittedOrClaimedOrInFlight)
                .filter(hr -> hr.getId() < id)
                .forEach(hr -> uu.addAll(hr.getSubmitters()));
        return uu;
    }

    default Set<User> usersWithActiveHelpRequests() {
        Set<User> uu = new HashSet<>();
        findAll()
                .stream()
                .filter(HelpRequest::isActiveAndSubmittedOrClaimed)
                .forEach(hr -> uu.addAll(hr.getSubmitters()));
        return uu;
    }
}

