package com.uu.au.controllers;

import com.uu.au.AUPortal;
import com.uu.au.enums.Level;
import com.uu.au.enums.Result;
import com.uu.au.enums.Role;
import com.uu.au.enums.errors.AuthErrors;
import com.uu.au.enums.errors.CourseErrors;
import com.uu.au.enums.errors.UserErrors;
import com.uu.au.models.*;
import com.uu.au.repository.*;
import net.coobird.thumbnailator.Thumbnails;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.aop.framework.AopContext;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.*;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.*;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentLinkedDeque;

/// This controller is an internal staging area for API endpoints
/// before we ultimately decide how to structure the API internally
@CrossOrigin(maxAge = 3600)
@RestController
class GodController {
    @Autowired
    UserRepository users;
    @Autowired
    HelpRequestRepository helpRequestRepository;
    @Autowired
    DemonstrationRepository demonstrationRepository;
    @Autowired
    AchievementController achievementController;
    @Autowired
    StudentController studentController;
    @Autowired
    TargetLevelRepository targetLevelRepository;
    @Autowired
    AchievementUnlockedRepository achievementUnlockedRepository;
    @Autowired
    AchievementRepository achievementRepository;
    @Autowired
    AchievementPushedBackRepository achievementPushedBackRepository;
    @Autowired
    DemonstrationController demonstrationController;
    @Autowired
    HelpRequestController helpRequestController;
    @Autowired
    EnrolmentRepository enrolmentRepository;
    @Autowired
    private WebSocketController webSocketController;

    private final Logger logger = LoggerFactory.getLogger(GodController.class);

    final ConcurrentLinkedDeque<Pair<Long, File>> imagesToScale = new ConcurrentLinkedDeque<>();

    @Scheduled(fixedRate = 3000)
    void processThumbnails() {
        while (!imagesToScale.isEmpty()) {
            var userId_file = imagesToScale.pop();
            var unscaled = userId_file.getSecond();

            try {
                var thumbnail = new File(unscaled.getParentFile(), "thumbnail." + unscaled.getName());

                Thumbnails.of(unscaled)
                        .size(400, 400)
                        .toFile(thumbnail);

                installThumbnailInUser(userId_file.getFirst(), thumbnail.getAbsolutePath());

            } catch (IOException e) {
                logger.error("Failed to scale " + unscaled.getAbsolutePath());
            }
        }
    }

    @Transactional
    void installThumbnailInUser(Long uid, String filePath) {
        users.findById(uid).ifPresent(u -> {
            u.setProfilePicThumbnail(filePath);
            profilePicCache.remove(uid);
            users.save(u);
        });
        webSocketController.notifyProcessedProfilePicture(uid);
    }

    @PreAuthorize("hasAuthority('Junior_TA') or hasAuthority('Senior_TA') or hasAuthority('Teacher')")
    @GetMapping("/clearLists")
    public void clearAllRequests() {
        helpRequestController.clearAllActiveHelpRequestsUser();
        demonstrationController.clearAllActiveDemonstrationsUser();
    }

    @Scheduled(cron = "50 14 13 * * ?")
    public void clearAllRequestsCron() {
        var course = courseRepository.currentCourseInstance();
        if (course == null || course.isClearQueuesUsingCron() == false) {
            logger.info("Cron job to clear queues was scheduled but ignored according to current course settings or is missing a course instance");
            return;
        }
        helpRequestController.clearAllActiveHelpRequests();
        demonstrationController.clearAllActiveDemonstrations();
    }

    @PreAuthorize("hasAuthority('Senior_TA') or hasAuthority('Teacher')")
    @GetMapping("/achievement/code-to-id/{code}")
    public String achivementCodeToId(@PathVariable String code) {
        var achievement = achievementRepository.findByCode(code);
        return achievement.map(value -> value.getId().toString()).orElseGet(() -> "Achivement " + code + " not found");
    }

    @PreAuthorize("hasAuthority('Senior_TA') or hasAuthority('Teacher')")
    @GetMapping("/achievement/all-remaining/{code}")
    public List<String> achievementAllRemaining(@PathVariable String code) {
        var achievement = achievementRepository.findByCode(code).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No such achievement '" + code + "'"));

        return users
                .findAllCurrentlyEnrolledStudents()
                .stream()
                .filter(u -> u.currentEnrolment().map(e -> !e.isUnlocked(achievement)).orElse(false))
                .map(u -> u.getFirstName() + " " + u.getLastName() + " <" + u.getEmail() + ">")
                .collect(Collectors.toList());
    }

    @GetMapping("/stats")
    public Json.Stats stats() {
        return stats(users.currentUser());
    }

    @PreAuthorize("hasAuthority('Senior_TA') or hasAuthority('Teacher')")
    @GetMapping("/stats/{userid}")
    public Json.Stats stats(@PathVariable Long userid) {
        return stats(users.findById(userid).orElseThrow(UserErrors::userNotFound));
    }

    private Json.Stats stats(User user) {
        final var enrolment = user.currentEnrolment().orElseThrow(UserErrors::enrolmentNotFound);
        final var weeksNeeded = user.getDeadline() != null ? (int) ChronoUnit.WEEKS.between(enrolment.getCourseInstance().getStartDate(), user.getDeadline()) : 20;
        final var currentCourseWeek = enrolment.getCourseInstance().currentCourseWeek();

        var _currentTarget = targetLevelRepository
                .findLatestByEnrolmentId(enrolment.getId());
        var currentTarget = _currentTarget.isPresent()
                ? _currentTarget.get().getLevel()
                : Level.GRADE_3;

        final var levelToTarget = new HashMap<Level, Integer>();

        for (var a : achievementRepository.findAll()) {
            switch (a.getLevel()) {
                case GRADE_3:
                    levelToTarget.put(Level.GRADE_3, levelToTarget.getOrDefault(Level.GRADE_3, 0) + 1);
                case GRADE_4:
                    levelToTarget.put(Level.GRADE_4, levelToTarget.getOrDefault(Level.GRADE_4, 0) + 1);
                case GRADE_5:
                    levelToTarget.put(Level.GRADE_5, levelToTarget.getOrDefault(Level.GRADE_5, 0) + 1);
            }
        }

        final var burnDown = enrolment.burnDown(levelToTarget);

        var unlockedSoFar = studentController.unlocked(user).size();
        var averageVelocity = currentCourseWeek > 0
                ? unlockedSoFar / (1.0 * currentCourseWeek)
                : 0;
        int remaining = burnDown.get(currentTarget).get(Math.max(currentCourseWeek, 0));
        double remainingWeeks = user.getDeadline() == null ? 20 - currentCourseWeek : Math.max((double) ChronoUnit.DAYS.between(LocalDate.now(), user.getDeadline()) / 7, 0.0);
        int remainingDays = user.getDeadline() == null ? (int) ChronoUnit.DAYS.between(LocalDate.now(), ChronoUnit.DAYS.addTo(enrolment.getCourseInstance().getStartDate(), 20*7)) : (int) ChronoUnit.DAYS.between(LocalDate.now(), user.getDeadline());

        int currentVelocity = currentCourseWeek > 0
                ? burnDown.get(Level.GRADE_5).get(currentCourseWeek - 1) - burnDown.get(Level.GRADE_5).get(currentCourseWeek)
                : unlockedSoFar;

        burnDown.values().forEach(list -> {
            if (list.size() >= 3) list.subList(0, 2).clear();
        });

        var res = Json.Stats
                .builder()
                .currentCourseWeek(currentCourseWeek + 1)
                .weeksNeeded(weeksNeeded)
                .remainingWeeks(remainingWeeks)
                .remaining(remaining)
                .averageVelocity(averageVelocity)
                .currentVelocity(currentVelocity)
                .targetVelocity((remainingWeeks > 1.0 && remaining > 0) ? ((double)remaining / ((double)remainingDays)) * 7.0 : remaining)
                .currentTarget(currentTarget.getLevel())
                .burnDown(burnDown)
                .achievementsPerLevel(levelToTarget)
                .build();
        return res;
    }

    public static long avg(long sum, long observations) {
        return observations > 0
                ? sum / observations
                : 0;
    }

    public long[] _getDemoQos() {
        var demos = demonstrationController.demonstrationRequestsCurrentCourseInstance();
        long demosPickedUp = 0;
        long demosReported = 0;
        long totalTimeDemosPickedUp = 0;
        long totalTimeDemosReported = 0;
        for (Demonstration demo : demos) {
            switch (demo.getStatus()) {
                case COMPLETED:
                    if (demo.getRequestTime() != null && demo.getReportTime() != null) {
                        demosReported += 1;
                        totalTimeDemosReported += Math.min(demo.roundTripTimeInMinutes(), 300);
                    }
                case CLAIMED:
                    if (demo.getRequestTime() != null && demo.getPickupTime() != null) {
                        demosPickedUp += 1;
                        totalTimeDemosPickedUp += Math.min(demo.pickupTimeInMinutes(), 300);
                    }
            }
        }
        return new long[]{demosPickedUp, totalTimeDemosPickedUp, demosReported, totalTimeDemosReported};
    }

    public long[] _getHelpQos() {
        var helpRequests = helpRequestController.helpRequestsCurrentCourseInstance();
        long helpRequestsPickedUp = 0;
        long helpRequestsReported = 0;
        long totalTimeHelpRequestsPickedUp = 0;
        long totalTimeHelpRequestsReported = 0;
        for (HelpRequest helpRequest : helpRequests) {
            switch (helpRequest.getStatus()) {
                case COMPLETED:
                    if (helpRequest.getRequestTime() != null && helpRequest.getReportTime() != null) {
                        helpRequestsReported += 1;
                        totalTimeHelpRequestsReported += Math.min(helpRequest.roundTripTimeInMinutes(), 300);
                    }
                case CLAIMED:
                    if (helpRequest.getRequestTime() != null && helpRequest.getPickupTime() != null) {
                        helpRequestsPickedUp += 1;
                        totalTimeHelpRequestsPickedUp += Math.min(helpRequest.pickupTimeInMinutes(), 300);
                    }
            }
        }
        return new long[]{helpRequestsPickedUp, totalTimeHelpRequestsPickedUp, helpRequestsReported, totalTimeHelpRequestsReported};
    }

    @PreAuthorize("hasAuthority('Junior_TA') or hasAuthority('Senior_TA') or hasAuthority('Teacher')")
    @GetMapping("/qos")
    public Json.QOSResult qos() {
        var instance = ((GodController)AopContext.currentProxy());
        var demoQos = instance._getDemoQos();
        long demosPickedUp = demoQos[0];
        long totalTimeDemosPickedUp = demoQos[1];
        long demosReported = demoQos[2];
        long totalTimeDemosReported = demoQos[3];

        var helpQos = instance._getHelpQos();
        long helpRequestsPickedUp = helpQos[0];
        long totalTimeHelpRequestsPickedUp = helpQos[1];
        long helpRequestsReported = helpQos[2];
        long totalTimeHelpRequestsReported = helpQos[3];

        var currentUser = users.currentUser();
        var currentUserRole = currentUser.getRole();
        boolean isPriviliged = currentUserRole == Role.SENIOR_TA || currentUserRole == Role.TEACHER;

        var qos = Json.QOSResult
            .builder()
            .demonstrationsPending(demonstrationController.pending().size())
            .helpRequestsPending(helpRequestController.pending().size())
            .demonstrationsPickupTime(avg(totalTimeDemosPickedUp, demosPickedUp))
            .demonstrationsRoundtripTime(avg(totalTimeDemosReported, demosReported))
            .helpRequestsPickupTime(avg(totalTimeHelpRequestsPickedUp, helpRequestsPickedUp))
            .helpRequestsRoundtripTime(avg(totalTimeHelpRequestsReported, helpRequestsReported));

        if (isPriviliged) {
            var students = users.findAll().stream()
                .filter(u -> u.isStudent()).collect(Collectors.toList());;
            long amountStudents = students.size();

            var studentsLoggedIn = users.findAll().stream()
                .filter(u -> u.isStudent())
                .filter(u->u.getLastLogin() != null).collect(Collectors.toList());;

            var studentsLoggedInLastTwoWeeks = users.findAll().stream()
                .filter(u -> u.isStudent())
                .filter(u->u.getLastLogin() != null)
                .filter(u-> ChronoUnit.WEEKS.between(u.getLastLogin(), LocalDateTime.now()) <= 2).collect(Collectors.toList());;

            Long procentEverLoggedIn = amountStudents > 0 ? 100 * studentsLoggedIn.size()/amountStudents : 0;
            Long procentLoggedInLastTwoWeeks = amountStudents > 0 ? 100 * studentsLoggedInLastTwoWeeks.size()/amountStudents : 0;
            qos
                .procentEverLoggedIn(procentEverLoggedIn)
                .procentLoggedInLastTwoWeeks(procentLoggedInLastTwoWeeks);
        }

        return qos.build();
    }



    // @PreAuthorize("hasAuthority('Junior_TA') or hasAuthority('Senior_TA') or hasAuthority('Teacher')")
    // @PostMapping("/grade/group")
    public String gradeGroup(@RequestBody Json.GroupGrading usersAndAchievements) {
        final var achievementsToUnlock = usersAndAchievements.getAchievementIds()
                .stream()
                .map(id -> achievementRepository.findOrThrow(id));
        final var usersAndEnrolments = usersAndAchievements.getUserIds().stream().map(id -> users.findOrThrow(id)).map(u -> Pair.of(u, u.currentEnrolment().orElseThrow(UserErrors::enrolmentNotFound))).collect(Collectors.toList());
        final var now = LocalDateTime.now();

        achievementsToUnlock.forEach(a -> {
            usersAndEnrolments.forEach(ue -> {
                if (ue.getFirst().currentResult(a) != Result.PASS) {
                    achievementUnlockedRepository
                            .save(AchievementUnlocked
                                    .builder()
                                    .achievement(a)
                                    .enrolment(ue.getSecond())
                                    .unlockTime(now)
                                    .build());
                }
            });
        });

        return "SUCCESS";
    }

    @Autowired
    CourseRepository courseRepository;

    @GetMapping("/course")
    public @ResponseBody Course getCourse() {
        var currentCourseList = courseRepository.findAll();
        if (currentCourseList.size() != 1) throw CourseErrors.emptyOrCorrupt();
        return currentCourseList.get(0);
    }

    @PreAuthorize("hasAuthority('Teacher')")
    @PostMapping("/course")
    public @ResponseBody String postCourse(@RequestBody Json.CourseInfo request) {
        if (courseRepository.count() > 0) throw CourseErrors.alreadyExists();
        var newCourse = Course.builder()
            .name(request.getName())
            .courseWebURL(request.getCourseWebURL())
            .gitHubOrgURL(request.getGithubBaseURL())
            .startDate(LocalDate.parse(request.getStartDate()))
            .helpModule(request.isHelpModule())
            .demoModule(request.isDemoModule())
            .build();
        courseRepository.save(newCourse);
        return "SUCCESS";
    }

    @PreAuthorize("hasAuthority('Teacher')")
    @PutMapping("/course")
    public @ResponseBody String putCourse(@RequestBody Json.CourseInfo request) {
        var currentCourseList = courseRepository.findAll();
        if (currentCourseList.size() != 1) throw CourseErrors.emptyOrCorrupt();
        var currentCourse = currentCourseList.get(0);
        if (request.getName() != null) currentCourse.setName(request.getName());
        if (request.getCourseWebURL() != null) currentCourse.setCourseWebURL(request.getCourseWebURL());
        if (request.getGithubBaseURL() != null) currentCourse.setGitHubOrgURL(request.getGithubBaseURL());
        if (request.getStartDate() != null) currentCourse.setStartDate(LocalDate.parse(request.getStartDate()));
        if (request.isDemoModule() != currentCourse.isDemoModule()) currentCourse.setDemoModule(request.isDemoModule());
        if (request.isOnlyIntroductionTasks() != currentCourse.isOnlyIntroductionTasks()) currentCourse.setOnlyIntroductionTasks(request.isOnlyIntroductionTasks());
        if (request.isHelpModule() != currentCourse.isHelpModule()) currentCourse.setHelpModule(request.isHelpModule());
        if (request.isExamMode() != currentCourse.isExamMode()) currentCourse.setExamMode(request.isExamMode());

        //nya
        if (request.isBurndownModule() != currentCourse.isBurndownModule()) currentCourse.setBurndownModule(request.isBurndownModule());
        if (request.isStatisticsModule() != currentCourse.isStatisticsModule()) currentCourse.setStatisticsModule(request.isStatisticsModule());

        if (request.getRoomSetting() != null) currentCourse.setRoomSetting(request.getRoomSetting());
        if (request.isClearQueuesUsingCron() != currentCourse.isClearQueuesUsingCron()) currentCourse.setClearQueuesUsingCron(request.isClearQueuesUsingCron());
        if (request.isProfilePictures() != currentCourse.isProfilePictures()) currentCourse.setProfilePictures(request.isProfilePictures());

        courseRepository.save(currentCourse);
        webSocketController.notifyNewCourse();
        return "SUCCESS";
    }

    @GetMapping("/admin/resetCodeExamBlocker")
    public @ResponseBody String resetCodeExamBlocker() {
        courseRepository.currentCourseInstance().setCodeExamDemonstrationBlocker(LocalDate.now());
        courseRepository.save(courseRepository.currentCourseInstance());
        return "Ignoring all failed code exam demonstration attempts earlier than " + LocalDate.now() + "\n";
    }

    @PreAuthorize("hasAuthority('Senior_TA') or hasAuthority('Teacher')")
    @PostMapping("/grade/group")
    public @ResponseBody String gradeGroup(@RequestBody Json.GroupGradingCurl request) {
        try {
            return gradeGroup(Json.GroupGrading.builder()
                    .userIds(List.of(users.findByUserNameOrThrow(request.getUsername()).getId()))
                    .achievementIds(request.getAchievements().stream().map(s -> achievementRepository.findByCode(s).orElseThrow().getId()).collect(Collectors.toList()))
                    .build());
        } catch (NoSuchElementException e) {
            return "User or achievement not found";
        } catch (Exception e) {
            e.printStackTrace();
            return "Something went wrong in group grading";
        }
    }

    @PreAuthorize("hasAuthority('Senior_TA') or hasAuthority('Teacher')")
    @PostMapping("/grade/group_users")
    public @ResponseBody String gradeGroup(@RequestBody Json.GroupGradingUsers request) {
        try {
            return gradeGroup(Json.GroupGrading.builder()
                    .userIds(request.getUserIds().stream().map(s -> users.findOrThrow(s).getId()).collect(Collectors.toList()))
                    .achievementIds(request.getAchievements().stream().map(s -> achievementRepository.findByCode(s).orElseThrow().getId()).collect(Collectors.toList()))
                    .build());
        } catch (NoSuchElementException e) {
            return "User or achievement not found";
        } catch (Exception e) {
            e.printStackTrace();
            return "Something went wrong in group grading";
        }
    }

    @PreAuthorize("hasAuthority('Junior_TA') or hasAuthority('Senior_TA') or hasAuthority('Teacher')")
    @GetMapping("/explore/student/{id}")
    public Json.StudentExplorer exploreStudent(@PathVariable Long id) {
        var user = users.findOrThrow(id);
        return Json.StudentExplorer
                .builder()
                .user(user)
                .courseInstance(user.currentCourseInstance().get())
                .unlocked(user.achievementsUnlocked().stream().sorted(Comparator.comparing(Achievement::getCode)).collect(Collectors.toList()))
                .build();
    }

    @PreAuthorize("hasAuthority('Junior_TA') or hasAuthority('Senior_TA') or hasAuthority('Teacher')")
    @PostMapping("/explore/velocity")
    public String exploreVelocity(@RequestBody Json.ExploreVelocity input) {
        return "\"Currently not implemented\"";
    }

    @Value("${server.profile.dir}")
    String profilePictureLocation;
//                        f.renameTo(new File(f.getAbsoluteFile() + ".revoked-at." + now.toString()));

    @PreAuthorize("hasAuthority('Teacher')")
    @DeleteMapping("/user/revoke-profile-pic/{id}")
    public void revokeProfilePic(@PathVariable Long id) {
        users.findById(id).ifPresent(u -> {
            try {
                Optional.ofNullable(u.getProfilePic()).ifPresent(p -> {
                    final var f = new File(p);
                    if (f.exists()) f.delete();
                    u.setProfilePic(null);
                    u.setVerifiedProfilePic(false);
                    u.setUserApprovedThumbnail(false);
                    users.save(u);
                });
                Optional.ofNullable(u.getProfilePicThumbnail()).ifPresent(p -> {
                    final var f = new File(p);
                    if (f.exists()) f.delete();
                    u.setProfilePicThumbnail(null);
                    u.setUserApprovedThumbnail(false);
                    users.save(u);
                });
            } catch (Exception e) {
                e.printStackTrace(System.err);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong");
            }
        });
    }

    @PostMapping("/user/upload-profile-pic")
    public void uploadProfilePic(@RequestParam("file") MultipartFile file) {
        users.currentUser2().ifPresent(u -> {
            if (u.getProfilePic() == null || !u.isUserApprovedThumbnail()) {
                try {
                    var now = LocalDateTime.now();
                    final var targetFile = new File(profilePictureLocation
                            + u.getUserName()
                            + '.'
                            + now.getYear() // Current year
                            + '-'
                            + now.getMonthValue() // Current month
                            + '-'
                            + now.getDayOfMonth() // Current day
                            + '@'
                            + now.getHour()
                            + ':'
                            + now.getMinute()
                            + ':'
                            + now.getSecond()
                            + file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.'))); /// Extension

                    file.transferTo(targetFile);
                    u.setProfilePic(targetFile.getAbsolutePath());
                    users.save(u);

                    imagesToScale.offer(Pair.of(u.getId(), targetFile));

                } catch (Exception e) {
                    e.printStackTrace(System.err);
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong -- maybe a bad file name?", e);
                }
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User profile already set");
            }
        });
    }

    private static final ConcurrentReferenceHashMap<Long, byte[]> profilePicCache = new ConcurrentReferenceHashMap<>();

    private static byte[] readImageFile(String path, Long id) {
        var cachedProfilePic = Optional.ofNullable(profilePicCache.get(id));

        if (cachedProfilePic.isPresent()) {
            return cachedProfilePic.get();
        } else {
            try(var bin = new BufferedInputStream(new FileInputStream(path))) {
                var result = bin.readAllBytes();
                profilePicCache.put(id, result);
                bin.close();
                return result;

            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
        }
    }

    @PreAuthorize("hasAuthority('Junior_TA') or hasAuthority('Senior_TA') or hasAuthority('Teacher')")
    @PutMapping("/user/profile-pic/{id}/verified")
    public void setProfilePicVerified(@PathVariable Long id) {
        var u = users.findOrThrow(id);

        u.setVerifiedProfilePic(!u.getNeedsProfilePic()); /// Ensures we do not verify non-existing picture
        users.save(u);
    }

    @GetMapping("/user/profile-pic/{id}/verified")
    public boolean isProfilePicVerified(@PathVariable Long id) {
        return users.findOrThrow(id).isVerifiedProfilePic();
    }

    @GetMapping("/user/profile-pic/{id}")
    public ResponseEntity<byte[]> profilePic(@PathVariable Long id) {
        var userInPicture =  users.findOrThrow(id);
        var currentUser = users.currentUser();

        if (userInPicture.getProfilePicThumbnail() == null) {
            return ResponseEntity.ok().body(new byte[0]);
        } else if ((currentUser.isTeacher() || userInPicture.isTeacher() || userInPicture.equals(currentUser))) {
            return  ResponseEntity.ok()
                        //.cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES)) NB! Need cache invalidation!
                        .body(readImageFile(userInPicture.getThumbnail(), id));
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }

    @PreAuthorize("hasAuthority('Junior_TA') or hasAuthority('Senior_TA') or hasAuthority('Teacher')")
    @GetMapping("/explore/achievement/{achievementId}")
    public Json.AchievementExplorer exploreAchievement(@PathVariable Long achievementId) {
        var unlocked = new ArrayList<User>();
        var remaining = new ArrayList<User>();
        var struggling = new ArrayList<User>();

        var enrolmentsWithAchievementUnlocked =
                achievementUnlockedRepository
                        .findAllByAchievementId(achievementId)
                        .stream().map(AchievementUnlocked::getEnrolment)
                        .collect(Collectors.toSet());

        var enrolmentsWithAchievementPushedBack =
                achievementPushedBackRepository
                        .findAllByAchievementId(achievementId)
                        .stream().map(AchievementPushedBack::getEnrolment)
                        .collect(Collectors.toSet());

        // Go through all users -- if the user's current enrolment is in
        // enrolmentsWithAchievementUnlocked, add to unlocked; if not, then
        // add to remaining -- and if also in enrolmentsWithAchievementPushedBack.
        // add to struggling
        users.findAll()
                .stream()
                .map(u -> Pair.of(u, u.currentEnrolment()))
                .filter(p -> p.getSecond().isPresent() && !p.getFirst().isPreviouslyEnrolled())
                .forEach(p -> {
                    var user = p.getFirst();
                    var enrolment = p.getSecond().get();

                    if (enrolmentsWithAchievementUnlocked.contains(enrolment)) {
                        unlocked.add(user);
                    } else {
                        remaining.add(user);

                        /// TODO: maybe have a higher threshold for this?
                        if (enrolmentsWithAchievementPushedBack.contains(enrolment)) {
                            struggling.add(user);
                        }
                    }
                });

        return Json.AchievementExplorer
                .builder()
                .unlocked(unlocked)
                .remaining(remaining)
                .struggling(struggling)
                .build();
    }

    @PreAuthorize("hasAuthority('Junior_TA') or hasAuthority('Senior_TA') or hasAuthority('Teacher')")
    @GetMapping("/explore/progress")
    public Json.ProgressExplorer exploreProgress() {
        var as = achievementRepository.findAll().stream().sorted(Comparator.comparing(Achievement::getCode)).collect(Collectors.toList());

        return Json.ProgressExplorer
                .builder()
                .achievements(as)
                .userProgress(users
                        .findAllCurrentlyEnrolledStudents()
                        .stream()
                        .sorted(Comparator.comparing(User::getLastName))
                        .map(u -> Json.UserProgress
                                .builder()
                                .progress(u.progress(as))
                                .user(u)
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    @PreAuthorize("hasAuthority('Junior_TA') or hasAuthority('Senior_TA') or hasAuthority('Teacher')")
    @GetMapping("/recent/demo")
    public List<Demonstration> recentDemo() {
        var demonstrations = demonstrationRepository
                .findAllByExaminer(users.currentUser())
                .stream()
                .sorted(Comparator.comparing(Demonstration::getRequestTime))
                .collect(Collectors.toList());

        var requests = demonstrations.size();
        var list = demonstrations.subList((requests < 5) ? 0 : (requests - 5), requests);
        Collections.reverse(list);
        return SquigglyUtils.listify(Squiggly.init(AUPortal.OBJECT_MAPPER, DemonstrationController.filterStudent), list, Demonstration.class);
    }

    @GetMapping("/recent/help")
    public List<HelpRequest> recentHelp() {
        var user = users.currentUser();
        if (user.isTeacher() || user.isCanClaimHelpRequests()) {
            var helpRequests = helpRequestRepository
            .findAllByHelper(users.currentUser())
            .stream()
            .sorted(Comparator.comparing(HelpRequest::getRequestTime))
            .collect(Collectors.toList());

            var requests = helpRequests.size();
            var list = helpRequests.subList((requests < 5) ? 0 : (requests - 5), requests);
            Collections.reverse(list);
            return SquigglyUtils.listify(Squiggly.init(AUPortal.OBJECT_MAPPER, HelpRequestController.filterStudent), list, HelpRequest.class);
        } else {
            throw AuthErrors.insufficientPrivileges();
        }
    }

    @GetMapping("/recent/student/help")
    public List<HelpRequest> recentStudentHelp() {
        var user = users.currentUser();
        if (user.isStudent() || user.isTeacher()) {
            var helpRequests = helpRequestRepository
            .findAllBySubmitters(users.currentUser())
            .stream()
            .sorted(Comparator.comparing(HelpRequest::getRequestTime))
            .collect(Collectors.toList());

            var requests = helpRequests.size();
            var list = helpRequests.subList((requests < 5) ? 0 : (requests - 5), requests);
            Collections.reverse(list);
            return SquigglyUtils.listify(Squiggly.init(AUPortal.OBJECT_MAPPER, HelpRequestController.filterStudent), list, HelpRequest.class);
        } else {
            throw CourseErrors.genericError();
        }
    }

    @GetMapping("/recent/student/demo")
    public List<Demonstration> recentStudentDemo() {
        var user = users.currentUser();
        if (user.isStudent() || user.isTeacher()) {
            var demonstrations = demonstrationRepository
                    .findAllBySubmitters(users.currentUser())
                    .stream()
                    .sorted(Comparator.comparing(Demonstration::getRequestTime))
                    .collect(Collectors.toList());

            var requests = demonstrations.size();
            var list = demonstrations.subList((requests < 5) ? 0 : (requests - 5), requests);
            Collections.reverse(list);
            return SquigglyUtils.listify(Squiggly.init(AUPortal.OBJECT_MAPPER, DemonstrationController.filterStudent), list, Demonstration.class);
        } else {
            throw CourseErrors.genericError();
        }
    }


    @PreAuthorize("hasAuthority('Teacher')")
    @ResponseBody
    @RequestMapping(value = "/report/finished", method = RequestMethod.GET, produces = MediaType.APPLICATION_PDF_VALUE)
    public byte[] finished(HttpServletRequest request , HttpServletResponse response) {
        final var achievements = achievementRepository.findAll();

        var results = users.findAllStudents().stream()
            .map(s -> Pair.of(s, s.getGradeAndDate(achievements)))
            .filter(p -> p.getSecond().isPresent())
            .map(p -> Json.Grade.builder()
                        .firstName(p.getFirst().getFirstName())
                        .lastName(p.getFirst().getLastName())
                        .username(p.getFirst().getUserName())
                        .grade(p.getSecond().orElseThrow().getFirst())
                        .completionDate(p.getSecond().orElseThrow().getSecond())
                        .build())
            .sorted((u1, u2) -> (u1.getLastName()).compareTo(u2.getLastName()))
            .collect(Collectors.toList());

        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(output);
            PdfDocument pdfDoc = new PdfDocument(writer);
            pdfDoc.addNewPage();
            Document document = new Document(pdfDoc);

            document.add(new Paragraph("Results for " + courseRepository.findAll().get(0).getName() + " with course start date at " + courseRepository.findAll().get(0).getStartDate() + " generated at " + LocalDate.now().toString()));

            float [] pointColumnWidths = {400F, 120F, 120F, 120F};
            Table table = new Table(pointColumnWidths);

            String[] tableHeaders = {"Name", "Username", "Grade", "Completion Date"};
            for(var h : tableHeaders) {
                Cell cell = new Cell();
                cell.add(new Paragraph(h));
                cell.addStyle(new Style().setBold());
                table.addHeaderCell(cell);
            }
            for(var r : results) {
                Cell cell = new Cell();
                cell.add(new Paragraph(r.getFirstName() + " " + r.getLastName()));
                table.addCell(cell);
                cell = new Cell();
                cell.add(new Paragraph(r.getUsername()));
                table.addCell(cell);
                cell = new Cell();
                cell.add(new Paragraph(r.getGrade().toString()));
                table.addCell(cell);
                cell = new Cell();
                cell.add(new Paragraph(r.getCompletionDate().toString()));
                table.addCell(cell);
            }

            document.add(table);
            document.close();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            response.setHeader("Content-Disposition", "attachment; filename=" + "grades_" + LocalDate.now() + ".pdf");

            return output.toByteArray();

            //return output;
        } catch (Exception e) {
            throw CourseErrors.genericError();
        }
    }

    @PreAuthorize("hasAuthority('Teacher')")
    @GetMapping("/report/partial/hp")
    public List<Json.AcademicPartialResults> partialHp() {
        final var achievements = achievementRepository.findAll();

        return users.findAllStudents().stream()
            .map(s -> Pair.of(s, s.getHP(s.filterPassedAchievements(achievements))))
            .filter(p -> p.getSecond().isPresent())
            .map(p -> Json.AcademicPartialResults.builder()
                        .firstName(p.getFirst().getFirstName())
                        .lastName(p.getFirst().getLastName())
                        .username(p.getFirst().getUserName())
                        .email(p.getFirst().getEmail())
                        .credits(p.getSecond().get())
                        .build())
        .collect(Collectors.toList());
    }

    @PreAuthorize("hasAuthority('Teacher')")
    @ResponseBody
    @RequestMapping(value = "/report/partial/hp/pdf", method = RequestMethod.GET, produces = MediaType.APPLICATION_PDF_VALUE)
    public byte[] partialHpPDF(HttpServletRequest request , HttpServletResponse response) {
        final var partialHPCredits = partialHp();
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(output);
            PdfDocument pdfDoc = new PdfDocument(writer);
            pdfDoc.addNewPage();
            Document document = new Document(pdfDoc);

            document.add(new Paragraph("Partial HP credits for " + courseRepository.findAll().get(0).getName() + " with course start date at " + courseRepository.findAll().get(0).getStartDate() + " generated at " + LocalDate.now().toString()));

            float [] pointColumnWidths = {400F, 120F, 120F};
            Table table = new Table(pointColumnWidths);

            String[] tableHeaders = {"Name", "Username", "HP code"};
            for(var h : tableHeaders) {
                Cell cell = new Cell();
                cell.add(new Paragraph(h));
                cell.addStyle(new Style().setBold());
                table.addHeaderCell(cell);
            }
            for(var r : partialHPCredits) {
                Cell cell = new Cell();
                cell.add(new Paragraph(r.getFirstName() + " " + r.getLastName()));
                table.addCell(cell);
                cell = new Cell();
                cell.add(new Paragraph(r.getUsername()));
                table.addCell(cell);
                cell = new Cell();
                cell.add(new Paragraph(r.getCredits().toString()));
                table.addCell(cell);
            }

            document.add(table);
            document.close();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            response.setHeader("Content-Disposition", "attachment; filename=" + "hp_credits_" + LocalDate.now() + ".pdf");

            return output.toByteArray();
        } catch (Exception e) {
            throw CourseErrors.genericError();
        }
    }


    @PreAuthorize("hasAuthority('Teacher')")
    @GetMapping("/report/partial")
    public List<Json.PartialResults> partial() {
        final var achievements = achievementRepository.findAll();

        return users.findAllStudents().stream()
            .map(s -> Pair.of(s, s.getGradeAndDate(achievements)))
            .filter(p -> p.getSecond().isEmpty())
            //.filter(p -> ChronoUnit.YEARS.between(p.getFirst().getLastLogin(), LocalDateTime.now()) < 2) fix later!
            .map(p -> Json.PartialResults.builder()
                        .firstName(p.getFirst().getFirstName())
                        .lastName(p.getFirst().getLastName())
                        .username(p.getFirst().getUserName())
                        .email(p.getFirst().getEmail())
                        .lastLogin(p.getFirst().getLastLogin() != null ? p.getFirst().getLastLogin().toLocalDate() : null)
                        .passedAchievements(p.getFirst().passedAchievements(achievements))
                        .exportDate(LocalDate.now())
                        .build())
            .collect(Collectors.toList());
    }

    @PreAuthorize("hasAuthority('Teacher')")
    @PostMapping("/import/partial")
    public void importPartial(@RequestParam("file") MultipartFile file) throws IOException {
        String content = new String(file.getBytes(), "UTF-8");
        JsonArray jArr = JsonParser.parseString(content).getAsJsonArray();
        for (var e : jArr) {
            JsonObject j = e.getAsJsonObject();
            if (j.get("lastLogin").isJsonNull()) continue;

            var u = User.builder()
                        .firstName(j.get("firstName").getAsString())
                        .lastName(j.get("lastName").getAsString())
                        .userName(j.get("username").getAsString())
                        .role(Role.STUDENT)
                        .enrolments(new HashSet<>())
                        .email(j.get("email").getAsString())
		        .lastLogin(null)
                        .build();
            var autoEnroll = Enrolment.builder().courseInstance(courseRepository.currentCourseInstance()).achievementsUnlocked(new HashSet<>()).achievementsPushedBack(new HashSet<>()).build();
            u.getEnrolments().add(autoEnroll);

            var courseStartTime = courseRepository.findAll().get(0).getStartDate();

            j.get("passedAchievements").getAsJsonArray()
                .forEach(codeJson -> {
                    var code = codeJson.getAsString();
                    if (code.length() == 0) return;
                    var a = achievementRepository.findByCode(code).orElseThrow();
                    var ae = AchievementUnlocked.builder()
                            .enrolment(autoEnroll)
                            .achievement(a)
                            .unlockTime(LocalDateTime.of(courseStartTime.getYear(),
                                                        courseStartTime.getMonth(),
                                                        courseStartTime.getDayOfMonth(),
                                                        0,
                                                        0))
                            .build();
                    autoEnroll.getAchievementsUnlocked().add(ae);
                });
            try {
                users.save(u);
            } catch (Exception execp) {
                // User already exists
                continue;
            }
            enrolmentRepository.save(autoEnroll);
        }
    }

    @PostMapping("/webhook/github/accept")
    public void webhookGithubAccept (@RequestHeader Map<String, String> headers, @RequestBody Json.WebhookGithubAccept body) {
	if (body.getMembership() == null) return;
	String gitHubHandle = body.getMembership().getUser().getLogin();
        Optional<User> u = users.findByGitHubHandle(gitHubHandle);

        if (u.isPresent()) {
                UserController.scheduleUserForRepoCreation(u.get());
                System.out.println("Scheduling repo creation for user " + u.get().getFirstName() + " " + u.get().getLastName());
        } else {
	    System.out.println("Error: no user found with GitHub handle '" + gitHubHandle + "'!");
        }
    }

}
