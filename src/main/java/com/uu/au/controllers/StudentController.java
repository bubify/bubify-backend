package com.uu.au.controllers;

import com.uu.au.enums.errors.UserErrors;
import com.uu.au.models.*;
import com.uu.au.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(maxAge = 3600)
@RestController
public class StudentController {
    @Autowired
    private UserRepository users;
    @Autowired
    private AchievementRepository achievements;

    @GetMapping("/remaining/demonstrable/count")
    public Long remainingDemonstrableCount() {
        return (long) remainingDemonstrable().size();
    }

    @GetMapping("/remaining/all/count")
    public Long remainingAllCount() {
        return (long) remainingAll().size();
    }

    @GetMapping("/unlocked/count")
    public Long unlockedCount() {
        return (long) unlocked().size();
    }

    @GetMapping("/progress")
    public Map<Integer, Integer> progress() {
        final var user = users.currentUser();
        final var enrolment = user.currentEnrolment();
        final var courseStartDay = user.currentCourseInstance().get().getStartDate().getDayOfYear();

        final var weekAchievementsMap = new HashMap<Integer, Integer>();

        enrolment.ifPresent(ce -> ce.getAchievementsUnlocked()
                .forEach(au -> {
                    var unlockDay = au.getUnlockTime().toLocalDate().getDayOfYear();
                    var courseWeek = (unlockDay - courseStartDay) / 7;
                    weekAchievementsMap.put(courseWeek, weekAchievementsMap.getOrDefault(courseWeek, 0) + 1);
                })
        );

        return weekAchievementsMap;
    }

    @GetMapping("/progress/detailed")
     public List<Json.Progress> progressDetailed() {
         return users.currentUser().currentEnrolment()
                 .map(value -> value
                         .getAchievementsUnlocked()
                         .stream()
                         .map(au -> new Json.Progress(au.getUnlockTime(), au.getAchievement()))
                         .sorted(Comparator.comparing(Json.Progress::getDate))
                         .collect(Collectors.toList()))
                 .orElseGet(LinkedList::new);
     }

    @GetMapping("/pushedback")
    public List<Achievement> pushedBack() {
        return users
                .currentUser()
                .currentEnrolment().get()
                .getAchievementsPushedBack()
                .stream()
                .map(AchievementPushedBack::getAchievement)
                .sorted(Comparator.comparing(Achievement::getId))
                .collect(Collectors.toList());
    }

     @GetMapping("/unlocked")
     public List<Achievement> unlocked() {
        return unlocked(users.currentUser());
     }

     public List<Achievement> unlocked(User user) {
         return user
             .currentEnrolment().orElseThrow(UserErrors::enrolmentNotFound)
             .getAchievementsUnlocked()
             .stream()
             .map(AchievementUnlocked::getAchievement)
             .sorted(Comparator.comparing(Achievement::getId))
             .collect(Collectors.toList());
     }

     @Autowired
     AchievementFailedRepository achievementFailedRepository;

     private List<Json.DecoratedAchievement> remaining(List<Achievement> allAchievements, User user, boolean removeUnlocked) {
         var enrolment = user.currentEnrolment();
         if (achievements.count() == 0 || enrolment.isEmpty()) return new ArrayList<Json.DecoratedAchievement>();
         var unlocked = user.achievementsUnlocked();
         var pushedBack = user.achievementsPushedBack();

         var course = user.currentCourseInstance().orElseThrow();
         var failedCodeExams = achievementFailedRepository
                 .findAll()
                 .stream()
                 .filter(a -> a.getEnrolment().equals(enrolment))
                 .filter(a -> a.getAchievement().isCodeExam() && a.getFailedTime().isAfter(course.codeExamDemonstrationBlocker().atStartOfDay()))
                 .map(a -> a.getAchievement())
                 .collect(Collectors.toSet());

         return allAchievements
                 .stream()
                 .filter(a -> !removeUnlocked || !unlocked.contains(a))
                 .filter(a -> course.isOnlyIntroductionTasks() && !user.isPreviouslyEnrolled() ? a.isIntroTask() : true)
                 .filter(a -> course.isExamMode() ? a.isCodeExam() : !a.isCodeExam())
	     //.filter(a -> !failedCodeExams.contains(a))
                 .sorted(Comparator.comparing(Achievement::getId))
                 .map(a -> new Json.DecoratedAchievement(a, pushedBack, unlocked, failedCodeExams.contains(a)))
                 .collect(Collectors.toList());
     }

     private List<Json.DecoratedAchievement> remainingAll(List<Achievement> allAchievements, User user, boolean removeUnlocked, boolean showAll) {
        var enrolment = user.currentEnrolment();
        if (achievements.count() == 0 || enrolment.isEmpty()) return new ArrayList<Json.DecoratedAchievement>();
        var unlocked = user.achievementsUnlocked();
        var pushedBack = user.achievementsPushedBack();

        var course = user.currentCourseInstance().orElseThrow();
        var failedCodeExams = achievementFailedRepository
                .findAll()
                .stream()
                .filter(a -> a.getEnrolment().equals(enrolment))
                .filter(a -> a.getAchievement().isCodeExam() && a.getFailedTime().isAfter(course.codeExamDemonstrationBlocker().atStartOfDay()))
                .map(a -> a.getAchievement())
                .collect(Collectors.toSet());

        return allAchievements
                .stream()
                .filter(a -> course.isOnlyIntroductionTasks() && !showAll ? a.isIntroTask() : true)
                .filter(a -> !removeUnlocked || !unlocked.contains(a))
        //.filter(a -> !failedCodeExams.contains(a))
                .sorted(Comparator.comparing(Achievement::getId))
                .map(a -> new Json.DecoratedAchievement(a, pushedBack, unlocked, failedCodeExams.contains(a)))
                .collect(Collectors.toList());
    }


     @GetMapping("/remaining/demonstrable")
     public List<Json.DecoratedAchievement> remainingDemonstrable() {
        return remaining(achievements.findAll(), users.currentUser(), true);
     }

    @GetMapping("/remaining/all")
    public List<Json.DecoratedAchievement> remainingAll() {
        return remainingAll(achievements.findAll(), users.currentUser(),false, false);
    }

    @PreAuthorize("hasAuthority('Senior_TA') or hasAuthority('Teacher')")
    @GetMapping("/remaining/demonstrable/{userid}")
    public List<Json.DecoratedAchievement> remainingDemonstrable(@PathVariable Long userid) {
        return remaining(achievements.findAll(), users.findOrThrow(userid), true);
    }

    @PreAuthorize("hasAuthority('Senior_TA') or hasAuthority('Teacher')")
    @GetMapping("/remaining/all/{userid}")
    public List<Json.DecoratedAchievement> remainingAll(@PathVariable Long userid) {
        return remainingAll(achievements.findAll(), users.findOrThrow(userid),false, true);
    }

    @PreAuthorize("hasAuthority('Senior_TA') or hasAuthority('Teacher') or hasAuthority('Junior_TA')")
    @GetMapping("/remaining/alreadyPassed/{userid}")
    public Map<Long, Boolean> remainingAlreadyPassed(@PathVariable Long userid, @RequestParam List<Long> achievementIds) {
        List<Achievement> achievementList = achievements.findAllById(achievementIds);
        List<String> passedAchievements = users
                .findOrThrow(userid)
                .passedAchievements(achievementList);

        Map<Long, Boolean> passStatusMap = new HashMap<>();
        for (Achievement achievement : achievementList) {
                passStatusMap.put(achievement.getId(), passedAchievements.contains(achievement.getCode()));
        }

        return passStatusMap;
    }

    @Autowired
     private AchievementPushedBackRepository achievementPushedBackRepository;
}
