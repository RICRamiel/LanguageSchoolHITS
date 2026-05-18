package com.hits.language_school_back.repository;

import com.hits.language_school_back.model.AssessmentItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssessmentItemRepository extends JpaRepository<AssessmentItem, UUID> {
    List<AssessmentItem> findAllByAssessmentId(UUID assessmentId);

    Optional<AssessmentItem> findByAssessmentIdAndCriterionId(UUID assessmentId, UUID criterionId);

    boolean existsByCriterionIdAndPointsGreaterThan(UUID criterionId, Integer points);
}
