package com.hits.language_school_back.infrastructure;

import com.hits.language_school_back.dto.PeerReviewEnableDTO;
import com.hits.language_school_back.model.Course;
import com.hits.language_school_back.model.Task;
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
}
