package com.hits.language_school_back.controller;

import com.hits.language_school_back.dto.CommentDTO;
import com.hits.language_school_back.mapper.CommentMapper;
import com.hits.language_school_back.service.CommentService;
import com.hits.language_school_back.model.Comment;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/comment")
public class CommentController {
    private final CommentService commentService;
    private final CommentMapper commentMapper;

    @PostMapping("/create")
    public ResponseEntity<CommentDTO> createComment(@RequestBody CommentDTO commentDTO){
        Comment comment = commentService.createComment(commentDTO);
        return ResponseEntity.ok(commentMapper.toDto(comment));
    }

    @PutMapping("/{commentId}/edit")
    public ResponseEntity<CommentDTO> editComment(@RequestBody CommentDTO commentDTO, @PathVariable UUID commentId){
        Comment comment = commentService.editComment(commentDTO, commentId);
        return ResponseEntity.ok(commentMapper.toDto(comment));
    }

    @DeleteMapping("/{commentId}/delete")
    void deleteComment(@PathVariable UUID commentId){
        commentService.deleteComment(commentId);
    }

    @GetMapping("/{taskId}/get")
    public ResponseEntity<List<CommentDTO>> getCommentsByTaskId(@PathVariable UUID taskId){
        List<CommentDTO> comments = commentService.getCommentsByTaskId(taskId);
        return ResponseEntity.ok(comments);
    }
}
