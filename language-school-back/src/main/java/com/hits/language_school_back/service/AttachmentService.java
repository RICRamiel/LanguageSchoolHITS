package com.hits.language_school_back.service;

import com.hits.language_school_back.dto.AttachmentDownloadInfo;
import com.hits.language_school_back.model.Attachment;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;


public interface AttachmentService {
    Attachment uploadAttachment(UUID taskId, MultipartFile file, UUID userId);

    Attachment uploadAttachmentForNotification(UUID notificationId, MultipartFile file, UUID userId);

    void deleteAttachment(UUID attachmentId);

    public String getDownloadLink(UUID attachmentId);

    InputStream downloadAttachment(UUID attachmentId);

    AttachmentDownloadInfo getDownloadInfo(UUID attachmentId);
}
