package com.hits.language_school_back.service;

import com.hits.language_school_back.config.MinioConfig;
import com.hits.language_school_back.dto.AttachmentDownloadInfo;
import com.hits.language_school_back.exception.ResourceNotFoundException;
import com.hits.language_school_back.infrastructure.AttachmentServiceImpl;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceTest {

    @Mock
    private AttachmentRepository attachmentRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private ParticipationRepository participationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MinioService minioService;
    @Mock
    private MinioConfig minioConfig;

    @InjectMocks
    private AttachmentServiceImpl attachmentService;

    private UUID attachmentId;
    private UUID taskId;
    private UUID notificationId;
    private UUID participationId;
    private UUID userId;
    private User user;
    private MockMultipartFile file;

    @BeforeEach
    void setUp() {
        attachmentId = UUID.randomUUID();
        taskId = UUID.randomUUID();
        notificationId = UUID.randomUUID();
        participationId = UUID.randomUUID();
        userId = UUID.randomUUID();

        user = User.builder().id(userId).build();
        file = new MockMultipartFile("file", "test.txt", "text/plain", "body".getBytes());
    }

    @Test
    void uploadAttachment_buildsAttachmentForTask() {
        Task task = Task.builder().id(taskId).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(minioService.uploadFile(file)).thenReturn("object-key");
        when(minioConfig.getBucket()).thenReturn("bucket");
        when(attachmentRepository.save(any(Attachment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Attachment result = attachmentService.uploadAttachment(taskId, file, userId);

        assertThat(result.getTask()).isEqualTo(task);
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getObjectKey()).isEqualTo("object-key");
        assertThat(result.getBucketName()).isEqualTo("bucket");
    }

    @Test
    void uploadAttachmentForNotification_attachesNotification() {
        Notification notification = new Notification();
        notification.setId(notificationId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
        when(minioService.uploadFile(file)).thenReturn("object-key");
        when(minioConfig.getBucket()).thenReturn("bucket");
        when(attachmentRepository.save(any(Attachment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Attachment result = attachmentService.uploadAttachmentForNotification(notificationId, file, userId);

        assertThat(result.getNotification()).isEqualTo(notification);
    }

    @Test
    void uploadAttachmentForParticipation_whenActorNotOwner_throws() {
        User anotherUser = User.builder().id(UUID.randomUUID()).build();
        Participation participation = new Participation();
        participation.setId(participationId);
        participation.setStudent(anotherUser);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(participationRepository.findById(participationId)).thenReturn(Optional.of(participation));

        assertThatThrownBy(() -> attachmentService.uploadAttachmentForParticipation(participationId, file, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Only the owner can upload files to the solution");

        verify(attachmentRepository, never()).save(any(Attachment.class));
    }

    @Test
    void deleteAttachment_deletesFromMinioAndRepository() {
        Attachment attachment = new Attachment();
        attachment.setId(attachmentId);
        attachment.setObjectKey("object-key");
        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));

        attachmentService.deleteAttachment(attachmentId);

        verify(minioService).deleteFile("object-key");
        verify(attachmentRepository).delete(attachment);
    }

    @Test
    void downloadAttachment_returnsStreamFromMinio() {
        Attachment attachment = new Attachment();
        attachment.setObjectKey("object-key");
        InputStream stream = new ByteArrayInputStream(new byte[]{1, 2});
        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));
        when(minioService.downloadFile("object-key")).thenReturn(stream);

        assertThat(attachmentService.downloadAttachment(attachmentId)).isSameAs(stream);
    }

    @Test
    void getDownloadInfo_mapsAttachmentFields() {
        Attachment attachment = new Attachment();
        attachment.setId(attachmentId);
        attachment.setFileName("test.txt");
        attachment.setFileType("text/plain");
        attachment.setFileSize(4L);
        attachment.setObjectKey("object-key");
        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));

        AttachmentDownloadInfo result = attachmentService.getDownloadInfo(attachmentId);

        assertThat(result.getId()).isEqualTo(attachmentId);
        assertThat(result.getFileName()).isEqualTo("test.txt");
        assertThat(result.getObjectKey()).isEqualTo("object-key");
    }

    @Test
    void getDownloadLink_whenMissingAttachment_throws() {
        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attachmentService.getDownloadLink(attachmentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Attachment not found");
    }
}
