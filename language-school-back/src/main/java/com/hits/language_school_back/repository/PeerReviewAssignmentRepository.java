package com.hits.language_school_back.repository;

import com.hits.language_school_back.enums.PeerReviewAssignmentStatus;
import com.hits.language_school_back.model.PeerReviewAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PeerReviewAssignmentRepository extends JpaRepository<PeerReviewAssignment, UUID> {
    List<PeerReviewAssignment> findAllByTaskId(UUID taskId);

    List<PeerReviewAssignment> findAllByTaskIdAndStatus(UUID taskId, PeerReviewAssignmentStatus status);

    Optional<PeerReviewAssignment> findByTaskIdAndReviewedTeamId(UUID taskId, UUID reviewedTeamId);

    Optional<PeerReviewAssignment> findByTaskIdAndReviewerTeamId(UUID taskId, UUID reviewerTeamId);

    boolean existsByTaskIdAndReviewedTeamId(UUID taskId, UUID reviewedTeamId);

    boolean existsByTaskIdAndReviewerTeamId(UUID taskId, UUID reviewerTeamId);
}
