package com.hits.language_school_back.infrastructure;

import com.hits.language_school_back.dto.TaskDTO;
import com.hits.language_school_back.dto.TaskStudentDTO;
import com.hits.language_school_back.dto.TaskTeacherDTO;
import com.hits.language_school_back.service.TaskService;
import com.hits.language_school_back.model.Task;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {
    @Override
    public List<TaskTeacherDTO> getTasksByTeacherId(Long TeacherId) {
        return List.of();
    }

    @Override
    public List<TaskStudentDTO> getTasksByGroupName(String name) {
        return List.of();
    }

    @Override
    public Task createTask(TaskDTO taskDTO) {
        return null;
    }

    @Override
    public void deleteTask(Long id) {

    }

    @Override
    public Task editTask(TaskDTO taskDTO, Long taskId) {
        return null;
    }
}
