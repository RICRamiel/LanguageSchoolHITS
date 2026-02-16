package com.hits.language_school_back.dto;
import lombok.Data;

@Data
public class CommentDTO {
    private String text;
    private Long userId;
    private Long taskId;
    private boolean privateStatus;
}
