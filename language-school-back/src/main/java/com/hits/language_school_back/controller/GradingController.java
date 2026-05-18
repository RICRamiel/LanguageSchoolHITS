package com.hits.language_school_back.controller;

import com.hits.language_school_back.dto.AssessmentDTO;
import com.hits.language_school_back.dto.AssessmentSubmitDTO;
import com.hits.language_school_back.dto.ParticipationAssessmentDTO;
import com.hits.language_school_back.dto.TaskCriterionDTO;
import com.hits.language_school_back.service.GradingService;
import com.hits.language_school_back.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/task/{taskId}")
public class GradingController {
    private final GradingService gradingService;
    private final UserService userService;

    @PostMapping("/criteria")
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<TaskCriterionDTO> createCriterion(
            @PathVariable UUID taskId,
            @RequestBody TaskCriterionDTO dto,
            HttpServletRequest request) {
        return ResponseEntity.ok(gradingService.createCriterion(taskId, dto, userService.getMe(request).getId()));
    }

    @PutMapping("/criteria/{criterionId}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<TaskCriterionDTO> editCriterion(
            @PathVariable UUID taskId,
            @PathVariable UUID criterionId,
            @RequestBody TaskCriterionDTO dto,
            HttpServletRequest request) {
        return ResponseEntity.ok(gradingService.editCriterion(taskId, criterionId, dto, userService.getMe(request).getId()));
    }

    @DeleteMapping("/criteria/{criterionId}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public void deactivateCriterion(
            @PathVariable UUID taskId,
            @PathVariable UUID criterionId,
            HttpServletRequest request) {
        gradingService.deactivateCriterion(taskId, criterionId, userService.getMe(request).getId());
    }

    @GetMapping("/criteria")
    public ResponseEntity<List<TaskCriterionDTO>> getCriteria(@PathVariable UUID taskId) {
        return ResponseEntity.ok(gradingService.getCriteria(taskId));
    }

    @PutMapping("/participations/{participationId}/teacher-assessment")
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<AssessmentDTO> submitTeacherAssessment(
            @PathVariable UUID taskId,
            @PathVariable UUID participationId,
            @RequestBody AssessmentSubmitDTO dto,
            HttpServletRequest request) {
        return ResponseEntity.ok(gradingService.submitTeacherAssessment(taskId, participationId, dto, userService.getMe(request).getId()));
    }

    @PutMapping("/participations/{participationId}/self-assessment")
    @PreAuthorize("hasAuthority('STUDENT')")
    public ResponseEntity<AssessmentDTO> submitSelfAssessment(
            @PathVariable UUID taskId,
            @PathVariable UUID participationId,
            @RequestBody AssessmentSubmitDTO dto,
            HttpServletRequest request) {
        return ResponseEntity.ok(gradingService.submitSelfAssessment(taskId, participationId, dto, userService.getMe(request).getId()));
    }

    @GetMapping("/participations/{participationId}/assessment")
    public ResponseEntity<ParticipationAssessmentDTO> getParticipationAssessment(
            @PathVariable UUID taskId,
            @PathVariable UUID participationId,
            HttpServletRequest request) {
        return ResponseEntity.ok(gradingService.getParticipationAssessment(taskId, participationId, userService.getMe(request).getId()));
    }
}
