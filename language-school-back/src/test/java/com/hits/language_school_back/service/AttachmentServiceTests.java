package com.hits.language_school_back.service;

import com.hits.language_school_back.config.MinioConfig;
import com.hits.language_school_back.exception.ResourceNotFoundException;
import com.hits.language_school_back.infrastructure.AttachmentServiceImpl;
import com.hits.language_school_back.model.Attachment;
import com.hits.language_school_back.model.Task;
import com.hits.language_school_back.model.User;
import com.hits.language_school_back.repository.AttachmentRepository;
import com.hits.language_school_back.repository.TaskRepository;
import com.hits.language_school_back.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceTests {
//
//    @Mock
//    private AttachmentRepository attachmentRepository;
//
//    @Mock
//    private TaskRepository taskRepository;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private MinioService minioService;
//
//    @Mock
//    private MinioConfig minioConfig;
//
//    @InjectMocks
//    private AttachmentServiceImpl attachmentService;
//
//    private User user;
//    private Task task;
//    private Attachment attachment;
//    private MultipartFile multipartFile;
//    private final String bucketName = "test-bucket";
//    private final String objectKey = "test-object-key";
//    private final Long attachmentId = 1L;
//    private final Long taskId = 1L;
//    private final Long userId = 1L;
//
//    @BeforeEach
//    void setUp() {
//        // Setup user
//        user = new User();
//        user.setId(userId);
//        user.setFirstName("John");
//        user.setLastName("Doe");
//        user.setEmail("john@example.com");
//
//        // Setup task
//        task = new Task();
//        task.setId(taskId);
//        task.setName("Test Task");
//        task.setDescription("Test Description");
//
//        // Setup attachment
//        attachment = new Attachment();
//        attachment.setId(attachmentId);
//        attachment.setFileName("test-file.txt");
//        attachment.setFileType("text/plain");
//        attachment.setFileSize(1024L);
//        attachment.setObjectKey(objectKey);
//        attachment.setUser(user);
//        attachment.setBucketName(bucketName);
//        attachment.setTask(task);
//
//        // Setup multipart file
//        multipartFile = new MockMultipartFile(
//                "file",
//                "test-file.txt",
//                "text/plain",
//                "Test file content".getBytes()
//        );
//    }
//
//    // ==================== UPLOAD ATTACHMENT ====================
//
//    @Test
//    @DisplayName("Should upload attachment successfully")
//    void uploadAttachment_ShouldUploadAndReturnAttachment() {
//        // Arrange
//        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
//        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
//        when(minioService.uploadFile(any(MultipartFile.class))).thenReturn(objectKey);
//        when(minioConfig.getBucket()).thenReturn(bucketName);
//        when(attachmentRepository.save(any(Attachment.class))).thenReturn(attachment);
//
//        // Act
//        Attachment result = attachmentService.uploadAttachment(taskId, multipartFile, userId);
//
//        // Assert
//        assertThat(result).isNotNull();
//        assertThat(result.getId()).isEqualTo(attachmentId);
//        assertThat(result.getFileName()).isEqualTo("test-file.txt");
//        assertThat(result.getFileType()).isEqualTo("text/plain");
//        assertThat(result.getFileSize()).isEqualTo(1024L);
//        assertThat(result.getObjectKey()).isEqualTo(objectKey);
//        assertThat(result.getUser()).isEqualTo(user);
//        assertThat(result.getBucketName()).isEqualTo(bucketName);
//        assertThat(result.getTask()).isEqualTo(task);
//
//        verify(userRepository).findById(userId);
//        verify(taskRepository).findById(taskId);
//        verify(minioService).uploadFile(multipartFile);
//        verify(minioConfig).getBucket();
//        verify(attachmentRepository).save(any(Attachment.class));
//    }
//
//    @Test
//    @DisplayName("Should throw ResourceNotFoundException when user not found during upload")
//    void uploadAttachment_WithNonExistentUser_ShouldThrowException() {
//        // Arrange
//        when(userRepository.findById(userId)).thenReturn(Optional.empty());
//
//        // Act & Assert
//        assertThatThrownBy(() -> attachmentService.uploadAttachment(taskId, multipartFile, userId))
//                .isInstanceOf(ResourceNotFoundException.class)
//                .hasMessageContaining("User not found: " + taskId);
//
//        verify(userRepository).findById(userId);
//        verify(taskRepository, never()).findById(anyLong());
//        verify(minioService, never()).uploadFile(any(MultipartFile.class));
//        verify(attachmentRepository, never()).save(any(Attachment.class));
//    }
//
//    @Test
//    @DisplayName("Should throw ResourceNotFoundException when task not found during upload")
//    void uploadAttachment_WithNonExistentTask_ShouldThrowException() {
//        // Arrange
//        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
//        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());
//
//        // Act & Assert
//        assertThatThrownBy(() -> attachmentService.uploadAttachment(taskId, multipartFile, userId))
//                .isInstanceOf(ResourceNotFoundException.class)
//                .hasMessageContaining("Task not found: " + taskId);
//
//        verify(userRepository).findById(userId);
//        verify(taskRepository).findById(taskId);
//        verify(minioService, never()).uploadFile(any(MultipartFile.class));
//        verify(attachmentRepository, never()).save(any(Attachment.class));
//    }
//
//    @Test
//    @DisplayName("Should handle null filename in multipart file")
//    void uploadAttachment_WithNullFilename_ShouldHandleGracefully() {
//        // Arrange
//        MultipartFile fileWithNullName = new MockMultipartFile(
//                "file",
//                null,
//                "text/plain",
//                "Test content".getBytes()
//        );
//
//        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
//        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
//        when(minioService.uploadFile(fileWithNullName)).thenReturn(objectKey);
//        when(minioConfig.getBucket()).thenReturn(bucketName);
//        when(attachmentRepository.save(any(Attachment.class))).thenAnswer(invocation -> {
//            Attachment savedAttachment = invocation.getArgument(0);
//            savedAttachment.setId(attachmentId);
//            return savedAttachment;
//        });
//
//        // Act
//        Attachment result = attachmentService.uploadAttachment(taskId, fileWithNullName, userId);
//
//        // Assert
//        assertThat(result).isNotNull();
//        assertThat(result.getFileType()).isEqualTo("text/plain");
//        assertThat(result.getFileSize()).isEqualTo(fileWithNullName.getSize());
//
//        verify(attachmentRepository).save(any(Attachment.class));
//    }
//
//    @Test
//    @DisplayName("Should handle empty file")
//    void uploadAttachment_WithEmptyFile_ShouldUpload() {
//        // Arrange
//        MultipartFile emptyFile = new MockMultipartFile(
//                "file",
//                "empty.txt",
//                "text/plain",
//                new byte[0]
//        );
//
//        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
//        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
//        when(minioService.uploadFile(emptyFile)).thenReturn(objectKey);
//        when(minioConfig.getBucket()).thenReturn(bucketName);
//        when(attachmentRepository.save(any(Attachment.class))).thenAnswer(invocation -> {
//            Attachment savedAttachment = invocation.getArgument(0);
//            savedAttachment.setId(attachmentId);
//            return savedAttachment;
//        });
//
//        // Act
//        Attachment result = attachmentService.uploadAttachment(taskId, emptyFile, userId);
//
//        // Assert
//        assertThat(result).isNotNull();
//        assertThat(result.getFileName()).isEqualTo("empty.txt");
//        assertThat(result.getFileSize()).isEqualTo(0L);
//
//        verify(attachmentRepository).save(any(Attachment.class));
//    }
//
//    // ==================== DELETE ATTACHMENT ====================
//
//    @Test
//    @DisplayName("Should delete attachment successfully")
//    void deleteAttachment_ShouldDeleteFromRepoAndMinio() {
//        // Arrange
//        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));
//        doNothing().when(minioService).deleteFile(objectKey);
//        doNothing().when(attachmentRepository).delete(attachment);
//
//        // Act
//        attachmentService.deleteAttachment(attachmentId);
//
//        // Assert
//        verify(attachmentRepository).findById(attachmentId);
//        verify(minioService).deleteFile(objectKey);
//        verify(attachmentRepository).delete(attachment);
//    }
//
//    @Test
//    @DisplayName("Should throw ResourceNotFoundException when attachment not found during delete")
//    void deleteAttachment_WithNonExistentAttachment_ShouldThrowException() {
//        // Arrange
//        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.empty());
//
//        // Act & Assert
//        assertThatThrownBy(() -> attachmentService.deleteAttachment(attachmentId))
//                .isInstanceOf(ResourceNotFoundException.class)
//                .hasMessageContaining("Attachment not found: " + attachmentId);
//
//        verify(attachmentRepository).findById(attachmentId);
//        verify(minioService, never()).deleteFile(anyString());
//        verify(attachmentRepository, never()).delete(any(Attachment.class));
//    }
//
//    @Test
//    @DisplayName("Should handle MinioService exception during delete")
//    void deleteAttachment_WhenMinioFails_ShouldThrowException() {
//        // Arrange
//        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));
//        doThrow(new RuntimeException("Minio error")).when(minioService).deleteFile(objectKey);
//
//        // Act & Assert
//        assertThatThrownBy(() -> attachmentService.deleteAttachment(attachmentId))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessageContaining("Minio error");
//
//        verify(attachmentRepository).findById(attachmentId);
//        verify(minioService).deleteFile(objectKey);
//        verify(attachmentRepository, never()).delete(any(Attachment.class));
//    }
//
//    // ==================== GET DOWNLOAD LINK ====================
//
//    @Test
//    @DisplayName("Should generate download link successfully")
//    void getDownloadLink_ShouldReturnPresignedUrl() {
//        // Arrange
//        String expectedUrl = "https://minio.example.com/test-bucket/test-object-key?token=123";
//        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));
//        when(minioService.generatePresignedUrl(objectKey, 15 * 60 * 1000)).thenReturn(expectedUrl);
//
//        // Act
//        String result = attachmentService.getDownloadLink(attachmentId);
//
//        // Assert
//        assertThat(result).isEqualTo(expectedUrl);
//        verify(attachmentRepository).findById(attachmentId);
//        verify(minioService).generatePresignedUrl(objectKey, 15 * 60 * 1000);
//    }
//
//    @Test
//    @DisplayName("Should throw exception when attachment not found during download link generation")
//    void getDownloadLink_WithNonExistentAttachment_ShouldThrowException() {
//        // Arrange
//        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.empty());
//
//        // Act & Assert
//        assertThatThrownBy(() -> attachmentService.getDownloadLink(attachmentId))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessageContaining("Attachment not found");
//
//        verify(attachmentRepository).findById(attachmentId);
//        verify(minioService, never()).generatePresignedUrl(anyString(), anyInt());
//    }
//
//    @Test
//    @DisplayName("Should handle MinioService exception during download link generation")
//    void getDownloadLink_WhenMinioFails_ShouldThrowException() {
//        // Arrange
//        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));
//        when(minioService.generatePresignedUrl(objectKey, 15 * 60 * 1000))
//                .thenThrow(new RuntimeException("Minio error"));
//
//        // Act & Assert
//        assertThatThrownBy(() -> attachmentService.getDownloadLink(attachmentId))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessageContaining("Minio error");
//
//        verify(attachmentRepository).findById(attachmentId);
//        verify(minioService).generatePresignedUrl(objectKey, 15 * 60 * 1000);
//    }
//
//    @Test
//    @DisplayName("Should generate download link with correct expiration time")
//    void getDownloadLink_ShouldUseCorrectExpirationTime() {
//        // Arrange
//        int expectedExpiration = 15 * 60 * 1000; // 15 minutes in milliseconds
//        String expectedUrl = "https://minio.example.com/test-bucket/test-object-key";
//
//        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));
//        when(minioService.generatePresignedUrl(objectKey, expectedExpiration)).thenReturn(expectedUrl);
//
//        // Act
//        String result = attachmentService.getDownloadLink(attachmentId);
//
//        // Assert
//        assertThat(result).isEqualTo(expectedUrl);
//        verify(minioService).generatePresignedUrl(objectKey, expectedExpiration);
//    }
//
//    // ==================== INTEGRATION SCENARIOS ====================
//
//    @Test
//    @DisplayName("Should upload, then delete attachment successfully")
//    void uploadThenDeleteAttachment_ShouldWorkSuccessfully() {
//        // Arrange - Upload
//        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
//        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
//        when(minioService.uploadFile(multipartFile)).thenReturn(objectKey);
//        when(minioConfig.getBucket()).thenReturn(bucketName);
//        when(attachmentRepository.save(any(Attachment.class))).thenReturn(attachment);
//
//        // Act - Upload
//        Attachment uploadedAttachment = attachmentService.uploadAttachment(taskId, multipartFile, userId);
//
//        // Assert - Upload
//        assertThat(uploadedAttachment).isNotNull();
//        assertThat(uploadedAttachment.getObjectKey()).isEqualTo(objectKey);
//
//        // Arrange - Delete
//        when(attachmentRepository.findById(uploadedAttachment.getId())).thenReturn(Optional.of(attachment));
//        doNothing().when(minioService).deleteFile(objectKey);
//        doNothing().when(attachmentRepository).delete(attachment);
//
//        // Act - Delete
//        attachmentService.deleteAttachment(uploadedAttachment.getId());
//
//        // Assert - Delete
//        verify(attachmentRepository).delete(attachment);
//        verify(minioService).deleteFile(objectKey);
//    }
//
//    @Test
//    @DisplayName("Should handle large file upload")
//    void uploadAttachment_WithLargeFile_ShouldHandleCorrectly() {
//        // Arrange
//        byte[] largeContent = new byte[10 * 1024 * 1024]; // 10MB file
//        MultipartFile largeFile = new MockMultipartFile(
//                "file",
//                "large-file.bin",
//                "application/octet-stream",
//                largeContent
//        );
//
//        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
//        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
//        when(minioService.uploadFile(largeFile)).thenReturn(objectKey);
//        when(minioConfig.getBucket()).thenReturn(bucketName);
//        when(attachmentRepository.save(any(Attachment.class))).thenAnswer(invocation -> {
//            Attachment savedAttachment = invocation.getArgument(0);
//            savedAttachment.setId(attachmentId);
//            return savedAttachment;
//        });
//
//        // Act
//        Attachment result = attachmentService.uploadAttachment(taskId, largeFile, userId);
//
//        // Assert
//        assertThat(result).isNotNull();
//        assertThat(result.getFileSize()).isEqualTo(largeContent.length);
//        assertThat(result.getFileType()).isEqualTo("application/octet-stream");
//
//        verify(minioService).uploadFile(largeFile);
//        verify(attachmentRepository).save(any(Attachment.class));
//    }
}