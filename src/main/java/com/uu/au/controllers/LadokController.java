package com.uu.au.controllers;

import com.uu.au.enums.LadokEntryType;
import com.uu.au.enums.Level;
import com.uu.au.enums.errors.UserErrors;
import com.uu.au.models.*;
import com.uu.au.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class LadokController {
    @Autowired
    private UserRepository users;

    @Autowired
    private AchievementRepository achievements;

    @Autowired
    private LadokEntryRepository ladokEntries;

    private Set<LadokEntry> newLadokEntriesForStudent(User u) {
        return ladokEntriesForStudent(u, true);
    }

    private Set<LadokEntry> allLadokEntriesForStudent(User u) {
        return ladokEntriesForStudent(u, false);
    }

    private Set<LadokEntry> ladokEntriesForStudent(User u, boolean onlyReturnNew) {
        /// Default result: the empty set
        final Set<LadokEntry> newEntries = new HashSet<>();

        /// Fetch student's existing ladok entries
        final Set<LadokEntry> existingEntries = ladokEntries.findByUser(u);
        /// Compute the set of achievements that have already been "consumed" by existing LADOK entries
        final Set<Achievement> consumedAchievements = new HashSet<>();
        existingEntries.forEach(e -> {
                   consumedAchievements.addAll(e.getConsumedByEntry());
                });

        /// Fetch student's unlocked achievements and subtract the lab achievements
        final Set<Achievement> unlockedAchievements =
                u.achievementsUnlocked()
                        .stream()
                        .filter(a -> a.getLevel() == Level.GRADE_3)
                        .collect(Collectors.toSet());
        /// Lab achievements do not give HP
        unlockedAchievements.removeAll(achievements.findAllLabAchievements());

        /// Achivements that mey be used to report HP = all unlocked, which have not been used previously (consumed)
        final Set<Achievement> reportableAchievements = unlockedAchievements.stream()
                .filter(a -> !consumedAchievements.contains(a))
                .collect(Collectors.toSet());
        /// Move all reportable Z achievements over to its own set (because it has separate constraints)
        final Set<Achievement> reportableAssignments =
                reportableAchievements.stream().filter(Achievement::isAssignment).collect(Collectors.toSet());
        reportableAchievements.removeAll(reportableAssignments); // To avoid overlapping

        /// Create entry for CEC is eligible, consuming CEC from reportable
        final Achievement CEC = achievements.findByCode("CEC").orElseThrow();
        if (reportableAchievements.contains(CEC)) {
            newEntries.add(LadokEntry.builder()
                    .user(u)
                    .type(LadokEntryType.IMPERATIVE_EXAM)
                    .consumedByEntry(Set.of(CEC))
                    .build());
            reportableAchievements.remove(CEC);
        }

        /// Create entry for JEC is eligible, consuming JEC from reportable
        final Achievement JEC = achievements.findByCode("JEC").orElseThrow();
        if (reportableAchievements.contains(JEC)) {
            newEntries.add(LadokEntry.builder()
                    .user(u)
                    .type(LadokEntryType.OBJECT_ORIENTED_EXAM)
                    .consumedByEntry(Set.of(JEC))
                    .build());
            reportableAchievements.remove(JEC);
        }

        /// Create entry for project if eligible, consuming all matching achievements
        /// https://wrigstad.com/ioopm/about.html#orgcf9b0e6
        Set<Achievement> projectAchievements = achievements.findAllProjectAchievements();
        if (reportableAchievements.containsAll(projectAchievements) && reportableAchievements.size() >= 10) { // 10 including project ach.
            reportableAchievements.removeAll(projectAchievements);
            var achievements= reportableAchievements.toArray(new Achievement[0]);
            var consumed = new HashSet<Achievement>(Arrays.asList(achievements).subList(0, 4));

            newEntries.add(LadokEntry.builder()
                    .user(u)
                    .type(LadokEntryType.PROJECT)
                    .consumedByEntry(consumed)
                    .build());
            reportableAchievements.removeAll(consumed);
        }

        /// Create entry for assignments 1 if eligible, consuming all matching achievements
        /// https://wrigstad.com/ioopm/about.html#org6e5ed7c
        if (reportableAssignments.size() > 1 && reportableAchievements.size() >= 15) {
            var entry= this.buildAssignmentEntry(reportableAchievements, reportableAssignments, LadokEntryType.ASSIGNMENTS1, u);
            newEntries.add(entry);
            reportableAchievements.removeAll(entry.getConsumedByEntry());
            reportableAssignments.removeAll(entry.getConsumedByEntry());
        }

        /// Create entry for assignments 1 if eligible, consuming all matching achievements
        /// https://wrigstad.com/ioopm/about.html#orgb556e8d
        if (reportableAssignments.size() > 1 && reportableAchievements.size() >= 15) {
            var entry = this.buildAssignmentEntry(reportableAchievements, reportableAssignments, LadokEntryType.ASSIGNMENTS2, u);
            newEntries.add(entry);
            reportableAchievements.removeAll(entry.getConsumedByEntry());
            reportableAssignments.removeAll(entry.getConsumedByEntry());
        }

        /// Save all newly created entries
        ladokEntries.saveAll(newEntries);
        ladokEntries.flush();

        if (!onlyReturnNew) {
            newEntries.addAll(existingEntries);
        } else {
            newEntries.addAll(existingEntries.stream().filter(e -> !e.isReported()).collect(Collectors.toSet()));
        }
        return newEntries;
    }

    private LadokEntry buildAssignmentEntry(Set<Achievement> reportableAchievements,
                                            Set<Achievement> reportableAssignments,
                                            LadokEntryType type,
                                            User u) {
        var assignments = reportableAssignments.toArray(new Achievement[0]);
        var achievements = reportableAchievements.toArray(new Achievement[0]);
        var consumed = new HashSet<Achievement>();
        consumed.addAll(Arrays.asList(assignments).subList(0, 2));
        consumed.addAll(Arrays.asList(achievements).subList(0, 15));

        return LadokEntry.builder()
                .user(u)
                .type(type)
                .consumedByEntry(consumed)
                .build();

    }

    @PreAuthorize("hasAuthority('Senior_TA') or hasAuthority('Teacher')")
    @PostMapping("/ladok/markAsReported")
    public List<Long> markLadokEntryAsReported(@RequestBody final List<Long> ladokEntryIds) {
        final LocalDateTime now = LocalDateTime.now();
        final var updatedEntries = ladokEntryIds.stream()
                .map(id -> ladokEntries.findById(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(e -> !e.isReported())
                .peek(e -> e.setReportTime(now))
                .collect(Collectors.toSet());
        ladokEntries.saveAll(updatedEntries);

        return updatedEntries.stream().map(LadokEntry::getId).collect(Collectors.toList());
    }

    @Autowired
    CourseRepository courses;

    @Autowired
    EnrolmentRepository enrolments;

    @PreAuthorize("hasAuthority('Senior_TA') or hasAuthority('Teacher')")
    @GetMapping("/ladok/listAll")
    public List<Json.LadokEntryInfo> allPossibleLadokEntries() {
        final var currentCourse = courses.currentCourseInstance();
        final var currentlyEnrolledStudents = users.findAllCurrentlyEnrolledStudents();

        return currentlyEnrolledStudents.stream()
                .sorted(Comparator.comparing(User::getLastName))
                .map(s -> Json.LadokEntryInfo.builder()
                        .user(s)
                        .ladokEntries(allLadokEntriesForStudent(s))
                        .build())
                .filter(i -> i.getLadokEntries().size()  > 0)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAuthority('Senior_TA') or hasAuthority('Teacher')")
    @GetMapping("/ladok/listUnreported")
    public List<Json.LadokEntryInfo> allUnreportedLadokEntries() {
        final var currentCourse = courses.currentCourseInstance();
        final var currentlyEnrolledStudents = users.findAllCurrentlyEnrolledStudents();

        return currentlyEnrolledStudents.stream()
                .sorted(Comparator.comparing(User::getLastName))
                .map(s -> Json.LadokEntryInfo.builder()
                        .user(s)
                        .ladokEntries(newLadokEntriesForStudent(s))
                        .build())
                .filter(i -> i.getLadokEntries().size()  > 0)
                .collect(Collectors.toList());
    }

    /// For efficiency, calculate grades using numbers
    private int achievementsNeededForGrade3 = 0;
    private int achievementsNeededForGrade4 = 0;
    private int achievementsNeededForGrade5 = 0;

    public Level calculateFinalGrade(Enrolment e) {
        /// Initialise requirements once
        if (achievementsNeededForGrade3 == 0) {
            final var req = achievements.findAllNeededForLevel(Level.GRADE_3);
            req.removeAll(achievements.findAllLabAchievements());
            achievementsNeededForGrade3 = req.size();
        }
        if (achievementsNeededForGrade4 == 0) {
            final var req = achievements.findAllNeededForLevel(Level.GRADE_4);
            req.removeAll(achievements.findAllLabAchievements());
            achievementsNeededForGrade4 = req.size() - achievementsNeededForGrade3;
        }
        if (achievementsNeededForGrade5 == 0) {
            final var req = achievements.findAllNeededForLevel(Level.GRADE_5);
            req.removeAll(achievements.findAllLabAchievements());
            achievementsNeededForGrade5 = req.size() - (achievementsNeededForGrade4 + achievementsNeededForGrade3);
        }

        /// Calculate unlocked achievements per level for student
        int unlocked3 = 0;
        int unlocked4 = 0;
        int unlocked5 = 0;
        final var unlocked = e.getAchievementsUnlocked().stream().map(AchievementUnlocked::getAchievement).filter(a -> !a.isLab()).collect(Collectors.toSet());
        for (Achievement a : unlocked) {
            switch (a.getLevel()) {
                case GRADE_3: unlocked3 += 1; break;
                case GRADE_4: unlocked4 += 1; break;
                case GRADE_5: unlocked5 += 1; break;
            }
        }

        /// Note: returns null if not passed yet
        if (unlocked3 < achievementsNeededForGrade3) {
            return null;
        } else {
            if (unlocked4 < achievementsNeededForGrade4) {
                return Level.GRADE_3;
            } else {
                if (unlocked5 < achievementsNeededForGrade5) {
                    return Level.GRADE_4;
                } else {
                    return Level.GRADE_5;
                }
            }
        }
    }

    @GetMapping("/ladok/finalGrade/{username}")
    public List<Json.FinalGradeInfo> finalGrade(String username) {
        final var currentUser = users.findByUserName(username).orElseThrow();
        final var currentCourse = courses.currentCourseInstance();
        final var currentEnrolment = currentUser.currentEnrolment().orElseThrow();

        if (currentEnrolment.getCourseInstance().equals(currentCourse)) {
            final var finalGrade = calculateFinalGrade(currentEnrolment);
            return finalGrade == null
                    ? List.of()
                    : List.of(Json.FinalGradeInfo.builder().user(currentUser).finalGrade(finalGrade).build());
        } else {
            throw UserErrors.notCurrentlyEnrolled();
        }
    }

    @PreAuthorize("hasAuthority('Senior_TA') or hasAuthority('Teacher')")
    @GetMapping("/ladok/finalGrades")
    public List<Json.FinalGradeInfo> finalGrades() {
        final var currentCourse = courses.currentCourseInstance();
        final var currentlyEnrolledStudents = users.findAllCurrentlyEnrolledStudents();

        return currentlyEnrolledStudents.stream()
                .filter(s ->allLadokEntriesForStudent(s).size() == LadokEntryType.NUMBER_OF_ENTRIES_FOR_PASS)
                .sorted(Comparator.comparing(User::getLastName))
                .map(u -> Json.FinalGradeInfo.builder()
                        .user(u)
                        .finalGrade(calculateFinalGrade(u.currentEnrolment().orElseThrow()))
                        .build())
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAuthority('Senior_TA') or hasAuthority('Teacher')")
    @GetMapping("/ladok/markAllFinalGradesReported")
    public List<String> showFinalGradesAndMarkReported() {
        final var now = LocalDateTime.now();
        final var finalGrades = finalGrades();
        final var fresh = new HashSet<User>();
        final var allEntries = finalGrades.stream()
                .flatMap(fgi -> newLadokEntriesForStudent(fgi.getUser()).stream())
                .filter(e -> !e.isReported())
                .peek(e -> e.setReportTime(now))
                .peek(e -> fresh.add(e.getUser()))
                .collect(Collectors.toSet());
        ladokEntries.saveAll(allEntries);
        return finalGrades.stream()
                .map(fgi -> fgi.getUser().getLastName() + ", " + fgi.getUser().getFirstName() + ", " + fgi.getFinalGrade() + (fresh.contains(fgi.getUser()) ? ", NEW" : ""))
                .sorted()
                .collect(Collectors.toList());


    }
}
