package com.hits.language_school_back.service;

import com.hits.language_school_back.dto.TaskDTO;
import com.hits.language_school_back.dto.TaskTeamCreateDTO;
import com.hits.language_school_back.dto.TaskTeamDTO;
import com.hits.language_school_back.dto.UserFullDTO;
import com.hits.language_school_back.enums.Role;
import com.hits.language_school_back.enums.TaskResolveType;
import com.hits.language_school_back.enums.TeamType;
import com.hits.language_school_back.infrastructure.TaskServiceImpl;
import com.hits.language_school_back.mapper.TaskStudentMapper;
import com.hits.language_school_back.mapper.TaskTeacherMapper;
import com.hits.language_school_back.mapper.TaskTeamMapper;
import com.hits.language_school_back.model.Course;
import com.hits.language_school_back.model.Participation;
import com.hits.language_school_back.model.StudentsInCourse;
import com.hits.language_school_back.model.Task;
import com.hits.language_school_back.model.Team;
import com.hits.language_school_back.model.User;
import com.hits.language_school_back.repository.CourseRepository;
import com.hits.language_school_back.repository.ParticipationRepository;
import com.hits.language_school_back.repository.StudentsInCourseRepository;
import com.hits.language_school_back.repository.TaskRepository;
import com.hits.language_school_back.repository.TeamRepository;
import com.hits.language_school_back.repository.UserRepository;
import com.hits.language_school_back.repository.VoteRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTeamTypeTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private ParticipationRepository participationRepository;
    @Mock
    private VoteRepository voteRepository;
    @Mock
    private StudentsInCourseRepository studentsInCourseRepository;
    @Mock
    private TaskTeacherMapper taskTeacherMapper;
    @Mock
    private TaskStudentMapper taskStudentMapper;
    @Mock
    private TaskTeamMapper taskTeamMapper;

    @InjectMocks
    private TaskServiceImpl taskService;

    private UUID taskId;
    private UUID courseId;
    private UUID teacherId;
    private UUID captainId;
    private UUID studentId;
    private User teacher;
    private User captain;
    private User student;
    private Course course;

    @BeforeEach
    void setUp() {
        taskId = UUID.randomUUID();
        courseId = UUID.randomUUID();
        teacherId = UUID.randomUUID();
        captainId = UUID.randomUUID();
        studentId = UUID.randomUUID();

        teacher = User.builder().id(teacherId).role(Role.TEACHER).firstName("Teach").lastName("Er").build();
        captain = User.builder().id(captainId).role(Role.STUDENT).firstName("Cap").lastName("Tain").build();
        student = User.builder().id(studentId).role(Role.STUDENT).firstName("Stu").lastName("Dent").build();
        course = Course.builder().id(courseId).teacher(teacher).build();
    }

    @Test
    void createTask_whenRandomType_distributesCourseStudentsAutomatically() {
        List<User> students = List.of(
                captain,
                student,
                User.builder().id(UUID.randomUUID()).role(Role.STUDENT).build(),
                User.builder().id(UUID.randomUUID()).role(Role.STUDENT).build()
        );
        TaskDTO dto = baseTaskDto(TeamType.RANDOM)
                .minTeamSize(2)
                .maxTeamSize(2)
                .minTeamsAmount(2)
                .maxTeamsAmount(2)
                .build();

        when(userRepository.findById(teacherId)).thenReturn(Optional.of(teacher));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(taskId);
            return task;
        });
        when(studentsInCourseRepository.findAllByCourseId(courseId)).thenReturn(students.stream()
                .map(this::enrollment)
                .toList());
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> {
            Team team = invocation.getArgument(0);
            if (team.getId() == null) {
                team.setId(UUID.randomUUID());
            }
            return team;
        });
        when(participationRepository.save(any(Participation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(participationRepository.findAllByTeamId(any())).thenReturn(List.of());

        taskService.createTask(dto, UserFullDTO.builder().id(teacherId).build());

        ArgumentCaptor<Participation> participationCaptor = ArgumentCaptor.forClass(Participation.class);
        verify(participationRepository, times(4)).save(participationCaptor.capture());
        assertThat(participationCaptor.getAllValues())
                .hasSize(4)
                .extracting(participation -> participation.getStudent().getId())
                .containsExactlyInAnyOrderElementsOf(students.stream().map(User::getId).toList());
        assertThat(participationCaptor.getAllValues())
                .filteredOn(Participation::getIsCaptain)
                .hasSize(2);
    }

    @Test
    void createTeam_whenFreeroam_allowsStudentToCreateOwnTeam() {
        Task task = persistedTask(TeamType.FREEROAM, 1, 3);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(captainId)).thenReturn(Optional.of(captain));
        when(studentsInCourseRepository.existsByStudentIdAndCourseId(captainId, courseId)).thenReturn(true);
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> {
            Team team = invocation.getArgument(0);
            team.setId(UUID.randomUUID());
            return team;
        });
        when(participationRepository.save(any(Participation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(participationRepository.findAllByTeamId(any())).thenReturn(List.of());
        when(studentsInCourseRepository.findAllByCourseId(courseId)).thenReturn(List.of());
        when(taskTeamMapper.toDto(any(Team.class))).thenReturn(TaskTeamDTO.builder().build());

        taskService.createTeam(taskId, TaskTeamCreateDTO.builder().name("A").build(), captainId);

        ArgumentCaptor<Participation> participationCaptor = ArgumentCaptor.forClass(Participation.class);
        verify(participationRepository).save(participationCaptor.capture());
        assertThat(participationCaptor.getValue().getStudent()).isEqualTo(captain);
        assertThat(participationCaptor.getValue().getIsCaptain()).isTrue();
    }

    @Test
    void addStudentToTeam_whenDraft_allowsCaptainToDraftStudent() {
        Task task = persistedTask(TeamType.DRAFT, 1, 3);
        Team team = persistedTeam(task);
        Participation captainParticipation = participation(team, captain, true);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(teamRepository.findByIdAndTaskId(team.getId(), taskId)).thenReturn(Optional.of(team));
        when(userRepository.findById(captainId)).thenReturn(Optional.of(captain));
        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(studentsInCourseRepository.existsByStudentIdAndCourseId(studentId, courseId)).thenReturn(true);
        when(participationRepository.findAllByTeamTaskId(taskId)).thenReturn(List.of(captainParticipation));
        when(participationRepository.findAllByTeamId(team.getId())).thenReturn(List.of(captainParticipation));
        when(participationRepository.findByTeamIdAndStudentId(team.getId(), captainId)).thenReturn(Optional.of(captainParticipation));
        when(participationRepository.save(any(Participation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(studentsInCourseRepository.findAllByCourseId(courseId)).thenReturn(List.of());
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(taskTeamMapper.toDto(any(Team.class))).thenReturn(TaskTeamDTO.builder().build());

        taskService.addStudentToTeam(taskId, team.getId(), studentId, captainId);

        ArgumentCaptor<Participation> participationCaptor = ArgumentCaptor.forClass(Participation.class);
        verify(participationRepository).save(participationCaptor.capture());
        assertThat(participationCaptor.getValue().getStudent()).isEqualTo(student);
        assertThat(participationCaptor.getValue().getIsCaptain()).isFalse();
    }

    @Test
    void addStudentToTeam_whenDraftAndActorIsNotCaptain_rejectsDraft() {
        Task task = persistedTask(TeamType.DRAFT, 1, 3);
        Team team = persistedTeam(task);
        Participation nonCaptainParticipation = participation(team, captain, false);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(teamRepository.findByIdAndTaskId(team.getId(), taskId)).thenReturn(Optional.of(team));
        when(userRepository.findById(captainId)).thenReturn(Optional.of(captain));
        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(studentsInCourseRepository.existsByStudentIdAndCourseId(studentId, courseId)).thenReturn(true);
        when(participationRepository.findAllByTeamTaskId(taskId)).thenReturn(List.of(nonCaptainParticipation));
        when(participationRepository.findAllByTeamId(team.getId())).thenReturn(List.of(nonCaptainParticipation));
        when(participationRepository.findByTeamIdAndStudentId(team.getId(), captainId)).thenReturn(Optional.of(nonCaptainParticipation));

        assertThatThrownBy(() -> taskService.addStudentToTeam(taskId, team.getId(), studentId, captainId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Only captain can draft students to the team");
    }

    @Test
    void createTeam_whenRandom_rejectsManualTeamCreation() {
        Task task = persistedTask(TeamType.RANDOM, 1, 3);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(captainId)).thenReturn(Optional.of(captain));
        when(studentsInCourseRepository.existsByStudentIdAndCourseId(captainId, courseId)).thenReturn(true);

        assertThatThrownBy(() -> taskService.createTeam(taskId, TaskTeamCreateDTO.builder().build(), captainId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Random teams are generated automatically");
    }

    @Test
    void submitSolution_whenTeamBelowMinSize_rejectsSubmission() {
        Task task = persistedTask(TeamType.FREEROAM, 2, 3);
        Team team = persistedTeam(task);
        Participation captainParticipation = participation(team, captain, true);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(participationRepository.findAllByTeamTaskId(taskId)).thenReturn(List.of(captainParticipation));
        when(participationRepository.findByTeamIdAndStudentId(team.getId(), captainId)).thenReturn(Optional.of(captainParticipation));
        when(participationRepository.findAllByTeamId(team.getId())).thenReturn(List.of(captainParticipation));

        assertThatThrownBy(() -> taskService.completeTask(taskId, captainId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Team does not have enough members to submit a solution");
    }

    private TaskDTO.TaskDTOBuilder baseTaskDto(TeamType teamType) {
        return TaskDTO.builder()
                .name("Task")
                .description("Description")
                .courseId(courseId)
                .totalPoints(100)
                .teamType(teamType)
                .resolveType(TaskResolveType.LAST_SUBMITTED_SOLUTION);
    }

    private Task persistedTask(TeamType teamType, int minTeamSize, int maxTeamSize) {
        return Task.builder()
                .id(taskId)
                .name("Task")
                .description("Description")
                .course(course)
                .createdBy(teacher)
                .teamType(teamType)
                .resolveType(TaskResolveType.LAST_SUBMITTED_SOLUTION)
                .minTeamSize(minTeamSize)
                .maxTeamSize(maxTeamSize)
                .submissionClosed(false)
                .build();
    }

    private Team persistedTeam(Task task) {
        Team team = new Team();
        team.setId(UUID.randomUUID());
        team.setName("Team");
        team.setTask(task);
        return team;
    }

    private Participation participation(Team team, User user, boolean captainFlag) {
        Participation participation = new Participation();
        participation.setId(UUID.randomUUID());
        participation.setTeam(team);
        participation.setStudent(user);
        participation.setIsCaptain(captainFlag);
        return participation;
    }

    private StudentsInCourse enrollment(User user) {
        return StudentsInCourse.builder()
                .course(course)
                .student(user)
                .courseGrade(0D)
                .build();
    }
}
