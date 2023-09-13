package com.uu.au.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uu.au.enums.DemonstrationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(indexes = @Index(name = "idx_status", columnList = "status"))
public class Demonstration {
    @Id
    @GeneratedValue
    private Long id;

    @CreationTimestamp
    private LocalDateTime requestTime;

    private LocalDateTime pickupTime;

    private LocalDateTime reportTime;

    @ManyToMany(cascade={CascadeType.PERSIST, CascadeType.MERGE})
    private Set<User> submitters;

    @NotNull
    @Builder.Default
    private DemonstrationStatus status = DemonstrationStatus.SUBMITTED;

    @ManyToMany(cascade={CascadeType.PERSIST, CascadeType.MERGE})
    private List<Achievement> achievements;

    @ManyToOne(cascade={CascadeType.PERSIST, CascadeType.MERGE})
    private User examiner;

    @JsonIgnore
    public boolean isActiveAndSubmittedOrClaimed() {
        return (status == DemonstrationStatus.SUBMITTED || status == DemonstrationStatus.CLAIMED)
            && requestTime.isAfter(LocalDateTime.now().minusHours(24));
    }

    @JsonIgnore
    public boolean isActive() {
        return reportTime == null
                && requestTime.isAfter(LocalDateTime.now().minusHours(24))
                && !status.equals(DemonstrationStatus.IN_FLIGHT);
    }

    @JsonIgnore
    public boolean isPickedUp() {
        return pickupTime != null;
    }

    @JsonIgnore
    public boolean isReported() {
        return reportTime != null;
    }

    @JsonIgnore
    public boolean isActiveAndClaimed() {
        return isActive() && status.equals(DemonstrationStatus.CLAIMED);
    }

    private boolean isFlagged() {
        return (submitters.size() != 2) || (achievements.size() > 4) || (achievements.size() < 2);
    }

    private String zoomRoom;

    private String zoomPassword;

    private String physicalRoom;

    @JsonIgnore
    public long pickupTimeInMinutes() {
        return ChronoUnit.MINUTES.between(requestTime, pickupTime);
    }

    @JsonIgnore
    public long roundTripTimeInMinutes() {
        return ChronoUnit.MINUTES.between(requestTime, reportTime);
    }
}
