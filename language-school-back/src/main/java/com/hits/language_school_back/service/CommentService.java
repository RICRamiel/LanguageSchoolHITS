package com.hits.language_school_back.service;

import com.hits.language_school_back.dto.CommentDTO;
import com.hits.language_school_back.model.Comment;

import java.util.List;
import java.util.UUID;

public interface CommentService {
    List<CommentDTO> getCommentsByTaskId(UUID taskId);
    Comment createComment(CommentDTO commentDTO);
    Comment editComment(CommentDTO commentDTO, UUID commentId);
    void deleteComment(UUID commentId);
}
