package com.hits.language_school_back.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface MinioService {
    String uploadFile(MultipartFile file);

    InputStream downloadFile(String objectKey);

    void deleteFile(String objectKey);

    String generatePresignedUrl(String objectKey, int durationMs);
}