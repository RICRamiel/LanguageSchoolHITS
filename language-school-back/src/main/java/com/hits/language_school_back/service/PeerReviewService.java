package com.hits.language_school_back.service;

import com.hits.language_school_back.dto.PeerReviewAssignmentDTO;
import com.hits.language_school_back.dto.PeerReviewEnableDTO;
import com.hits.language_school_back.dto.PeerReviewManualAssignmentDTO;
import com.hits.language_school_back.model.Task;

import java.util.UUID;

public interface PeerReviewService {
    Task enablePeerReview(UUID taskId, PeerReviewEnableDTO dto, UUID teacherId);

    PeerReviewAssignmentDTO assignManualPeerReview(UUID taskId, PeerReviewManualAssignmentDTO dto, UUID teacherId);
}
