package com.uu.au.controllers;

import com.uu.au.enums.AchievementType;
import com.uu.au.enums.RoleConverter;
import com.uu.au.enums.errors.AuthErrors;
import com.uu.au.enums.errors.CSVErrors;
import com.uu.au.enums.Level;
import com.uu.au.enums.Role;
import com.uu.au.enums.errors.UserErrors;
import com.uu.au.models.*;
import com.uu.au.repository.AchievementRepository;
import com.uu.au.repository.CourseRepository;
import com.uu.au.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Set;

import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


@CrossOrigin(maxAge = 3600)
@RestController
public class UserController {

    @Value("${github.oauth.clientId}")
    private String gitHubclientId;

    @Value("${github.oauth.clientSecret}")
    private String gitHubClientSecret;

    @Autowired
    private UserRepository users;

    @Autowired
    private CourseRepository courseRepository;

    private final Logger logger = LoggerFactory.getLogger(UserController.class);

    /**
     * Used for reconnection scheme to check if we still have a valid token
     */
    @GetMapping("/ping")
    @ResponseBody void ping() {

    }

    @DeleteMapping("/user/zoom-link")
    void nullifyZoomLink() {
        users.currentUser2().ifPresent(u -> {
            u.setZoomRoom(null);
            users.save(u);
        });
    }

    @GetMapping("/allStudentNamesAndIds")
    ResponseEntity<List<Json.NameId>> allStudentNamesAndIds() {
        final var currentCourseInstance = courseRepository.currentCourseInstance();

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.MINUTES))
                .body(users
                    .findAll()
                    .stream()
                    .filter(u -> u.getRole() == Role.STUDENT && u.currentCourseInstance().isPresent() && u.currentCourseInstance().get().equals(currentCourseInstance))
                    .map(u -> new Json.NameId(u.getFirstName(), u.getLastName(), u.getId()))
                    .collect(Collectors.toList()));
    }

    @GetMapping("/allNamesAndIds")
    List<Json.NameId> allNamesAndIds() {
        return users
                .findAll()
                .stream()
                .map(u -> new Json.NameId(u.getFirstName(), u.getLastName(), u.getId()))
                .collect(Collectors.toList());
    }

    @GetMapping("/admin/user")
    User adminGetUser(@RequestParam String username) {
        return users.findByUserName(username).orElse(null);
    }

    @PostMapping("/admin/add-user")
    String addStudent(@RequestBody String firstLastEmailRoleCSV) {
        return addUserCSVBased(firstLastEmailRoleCSV, true/*internal usage*/);
    }

    @PutMapping("/user")
    @ResponseBody boolean putUser(@RequestBody User updatedUser) {
        final var currentUser = users.currentUser();
        final boolean isPriviliged = currentUser.isPriviliged();
        // Only SENIOR_TA and TEACHER may update other users
        if (!isPriviliged) {
            if (!updatedUser.getId().equals(currentUser.getId())) throw AuthErrors.insufficientPrivileges();
        }

        var user = users.findById(updatedUser.getId());
        if (user.isPresent()) {
            var t = user.get();
            var f = updatedUser;

            if (f.getRole() != null && t.getRole() != f.getRole() ||
                    f.getFirstName() != null ||
                    f.getLastName() != null ||
                    f.getUserName() != null ||
                    f.getEmail() != null ||
                    f.getGitHubHandle() != null ||
                    f.isVerifiedProfilePic() != t.isVerifiedProfilePic() ||
                    f.isCanClaimHelpRequests() != t.isCanClaimHelpRequests()) {
                // Needs to be SENIOR_TA or TEACHER for this action
                if (!isPriviliged) throw AuthErrors.insufficientPrivileges();
            }

            /**
             * Needs privileged
             */
            if (f.getFirstName() != null && isPriviliged) t.setFirstName(f.getFirstName());
            if (f.getLastName() != null && isPriviliged) t.setLastName(f.getLastName());
            if (f.getUserName() != null && isPriviliged) t.setUserName(f.getUserName());
            if (f.getEmail() != null && isPriviliged) t.setEmail(f.getEmail());
            if (f.getRole() != null && isPriviliged) t.setRole(f.getRole());
            if (f.isVerifiedProfilePic() != t.isVerifiedProfilePic() && isPriviliged) t.setVerifiedProfilePic(f.isVerifiedProfilePic());
            if (f.isCanClaimHelpRequests() != t.isCanClaimHelpRequests() && isPriviliged) t.setCanClaimHelpRequests(f.isCanClaimHelpRequests());
            if ((t.getGitHubHandle() != null && f.getGitHubHandle() != null) &&
                    !f.getGitHubHandle().equalsIgnoreCase(t.getGitHubHandle())) {
                t.setGitHubHandle(f.getGitHubHandle().equals("") ? null : f.getGitHubHandle());
                t.setGitHubFlowSuccessful(false);
            }

            /**
             * Anyone can update these fields
             */
            if (f.getZoomRoom() != null) t.setZoomRoom(f.getZoomRoom().equals("") ? null : f.getZoomRoom());
            if (f.getDeadline() != null) t.setDeadline(f.getDeadline());

            users.save(t);
            webSocketController.notifyProfileChange(t, "/refresh");
            return true;

        } else {
            return false;
        }
    }

    @PostMapping("/admin/add-achievement")
    String addAchievement(@RequestBody String entryInCSV) {
        if (entryInCSV.startsWith("#")) return "Skipped comment";

        var columns = entryInCSV.split(";");
        var code = columns[0];
        var name = columns[1];
        var level = columns[2];
        var type = columns[3];
        var url = columns[4];

        var achievement = Achievement
                .builder()
                .code(code)
                .name(name)
                .level(Level.forValues(level))
                .achievementType(AchievementType.valueOf(type))
                .urlToDescription(url)
                .build();

        achievementRepository.save(achievement);

        return "Added " + achievement.getCode() + " " + achievement.getName();
    }

    @GetMapping("/admin/finger")
    User finger(@RequestParam String username) {
        var user = users.findByUserName(username);

        return user.isPresent() ? user.get() : null;
    }

    @GetMapping("/admin/github-backlog")
    @ResponseBody String githubBacklog() {
        return users
                .findAll()
                .stream()
                .filter(u -> !u.isGitHubFlowSuccessful())
                .map(u -> u.getGitHubHandle() + ";" + u.emailPrefix())
                .collect(Collectors.joining("\n"));
    }

    @Autowired
    AchievementRepository achievementRepository;

    RoleConverter roleConverter = new RoleConverter();

    /**
     *
     * @param firstLastEmailRoleCSV
     * Expects the following formatting: first;last;email;username;role
     * Example: Bird;Hansson;bird.hansson.1234@student.uu.se;STUDENT
     *
     * Valid values for role: STUDENT, JUNIOR_TA, SENIOR_TA, TEACHER
     * @return
     */
    public String addUserCSVBased(String firstLastEmailRoleCSV, boolean internal) {
        if (!internal) {
            var currentUser = users.currentUser().getRole();
            // Should already have been checked but be paranoid
            if (!(currentUser == Role.SENIOR_TA ||
                    currentUser == Role.TEACHER)) {
                throw AuthErrors.insufficientPrivileges();
            }
        }

        if (firstLastEmailRoleCSV.contains(System.lineSeparator())) {
            throw CSVErrors.csvContainsMoreThanASingleLine();
        }

        String[] columns = firstLastEmailRoleCSV.split(";");

        if (columns.length != 5) {
            throw CSVErrors.malformed();
        }

        var first = columns[0].trim();
        var last = columns[1].trim();
        var email = columns[2].trim().toLowerCase();
        var userName = columns[3].trim().toLowerCase();
        var roleStr = columns[4].trim().toUpperCase();

        if (first.length() < 2) throw UserErrors.userFirstNameShort();
        if (last.length() < 2) throw UserErrors.userLastNameShort();
        if (userName.length() == 0) throw UserErrors.userMissingUsername();
        if (roleStr.length() == 0) throw UserErrors.userMissingRole();

        var role = roleConverter.convertToEntityAttribute(roleStr);
        if (role == null) throw UserErrors.userMalformedRole();
        if (!internal) {
            var currentUser = users.currentUser().getRole();
            if (role == Role.TEACHER && currentUser != Role.TEACHER) throw AuthErrors.insufficientPrivileges();
        }

        var existing = users.findByUserName(userName);
        if (existing.isPresent()) {
            throw UserErrors.userAlreadyExists();
        }

        var currentCourse = courseRepository.currentCourseInstance();

        var user = User
                .builder()
                .firstName(first)
                .lastName(last)
                .email(email)
                .userName(userName)
                .role(role)
                .enrolments(role == Role.STUDENT
                        ? Set.of(Enrolment.builder().courseInstance(currentCourse).achievementsPushedBack(Set.of()).achievementsUnlocked(Set.of()).build())
                        : Set.of())
                .build();

        users.save(user);
        return "SUCCESS";
    }

    @Autowired
    AuthController authController;

    @Autowired
    CourseRepository course;

    @CrossOrigin
    @PostMapping("/user/approveThumbnail")
    public User approveThumbnail() {
        var currentUser = users.currentUser();
        if (currentUser.getProfilePicThumbnail() == null) {
            throw UserErrors.userMissingThumbnail();
        }
        currentUser.setUserApprovedThumbnail(true);
        users.save(currentUser);
        return currentUser;
    }

    @CrossOrigin
    @GetMapping("/setupStatus")
    public Boolean isSetupComplete() {
        var u = users.currentUser();
        var c = course.currentCourseInstance();
        var profilePictureActivated = c.isProfilePictures();
        var zoomActivated = !c.getRoomSetting().equals("PHYSICAL");

        var needProfilePic = u.getNeedsProfilePic() && profilePictureActivated;
        var needZoom = u.getNeedsZoomLink() && zoomActivated;

        return u.getRole().equals(Role.STUDENT)
            ? !(needProfilePic || needZoom)
            : !needProfilePic;
    }


    @CrossOrigin
    @GetMapping("/user")
    public User user(HttpServletRequest request, HttpServletResponse response) {
        return users.currentUser();
    }

    @CrossOrigin
    @GetMapping("/basicData")
    public Object basicData(HttpServletRequest request, HttpServletResponse response) {
        var user = users.currentUser();

        return Json.BasicData.builder()
            .user(user)
            .course(courseRepository.currentCourseInstance())
            .build();
    }

    @PreAuthorize("hasAuthority('Senior_TA') or hasAuthority('Teacher')")
    @GetMapping("/users")
    List<User> all() {
        return users.findAll();
    }

    @PreAuthorize("hasAuthority('Senior_TA') or hasAuthority('Teacher')")
    @PostMapping("/users")
    String postUser(@RequestBody Json.CreateUser u) {
        return addUserCSVBased(String.join(";", u.getFirstName(),
                u.getLastName(), u.getEmail(), u.getUserName(), u.getRole()), false /*public usage*/);
    }

    @GetMapping("/users/{id}")
    User one(@PathVariable Long id) {
        var user = users.currentUser();
        if ((user.isStudent() || user.isJuniorTA()) && user.getId() != id) {
            AuthErrors.insufficientPrivileges();
        }
        return users.findById(id)
            .orElseThrow(UserErrors::userNotFound);
    }

    @PreAuthorize("hasAuthority('Junior_TA') or hasAuthority('Senior_TA') or hasAuthority('Teacher')")
    @GetMapping("/whois/{userName}")
    User whois(@PathVariable String userName) {
        return users.findByUserName(userName)
                .orElseThrow(UserErrors::userNotFound);
    }

    @PreAuthorize("hasAuthority('Senior_TA') or hasAuthority('Teacher')")
    @PutMapping("/users/{id}")
    User replaceUser(@RequestBody User newUser, @PathVariable Long id) {
        return users.findById(id)
            .map(user -> {
                    user.setFirstName(user.getFirstName());
                    user.setLastName(user.getLastName());
                    user.setUserName(user.getUserName());
                    user.setRole(user.getRole());
                    return users.save(user);
                })
            .orElseGet(() -> {
                    throw UserErrors.userNotFound();
                });
    }

    private final RestTemplateBuilder gitHubRequests = new RestTemplateBuilder();

    // FIXME apparently WebClient is the new hotness, we should use that.
    @GetMapping("/login/oauth2/code/github")
    public RedirectView githubCallback(@RequestParam("code") String code, @RequestParam("state") String state) {
        // TODO: is this stateless and should be made a field?
        RestTemplate gitHubApi = gitHubRequests
                .setConnectTimeout(Duration.ofSeconds(30))
                .setReadTimeout(Duration.ofSeconds(30))
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        var postBody = Map.of(
                "code", code,
                "client_id", this.gitHubclientId,
                "client_secret", this.gitHubClientSecret,
                "scope", "read:user");

        ResponseEntity<String> response = gitHubApi
                .postForEntity("https://github.com/login/oauth/access_token",
                        new HttpEntity<>(postBody, headers),
                        String.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "GitHub timeout or other API error");
        }

        RedirectView redirectView = new RedirectView();

        // who cares that you may throw an exception???
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            String accessToken = root.path("access_token").asText();

            HttpHeaders authenticationHeaders = new HttpHeaders();
            authenticationHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            authenticationHeaders.set("Authorization", "token " + accessToken);

            ResponseEntity<String> userResponse = gitHubApi
                    .exchange("https://api.github.com/user",
                            HttpMethod.GET,
                            new HttpEntity<>(authenticationHeaders),
                            String.class);

            var gitHubHandle = mapper.readTree(userResponse.getBody())
                    .path("login")
                    .asText();
            var user = users.currentUser2();
            if (user.isPresent()) {
                user.get().setGitHubHandle(gitHubHandle);

                gitHubInviteBacklog.offer(user.get());
                users.save(user.get());

                redirectView.setUrl("/api/autoclose");

                webSocketController.notifyProfileChange(user.get(), "/githubQueued");
            } else {
                redirectView.setUrl("/github-error");

            }
        } catch (JsonProcessingException e) {
            // FIXME this is not the actual error!
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "GitHub timeout or other API error", e);
        }

        return redirectView;
    }

    @Autowired
    private WebSocketController webSocketController;

    @GetMapping("/authenticate-github")
    public RedirectView githubUsername(@RequestParam("state") String state) {

        RedirectView redirectView = new RedirectView();

        var id = users.currentUser().getId();
        var key = authController.keyFromUserIdWithAudience(id, "GitHubRequestFIXME"); // FIXME: add validation in callback

        redirectView.setUrl("https://github.com/login/oauth/authorize?client_id=60ccbeca3ab46b3d1699&state=" + key);
        return redirectView;
    }

    private static final ConcurrentLinkedDeque<User> gitHubInviteBacklog = new ConcurrentLinkedDeque<>();
    private static final ConcurrentLinkedDeque<User> gitHubCreateRepoBacklog = new ConcurrentLinkedDeque<>();

    public static void scheduleUserForRepoCreation(User u) {
        gitHubCreateRepoBacklog.add(u);
    }

    @Scheduled(fixedDelay = 60000)
    public void runGitHubFlowInvite() {
        logger.info("gitHubInviteBacklog size: " + gitHubInviteBacklog.size());
        /// Send out invitations
        while (!gitHubInviteBacklog.isEmpty()) {
            var user = gitHubInviteBacklog.poll();

            try {
                Process process = new ProcessBuilder("sh", "-c", "./github_invite.sh " + user.getGitHubHandle() + " " + user.emailPrefix())
                        .redirectErrorStream(true)
                        .directory(new File(System.getProperty("user.home")))
                        .start();

                process.waitFor(15, TimeUnit.SECONDS);
                process.destroy();
                process.waitFor();

                webSocketController.notifyProfileChange(user, "/refresh");
            } catch (IOException | InterruptedException e) {
                /// Do nothing for now
            }
        }
    }

    @Scheduled(fixedDelay = 60000)
    public void runGitHubFlowProcess() {
        logger.info("gitHubCreateRepoBacklog size: " + gitHubCreateRepoBacklog.size());
        /// Create repos for accepted invitations
        while (!gitHubCreateRepoBacklog.isEmpty()) {
            var user = gitHubCreateRepoBacklog.poll();

            try {
		System.out.println("Creating repo for " + user.getFirstName() + " " + user.getLastName());
                Process process = new ProcessBuilder("sh", "-c", "./github_invite_accepted_actions.sh " + user.getGitHubHandle() + " " + user.emailPrefix())
                        .redirectErrorStream(true)
                        .directory(new File(System.getProperty("user.home")))
                        .start();

                process.waitFor(15, TimeUnit.SECONDS);
                process.destroy();
                process.waitFor();

                user.setGitHubFlowSuccessful(true);
                users.save(user);
		webSocketController.notifyProfileChange(user, "/refresh");
            } catch (IOException | InterruptedException e) {
                /// Do nothing for now
            }
        }
    }
}
