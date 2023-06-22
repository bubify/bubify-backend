package com.uu.au.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {
    @Id
    @GeneratedValue
    @JsonIgnore
    private Long id;

    @NotNull
    private LocalDate startDate;

    // FIXKE: add a duration as well -- needed to calculate targetVelocity

    @NotBlank
    private String name;

    @NotBlank
    private String gitHubOrgURL;

    @NotBlank
    private String courseWebURL;

    @NotNull
    @Builder.Default
    private boolean helpModule = true;

    @NotNull
    @Builder.Default
    private boolean demoModule = true;

    @NotNull
    @Builder.Default
    private boolean onlyIntroductionTasks = true;

    @NotNull
    @Builder.Default
    private boolean burndownModule = true;

    @NotNull
    @Builder.Default
    private boolean statisticsModule = true;

    @NotNull
    @Builder.Default
    private boolean examMode = false;

    @NotNull
    @Builder.Default
    private boolean profilePictures = true;

    @JsonIgnore
    private LocalDate codeExamDemonstrationBlocker;

    @Builder.Default
    private boolean clearQueuesUsingCron = true;

    @NotNull
    @Builder.Default
    private String roomSetting = "BOTH";

    @CreationTimestamp
    @Column(name="createdDateTime", columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdDateTime;

    @UpdateTimestamp
    @Column(name="updatedDateTime", columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedDateTime;


    public LocalDate  codeExamDemonstrationBlocker() {
        return codeExamDemonstrationBlocker == null
                ? startDate
                : codeExamDemonstrationBlocker;
    }

    @JsonIgnore
    public int getYear() {
        return startDate.getYear();
    }

    /// note: indexed from 0
    public int currentCourseWeek() {
        return Math.max((int) ChronoUnit.WEEKS.between(startDate, LocalDate.now()), 0);
    }

    public boolean equals(Object other) {
        return (other instanceof Course) && ((Course) other).getId().equals(id);
    }
}
