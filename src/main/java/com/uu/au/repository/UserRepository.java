package com.uu.au.repository;

import com.uu.au.enums.Role;
import com.uu.au.enums.errors.UserErrors;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.uu.au.models.User;

@Repository
public interface UserRepository
        extends JpaRepository<User, Long> {

    Optional<User> findByUserName(String userName);
    Optional<User> findByEmail(String userName);

    default User currentUser() {
        try {
            return findById(Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName()))
                    .orElseThrow(UserErrors::invalidCurrentUser);

        } catch (NumberFormatException nfe) {
            throw UserErrors.malformedUserName(SecurityContextHolder.getContext().getAuthentication().getName());
        }
    }

    default Optional<User> currentUser2() {
        return Optional.ofNullable(currentUser());
    }

    default Set<User> findAllStudents() {
        return findAll()
            .stream()
            .filter(u -> u.getRole() == Role.STUDENT)
            .collect(Collectors.toSet());
    }

    default Set<User> findAllCurrentlyEnrolledStudents() {
        return findAll()
                .stream()
                .filter(u -> u.getRole() == Role.STUDENT && u.currentEnrolment().isPresent())
                .collect(Collectors.toSet());
    }

    default User findOrThrow(Long id) {
        return findById(id)
                .orElseThrow(UserErrors::userNotFound);
    }

    default User findByUserNameOrThrow(String username) {
        return findByUserName(username).orElseThrow(UserErrors::userNotFound);
    }

    Optional<User> findByGitHubHandle(String gitHubHandle);
}
