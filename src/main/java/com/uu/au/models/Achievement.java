package com.uu.au.models;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uu.au.enums.AchievementType;
import com.uu.au.enums.Level;
import lombok.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of={"id"})
public class Achievement {
    @Id
    @GeneratedValue
    private Long id;

    @Column(unique=true, length=32)
    @NotBlank
    private String code;

    @Column(unique=true, length=512)
    @NotBlank
    private String name;

    @Column(length=512)
    @NotBlank
    private String urlToDescription;

    @Builder.Default
    private AchievementType achievementType = AchievementType.ACHIEVEMENT;

    @NotNull
    private Level level;

    @CreationTimestamp
    @Column(name="createdDateTime", columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdDateTime;

    @UpdateTimestamp
    @Column(name="updatedDateTime", columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedDateTime;


    @JsonIgnore
    public boolean requiredForLevel(Level otherLevel) {
        return this.level.lessThanOrEqual(otherLevel);
    }

    public boolean isIntroTask() {
        return achievementType == AchievementType.INTRO_LAB;
    }

    public boolean isCodeExam() {
        return achievementType.equals(AchievementType.CODE_EXAM);
    }

    public boolean isAssignment() {
        return code.startsWith("Z");
    }

    public boolean isLab() {
        return code.startsWith("LAB");
    }
}
