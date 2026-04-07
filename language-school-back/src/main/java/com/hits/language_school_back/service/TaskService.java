package com.hits.language_school_back.service;

import com.hits.language_school_back.dto.TaskDTO;
import com.hits.language_school_back.dto.TaskParticipationGradeDTO;
import com.hits.language_school_back.dto.TaskSolutionSubmitDTO;
import com.hits.language_school_back.dto.TaskStudentDTO;
import com.hits.language_school_back.dto.TaskTeacherDTO;
import com.hits.language_school_back.dto.TaskTeamCreateDTO;
import com.hits.language_school_back.dto.TaskTeamDTO;
import com.hits.language_school_back.dto.TaskTeamGradeDTO;
import com.hits.language_school_back.dto.UserFullDTO;
import com.hits.language_school_back.model.Task;

import java.util.List;
import java.util.UUID;

public interface TaskService {
    List<TaskTeacherDTO> getTasksByTeacherId(UUID teacherId);

    List<TaskStudentDTO> getTasksByGroupName(String name, UUID userId);

    List<TaskStudentDTO> getTasksByCourseId(UUID courseId, UUID userId);

    Task createTask(TaskDTO taskDTO, UserFullDTO userFullDTO);

    void deleteTask(UUID id);

    Task editTask(TaskDTO taskDTO, UUID taskId);

    TaskTeamDTO createTeam(UUID taskId, TaskTeamCreateDTO dto, UUID userId);

    TaskTeamDTO joinTeam(UUID taskId, UUID teamId, UUID userId);

    TaskTeamDTO addStudentToTeam(UUID taskId, UUID teamId, UUID studentId, UUID actorId);

    TaskTeamDTO appointCaptain(UUID taskId, UUID teamId, UUID studentId, UUID actorId);

    TaskTeamDTO submitSolution(UUID taskId, TaskSolutionSubmitDTO dto, UUID userId);

    TaskTeamDTO voteForSolution(UUID taskId, UUID participationId, UUID userId);

    TaskTeamDTO gradeParticipation(UUID taskId, UUID participationId, TaskParticipationGradeDTO dto, UUID teacherId);

    TaskTeamDTO gradeTeam(UUID taskId, UUID teamId, TaskTeamGradeDTO dto, UUID teacherId);

    TaskTeacherDTO finalizeTask(UUID taskId, UUID teacherId);

    void completeTask(UUID taskId, UUID userId);

    List<TaskStudentDTO> getTasksByGroupNameReal(String groupName);
}
