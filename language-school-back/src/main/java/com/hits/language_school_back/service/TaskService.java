package com.hits.language_school_back.service;

import com.hits.language_school_back.dto.TaskDTO;
import com.hits.language_school_back.dto.TaskStudentDTO;
import com.hits.language_school_back.dto.TaskTeacherDTO;
import com.hits.language_school_back.dto.UserFullDTO;
import com.hits.language_school_back.model.Task;

import java.util.List;

public interface TaskService {
    List<TaskTeacherDTO> getTasksByTeacherId(Long TeacherId);
    List<TaskStudentDTO> getTasksByGroupName(String name, Long userId);
    Task createTask(TaskDTO taskDTO, UserFullDTO userFullDTO);
    void deleteTask(Long id);
    Task editTask(TaskDTO taskDTO, Long taskId);

    void completeTask(Long taskId, Long userId);
}
