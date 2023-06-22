package com.uu.au.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AchievementPushedBack {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @NotNull
    @JoinColumn(name="enrolment_id")
    private Enrolment enrolment;

    @ManyToOne
    @NotNull
    @JoinColumn(name="achievement_id")
    private Achievement achievement;

    @CreationTimestamp
    private LocalDateTime pushedBackTime;

    @UpdateTimestamp
    @Column(name="updatedDateTime", columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedDateTime;

    public boolean isActive() {
        return pushedBackTime.isAfter(LocalDateTime.now().minusDays(1));
    }
}
