package com.hits.language_school_back.bdd;

import com.hits.language_school_back.dto.AssessmentItemRequestDTO;
import com.hits.language_school_back.dto.AssessmentSubmitDTO;
import com.hits.language_school_back.dto.PeerReviewManualAssignmentDTO;
import com.hits.language_school_back.dto.PeerReviewResultDTO;
import com.hits.language_school_back.dto.PeerReviewResultsDTO;
import com.hits.language_school_back.dto.PeerReviewSettingsDTO;
import com.hits.language_school_back.enums.AssessmentStatus;
import com.hits.language_school_back.enums.AssessmentType;
import com.hits.language_school_back.enums.PeerReviewAssignmentStatus;
import com.hits.language_school_back.enums.PeerReviewDistributionType;
import com.hits.language_school_back.enums.Role;
import com.hits.language_school_back.enums.SolutionStatus;
import com.hits.language_school_back.infrastructure.PeerReviewDistributionServiceImpl;
import com.hits.language_school_back.infrastructure.PeerReviewServiceImpl;
import com.hits.language_school_back.model.Assessment;
import com.hits.language_school_back.model.AssessmentItem;
import com.hits.language_school_back.model.Course;
import com.hits.language_school_back.model.PeerReviewAssignment;
import com.hits.language_school_back.model.Participation;
import com.hits.language_school_back.model.Task;
import com.hits.language_school_back.model.TaskCriterion;
import com.hits.language_school_back.model.Team;
import com.hits.language_school_back.model.User;
import com.hits.language_school_back.repository.AssessmentItemRepository;
import com.hits.language_school_back.repository.AssessmentRepository;
import com.hits.language_school_back.repository.ParticipationRepository;
import com.hits.language_school_back.repository.PeerReviewAssignmentRepository;
import com.hits.language_school_back.repository.StudentsInCourseRepository;
import com.hits.language_school_back.repository.TaskCriterionRepository;
import com.hits.language_school_back.repository.TaskRepository;
import com.hits.language_school_back.repository.TeamRepository;
import com.hits.language_school_back.service.PeerReviewDistributionService;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PeerReviewSteps {

    private TeamRepository teamRepository;
    private PeerReviewAssignmentRepository peerReviewAssignmentRepository;
    private PeerReviewDistributionServiceImpl distributionService;

    private TaskRepository taskRepository;
    private ParticipationRepository participationRepository;
    private TaskCriterionRepository taskCriterionRepository;
    private AssessmentRepository assessmentRepository;
    private AssessmentItemRepository assessmentItemRepository;
    private StudentsInCourseRepository studentsInCourseRepository;
    private PeerReviewDistributionService peerReviewDistributionServiceMock;
    private PeerReviewServiceImpl peerReviewService;

    private UUID taskId;
    private UUID teacherId;
    private UUID studentId;
    private Task task;
    private List<Team> teams;
    private List<PeerReviewAssignment> capturedAssignments;
    private Exception thrownException;
    private Team reviewerTeam;
    private Team reviewedTeam;
    private Participation targetParticipation;
    private PeerReviewAssignment existingAssignment;
    private TaskCriterion criterion;
    private PeerReviewResultDTO editResult;
    private PeerReviewResultsDTO confirmResult;
    private PeerReviewSettingsDTO settingsResult;

    @Before
    public void setUp() {
        teamRepository = mock(TeamRepository.class);
        peerReviewAssignmentRepository = mock(PeerReviewAssignmentRepository.class);
        distributionService = new PeerReviewDistributionServiceImpl(teamRepository, peerReviewAssignmentRepository);

        taskRepository = mock(TaskRepository.class);
        participationRepository = mock(ParticipationRepository.class);
        taskCriterionRepository = mock(TaskCriterionRepository.class);
        assessmentRepository = mock(AssessmentRepository.class);
        assessmentItemRepository = mock(AssessmentItemRepository.class);
        studentsInCourseRepository = mock(StudentsInCourseRepository.class);
        peerReviewDistributionServiceMock = mock(PeerReviewDistributionService.class);
        peerReviewService = new PeerReviewServiceImpl(
                taskRepository, teamRepository, participationRepository,
                taskCriterionRepository, assessmentRepository, assessmentItemRepository,
                studentsInCourseRepository, peerReviewAssignmentRepository, peerReviewDistributionServiceMock
        );

        taskId = UUID.randomUUID();
        teacherId = UUID.randomUUID();
        studentId = UUID.randomUUID();

        User teacher = User.builder()
                .id(teacherId)
                .role(Role.TEACHER)
                .firstName("Teacher")
                .lastName("Test")
                .email("teacher@test.com")
                .password("pass")
                .build();

        Course course = Course.builder()
                .id(UUID.randomUUID())
                .teacher(teacher)
                .build();

        task = Task.builder()
                .id(taskId)
                .name("Peer Task")
                .description("Description")
                .course(course)
                .createdBy(teacher)
                .peerReviewEnabled(true)
                .peerReviewerVisibleToTeams(false)
                .submissionClosed(true)
                .build();

        teams = new ArrayList<>();
        capturedAssignments = null;
        thrownException = null;
        existingAssignment = null;
        reviewerTeam = null;
        reviewedTeam = null;
        targetParticipation = null;
        criterion = null;
        editResult = null;
        confirmResult = null;
        settingsResult = null;
    }

    // ─── Distribution setup ───────────────────────────────────────────────────

    @Given("задача с {string} распределением peer-оценки")
    public void taskWithDistribution(String distributionType) {
        task.setPeerReviewDistributionType(PeerReviewDistributionType.valueOf(distributionType));
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    }

    @Given("задача с peer-оценкой включена")
    public void taskWithPeerReviewEnabled() {
        task.setPeerReviewDistributionType(PeerReviewDistributionType.PAIR);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
    }

    @And("{int} команды сдали решение")
    public void nTeamsSubmittedSolution(int count) {
        teams = IntStream.range(0, count)
                .mapToObj(i -> teamWithSolution(String.valueOf((char) ('A' + i))))
                .collect(Collectors.toList());
    }

    @And("5 команд сдали решение")
    public void fiveTeamsSubmittedSolution() {
        nTeamsSubmittedSolution(5);
    }

    // ─── Distribution action ──────────────────────────────────────────────────

    @When("запускается автоматическое распределение")
    public void runAutoDistribution() {
        when(peerReviewAssignmentRepository.findAllByTaskId(taskId)).thenReturn(List.of());
        when(teamRepository.findAllByTaskId(taskId)).thenReturn(teams);
        when(peerReviewAssignmentRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        distributionService.createDistributionIfReady(task);

        ArgumentCaptor<Iterable<PeerReviewAssignment>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(peerReviewAssignmentRepository).saveAll(captor.capture());
        capturedAssignments = StreamSupport.stream(captor.getValue().spliterator(), false).toList();
    }

    // ─── Distribution assertions ──────────────────────────────────────────────

    @Then("создаётся {int} назначения")
    public void assignmentCountIs(int expected) {
        assertThat(capturedAssignments).hasSize(expected);
    }

    @And("каждая команда оценивается ровно одним оценщиком")
    public void eachTeamHasExactlyOneReviewer() {
        Set<UUID> reviewedIds = new HashSet<>();
        for (PeerReviewAssignment a : capturedAssignments) {
            assertThat(reviewedIds.add(a.getReviewedTeam().getId()))
                    .as("Reviewed team %s appears more than once", a.getReviewedTeam().getName())
                    .isTrue();
        }
        long withReviewer = capturedAssignments.stream()
                .filter(a -> a.getReviewerTeam() != null).count();
        assertThat(withReviewer).isEqualTo(capturedAssignments.size());
    }

    @And("ни одна команда не оценивает саму себя")
    public void noSelfReview() {
        assertThat(capturedAssignments)
                .filteredOn(a -> a.getReviewerTeam() != null)
                .noneMatch(a -> a.getReviewerTeam().getId().equals(a.getReviewedTeam().getId()));
    }

    @Then("одна команда остаётся без оценщика")
    public void oneTeamWithoutReviewer() {
        assertThat(capturedAssignments)
                .filteredOn(a -> a.getStatus() == PeerReviewAssignmentStatus.WITHOUT_REVIEWER)
                .hasSize(1);
    }

    @And("у каждого оценщика только одно назначение")
    public void uniqueReviewerAssignments() {
        Set<UUID> reviewerIds = new HashSet<>();
        for (PeerReviewAssignment a : capturedAssignments) {
            if (a.getReviewerTeam() != null) {
                assertThat(reviewerIds.add(a.getReviewerTeam().getId()))
                        .as("Reviewer team %s has more than one assignment", a.getReviewerTeam().getName())
                        .isTrue();
            }
        }
    }

    // ─── Without-reviewer warning ─────────────────────────────────────────────

    @And("существует назначение без оценщика для команды {string}")
    public void withoutReviewerAssignmentForTeam(String teamName) {
        Team lonelyTeam = teamWithSolution(teamName);

        PeerReviewAssignment withoutReviewer = new PeerReviewAssignment();
        withoutReviewer.setId(UUID.randomUUID());
        withoutReviewer.setTask(task);
        withoutReviewer.setReviewedTeam(lonelyTeam);
        withoutReviewer.setTargetParticipation(lonelyTeam.getSolutionParticipation());
        withoutReviewer.setStatus(PeerReviewAssignmentStatus.WITHOUT_REVIEWER);

        when(peerReviewAssignmentRepository.findAllByTaskId(taskId)).thenReturn(List.of(withoutReviewer));
    }

    @When("преподаватель запрашивает настройки peer-оценки")
    public void teacherGetsSettings() {
        settingsResult = peerReviewService.getPeerReviewSettings(taskId, teacherId);
    }

    @Then("ответ содержит предупреждение о команде без оценщика")
    public void responseContainsWithoutReviewerWarning() {
        assertThat(settingsResult.getHasTeamsWithoutReviewer()).isTrue();
        assertThat(settingsResult.getTeamsWithoutReviewer()).isNotEmpty();
    }

    // ─── Manual assignment — self-review ──────────────────────────────────────

    @When("преподаватель назначает команду ревьюером самой себя")
    public void teacherAssignsSelfReview() {
        reviewerTeam = teamWithSolution("Alpha");
        when(teamRepository.findByIdAndTaskId(reviewerTeam.getId(), taskId)).thenReturn(Optional.of(reviewerTeam));

        try {
            peerReviewService.assignManualPeerReview(
                    taskId,
                    PeerReviewManualAssignmentDTO.builder()
                            .reviewerTeamId(reviewerTeam.getId())
                            .reviewedTeamId(reviewerTeam.getId())
                            .build(),
                    teacherId
            );
        } catch (Exception e) {
            thrownException = e;
        }
    }

    // ─── Manual assignment — duplicate reviewer ───────────────────────────────

    @And("команда уже имеет назначенного ревьюера")
    public void teamAlreadyHasReviewer() {
        reviewerTeam = teamWithSolution("Alpha");
        reviewedTeam = teamWithSolution("Beta");
        targetParticipation = reviewedTeam.getSolutionParticipation();

        Team otherReviewer = teamWithSolution("Gamma");
        PeerReviewAssignment existing = new PeerReviewAssignment();
        existing.setId(UUID.randomUUID());
        existing.setTask(task);
        existing.setReviewerTeam(otherReviewer);
        existing.setReviewedTeam(reviewedTeam);
        existing.setTargetParticipation(targetParticipation);
        existing.setStatus(PeerReviewAssignmentStatus.ASSIGNED);

        when(teamRepository.findByIdAndTaskId(reviewerTeam.getId(), taskId)).thenReturn(Optional.of(reviewerTeam));
        when(teamRepository.findByIdAndTaskId(reviewedTeam.getId(), taskId)).thenReturn(Optional.of(reviewedTeam));
        when(peerReviewAssignmentRepository.findByTaskIdAndReviewedTeamId(taskId, reviewedTeam.getId()))
                .thenReturn(Optional.of(existing));
    }

    @When("преподаватель назначает второго ревьюера той же команде")
    public void teacherAssignsSecondReviewer() {
        try {
            peerReviewService.assignManualPeerReview(
                    taskId,
                    PeerReviewManualAssignmentDTO.builder()
                            .reviewerTeamId(reviewerTeam.getId())
                            .reviewedTeamId(reviewedTeam.getId())
                            .build(),
                    teacherId
            );
        } catch (Exception e) {
            thrownException = e;
        }
    }

    // ─── Student submission — captain-only & no re-submit ────────────────────

    @Given("задача с peer-оценкой включена и назначением для команды-ревьюера")
    public void taskWithPeerReviewAndAssignment() {
        task.setPeerReviewDistributionType(PeerReviewDistributionType.PAIR);
        reviewerTeam = teamWithSolution("Reviewer");
        reviewedTeam = teamWithSolution("Reviewed");
        targetParticipation = reviewedTeam.getSolutionParticipation();

        existingAssignment = new PeerReviewAssignment();
        existingAssignment.setId(UUID.randomUUID());
        existingAssignment.setTask(task);
        existingAssignment.setReviewerTeam(reviewerTeam);
        existingAssignment.setReviewedTeam(reviewedTeam);
        existingAssignment.setTargetParticipation(targetParticipation);
        existingAssignment.setStatus(PeerReviewAssignmentStatus.ASSIGNED);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(peerReviewAssignmentRepository.findByTaskIdAndReviewerTeamId(taskId, reviewerTeam.getId()))
                .thenReturn(Optional.of(existingAssignment));
    }

    @And("студент является рядовым участником команды-ревьюера")
    public void studentIsNonCaptainMember() {
        Participation member = new Participation();
        member.setId(UUID.randomUUID());
        member.setTeam(reviewerTeam);
        member.setStudent(User.builder().id(studentId).role(Role.STUDENT)
                .firstName("S").lastName("T").email("s@t.com").password("p").build());
        member.setIsCaptain(false);
        member.setSolutionStatus(SolutionStatus.DRAFT);
        when(participationRepository.findAllByTeamTaskId(taskId)).thenReturn(List.of(member));
    }

    @When("студент пытается отправить peer-оценку")
    public void studentTriesToSubmit() {
        try {
            peerReviewService.submitMyPeerReviewAssignment(
                    taskId,
                    assessmentPayload(UUID.randomUUID(), 7, "Good"),
                    studentId
            );
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @And("назначение уже отправлено капитаном")
    public void assignmentAlreadySubmitted() {
        User captain = User.builder().id(studentId).role(Role.STUDENT)
                .firstName("C").lastName("T").email("c@t.com").password("p").build();
        Assessment submitted = peerAssessment(targetParticipation, captain, 8);
        existingAssignment.setStatus(PeerReviewAssignmentStatus.SUBMITTED);
        existingAssignment.setAssessment(submitted);

        Participation captainParticipation = captainParticipation(reviewerTeam, studentId);
        when(participationRepository.findAllByTeamTaskId(taskId)).thenReturn(List.of(captainParticipation));
    }

    @When("капитан пытается отправить оценку повторно")
    public void captainTriesToResubmit() {
        try {
            peerReviewService.submitMyPeerReviewAssignment(
                    taskId,
                    assessmentPayload(UUID.randomUUID(), 9, "Changed"),
                    studentId
            );
        } catch (Exception e) {
            thrownException = e;
        }
    }

    // ─── Teacher edit ─────────────────────────────────────────────────────────

    @Given("задача с peer-оценкой и отправленным назначением")
    public void taskWithSubmittedAssignment() {
        taskWithSubmittedAssignmentAndScore(8);
    }

    @Given("задача с peer-оценкой и отправленным назначением с баллом {int}")
    public void taskWithSubmittedAssignmentAndScore(int score) {
        task.setPeerReviewDistributionType(PeerReviewDistributionType.PAIR);
        reviewerTeam = teamWithSolution("Reviewer");
        reviewedTeam = teamWithSolution("Reviewed");
        targetParticipation = reviewedTeam.getSolutionParticipation();
        criterion = criterion("Architecture", 10);

        User captain = User.builder().id(studentId).role(Role.STUDENT)
                .firstName("C").lastName("T").email("c@t.com").password("p").build();
        Assessment assessment = peerAssessment(targetParticipation, captain, score);
        AssessmentItem item = assessmentItem(assessment, criterion, score, "Original");

        existingAssignment = new PeerReviewAssignment();
        existingAssignment.setId(UUID.randomUUID());
        existingAssignment.setTask(task);
        existingAssignment.setReviewerTeam(reviewerTeam);
        existingAssignment.setReviewedTeam(reviewedTeam);
        existingAssignment.setTargetParticipation(targetParticipation);
        existingAssignment.setStatus(PeerReviewAssignmentStatus.SUBMITTED);
        existingAssignment.setAssessment(assessment);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(peerReviewAssignmentRepository.findById(existingAssignment.getId()))
                .thenReturn(Optional.of(existingAssignment));
        when(peerReviewAssignmentRepository.findAllByTaskId(taskId))
                .thenReturn(List.of(existingAssignment));
        when(taskCriterionRepository.findAllByTaskIdAndActiveTrueOrderByOrderIndexAscTitleAsc(taskId))
                .thenReturn(List.of(criterion));
        when(assessmentItemRepository.findAllByAssessmentId(assessment.getId())).thenReturn(List.of(item));
        when(assessmentItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(assessmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(peerReviewAssignmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(taskRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(participationRepository.findAllByTeamId(reviewedTeam.getId()))
                .thenReturn(List.of(targetParticipation));
    }

    @When("преподаватель редактирует peer-оценку")
    public void teacherEditsPeerAssessment() {
        editResult = peerReviewService.editPeerReviewAssessment(
                taskId,
                existingAssignment.getId(),
                assessmentPayload(criterion.getId(), 9, "Teacher correction"),
                teacherId
        );
    }

    @Then("статус назначения становится {string}")
    public void assignmentStatusBecomes(String expectedStatus) {
        PeerReviewAssignmentStatus status = PeerReviewAssignmentStatus.valueOf(expectedStatus);
        if (editResult != null) {
            assertThat(editResult.getStatus()).isEqualTo(status);
        } else if (confirmResult != null) {
            assertThat(confirmResult.getResults())
                    .allSatisfy(r -> assertThat(r.getStatus()).isEqualTo(status));
        }
    }

    @And("преподаватель сохраняется как редактор")
    public void teacherSavedAsEditor() {
        assertThat(editResult.getAssignment().getTeacherEditorId()).isEqualTo(teacherId);
        assertThat(editResult.getAssignment().getTeacherEditedAt()).isNotNull();
    }

    // ─── Confirm peer review ──────────────────────────────────────────────────

    @When("преподаватель подтверждает результаты peer-оценки")
    public void teacherConfirmsPeerReview() {
        confirmResult = peerReviewService.confirmPeerReviewResults(taskId, teacherId);
    }

    @Then("итоговая оценка команды становится {int}")
    public void teamFinalScoreIs(int expectedScore) {
        assertThat(reviewedTeam.getCommandMark()).isEqualTo(expectedScore);
    }

    // ─── Shared error assertion ───────────────────────────────────────────────

    @Then("запрос отклоняется с сообщением {string}")
    public void requestRejectedWithMessage(String expectedMessage) {
        assertThat(thrownException)
                .isNotNull()
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(expectedMessage);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private Team teamWithSolution(String name) {
        Team team = new Team();
        team.setId(UUID.randomUUID());
        team.setName(name);
        team.setTask(task);

        User student = User.builder()
                .id(UUID.randomUUID())
                .role(Role.STUDENT)
                .firstName("S")
                .lastName(name)
                .email(name.toLowerCase() + UUID.randomUUID() + "@test.com")
                .password("pass")
                .build();

        Participation participation = new Participation();
        participation.setId(UUID.randomUUID());
        participation.setTeam(team);
        participation.setStudent(student);
        participation.setIsCaptain(true);
        participation.setSolutionStatus(SolutionStatus.LOCKED);
        team.setSolutionParticipation(participation);
        return team;
    }

    private Participation captainParticipation(Team team, UUID userId) {
        Participation participation = new Participation();
        participation.setId(UUID.randomUUID());
        participation.setTeam(team);
        participation.setStudent(User.builder()
                .id(userId)
                .role(Role.STUDENT)
                .firstName("C").lastName("T").email("c@t.com").password("p")
                .build());
        participation.setIsCaptain(true);
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

    private Assessment peerAssessment(Participation participation, User assessor, int totalPoints) {
        Assessment assessment = new Assessment();
        assessment.setId(UUID.randomUUID());
        assessment.setTask(task);
        assessment.setParticipation(participation);
        assessment.setAssessor(assessor);
        assessment.setType(AssessmentType.PEER);
        assessment.setStatus(AssessmentStatus.SUBMITTED);
        assessment.setTotalPoints(totalPoints);
        assessment.setCreatedAt(LocalDateTime.now());
        assessment.setUpdatedAt(LocalDateTime.now());
        return assessment;
    }

    private AssessmentItem assessmentItem(Assessment assessment, TaskCriterion crit, int points, String comment) {
        AssessmentItem item = new AssessmentItem();
        item.setId(UUID.randomUUID());
        item.setAssessment(assessment);
        item.setCriterion(crit);
        item.setPoints(points);
        item.setComment(comment);
        return item;
    }

    private AssessmentSubmitDTO assessmentPayload(UUID criterionId, int points, String comment) {
        return AssessmentSubmitDTO.builder()
                .items(List.of(AssessmentItemRequestDTO.builder()
                        .criterionId(criterionId)
                        .points(points)
                        .comment(comment)
                        .build()))
                .build();
    }
}
