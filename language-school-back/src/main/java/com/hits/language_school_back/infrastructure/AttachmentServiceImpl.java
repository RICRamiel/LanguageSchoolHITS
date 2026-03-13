package com.hits.language_school_back.infrastructure;

import com.hits.language_school_back.config.MinioConfig;
import com.hits.language_school_back.dto.AttachmentDownloadInfo;
import com.hits.language_school_back.exception.ResourceNotFoundException;
import com.hits.language_school_back.model.Attachment;
import com.hits.language_school_back.model.Notification;
import com.hits.language_school_back.model.Task;
import com.hits.language_school_back.model.User;
import com.hits.language_school_back.repository.AttachmentRepository;
import com.hits.language_school_back.repository.NotificationRepository;
import com.hits.language_school_back.repository.TaskRepository;
import com.hits.language_school_back.repository.UserRepository;
import com.hits.language_school_back.service.AttachmentService;
import com.hits.language_school_back.service.MinioService;
import com.hits.language_school_back.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {
    private final AttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final MinioService minioService;
    private final MinioConfig minioConfig;

    @Transactional
    public Attachment uploadAttachment(Long taskId, MultipartFile file, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + taskId));
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

        String objectKey = minioService.uploadFile(file);

        Attachment attachment = new Attachment();
        attachment.setFileName(file.getOriginalFilename());
        attachment.setFileType(file.getContentType());
        attachment.setFileSize(file.getSize());
        attachment.setObjectKey(objectKey);
        attachment.setUser(user);
        attachment.setBucketName(minioConfig.getBucket());
        attachment.setTask(task);

        return attachmentRepository.save(attachment);
    }

    @Override
    public Attachment uploadAttachmentForNotification(UUID notificationId, MultipartFile file, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));

        String objectKey = minioService.uploadFile(file);

        Attachment attachment = new Attachment();
        attachment.setFileName(file.getOriginalFilename());
        attachment.setFileType(file.getContentType());
        attachment.setFileSize(file.getSize());
        attachment.setObjectKey(objectKey);
        attachment.setUser(user);
        attachment.setBucketName(minioConfig.getBucket());
        attachment.setNotification(notification);

        return attachmentRepository.save(attachment);
    }

    @Transactional
    public void deleteAttachment(Long attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found: " + attachmentId));

        minioService.deleteFile(attachment.getObjectKey());

        attachmentRepository.delete(attachment);
    }

    @Override
    public InputStream downloadAttachment(Long attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found: " + attachmentId));

        return minioService.downloadFile(attachment.getObjectKey());
    }

    public String getDownloadLink(Long attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));

        return minioService.generatePresignedUrl(
                attachment.getObjectKey(),
                15 * 60 * 1000
        );
    }

    @Override
    public AttachmentDownloadInfo getDownloadInfo(Long attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found: " + attachmentId));

        return AttachmentDownloadInfo.builder()
                .id(attachment.getId())
                .fileName(attachment.getFileName())
                .fileType(attachment.getFileType())
                .fileSize(attachment.getFileSize())
                .objectKey(attachment.getObjectKey())
                .build();
    }
}