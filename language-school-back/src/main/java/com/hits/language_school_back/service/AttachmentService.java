package com.hits.language_school_back.service;

import com.hits.language_school_back.model.Attachment;
import org.springframework.web.multipart.MultipartFile;


public interface AttachmentService {
    Attachment uploadAttachment(Long taskId, MultipartFile file);

    void deleteAttachment(Long attachmentId);

    public String getDownloadLink(Long attachmentId);
}
