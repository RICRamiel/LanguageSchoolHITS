package com.hits.language_school_back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskTeamDTO {
    private UUID id;
    private String name;
    private Integer commandMark;
    private Double averageMark;
    private Integer membersCount;
    private UUID captainId;
    private UUID solutionParticipationId;
    private List<TaskParticipationDTO> participations;
}
