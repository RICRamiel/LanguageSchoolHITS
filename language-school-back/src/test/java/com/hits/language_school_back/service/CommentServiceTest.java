package com.hits.language_school_back.service;

import com.hits.language_school_back.dto.CommentDTO;
import com.hits.language_school_back.infrastructure.CommentServiceImpl;
import com.hits.language_school_back.model.Comment;
import com.hits.language_school_back.model.Task;
import com.hits.language_school_back.model.User;
import com.hits.language_school_back.repository.CommentRepository;
import com.hits.language_school_back.repository.TaskRepository;
import com.hits.language_school_back.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.mockito.ArgumentMatchers.argThat;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {
    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private CommentServiceImpl commentService;

    private User testUser;
    private Task testTask;
    private Comment testComment;
    private CommentDTO testCommentDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);

        testTask = new Task();
        testTask.setId(1L);

        testComment = new Comment();
        testComment.setId(1L);
        testComment.setText("Test comment");
        testComment.setUser(testUser);
        testComment.setTask(testTask);
        testComment.setPrivateStatus(false);

        testCommentDTO = new CommentDTO();
        testCommentDTO.setText("Test comment");
        testCommentDTO.setUserId(1L);
        testCommentDTO.setTaskId(1L);
        testCommentDTO.setPrivateStatus(false);
    }

    @Test
    void getCommentsByTaskId_ShouldReturnListOfCommentDTOs_WhenTaskExists() {
        Long taskId = 1L;
        when(commentRepository.findByTaskId(taskId)).thenReturn(List.of(testComment));

        List<CommentDTO> result = commentService.getCommentsByTaskId(taskId);

        assertThat(result).isNotEmpty().hasSize(1);
        assertThat(result.getFirst().getText()).isEqualTo(testComment.getText());
        assertThat(result.getFirst().getUserId()).isEqualTo(testComment.getUser().getId());
        assertThat(result.getFirst().getTaskId()).isEqualTo(testComment.getTask().getId());

        verify(commentRepository).findByTaskId(taskId);
    }

    @Test
    void getCommentsByTaskId_ShouldReturnEmptyList_WhenNoCommentsExist() {
        Long taskId = 1L;
        when(commentRepository.findByTaskId(taskId)).thenReturn(List.of());

        List<CommentDTO> result = commentService.getCommentsByTaskId(taskId);

        assertThat(result).isEmpty();
        verify(commentRepository).findByTaskId(taskId);
    }

    @Test
    void createComment_ShouldReturnSavedComment_WhenValidDataProvided() {
        when(userRepository.findById(testCommentDTO.getUserId())).thenReturn(Optional.of(testUser));
        when(taskRepository.findById(testCommentDTO.getTaskId())).thenReturn(Optional.of(testTask));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        Comment result = commentService.createComment(testCommentDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testComment.getId());
        assertThat(result.getText()).isEqualTo(testComment.getText());
        assertThat(result.getUser()).isEqualTo(testUser);
        assertThat(result.getTask()).isEqualTo(testTask);

        verify(userRepository).findById(testCommentDTO.getUserId());
        verify(taskRepository).findById(testCommentDTO.getTaskId());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void createComment_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findById(testCommentDTO.getUserId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.createComment(testCommentDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findById(testCommentDTO.getUserId());
        verify(taskRepository, never()).findById(any());
        verify(commentRepository, never()).save(any());
    }

    @Test
    void createComment_ShouldThrowException_WhenTaskNotFound() {
        when(userRepository.findById(testCommentDTO.getUserId())).thenReturn(Optional.of(testUser));
        when(taskRepository.findById(testCommentDTO.getTaskId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.createComment(testCommentDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Task not found");

        verify(userRepository).findById(testCommentDTO.getUserId());
        verify(taskRepository).findById(testCommentDTO.getTaskId());
        verify(commentRepository, never()).save(any());
    }

    @Test
    void editComment_ShouldReturnUpdatedComment_WhenCommentExistsAndValidData() {
        Long commentId = 1L;
        CommentDTO updateDTO = new CommentDTO();
        updateDTO.setText("Updated text");
        updateDTO.setPrivateStatus(true);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        Comment result = commentService.editComment(updateDTO, commentId);

        assertThat(result).isNotNull();
        assertThat(result.getText()).isEqualTo(updateDTO.getText());
        assertThat(result.isPrivateStatus()).isEqualTo(updateDTO.isPrivateStatus());

        verify(commentRepository).findById(commentId);
        verify(commentRepository).save(testComment);
    }

    @Test
    void editComment_ShouldThrowException_WhenCommentNotFound() {
        Long commentId = 999L;
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.editComment(testCommentDTO, commentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Comment not found");

        verify(commentRepository).findById(commentId);
        verify(commentRepository, never()).save(any());
    }

    @Test
    void editComment_ShouldOnlyUpdateProvidedFields_WhenSomeFieldsAreNull() {
        Long commentId = 1L;
        CommentDTO updateDTO = new CommentDTO();
        updateDTO.setText("Only text updated");
        // userId, taskId, privateStatus are null

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        Comment result = commentService.editComment(updateDTO, commentId);

        assertThat(result.getText()).isEqualTo("Only text updated");
        assertThat(result.isPrivateStatus()).isEqualTo(testComment.isPrivateStatus());
        assertThat(result.getUser()).isEqualTo(testComment.getUser());
        assertThat(result.getTask()).isEqualTo(testComment.getTask());

        verify(commentRepository).findById(commentId);
        verify(commentRepository).save(testComment);
    }

    @Test
    void deleteComment_ShouldDeleteComment_WhenCommentExists() {
        Long commentId = 1L;
        when(commentRepository.existsById(commentId)).thenReturn(true);
        doNothing().when(commentRepository).deleteById(commentId);

        commentService.deleteComment(commentId);

        verify(commentRepository).existsById(commentId);
        verify(commentRepository).deleteById(commentId);
    }

    @Test
    void deleteComment_ShouldThrowException_WhenCommentNotFound() {
        Long commentId = 999L;
        when(commentRepository.existsById(commentId)).thenReturn(false);

        assertThatThrownBy(() -> commentService.deleteComment(commentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Comment not found");

        verify(commentRepository).existsById(commentId);
        verify(commentRepository, never()).deleteById(any());
    }

    @Test
    void createComment_ShouldHandlePrivateStatusCorrectly() {
        testCommentDTO.setPrivateStatus(true);
        when(userRepository.findById(testCommentDTO.getUserId())).thenReturn(Optional.of(testUser));
        when(taskRepository.findById(testCommentDTO.getTaskId())).thenReturn(Optional.of(testTask));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment savedComment = invocation.getArgument(0);
            savedComment.setId(1L);
            return savedComment;
        });

        Comment result = commentService.createComment(testCommentDTO);

        assertThat(result.isPrivateStatus()).isTrue();
        verify(commentRepository).save(argThat(Comment::isPrivateStatus));
    }
}
