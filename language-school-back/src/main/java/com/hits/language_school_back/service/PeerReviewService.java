package com.hits.language_school_back.service;

import com.hits.language_school_back.dto.PeerReviewAccessDTO;
import com.hits.language_school_back.dto.PeerReviewAssignmentDTO;
import com.hits.language_school_back.dto.PeerReviewEnableDTO;
import com.hits.language_school_back.dto.PeerReviewManualAssignmentDTO;
import com.hits.language_school_back.dto.PeerReviewResultDTO;
import com.hits.language_school_back.dto.PeerReviewResultsDTO;
import com.hits.language_school_back.dto.PeerReviewSettingsDTO;
import com.hits.language_school_back.dto.AssessmentSubmitDTO;
import com.hits.language_school_back.model.Task;

import java.util.UUID;

public interface PeerReviewService {
    Task enablePeerReview(UUID taskId, PeerReviewEnableDTO dto, UUID teacherId);

    PeerReviewAssignmentDTO assignManualPeerReview(UUID taskId, PeerReviewManualAssignmentDTO dto, UUID teacherId);

    PeerReviewSettingsDTO getPeerReviewSettings(UUID taskId, UUID teacherId);

    PeerReviewResultsDTO getPeerReviewResults(UUID taskId, UUID teacherId);

    PeerReviewResultsDTO confirmPeerReviewResults(UUID taskId, UUID teacherId);

    PeerReviewAccessDTO getMyPeerReviewAssignment(UUID taskId, UUID studentId);

    PeerReviewAccessDTO submitMyPeerReviewAssignment(UUID taskId, AssessmentSubmitDTO dto, UUID studentId);

    PeerReviewResultDTO editPeerReviewAssessment(UUID taskId, UUID assignmentId, AssessmentSubmitDTO dto, UUID teacherId);
}
