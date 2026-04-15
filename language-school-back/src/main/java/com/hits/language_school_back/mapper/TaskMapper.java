package com.hits.language_school_back.mapper;

import com.hits.language_school_back.dto.TaskDTO;
import com.hits.language_school_back.model.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {
    public TaskDTO toDto(Task task) {
        return TaskDTO.builder()
                .id(task.getId())
                .deadline(task.getDeadline())
                .description(task.getDescription())
                .name(task.getName())
                .courseId(task.getCourse() == null ? null : task.getCourse().getId())
                .courseName(task.getCourse() == null ? null : task.getCourse().getName())
                .totalPoints(task.getTotalPoints())
                .maxTeamSize(task.getMaxTeamSize())
                .minTeamSize(task.getMinTeamSize())
                .maxTeamsAmount(task.getMaxTeamsAmount())
                .minTeamsAmount(task.getMinTeamsAmount())
                .votesThreshold(task.getVotesThreshold())
                .teamsCreationTimeout(task.getTeamsCreationTimeout())
                .teamType(task.getTeamType())
                .resolveType(task.getResolveType())
                .submissionClosed(task.getSubmissionClosed())
                .build();
    }
}
