package com.hits.language_school_back.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class CommentDTO {
    private String text;
    private Long userId;
    private Long taskId;
    private boolean privateStatus;
}
