package com.uu.au.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uu.au.enums.Level;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.uu.au.enums.Level.*;
import static com.uu.au.enums.Level.GRADE_5;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseInstance {
    @Id
    @GeneratedValue
    @JsonIgnore
    private Long id;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @OneToMany(mappedBy="enrolment", cascade=CascadeType.ALL)
    private Set<AchievementUnlocked> achievementsUnlocked;

    @OneToMany(mappedBy="enrolment", cascade=CascadeType.ALL)
    private Set<AchievementPushedBack> achievementsPushedBack;

    @JsonIgnore
    public boolean isUnlocked(Achievement a) {
        return achievementsUnlocked.stream().anyMatch(au -> au.getAchievement().equals(a));
    }

    @JsonIgnore
    public boolean thisYear() {
        final var currentYear = Calendar.getInstance().get(Calendar.YEAR);
        final var lastYear = currentYear - 1;
        return getYear() == currentYear || getYear() == lastYear; // FIXME: update to work on periods or similar
    }

    @JsonIgnore
    public Integer getYear() {
        return startDate.getYear();
    }

    public Map<Level, List<Integer>> burnUp() {
        final var weeks = currentCourseWeek() + 1;
        final var burnUp = Map.of(GRADE_3, getNEs(weeks, 0), GRADE_4, getNEs(weeks, 0), GRADE_5, getNEs(weeks, 0));

        final var firstDayOfCourse = getStartDate();
        achievementsUnlocked.stream().sorted(Comparator.comparing(AchievementUnlocked::getUnlockTime)).collect(Collectors.toSet()).forEach(au -> {
            /// var unlockWeek = (au.getUnlockTime().getDayOfYear() - firstDayOfCourse) / 7; /// FIXME: left as original documentation
            int unlockWeek = (int) ChronoUnit.WEEKS.between(firstDayOfCourse, au.getUnlockTime());

            switch (au.getAchievement().getLevel()) {
                case GRADE_3: incrementNthPos(burnUp.get(GRADE_3), unlockWeek);
                case GRADE_4: incrementNthPos(burnUp.get(GRADE_4), unlockWeek);
                case GRADE_5: incrementNthPos(burnUp.get(GRADE_5), unlockWeek);
            }
        });

        burnUp.values().forEach(b -> {
            for (int i = 1; i < b.size(); ++i) {
                b.set(i, b.get(i) + b.get(i - 1));
            }
        });

        return burnUp;
    }

    /// note: indexed from 0
    public int currentCourseWeek() {
        return (int) ChronoUnit.WEEKS.between(startDate, LocalDate.now());
    }

    private static void incrementNthPos(List<Integer> is, int n) {
        is.set(n, is.get(n) + 1);
    }

    private static <V> List<V> getNEs(int n, V e) {
        List<V> result = new ArrayList<>(n);
        for (int i = 0; i < n; ++i) {
            result.add(e);
        }
        return result;
    }

    public Map<Level, List<Integer>> burnDown(Map<Level, Integer> levelToMax) {
        var burnDown = burnUp();
        levelToMax.forEach((level, max) -> {
            var burnUp = burnDown.get(level);
            var size = burnUp.size();
            for (int i = 0; i < size; ++i) {
                burnUp.set(i, max - burnUp.get(i));
            }
        });
        return burnDown;
    }
}
