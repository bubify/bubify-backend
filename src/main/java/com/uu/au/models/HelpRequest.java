package com.uu.au.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uu.au.enums.DemonstrationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// @Table(indexes = @Index(name = "idx_status", columnList = "status")) // BUG? Same column name as in Demonstration!
@Table(indexes = @Index(name = "idx_status_help", columnList = "status"))
public class HelpRequest {
    @Id
    @GeneratedValue
    private Long id;

    @CreationTimestamp
    private LocalDateTime requestTime;

    @Nullable
    private LocalDateTime pickupTime;

    @Nullable
    private LocalDateTime reportTime;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    private Set<User> submitters;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    private User helper;

    @Nullable
    private String message;

    private String zoomRoom;

    private String zoomPassword;

    private String physicalRoom;

    @Builder.Default
    @Column(name = "status")
    private DemonstrationStatus status = DemonstrationStatus.SUBMITTED;

    @JsonIgnore
    public boolean isActive() {
        return reportTime == null
                && requestTime.isAfter(LocalDateTime.now().minusHours(24))
                && !status.equals(DemonstrationStatus.IN_FLIGHT);
    }

    @JsonIgnore
    public boolean isActiveAndSubmitted() {
        return isActive() && status.equals(DemonstrationStatus.SUBMITTED);
    }

    @JsonIgnore
    public boolean isActiveAndClaimed() {
        return isActive() && status.equals(DemonstrationStatus.CLAIMED);
    }

    @JsonIgnore
    public boolean isActiveAndSubmittedOrClaimedOrInFlight() {
        return isActive() && (status.equals(DemonstrationStatus.SUBMITTED) || status.equals(DemonstrationStatus.CLAIMED) || status.equals(DemonstrationStatus.IN_FLIGHT));
    }

    @JsonIgnore
    public boolean isActiveAndSubmittedOrClaimed() {
        return isActive() && (status.equals(DemonstrationStatus.SUBMITTED) || status.equals(DemonstrationStatus.CLAIMED));
    }

    @JsonIgnore
    public boolean isPickedUp() {
        return status.equals(DemonstrationStatus.CLAIMED);
    }

    public boolean includesSubmitter(User u) {
        return submitters != null
                && submitters.contains(u);
    }

    @JsonIgnore
    public long pickupTimeInMinutes() {
        return ChronoUnit.MINUTES.between(requestTime, pickupTime);
    }

    @JsonIgnore
    public long roundTripTimeInMinutes() {
        return ChronoUnit.MINUTES.between(requestTime, reportTime);
    }
}
