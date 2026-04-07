package com.hits.language_school_back.infrastructure;

import com.hits.language_school_back.dto.CommentDTO;
import com.hits.language_school_back.model.Comment;
import com.hits.language_school_back.model.Task;
import com.hits.language_school_back.model.User;
import com.hits.language_school_back.repository.CommentRepository;
import com.hits.language_school_back.repository.TaskRepository;
import com.hits.language_school_back.repository.UserRepository;
import com.hits.language_school_back.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    @Override
    public List<CommentDTO> getCommentsByTaskId(UUID taskId) {
        return commentRepository.findByTaskId(taskId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Comment createComment(CommentDTO commentDTO) {
        User user = userRepository.findById(commentDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + commentDTO.getUserId()));

        Task task = taskRepository.findById(commentDTO.getTaskId())
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + commentDTO.getTaskId()));

        Comment comment = new Comment();
        comment.setText(commentDTO.getText());
        comment.setUser(user);
        comment.setTask(task);
        comment.setPrivateStatus(commentDTO.isPrivateStatus());

        return commentRepository.save(comment);
    }

    @Override
    @Transactional
    public Comment editComment(CommentDTO commentDTO, UUID commentId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

        if (commentDTO.getText() != null) {
            comment.setText(commentDTO.getText());
        }
        comment.setPrivateStatus(commentDTO.isPrivateStatus());
        return commentRepository.save(comment);
    }

    @Override
    public void deleteComment(UUID commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new RuntimeException("Comment not found with id: " + commentId);
        }
        commentRepository.deleteById(commentId);
    }

    private CommentDTO convertToDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setUserId(comment.getUser().getId());
        dto.setTaskId(comment.getTask().getId());
        dto.setPrivateStatus(comment.isPrivateStatus());
        return dto;
    }
}
