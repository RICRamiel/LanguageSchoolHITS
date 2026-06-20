package com.hits.language_school_back.infrastructure;

import com.hits.language_school_back.enums.PeerReviewAssignmentStatus;
import com.hits.language_school_back.enums.PeerReviewDistributionType;
import com.hits.language_school_back.model.PeerReviewAssignment;
import com.hits.language_school_back.model.Task;
import com.hits.language_school_back.model.Team;
import com.hits.language_school_back.repository.PeerReviewAssignmentRepository;
import com.hits.language_school_back.repository.TeamRepository;
import com.hits.language_school_back.service.PeerReviewDistributionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PeerReviewDistributionServiceImpl implements PeerReviewDistributionService {
    private final TeamRepository teamRepository;
    private final PeerReviewAssignmentRepository peerReviewAssignmentRepository;

    @Override
    @Transactional
    public void createDistributionIfReady(Task task) {
        if (task == null || !Boolean.TRUE.equals(task.getPeerReviewEnabled())) {
            return;
        }
        if (!Boolean.TRUE.equals(task.getSubmissionClosed())) {
            log.debug("Peer-review distribution for task {} is deferred until submissions are closed", task.getId());
            return;
        }
        if (task.getPeerReviewDistributionType() == null) {
            throw new IllegalArgumentException("peerReviewDistributionType is required when peer review is enabled");
        }
        if (task.getPeerReviewDistributionType() == PeerReviewDistributionType.MANUAL) {
            return;
        }
        if (!peerReviewAssignmentRepository.findAllByTaskId(task.getId()).isEmpty()) {
            log.debug("Peer-review distribution for task {} already exists", task.getId());
            return;
        }

        List<Team> teams = submittedTeams(task);
        List<PeerReviewAssignment> assignments = switch (task.getPeerReviewDistributionType()) {
            case PAIR -> createPairAssignments(task, teams);
            case CIRCLE -> createCircleAssignments(task, teams);
            case RANDOM_PAIR -> createRandomPairAssignments(task, teams);
            case RANDOM_CIRCLE -> createRandomCircleAssignments(task, teams);
            case MANUAL -> List.of();
        };

        if (!assignments.isEmpty()) {
            peerReviewAssignmentRepository.saveAll(assignments);
        }
    }

    private List<Team> submittedTeams(Task task) {
        return teamRepository.findAllByTaskId(task.getId()).stream()
                .filter(team -> team.getSolutionParticipation() != null)
                .sorted(Comparator.comparing(Team::getName, Comparator.nullsLast(String::compareTo))
                        .thenComparing(team -> team.getId() == null ? "" : team.getId().toString()))
                .toList();
    }

    private List<PeerReviewAssignment> createPairAssignments(Task task, List<Team> teams) {
        List<PeerReviewAssignment> assignments = new ArrayList<>();
        for (int i = 0; i + 1 < teams.size(); i += 2) {
            Team first = teams.get(i);
            Team second = teams.get(i + 1);
            assignments.add(assignment(task, first, second));
            assignments.add(assignment(task, second, first));
        }
        if (teams.size() % 2 == 1) {
            assignments.add(withoutReviewer(task, teams.get(teams.size() - 1)));
        }
        return assignments;
    }

    private List<PeerReviewAssignment> createRandomPairAssignments(Task task, List<Team> teams) {
        List<Team> shuffled = new ArrayList<>(teams);
        Collections.shuffle(shuffled);
        return createPairAssignments(task, shuffled);
    }

    private List<PeerReviewAssignment> createCircleAssignments(Task task, List<Team> teams) {
        if (teams.isEmpty()) {
            return List.of();
        }
        if (teams.size() == 1) {
            return List.of(withoutReviewer(task, teams.get(0)));
        }

        List<PeerReviewAssignment> assignments = new ArrayList<>();
        for (int i = 0; i < teams.size(); i++) {
            Team reviewer = teams.get(i);
            Team reviewed = teams.get((i + 1) % teams.size());
            assignments.add(assignment(task, reviewer, reviewed));
        }
        return assignments;
    }

    private List<PeerReviewAssignment> createRandomCircleAssignments(Task task, List<Team> teams) {
        List<Team> shuffled = new ArrayList<>(teams);
        Collections.shuffle(shuffled);
        return createCircleAssignments(task, shuffled);
    }

    private PeerReviewAssignment assignment(Task task, Team reviewer, Team reviewed) {
        PeerReviewAssignment assignment = baseAssignment(task, reviewed);
        assignment.setReviewerTeam(reviewer);
        assignment.setStatus(PeerReviewAssignmentStatus.ASSIGNED);
        return assignment;
    }

    private PeerReviewAssignment withoutReviewer(Task task, Team reviewed) {
        PeerReviewAssignment assignment = baseAssignment(task, reviewed);
        assignment.setStatus(PeerReviewAssignmentStatus.WITHOUT_REVIEWER);
        return assignment;
    }

    private PeerReviewAssignment baseAssignment(Task task, Team reviewed) {
        PeerReviewAssignment assignment = new PeerReviewAssignment();
        assignment.setTask(task);
        assignment.setReviewedTeam(reviewed);
        assignment.setTargetParticipation(reviewed.getSolutionParticipation());
        return assignment;
    }
}
