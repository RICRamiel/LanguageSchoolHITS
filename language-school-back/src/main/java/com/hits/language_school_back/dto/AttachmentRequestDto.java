package com.hits.language_school_back.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class AttachmentRequestDto {
    private MultipartFile file;
    private Long taskId;
}