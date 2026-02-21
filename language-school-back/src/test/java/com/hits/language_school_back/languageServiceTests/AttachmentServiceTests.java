package com.hits.language_school_back.languageServiceTests;

import com.hits.language_school_back.config.MinioConfig;
import com.hits.language_school_back.infrastructure.AttachmentServiceImpl;
import com.hits.language_school_back.model.Attachment;
import com.hits.language_school_back.model.Task;
import com.hits.language_school_back.repository.AttachmentRepository;
import com.hits.language_school_back.repository.TaskRepository;
import com.hits.language_school_back.service.MinioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceTests {

    @Mock
    private AttachmentRepository attachmentRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private MinioService minioService;

    @Mock
    private MinioConfig minioConfig;

    @InjectMocks
    private AttachmentServiceImpl attachmentService;

    private Task task;
    private MultipartFile file;
    private Attachment attachment;

    @BeforeEach
    void setUp() {
        task = new Task();
        task.setId(1L);

        attachment = new Attachment();
        attachment.setId(1L);
        attachment.setObjectKey("test-key");
        attachment.setBucketName("test-bucket");

        file = mock(MultipartFile.class);
    }

    @Nested
    class UploadAttachmentTests {

        @Test
        void shouldUploadAttachmentSuccessfully() {
            // Arrange
            Long taskId = 1L;
            String fileName = "test.txt";
            String contentType = "text/plain";
            long fileSize = 1024L;
            String objectKey = "generated-key";
            String bucketName = "attachments-bucket";

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
            when(minioService.uploadFile(file)).thenReturn(objectKey);
            when(minioConfig.getBucket()).thenReturn(bucketName);
            when(file.getOriginalFilename()).thenReturn(fileName);
            when(file.getContentType()).thenReturn(contentType);
            when(file.getSize()).thenReturn(fileSize);

            Attachment savedAttachment = new Attachment();
            savedAttachment.setId(1L);
            savedAttachment.setFileName(fileName);
            savedAttachment.setFileType(contentType);
            savedAttachment.setFileSize(fileSize);
            savedAttachment.setObjectKey(objectKey);
            savedAttachment.setBucketName(bucketName);
            savedAttachment.setTask(task);

            when(attachmentRepository.save(any(Attachment.class))).thenReturn(savedAttachment);

            // Act
            Attachment result = attachmentService.uploadAttachment(taskId, file);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getFileName()).isEqualTo(fileName);
            assertThat(result.getFileType()).isEqualTo(contentType);
            assertThat(result.getFileSize()).isEqualTo(fileSize);
            assertThat(result.getObjectKey()).isEqualTo(objectKey);
            assertThat(result.getBucketName()).isEqualTo(bucketName);
            assertThat(result.getTask()).isEqualTo(task);

            verify(taskRepository).findById(taskId);
            verify(minioService).uploadFile(file);
            verify(minioConfig).getBucket();
            verify(attachmentRepository).save(any(Attachment.class));
        }

        @Test
        void shouldThrowExceptionWhenTaskNotFound() {
            // Arrange
            Long taskId = 999L;
            when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> attachmentService.uploadAttachment(taskId, file))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Task not found: " + taskId);

            verify(taskRepository).findById(taskId);
            verify(minioService, never()).uploadFile(any());
            verify(attachmentRepository, never()).save(any());
        }

        @Test
        void shouldHandleMinioServiceFailure() {
            // Arrange
            Long taskId = 1L;
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
            when(minioService.uploadFile(file)).thenThrow(new RuntimeException("Minio upload failed"));

            // Act & Assert
            assertThatThrownBy(() -> attachmentService.uploadAttachment(taskId, file))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Minio upload failed");

            verify(taskRepository).findById(taskId);
            verify(minioService).uploadFile(file);
            verify(attachmentRepository, never()).save(any());
        }

        @Test
        void shouldHandleNullFileMetadata() {
            // Arrange
            Long taskId = 1L;
            String objectKey = "generated-key";

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
            when(minioService.uploadFile(file)).thenReturn(objectKey);
            when(file.getOriginalFilename()).thenReturn(null);
            when(file.getContentType()).thenReturn(null);
            when(file.getSize()).thenReturn(0L);

            // Act
            attachmentService.uploadAttachment(taskId, file);

            // Assert
            verify(attachmentRepository).save(argThat(attachment ->
                    attachment.getFileName() == null &&
                            attachment.getFileType() == null &&
                            attachment.getFileSize() == 0L
            ));
        }
    }

    @Nested
    class DeleteAttachmentTests {

        @Test
        void shouldDeleteAttachmentSuccessfully() {
            // Arrange
            Long attachmentId = 1L;
            when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));
            doNothing().when(minioService).deleteFile(attachment.getObjectKey());

            // Act
            attachmentService.deleteAttachment(attachmentId);

            // Assert
            verify(attachmentRepository).findById(attachmentId);
            verify(minioService).deleteFile(attachment.getObjectKey());
            verify(attachmentRepository).delete(attachment);
        }

        @Test
        void shouldThrowExceptionWhenAttachmentNotFound() {
            // Arrange
            Long attachmentId = 999L;
            when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> attachmentService.deleteAttachment(attachmentId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Attachment not found: " + attachmentId);

            verify(attachmentRepository).findById(attachmentId);
            verify(minioService, never()).deleteFile(any());
            verify(attachmentRepository, never()).delete(any());
        }

        @Test
        void shouldHandleMinioServiceFailureDuringDelete() {
            // Arrange
            Long attachmentId = 1L;
            when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));
            doThrow(new RuntimeException("Minio delete failed")).when(minioService).deleteFile(attachment.getObjectKey());

            // Act & Assert
            assertThatThrownBy(() -> attachmentService.deleteAttachment(attachmentId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Minio delete failed");

            verify(attachmentRepository).findById(attachmentId);
            verify(minioService).deleteFile(attachment.getObjectKey());
            verify(attachmentRepository, never()).delete(any());
        }
    }

    @Nested
    class GetDownloadLinkTests {

        @Test
        void shouldGenerateDownloadLinkSuccessfully() {
            // Arrange
            Long attachmentId = 1L;
            String expectedUrl = "http://minio/test-bucket/test-key?token=123";

            when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));
            when(minioService.generatePresignedUrl(attachment.getObjectKey(), 15 * 60 * 1000))
                    .thenReturn(expectedUrl);

            // Act
            String result = attachmentService.getDownloadLink(attachmentId);

            // Assert
            assertThat(result).isEqualTo(expectedUrl);

            verify(attachmentRepository).findById(attachmentId);
            verify(minioService).generatePresignedUrl(attachment.getObjectKey(), 15 * 60 * 1000);
        }

        @Test
        void shouldThrowExceptionWhenAttachmentNotFound() {
            // Arrange
            Long attachmentId = 999L;
            when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> attachmentService.getDownloadLink(attachmentId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Attachment not found");

            verify(attachmentRepository).findById(attachmentId);
            verify(minioService, never()).generatePresignedUrl(any(), anyInt());
        }

        @Test
        void shouldHandleMinioServiceFailureDuringLinkGeneration() {
            // Arrange
            Long attachmentId = 1L;
            when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));
            when(minioService.generatePresignedUrl(attachment.getObjectKey(), 15 * 60 * 1000))
                    .thenThrow(new RuntimeException("Minio link generation failed"));

            // Act & Assert
            assertThatThrownBy(() -> attachmentService.getDownloadLink(attachmentId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Minio link generation failed");

            verify(attachmentRepository).findById(attachmentId);
            verify(minioService).generatePresignedUrl(attachment.getObjectKey(), 15 * 60 * 1000);
        }
    }
}