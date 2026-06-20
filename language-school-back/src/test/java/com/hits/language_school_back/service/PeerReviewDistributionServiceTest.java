package com.hits.language_school_back.service;

import com.hits.language_school_back.enums.PeerReviewAssignmentStatus;
import com.hits.language_school_back.enums.PeerReviewDistributionType;
import com.hits.language_school_back.enums.SolutionStatus;
import com.hits.language_school_back.infrastructure.PeerReviewDistributionServiceImpl;
import com.hits.language_school_back.model.PeerReviewAssignment;
import com.hits.language_school_back.model.Participation;
import com.hits.language_school_back.model.Task;
import com.hits.language_school_back.model.Team;
import com.hits.language_school_back.repository.PeerReviewAssignmentRepository;
import com.hits.language_school_back.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PeerReviewDistributionServiceTest {
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private PeerReviewAssignmentRepository peerReviewAssignmentRepository;

    @InjectMocks
    private PeerReviewDistributionServiceImpl distributionService;

    private UUID taskId;
    private Task task;

    @BeforeEach
    void setUp() {
        taskId = UUID.randomUUID();
        task = Task.builder()
                .id(taskId)
                .name("Task")
                .description("Description")
                .peerReviewEnabled(true)
                .submissionClosed(true)
                .build();
    }

    @Test
    void createDistributionIfReady_whenPairAndEvenTeams_createsMutualPairs() {
        task.setPeerReviewDistributionType(PeerReviewDistributionType.PAIR);
        Team a = team("A", true);
        Team b = team("B", true);
        Team c = team("C", true);
        Team d = team("D", true);
        prepareTeams(List.of(a, b, c, d));

        distributionService.createDistributionIfReady(task);

        List<PeerReviewAssignment> assignments = savedAssignments();
        assertThat(assignments).hasSize(4);
        assertAssignment(assignments, a, b, PeerReviewAssignmentStatus.ASSIGNED);
        assertAssignment(assignments, b, a, PeerReviewAssignmentStatus.ASSIGNED);
        assertAssignment(assignments, c, d, PeerReviewAssignmentStatus.ASSIGNED);
        assertAssignment(assignments, d, c, PeerReviewAssignmentStatus.ASSIGNED);
        assertEverySubmittedTeamHasOneReviewer(assignments, List.of(a, b, c, d));
    }

    @Test
    void createDistributionIfReady_whenPairAndOddTeams_createsOneWithoutReviewer() {
        task.setPeerReviewDistributionType(PeerReviewDistributionType.PAIR);
        Team a = team("A", true);
        Team b = team("B", true);
        Team c = team("C", true);
        prepareTeams(List.of(a, b, c));

        distributionService.createDistributionIfReady(task);

        List<PeerReviewAssignment> assignments = savedAssignments();
        assertThat(assignments).hasSize(3);
        assertAssignment(assignments, a, b, PeerReviewAssignmentStatus.ASSIGNED);
        assertAssignment(assignments, b, a, PeerReviewAssignmentStatus.ASSIGNED);
        assertWithoutReviewer(assignments, c);
    }

    @Test
    void createDistributionIfReady_whenCircle_createsCircleAssignments() {
        task.setPeerReviewDistributionType(PeerReviewDistributionType.CIRCLE);
        Team a = team("A", true);
        Team b = team("B", true);
        Team c = team("C", true);
        prepareTeams(List.of(a, b, c));

        distributionService.createDistributionIfReady(task);

        List<PeerReviewAssignment> assignments = savedAssignments();
        assertThat(assignments).hasSize(3);
        assertAssignment(assignments, a, b, PeerReviewAssignmentStatus.ASSIGNED);
        assertAssignment(assignments, b, c, PeerReviewAssignmentStatus.ASSIGNED);
        assertAssignment(assignments, c, a, PeerReviewAssignmentStatus.ASSIGNED);
        assertEverySubmittedTeamHasOneReviewer(assignments, List.of(a, b, c));
    }

    @Test
    void createDistributionIfReady_whenRandomPairAndOddTeams_keepsPairInvariants() {
        task.setPeerReviewDistributionType(PeerReviewDistributionType.RANDOM_PAIR);
        List<Team> teams = List.of(team("A", true), team("B", true), team("C", true), team("D", true), team("E", true));
        prepareTeams(teams);

        distributionService.createDistributionIfReady(task);

        List<PeerReviewAssignment> assignments = savedAssignments();
        assertThat(assignments).hasSize(5);
        assertThat(assignments.stream().filter(assignment -> assignment.getStatus() == PeerReviewAssignmentStatus.WITHOUT_REVIEWER)).hasSize(1);
        assertThat(assignments.stream().filter(assignment -> assignment.getStatus() == PeerReviewAssignmentStatus.ASSIGNED)).hasSize(4);
        assertNoSelfReview(assignments);
        assertUniqueReviewedTeams(assignments);
        assertUniqueReviewerTeams(assignments);
    }

    @Test
    void createDistributionIfReady_whenRandomCircle_keepsCircleInvariants() {
        task.setPeerReviewDistributionType(PeerReviewDistributionType.RANDOM_CIRCLE);
        List<Team> teams = List.of(team("A", true), team("B", true), team("C", true), team("D", true));
        prepareTeams(teams);

        distributionService.createDistributionIfReady(task);

        List<PeerReviewAssignment> assignments = savedAssignments();
        assertThat(assignments).hasSize(4);
        assertNoSelfReview(assignments);
        assertUniqueReviewedTeams(assignments);
        assertUniqueReviewerTeams(assignments);
        assertEverySubmittedTeamHasOneReviewer(assignments, teams);
    }

    @Test
    void createDistributionIfReady_excludesTeamsWithoutSubmittedSolution() {
        task.setPeerReviewDistributionType(PeerReviewDistributionType.PAIR);
        Team a = team("A", true);
        Team b = team("B", true);
        Team c = team("C", false);
        prepareTeams(List.of(a, b, c));

        distributionService.createDistributionIfReady(task);

        List<PeerReviewAssignment> assignments = savedAssignments();
        assertThat(assignments).hasSize(2);
        assertAssignment(assignments, a, b, PeerReviewAssignmentStatus.ASSIGNED);
        assertAssignment(assignments, b, a, PeerReviewAssignmentStatus.ASSIGNED);
        assertThat(assignments).noneMatch(assignment -> sameTeam(assignment.getReviewedTeam(), c) || sameTeam(assignment.getReviewerTeam(), c));
    }

    @Test
    void createDistributionIfReady_whenManualDistribution_doesNotCreateAssignments() {
        task.setPeerReviewDistributionType(PeerReviewDistributionType.MANUAL);

        distributionService.createDistributionIfReady(task);

        verify(peerReviewAssignmentRepository, never()).saveAll(any());
        verify(teamRepository, never()).findAllByTaskId(taskId);
    }

    @Test
    void createDistributionIfReady_whenAssignmentsAlreadyExist_doesNotRecreateDistribution() {
        task.setPeerReviewDistributionType(PeerReviewDistributionType.CIRCLE);
        when(peerReviewAssignmentRepository.findAllByTaskId(taskId)).thenReturn(List.of(new PeerReviewAssignment()));

        distributionService.createDistributionIfReady(task);

        verify(peerReviewAssignmentRepository, never()).saveAll(any());
        verify(teamRepository, never()).findAllByTaskId(taskId);
    }

    @Test
    void createDistributionIfReady_whenDistributionTypeMissing_throws() {
        assertThatThrownBy(() -> distributionService.createDistributionIfReady(task))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("peerReviewDistributionType is required when peer review is enabled");
    }

    private void prepareTeams(List<Team> teams) {
        when(peerReviewAssignmentRepository.findAllByTaskId(taskId)).thenReturn(List.of());
        when(teamRepository.findAllByTaskId(taskId)).thenReturn(teams);
    }

    private List<PeerReviewAssignment> savedAssignments() {
        ArgumentCaptor<Iterable<PeerReviewAssignment>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(peerReviewAssignmentRepository).saveAll(captor.capture());
        return StreamSupport.stream(captor.getValue().spliterator(), false).toList();
    }

    private Team team(String name, boolean submitted) {
        Team team = new Team();
        team.setId(UUID.randomUUID());
        team.setName(name);
        team.setTask(task);
        if (submitted) {
            Participation participation = new Participation();
            participation.setId(UUID.randomUUID());
            participation.setTeam(team);
            participation.setIsCaptain(true);
            participation.setSolutionStatus(SolutionStatus.LOCKED);
            team.setSolutionParticipation(participation);
        }
        return team;
    }

    private void assertAssignment(List<PeerReviewAssignment> assignments, Team reviewer, Team reviewed, PeerReviewAssignmentStatus status) {
        assertThat(assignments)
                .anySatisfy(assignment -> {
                    assertThat(sameTeam(assignment.getReviewerTeam(), reviewer)).isTrue();
                    assertThat(sameTeam(assignment.getReviewedTeam(), reviewed)).isTrue();
                    assertThat(assignment.getTargetParticipation()).isEqualTo(reviewed.getSolutionParticipation());
                    assertThat(assignment.getStatus()).isEqualTo(status);
                });
    }

    private void assertWithoutReviewer(List<PeerReviewAssignment> assignments, Team reviewed) {
        assertThat(assignments)
                .anySatisfy(assignment -> {
                    assertThat(assignment.getReviewerTeam()).isNull();
                    assertThat(sameTeam(assignment.getReviewedTeam(), reviewed)).isTrue();
                    assertThat(assignment.getTargetParticipation()).isEqualTo(reviewed.getSolutionParticipation());
                    assertThat(assignment.getStatus()).isEqualTo(PeerReviewAssignmentStatus.WITHOUT_REVIEWER);
                });
    }

    private void assertEverySubmittedTeamHasOneReviewer(List<PeerReviewAssignment> assignments, List<Team> teams) {
        for (Team team : teams) {
            assertThat(assignments.stream()
                    .filter(assignment -> sameTeam(assignment.getReviewedTeam(), team))
                    .filter(assignment -> assignment.getReviewerTeam() != null)
                    .count()).isEqualTo(1L);
        }
    }

    private void assertNoSelfReview(List<PeerReviewAssignment> assignments) {
        assertThat(assignments)
                .filteredOn(assignment -> assignment.getReviewerTeam() != null)
                .noneMatch(assignment -> sameTeam(assignment.getReviewerTeam(), assignment.getReviewedTeam()));
    }

    private void assertUniqueReviewedTeams(List<PeerReviewAssignment> assignments) {
        Set<UUID> reviewedTeamIds = new HashSet<>();
        for (PeerReviewAssignment assignment : assignments) {
            assertThat(reviewedTeamIds.add(assignment.getReviewedTeam().getId())).isTrue();
        }
    }

    private void assertUniqueReviewerTeams(List<PeerReviewAssignment> assignments) {
        Set<UUID> reviewerTeamIds = new HashSet<>();
        for (PeerReviewAssignment assignment : assignments) {
            if (assignment.getReviewerTeam() != null) {
                assertThat(reviewerTeamIds.add(assignment.getReviewerTeam().getId())).isTrue();
            }
        }
    }

    private boolean sameTeam(Team first, Team second) {
        return first != null && second != null && first.getId().equals(second.getId());
    }
}
