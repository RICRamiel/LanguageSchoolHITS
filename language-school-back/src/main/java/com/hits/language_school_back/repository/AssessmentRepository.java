package com.hits.language_school_back.repository;

import com.hits.language_school_back.enums.AssessmentType;
import com.hits.language_school_back.model.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssessmentRepository extends JpaRepository<Assessment, UUID> {
    Optional<Assessment> findByParticipationIdAndType(UUID participationId, AssessmentType type);

    List<Assessment> findAllByParticipationId(UUID participationId);
}
