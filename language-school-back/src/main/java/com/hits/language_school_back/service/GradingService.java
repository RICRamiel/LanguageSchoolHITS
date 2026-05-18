package com.hits.language_school_back.service;

import com.hits.language_school_back.dto.AssessmentDTO;
import com.hits.language_school_back.dto.AssessmentSubmitDTO;
import com.hits.language_school_back.dto.ParticipationAssessmentDTO;
import com.hits.language_school_back.dto.TaskCriterionDTO;

import java.util.List;
import java.util.UUID;

public interface GradingService {
    TaskCriterionDTO createCriterion(UUID taskId, TaskCriterionDTO dto, UUID teacherId);

    TaskCriterionDTO editCriterion(UUID taskId, UUID criterionId, TaskCriterionDTO dto, UUID teacherId);

    void deactivateCriterion(UUID taskId, UUID criterionId, UUID teacherId);

    List<TaskCriterionDTO> getCriteria(UUID taskId);

    AssessmentDTO submitTeacherAssessment(UUID taskId, UUID participationId, AssessmentSubmitDTO dto, UUID teacherId);

    AssessmentDTO submitSelfAssessment(UUID taskId, UUID participationId, AssessmentSubmitDTO dto, UUID studentId);

    ParticipationAssessmentDTO getParticipationAssessment(UUID taskId, UUID participationId, UUID actorId);
}
