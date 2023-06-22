package com.uu.au.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uu.au.enums.AcademicCreditType;
import com.uu.au.enums.AchievementType;
import com.uu.au.enums.Level;
import com.uu.au.enums.Result;
import com.uu.au.enums.Role;

import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.util.Pair;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of={"id"})
public class User {
    @Id
    @GeneratedValue
    private Long id;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    @Column(unique = true)
    private String userName;

    @NotBlank
    private String email;

    @NotNull
    private Role role;

    private String gitHubHandle;

    @Builder.Default
    private boolean gitHubFlowSuccessful = false;

    public boolean getNeedsGitHubHandle() {
        return gitHubHandle == null;
    }

    public Optional<String> getGitHubRepoURL() {
        return currentCourseInstance().map(c -> c.getGitHubOrgURL() + emailPrefix());
    }

    private String zoomRoom;

    @JsonIgnore
    private String profilePic;

    @JsonIgnore
    private String profilePicThumbnail;

    @JsonIgnore
    @Builder.Default
    private boolean userApprovedThumbnail = false;

    @Builder.Default
    private boolean verifiedProfilePic = false;

    @Builder.Default
    private boolean canClaimHelpRequests = false; // FIXME Should move to own table

    @JsonIgnore
    @NotNull
    @Builder.Default
    private boolean previouslyEnrolled = false;

    @UpdateTimestamp
    @Column(name="updatedDateTime", columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedDateTime;

    private LocalDateTime lastLogin;

    private LocalDate deadline;

    public boolean getNeedsZoomLink() {
        return zoomRoom == null;
    }

    public boolean getNeedsProfilePic() {
        return profilePic == null;
    }

    @JsonIgnore
    public String getThumbnail() {
        return profilePicThumbnail != null
                ? profilePicThumbnail
                : profilePic;
    }

    @JsonIgnore
    @OneToMany(cascade=CascadeType.ALL, orphanRemoval=true, fetch = FetchType.EAGER)
    private Set<Enrolment> enrolments;

    @JsonIgnore
    public boolean isPriviliged() {
        return role.equals(Role.TEACHER) || role.equals(Role.SENIOR_TA);
    }

    public String emailAddress() {
        return isPriviliged()
                ? firstName + "." + lastName + "@it.uu.se"
                : firstName + "." + lastName + "." + userName.substring(4) + "@student.uu.se";
    }

    @JsonIgnore
    public Optional<Enrolment> lastEnrolment() {
        if (enrolments == null) return Optional.empty();
        return enrolments
                .stream()
                .max(Comparator.comparing(Enrolment::getYear));
    }

    @JsonIgnore
    public Optional<Enrolment> currentEnrolment() {
        var lastEnrolment = lastEnrolment();
        return lastEnrolment.isPresent() // FIXME
                ? lastEnrolment
                : Optional.empty();
    }

    @JsonIgnore
    public Optional<Course> currentCourseInstance() {
        return currentEnrolment().map(Enrolment::getCourseInstance);
    }

    @JsonIgnore
    public Set<Achievement> achievementsUnlocked() {
        return currentEnrolment()
                .map(enrolment -> enrolment.getAchievementsUnlocked().stream().map(AchievementUnlocked::getAchievement).collect(Collectors.toSet()))
                .orElseGet(HashSet::new);
    }


    @JsonIgnore
    public Set<Achievement> achievementsPushedBack() {
        return currentEnrolment()
                .map(enrolment -> enrolment.getAchievementsPushedBack().stream().filter(AchievementPushedBack::isActive).map(AchievementPushedBack::getAchievement).collect(Collectors.toSet()))
                .orElseGet(HashSet::new);
    }

    @JsonIgnore
    public boolean isStudent() {
        return role == Role.STUDENT;
    }

    @JsonIgnore
    public boolean isJuniorTA() {
        return role == Role.JUNIOR_TA;
    }

    @JsonIgnore
    public boolean isTeacher() {
        return !isStudent();
    }

    @JsonIgnore
    public boolean isSeniorTAOrTeacher() {
        return role == Role.TEACHER || role == Role.SENIOR_TA;
    }

    @JsonIgnore
    public String emailPrefix() {
        return email.substring(0, email.indexOf('@'));
    }

    @JsonIgnore
    public Result currentResult(Achievement a) {
        return achievementsUnlocked().contains(a)
                ? Result.PASS
                : achievementsPushedBack().contains(a)
                ? Result.PUSHBACK
                : Result.FAIL;
    }

    @JsonIgnore
    public List<Result> progress(List<Achievement> achievements) {
        return achievements
                .stream()
                .map(this::currentResult)
                .collect(Collectors.toList());
    }

    private boolean passedAll(List<Achievement> achievements) {
        for (Achievement a : achievements) {
            if (this.currentResult(a) != Result.PASS) return false;
        }
        return true;
    }

    private LocalDate whenDidWePassGrade(Level lvl) {
        return this.currentEnrolment().get().getAchievementsUnlocked()
            .stream()
            .filter(a -> a.getAchievement().getLevel() == lvl)
            .map(au -> au.getUnlockTime())
            .reduce((a, b) -> a.isBefore(b) ? b : a).orElseThrow().toLocalDate();
    }

    @JsonIgnore
    public Optional<Pair<Level, LocalDate>> getGradeAndDate(List<Achievement> achievements) {
        Level grade = null;
        LocalDate time = null;

        if (passedAll(achievements.stream().filter(a -> a.getLevel() == Level.GRADE_3).collect(Collectors.toList()))) {
            grade = Level.GRADE_3;
            time = whenDidWePassGrade(Level.GRADE_3);
            if (passedAll(achievements.stream().filter(a -> a.getLevel() == Level.GRADE_4).collect(Collectors.toList()))) {
                grade = Level.GRADE_4;
                time = whenDidWePassGrade(Level.GRADE_4);
                if (passedAll(achievements.stream().filter(a -> a.getLevel() == Level.GRADE_5).collect(Collectors.toList()))) {
                    grade = Level.GRADE_5;
                    time = whenDidWePassGrade(Level.GRADE_5);
                }
            }
        }

        return grade == null || time == null
            ? Optional.empty()
            : Optional.of(Pair.of(grade, time));
   }

   @JsonIgnore
   public Optional<List<AcademicCreditType>> getHP(List<Achievement> achievements) {
        ArrayList<AcademicCreditType> credits = new ArrayList<>();

        long projectAchievementsSize = 0;
        long achievementsSize = 0;
        long assignmentSize = 0;

        for (var a : achievements) {
            if (a.getAchievementType() == AchievementType.PROJECT) projectAchievementsSize++;
            else if (a.getAchievementType() == AchievementType.ACHIEVEMENT && a.getLevel() == Level.GRADE_3) achievementsSize++;
            else if (a.getAchievementType() == AchievementType.ASSIGNMENT) assignmentSize++;
        }

        if (achievementsSize >= 4 && projectAchievementsSize == 6) {
            credits.add(AcademicCreditType.PROJECT);
            achievementsSize -= 4;
        }

        if (achievementsSize >= 30 && assignmentSize >= 4) {
            credits.add(AcademicCreditType.INLUPP1);
            credits.add(AcademicCreditType.INLUPP2);
        } else if (achievementsSize >= 15 && assignmentSize >= 2) {
            credits.add(AcademicCreditType.INLUPP1);
        }

        return credits.size() == 0
            ? Optional.empty()
            : Optional.of(credits);
  }


    @JsonIgnore
    public List<String> passedAchievements(List<Achievement> achievements) {
        return achievements.stream()
            .filter(a -> this.currentResult(a) == Result.PASS)
            .map(a -> a.getCode())
            .collect(Collectors.toList());
    }

    @JsonIgnore
    public List<Achievement> filterPassedAchievements(List<Achievement> achievements) {
        return achievements.stream()
            .filter(a -> this.currentResult(a) == Result.PASS)
            .collect(Collectors.toList());
    }
}
