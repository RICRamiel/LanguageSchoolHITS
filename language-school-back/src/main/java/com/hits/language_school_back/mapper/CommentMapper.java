package com.hits.language_school_back.mapper;

import com.hits.language_school_back.dto.CommentDTO;
import com.hits.language_school_back.model.Comment;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {
    public CommentDTO toDto(Comment model) {

        return CommentDTO.builder()
                .privateStatus(model.isPrivateStatus())
                .taskId(model.getTask().getId())
                .userId(model.getUser().getId())
                .text(model.getText())
                .build();
    }
}