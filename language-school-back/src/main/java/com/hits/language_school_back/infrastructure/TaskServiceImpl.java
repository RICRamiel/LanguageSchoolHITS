package com.hits.language_school_back.infrastructure;

import com.google.common.base.Strings;
import com.hits.language_school_back.dto.TaskDTO;
import com.hits.language_school_back.dto.TaskStudentDTO;
import com.hits.language_school_back.dto.TaskTeacherDTO;
import com.hits.language_school_back.dto.UserFullDTO;
import com.hits.language_school_back.mapper.TaskStudentMapper;
import com.hits.language_school_back.mapper.TaskTeacherMapper;
import com.hits.language_school_back.model.Task;
import com.hits.language_school_back.repository.TaskRepository;
import com.hits.language_school_back.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskTeacherMapper taskTeacherMapper;
    private final TaskStudentMapper taskStudentMapper;

    @Override
    public List<TaskTeacherDTO> getTasksByTeacherId(UUID teacherId) {
        return taskTeacherMapper.toDtoList(taskRepository.findAllByOrderByDeadlineAsc());
    }

    @Override
    public List<TaskStudentDTO> getTasksByGroupName(String name, UUID userId) {
        return taskStudentMapper.toDtoList(taskRepository.findAllByOrderByDeadlineAsc());
    }

    @Override
    public Task createTask(TaskDTO taskDTO, UserFullDTO userFullDTO) {
        Task task = new Task();
        task.setName(taskDTO.getName());
        task.setDescription(taskDTO.getDescription());
        task.setDeadline(taskDTO.getDeadline());
        return taskRepository.save(task);
    }

    @Override
    public void deleteTask(UUID id) {
        taskRepository.deleteById(id);
    }

    @Override
    public Task editTask(TaskDTO taskDTO, UUID taskId) {
        Task task = taskRepository.findById(taskId).orElseThrow();

        if (!Strings.isNullOrEmpty(taskDTO.getName())) {
            task.setName(taskDTO.getName());
        }
        if (!Strings.isNullOrEmpty(taskDTO.getDescription())) {
            task.setDescription(taskDTO.getDescription());
        }
        if (taskDTO.getDeadline() != null) {
            task.setDeadline(taskDTO.getDeadline());
        }

        return taskRepository.save(task);
    }

    @Override
    public void completeTask(UUID taskId, UUID userId) {
        if (!taskRepository.existsById(taskId)) {
            throw new RuntimeException("Task not found: " + taskId);
        }
        log.debug("Completion requested for task {} by user {}", taskId, userId);
    }

    @Override
    public List<TaskStudentDTO> getTasksByGroupNameReal(String groupName) {
        return taskStudentMapper.toDtoList(taskRepository.findAllByOrderByDeadlineAsc());
    }
}
