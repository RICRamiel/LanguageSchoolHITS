package com.hits.language_school_back.dto;

import com.hits.language_school_back.enums.PeerReviewAssignmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeerReviewAssignmentDTO {
    private UUID id;
    private UUID taskId;
    private UUID reviewerTeamId;
    private UUID reviewedTeamId;
    private UUID targetParticipationId;
    private UUID assessmentId;
    private PeerReviewAssignmentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime submittedAt;
    private UUID teacherEditorId;
    private LocalDateTime teacherEditedAt;
}
