package com.hits.language_school_back.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttachmentDownloadInfo {
    private Long id;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String objectKey;
}