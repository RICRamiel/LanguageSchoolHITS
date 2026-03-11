package com.hits.language_school_back.mapper;

import com.hits.language_school_back.dto.TaskStudentDTO;
import com.hits.language_school_back.model.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TaskStudentGroupMapper {

    public TaskStudentDTO toDto(Task task) {

        return TaskStudentDTO.builder()
                .taskStatus(task.getTaskStatus())
                .id(task.getId())
                .deadline(task.getDeadline())
                .description(task.getDescription())
                .name(task.getName())
                .build();
    }

    public List<TaskStudentDTO> toDtoList(List<Task> tasks) {
        if (tasks == null) {
            return null;
        }

        return tasks.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
