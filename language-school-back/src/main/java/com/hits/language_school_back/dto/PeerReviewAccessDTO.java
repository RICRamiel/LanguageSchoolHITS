package com.hits.language_school_back.dto;

import com.hits.language_school_back.enums.PeerReviewAssignmentStatus;
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
public class PeerReviewAccessDTO {
    private UUID taskId;
    private PeerReviewAssignmentDTO assignment;
    private AssessmentDTO assessment;
    private UUID reviewerTeamId;
    private String reviewerTeamName;
    private UUID reviewedTeamId;
    private String reviewedTeamName;
    private UUID targetParticipationId;
    private PeerReviewAssignmentStatus status;
    private Boolean canSubmit;
    private Integer totalMaxPoints;
    private List<TaskCriterionDTO> criteria;
}
