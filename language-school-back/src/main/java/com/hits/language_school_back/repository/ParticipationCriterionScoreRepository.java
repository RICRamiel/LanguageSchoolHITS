package com.hits.language_school_back.repository;

import com.hits.language_school_back.model.ParticipationCriterionScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParticipationCriterionScoreRepository extends JpaRepository<ParticipationCriterionScore, UUID> {
    List<ParticipationCriterionScore> findAllByParticipationId(UUID participationId);

    Optional<ParticipationCriterionScore> findByParticipationIdAndCriterionId(UUID participationId, UUID criterionId);

    long countByCriterionTaskId(UUID taskId);
}
