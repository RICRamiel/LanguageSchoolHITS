package com.hits.language_school_back.infrastructure;

import com.google.common.base.Strings;
import com.hits.language_school_back.dto.TaskDTO;
import com.hits.language_school_back.dto.TaskParticipationGradeDTO;
import com.hits.language_school_back.dto.TaskSolutionSubmitDTO;
import com.hits.language_school_back.dto.TaskStudentDTO;
import com.hits.language_school_back.dto.TaskTeacherDTO;
import com.hits.language_school_back.dto.TaskTeamCreateDTO;
import com.hits.language_school_back.dto.TaskTeamDTO;
import com.hits.language_school_back.dto.TaskTeamGradeDTO;
import com.hits.language_school_back.dto.UserFullDTO;
import com.hits.language_school_back.enums.Role;
import com.hits.language_school_back.enums.SolutionStatus;
import com.hits.language_school_back.enums.TaskResolveType;
import com.hits.language_school_back.enums.TeamType;
import com.hits.language_school_back.mapper.TaskStudentMapper;
import com.hits.language_school_back.mapper.TaskTeacherMapper;
import com.hits.language_school_back.mapper.TaskTeamMapper;
import com.hits.language_school_back.model.Course;
import com.hits.language_school_back.model.Participation;
import com.hits.language_school_back.model.StudentsInCourse;
import com.hits.language_school_back.model.Task;
import com.hits.language_school_back.model.Team;
import com.hits.language_school_back.model.User;
import com.hits.language_school_back.model.Vote;
import com.hits.language_school_back.repository.CourseRepository;
import com.hits.language_school_back.repository.ParticipationRepository;
import com.hits.language_school_back.repository.StudentsInCourseRepository;
import com.hits.language_school_back.repository.TaskRepository;
import com.hits.language_school_back.repository.TeamRepository;
import com.hits.language_school_back.repository.UserRepository;
import com.hits.language_school_back.repository.VoteRepository;
import com.hits.language_school_back.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final ParticipationRepository participationRepository;
    private final VoteRepository voteRepository;
    private final StudentsInCourseRepository studentsInCourseRepository;
    private final TaskTeacherMapper taskTeacherMapper;
    private final TaskStudentMapper taskStudentMapper;
    private final TaskTeamMapper taskTeamMapper;

    @Override
    @Transactional
    public List<TaskTeacherDTO> getTasksByTeacherId(UUID teacherId) {
        List<Task> tasks = taskRepository.findAllByCourseTeacherIdOrderByDeadlineAsc(teacherId);
        tasks.forEach(this::finalizeIfDeadlineReached);
        return taskTeacherMapper.toDtoList(tasks);
    }

    @Override
    @Transactional
    public List<TaskStudentDTO> getTasksByGroupName(String name, UUID userId) {
        List<Task> tasks = taskRepository.findAllByCourseNameOrderByDeadlineAsc(name);
        tasks.forEach(this::finalizeIfDeadlineReached);
        return taskStudentMapper.toDtoList(tasks, userId);
    }

    @Override
    @Transactional
    public List<TaskStudentDTO> getTasksByCourseId(UUID courseId, UUID userId) {
        List<Task> tasks = taskRepository.findAllByCourseIdOrderByDeadlineAsc(courseId);
        tasks.forEach(this::finalizeIfDeadlineReached);
        return taskStudentMapper.toDtoList(tasks, userId);
    }

    @Override
    @Transactional
    public Task createTask(TaskDTO taskDTO, UserFullDTO userFullDTO) {
        User creator = getUser(userFullDTO.getId());
        Course course = getCourse(taskDTO.getCourseId());
        ensureTeacherCanManageCourse(creator.getId(), course);

        Task task = new Task();
        applyTaskDto(task, taskDTO, course, creator);
        task.setCreatedAt(LocalDateTime.now());
        task.setSubmissionClosed(Boolean.FALSE);

        return taskRepository.save(task);
    }

    @Override
    @Transactional
    public void deleteTask(UUID id) {
        taskRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Task editTask(TaskDTO taskDTO, UUID taskId) {
        Task task = getTask(taskId);
        ensureTeacherCanManageCourse(task.getCreatedBy().getId(), task.getCourse());

        Course course = task.getCourse();
        if (taskDTO.getCourseId() != null && !taskDTO.getCourseId().equals(course.getId())) {
            course = getCourse(taskDTO.getCourseId());
            ensureTeacherCanManageCourse(task.getCreatedBy().getId(), course);
        }

        applyTaskDto(task, taskDTO, course, task.getCreatedBy());
        return taskRepository.save(task);
    }

    @Override
    @Transactional
    public TaskTeamDTO createTeam(UUID taskId, TaskTeamCreateDTO dto, UUID userId) {
        Task task = getTask(taskId);
        finalizeIfDeadlineReached(task);
        ensureTaskOpen(task);

        User actor = getUser(userId);
        UUID captainId = dto.getCaptainId() == null ? userId : dto.getCaptainId();
        User captain = getUser(captainId);

        if (isSoloTask(task)) {
            captain = actor;
            captainId = userId;
        }

        ensureStudentInCourse(task.getCourse().getId(), captainId);
        ensureCanCreateTeam(task, actor);
        ensureTeamSlotsAvailable(task);

        Team team = new Team();
        team.setName(resolveTeamName(task, dto, captain));
        team.setTask(task);
        team.setCommandMark(0);
        team.setAverageMark(0D);
        Team savedTeam = teamRepository.save(team);

        Participation captainParticipation = createParticipation(savedTeam, captain, true);
        savedTeam.setSolutionParticipation(null);
        Team result = teamRepository.save(savedTeam);
        recalculateTeamStats(result);
        recalculateCourseGrades(task.getCourse().getId());
        log.debug("Team {} created in task {}", result.getId(), taskId);
        return taskTeamMapper.toDto(withCaptain(result, captainParticipation));
    }

    @Override
    @Transactional
    public TaskTeamDTO joinTeam(UUID taskId, UUID teamId, UUID userId) {
        return addStudentToTeam(taskId, teamId, userId, userId);
    }

    @Override
    @Transactional
    public TaskTeamDTO addStudentToTeam(UUID taskId, UUID teamId, UUID studentId, UUID actorId) {
        Task task = getTask(taskId);
        finalizeIfDeadlineReached(task);
        ensureTaskOpen(task);

        Team team = getTeam(taskId, teamId);
        User actor = getUser(actorId);
        User student = getUser(studentId);
        ensureStudentInCourse(task.getCourse().getId(), studentId);
        ensureStudentHasNoTeam(taskId, studentId);
        ensureTeamCapacity(task, team);
        ensureCanAssignStudent(task, team, actor, studentId);

        createParticipation(team, student, false);
        recalculateTeamStats(team);
        recalculateCourseGrades(task.getCourse().getId());
        return taskTeamMapper.toDto(reloadTeam(teamId));
    }

    @Override
    @Transactional
    public TaskTeamDTO appointCaptain(UUID taskId, UUID teamId, UUID studentId, UUID actorId) {
        Task task = getTask(taskId);
        Team team = getTeam(taskId, teamId);
        User actor = getUser(actorId);
        ensureTeacherOrCaptain(team, actor);

        List<Participation> participations = participationRepository.findAllByTeamId(teamId);
        Participation selected = participations.stream()
                .filter(p -> p.getStudent().getId().equals(studentId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Student is not in this team"));

        participations.forEach(p -> p.setIsCaptain(p.getId().equals(selected.getId())));
        participationRepository.saveAll(participations);
        recalculateTeamStats(team);
        return taskTeamMapper.toDto(reloadTeam(teamId));
    }

    @Override
    @Transactional
    public TaskTeamDTO submitSolution(UUID taskId, TaskSolutionSubmitDTO dto, UUID userId) {
        Task task = getTask(taskId);
        finalizeIfDeadlineReached(task);
        ensureTaskOpen(task);

        Team team = resolveSubmissionTeam(task, dto, userId);
        Participation participation = participationRepository.findByTeamIdAndStudentId(team.getId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this team"));

        participation.setSubmittedAt(LocalDateTime.now());
        participation.setSolutionStatus(SolutionStatus.SUBMITTED);
        participationRepository.save(participation);

        recalculateTeamStats(team);
        return taskTeamMapper.toDto(reloadTeam(team.getId()));
    }

    @Override
    @Transactional
    public TaskTeamDTO voteForSolution(UUID taskId, UUID participationId, UUID userId) {
        Task task = getTask(taskId);
        finalizeIfDeadlineReached(task);
        ensureTaskOpen(task);

        Participation target = getParticipation(participationId);
        ensureParticipationInTask(target, taskId);
        if (Objects.equals(target.getStudent().getId(), userId)) {
            throw new IllegalArgumentException("You cannot vote for your own solution");
        }
        participationRepository.findByTeamIdAndStudentId(target.getTeam().getId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("Only team members can vote"));
        if (target.getSubmittedAt() == null) {
            throw new IllegalArgumentException("Only submitted solutions can be voted for");
        }
        if (voteRepository.existsByParticipationIdAndUserId(participationId, userId)) {
            throw new IllegalArgumentException("Vote already exists");
        }

        Vote vote = new Vote();
        vote.setParticipation(target);
        vote.setUser(getUser(userId));
        voteRepository.save(vote);

        return taskTeamMapper.toDto(reloadTeam(target.getTeam().getId()));
    }

    @Override
    @Transactional
    public TaskTeamDTO gradeParticipation(UUID taskId, UUID participationId, TaskParticipationGradeDTO dto, UUID teacherId) {
        Task task = getTask(taskId);
        ensureTeacherCanManageCourse(teacherId, task.getCourse());
        Participation participation = getParticipation(participationId);
        ensureParticipationInTask(participation, taskId);

        int mark = normalizeMark(dto.getMark(), task.getTotalPoints());
        participation.setMark(mark);
        participationRepository.save(participation);

        recalculateTeamStats(participation.getTeam());
        recalculateCourseGrades(task.getCourse().getId());
        return taskTeamMapper.toDto(reloadTeam(participation.getTeam().getId()));
    }

    @Override
    @Transactional
    public TaskTeamDTO gradeTeam(UUID taskId, UUID teamId, TaskTeamGradeDTO dto, UUID teacherId) {
        Task task = getTask(taskId);
        ensureTeacherCanManageCourse(teacherId, task.getCourse());
        Team team = getTeam(taskId, teamId);
        team.setCommandMark(normalizeMark(dto.getMark(), task.getTotalPoints()));
        teamRepository.save(team);

        recalculateTeamStats(team);
        recalculateCourseGrades(task.getCourse().getId());
        return taskTeamMapper.toDto(reloadTeam(teamId));
    }

    @Override
    @Transactional
    public TaskTeacherDTO finalizeTask(UUID taskId, UUID teacherId) {
        Task task = getTask(taskId);
        ensureTeacherCanManageCourse(teacherId, task.getCourse());
        finalizeTaskInternal(task);
        return taskTeacherMapper.toDto(getTask(taskId));
    }

    @Override
    @Transactional
    public void completeTask(UUID taskId, UUID userId) {
        submitSolution(taskId, TaskSolutionSubmitDTO.builder().build(), userId);
    }

    @Override
    @Transactional
    public List<TaskStudentDTO> getTasksByGroupNameReal(String groupName) {
        return getTasksByGroupName(groupName, null);
    }

    private void applyTaskDto(Task task, TaskDTO taskDTO, Course course, User creator) {
        if (!Strings.isNullOrEmpty(taskDTO.getName())) {
            task.setName(taskDTO.getName());
        }
        if (!Strings.isNullOrEmpty(taskDTO.getDescription())) {
            task.setDescription(taskDTO.getDescription());
        }
        if (taskDTO.getDeadline() != null) {
            task.setDeadline(taskDTO.getDeadline());
        }
        if (taskDTO.getTotalPoints() != null) {
            task.setTotalPoints(taskDTO.getTotalPoints());
        }
        if (taskDTO.getVotesThreshold() != null) {
            task.setVotesThreshold(taskDTO.getVotesThreshold());
        }
        if (taskDTO.getTeamsCreationTimeout() != null) {
            task.setTeamsCreationTimeout(taskDTO.getTeamsCreationTimeout());
        }
        if (taskDTO.getSubmissionClosed() != null) {
            task.setSubmissionClosed(taskDTO.getSubmissionClosed());
        } else if (task.getSubmissionClosed() == null) {
            task.setSubmissionClosed(Boolean.FALSE);
        }

        task.setCourse(course);
        task.setCreatedBy(creator);
        task.setTeamType(taskDTO.getTeamType() == null ? task.getTeamType() : taskDTO.getTeamType());
        task.setResolveType(taskDTO.getResolveType() == null ? task.getResolveType() : taskDTO.getResolveType());

        validateTaskConfiguration(taskDTO, task);

        task.setMinTeamSize(resolveInteger(taskDTO.getMinTeamSize(), task.getMinTeamSize()));
        task.setMaxTeamSize(resolveInteger(taskDTO.getMaxTeamSize(), task.getMaxTeamSize()));
        task.setMinTeamsAmount(resolveInteger(taskDTO.getMinTeamsAmount(), task.getMinTeamsAmount()));
        task.setMaxTeamsAmount(resolveInteger(taskDTO.getMaxTeamsAmount(), task.getMaxTeamsAmount()));
    }

    private void validateTaskConfiguration(TaskDTO taskDTO, Task task) {
        TeamType teamType = taskDTO.getTeamType() == null ? task.getTeamType() : taskDTO.getTeamType();
        TaskResolveType resolveType = taskDTO.getResolveType() == null ? task.getResolveType() : taskDTO.getResolveType();

        if (teamType == null || resolveType == null) {
            throw new IllegalArgumentException("Team type and solution type are required");
        }

        Integer minTeamSize = resolveInteger(taskDTO.getMinTeamSize(), task.getMinTeamSize());
        Integer maxTeamSize = resolveInteger(taskDTO.getMaxTeamSize(), task.getMaxTeamSize());
        Integer minTeamsAmount = resolveInteger(taskDTO.getMinTeamsAmount(), task.getMinTeamsAmount());
        Integer maxTeamsAmount = resolveInteger(taskDTO.getMaxTeamsAmount(), task.getMaxTeamsAmount());

        if (maxTeamSize != null && maxTeamSize == 1) {
            minTeamSize = 1;
            maxTeamSize = 1;
        }

        if (minTeamSize != null && maxTeamSize != null && minTeamSize > maxTeamSize) {
            throw new IllegalArgumentException("minTeamSize must be less than or equal to maxTeamSize");
        }
        if (minTeamsAmount != null && maxTeamsAmount != null && minTeamsAmount > maxTeamsAmount) {
            throw new IllegalArgumentException("minTeamsAmount must be less than or equal to maxTeamsAmount");
        }
        if ((resolveType == TaskResolveType.AT_LEAST_VOTES_SOLUTION || resolveType == TaskResolveType.MOST_VOTES_SOLUTION)
                && taskDTO.getVotesThreshold() == null
                && task.getVotesThreshold() == null) {
            throw new IllegalArgumentException("votesThreshold is required for vote-based solution resolution");
        }
        if (teamType == TeamType.DRAFT && task.getTeamsCreationTimeout() == null && taskDTO.getTeamsCreationTimeout() == null) {
            throw new IllegalArgumentException("teamsCreationTimeout is required for draft team creation");
        }

        task.setTeamType(teamType);
        task.setResolveType(resolveType);
        task.setMinTeamSize(minTeamSize);
        task.setMaxTeamSize(maxTeamSize);
        task.setMinTeamsAmount(minTeamsAmount);
        task.setMaxTeamsAmount(maxTeamsAmount);
        if (teamType != TeamType.DRAFT) {
            task.setTeamsCreationTimeout(null);
        }
    }

    private Task resolveTaskById(UUID taskId) {
        Task task = getTask(taskId);
        finalizeIfDeadlineReached(task);
        return task;
    }

    private Task getTask(UUID taskId) {
        return taskRepository.findById(taskId).orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));
    }

    private Course getCourse(UUID courseId) {
        return courseRepository.findById(courseId).orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }

    private Team getTeam(UUID taskId, UUID teamId) {
        return teamRepository.findByIdAndTaskId(teamId, taskId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found in task: " + teamId));
    }

    private Participation getParticipation(UUID participationId) {
        return participationRepository.findById(participationId)
                .orElseThrow(() -> new IllegalArgumentException("Participation not found: " + participationId));
    }

    private void ensureTeacherCanManageCourse(UUID teacherId, Course course) {
        if (course.getTeacher() == null || !course.getTeacher().getId().equals(teacherId)) {
            throw new IllegalArgumentException("Only the course teacher can manage this task");
        }
    }

    private void ensureStudentInCourse(UUID courseId, UUID studentId) {
        if (!studentsInCourseRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new IllegalArgumentException("Student is not assigned to this course");
        }
    }

    private void ensureTaskOpen(Task task) {
        if (Boolean.TRUE.equals(task.getSubmissionClosed())) {
            throw new IllegalArgumentException("Task submissions are closed");
        }
    }

    private void ensureCanCreateTeam(Task task, User actor) {
        boolean teacher = actor.getRole() == Role.TEACHER && task.getCourse().getTeacher().getId().equals(actor.getId());
        boolean student = actor.getRole() == Role.STUDENT;

        if (teacher) {
            return;
        }

        if (!student) {
            throw new IllegalArgumentException("Only teacher or students can create teams");
        }

        if (task.getTeamType() != TeamType.FREEROAM && task.getTeamType() != TeamType.DRAFT && !isSoloTask(task)) {
            throw new IllegalArgumentException("Students cannot create teams for this task type");
        }
    }

    private void ensureTeamSlotsAvailable(Task task) {
        if (task.getMaxTeamsAmount() != null && teamRepository.countByTaskId(task.getId()) >= task.getMaxTeamsAmount()) {
            throw new IllegalArgumentException("Maximum amount of teams has been reached");
        }
    }

    private void ensureStudentHasNoTeam(UUID taskId, UUID studentId) {
        boolean exists = participationRepository.findAllByTeamTaskId(taskId).stream()
                .anyMatch(p -> p.getStudent().getId().equals(studentId));
        if (exists) {
            throw new IllegalArgumentException("Student already belongs to a team in this task");
        }
    }

    private void ensureTeamCapacity(Task task, Team team) {
        List<Participation> participations = participationRepository.findAllByTeamId(team.getId());
        if (task.getMaxTeamSize() != null && participations.size() >= task.getMaxTeamSize()) {
            throw new IllegalArgumentException("Team is already full");
        }
    }

    private void ensureCanAssignStudent(Task task, Team team, User actor, UUID studentId) {
        boolean teacher = actor.getRole() == Role.TEACHER && task.getCourse().getTeacher().getId().equals(actor.getId());
        boolean selfJoin = actor.getId().equals(studentId);

        if (teacher) {
            return;
        }

        if (task.getTeamType() == TeamType.DRAFT) {
            boolean captain = participationRepository.findByTeamIdAndStudentId(team.getId(), actor.getId())
                    .map(Participation::getIsCaptain)
                    .orElse(false);
            if (!captain) {
                throw new IllegalArgumentException("Only captain can draft students to the team");
            }
            return;
        }

        if (!selfJoin) {
            throw new IllegalArgumentException("Students can only add themselves");
        }

        if (task.getTeamType() != TeamType.FREEROAM && !isSoloTask(task)) {
            throw new IllegalArgumentException("This task requires teacher-managed team assignment");
        }
    }

    private void ensureTeacherOrCaptain(Team team, User actor) {
        boolean teacher = actor.getRole() == Role.TEACHER && team.getTask().getCourse().getTeacher().getId().equals(actor.getId());
        boolean captain = participationRepository.findByTeamIdAndStudentId(team.getId(), actor.getId())
                .map(Participation::getIsCaptain)
                .orElse(false);
        if (!teacher && !captain) {
            throw new IllegalArgumentException("Only teacher or captain can manage the team");
        }
    }

    private Participation createParticipation(Team team, User student, boolean isCaptain) {
        Participation participation = new Participation();
        participation.setTeam(team);
        participation.setStudent(student);
        participation.setIsCaptain(isCaptain);
        participation.setMark(null);
        participation.setAverageMark(0D);
        participation.setSolutionStatus(SolutionStatus.DRAFT);
        return participationRepository.save(participation);
    }

    private Team withCaptain(Team team, Participation captainParticipation) {
        team.setParticipationList(List.of(captainParticipation));
        return team;
    }

    private Team reloadTeam(UUID teamId) {
        return teamRepository.findById(teamId).orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId));
    }

    private Team resolveSubmissionTeam(Task task, TaskSolutionSubmitDTO dto, UUID userId) {
        if (dto.getTeamId() != null) {
            return getTeam(task.getId(), dto.getTeamId());
        }
        return participationRepository.findAllByTeamTaskId(task.getId()).stream()
                .filter(participation -> participation.getStudent().getId().equals(userId))
                .map(Participation::getTeam)
                .findFirst()
                .orElseGet(() -> {
                    if (!isSoloTask(task)) {
                        throw new IllegalArgumentException("User is not assigned to a team");
                    }
                    return createTeam(task.getId(), TaskTeamCreateDTO.builder().build(), userId) == null ? null : getSoloTeam(task.getId(), userId);
                });
    }

    private Team getSoloTeam(UUID taskId, UUID userId) {
        return participationRepository.findAllByTeamTaskId(taskId).stream()
                .filter(participation -> participation.getStudent().getId().equals(userId))
                .map(Participation::getTeam)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Solo team was not created"));
    }

    private void ensureParticipationInTask(Participation participation, UUID taskId) {
        if (participation.getTeam() == null || participation.getTeam().getTask() == null
                || !participation.getTeam().getTask().getId().equals(taskId)) {
            throw new IllegalArgumentException("Participation does not belong to this task");
        }
    }

    private void finalizeIfDeadlineReached(Task task) {
        if (task.getDeadline() != null && LocalDate.now().isAfter(task.getDeadline()) && !Boolean.TRUE.equals(task.getSubmissionClosed())) {
            finalizeTaskInternal(task);
        }
    }

    private void finalizeTaskInternal(Task task) {
        task.setSubmissionClosed(Boolean.TRUE);
        task.setFinalizedAt(LocalDateTime.now());

        List<Team> teams = teamRepository.findAllByTaskId(task.getId());
        for (Team team : teams) {
            List<Participation> participations = participationRepository.findAllByTeamId(team.getId());
            Participation selected = resolveFinalParticipation(task, participations);
            team.setSolutionParticipation(selected);
            teamRepository.save(team);

            for (Participation participation : participations) {
                if (selected != null && selected.getId().equals(participation.getId())) {
                    participation.setSolutionStatus(SolutionStatus.LOCKED);
                } else if (participation.getSubmittedAt() != null || participation.getSolutionStatus() == SolutionStatus.SUBMITTED) {
                    participation.setSolutionStatus(SolutionStatus.OVERDUE);
                }
            }
            participationRepository.saveAll(participations);
            recalculateTeamStats(team);
        }

        taskRepository.save(task);
        recalculateCourseGrades(task.getCourse().getId());
    }

    private Participation resolveFinalParticipation(Task task, List<Participation> participations) {
        List<Participation> submitted = participations.stream()
                .filter(participation -> participation.getSubmittedAt() != null)
                .toList();
        if (submitted.isEmpty()) {
            return null;
        }

        return switch (task.getResolveType()) {
            case FIRST_SUBMITTED_SOLUTION -> submitted.stream()
                    .min(Comparator.comparing(Participation::getSubmittedAt))
                    .orElse(null);
            case LAST_SUBMITTED_SOLUTION -> submitted.stream()
                    .max(Comparator.comparing(Participation::getSubmittedAt))
                    .orElse(null);
            case CAPTAINS_SOLUTION -> submitted.stream()
                    .filter(Participation::getIsCaptain)
                    .max(Comparator.comparing(Participation::getSubmittedAt))
                    .orElse(submitted.stream().max(Comparator.comparing(Participation::getSubmittedAt)).orElse(null));
            case MOST_VOTES_SOLUTION -> submitted.stream()
                    .max(Comparator.comparingLong((Participation p) -> voteRepository.countByParticipationId(p.getId()))
                            .thenComparing(Participation::getSubmittedAt))
                    .orElse(null);
            case AT_LEAST_VOTES_SOLUTION -> submitted.stream()
                    .filter(p -> voteRepository.countByParticipationId(p.getId()) >= (task.getVotesThreshold() == null ? 0 : task.getVotesThreshold()))
                    .max(Comparator.comparing(Participation::getSubmittedAt))
                    .orElseGet(() -> submitted.stream()
                            .max(Comparator.comparingLong((Participation p) -> voteRepository.countByParticipationId(p.getId()))
                                    .thenComparing(Participation::getSubmittedAt))
                            .orElse(null));
        };
    }

    private void recalculateTeamStats(Team team) {
        List<Participation> participations = participationRepository.findAllByTeamId(team.getId());
        double teamMark = team.getCommandMark() == null ? 0D : team.getCommandMark();
        for (Participation participation : participations) {
            double individualMark = participation.getMark() == null ? 0D : participation.getMark();
            if (participation.getMark() != null || team.getCommandMark() != null) {
                participation.setAverageMark(round((individualMark + teamMark) / 2D));
            }
        }
        participationRepository.saveAll(participations);

        team.setAverageMark(round(participations.stream()
                .map(Participation::getAverageMark)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0D)));
        teamRepository.save(team);
    }

    private void recalculateCourseGrades(UUID courseId) {
        List<StudentsInCourse> courseStudents = studentsInCourseRepository.findAllByCourseId(courseId);
        for (StudentsInCourse relation : courseStudents) {
            List<Participation> participations = participationRepository.findAllByStudentId(relation.getStudent().getId()).stream()
                    .filter(p -> p.getTeam() != null
                            && p.getTeam().getTask() != null
                            && p.getTeam().getTask().getCourse() != null
                            && p.getTeam().getTask().getCourse().getId().equals(courseId))
                    .filter(p -> p.getAverageMark() != null)
                    .toList();
            double average = participations.stream()
                    .mapToDouble(Participation::getAverageMark)
                    .average()
                    .orElse(0D);
            relation.setCourseGrade(round(average));
        }
        studentsInCourseRepository.saveAll(courseStudents);
    }

    private Integer normalizeMark(Integer mark, Integer totalPoints) {
        if (mark == null) {
            throw new IllegalArgumentException("Mark is required");
        }
        if (mark < 0) {
            throw new IllegalArgumentException("Mark cannot be negative");
        }
        if (totalPoints != null && mark > totalPoints) {
            throw new IllegalArgumentException("Mark cannot exceed task total points");
        }
        return mark;
    }

    private String resolveTeamName(Task task, TaskTeamCreateDTO dto, User captain) {
        if (isSoloTask(task)) {
            return captain.getFirstName() + " " + captain.getLastName();
        }
        if (!Strings.isNullOrEmpty(dto.getName())) {
            return dto.getName();
        }
        return "Команда " + (teamRepository.countByTaskId(task.getId()) + 1);
    }

    private boolean isSoloTask(Task task) {
        return task.getMaxTeamSize() != null && task.getMaxTeamSize() == 1;
    }

    private Integer resolveInteger(Integer requested, Integer current) {
        return requested != null ? requested : current;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
