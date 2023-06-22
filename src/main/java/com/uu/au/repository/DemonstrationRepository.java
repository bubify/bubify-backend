package com.uu.au.repository;

import com.uu.au.models.Achievement;
import com.uu.au.models.Demonstration;
import com.uu.au.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
 
@Repository
public interface DemonstrationRepository
        extends JpaRepository<Demonstration, Long> {

    List<Demonstration> findAllByExaminer(User user);

    List<Demonstration> findAllBySubmitters(User user);

    default Set<User> usersWithActiveDemoRequests() {
        return findAll()
                .stream()
                .filter(Demonstration::isActiveAndSubmittedOrClaimed)
                .flatMap(d -> d.getSubmitters().stream())
                .collect(Collectors.toSet());
    }
}

