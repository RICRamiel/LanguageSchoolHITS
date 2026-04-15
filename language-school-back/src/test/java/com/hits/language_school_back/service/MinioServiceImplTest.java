package com.hits.language_school_back.service;

import com.hits.language_school_back.config.MinioConfig;
import com.hits.language_school_back.exception.FileStorageException;
import com.hits.language_school_back.infrastructure.MinioServiceImpl;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import org.junit.jupiter.api.BeforeEach;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MinioServiceImplTest {

    @Mock
    private MinioClient minioClient;
    @Mock
    private MinioConfig minioConfig;
    @Mock
    private MultipartFile file;

    @InjectMocks
    private MinioServiceImpl minioService;

    @Captor
    private ArgumentCaptor<PutObjectArgs> putCaptor;
    @Captor
    private ArgumentCaptor<GetObjectArgs> getCaptor;
    @Captor
    private ArgumentCaptor<GetPresignedObjectUrlArgs> presignedCaptor;
    @Captor
    private ArgumentCaptor<MakeBucketArgs> makeBucketCaptor;

    @BeforeEach
    void setUp() {
        when(minioConfig.getBucket()).thenReturn("bucket");
    }

    @Test
    void uploadFile_putsObjectAndReturnsGeneratedKey() throws Exception {
        InputStream stream = new ByteArrayInputStream("hello".getBytes());
        when(file.getOriginalFilename()).thenReturn("test.txt");
        when(file.getInputStream()).thenReturn(stream);
        when(file.getSize()).thenReturn(5L);
        when(file.getContentType()).thenReturn("text/plain");
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(org.mockito.Mockito.mock(ObjectWriteResponse.class));

        String result = minioService.uploadFile(file);

        assertThat(result).endsWith("_test.txt");
        verify(minioClient).putObject(putCaptor.capture());
        assertThat(putCaptor.getValue().bucket()).isEqualTo("bucket");
        assertThat(putCaptor.getValue().contentType()).isEqualTo("text/plain");
    }

    @Test
    void uploadFile_whenBucketMissing_createsBucket() throws Exception {
        when(file.getOriginalFilename()).thenReturn("test.txt");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("hello".getBytes()));
        when(file.getSize()).thenReturn(5L);
        when(file.getContentType()).thenReturn("text/plain");
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(org.mockito.Mockito.mock(ObjectWriteResponse.class));

        minioService.uploadFile(file);

        verify(minioClient).makeBucket(makeBucketCaptor.capture());
        assertThat(makeBucketCaptor.getValue().bucket()).isEqualTo("bucket");
    }

    @Test
    void downloadFile_readsObjectFromConfiguredBucket() throws Exception {
        GetObjectResponse response = org.mockito.Mockito.mock(GetObjectResponse.class);
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(response);

        InputStream result = minioService.downloadFile("object-key");

        assertThat(result).isSameAs(response);
        verify(minioClient).getObject(getCaptor.capture());
        assertThat(getCaptor.getValue().bucket()).isEqualTo("bucket");
        assertThat(getCaptor.getValue().object()).isEqualTo("object-key");
    }

    @Test
    void deleteFile_whenClientFails_wrapsException() throws Exception {
        doThrow(new RuntimeException("boom")).when(minioClient).removeObject(any(RemoveObjectArgs.class));

        assertThatThrownBy(() -> minioService.deleteFile("object-key"))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("Failed to delete file");
    }

    @Test
    void generatePresignedUrl_usesGetMethodAndConfiguredBucket() throws Exception {
        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class))).thenReturn("http://signed");

        String result = minioService.generatePresignedUrl("object-key", 15 * 60 * 1000);

        assertThat(result).isEqualTo("http://signed");
        verify(minioClient).getPresignedObjectUrl(presignedCaptor.capture());
        assertThat(presignedCaptor.getValue().bucket()).isEqualTo("bucket");
        assertThat(presignedCaptor.getValue().object()).isEqualTo("object-key");
        assertThat(presignedCaptor.getValue().method()).isEqualTo(Method.GET);
    }
}
