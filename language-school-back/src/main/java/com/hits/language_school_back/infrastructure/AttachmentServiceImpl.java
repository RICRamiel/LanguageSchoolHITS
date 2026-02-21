package com.hits.language_school_back.infrastructure;

import com.hits.language_school_back.config.MinioConfig;
import com.hits.language_school_back.model.Attachment;
import com.hits.language_school_back.repository.AttachmentRepository;
import com.hits.language_school_back.repository.TaskRepository;
import com.hits.language_school_back.service.AttachmentService;
import com.hits.language_school_back.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {
    private final AttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;
    private final MinioService minioService;
    private final MinioConfig minioConfig;

    public Attachment uploadAttachment(Long taskId, MultipartFile file) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void deleteAttachment(Long attachmentId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getDownloadLink(Long attachmentId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}