package com.hits.language_school_back.infrastructure;

import com.hits.language_school_back.service.AttachmentService;
import com.hits.language_school_back.model.Attachment;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AttachmentServiceImpl implements AttachmentService {
    @Override
    public Attachment uploadAttachment(Long taskId, MultipartFile file) {
        return null;
    }

    @Override
    public void deleteAttachment(Long attachmentId) {

    }

    @Override
    public String getDownloadLink(Long attachmentId) {
        return "";
    }
}
