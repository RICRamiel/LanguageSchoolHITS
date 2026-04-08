package com.hits.language_school_back.mapper;

import com.hits.language_school_back.dto.TaskParticipationDTO;
import com.hits.language_school_back.dto.TaskTeamDTO;
import com.hits.language_school_back.model.Participation;
import com.hits.language_school_back.model.Team;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class TaskTeamMapper {

    private final TaskParticipationMapper participationMapper;

    public TaskTeamMapper(TaskParticipationMapper participationMapper) {
        this.participationMapper = participationMapper;
    }

    public TaskTeamDTO toDto(Team team) {
        List<Participation> participations = team.getParticipationList() == null
                ? List.of()
                : team.getParticipationList().stream()
                .sorted(Comparator.comparing(Participation::getSubmittedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();

        List<TaskParticipationDTO> participationDtos = participations.stream()
                .map(participationMapper::toDto)
                .toList();

        return TaskTeamDTO.builder()
                .id(team.getId())
                .name(team.getName())
                .commandMark(team.getCommandMark())
                .averageMark(team.getAverageMark())
                .membersCount(participationDtos.size())
                .captainId(participations.stream()
                        .filter(Participation::getIsCaptain)
                        .map(p -> p.getStudent().getId())
                        .findFirst()
                        .orElse(null))
                .solutionParticipationId(team.getSolutionParticipation() == null ? null : team.getSolutionParticipation().getId())
                .participations(participationDtos)
                .build();
    }
}
