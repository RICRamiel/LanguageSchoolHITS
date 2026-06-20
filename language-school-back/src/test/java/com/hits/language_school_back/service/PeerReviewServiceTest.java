package com.hits.language_school_back.service;

import com.hits.language_school_back.dto.PeerReviewAccessDTO;
import com.hits.language_school_back.dto.PeerReviewAssignmentDTO;
import com.hits.language_school_back.dto.PeerReviewEnableDTO;
import com.hits.language_school_back.dto.PeerReviewManualAssignmentDTO;
import com.hits.language_school_back.dto.PeerReviewSettingsDTO;
import com.hits.language_school_back.enums.PeerReviewAssignmentStatus;
import com.hits.language_school_back.enums.PeerReviewDistributionType;
import com.hits.language_school_back.enums.Role;
import com.hits.language_school_back.enums.SolutionStatus;
import com.hits.language_school_back.infrastructure.PeerReviewServiceImpl;
import com.hits.language_school_back.model.Course;
import com.hits.language_school_back.model.PeerReviewAssignment;
import com.hits.language_school_back.model.Participation;
import com.hits.language_school_back.model.Task;
import com.hits.language_school_back.model.TaskCriterion;
import com.hits.language_school_back.model.Team;
import com.hits.language_school_back.model.User;
import com.hits.language_school_back.repository.ParticipationRepository;
import com.hits.language_school_back.repository.PeerReviewAssignmentRepository;
import com.hits.language_school_back.repository.TaskCriterionRepository;
import com.hits.language_school_back.repository.TaskRepository;
import com.hits.language_school_back.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
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
    private ParticipationRepository participationRepository;
    @Mock
    private TaskCriterionRepository taskCriterionRepository;
    @Mock
    private PeerReviewAssignmentRepository peerReviewAssignmentRepository;
    @Mock
    private PeerReviewDistributionService peerReviewDistributionService;

    @InjectMocks
    private PeerReviewServiceImpl peerReviewService;

    private UUID taskId;
    private UUID teacherId;
    private UUID otherTeacherId;
    private UUID reviewerTeamId;
    private UUID reviewedTeamId;
    private UUID studentId;
    private Task task;
    private Team reviewerTeam;
    private Team reviewedTeam;
    private Participation targetParticipation;

    @BeforeEach
    void setUp() {
        taskId = UUID.randomUUID();
        teacherId = UUID.randomUUID();
        otherTeacherId = UUID.randomUUID();
        reviewerTeamId = UUID.randomUUID();
        reviewedTeamId = UUID.randomUUID();
        studentId = UUID.randomUUID();

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

        reviewerTeam = team(reviewerTeamId, "Reviewer");
        reviewedTeam = team(reviewedTeamId, "Reviewed");
        targetParticipation = participation(reviewedTeam);
        reviewedTeam.setSolutionParticipation(targetParticipation);
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

    @Test
    void assignManualPeerReview_whenSettingsValid_savesAssignment() {
        prepareManualTask();
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(teamRepository.findByIdAndTaskId(reviewerTeamId, taskId)).thenReturn(Optional.of(reviewerTeam));
        when(teamRepository.findByIdAndTaskId(reviewedTeamId, taskId)).thenReturn(Optional.of(reviewedTeam));
        when(peerReviewAssignmentRepository.findByTaskIdAndReviewedTeamId(taskId, reviewedTeamId)).thenReturn(Optional.empty());
        when(peerReviewAssignmentRepository.findByTaskIdAndReviewerTeamId(taskId, reviewerTeamId)).thenReturn(Optional.empty());
        when(peerReviewAssignmentRepository.save(any(PeerReviewAssignment.class))).thenAnswer(invocation -> {
            PeerReviewAssignment assignment = invocation.getArgument(0);
            assignment.setId(UUID.randomUUID());
            return assignment;
        });

        PeerReviewAssignmentDTO result = peerReviewService.assignManualPeerReview(taskId, manualAssignmentDto(), teacherId);

        assertThat(result.getTaskId()).isEqualTo(taskId);
        assertThat(result.getReviewerTeamId()).isEqualTo(reviewerTeamId);
        assertThat(result.getReviewedTeamId()).isEqualTo(reviewedTeamId);
        assertThat(result.getTargetParticipationId()).isEqualTo(targetParticipation.getId());
        assertThat(result.getStatus()).isEqualTo(PeerReviewAssignmentStatus.ASSIGNED);

        ArgumentCaptor<PeerReviewAssignment> assignmentCaptor = ArgumentCaptor.forClass(PeerReviewAssignment.class);
        verify(peerReviewAssignmentRepository).save(assignmentCaptor.capture());
        assertThat(assignmentCaptor.getValue().getTask()).isEqualTo(task);
        assertThat(assignmentCaptor.getValue().getReviewerTeam()).isEqualTo(reviewerTeam);
        assertThat(assignmentCaptor.getValue().getReviewedTeam()).isEqualTo(reviewedTeam);
        assertThat(assignmentCaptor.getValue().getTargetParticipation()).isEqualTo(targetParticipation);
    }

    @Test
    void assignManualPeerReview_whenTeamReviewsItself_rejectsAssignment() {
        prepareManualTask();
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(teamRepository.findByIdAndTaskId(reviewerTeamId, taskId)).thenReturn(Optional.of(reviewerTeam));

        assertThatThrownBy(() -> peerReviewService.assignManualPeerReview(
                taskId,
                PeerReviewManualAssignmentDTO.builder()
                        .reviewerTeamId(reviewerTeamId)
                        .reviewedTeamId(reviewerTeamId)
                        .build(),
                teacherId
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Team cannot review itself");

        verify(peerReviewAssignmentRepository, never()).save(any(PeerReviewAssignment.class));
    }

    @Test
    void assignManualPeerReview_whenReviewedTeamAlreadyHasReviewer_rejectsAssignment() {
        prepareManualTask();
        Team otherReviewer = team(UUID.randomUUID(), "Other reviewer");
        PeerReviewAssignment existing = assignment(otherReviewer, reviewedTeam);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(teamRepository.findByIdAndTaskId(reviewerTeamId, taskId)).thenReturn(Optional.of(reviewerTeam));
        when(teamRepository.findByIdAndTaskId(reviewedTeamId, taskId)).thenReturn(Optional.of(reviewedTeam));
        when(peerReviewAssignmentRepository.findByTaskIdAndReviewedTeamId(taskId, reviewedTeamId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> peerReviewService.assignManualPeerReview(taskId, manualAssignmentDto(), teacherId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Reviewed team already has reviewer");

        verify(peerReviewAssignmentRepository, never()).save(any(PeerReviewAssignment.class));
    }

    @Test
    void assignManualPeerReview_whenReviewerAlreadyHasAssignment_rejectsAssignment() {
        prepareManualTask();
        Team otherReviewed = team(UUID.randomUUID(), "Other reviewed");
        PeerReviewAssignment existing = assignment(reviewerTeam, otherReviewed);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(teamRepository.findByIdAndTaskId(reviewerTeamId, taskId)).thenReturn(Optional.of(reviewerTeam));
        when(teamRepository.findByIdAndTaskId(reviewedTeamId, taskId)).thenReturn(Optional.of(reviewedTeam));
        when(peerReviewAssignmentRepository.findByTaskIdAndReviewedTeamId(taskId, reviewedTeamId)).thenReturn(Optional.empty());
        when(peerReviewAssignmentRepository.findByTaskIdAndReviewerTeamId(taskId, reviewerTeamId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> peerReviewService.assignManualPeerReview(taskId, manualAssignmentDto(), teacherId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Reviewer team already has review assignment");

        verify(peerReviewAssignmentRepository, never()).save(any(PeerReviewAssignment.class));
    }

    @Test
    void assignManualPeerReview_whenTaskIsOpen_rejectsAssignment() {
        prepareManualTask();
        task.setSubmissionClosed(false);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> peerReviewService.assignManualPeerReview(taskId, manualAssignmentDto(), teacherId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Task submissions must be closed before assigning peer reviews");

        verify(peerReviewAssignmentRepository, never()).save(any(PeerReviewAssignment.class));
    }

    @Test
    void assignManualPeerReview_whenDistributionTypeIsNotManual_rejectsAssignment() {
        prepareManualTask();
        task.setPeerReviewDistributionType(PeerReviewDistributionType.PAIR);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> peerReviewService.assignManualPeerReview(taskId, manualAssignmentDto(), teacherId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Manual peer-review assignment requires MANUAL distribution type");

        verify(peerReviewAssignmentRepository, never()).save(any(PeerReviewAssignment.class));
    }

    @Test
    void assignManualPeerReview_whenReviewedTeamHasNoSolution_rejectsAssignment() {
        prepareManualTask();
        reviewedTeam.setSolutionParticipation(null);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(teamRepository.findByIdAndTaskId(reviewerTeamId, taskId)).thenReturn(Optional.of(reviewerTeam));
        when(teamRepository.findByIdAndTaskId(reviewedTeamId, taskId)).thenReturn(Optional.of(reviewedTeam));
        when(peerReviewAssignmentRepository.findByTaskIdAndReviewedTeamId(taskId, reviewedTeamId)).thenReturn(Optional.empty());
        when(peerReviewAssignmentRepository.findByTaskIdAndReviewerTeamId(taskId, reviewerTeamId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> peerReviewService.assignManualPeerReview(taskId, manualAssignmentDto(), teacherId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Reviewed team has no submitted solution");

        verify(peerReviewAssignmentRepository, never()).save(any(PeerReviewAssignment.class));
    }

    @Test
    void getPeerReviewSettings_whenTeamWithoutReviewerExists_returnsWarningWithTeamName() {
        task.setPeerReviewEnabled(true);
        task.setPeerReviewDistributionType(PeerReviewDistributionType.PAIR);
        PeerReviewAssignment assigned = assignment(reviewerTeam, reviewedTeam);
        Team teamWithoutReviewer = team(UUID.randomUUID(), "Lonely team");
        teamWithoutReviewer.setSolutionParticipation(participation(teamWithoutReviewer));
        PeerReviewAssignment withoutReviewer = assignment(null, teamWithoutReviewer);
        withoutReviewer.setStatus(PeerReviewAssignmentStatus.WITHOUT_REVIEWER);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(peerReviewAssignmentRepository.findAllByTaskId(taskId)).thenReturn(List.of(assigned, withoutReviewer));

        PeerReviewSettingsDTO result = peerReviewService.getPeerReviewSettings(taskId, teacherId);

        assertThat(result.getTaskId()).isEqualTo(taskId);
        assertThat(result.getPeerReviewEnabled()).isTrue();
        assertThat(result.getPeerReviewDistributionType()).isEqualTo(PeerReviewDistributionType.PAIR);
        assertThat(result.getAssignments()).hasSize(2);
        assertThat(result.getHasTeamsWithoutReviewer()).isTrue();
        assertThat(result.getTeamsWithoutReviewer()).singleElement().satisfies(warning -> {
            assertThat(warning.getTeamId()).isEqualTo(teamWithoutReviewer.getId());
            assertThat(warning.getTeamName()).isEqualTo("Lonely team");
            assertThat(warning.getMessage()).contains("Lonely team");
        });
    }

    @Test
    void getPeerReviewSettings_whenActorIsNotCourseTeacher_rejectsRequest() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> peerReviewService.getPeerReviewSettings(taskId, otherTeacherId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Only the course teacher can manage this task");
    }

    @Test
    void getMyPeerReviewAssignment_whenUserIsReviewerTeamCaptain_returnsAssignmentAndCriteria() {
        task.setPeerReviewEnabled(true);
        PeerReviewAssignment assignment = assignment(reviewerTeam, reviewedTeam);
        Participation captainParticipation = participation(reviewerTeam, studentId, true);
        TaskCriterion criterion = criterion("Architecture", 10);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(participationRepository.findAllByTeamTaskId(taskId)).thenReturn(List.of(captainParticipation));
        when(peerReviewAssignmentRepository.findByTaskIdAndReviewerTeamId(taskId, reviewerTeamId)).thenReturn(Optional.of(assignment));
        when(taskCriterionRepository.findAllByTaskIdAndActiveTrueOrderByOrderIndexAscTitleAsc(taskId)).thenReturn(List.of(criterion));

        PeerReviewAccessDTO result = peerReviewService.getMyPeerReviewAssignment(taskId, studentId);

        assertThat(result.getTaskId()).isEqualTo(taskId);
        assertThat(result.getAssignment().getId()).isEqualTo(assignment.getId());
        assertThat(result.getReviewerTeamId()).isEqualTo(reviewerTeamId);
        assertThat(result.getReviewedTeamId()).isEqualTo(reviewedTeamId);
        assertThat(result.getReviewedTeamName()).isEqualTo("Reviewed");
        assertThat(result.getCanSubmit()).isTrue();
        assertThat(result.getCriteria()).singleElement().satisfies(dto -> {
            assertThat(dto.getTitle()).isEqualTo("Architecture");
            assertThat(dto.getMaxPoints()).isEqualTo(10);
        });
    }

    @Test
    void getMyPeerReviewAssignment_whenUserIsReviewerTeamMemberButNotCaptain_rejectsAccess() {
        task.setPeerReviewEnabled(true);
        Participation memberParticipation = participation(reviewerTeam, studentId, false);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(participationRepository.findAllByTeamTaskId(taskId)).thenReturn(List.of(memberParticipation));

        assertThatThrownBy(() -> peerReviewService.getMyPeerReviewAssignment(taskId, studentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Only reviewer team captain can access peer review assignment");

        verify(peerReviewAssignmentRepository, never()).findByTaskIdAndReviewerTeamId(any(), any());
    }

    private PeerReviewEnableDTO enableDto(PeerReviewDistributionType distributionType, Boolean reviewerVisibleToTeams) {
        return PeerReviewEnableDTO.builder()
                .peerReviewDistributionType(distributionType)
                .peerReviewerVisibleToTeams(reviewerVisibleToTeams)
                .build();
    }

    private PeerReviewManualAssignmentDTO manualAssignmentDto() {
        return PeerReviewManualAssignmentDTO.builder()
                .reviewerTeamId(reviewerTeamId)
                .reviewedTeamId(reviewedTeamId)
                .build();
    }

    private void prepareManualTask() {
        task.setPeerReviewEnabled(true);
        task.setPeerReviewDistributionType(PeerReviewDistributionType.MANUAL);
        task.setSubmissionClosed(true);
    }

    private Team team(UUID id, String name) {
        Team team = new Team();
        team.setId(id);
        team.setName(name);
        team.setTask(task);
        return team;
    }

    private Participation participation(Team team) {
        return participation(team, UUID.randomUUID(), true);
    }

    private Participation participation(Team team, UUID userId, boolean captain) {
        Participation participation = new Participation();
        participation.setId(UUID.randomUUID());
        participation.setTeam(team);
        participation.setStudent(User.builder().id(userId).role(Role.STUDENT).build());
        participation.setIsCaptain(captain);
        participation.setSolutionStatus(SolutionStatus.LOCKED);
        return participation;
    }

    private TaskCriterion criterion(String title, int maxPoints) {
        TaskCriterion criterion = new TaskCriterion();
        criterion.setId(UUID.randomUUID());
        criterion.setTask(task);
        criterion.setTitle(title);
        criterion.setDescription(title + " description");
        criterion.setMaxPoints(maxPoints);
        criterion.setSectionName("General");
        criterion.setOrderIndex(1);
        criterion.setActive(true);
        return criterion;
    }

    private PeerReviewAssignment assignment(Team reviewerTeam, Team reviewedTeam) {
        PeerReviewAssignment assignment = new PeerReviewAssignment();
        assignment.setId(UUID.randomUUID());
        assignment.setTask(task);
        assignment.setReviewerTeam(reviewerTeam);
        assignment.setReviewedTeam(reviewedTeam);
        assignment.setTargetParticipation(reviewedTeam.getSolutionParticipation());
        assignment.setStatus(PeerReviewAssignmentStatus.ASSIGNED);
        return assignment;
    }
}
