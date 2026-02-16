package com.hits.language_school_back.inteface;

import com.hits.language_school_back.dto.CommentDTO;

import java.util.List;

public interface CommentService {
    List<CommentDTO> getCommentsByTaskId(Long taskId);
    void createComment(CommentDTO commentDTO);
    void editComment(CommentDTO commentDTO);
    void deleteComment(Long commentId);
}
