package com.uu.au.models;

import com.uu.au.enums.AcademicCreditType;
import com.uu.au.enums.AchievementType;
import com.uu.au.enums.Level;
import com.uu.au.enums.Result;
import com.uu.au.enums.SortKey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Json {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Status {
        private String message;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class Target {
        private Level target;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NameId {
        private String firstName;
        private String lastName;
        private Long id;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Stats {
        private int currentCourseWeek;
        private int weeksNeeded;
        private int remaining;
        private double remainingWeeks;
        private int currentVelocity;
        private double averageVelocity;
        private double targetVelocity;
        private int currentTarget;
        private Map<Level, List<Integer>> burnDown;
        private Map<Level, Integer> achievementsPerLevel;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Grade {
        private String firstName;
        private String lastName;
        private String username;
        private Level grade;
        private LocalDate completionDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PartialResults {
        private String firstName;
        private String lastName;
        private String username;
        private String email;
        private List<String> passedAchievements;
        private LocalDate exportDate;
        private LocalDate lastLogin;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AcademicPartialResults {
        private String firstName;
        private String lastName;
        private String username;
        private String email;
        private List<AcademicCreditType> credits;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Burndown {
        private Map<Level, List<Integer>> burnDown;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentExplorer {
        private User user;
        private Course courseInstance;
        private List<Achievement> unlocked;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AchievementExplorer {
        private List<User> unlocked;
        private List<User> remaining;
        private List<User> struggling;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProgressExplorer {
        private List<Achievement> achievements;
        private List<UserProgress> userProgress;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserProgress {
        private User user;
        private List<Result> progress;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HelpRequestId {
        private Long helpRequestId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DemonstrationRequest {
        private List<Long> achievementIds;
        private List<Long> ids;
        private String zoomPassword;
        private String physicalRoom;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FirstNameLastNameUserName {
        private String firstName;
        private String lastName;
        private String userName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateUser {
        private String firstName;
        private String lastName;
        private String userName;
        private String email;
        private String role;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HelpRequest {
        private List<Long> ids;
        private String message;
        private String zoomPassword;
        private String physicalRoom;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Progress {
        private LocalDateTime date;
        private Achievement achievement;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BasicData {
        private User user;
        private Boolean completeSetup;
        private Course course;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExploreVelocity {
        private Long velocity;
        private SortKey sortBy;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupGrading {
        private List<Long> userIds;
        private List<Long> achievementIds;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupGradingCurl {
        private String username;
        private List<String> achievements;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupGradingUsers {
        private List<Long> userIds;
        private List<String> achievements;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DemoResult {
        private Long demoId;
        private List<AchievementId_UserId_Result> results;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AchievementId_UserId_Result {
        private Long achievementId;
        private Long id;
        private Result result;
    }

    @Data
    @Builder
    public static class QOSResult {
        private long helpRequestsPending;
        private long helpRequestsPickupTime;
        private long helpRequestsRoundtripTime;
        private long demonstrationsPending;
        private long demonstrationsPickupTime;
        private long demonstrationsRoundtripTime;
        private Long procentEverLoggedIn;
        private Long procentLoggedInLastTwoWeeks;
    }

    @Data
    @Builder
    public static class CourseInfo {
        private String name;
        private String courseWebURL;
        private String codeSpaceBaseURL;
        private String githubBaseURL;
        private String startDate;
        private boolean helpModule;
        private boolean demoModule;
        private boolean statisticsModule;
        private boolean burndownModule;
        private boolean examMode;
        private boolean onlyIntroductionTasks;
        private String roomSetting;
        private boolean clearQueuesUsingCron;
        private boolean profilePictures;
    }

    @Data
    @Builder
    public static class LadokEntryInfo {
        private User user;
        private Set<LadokEntry> ladokEntries;
        private boolean reported;
    }

    @Data
    @Builder
    public static class FinalGradeInfo {
        private User user;
        private Level finalGrade;
    }

    public static class DecoratedAchievement {
        public DecoratedAchievement(Achievement achievement, Set<Achievement> currentPushBacks, Set<Achievement> unlocked, boolean blocked) {
            this.id = achievement.getId();
            this.code = achievement.getCode();
            this.name = achievement.getName();
            this.urlToDescription = achievement.getUrlToDescription();
            this.achievementType = achievement.getAchievementType();
            this.level = achievement.getLevel();
            this.isClassicalAchievement = true;
            this.currentlyPushedBack = currentPushBacks.contains(achievement) || blocked;
            this.unlocked = unlocked.contains(achievement);
            this.blocked = blocked;
        }

        public final Long id;
        public final String code;
        public final String name;
        public final String urlToDescription;
        public final AchievementType achievementType;
        public final Level level;
        public final boolean isClassicalAchievement;
        public final boolean currentlyPushedBack;
        public final boolean unlocked;
        public final boolean blocked;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebhookGithubAccept {
        private String action;
        private GithubMembership membership;
    }
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GithubMembership {
        private String state;
        private String role;
        private GithubUser user;
    }
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GithubUser {
        private String login;
    }
}
