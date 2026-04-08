package com.hits.language_school_back.mapper;

import com.hits.language_school_back.dto.TaskStudentDTO;
import com.hits.language_school_back.enums.SolutionStatus;
import com.hits.language_school_back.enums.TaskStatus;
import com.hits.language_school_back.model.Participation;
import com.hits.language_school_back.model.Task;
import com.hits.language_school_back.model.Team;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
public class TaskStudentMapper {

    private final TaskTeamMapper taskTeamMapper;
    private final UserMapper userMapper;

    public TaskStudentMapper(TaskTeamMapper taskTeamMapper, UserMapper userMapper) {
        this.taskTeamMapper = taskTeamMapper;
        this.userMapper = userMapper;
    }

    public TaskStudentDTO toDto(Task task, UUID userId) {
        return TaskStudentDTO.builder()
                .id(task.getId())
                .name(task.getName())
                .description(task.getDescription())
                .deadline(task.getDeadline())
                .courseId(task.getCourse() == null ? null : task.getCourse().getId())
                .courseName(task.getCourse() == null ? null : task.getCourse().getName())
                .totalPoints(task.getTotalPoints())
                .teamType(task.getTeamType())
                .resolveType(task.getResolveType())
                .submissionClosed(task.getSubmissionClosed())
                .taskStatus(resolveStatus(task, userId))
                .teacher(task.getCourse() == null || task.getCourse().getTeacher() == null ? null : userMapper.userToUserDto(task.getCourse().getTeacher()))
                .currentTeamId(resolveCurrentTeamId(task, userId))
                .finalizedAt(task.getFinalizedAt())
                .teams(task.getTeamList() == null ? List.of() : task.getTeamList().stream().map(taskTeamMapper::toDto).toList())
                .build();
    }

    public List<TaskStudentDTO> toDtoList(List<Task> tasks, UUID userId) {
        if (tasks == null) {
            return null;
        }

        return tasks.stream().map(task -> toDto(task, userId)).toList();
    }

    private TaskStatus resolveStatus(Task task, UUID userId) {
        Participation participation = findParticipation(task, userId);
        if (participation != null && (participation.getSolutionStatus() == SolutionStatus.SELECTED || participation.getSolutionStatus() == SolutionStatus.LOCKED)) {
            return TaskStatus.COMPLETE;
        }
        if (task.getSubmissionClosed() || (task.getDeadline() != null && task.getDeadline().isBefore(LocalDate.now()))) {
            return TaskStatus.OVERDUE;
        }
        return TaskStatus.PENDING;
    }

    private UUID resolveCurrentTeamId(Task task, UUID userId) {
        Participation participation = findParticipation(task, userId);
        return participation == null || participation.getTeam() == null ? null : participation.getTeam().getId();
    }

    private Participation findParticipation(Task task, UUID userId) {
        if (task.getTeamList() == null) {
            return null;
        }
        for (Team team : task.getTeamList()) {
            if (team.getParticipationList() == null) {
                continue;
            }
            for (Participation participation : team.getParticipationList()) {
                if (participation.getStudent() != null && participation.getStudent().getId().equals(userId)) {
                    return participation;
                }
            }
        }
        return null;
    }
}
