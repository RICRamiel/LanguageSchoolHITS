package com.hits.language_school_back.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AttachmentDownloadInfo {
    private UUID id;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String objectKey;
}