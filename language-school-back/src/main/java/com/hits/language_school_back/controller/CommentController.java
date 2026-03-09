package com.hits.language_school_back.controller;

import com.hits.language_school_back.dto.CommentDTO;
import com.hits.language_school_back.mapper.CommentMapper;
import com.hits.language_school_back.service.CommentService;
import com.hits.language_school_back.model.Comment;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<CommentDTO> editComment(@RequestBody CommentDTO commentDTO, @PathVariable Long commentId){
        Comment comment = commentService.editComment(commentDTO, commentId);
        return ResponseEntity.ok(commentMapper.toDto(comment));
    }

    @DeleteMapping("/{commentId}/delete")
    void editComment(@PathVariable Long commentId){
        commentService.deleteComment(commentId);
    }

    @GetMapping("/{taskId}/get")
    public ResponseEntity<List<CommentDTO>> getCommentsByTaskId(@PathVariable Long taskId){
        List<CommentDTO> comments = commentService.getCommentsByTaskId(taskId);
        return ResponseEntity.ok(comments);
    }
}
