package com.hits.language_school_back.mapper;

import com.hits.language_school_back.dto.TaskDTO;
import com.hits.language_school_back.model.Task;
import org.springframework.stereotype.Component;


@Component
public class TaskMapper {
    public TaskDTO toDto(Task task) {

        return TaskDTO.builder()
                .deadline(task.getDeadline())
                .description(task.getDescription())
                .name(task.getName())
                .build();
    }
}
