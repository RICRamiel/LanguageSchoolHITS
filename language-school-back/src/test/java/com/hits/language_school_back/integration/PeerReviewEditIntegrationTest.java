package com.hits.language_school_back.integration;

import com.hits.language_school_back.dto.AssessmentItemRequestDTO;
import com.hits.language_school_back.dto.AssessmentSubmitDTO;
import com.hits.language_school_back.dto.PeerReviewResultDTO;
import com.hits.language_school_back.enums.AssessmentStatus;
import com.hits.language_school_back.enums.AssessmentType;
import com.hits.language_school_back.enums.PeerReviewAssignmentStatus;
import com.hits.language_school_back.enums.PeerReviewDistributionType;
import com.hits.language_school_back.enums.Role;
import com.hits.language_school_back.enums.SolutionStatus;
import com.hits.language_school_back.enums.TaskResolveType;
import com.hits.language_school_back.enums.TeamType;
import com.hits.language_school_back.infrastructure.PeerReviewServiceImpl;
import com.hits.language_school_back.model.Assessment;
import com.hits.language_school_back.model.AssessmentItem;
import com.hits.language_school_back.model.Course;
import com.hits.language_school_back.model.Language;
import com.hits.language_school_back.model.Participation;
import com.hits.language_school_back.model.PeerReviewAssignment;
import com.hits.language_school_back.model.Task;
import com.hits.language_school_back.model.TaskCriterion;
import com.hits.language_school_back.model.Team;
import com.hits.language_school_back.model.User;
import com.hits.language_school_back.repository.AssessmentItemRepository;
import com.hits.language_school_back.repository.AssessmentRepository;
import com.hits.language_school_back.repository.CourseRepository;
import com.hits.language_school_back.repository.LanguageRepository;
import com.hits.language_school_back.repository.ParticipationRepository;
import com.hits.language_school_back.repository.PeerReviewAssignmentRepository;
import com.hits.language_school_back.repository.TaskCriterionRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PeerReviewEditIntegrationTest {

    @MockitoBean
    private MinioClient minioClient;

    @Autowired private PeerReviewServiceImpl peerReviewService;
    @Autowired private PeerReviewAssignmentRepository assignmentRepository;
    @Autowired private AssessmentRepository assessmentRepository;
    @Autowired private AssessmentItemRepository assessmentItemRepository;
    @Autowired private TaskCriterionRepository taskCriterionRepository;
    @Autowired private TeamRepository teamRepository;
    @Autowired private TaskRepository taskRepository;
    @Autowired private ParticipationRepository participationRepository;
    @Autowired private CourseRepository courseRepository;
    @Autowired private LanguageRepository languageRepository;
    @Autowired private UserRepository userRepository;

    private Task task;
    private User teacher;
    private TaskCriterion criterion;
    private Team reviewerTeam;
    private Team reviewedTeam;
    private PeerReviewAssignment assignment;

    @BeforeEach
    void setUp() {
        Language language = new Language();
        language.setName("English-edit");
        languageRepository.save(language);

        teacher = User.builder()
                .firstName("Teacher")
                .lastName("Edit")
                .email("teacher-edit-" + UUID.randomUUID() + "@integration.test")
                .password("pass")
                .role(Role.TEACHER)
                .build();
        userRepository.save(teacher);

        Course course = Course.builder()
                .name("Edit Integration Test Course")
                .description("Course for edit tests")
                .satisfactorilyMarkThreshold(50)
                .goodMarkThreshold(70)
                .excellentMarkThreshold(90)
                .language(language)
                .teacher(teacher)
                .build();
        courseRepository.save(course);

        task = Task.builder()
                .name("Peer Edit Task")
                .description("Integration test edit task")
                .course(course)
                .createdBy(teacher)
                .teamType(TeamType.FREEROAM)
                .resolveType(TaskResolveType.FIRST_SUBMITTED_SOLUTION)
                .submissionClosed(true)
                .peerReviewEnabled(true)
                .peerReviewDistributionType(PeerReviewDistributionType.PAIR)
                .build();
        taskRepository.save(task);

        criterion = new TaskCriterion();
        criterion.setTask(task);
        criterion.setTitle("Code Quality");
        criterion.setDescription("Quality of the submitted code");
        criterion.setMaxPoints(10);
        criterion.setSectionName("Technical");
        criterion.setOrderIndex(1);
        criterion.setActive(true);
        taskCriterionRepository.save(criterion);

        User reviewerStudent = User.builder()
                .firstName("Reviewer")
                .lastName("Student")
                .email("reviewer-" + UUID.randomUUID() + "@integration.test")
                .password("pass")
                .role(Role.STUDENT)
                .build();
        userRepository.save(reviewerStudent);

        User reviewedStudent = User.builder()
                .firstName("Reviewed")
                .lastName("Student")
                .email("reviewed-" + UUID.randomUUID() + "@integration.test")
                .password("pass")
                .role(Role.STUDENT)
                .build();
        userRepository.save(reviewedStudent);

        reviewerTeam = new Team();
        reviewerTeam.setName("ReviewerTeam-" + task.getId());
        reviewerTeam.setTask(task);
        teamRepository.save(reviewerTeam);

        reviewedTeam = new Team();
        reviewedTeam.setName("ReviewedTeam-" + task.getId());
        reviewedTeam.setTask(task);
        teamRepository.save(reviewedTeam);

        Participation reviewerParticipation = new Participation();
        reviewerParticipation.setIsCaptain(true);
        reviewerParticipation.setStudent(reviewerStudent);
        reviewerParticipation.setTeam(reviewerTeam);
        reviewerParticipation.setSolutionStatus(SolutionStatus.LOCKED);
        participationRepository.save(reviewerParticipation);

        Participation targetParticipation = new Participation();
        targetParticipation.setIsCaptain(true);
        targetParticipation.setStudent(reviewedStudent);
        targetParticipation.setTeam(reviewedTeam);
        targetParticipation.setSolutionStatus(SolutionStatus.LOCKED);
        participationRepository.save(targetParticipation);

        reviewerTeam.setSolutionParticipation(reviewerParticipation);
        teamRepository.save(reviewerTeam);

        reviewedTeam.setSolutionParticipation(targetParticipation);
        teamRepository.save(reviewedTeam);

        Assessment assessment = new Assessment();
        assessment.setTask(task);
        assessment.setParticipation(targetParticipation);
        assessment.setAssessor(reviewerStudent);
        assessment.setType(AssessmentType.PEER);
        assessment.setStatus(AssessmentStatus.SUBMITTED);
        assessment.setTotalPoints(7);
        assessment.setCreatedAt(LocalDateTime.now());
        assessment.setUpdatedAt(LocalDateTime.now());
        assessmentRepository.save(assessment);

        AssessmentItem item = new AssessmentItem();
        item.setAssessment(assessment);
        item.setCriterion(criterion);
        item.setPoints(7);
        item.setComment("Original comment");
        assessmentItemRepository.save(item);

        assignment = new PeerReviewAssignment();
        assignment.setTask(task);
        assignment.setReviewerTeam(reviewerTeam);
        assignment.setReviewedTeam(reviewedTeam);
        assignment.setTargetParticipation(targetParticipation);
        assignment.setAssessment(assessment);
        assignment.setStatus(PeerReviewAssignmentStatus.SUBMITTED);
        assignment.setSubmittedAt(LocalDateTime.now());
        assignmentRepository.save(assignment);
    }

    @Test
    void teacherEdit_updatesAssessmentPointsAndSetsTeacherEditedStatus() {
        AssessmentSubmitDTO dto = AssessmentSubmitDTO.builder()
                .items(List.of(
                        AssessmentItemRequestDTO.builder()
                                .criterionId(criterion.getId())
                                .points(9)
                                .comment("Teacher override")
                                .build()
                ))
                .build();

        PeerReviewResultDTO result = peerReviewService.editPeerReviewAssessment(
                task.getId(), assignment.getId(), dto, teacher.getId());

        assertThat(result.getStatus()).isEqualTo(PeerReviewAssignmentStatus.TEACHER_EDITED);
        assertThat(result.getAssignment().getTeacherEditorId()).isEqualTo(teacher.getId());
        assertThat(result.getAssignment().getTeacherEditedAt()).isNotNull();

        PeerReviewAssignment persisted = assignmentRepository.findById(assignment.getId()).orElseThrow();
        assertThat(persisted.getStatus()).isEqualTo(PeerReviewAssignmentStatus.TEACHER_EDITED);
        assertThat(persisted.getTeacherEditor().getId()).isEqualTo(teacher.getId());
        assertThat(persisted.getAssessment().getTotalPoints()).isEqualTo(9);
    }

    @Test
    void teacherEdit_updatesTeamCommandMark() {
        AssessmentSubmitDTO dto = AssessmentSubmitDTO.builder()
                .items(List.of(
                        AssessmentItemRequestDTO.builder()
                                .criterionId(criterion.getId())
                                .points(8)
                                .comment("Updated")
                                .build()
                ))
                .build();

        peerReviewService.editPeerReviewAssessment(
                task.getId(), assignment.getId(), dto, teacher.getId());

        Team updatedReviewedTeam = teamRepository.findById(reviewedTeam.getId()).orElseThrow();
        assertThat(updatedReviewedTeam.getCommandMark()).isEqualTo(8);
    }

    @Test
    void teacherEdit_allowsSecondEditOnTeacherEditedAssignment() {
        AssessmentSubmitDTO firstEdit = AssessmentSubmitDTO.builder()
                .items(List.of(AssessmentItemRequestDTO.builder()
                        .criterionId(criterion.getId()).points(6).comment("First").build()))
                .build();
        peerReviewService.editPeerReviewAssessment(task.getId(), assignment.getId(), firstEdit, teacher.getId());

        AssessmentSubmitDTO secondEdit = AssessmentSubmitDTO.builder()
                .items(List.of(AssessmentItemRequestDTO.builder()
                        .criterionId(criterion.getId()).points(10).comment("Second").build()))
                .build();
        PeerReviewResultDTO result = peerReviewService.editPeerReviewAssessment(
                task.getId(), assignment.getId(), secondEdit, teacher.getId());

        assertThat(result.getStatus()).isEqualTo(PeerReviewAssignmentStatus.TEACHER_EDITED);
        assertThat(result.getAssignment().getTeacherEditorId()).isEqualTo(teacher.getId());
    }

    @Test
    void teacherEdit_withWrongTeacher_rejectsEdit() {
        User otherTeacher = User.builder()
                .firstName("Other")
                .lastName("Teacher")
                .email("other-teacher-" + UUID.randomUUID() + "@integration.test")
                .password("pass")
                .role(Role.TEACHER)
                .build();
        userRepository.save(otherTeacher);

        AssessmentSubmitDTO dto = AssessmentSubmitDTO.builder()
                .items(List.of(AssessmentItemRequestDTO.builder()
                        .criterionId(criterion.getId()).points(5).comment("Hack").build()))
                .build();

        assertThatThrownBy(() ->
                peerReviewService.editPeerReviewAssessment(task.getId(), assignment.getId(), dto, otherTeacher.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only the course teacher can manage this task");
    }
}
