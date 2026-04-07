package com.hits.language_school_back.service;

import com.hits.language_school_back.dto.TaskDTO;
import com.hits.language_school_back.dto.TaskStudentDTO;
import com.hits.language_school_back.dto.TaskTeacherDTO;
import com.hits.language_school_back.dto.UserFullDTO;
import com.hits.language_school_back.model.Task;

import java.util.List;
import java.util.UUID;

public interface TaskService {
    List<TaskTeacherDTO> getTasksByTeacherId(UUID teacherId);
    List<TaskStudentDTO> getTasksByGroupName(String name, UUID userId);
    Task createTask(TaskDTO taskDTO, UserFullDTO userFullDTO);
    void deleteTask(UUID id);
    Task editTask(TaskDTO taskDTO, UUID taskId);

    void completeTask(UUID taskId, UUID userId);

    List<TaskStudentDTO> getTasksByGroupNameReal(String groupName);
}
