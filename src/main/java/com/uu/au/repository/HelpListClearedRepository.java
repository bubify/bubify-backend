package com.uu.au.repository;

import com.uu.au.models.HelpListCleared;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HelpListClearedRepository
        extends JpaRepository<HelpListCleared, Long> {
}
