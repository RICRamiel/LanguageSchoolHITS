package com.hits.language_school_back.model;

import com.hits.language_school_back.enums.TaskResolveType;
import com.hits.language_school_back.enums.TeamType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "task")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    private String name;
    @NotBlank
    private String description;
    private LocalDate deadline;

    @NotBlank
    private Integer maxTeamSize;
    @NotBlank
    private Integer minTeamSize;

    @NotBlank
    private Integer maxTeamsAmount;
    @NotBlank
    private Integer minTeamsAmount;

    private Integer votesThreshold;
    private Duration teamsCreationTimeout;
    private LocalDateTime createdAt;

    @NotBlank
    private TeamType teamType;
    @NotBlank
    private TaskResolveType resolveType;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "task")
    private List<Comment> commentList;

    @OneToMany(mappedBy = "task")
    private List<Task> taskList;
}
