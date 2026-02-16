package com.hits.language_school_back.inteface;

import com.hits.language_school_back.dto.TaskDTO;
import com.hits.language_school_back.dto.TaskStudentDTO;
import com.hits.language_school_back.dto.TaskTeacherDTO;

import java.util.List;

public interface TaskService {
    List<TaskTeacherDTO> getTasksByTeacherId(Long TeacherId);
    List<TaskStudentDTO> getTasksByGroupName(String name);
    void createTask(TaskDTO taskDTO);
    void deleteTask(Long id);
    void editTask(TaskDTO taskDTO);
}
