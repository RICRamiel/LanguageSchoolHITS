package com.hits.language_school_back.infrastructure;

import com.hits.language_school_back.dto.CommentDTO;
import com.hits.language_school_back.service.CommentService;
import com.hits.language_school_back.model.Comment;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {
    @Override
    public List<CommentDTO> getCommentsByTaskId(Long taskId) {
        return List.of();
    }

    @Override
    public Comment createComment(CommentDTO commentDTO) {
        return null;
    }

    @Override
    public Comment editComment(CommentDTO commentDTO, Long commentId) {
        return null;
    }

    @Override
    public void deleteComment(Long commentId) {

    }
}
