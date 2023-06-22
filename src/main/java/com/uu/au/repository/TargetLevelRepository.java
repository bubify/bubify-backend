package com.uu.au.repository;

import com.uu.au.models.TargetLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Repository
public interface TargetLevelRepository
        extends JpaRepository<TargetLevel, Long> {

    List<TargetLevel> findAllByEnrolmentId(Long enrolmentId);

    default Optional<TargetLevel> findLatestByEnrolmentId(Long enrolmentId) {
        return findAllByEnrolmentId(enrolmentId).stream().max(Comparator.comparing(TargetLevel::getChangeTime));
    }
}

