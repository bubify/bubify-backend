package com.uu.au.repository;

import com.uu.au.models.DemoListCleared;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DemoListClearedRepository
        extends JpaRepository<DemoListCleared, Long> {
}


