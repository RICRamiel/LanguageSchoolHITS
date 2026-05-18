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
public class AssessmentItemDTO {
    private UUID criterionId;
    private String title;
    private String description;
    private Integer maxPoints;
    private String sectionName;
    private Integer orderIndex;
    private Boolean active;
    private Integer points;
    private String comment;
    private Integer teacherPoints;
    private Integer selfPoints;
    private String teacherComment;
    private String selfComment;
}
