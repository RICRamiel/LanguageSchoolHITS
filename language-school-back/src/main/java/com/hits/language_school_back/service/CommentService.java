package com.hits.language_school_back.service;

import com.hits.language_school_back.dto.CommentDTO;
import com.hits.language_school_back.model.Comment;

import java.util.List;

public interface CommentService {
    List<CommentDTO> getCommentsByTaskId(Long taskId);
    Comment createComment(CommentDTO commentDTO);
    Comment editComment(CommentDTO commentDTO, Long commentId);
    void deleteComment(Long commentId);
}
