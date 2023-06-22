package com.uu.au.repository;

import com.uu.au.models.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.uu.au.models.Enrolment;

import java.util.Set;
import java.util.stream.Collectors;

@Repository
public interface EnrolmentRepository
        extends JpaRepository<Enrolment, Long> {

    public Set<Enrolment> findByCourseInstance(Course c);
}

