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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    private UUID userId;
    private UUID taskId;
    private UUID commentId;
    private User user;
    private Task task;
    private Comment comment;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        taskId = UUID.randomUUID();
        commentId = UUID.randomUUID();

        user = User.builder().id(userId).build();
        task = Task.builder().id(taskId).build();

        comment = new Comment();
        comment.setId(commentId);
        comment.setText("text");
        comment.setUser(user);
        comment.setTask(task);
        comment.setPrivateStatus(true);
    }

    @Test
    void getCommentsByTaskId_mapsEntityToDto() {
        when(commentRepository.findByTaskId(taskId)).thenReturn(List.of(comment));

        List<CommentDTO> result = commentService.getCommentsByTaskId(taskId);

        assertThat(result).singleElement().satisfies(dto -> {
            assertThat(dto.getId()).isEqualTo(commentId);
            assertThat(dto.getUserId()).isEqualTo(userId);
            assertThat(dto.getTaskId()).isEqualTo(taskId);
            assertThat(dto.isPrivateStatus()).isTrue();
        });
    }

    @Test
    void createComment_savesCommentWithResolvedRelations() {
        CommentDTO dto = CommentDTO.builder()
                .text("new comment")
                .userId(userId)
                .taskId(taskId)
                .privateStatus(false)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Comment result = commentService.createComment(dto);

        assertThat(result.getText()).isEqualTo("new comment");
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getTask()).isEqualTo(task);
    }

    @Test
    void createComment_whenUserMissing_throws() {
        CommentDTO dto = CommentDTO.builder().userId(userId).taskId(taskId).build();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.createComment(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");

        verify(taskRepository, never()).findById(taskId);
    }

    @Test
    void editComment_updatesTextAndPrivateStatus() {
        CommentDTO dto = CommentDTO.builder().text("updated").privateStatus(false).build();
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(commentRepository.save(comment)).thenReturn(comment);

        Comment result = commentService.editComment(dto, commentId);

        assertThat(result.getText()).isEqualTo("updated");
        assertThat(result.isPrivateStatus()).isFalse();
    }

    @Test
    void deleteComment_whenExists_deletesById() {
        when(commentRepository.existsById(commentId)).thenReturn(true);

        commentService.deleteComment(commentId);

        verify(commentRepository).deleteById(commentId);
    }

    @Test
    void createComment_persistsPrivateFlag() {
        CommentDTO dto = CommentDTO.builder()
                .text("secret")
                .userId(userId)
                .taskId(taskId)
                .privateStatus(true)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        commentService.createComment(dto);

        verify(commentRepository).save(argThat(Comment::isPrivateStatus));
    }
}
