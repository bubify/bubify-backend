package com.uu.au.models;

import com.uu.au.enums.Level;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TargetLevel {
    @Id
    @GeneratedValue
    private Long id;

    /// FIXME: turn into proper JPA relation
    @NotNull
    private Long enrolmentId;

    private Level level;

    @CreationTimestamp
    private LocalDateTime changeTime;
}
