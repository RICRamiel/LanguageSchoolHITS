package com.hits.language_school_back.service;

import com.hits.language_school_back.dto.CriterionScoreDTO;
import com.hits.language_school_back.dto.ParticipationCriteriaGradeDTO;
import com.hits.language_school_back.dto.TaskTeamDTO;
import com.hits.language_school_back.infrastructure.TaskServiceImpl;
import com.hits.language_school_back.mapper.TaskStudentMapper;
import com.hits.language_school_back.mapper.TaskTeacherMapper;
import com.hits.language_school_back.mapper.TaskTeamMapper;
import com.hits.language_school_back.model.Course;
import com.hits.language_school_back.model.Participation;
import com.hits.language_school_back.model.ParticipationCriterionScore;
import com.hits.language_school_back.model.Task;
import com.hits.language_school_back.model.TaskGradingCriterion;
import com.hits.language_school_back.model.Team;
import com.hits.language_school_back.model.User;
import com.hits.language_school_back.repository.CourseRepository;
import com.hits.language_school_back.repository.ParticipationCriterionScoreRepository;
import com.hits.language_school_back.repository.ParticipationRepository;
import com.hits.language_school_back.repository.StudentsInCourseRepository;
import com.hits.language_school_back.repository.TaskGradingCriterionRepository;
import com.hits.language_school_back.repository.TaskRepository;
import com.hits.language_school_back.repository.TeamRepository;
import com.hits.language_school_back.repository.UserRepository;
import com.hits.language_school_back.repository.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock private TaskRepository taskRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private UserRepository userRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private ParticipationRepository participationRepository;
    @Mock private ParticipationCriterionScoreRepository participationCriterionScoreRepository;
    @Mock private TaskGradingCriterionRepository taskGradingCriterionRepository;
    @Mock private VoteRepository voteRepository;
    @Mock private StudentsInCourseRepository studentsInCourseRepository;
    @Mock private TaskTeacherMapper taskTeacherMapper;
    @Mock private TaskStudentMapper taskStudentMapper;
    @Mock private TaskTeamMapper taskTeamMapper;

    @InjectMocks
    private TaskServiceImpl taskService;

    private UUID taskId;
    private UUID teacherId;
    private UUID participationId;
    private UUID teamId;
    private Task task;
    private Participation participation;
    private TaskGradingCriterion requiredCriterion;
    private TaskGradingCriterion optionalCriterion;

    @BeforeEach
    void setUp() {
        taskId = UUID.randomUUID();
        teacherId = UUID.randomUUID();
        participationId = UUID.randomUUID();
        teamId = UUID.randomUUID();

        User teacher = new User();
        teacher.setId(teacherId);

        Course course = Course.builder().teacher(teacher).build();
        task = Task.builder().id(taskId).course(course).totalPoints(100).build();

        Team team = new Team();
        team.setId(teamId);
        team.setTask(task);
        team.setCommandMark(0);

        participation = new Participation();
        participation.setId(participationId);
        participation.setTeam(team);
        participation.setAverageMark(0D);

        requiredCriterion = new TaskGradingCriterion();
        requiredCriterion.setId(UUID.randomUUID());
        requiredCriterion.setTask(task);
        requiredCriterion.setName("Accuracy");
        requiredCriterion.setMaxPoints(60);
        requiredCriterion.setRequired(true);

        optionalCriterion = new TaskGradingCriterion();
        optionalCriterion.setId(UUID.randomUUID());
        optionalCriterion.setTask(task);
        optionalCriterion.setName("Style");
        optionalCriterion.setMaxPoints(40);
        optionalCriterion.setRequired(false);
    }

    @Test
    void gradeParticipationByCriteria_rejectsScoreAboveCriterionMax() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(participationRepository.findById(participationId)).thenReturn(Optional.of(participation));
        when(taskGradingCriterionRepository.findAllByTaskIdOrderByPositionAsc(taskId)).thenReturn(List.of(requiredCriterion));

        ParticipationCriteriaGradeDTO dto = ParticipationCriteriaGradeDTO.builder()
                .criteria(List.of(CriterionScoreDTO.builder().criterionId(requiredCriterion.getId()).points(61).build()))
                .build();

        assertThatThrownBy(() -> taskService.gradeParticipationByCriteria(taskId, participationId, dto, teacherId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot exceed criterion max points");
    }

    @Test
    void gradeParticipationByCriteria_computesFinalMarkAutomatically() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(participationRepository.findById(participationId)).thenReturn(Optional.of(participation));
        when(taskGradingCriterionRepository.findAllByTaskIdOrderByPositionAsc(taskId)).thenReturn(List.of(requiredCriterion, optionalCriterion));
        when(participationCriterionScoreRepository.findByParticipationIdAndCriterionId(participationId, requiredCriterion.getId())).thenReturn(Optional.empty());
        when(participationCriterionScoreRepository.findByParticipationIdAndCriterionId(participationId, optionalCriterion.getId())).thenReturn(Optional.empty());
        when(participationCriterionScoreRepository.findAllByParticipationId(participationId)).thenReturn(List.of(
                score(requiredCriterion, 55),
                score(optionalCriterion, 30)
        ));
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(participation.getTeam()));
        when(participationRepository.findAllByTeamId(teamId)).thenReturn(List.of(participation));
        when(studentsInCourseRepository.findAllByCourseId(any())).thenReturn(List.of());
        when(taskTeamMapper.toDto(any())).thenReturn(new TaskTeamDTO());

        taskService.gradeParticipationByCriteria(taskId, participationId, ParticipationCriteriaGradeDTO.builder()
                .criteria(List.of(
                        CriterionScoreDTO.builder().criterionId(requiredCriterion.getId()).points(55).build(),
                        CriterionScoreDTO.builder().criterionId(optionalCriterion.getId()).points(30).build()
                ))
                .publish(false)
                .build(), teacherId);

        assertThat(participation.getMark()).isEqualTo(85);
    }

    @Test
    void gradeParticipationByCriteria_publishFailsWhenRequiredCriterionMissing() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(participationRepository.findById(participationId)).thenReturn(Optional.of(participation));
        when(taskGradingCriterionRepository.findAllByTaskIdOrderByPositionAsc(taskId)).thenReturn(List.of(requiredCriterion, optionalCriterion));
        when(participationCriterionScoreRepository.findByParticipationIdAndCriterionId(participationId, optionalCriterion.getId())).thenReturn(Optional.empty());
        when(participationCriterionScoreRepository.findAllByParticipationId(participationId)).thenReturn(List.of(score(optionalCriterion, 30)));

        ParticipationCriteriaGradeDTO dto = ParticipationCriteriaGradeDTO.builder()
                .criteria(List.of(CriterionScoreDTO.builder().criterionId(optionalCriterion.getId()).points(30).build()))
                .publish(true)
                .build();

        assertThatThrownBy(() -> taskService.gradeParticipationByCriteria(taskId, participationId, dto, teacherId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("required criteria are missing");

        verify(participationRepository, never()).save(participation);
    }

    @Test
    void gradeParticipationByCriteria_publishSucceedsWhenRequiredCriteriaFilled() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(participationRepository.findById(participationId)).thenReturn(Optional.of(participation));
        when(taskGradingCriterionRepository.findAllByTaskIdOrderByPositionAsc(taskId)).thenReturn(List.of(requiredCriterion));
        when(participationCriterionScoreRepository.findByParticipationIdAndCriterionId(participationId, requiredCriterion.getId())).thenReturn(Optional.empty());
        when(participationCriterionScoreRepository.findAllByParticipationId(participationId)).thenReturn(List.of(score(requiredCriterion, 50)));
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(participation.getTeam()));
        when(participationRepository.findAllByTeamId(teamId)).thenReturn(List.of(participation));
        when(studentsInCourseRepository.findAllByCourseId(any())).thenReturn(List.of());
        when(taskTeamMapper.toDto(any())).thenReturn(new TaskTeamDTO());

        taskService.gradeParticipationByCriteria(taskId, participationId, ParticipationCriteriaGradeDTO.builder()
                .criteria(List.of(CriterionScoreDTO.builder().criterionId(requiredCriterion.getId()).points(50).build()))
                .publish(true)
                .build(), teacherId);

        assertThat(participation.getReviewPublished()).isTrue();
    }

    private ParticipationCriterionScore score(TaskGradingCriterion criterion, int points) {
        ParticipationCriterionScore score = new ParticipationCriterionScore();
        score.setCriterion(criterion);
        score.setParticipation(participation);
        score.setPoints(points);
        return score;
    }
}
