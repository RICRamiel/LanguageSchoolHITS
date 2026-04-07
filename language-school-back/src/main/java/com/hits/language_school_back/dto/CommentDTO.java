package com.hits.language_school_back.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class CommentDTO {
    private UUID id;
    private String text;
    private UUID userId;
    private UUID taskId;
    private boolean privateStatus;
}
