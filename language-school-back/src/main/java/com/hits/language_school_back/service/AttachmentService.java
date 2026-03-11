package com.hits.language_school_back.service;

import com.hits.language_school_back.dto.AttachmentDownloadInfo;
import com.hits.language_school_back.model.Attachment;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;


public interface AttachmentService {
    Attachment uploadAttachment(Long taskId, MultipartFile file, Long userId);

    Attachment uploadAttachmentForNotification(UUID notificationId, MultipartFile file, Long userId);

    void deleteAttachment(Long attachmentId);

    public String getDownloadLink(Long attachmentId);

    InputStream downloadAttachment(Long attachmentId);

    AttachmentDownloadInfo getDownloadInfo(Long attachmentId);
}
