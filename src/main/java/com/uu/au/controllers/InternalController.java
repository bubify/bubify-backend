package com.uu.au.controllers;

import java.net.InetAddress;
import java.time.LocalDate;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.devtools.restart.Restarter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.uu.au.enums.RoleConverter;
import com.uu.au.enums.errors.AuthErrors;
import com.uu.au.enums.errors.CourseErrors;
import com.uu.au.enums.errors.UserErrors;
import com.uu.au.models.Course;
import com.uu.au.models.Enrolment;
import com.uu.au.models.Json;
import com.uu.au.models.User;
import com.uu.au.repository.CourseRepository;
import com.uu.au.repository.UserRepository;

// These may only be called from localhost
@Controller
public class InternalController {
    @Autowired
    AuthController authController;

    @Autowired
    UserController userController;

    @Autowired
    UserRepository users;

    @Autowired
    CourseRepository course;


    private final Logger logger = LoggerFactory.getLogger(DevelopmentController.class);

    // This method should not be needed but be paranoid
    private boolean isLocalhost(HttpServletRequest request) {
        return request.getRemoteAddr().equals(InetAddress.getLoopbackAddress().getHostAddress());
    }

    @CrossOrigin
    @GetMapping("/internal/restart")
    public @ResponseBody String restartGet(HttpServletRequest request) {
        if (!isLocalhost((request))) return "ERROR: Only callable from host";
        logger.info("Restarting backend");
        Restarter.getInstance().restart();
        return "OK restarting";
    }

    RoleConverter roleConverter = new RoleConverter();

    @PostMapping("/internal/user")
    public @ResponseBody String postUser(HttpServletRequest request, @RequestBody Json.CreateUser u) {
        if (!isLocalhost((request))) return "ERROR: Only callable from host";

        logger.info("Creating user from developer endpoint");
        if (course.count() == 0) throw CourseErrors.emptyOrCorrupt();

        var existing = users.findByUserName(u.getUserName());
        if (existing.isPresent()) {
            throw UserErrors.userAlreadyExists();
        }

        var currentCourse = course.currentCourseInstance();

        var role = roleConverter.convertToEntityAttribute(u.getRole());
        if (role == null) throw UserErrors.userMalformedRole();

        var user = User
                .builder()
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .email(u.getEmail())
                .userName(u.getUserName())
                .role(role)
                .enrolments(Set.of(Enrolment.builder().courseInstance(currentCourse).achievementsPushedBack(Set.of()).achievementsUnlocked(Set.of()).build()))
                .build();

        users.save(user);
        return "SUCCESS on user";
    }

    @PostMapping("/internal/course")
    public @ResponseBody String postCourse(HttpServletRequest request, @RequestBody Json.CourseInfo c) {
        if (!isLocalhost((request))) return "ERROR";

        logger.info("Creating course from developer endpoint");
        if (course.count() > 0) throw CourseErrors.alreadyExists();
        var newCourse = Course.builder()
            .name(c.getName())
            .startDate(LocalDate.now())
            .build();
        course.save(newCourse);
        return "SUCCESS on course\n";
    }
}
