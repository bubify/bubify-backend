package com.uu.au.repository;

import com.uu.au.models.AchievementPushedBack;
import com.uu.au.models.Achievement;
import com.uu.au.models.User;
import com.uu.au.models.Course;
import com.uu.au.models.Enrolment;
import org.springframework.web.server.ResponseStatusException;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class AchievementPushedBackRepositoryTests {

    @Autowired
    private AchievementPushedBackRepository achievementPushedBackRepository;
    
    @Autowired
    private EnrolmentRepository enrolmentRepository;
    
    @Autowired
    private AchievementRepository achievementRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CourseRepository courseRepository;

    @Test
    public void testFindAll(){
        // Create an AchievementPushedBack object, persist it and test the findAll method
        AchievementPushedBack achievementPushedBack = AchievementPushedBack.builder().build();

        // Test before saving the AchievementPushedBack
        List<AchievementPushedBack> achievementPushedBackList = achievementPushedBackRepository.findAll();
        assertEquals(0, achievementPushedBackList.size());

        // Save the AchievementPushedBack and test again
        achievementPushedBackRepository.save(achievementPushedBack);
        achievementPushedBackList = achievementPushedBackRepository.findAll();
        assertEquals(1, achievementPushedBackList.size());
    }

    @Test
    public void testFindAllByEnrolmentId(){
        // Create an AchievementPushedBack and Enrolement object, persist them and test the findAllByEnrolmentId method
        Enrolment enrolment = Enrolment.builder().build();
        enrolmentRepository.save(enrolment);
        AchievementPushedBack achievementPushedBack = AchievementPushedBack.builder().enrolment(enrolment).build();

        // Test before saving the AchievementPushedBack
        Set<AchievementPushedBack> achievementPushedBackSet = achievementPushedBackRepository.findAllByEnrolmentId(enrolment.getId());
        assertEquals(0, achievementPushedBackSet.size());

        // Save the AchievementPushedBack and test again
        achievementPushedBackRepository.save(achievementPushedBack);
        achievementPushedBackSet = achievementPushedBackRepository.findAllByEnrolmentId(enrolment.getId());
        assertEquals(1, achievementPushedBackSet.size());

        // Add another AchievementPushedBack and test again
        AchievementPushedBack achievementPushedBack2 = AchievementPushedBack.builder().enrolment(enrolment).build();
        achievementPushedBackRepository.save(achievementPushedBack2);
        achievementPushedBackSet = achievementPushedBackRepository.findAllByEnrolmentId(enrolment.getId());
        assertEquals(2, achievementPushedBackSet.size());
    }

    @Test
    public void testFindAllByAchievementId(){
        // Create an AchievementPushedBack and Achievement object, persist them and test the findAllByAchievementId method
        Achievement achievement = Achievement.builder().build();
        achievementRepository.save(achievement);
        AchievementPushedBack achievementPushedBack = AchievementPushedBack.builder().achievement(achievement).build();

        // Test before saving the AchievementPushedBack
        Set<AchievementPushedBack> achievementPushedBackSet = achievementPushedBackRepository.findAllByAchievementId(achievement.getId());
        assertEquals(0, achievementPushedBackSet.size());

        // Save the AchievementPushedBack and test again
        achievementPushedBackRepository.save(achievementPushedBack);
        achievementPushedBackSet = achievementPushedBackRepository.findAllByAchievementId(achievement.getId());
        assertEquals(1, achievementPushedBackSet.size());

        // Add another AchievementPushedBack with same Achievement and test again
        AchievementPushedBack achievementPushedBack2 = AchievementPushedBack.builder().achievement(achievement).build();
        achievementPushedBackRepository.save(achievementPushedBack2);
        achievementPushedBackSet = achievementPushedBackRepository.findAllByAchievementId(achievement.getId());
        assertEquals(2, achievementPushedBackSet.size());
    }

    @Test
    public void testFindAllActivePushBacksForId(){
        // Create an AchievementPushedBack and others objects needed, persist them and test the findAllActivePushBacksForId method
        Achievement achievement = Achievement.builder().build();
        achievementRepository.save(achievement);
        Course course = Course.builder().build();
        courseRepository.save(course);
        Enrolment enrolment = Enrolment.builder().courseInstance(course).build();
        enrolmentRepository.save(enrolment);
        User user = User.builder().enrolments(Set.of(enrolment)).build();
        userRepository.save(user);
        AchievementPushedBack achievementPushedBack = AchievementPushedBack.builder().achievement(achievement).enrolment(enrolment).pushedBackTime(LocalDateTime.now()).build();

        // Test before saving the AchievementPushedBack
        Set<Achievement> achievementSet = achievementPushedBackRepository.findAllActivePushBacksForId(user);
        assertEquals(0, achievementSet.size());

        // Save the AchievementPushedBack and test again
        achievementPushedBackRepository.save(achievementPushedBack);
        achievementSet = achievementPushedBackRepository.findAllActivePushBacksForId(user);
        assertEquals(1, achievementSet.size());

        // Add another AchievementPushedBack and test again
        Achievement achievement2 = Achievement.builder().build();
        achievementRepository.save(achievement2);
        AchievementPushedBack achievementPushedBack2 = AchievementPushedBack.builder().achievement(achievement2).enrolment(enrolment).pushedBackTime(LocalDateTime.now()).build();
        achievementPushedBackRepository.save(achievementPushedBack2);
        achievementSet = achievementPushedBackRepository.findAllActivePushBacksForId(user);
        assertEquals(2, achievementSet.size());

        // Assert UserErrors::enrolmentNotFound is thrown when user has no enrolments
        User user2 = User.builder().build();
        assertThrows(ResponseStatusException.class, () -> achievementPushedBackRepository.findAllActivePushBacksForId(user2));

        // BUG? java.lang.NullPointerException: Cannot invoke "com.uu.au.models.User.currentEnrolment()" because "user" is null
        // Method currently unused in the codebase
        // assertNull(achievementPushedBackRepository.findAllActivePushBacksForId(null));
    }
}