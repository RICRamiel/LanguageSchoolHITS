package com.hits.language_school_back.service;

import com.hits.language_school_back.dto.PeerReviewEnableDTO;
import com.hits.language_school_back.enums.PeerReviewDistributionType;
import com.hits.language_school_back.enums.Role;
import com.hits.language_school_back.infrastructure.PeerReviewServiceImpl;
import com.hits.language_school_back.model.Course;
import com.hits.language_school_back.model.Task;
import com.hits.language_school_back.model.User;
import com.hits.language_school_back.repository.TaskRepository;
import com.hits.language_school_back.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PeerReviewServiceTest {
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private PeerReviewDistributionService peerReviewDistributionService;

    @InjectMocks
    private PeerReviewServiceImpl peerReviewService;

    private UUID taskId;
    private UUID teacherId;
    private UUID otherTeacherId;
    private Task task;

    @BeforeEach
    void setUp() {
        taskId = UUID.randomUUID();
        teacherId = UUID.randomUUID();
        otherTeacherId = UUID.randomUUID();

        User teacher = User.builder()
                .id(teacherId)
                .role(Role.TEACHER)
                .build();
        Course course = Course.builder()
                .id(UUID.randomUUID())
                .teacher(teacher)
                .build();
        task = Task.builder()
                .id(taskId)
                .name("Peer task")
                .description("Description")
                .course(course)
                .createdBy(teacher)
                .peerReviewEnabled(false)
                .peerReviewerVisibleToTeams(false)
                .build();
    }

    @Test
    void enablePeerReview_whenTeacherOwnsTaskAndTeamsExist_savesSettingsAndStartsDistribution() {
        PeerReviewEnableDTO dto = enableDto(PeerReviewDistributionType.PAIR, true);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(teamRepository.countByTaskId(taskId)).thenReturn(2L);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task result = peerReviewService.enablePeerReview(taskId, dto, teacherId);

        assertThat(result.getPeerReviewEnabled()).isTrue();
        assertThat(result.getPeerReviewDistributionType()).isEqualTo(PeerReviewDistributionType.PAIR);
        assertThat(result.getPeerReviewerVisibleToTeams()).isTrue();
        verify(taskRepository).save(task);
        verify(peerReviewDistributionService).createDistributionIfReady(task);
    }

    @Test
    void enablePeerReview_whenDistributionTypeMissing_rejectsRequest() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> peerReviewService.enablePeerReview(taskId, enableDto(null, true), teacherId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("peerReviewDistributionType is required when peer review is enabled");

        verify(taskRepository, never()).save(any(Task.class));
        verify(peerReviewDistributionService, never()).createDistributionIfReady(any(Task.class));
    }

    @Test
    void enablePeerReview_whenActorIsNotCourseTeacher_rejectsRequest() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> peerReviewService.enablePeerReview(taskId, enableDto(PeerReviewDistributionType.PAIR, true), otherTeacherId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Only the course teacher can manage this task");

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void enablePeerReview_whenTaskHasNoTeams_rejectsRequest() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(teamRepository.countByTaskId(taskId)).thenReturn(0L);

        assertThatThrownBy(() -> peerReviewService.enablePeerReview(taskId, enableDto(PeerReviewDistributionType.PAIR, true), teacherId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Task must have teams before enabling peer review");

        verify(taskRepository, never()).save(any(Task.class));
        verify(peerReviewDistributionService, never()).createDistributionIfReady(any(Task.class));
    }

    @Test
    void enablePeerReview_whenAlreadyEnabledWithSameSettings_isIdempotent() {
        task.setPeerReviewEnabled(true);
        task.setPeerReviewDistributionType(PeerReviewDistributionType.CIRCLE);
        task.setPeerReviewerVisibleToTeams(false);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(teamRepository.countByTaskId(taskId)).thenReturn(3L);

        Task result = peerReviewService.enablePeerReview(taskId, enableDto(PeerReviewDistributionType.CIRCLE, false), teacherId);

        assertThat(result).isSameAs(task);
        verify(taskRepository, never()).save(any(Task.class));
        verify(peerReviewDistributionService).createDistributionIfReady(task);
    }

    @Test
    void enablePeerReview_whenAlreadyEnabledWithDifferentSettings_rejectsRequest() {
        task.setPeerReviewEnabled(true);
        task.setPeerReviewDistributionType(PeerReviewDistributionType.CIRCLE);
        task.setPeerReviewerVisibleToTeams(false);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(teamRepository.countByTaskId(taskId)).thenReturn(3L);

        assertThatThrownBy(() -> peerReviewService.enablePeerReview(taskId, enableDto(PeerReviewDistributionType.PAIR, false), teacherId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Peer review is already enabled for this task with different settings");

        verify(taskRepository, never()).save(any(Task.class));
        verify(peerReviewDistributionService, never()).createDistributionIfReady(any(Task.class));
    }

    private PeerReviewEnableDTO enableDto(PeerReviewDistributionType distributionType, Boolean reviewerVisibleToTeams) {
        return PeerReviewEnableDTO.builder()
                .peerReviewDistributionType(distributionType)
                .peerReviewerVisibleToTeams(reviewerVisibleToTeams)
                .build();
    }
}
