package com.hits.language_school_back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskCriterionDTO {
    private UUID id;
    private UUID taskId;
    private String title;
    private String description;
    private Integer maxPoints;
    private String sectionName;
    private Integer orderIndex;
    private Boolean active;
}
