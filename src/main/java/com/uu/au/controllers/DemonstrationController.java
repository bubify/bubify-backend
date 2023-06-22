package com.uu.au.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import com.uu.au.AUPortal;
import com.uu.au.enums.DemonstrationStatus;
import com.uu.au.enums.Result;
import com.uu.au.enums.errors.*;
import com.uu.au.enums.Role;
import com.uu.au.models.*;
import com.uu.au.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
class DemonstrationController {

    @Autowired
    private DemonstrationRepository demonstrations;

    @Autowired
    private UserRepository users;

    @Autowired
    private AchievementRepository achievements;

    @Autowired
    private AchievementUnlockedRepository achievementUnlockedRepository;

    @Autowired
    private AchievementPushedBackRepository achievementPushedBackRepository;

    @Autowired
    private CourseRepository courses;

    @Autowired
    private WebSocketController webSocketController;

    @Autowired
    private AchievementFailedRepository achievementFailedRepository;

    public static final String filterStudent = "**,submitters[id,firstName,lastName,zoomRoom,verifiedProfilePic,needsProfilePic],examiner[id,firstName,lastName,-zoomRoom,-zoomPassword,-physicalRoom,-message]";

    public List<Demonstration> demonstrationRequestsCurrentCourseInstance() {
        var currentCourseStartDate = courses.currentCourseInstance().getStartDate().atStartOfDay();

        return demonstrations
                .findAll()
                .stream()
                .filter(d -> d.getRequestTime().isAfter(currentCourseStartDate))
                .collect(Collectors.toList());
    }

    private static final ReentrantLock uglyDemonstrationLock = new ReentrantLock();

    @PostMapping("/demonstration/request")
    public Demonstration requestDemonstration(@RequestBody Json.DemonstrationRequest demonstrationRequest) {
        uglyDemonstrationLock.lock();
        try {
            return _requestDemonstration(demonstrationRequest);
        } finally {
            uglyDemonstrationLock.unlock();
        }
    }
    public Demonstration _requestDemonstration(@RequestBody Json.DemonstrationRequest demonstrationRequest) {
        if (demonstrationRequest.getAchievementIds().isEmpty()) {
            throw DemonstrationErrors.demonstrationWithZeroAchievements();
        }

        var user = users.currentUser();

        var students = demonstrationRequest.getIds()
                .stream()
                .map(id -> users.findById(id)
                        .orElseThrow(UserErrors::userNotFound))
                .collect(Collectors.toSet());

        if (students.contains(user) == false) {
            throw GenericRequestListErrors.currentUserNotInSubmitters();
        }

        var course = courses.currentCourseInstance();
        var usersWithActiveDemonstrations = demonstrations.usersWithActiveDemoRequests();

        students.forEach(s -> {
                if (s.currentCourseInstance().isEmpty() || !s.currentCourseInstance().get().equals(course)) {
                    throw UserErrors.notCurrentlyEnrolled();
                }
                if (usersWithActiveDemonstrations.contains(s)) {
                    throw HelpErrors.userInMultipleHelpRequest();
                }
            });

        var goals = demonstrationRequest.getAchievementIds()
                .stream()
                .map(id -> achievements.findById(id)
                        .orElseThrow(AchievementErrors::achievementNotFound))
                .sorted(Comparator.comparing(Achievement::getCode))
                .collect(Collectors.toList());

        var enrolment = user.currentEnrolment().orElseThrow();
        var illegalDemonstrations = achievementFailedRepository
                .findAll()
                .stream()
                .filter(a -> a.getEnrolment().equals(enrolment))
                .filter(a -> a.getAchievement().isCodeExam() && a.getFailedTime().isAfter(course.codeExamDemonstrationBlocker().atStartOfDay()))
                .map(a -> a.getAchievement())
                .filter(a -> goals.contains(a))
                .collect(Collectors.toSet());

        if (illegalDemonstrations.size() > 0) {
            throw AuthErrors.actionNotAllowedByStudent(); // FIXME: better error code
        }

        var demo = Demonstration
                .builder()
                .submitters(students)
                .achievements(goals)
                .requestTime(LocalDateTime.now())
                .status(DemonstrationStatus.SUBMITTED)
                .zoomRoom(users.currentUser().getZoomRoom())
                .zoomPassword(demonstrationRequest.getZoomPassword())
                .physicalRoom(demonstrationRequest.getPhysicalRoom())
                .build();

        return saveAndNotify(demo);
    }

    private Demonstration fetchDemoAsTeacher(Long demoId) {
        if (users.currentUser().getRole() == Role.STUDENT) {
            throw DemonstrationErrors.studentsMayNotClaimDemonstration();
        }

        return demonstrations
                .findById(demoId)
                .orElseThrow(DemonstrationErrors::demonstrationNotFound);
    }

    @PreAuthorize("hasAuthority('Junior_TA') or hasAuthority('Senior_TA') or hasAuthority('Teacher')")
    @PostMapping("/demonstration/claim")
    public void claimDemonstration(@RequestBody Long demoId) {
        try {
            uglyDemonstrationLock.lock();
            _claimDemonstration(demoId);
        } finally {
            uglyDemonstrationLock.unlock();
        }
    }

    public void _claimDemonstration(@RequestBody Long demoId) {
        var demo = fetchDemoAsTeacher(demoId);
        var user = users.currentUser();

        if (demo.getStatus().equals(DemonstrationStatus.SUBMITTED)) {
            demo.setStatus(DemonstrationStatus.CLAIMED);
            demo.setExaminer(user);
            demo.setPickupTime(LocalDateTime.now());
            demo = demonstrations.saveAndFlush(demo);

            var examiner = demo.getExaminer();

            webSocketController.notifyDemoClaim(demo.getSubmitters(), examiner.getFirstName() + " " + examiner.getLastName());
            webSocketController.notifyDemoListSubscribers();
        }
    }

    @Autowired
    BackupController backupController;

    @PreAuthorize("hasAuthority('Junior_TA') or hasAuthority('Senior_TA') or hasAuthority('Teacher')")
    @PostMapping("/demonstration/unclaim")
    public void unclaimDemonstration(@RequestBody Long demoId) {
        try {
            uglyDemonstrationLock.lock();
            _unclaimDemonstration(demoId);
        } finally {
            uglyDemonstrationLock.unlock();
        }
    }

    public Demonstration _unclaimDemonstration(@RequestBody Long demoId) {
        var demo = fetchDemoAsTeacher(demoId);

        demo.setPickupTime(null);
        demo.setExaminer(null);
        demo.setStatus(DemonstrationStatus.SUBMITTED);

        return saveAndNotify(demo);
    }

    private Demonstration saveAndNotify(Demonstration demo) {
        var result = demonstrations.save(demo);
        webSocketController.notifyDemoListSubscribers();
        return SquigglyUtils.objectify(Squiggly.init(AUPortal.OBJECT_MAPPER, DemonstrationController.filterStudent), result, Demonstration.class);
    }

    /// results has this structure:
    ///
    /// achievmentId : [ { id : result } , ... , { id : result } ] , ... ]
    @PreAuthorize("hasAuthority('Junior_TA') or hasAuthority('Senior_TA') or hasAuthority('Teacher')")
    @PostMapping("/demonstration/done")
    public Demonstration completeDemonstration(@RequestBody Json.DemoResult demoResult) {
        try {
            uglyDemonstrationLock.lock();
            return _completeDemonstration(demoResult);
        } finally {
            uglyDemonstrationLock.unlock();
        }
    }

    public Demonstration _completeDemonstration(@RequestBody Json.DemoResult demoResult) {
        var demo = fetchDemoAsTeacher(demoResult.getDemoId());

        final var studentsInRequest = demo.getSubmitters();
        final var achievementsInRequest = demo.getAchievements();

        if (demo.getStatus() == DemonstrationStatus.COMPLETED) {
            throw DemonstrationErrors.demonstrationAlreadyGraded();
        } else {
            demo.setStatus(DemonstrationStatus.COMPLETED);
        }

        demo.setReportTime(LocalDateTime.now());
        var needProfilePicture = courses.currentCourseInstance().isProfilePictures();

        demoResult.getResults().forEach(t -> {
            var u = users.findOrThrow(t.getId());
            var a = achievements.findOrThrow(t.getAchievementId());
            Optional<Enrolment> e = u.currentEnrolment();

            if (e.isEmpty()) throw UserErrors.notCurrentlyEnrolled();

            if (u.achievementsPushedBack().contains(a)) {
                throw DemonstrationErrors.attemptToUnlockPushedBackAchievement();
            }

            if (needProfilePicture) {
                studentsInRequest.stream().filter(s -> !s.isVerifiedProfilePic()).findAny().map(s -> {
                    throw DemonstrationErrors.gradingRequiresVerifiedProfilePictures(); /// FIXME
                });
            }

            if (!studentsInRequest.contains(u) || !achievementsInRequest.contains(a)) {
                throw GenericRequestListErrors.doneDoesNotMatchRequest();
            }
        });

        demoResult.getResults().forEach(t -> {
            var u = users.findOrThrow(t.getId());
            var a = achievements.findOrThrow(t.getAchievementId());
            //noinspection OptionalGetWithoutIsPresent
            var e = u.currentEnrolment().get();

            // Don't add multiple PASS
            if (u.currentResult(a) == Result.PASS) {
                return;
            }

            switch (t.getResult()) {
                case FAIL:
                    achievementFailedRepository
                            .save(AchievementFailed
                                    .builder()
                                    .achievement(a)
                                    .enrolment(e)
                                    .build());
                    break;
                case PASS:
                    achievementUnlockedRepository
                            .save(AchievementUnlocked
                                    .builder()
                                    .achievement(a)
                                    .enrolment(e)
                                    .build());
                    backupController.backupUnlockedAchievement(u, a);
                    break;
                case PUSHBACK:
                    if (!u.achievementsUnlocked().contains(a)) {
                        achievementPushedBackRepository
                                .save(AchievementPushedBack
                                        .builder()
                                        .achievement(a)
                                        .enrolment(e)
                                        .build());
                    }
                    break;
                default:
                    // This should never happen
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unknown result type");
            }
        });

        studentsInRequest.forEach(u -> users.save(u));

        return saveAndNotify(demo);
    }

    public List<Demonstration> activeAndSubmitted() {
        return demonstrations
                .findAll()
                .stream()
                .filter(d -> d.isActiveAndSubmittedOrClaimed() && d.getStatus() == DemonstrationStatus.SUBMITTED)
                .collect(Collectors.toList());
    }

    @GetMapping("/demonstrations/activeAndSubmittedOrPickedUp")
    public List<Demonstration> activeAndSubmittedOrPickedUp() {
        var demoList = demonstrations
            .findAll()
            .stream()
            .filter(d -> d.isActiveAndSubmittedOrClaimed() && (d.getStatus() == DemonstrationStatus.SUBMITTED || d.getStatus() == DemonstrationStatus.CLAIMED))
            .collect(Collectors.toList());

        return SquigglyUtils.listify(Squiggly.init(AUPortal.OBJECT_MAPPER, DemonstrationController.filterStudent), demoList, Demonstration.class);
    }

    public List<Demonstration> pending() {
        return demonstrations
                .findAll()
                .stream()
                .filter(d -> d.isActiveAndSubmittedOrClaimed() && (d.getStatus() == DemonstrationStatus.SUBMITTED))
                .collect(Collectors.toList());
    }

    // TODO keep?
    @PreAuthorize("hasAuthority('Teacher')")
    @GetMapping("/demonstration/matchMaking")
    public List<Pair<User, Set<Achievement>>> matchMaking() {
        Set<Achievement> unlocked = users.currentUser().achievementsUnlocked();
        Set<Achievement> remaining = achievements
                .findAll()
                .stream()
                .filter(a -> !unlocked.contains(a))
                .collect(Collectors.toSet());

        return users.findAllCurrentlyEnrolledStudents()
                .stream()
                .map(u -> {
                    Set<Achievement> overlap = new HashSet<>(remaining);
                    overlap.removeAll(u.achievementsUnlocked());
                    return Pair.of(u, overlap);
                })
                .filter(p -> p.getSecond().size() > 0)
                .sorted((p1, p2) -> p2.getSecond().size() - p1.getSecond().size())
                .collect(Collectors.toList());
    }

    @GetMapping("/demonstration/cancel/{demoId}")
    public void cancelDemonstration(@PathVariable Long demoId) {
        try {
            uglyDemonstrationLock.lock();
            _cancelDemonstration(demoId);
        } finally {
            uglyDemonstrationLock.unlock();
        }
    }
    public void _cancelDemonstration(@PathVariable Long demoId) {
        final var demo = demonstrations.findById(demoId).orElseThrow(DemonstrationErrors::demonstrationNotFound);
        final var currentUser = users.currentUser();

        if (currentUser.isStudent() && !demo.getSubmitters().contains(currentUser)) {
            throw GenericRequestListErrors.currentUserNotInSubmitters();
        }

        demo.setStatus((currentUser.isStudent())
                       ? DemonstrationStatus.CANCELLED_BY_STUDENT
                       : DemonstrationStatus.CANCELLED_BY_TEACHER);

        saveAndNotify(demo);
    }

    @PreAuthorize("hasAuthority('Junior_TA') or hasAuthority('Senior_TA') or hasAuthority('Teacher')")
    @GetMapping("/demonstration/clearList")
    public void clearAllActiveDemonstrationsUser() {
        try {
            uglyDemonstrationLock.lock();
            _clearAllActiveDemonstrationsUser();
        } finally {
            uglyDemonstrationLock.unlock();
        }
    }

    public void clearAllActiveDemonstrations() {
        try {
            uglyDemonstrationLock.lock();
            _clearAllActiveDemonstrations();
        } finally {
            uglyDemonstrationLock.unlock();
        }
    }

    public void _clearAllActiveDemonstrations() {
        activeAndSubmitted().forEach(d -> {
            d.setStatus(DemonstrationStatus.CANCELLED_BY_TEACHER);
            demonstrations.save(d);
        });

        webSocketController.notifyDemoListCleared();
        webSocketController.notifyDemoListSubscribers();
    }

    public void _clearAllActiveDemonstrationsUser() {
        _clearAllActiveDemonstrations();
        demoListClearedRepository.save(DemoListCleared.builder().user(users.currentUser()).build());
    }

    @Autowired
    DemoListClearedRepository demoListClearedRepository;
}
