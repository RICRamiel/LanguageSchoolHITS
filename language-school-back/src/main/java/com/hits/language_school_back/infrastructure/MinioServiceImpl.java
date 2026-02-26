package com.hits.language_school_back.infrastructure;

import com.hits.language_school_back.config.MinioConfig;
import com.hits.language_school_back.exception.FileStorageException;
import com.hits.language_school_back.service.MinioService;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioServiceImpl implements MinioService {
    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    public String uploadFile(MultipartFile file) {
        try {
            String objectKey = generateObjectKey(file.getOriginalFilename());

            createBucketIfNotExists();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfig.getBucket())
                            .object(objectKey)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            log.info("File uploaded to MinIO: {}", objectKey);
            return objectKey;

        } catch (Exception e) {
            log.error("Failed to upload file to MinIO", e);
            throw new FileStorageException("Failed to upload file: " + e.getMessage());
        }
    }

    public InputStream downloadFile(String objectKey) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioConfig.getBucket())
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to download file from MinIO", e);
            throw new FileStorageException("Failed to download file: " + e.getMessage());
        }
    }

    public void deleteFile(String objectKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioConfig.getBucket())
                            .object(objectKey)
                            .build()
            );
            log.info("File deleted from MinIO: {}", objectKey);
        } catch (Exception e) {
            log.error("Failed to delete file from MinIO", e);
            throw new FileStorageException("Failed to delete file: " + e.getMessage());
        }
    }

    public String generatePresignedUrl(String objectKey, int durationMs) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(minioConfig.getBucket())
                            .object(objectKey)
                            .method(Method.GET)
                            .expiry(durationMs, TimeUnit.MILLISECONDS)
                            .build()
            );
        } catch (Exception e) {
            throw new FileStorageException("Failed to generate presigned URL", e);
        }
    }

    private void createBucketIfNotExists() throws Exception {
        boolean found = minioClient.bucketExists(
                BucketExistsArgs.builder()
                        .bucket(minioConfig.getBucket())
                        .build()
        );

        if (!found) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(minioConfig.getBucket())
                            .build()
            );
            log.info("Bucket created: {}", minioConfig.getBucket());
        }
    }

    private String generateObjectKey(String originalFilename) {
        return UUID.randomUUID() + "_" + originalFilename;
    }
}