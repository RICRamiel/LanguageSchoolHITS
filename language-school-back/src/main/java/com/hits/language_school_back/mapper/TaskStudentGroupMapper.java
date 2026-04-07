package com.hits.language_school_back.mapper;

import com.hits.language_school_back.dto.TaskStudentDTO;
import com.hits.language_school_back.model.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TaskStudentGroupMapper {
    private final TaskStudentMapper taskStudentMapper;

    public TaskStudentDTO toDto(Task task, UUID userId) {
        return taskStudentMapper.toDto(task, userId);
    }

    public List<TaskStudentDTO> toDtoList(List<Task> tasks, UUID userId) {
        return taskStudentMapper.toDtoList(tasks, userId);
    }
}
