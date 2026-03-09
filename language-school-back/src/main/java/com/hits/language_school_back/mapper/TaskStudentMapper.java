package com.hits.language_school_back.mapper;

import com.hits.language_school_back.dto.TaskStudentDTO;
import com.hits.language_school_back.enums.TaskStatus;
import com.hits.language_school_back.model.Task;
import com.hits.language_school_back.model.TaskStudent;
import com.hits.language_school_back.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TaskStudentMapper {

    private final TaskRepository taskRepository;
    private final UserMapper userMapper;

    public TaskStudentDTO toDto(TaskStudent taskStudent) {
        if (taskStudent == null) {
            return null;
        }

        TaskStudentDTO dto = new TaskStudentDTO();
        Task task = taskRepository.findById(taskStudent.getTaskId()).orElseThrow();
        dto.setId(taskStudent.getTaskId());
        dto.setName(task.getName());
        dto.setDescription(task.getDescription());
        dto.setDeadline(task.getDeadline());
        dto.setTaskStatus(taskStudent.getTaskStatus());
        dto.setTeacher(userMapper.userToUserDto(task.getUser()));

        if (task.getDeadline().isBefore(LocalDate.now()) && task.getTaskStatus() != TaskStatus.COMPLETE) {
            task.setTaskStatus(TaskStatus.OVERDUE);
            dto.setTaskStatus(TaskStatus.OVERDUE);
        }

        if (task.getUser() != null) {
            dto.setTeacher(userMapper.userToUserDto(task.getUser()));
        }

        return dto;
    }

    public List<TaskStudentDTO> toDtoList(List<TaskStudent> tasks) {
        if (tasks == null) {
            return null;
        }

        return tasks.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}