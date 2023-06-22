package com.uu.au.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties({ "enrolment", "achievement" })
public class AchievementUnlocked {
    @Id
    @GeneratedValue
    private Long id;

    @NotNull
    @ManyToOne(cascade=CascadeType.ALL)
    @JoinColumn(name="enrolment_id")
    @JsonIgnore
    private Enrolment enrolment;

    @NotNull
    @ManyToOne(cascade=CascadeType.ALL)
    @JoinColumn(name="achievement_id")
    @JsonIgnore
    private Achievement achievement;

    @CreationTimestamp
    private LocalDateTime unlockTime;

    @UpdateTimestamp
    @Column(name="updatedDateTime", columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedDateTime;
}
