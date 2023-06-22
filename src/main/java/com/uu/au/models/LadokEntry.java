package com.uu.au.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uu.au.enums.LadokEntryType;
import com.uu.au.enums.Result;
import com.uu.au.enums.Role;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
//@EqualsAndHashCode(of={"id"})
public class LadokEntry {
    @Id
    @GeneratedValue
    private Long id;

    @JsonIgnore
    @NotNull
    @ManyToOne(cascade=CascadeType.ALL)
    private User user;

    @NotNull
    private LadokEntryType type;

    @JsonIgnore
    @ManyToMany(cascade=CascadeType.ALL)
    private Set<Achievement> consumedByEntry;

    private LocalDateTime reportTime;

    public boolean isReported() {
        return this.reportTime != null;
    }
}
