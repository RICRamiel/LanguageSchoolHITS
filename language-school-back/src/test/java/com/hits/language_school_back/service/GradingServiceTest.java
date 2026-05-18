package com.hits.language_school_back.service;

import com.hits.language_school_back.dto.AssessmentItemRequestDTO;
import com.hits.language_school_back.dto.AssessmentSubmitDTO;
import com.hits.language_school_back.dto.TaskCriterionDTO;
import com.hits.language_school_back.enums.AssessmentType;
import com.hits.language_school_back.enums.Role;
import com.hits.language_school_back.infrastructure.GradingServiceImpl;
import com.hits.language_school_back.model.Assessment;
import com.hits.language_school_back.model.AssessmentItem;
import com.hits.language_school_back.model.Course;
import com.hits.language_school_back.model.Participation;
import com.hits.language_school_back.model.StudentsInCourse;
import com.hits.language_school_back.model.Task;
import com.hits.language_school_back.model.TaskCriterion;
import com.hits.language_school_back.model.Team;
import com.hits.language_school_back.model.User;
import com.hits.language_school_back.repository.AssessmentItemRepository;
import com.hits.language_school_back.repository.AssessmentRepository;
import com.hits.language_school_back.repository.ParticipationRepository;
import com.hits.language_school_back.repository.StudentsInCourseRepository;
import com.hits.language_school_back.repository.TaskCriterionRepository;
import com.hits.language_school_back.repository.TaskRepository;
import com.hits.language_school_back.repository.TeamRepository;
import com.hits.language_school_back.repository.UserRepository;
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
class GradingServiceTest {
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private TaskCriterionRepository criterionRepository;
    @Mock
    private AssessmentRepository assessmentRepository;
    @Mock
    private AssessmentItemRepository assessmentItemRepository;
    @Mock
    private ParticipationRepository participationRepository;
    @Mock
    private StudentsInCourseRepository studentsInCourseRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GradingServiceImpl gradingService;

    private UUID taskId;
    private UUID courseId;
    private UUID teacherId;
    private UUID studentId;
    private UUID otherStudentId;
    private User teacher;
    private User student;
    private User otherStudent;
    private Course course;
    private Task task;
    private Team team;
    private Participation participation;
    private TaskCriterion criterion;

    @BeforeEach
    void setUp() {
        taskId = UUID.randomUUID();
        courseId = UUID.randomUUID();
        teacherId = UUID.randomUUID();
        studentId = UUID.randomUUID();
        otherStudentId = UUID.randomUUID();

        teacher = User.builder().id(teacherId).role(Role.TEACHER).build();
        student = User.builder().id(studentId).role(Role.STUDENT).build();
        otherStudent = User.builder().id(otherStudentId).role(Role.STUDENT).build();
        course = Course.builder().id(courseId).teacher(teacher).build();
        task = Task.builder().id(taskId).course(course).totalPoints(100).build();
        team = new Team();
        team.setId(UUID.randomUUID());
        team.setTask(task);
        team.setCommandMark(null);
        participation = new Participation();
        participation.setId(UUID.randomUUID());
        participation.setStudent(student);
        participation.setTeam(team);

        criterion = new TaskCriterion();
        criterion.setId(UUID.randomUUID());
        criterion.setTask(task);
        criterion.setTitle("API auth");
        criterion.setMaxPoints(10);
        criterion.setOrderIndex(1);
        criterion.setActive(true);
    }

    @Test
    void createCriterion_whenTeacherOwnsCourse_savesCriterionAndSyncsTaskTotal() {
        TaskCriterionDTO dto = TaskCriterionDTO.builder()
                .title("Unit tests")
                .maxPoints(20)
                .sectionName("Backend")
                .orderIndex(2)
                .build();
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(criterionRepository.save(any(TaskCriterion.class))).thenAnswer(invocation -> {
            TaskCriterion saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });
        when(criterionRepository.findAllByTaskIdAndActiveTrueOrderByOrderIndexAscTitleAsc(taskId))
                .thenReturn(List.of(criterion, criterionWithMax(20)));

        TaskCriterionDTO result = gradingService.createCriterion(taskId, dto, teacherId);

        assertThat(result.getTitle()).isEqualTo("Unit tests");
        assertThat(task.getTotalPoints()).isEqualTo(30);
        verify(taskRepository).save(task);
    }

    @Test
    void createCriterion_whenActorIsNotCourseTeacher_throws() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> gradingService.createCriterion(taskId, TaskCriterionDTO.builder().title("x").maxPoints(10).build(), UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Only the course teacher can manage grading");
    }

    @Test
    void submitTeacherAssessment_updatesOfficialParticipationMarkAndAverage() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(participationRepository.findById(participation.getId())).thenReturn(Optional.of(participation));
        when(userRepository.findById(teacherId)).thenReturn(Optional.of(teacher));
        when(assessmentRepository.findByParticipationIdAndType(participation.getId(), AssessmentType.TEACHER)).thenReturn(Optional.empty());
        when(assessmentRepository.save(any(Assessment.class))).thenAnswer(invocation -> {
            Assessment saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                saved.setId(UUID.randomUUID());
            }
            return saved;
        });
        when(criterionRepository.findAllByTaskIdAndActiveTrueOrderByOrderIndexAscTitleAsc(taskId)).thenReturn(List.of(criterion));
        when(assessmentItemRepository.findAllByAssessmentId(any())).thenReturn(List.of());
        when(assessmentItemRepository.save(any(AssessmentItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(participationRepository.findAllByTeamId(team.getId())).thenReturn(List.of(participation));
        when(studentsInCourseRepository.findAllByCourseId(courseId)).thenReturn(List.of(enrollment(student)));
        when(participationRepository.findAllByStudentId(studentId)).thenReturn(List.of(participation));

        gradingService.submitTeacherAssessment(taskId, participation.getId(), assessment(criterion.getId(), 8), teacherId);

        assertThat(participation.getMark()).isEqualTo(8);
        assertThat(participation.getAverageMark()).isEqualTo(8D);
        verify(participationRepository).save(participation);
    }

    @Test
    void submitTeacherAssessment_whenPointsExceedCriterionMax_throws() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(participationRepository.findById(participation.getId())).thenReturn(Optional.of(participation));
        when(userRepository.findById(teacherId)).thenReturn(Optional.of(teacher));
        when(assessmentRepository.findByParticipationIdAndType(participation.getId(), AssessmentType.TEACHER)).thenReturn(Optional.empty());
        when(assessmentRepository.save(any(Assessment.class))).thenAnswer(invocation -> {
            Assessment saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });
        when(criterionRepository.findAllByTaskIdAndActiveTrueOrderByOrderIndexAscTitleAsc(taskId)).thenReturn(List.of(criterion));

        assertThatThrownBy(() -> gradingService.submitTeacherAssessment(taskId, participation.getId(), assessment(criterion.getId(), 11), teacherId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Assessment points cannot exceed criterion maxPoints");

        verify(participationRepository, never()).save(participation);
    }

    @Test
    void submitSelfAssessment_whenStudentOwnsParticipation_doesNotChangeOfficialMark() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(participationRepository.findById(participation.getId())).thenReturn(Optional.of(participation));
        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(assessmentRepository.findByParticipationIdAndType(participation.getId(), AssessmentType.SELF)).thenReturn(Optional.empty());
        when(assessmentRepository.save(any(Assessment.class))).thenAnswer(invocation -> {
            Assessment saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });
        when(criterionRepository.findAllByTaskIdAndActiveTrueOrderByOrderIndexAscTitleAsc(taskId)).thenReturn(List.of(criterion));
        when(assessmentItemRepository.findAllByAssessmentId(any())).thenReturn(List.of());
        when(assessmentItemRepository.save(any(AssessmentItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        gradingService.submitSelfAssessment(taskId, participation.getId(), assessment(criterion.getId(), 10), studentId);

        assertThat(participation.getMark()).isNull();
        verify(participationRepository, never()).save(participation);
    }

    @Test
    void submitSelfAssessment_whenStudentDoesNotOwnParticipation_throws() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(participationRepository.findById(participation.getId())).thenReturn(Optional.of(participation));

        assertThatThrownBy(() -> gradingService.submitSelfAssessment(taskId, participation.getId(), assessment(criterion.getId(), 10), otherStudentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Students can self-assess only their own participation");
    }

    @Test
    void editCriterion_whenExistingScoreExceedsNewMax_throws() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(criterionRepository.findById(criterion.getId())).thenReturn(Optional.of(criterion));
        when(assessmentItemRepository.existsByCriterionIdAndPointsGreaterThan(criterion.getId(), 5)).thenReturn(true);

        assertThatThrownBy(() -> gradingService.editCriterion(taskId, criterion.getId(), TaskCriterionDTO.builder().title("API auth").maxPoints(5).build(), teacherId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Existing assessment points exceed new criterion maxPoints");
    }

    private AssessmentSubmitDTO assessment(UUID criterionId, int points) {
        return AssessmentSubmitDTO.builder()
                .items(List.of(AssessmentItemRequestDTO.builder()
                        .criterionId(criterionId)
                        .points(points)
                        .comment("ok")
                        .build()))
                .build();
    }

    private TaskCriterion criterionWithMax(int maxPoints) {
        TaskCriterion result = new TaskCriterion();
        result.setId(UUID.randomUUID());
        result.setTask(task);
        result.setTitle("criterion " + maxPoints);
        result.setMaxPoints(maxPoints);
        result.setActive(true);
        return result;
    }

    private StudentsInCourse enrollment(User user) {
        return StudentsInCourse.builder()
                .course(course)
                .student(user)
                .courseGrade(0D)
                .build();
    }
}
