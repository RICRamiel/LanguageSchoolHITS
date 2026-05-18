package com.hits.language_school_back.service;

import com.hits.language_school_back.dto.TaskDTO;
import com.hits.language_school_back.dto.TaskParticipationGradeDTO;
import com.hits.language_school_back.dto.TaskTeamCreateDTO;
import com.hits.language_school_back.dto.TaskTeamDTO;
import com.hits.language_school_back.dto.TaskTeamGradeDTO;
import com.hits.language_school_back.dto.UserFullDTO;
import com.hits.language_school_back.enums.Role;
import com.hits.language_school_back.enums.SolutionStatus;
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
import java.time.LocalDateTime;

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

    @Test
    void editTask_whenActorIsNotCourseTeacher_rejectsEdit() {
        Task task = persistedTask(TeamType.FREEROAM, 1, 3);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.editTask(TaskDTO.builder().name("New").build(), taskId, UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Only the course teacher can manage this task");
    }

    @Test
    void deleteTask_whenActorIsNotCourseTeacher_rejectsDelete() {
        Task task = persistedTask(TeamType.FREEROAM, 1, 3);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.deleteTask(taskId, UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Only the course teacher can manage this task");
    }

    @Test
    void createTeam_whenCaptainAlreadyHasTeam_rejectsDuplicateTeam() {
        Task task = persistedTask(TeamType.FREEROAM, 1, 3);
        Team existingTeam = persistedTeam(task);
        Participation existingParticipation = participation(existingTeam, captain, true);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(captainId)).thenReturn(Optional.of(captain));
        when(studentsInCourseRepository.existsByStudentIdAndCourseId(captainId, courseId)).thenReturn(true);
        when(participationRepository.findAllByTeamTaskId(taskId)).thenReturn(List.of(existingParticipation));

        assertThatThrownBy(() -> taskService.createTeam(taskId, TaskTeamCreateDTO.builder().build(), captainId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Student already belongs to a team in this task");
    }

    @Test
    void gradeParticipation_whenNoTeamMark_usesIndividualMarkAsAverage() {
        Task task = persistedTask(TeamType.FREEROAM, 1, 3);
        Team team = persistedTeam(task);
        team.setCommandMark(null);
        Participation participation = participation(team, captain, true);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(participationRepository.findById(participation.getId())).thenReturn(Optional.of(participation));
        when(participationRepository.findAllByTeamId(team.getId())).thenReturn(List.of(participation));
        when(studentsInCourseRepository.findAllByCourseId(courseId)).thenReturn(List.of(enrollment(captain)));
        when(participationRepository.findAllByStudentId(captainId)).thenReturn(List.of(participation));
        when(teamRepository.save(team)).thenReturn(team);
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(taskTeamMapper.toDto(team)).thenReturn(TaskTeamDTO.builder().build());

        taskService.gradeParticipation(taskId, participation.getId(), TaskParticipationGradeDTO.builder().mark(80).build(), teacherId);

        assertThat(participation.getAverageMark()).isEqualTo(80D);
    }

    @Test
    void gradeTeam_whenNoIndividualMarks_usesTeamMarkForMembersAverage() {
        Task task = persistedTask(TeamType.FREEROAM, 1, 3);
        Team team = persistedTeam(task);
        Participation captainParticipation = participation(team, captain, true);
        Participation studentParticipation = participation(team, student, false);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(teamRepository.findByIdAndTaskId(team.getId(), taskId)).thenReturn(Optional.of(team));
        when(participationRepository.findAllByTeamId(team.getId())).thenReturn(List.of(captainParticipation, studentParticipation));
        when(studentsInCourseRepository.findAllByCourseId(courseId)).thenReturn(List.of(enrollment(captain), enrollment(student)));
        when(participationRepository.findAllByStudentId(captainId)).thenReturn(List.of(captainParticipation));
        when(participationRepository.findAllByStudentId(studentId)).thenReturn(List.of(studentParticipation));
        when(teamRepository.save(team)).thenReturn(team);
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(taskTeamMapper.toDto(team)).thenReturn(TaskTeamDTO.builder().build());

        taskService.gradeTeam(taskId, team.getId(), TaskTeamGradeDTO.builder().mark(70).build(), teacherId);

        assertThat(captainParticipation.getAverageMark()).isEqualTo(70D);
        assertThat(studentParticipation.getAverageMark()).isEqualTo(70D);
    }

    @Test
    void finalizeTask_whenCaptainSolutionRequiredAndCaptainDidNotSubmit_selectsNoSolution() {
        Task task = persistedTask(TeamType.FREEROAM, 1, 3);
        task.setResolveType(TaskResolveType.CAPTAINS_SOLUTION);
        Team team = persistedTeam(task);
        Participation captainParticipation = participation(team, captain, true);
        Participation studentParticipation = participation(team, student, false);
        studentParticipation.setSubmittedAt(LocalDateTime.now());
        studentParticipation.setSolutionStatus(SolutionStatus.SUBMITTED);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(teamRepository.findAllByTaskId(taskId)).thenReturn(List.of(team));
        when(participationRepository.findAllByTeamId(team.getId())).thenReturn(List.of(captainParticipation, studentParticipation));
        when(studentsInCourseRepository.findAllByCourseId(courseId)).thenReturn(List.of(enrollment(captain), enrollment(student)));
        when(participationRepository.findAllByStudentId(captainId)).thenReturn(List.of(captainParticipation));
        when(participationRepository.findAllByStudentId(studentId)).thenReturn(List.of(studentParticipation));
        when(teamRepository.save(team)).thenReturn(team);

        taskService.finalizeTask(taskId, teacherId);

        assertThat(team.getSolutionParticipation()).isNull();
        assertThat(studentParticipation.getSolutionStatus()).isEqualTo(SolutionStatus.OVERDUE);
    }

    @Test
    void finalizeTask_whenVotesThresholdNotReached_selectsNoSolution() {
        Task task = persistedTask(TeamType.FREEROAM, 1, 3);
        task.setResolveType(TaskResolveType.AT_LEAST_VOTES_SOLUTION);
        task.setVotesThreshold(2);
        Team team = persistedTeam(task);
        Participation participation = participation(team, captain, true);
        participation.setSubmittedAt(LocalDateTime.now());
        participation.setSolutionStatus(SolutionStatus.SUBMITTED);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(teamRepository.findAllByTaskId(taskId)).thenReturn(List.of(team));
        when(participationRepository.findAllByTeamId(team.getId())).thenReturn(List.of(participation));
        when(voteRepository.countByParticipationId(participation.getId())).thenReturn(1L);
        when(studentsInCourseRepository.findAllByCourseId(courseId)).thenReturn(List.of(enrollment(captain)));
        when(participationRepository.findAllByStudentId(captainId)).thenReturn(List.of(participation));
        when(teamRepository.save(team)).thenReturn(team);

        taskService.finalizeTask(taskId, teacherId);

        assertThat(team.getSolutionParticipation()).isNull();
        assertThat(participation.getSolutionStatus()).isEqualTo(SolutionStatus.OVERDUE);
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
