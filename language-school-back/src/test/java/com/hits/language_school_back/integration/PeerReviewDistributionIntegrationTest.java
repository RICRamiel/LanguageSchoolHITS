package com.hits.language_school_back.integration;

import com.hits.language_school_back.enums.PeerReviewAssignmentStatus;
import com.hits.language_school_back.enums.PeerReviewDistributionType;
import com.hits.language_school_back.enums.Role;
import com.hits.language_school_back.enums.SolutionStatus;
import com.hits.language_school_back.enums.TaskResolveType;
import com.hits.language_school_back.enums.TeamType;
import com.hits.language_school_back.infrastructure.PeerReviewDistributionServiceImpl;
import com.hits.language_school_back.model.Course;
import com.hits.language_school_back.model.Language;
import com.hits.language_school_back.model.Participation;
import com.hits.language_school_back.model.PeerReviewAssignment;
import com.hits.language_school_back.model.Task;
import com.hits.language_school_back.model.Team;
import com.hits.language_school_back.model.User;
import com.hits.language_school_back.repository.CourseRepository;
import com.hits.language_school_back.repository.LanguageRepository;
import com.hits.language_school_back.repository.ParticipationRepository;
import com.hits.language_school_back.repository.PeerReviewAssignmentRepository;
import com.hits.language_school_back.repository.TaskRepository;
import com.hits.language_school_back.repository.TeamRepository;
import com.hits.language_school_back.repository.UserRepository;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PeerReviewDistributionIntegrationTest {

    @MockitoBean
    private MinioClient minioClient;

    @Autowired
    private PeerReviewDistributionServiceImpl distributionService;
    @Autowired
    private PeerReviewAssignmentRepository assignmentRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private ParticipationRepository participationRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private LanguageRepository languageRepository;
    @Autowired
    private UserRepository userRepository;

    private Task task;

    @BeforeEach
    void setUp() {
        Language language = new Language();
        language.setName("English");
        languageRepository.save(language);

        User teacher = User.builder()
                .firstName("Teacher")
                .lastName("Test")
                .email("teacher@integration.test")
                .password("pass")
                .role(Role.TEACHER)
                .build();
        userRepository.save(teacher);

        Course course = Course.builder()
                .name("Integration Test Course")
                .description("Course for integration tests")
                .satisfactorilyMarkThreshold(50)
                .goodMarkThreshold(70)
                .excellentMarkThreshold(90)
                .language(language)
                .teacher(teacher)
                .build();
        courseRepository.save(course);

        task = Task.builder()
                .name("Peer Task")
                .description("Integration test task")
                .course(course)
                .createdBy(teacher)
                .teamType(TeamType.FREEROAM)
                .resolveType(TaskResolveType.FIRST_SUBMITTED_SOLUTION)
                .submissionClosed(true)
                .peerReviewEnabled(true)
                .build();
        taskRepository.save(task);
    }

    @Test
    void pairDistribution_withEvenTeams_savesMutualPairsToDatabase() {
        task.setPeerReviewDistributionType(PeerReviewDistributionType.PAIR);
        taskRepository.save(task);

        Team teamA = createTeamWithSolution("Alpha");
        Team teamB = createTeamWithSolution("Beta");

        distributionService.createDistributionIfReady(task);

        List<PeerReviewAssignment> assignments = assignmentRepository.findAllByTaskId(task.getId());
        assertThat(assignments).hasSize(2);
        assertNoSelfReview(assignments);
        assertMutualPairing(assignments, teamA, teamB);
        assertThat(assignments).allMatch(a -> a.getStatus() == PeerReviewAssignmentStatus.ASSIGNED);
    }

    @Test
    void pairDistribution_withOddTeams_createsWithoutReviewerAssignmentInDatabase() {
        task.setPeerReviewDistributionType(PeerReviewDistributionType.PAIR);
        taskRepository.save(task);

        createTeamWithSolution("Alpha");
        createTeamWithSolution("Beta");
        createTeamWithSolution("Gamma");

        distributionService.createDistributionIfReady(task);

        List<PeerReviewAssignment> assignments = assignmentRepository.findAllByTaskId(task.getId());
        assertThat(assignments).hasSize(3);
        assertThat(assignments)
                .filteredOn(a -> a.getStatus() == PeerReviewAssignmentStatus.WITHOUT_REVIEWER)
                .hasSize(1);
        assertThat(assignments)
                .filteredOn(a -> a.getStatus() == PeerReviewAssignmentStatus.ASSIGNED)
                .hasSize(2);
        assertNoSelfReview(assignments);
    }

    @Test
    void circleDistribution_savesFullCircularChainToDatabase() {
        task.setPeerReviewDistributionType(PeerReviewDistributionType.CIRCLE);
        taskRepository.save(task);

        createTeamWithSolution("Alpha");
        createTeamWithSolution("Beta");
        createTeamWithSolution("Gamma");

        distributionService.createDistributionIfReady(task);

        List<PeerReviewAssignment> assignments = assignmentRepository.findAllByTaskId(task.getId());
        assertThat(assignments).hasSize(3);
        assertNoSelfReview(assignments);
        assertUniqueReviewedTeams(assignments);
        assertUniqueReviewerTeams(assignments);
        assertThat(assignments).allMatch(a -> a.getStatus() == PeerReviewAssignmentStatus.ASSIGNED);
    }

    @Test
    void randomPairDistribution_keepsInvariantsInDatabase() {
        task.setPeerReviewDistributionType(PeerReviewDistributionType.RANDOM_PAIR);
        taskRepository.save(task);

        IntStream.range(0, 5)
                .forEach(i -> createTeamWithSolution(String.valueOf((char) ('A' + i))));

        distributionService.createDistributionIfReady(task);

        List<PeerReviewAssignment> assignments = assignmentRepository.findAllByTaskId(task.getId());
        assertThat(assignments).hasSize(5);
        assertThat(assignments)
                .filteredOn(a -> a.getStatus() == PeerReviewAssignmentStatus.WITHOUT_REVIEWER)
                .hasSize(1);
        assertNoSelfReview(assignments);
        assertUniqueReviewerTeams(assignments);
        assertUniqueReviewedTeams(assignments);
    }

    @Test
    void randomCircleDistribution_keepsInvariantsInDatabase() {
        task.setPeerReviewDistributionType(PeerReviewDistributionType.RANDOM_CIRCLE);
        taskRepository.save(task);

        IntStream.range(0, 4)
                .forEach(i -> createTeamWithSolution(String.valueOf((char) ('A' + i))));

        distributionService.createDistributionIfReady(task);

        List<PeerReviewAssignment> assignments = assignmentRepository.findAllByTaskId(task.getId());
        assertThat(assignments).hasSize(4);
        assertNoSelfReview(assignments);
        assertUniqueReviewedTeams(assignments);
        assertUniqueReviewerTeams(assignments);
        assertThat(assignments).allMatch(a -> a.getStatus() == PeerReviewAssignmentStatus.ASSIGNED);
    }

    @Test
    void teamsWithoutSubmittedSolution_areExcludedFromDistribution() {
        task.setPeerReviewDistributionType(PeerReviewDistributionType.PAIR);
        taskRepository.save(task);

        createTeamWithSolution("Alpha");
        createTeamWithSolution("Beta");
        Team teamNoSolution = createTeamWithoutSolution("Gamma");

        distributionService.createDistributionIfReady(task);

        List<PeerReviewAssignment> assignments = assignmentRepository.findAllByTaskId(task.getId());
        assertThat(assignments).hasSize(2);
        assertThat(assignments).noneMatch(a ->
                teamNoSolution.getId().equals(a.getReviewedTeam() != null ? a.getReviewedTeam().getId() : null)
                        || teamNoSolution.getId().equals(a.getReviewerTeam() != null ? a.getReviewerTeam().getId() : null));
    }

    @Test
    void distributionIsIdempotent_secondCallDoesNotRecreateAssignments() {
        task.setPeerReviewDistributionType(PeerReviewDistributionType.CIRCLE);
        taskRepository.save(task);

        createTeamWithSolution("Alpha");
        createTeamWithSolution("Beta");

        distributionService.createDistributionIfReady(task);
        long countAfterFirst = assignmentRepository.findAllByTaskId(task.getId()).size();

        distributionService.createDistributionIfReady(task);
        long countAfterSecond = assignmentRepository.findAllByTaskId(task.getId()).size();

        assertThat(countAfterSecond).isEqualTo(countAfterFirst);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private Team createTeamWithSolution(String name) {
        User student = User.builder()
                .firstName("Student")
                .lastName(name)
                .email(name.toLowerCase() + "-" + UUID.randomUUID() + "@integration.test")
                .password("pass")
                .role(Role.STUDENT)
                .build();
        userRepository.save(student);

        Team team = new Team();
        team.setName(name + "-" + task.getId());
        team.setTask(task);
        teamRepository.save(team);

        Participation participation = new Participation();
        participation.setIsCaptain(true);
        participation.setStudent(student);
        participation.setTeam(team);
        participation.setSolutionStatus(SolutionStatus.LOCKED);
        participationRepository.save(participation);

        team.setSolutionParticipation(participation);
        teamRepository.save(team);

        return team;
    }

    private Team createTeamWithoutSolution(String name) {
        Team team = new Team();
        team.setName(name + "-nosolution-" + task.getId());
        team.setTask(task);
        teamRepository.save(team);
        return team;
    }

    private void assertNoSelfReview(List<PeerReviewAssignment> assignments) {
        assertThat(assignments)
                .filteredOn(a -> a.getReviewerTeam() != null)
                .noneMatch(a -> a.getReviewerTeam().getId().equals(a.getReviewedTeam().getId()));
    }

    private void assertMutualPairing(List<PeerReviewAssignment> assignments, Team teamA, Team teamB) {
        boolean aReviewsB = assignments.stream().anyMatch(a ->
                a.getReviewerTeam() != null
                        && a.getReviewerTeam().getId().equals(teamA.getId())
                        && a.getReviewedTeam().getId().equals(teamB.getId()));
        boolean bReviewsA = assignments.stream().anyMatch(a ->
                a.getReviewerTeam() != null
                        && a.getReviewerTeam().getId().equals(teamB.getId())
                        && a.getReviewedTeam().getId().equals(teamA.getId()));
        assertThat(aReviewsB).as("Team A should review Team B").isTrue();
        assertThat(bReviewsA).as("Team B should review Team A").isTrue();
    }

    private void assertUniqueReviewedTeams(List<PeerReviewAssignment> assignments) {
        Set<UUID> ids = new HashSet<>();
        for (PeerReviewAssignment a : assignments) {
            assertThat(ids.add(a.getReviewedTeam().getId())).isTrue();
        }
    }

    private void assertUniqueReviewerTeams(List<PeerReviewAssignment> assignments) {
        Set<UUID> ids = new HashSet<>();
        for (PeerReviewAssignment a : assignments) {
            if (a.getReviewerTeam() != null) {
                assertThat(ids.add(a.getReviewerTeam().getId())).isTrue();
            }
        }
    }
}
