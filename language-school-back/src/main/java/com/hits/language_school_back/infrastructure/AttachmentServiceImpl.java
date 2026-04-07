package com.hits.language_school_back.infrastructure;

import com.hits.language_school_back.config.MinioConfig;
import com.hits.language_school_back.dto.AttachmentDownloadInfo;
import com.hits.language_school_back.exception.ResourceNotFoundException;
import com.hits.language_school_back.model.Attachment;
import com.hits.language_school_back.model.Notification;
import com.hits.language_school_back.model.Participation;
import com.hits.language_school_back.model.Task;
import com.hits.language_school_back.model.User;
import com.hits.language_school_back.repository.AttachmentRepository;
import com.hits.language_school_back.repository.NotificationRepository;
import com.hits.language_school_back.repository.ParticipationRepository;
import com.hits.language_school_back.repository.TaskRepository;
import com.hits.language_school_back.repository.UserRepository;
import com.hits.language_school_back.service.AttachmentService;
import com.hits.language_school_back.service.MinioService;
import lombok.RequiredArgsConstructor;
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
    private final ParticipationRepository participationRepository;
    private final UserRepository userRepository;
    private final MinioService minioService;
    private final MinioConfig minioConfig;

    @Transactional
    public Attachment uploadAttachment(UUID taskId, MultipartFile file, UUID userId) {
        User user = getUser(userId);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

        Attachment attachment = buildAttachment(file, user);
        attachment.setTask(task);
        return attachmentRepository.save(attachment);
    }

    @Override
    public Attachment uploadAttachmentForNotification(UUID notificationId, MultipartFile file, UUID userId) {
        User user = getUser(userId);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));

        Attachment attachment = buildAttachment(file, user);
        attachment.setNotification(notification);
        return attachmentRepository.save(attachment);
    }

    @Override
    public Attachment uploadAttachmentForParticipation(UUID participationId, MultipartFile file, UUID userId) {
        User user = getUser(userId);
        Participation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new ResourceNotFoundException("Participation not found: " + participationId));
        if (!participation.getStudent().getId().equals(userId)) {
            throw new IllegalArgumentException("Only the owner can upload files to the solution");
        }

        Attachment attachment = buildAttachment(file, user);
        attachment.setParticipation(participation);
        return attachmentRepository.save(attachment);
    }

    @Transactional
    public void deleteAttachment(UUID attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found: " + attachmentId));

        minioService.deleteFile(attachment.getObjectKey());
        attachmentRepository.delete(attachment);
    }

    @Override
    public InputStream downloadAttachment(UUID attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found: " + attachmentId));

        return minioService.downloadFile(attachment.getObjectKey());
    }

    public String getDownloadLink(UUID attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found: " + attachmentId));

        return minioService.generatePresignedUrl(attachment.getObjectKey(), 15 * 60 * 1000);
    }

    @Override
    public AttachmentDownloadInfo getDownloadInfo(UUID attachmentId) {
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

    private Attachment buildAttachment(MultipartFile file, User user) {
        String objectKey = minioService.uploadFile(file);

        Attachment attachment = new Attachment();
        attachment.setFileName(file.getOriginalFilename());
        attachment.setFileType(file.getContentType());
        attachment.setFileSize(file.getSize());
        attachment.setObjectKey(objectKey);
        attachment.setUser(user);
        attachment.setBucketName(minioConfig.getBucket());
        return attachment;
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }
}
