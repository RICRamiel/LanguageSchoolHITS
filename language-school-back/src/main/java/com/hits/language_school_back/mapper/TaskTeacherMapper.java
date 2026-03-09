package com.hits.language_school_back.mapper;

import com.hits.language_school_back.dto.TaskTeacherDTO;
import com.hits.language_school_back.model.Task;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Component
public class TaskTeacherMapper {

    private final CommentMapper commentMapper;

    public TaskTeacherDTO toDto(Task task) {
        if (task == null) {
            return null;
        }

        TaskTeacherDTO dto = new TaskTeacherDTO();
        dto.setId(task.getId());
        dto.setName(task.getName());
        dto.setDescription(task.getDescription());
        dto.setDeadline(task.getDeadline());
        dto.setCommentList(task.getCommentList().stream().map(commentMapper::toDto).toList());

        return dto;
    }

    public List<TaskTeacherDTO> toDtoList(List<Task> tasks) {
        if (tasks == null) {
            return null;
        }

        return tasks.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}