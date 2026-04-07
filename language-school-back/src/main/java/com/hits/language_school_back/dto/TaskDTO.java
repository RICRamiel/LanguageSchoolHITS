package com.hits.language_school_back.dto;

import com.hits.language_school_back.enums.TaskResolveType;
import com.hits.language_school_back.enums.TeamType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskDTO {
    private UUID id;
    private String name;
    private String description;
    private LocalDate deadline;
    private UUID courseId;
    private String courseName;
    private Integer totalPoints;
    private Integer maxTeamSize;
    private Integer minTeamSize;
    private Integer maxTeamsAmount;
    private Integer minTeamsAmount;
    private Integer votesThreshold;
    private Duration teamsCreationTimeout;
    private TeamType teamType;
    private TaskResolveType resolveType;
    private Boolean submissionClosed;
}
