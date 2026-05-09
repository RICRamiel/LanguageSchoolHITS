package com.hits.language_school_back.repository;

import com.hits.language_school_back.model.TaskGradingCriterion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskGradingCriterionRepository extends JpaRepository<TaskGradingCriterion, UUID> {
    List<TaskGradingCriterion> findAllByTaskIdOrderByPositionAsc(UUID taskId);
}
