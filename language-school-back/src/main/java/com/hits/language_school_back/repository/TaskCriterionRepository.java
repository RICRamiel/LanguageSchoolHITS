package com.hits.language_school_back.repository;

import com.hits.language_school_back.model.TaskCriterion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskCriterionRepository extends JpaRepository<TaskCriterion, UUID> {
    List<TaskCriterion> findAllByTaskIdOrderByOrderIndexAscTitleAsc(UUID taskId);

    List<TaskCriterion> findAllByTaskIdAndActiveTrueOrderByOrderIndexAscTitleAsc(UUID taskId);

    boolean existsByTaskIdAndActiveTrue(UUID taskId);
}
