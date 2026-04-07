package com.hits.language_school_back.mapper;

import com.hits.language_school_back.dto.TaskStudentDTO;
import com.hits.language_school_back.enums.TaskStatus;
import com.hits.language_school_back.model.Task;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class TaskStudentMapper {

    public TaskStudentDTO toDto(Task task) {
        if (task == null) {
            return null;
        }

        TaskStudentDTO dto = new TaskStudentDTO();
        dto.setId(task.getId());
        dto.setName(task.getName());
        dto.setDescription(task.getDescription());
        dto.setDeadline(task.getDeadline());
        dto.setTaskStatus(resolveStatus(task));
        return dto;
    }

    public List<TaskStudentDTO> toDtoList(List<Task> tasks) {
        if (tasks == null) {
            return null;
        }

        return tasks.stream().map(this::toDto).toList();
    }

    private TaskStatus resolveStatus(Task task) {
        if (task.getDeadline() == null) {
            return TaskStatus.PENDING;
        }
        return task.getDeadline().isBefore(LocalDate.now()) ? TaskStatus.OVERDUE : TaskStatus.PENDING;
    }
}
