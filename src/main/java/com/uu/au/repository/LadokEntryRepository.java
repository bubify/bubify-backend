package com.uu.au.repository;

import com.uu.au.models.LadokEntry;
import com.uu.au.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

public interface LadokEntryRepository extends JpaRepository<LadokEntry, Long> {
    Optional<LadokEntry> findById(Long id);
    Set<LadokEntry> findByUser(User u);
}

