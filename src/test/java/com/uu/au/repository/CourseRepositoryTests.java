package com.uu.au.repository;

import com.uu.au.models.Course;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

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
}