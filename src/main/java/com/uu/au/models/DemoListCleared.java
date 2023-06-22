package com.uu.au.models;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of={"id"})
public class DemoListCleared {
    @Id
    @GeneratedValue
    private Long id;

    @CreationTimestamp
    private LocalDateTime time;

    @NotNull
    @ManyToOne
    @JoinColumn(nullable=false)
    private User user;
}
