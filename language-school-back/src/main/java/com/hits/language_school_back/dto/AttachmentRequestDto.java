package com.hits.language_school_back.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Data
public class AttachmentRequestDto {
    private MultipartFile file;
    private UUID taskId;
}