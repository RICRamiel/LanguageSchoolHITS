package com.hits.language_school_back.service;

import com.hits.language_school_back.config.MinioConfig;
import com.hits.language_school_back.exception.FileStorageException;
import com.hits.language_school_back.infrastructure.MinioServiceImpl;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.UnknownHostException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MinioServiceImplTest {

    @Mock
    private MinioClient minioClient;

    @Mock
    private MinioConfig minioConfig;

    @InjectMocks
    private MinioServiceImpl minioService;

    @Mock
    private MultipartFile file;

    @Captor
    private ArgumentCaptor<PutObjectArgs> putObjectArgsCaptor;

    @Captor
    private ArgumentCaptor<GetObjectArgs> getObjectArgsCaptor;

    @Captor
    private ArgumentCaptor<RemoveObjectArgs> removeObjectArgsCaptor;

    @Captor
    private ArgumentCaptor<GetPresignedObjectUrlArgs> presignedUrlArgsCaptor;

    @Captor
    private ArgumentCaptor<BucketExistsArgs> bucketExistsArgsCaptor;

    @Captor
    private ArgumentCaptor<MakeBucketArgs> makeBucketArgsCaptor;

    private static final String BUCKET_NAME = "test-bucket";
    private static final String FILE_NAME = "test.txt";
    private static final String CONTENT_TYPE = "text/plain";
    private static final long FILE_SIZE = 1024L;

    @BeforeEach
    void setUp() {
        lenient().when(minioConfig.getBucket()).thenReturn(BUCKET_NAME);
    }

    @Nested
    class UploadFileTests {

        @Test
        void shouldUploadFileSuccessfully() throws Exception {
            // Arrange
            InputStream inputStream = new ByteArrayInputStream("test content".getBytes());
            ObjectWriteResponse mockResponse = mock(ObjectWriteResponse.class);

            when(file.getOriginalFilename()).thenReturn(FILE_NAME);
            when(file.getInputStream()).thenReturn(inputStream);
            when(file.getSize()).thenReturn(FILE_SIZE);
            when(file.getContentType()).thenReturn(CONTENT_TYPE);

            when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
            when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(mockResponse);

            // Act
            String result = minioService.uploadFile(file);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).endsWith("_" + FILE_NAME);

            verify(minioClient).bucketExists(bucketExistsArgsCaptor.capture());
            assertThat(bucketExistsArgsCaptor.getValue().bucket()).isEqualTo(BUCKET_NAME);

            verify(minioClient).putObject(putObjectArgsCaptor.capture());
            PutObjectArgs capturedArgs = putObjectArgsCaptor.getValue();

            assertThat(capturedArgs.bucket()).isEqualTo(BUCKET_NAME);
            assertThat(capturedArgs.object()).endsWith(FILE_NAME);
            assertThat(capturedArgs.contentType()).isEqualTo(CONTENT_TYPE);

            verify(minioClient, never()).makeBucket(any());
        }

        @Test
        void shouldCreateBucketIfNotExists() throws Exception {
            // Arrange
            InputStream inputStream = new ByteArrayInputStream("test content".getBytes());
            ObjectWriteResponse mockResponse = mock(ObjectWriteResponse.class);

            when(file.getOriginalFilename()).thenReturn(FILE_NAME);
            when(file.getInputStream()).thenReturn(inputStream);
            when(file.getSize()).thenReturn(FILE_SIZE);
            when(file.getContentType()).thenReturn(CONTENT_TYPE);

            when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);
            doNothing().when(minioClient).makeBucket(any(MakeBucketArgs.class));
            when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(mockResponse);

            // Act
            String result = minioService.uploadFile(file);

            // Assert
            assertThat(result).isNotNull();

            verify(minioClient).bucketExists(any(BucketExistsArgs.class));
            verify(minioClient).makeBucket(makeBucketArgsCaptor.capture());
            assertThat(makeBucketArgsCaptor.getValue().bucket()).isEqualTo(BUCKET_NAME);
            verify(minioClient).putObject(any(PutObjectArgs.class));
        }

        @Test
        void shouldGenerateUUIDAsPartOfObjectKey() throws Exception {
            // Arrange
            InputStream inputStream = new ByteArrayInputStream("test content".getBytes());
            ObjectWriteResponse mockResponse = mock(ObjectWriteResponse.class);

            when(file.getOriginalFilename()).thenReturn(FILE_NAME);
            when(file.getInputStream()).thenReturn(inputStream);
            when(file.getSize()).thenReturn(FILE_SIZE);
            when(file.getContentType()).thenReturn(CONTENT_TYPE);

            when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
            when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(mockResponse);

            // Act
            String result = minioService.uploadFile(file);

            // Assert
            assertThat(result).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}_" + FILE_NAME);
        }

        @Test
        void shouldHandleNullOriginalFilename() throws Exception {
            // Arrange
            InputStream inputStream = new ByteArrayInputStream("test content".getBytes());
            ObjectWriteResponse mockResponse = mock(ObjectWriteResponse.class);

            when(file.getOriginalFilename()).thenReturn(null);
            when(file.getInputStream()).thenReturn(inputStream);
            when(file.getSize()).thenReturn(FILE_SIZE);
            when(file.getContentType()).thenReturn(CONTENT_TYPE);

            when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
            when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(mockResponse);

            // Act
            String result = minioService.uploadFile(file);

            // Assert
            assertThat(result).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}_null");
        }

        @Test
        void shouldThrowExceptionWhenUploadFails() throws Exception {
            // Arrange
            InputStream inputStream = new ByteArrayInputStream("test content".getBytes());

            when(file.getOriginalFilename()).thenReturn(FILE_NAME);
            when(file.getInputStream()).thenReturn(inputStream);
            when(file.getSize()).thenReturn(FILE_SIZE);
            when(file.getContentType()).thenReturn(CONTENT_TYPE);

            when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
            when(minioClient.putObject(any(PutObjectArgs.class)))
                    .thenThrow(new RuntimeException("Upload failed"));

            // Act & Assert
            assertThatThrownBy(() -> minioService.uploadFile(file))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessageContaining("Failed to upload file");
        }
    }

    @Nested
    class DownloadFileTests {

        @Test
        void shouldDownloadFileSuccessfully() throws Exception {
            // Arrange
            String objectKey = "test-key";

            GetObjectResponse mockResponse = mock(GetObjectResponse.class);
            when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(mockResponse);

            // Act
            InputStream result = minioService.downloadFile(objectKey);

            // Assert
            assertThat(result).isNotNull();

            verify(minioClient).getObject(getObjectArgsCaptor.capture());
            GetObjectArgs capturedArgs = getObjectArgsCaptor.getValue();

            assertThat(capturedArgs.bucket()).isEqualTo(BUCKET_NAME);
            assertThat(capturedArgs.object()).isEqualTo(objectKey);
        }

        @Test
        void shouldThrowExceptionWhenDownloadFails() throws Exception {
            // Arrange
            String objectKey = "test-key";

            when(minioClient.getObject(any(GetObjectArgs.class)))
                    .thenThrow(new RuntimeException("Download failed"));

            // Act & Assert
            assertThatThrownBy(() -> minioService.downloadFile(objectKey))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessageContaining("Failed to download file");
        }

        @Test
        void shouldHandleMinioSpecificException() throws Exception {
            // Arrange
            String objectKey = "test-key";

            // ErrorResponseException имеет конструктор с 3 параметрами в новых версиях
            ErrorResponseException mockException = mock(ErrorResponseException.class);
            when(minioClient.getObject(any(GetObjectArgs.class)))
                    .thenThrow(mockException);

            // Act & Assert
            assertThatThrownBy(() -> minioService.downloadFile(objectKey))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessageContaining("Failed to download file");
        }

        @Test
        void shouldHandleUnknownHostException() throws Exception {
            // Arrange
            String objectKey = "test-key";

            when(minioClient.getObject(any(GetObjectArgs.class)))
                    .thenThrow(new UnknownHostException("Unknown host"));

            // Act & Assert
            assertThatThrownBy(() -> minioService.downloadFile(objectKey))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessageContaining("Failed to download file");
        }
    }

    @Nested
    class DeleteFileTests {

        @Test
        void shouldDeleteFileSuccessfully() throws Exception {
            // Arrange
            String objectKey = "test-key";
            doNothing().when(minioClient).removeObject(any(RemoveObjectArgs.class));

            // Act
            minioService.deleteFile(objectKey);

            // Assert
            verify(minioClient).removeObject(removeObjectArgsCaptor.capture());
            RemoveObjectArgs capturedArgs = removeObjectArgsCaptor.getValue();

            assertThat(capturedArgs.bucket()).isEqualTo(BUCKET_NAME);
            assertThat(capturedArgs.object()).isEqualTo(objectKey);
        }

        @Test
        void shouldThrowExceptionWhenDeleteFails() throws Exception {
            // Arrange
            String objectKey = "test-key";
            doThrow(new RuntimeException("Delete failed"))
                    .when(minioClient).removeObject(any(RemoveObjectArgs.class));

            // Act & Assert
            assertThatThrownBy(() -> minioService.deleteFile(objectKey))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessageContaining("Failed to delete file");
        }
    }

    @Nested
    class GeneratePresignedUrlTests {

        @Test
        void shouldGeneratePresignedUrlSuccessfully() throws Exception {
            // Arrange
            String objectKey = "test-key";
            int durationMs = 15 * 60 * 1000;
            String expectedUrl = "http://minio/test-bucket/test-key?token=123";

            when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                    .thenReturn(expectedUrl);

            // Act
            String result = minioService.generatePresignedUrl(objectKey, durationMs);

            // Assert
            assertThat(result).isEqualTo(expectedUrl);

            verify(minioClient).getPresignedObjectUrl(presignedUrlArgsCaptor.capture());
            GetPresignedObjectUrlArgs capturedArgs = presignedUrlArgsCaptor.getValue();

            assertThat(capturedArgs.bucket()).isEqualTo(BUCKET_NAME);
            assertThat(capturedArgs.object()).isEqualTo(objectKey);
            assertThat(capturedArgs.method()).isEqualTo(Method.GET);
            assertThat(capturedArgs.expiry()).isEqualTo(durationMs / 1000);
        }

        @Test
        void shouldThrowExceptionWhenUrlGenerationFails() throws Exception {
            // Arrange
            String objectKey = "test-key";
            int durationMs = 15 * 60 * 1000;

            when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                    .thenThrow(new RuntimeException("URL generation failed"));

            // Act & Assert
            assertThatThrownBy(() -> minioService.generatePresignedUrl(objectKey, durationMs))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessageContaining("Failed to generate presigned URL");
        }
    }

    @Nested
    class PrivateMethodBehaviorTests {

        @Test
        void shouldGenerateUniqueObjectKeysForSameFile() throws Exception {
            // Arrange
            InputStream inputStream = new ByteArrayInputStream("test content".getBytes());
            ObjectWriteResponse mockResponse = mock(ObjectWriteResponse.class);

            when(file.getOriginalFilename()).thenReturn(FILE_NAME);
            when(file.getInputStream()).thenReturn(inputStream);
            when(file.getSize()).thenReturn(FILE_SIZE);
            when(file.getContentType()).thenReturn(CONTENT_TYPE);

            when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
            when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(mockResponse);

            // Act
            String result1 = minioService.uploadFile(file);
            String result2 = minioService.uploadFile(file);

            // Assert
            assertThat(result1).isNotEqualTo(result2);
            assertThat(result1).endsWith(FILE_NAME);
            assertThat(result2).endsWith(FILE_NAME);
        }
    }
}