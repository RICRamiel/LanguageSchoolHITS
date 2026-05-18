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
public class ParticipationAssessmentDTO {
    private UUID taskId;
    private UUID participationId;
    private Integer totalMaxPoints;
    private Integer teacherTotal;
    private Integer selfTotal;
    private List<AssessmentItemDTO> criteria;
    private AssessmentDTO teacherAssessment;
    private AssessmentDTO selfAssessment;
}
