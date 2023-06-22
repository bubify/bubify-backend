package com.uu.au.repository;

import com.uu.au.models.Course;
import com.uu.au.models.Enrolment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.Set;

@Repository
public interface CourseRepository
        extends JpaRepository<Course, Long> {

        default Course currentCourseInstance() {
                return findAll()
                        .stream()
                        .max(Comparator.comparing(Course::getYear))
                        .orElseThrow(() -> new RuntimeException("No courses exist in database"));
        }
}
