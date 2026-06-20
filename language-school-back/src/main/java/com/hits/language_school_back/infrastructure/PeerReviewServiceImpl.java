package com.hits.language_school_back.infrastructure;

import com.hits.language_school_back.dto.PeerReviewAssignmentDTO;
import com.hits.language_school_back.dto.PeerReviewEnableDTO;
import com.hits.language_school_back.dto.PeerReviewManualAssignmentDTO;
import com.hits.language_school_back.enums.PeerReviewAssignmentStatus;
import com.hits.language_school_back.enums.PeerReviewDistributionType;
import com.hits.language_school_back.model.Course;
import com.hits.language_school_back.model.PeerReviewAssignment;
import com.hits.language_school_back.model.Participation;
import com.hits.language_school_back.model.Task;
import com.hits.language_school_back.model.Team;
import com.hits.language_school_back.repository.PeerReviewAssignmentRepository;
import com.hits.language_school_back.repository.TaskRepository;
import com.hits.language_school_back.repository.TeamRepository;
import com.hits.language_school_back.service.PeerReviewDistributionService;
import com.hits.language_school_back.service.PeerReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PeerReviewServiceImpl implements PeerReviewService {
    private final TaskRepository taskRepository;
    private final TeamRepository teamRepository;
    private final PeerReviewAssignmentRepository peerReviewAssignmentRepository;
    private final PeerReviewDistributionService peerReviewDistributionService;

    @Override
    @Transactional
    public Task enablePeerReview(UUID taskId, PeerReviewEnableDTO dto, UUID teacherId) {
        Task task = getTask(taskId);
        ensureTeacherCanManageCourse(teacherId, task.getCourse());
        validateEnableDto(dto);
        ensureTaskHasTeams(taskId);

        Boolean reviewerVisibleToTeams = Boolean.TRUE.equals(dto.getPeerReviewerVisibleToTeams());
        if (Boolean.TRUE.equals(task.getPeerReviewEnabled())) {
            ensurePeerReviewSettingsMatch(task, dto, reviewerVisibleToTeams);
            peerReviewDistributionService.createDistributionIfReady(task);
            return task;
        }

        task.setPeerReviewEnabled(Boolean.TRUE);
        task.setPeerReviewDistributionType(dto.getPeerReviewDistributionType());
        task.setPeerReviewerVisibleToTeams(reviewerVisibleToTeams);

        Task saved = taskRepository.save(task);
        peerReviewDistributionService.createDistributionIfReady(saved);
        return saved;
    }

    @Override
    @Transactional
    public PeerReviewAssignmentDTO assignManualPeerReview(UUID taskId, PeerReviewManualAssignmentDTO dto, UUID teacherId) {
        Task task = getTask(taskId);
        ensureTeacherCanManageCourse(teacherId, task.getCourse());
        validateManualAssignmentDto(dto);
        ensureManualDistributionEnabled(task);
        ensureTaskClosedForAssignments(task);

        Team reviewerTeam = getTeam(taskId, dto.getReviewerTeamId());
        Team reviewedTeam = getTeam(taskId, dto.getReviewedTeamId());
        ensureDifferentTeams(reviewerTeam, reviewedTeam);

        PeerReviewAssignment existingForReviewed = peerReviewAssignmentRepository
                .findByTaskIdAndReviewedTeamId(taskId, reviewedTeam.getId())
                .orElse(null);
        if (existingForReviewed != null) {
            if (sameAssignment(existingForReviewed, reviewerTeam, reviewedTeam)) {
                return toAssignmentDto(existingForReviewed);
            }
            throw new IllegalArgumentException("Reviewed team already has reviewer");
        }

        peerReviewAssignmentRepository.findByTaskIdAndReviewerTeamId(taskId, reviewerTeam.getId())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Reviewer team already has review assignment");
                });

        Participation targetParticipation = reviewedTeam.getSolutionParticipation();
        if (targetParticipation == null) {
            throw new IllegalArgumentException("Reviewed team has no submitted solution");
        }

        PeerReviewAssignment assignment = new PeerReviewAssignment();
        assignment.setTask(task);
        assignment.setReviewerTeam(reviewerTeam);
        assignment.setReviewedTeam(reviewedTeam);
        assignment.setTargetParticipation(targetParticipation);
        assignment.setStatus(PeerReviewAssignmentStatus.ASSIGNED);

        return toAssignmentDto(peerReviewAssignmentRepository.save(assignment));
    }

    private Task getTask(UUID taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));
    }

    private void validateEnableDto(PeerReviewEnableDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Peer-review settings are required");
        }
        if (dto.getPeerReviewDistributionType() == null) {
            throw new IllegalArgumentException("peerReviewDistributionType is required when peer review is enabled");
        }
    }

    private void validateManualAssignmentDto(PeerReviewManualAssignmentDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Peer-review assignment is required");
        }
        if (dto.getReviewerTeamId() == null || dto.getReviewedTeamId() == null) {
            throw new IllegalArgumentException("reviewerTeamId and reviewedTeamId are required");
        }
    }

    private void ensureTaskHasTeams(UUID taskId) {
        if (teamRepository.countByTaskId(taskId) == 0) {
            throw new IllegalArgumentException("Task must have teams before enabling peer review");
        }
    }

    private void ensureTeacherCanManageCourse(UUID teacherId, Course course) {
        if (course == null || course.getTeacher() == null || !course.getTeacher().getId().equals(teacherId)) {
            throw new IllegalArgumentException("Only the course teacher can manage this task");
        }
    }

    private void ensurePeerReviewSettingsMatch(Task task, PeerReviewEnableDTO dto, Boolean reviewerVisibleToTeams) {
        boolean sameDistributionType = task.getPeerReviewDistributionType() == dto.getPeerReviewDistributionType();
        boolean sameReviewerVisibility = Objects.equals(Boolean.TRUE.equals(task.getPeerReviewerVisibleToTeams()), reviewerVisibleToTeams);
        if (!sameDistributionType || !sameReviewerVisibility) {
            throw new IllegalArgumentException("Peer review is already enabled for this task with different settings");
        }
    }

    private Team getTeam(UUID taskId, UUID teamId) {
        return teamRepository.findByIdAndTaskId(teamId, taskId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found in task: " + teamId));
    }

    private void ensureManualDistributionEnabled(Task task) {
        if (!Boolean.TRUE.equals(task.getPeerReviewEnabled())) {
            throw new IllegalArgumentException("Peer review is not enabled for this task");
        }
        if (task.getPeerReviewDistributionType() != PeerReviewDistributionType.MANUAL) {
            throw new IllegalArgumentException("Manual peer-review assignment requires MANUAL distribution type");
        }
    }

    private void ensureTaskClosedForAssignments(Task task) {
        if (!Boolean.TRUE.equals(task.getSubmissionClosed())) {
            throw new IllegalArgumentException("Task submissions must be closed before assigning peer reviews");
        }
    }

    private void ensureDifferentTeams(Team reviewerTeam, Team reviewedTeam) {
        if (Objects.equals(reviewerTeam.getId(), reviewedTeam.getId())) {
            throw new IllegalArgumentException("Team cannot review itself");
        }
    }

    private boolean sameAssignment(PeerReviewAssignment assignment, Team reviewerTeam, Team reviewedTeam) {
        return assignment.getReviewerTeam() != null
                && Objects.equals(assignment.getReviewerTeam().getId(), reviewerTeam.getId())
                && assignment.getReviewedTeam() != null
                && Objects.equals(assignment.getReviewedTeam().getId(), reviewedTeam.getId());
    }

    private PeerReviewAssignmentDTO toAssignmentDto(PeerReviewAssignment assignment) {
        return PeerReviewAssignmentDTO.builder()
                .id(assignment.getId())
                .taskId(assignment.getTask() == null ? null : assignment.getTask().getId())
                .reviewerTeamId(assignment.getReviewerTeam() == null ? null : assignment.getReviewerTeam().getId())
                .reviewedTeamId(assignment.getReviewedTeam() == null ? null : assignment.getReviewedTeam().getId())
                .targetParticipationId(assignment.getTargetParticipation() == null ? null : assignment.getTargetParticipation().getId())
                .assessmentId(assignment.getAssessment() == null ? null : assignment.getAssessment().getId())
                .status(assignment.getStatus())
                .createdAt(assignment.getCreatedAt())
                .submittedAt(assignment.getSubmittedAt())
                .teacherEditedAt(assignment.getTeacherEditedAt())
                .build();
    }
}
