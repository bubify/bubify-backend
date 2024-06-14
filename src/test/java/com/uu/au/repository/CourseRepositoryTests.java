package com.uu.au.repository;

import com.uu.au.models.Course;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class CourseRepositoryTests {

    @Autowired
    private CourseRepository courseRepository;

    @Test
    public void testFindAll(){
        // Create an Course object, persist it and test the findAll method
        Course course = Course.builder().build();

        // Test before saving the Course
        List<Course> courseList = courseRepository.findAll();
        assertEquals(0, courseList.size());

        // Save the Course and test again
        courseRepository.save(course);
        courseList = courseRepository.findAll();
        assertEquals(1, courseList.size());
    }

    @Test
    public void testCurrentCourseInstance(){
        // Create an Course object, persist it and test the currentCourseInstance method
        Course course = Course.builder().startDate(LocalDate.of(2023,1,1)).build();

        // Test before saving the Course
        assertThrows(RuntimeException.class, () -> courseRepository.currentCourseInstance());

        // Save the Course and test again
        courseRepository.save(course);
        Course currentCourse = courseRepository.currentCourseInstance();
        assertEquals(course, currentCourse);

        // Create another Course object, persist it and test the currentCourseInstance method
        Course course2 = Course.builder().startDate(LocalDate.of(2024,1,1)).build();

        // Save the Course and test again
        courseRepository.save(course2);
        currentCourse = courseRepository.currentCourseInstance();
        assertEquals(course2, currentCourse);
    }
}