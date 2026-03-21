package com.hits.language_school_back.mapper;

import com.hits.language_school_back.dto.TaskDTO;
import com.hits.language_school_back.model.Task;
import com.hits.language_school_back.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class TaskMapper {

    private final TaskRepository taskRepository;

    public TaskDTO toDto(Task task) {

        return TaskDTO.builder()
                .groupName(task.getGroup().getName())
                .deadline(task.getDeadline())
                .description(task.getDescription())
                .name(task.getName())
                .build();
    }
}